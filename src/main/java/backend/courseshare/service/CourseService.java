package backend.courseshare.service;

import backend.courseshare.dto.course.CreateCourseRequest;
import backend.courseshare.dto.course.PatchCourseRequest;
import backend.courseshare.dto.course.UpdateCourseRequest;
import backend.courseshare.entity.Category;
import backend.courseshare.entity.Course;
import backend.courseshare.entity.CourseCodeCounter;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.CourseCodeCounterRepository;
import backend.courseshare.repository.CourseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepo;
    private final CourseCodeCounterRepository counterRepo;
    private final CategoryService categoryService;

    public CourseService(CourseRepository courseRepo,
                         CourseCodeCounterRepository counterRepo,
                         CategoryService categoryService) {
        this.courseRepo = courseRepo;
        this.counterRepo = counterRepo;
        this.categoryService = categoryService;
    }

    /* ---------------- Public Create (single entrypoint) ---------------- */

    @Transactional
    public Course createCourse(CreateCourseRequest dto, Users creator) {

        System.out.println("CourseService.createCourse called. dto=" + dto + " creator=" + (creator == null ? null : creator.getUsername()));

        if (creator == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Creator is required");
        }
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body required");
        }

        String title = dto.title();
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        String description = dto.description();

        // Resolve category (prefer slug, else name)
        Category category;
        if (dto.categorySlug() != null && !dto.categorySlug().isBlank()) {
            category = categoryService.findBySlugOrThrow(dto.categorySlug());
        } else if (dto.categoryName() != null && !dto.categoryName().isBlank()) {
            category = categoryService.resolveOrCreateCategory(dto.categoryName());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category slug or name is required");
        }

        // If client provided courseCode: validate uniqueness (recommend admin-only)
        if (dto.courseCode() != null && !dto.courseCode().isBlank()) {
            String candidate = dto.courseCode().trim();
            if (courseRepo.existsByCourseCode(candidate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Course code already exists");
            }
            Course course = new Course(title.trim(), description == null ? null : description.trim(), creator, candidate, category);
            try {
                return courseRepo.save(course);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to save course: " + ex.getMessage(), ex);
            }
        }

        // Otherwise generate sequential code and save
        return generateSequentialCourse(title.trim(), description == null ? null : description.trim(), creator, category);
    }

    /* ---------------- Private sequential generator helper ---------------- */

    /**
     * Generates and persists a Course with a sequential code using the CourseCodeCounter table.
     * This method encapsulates all counter locking & retry logic.
     */
    @Transactional
    private Course generateSequentialCourse(String title, String description, Users creator, Category category) {
        if (category == null) throw new IllegalArgumentException("Category is required");

        String prefix = buildPrefixFromCategory(category.getName());
        final int MAX_ATTEMPTS = 5;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            Optional<CourseCodeCounter> maybe = counterRepo.findByPrefixForUpdate(prefix);

            CourseCodeCounter counter;
            if (maybe.isPresent()) {
                counter = maybe.get();

                // increment and persist
                int next = counter.getLastNumber() + 1;
                if (next > 999) {
                    throw new IllegalStateException("Course code overflow for prefix: " + prefix);
                }
                counter.setLastNumber(next);
                counterRepo.saveAndFlush(counter);

                String number = String.format("%03d", next);
                String code = prefix + "-" + number;
                Course course = new Course(title, description, creator, code, category);

                try {
                    return courseRepo.save(course);
                } catch (DataIntegrityViolationException ex) {
                    if (attempt == MAX_ATTEMPTS)
                        throw new IllegalStateException("Failed to save course after retries for prefix: " + prefix, ex);
                    continue;
                }

            } else {

                try {
                    counter = counterRepo.saveAndFlush(new CourseCodeCounter(prefix, 1));
                    String number = String.format("%03d", 1);
                    String code = prefix + " " + number;
                    Course course = new Course(title, description, creator, code, category);
                    try {
                        return courseRepo.save(course);
                    } catch (DataIntegrityViolationException ex) {
                        if (attempt == MAX_ATTEMPTS)
                            throw new IllegalStateException("Failed to save course after retries for prefix: " + prefix, ex);
                        continue;
                    }
                } catch (DataIntegrityViolationException ex) {
                    if (attempt == MAX_ATTEMPTS)
                        throw new IllegalStateException("Failed to create or obtain counter for prefix: " + prefix, ex);
                    continue;
                }
            }
        }

        throw new IllegalStateException("Unable to generate course code for prefix: " + prefix);
    }

    /* ---------------- Helpers ---------------- */

    private String buildPrefixFromCategory(String name) {
        if (name == null || name.isBlank()) return "XX";
        String onlyLetters = name.replaceAll("[^A-Za-z]", "");
        onlyLetters = onlyLetters.toUpperCase(Locale.ROOT);
        return (onlyLetters + "XX").substring(0, 2);
    }

    /* ---------------- Read / List ---------------- */

    @Transactional(readOnly = true)
    public Page<Course> listCourses(Pageable pageable) {
        return courseRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Course findByCourseCodeOrThrow(String courseCode) {
        if (courseCode == null || courseCode.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseCode is required");
        return courseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    /* ---------------- Update (PUT) ---------------- */

    @Transactional
    public Course updateCourse(String courseCode, UpdateCourseRequest req, Users actingUser) {
        Course existing = findByCourseCodeOrThrow(courseCode);

        if (req.title() == null || req.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        existing.setTitle(req.title().trim());
        existing.setDescription(req.description() == null ? null : req.description().trim());

        // Category resolution
        Category category = null;
        if (req.categorySlug() != null && !req.categorySlug().isBlank()) {
            category = categoryService.findBySlugOrThrow(req.categorySlug());
        } else if (req.categoryName() != null && !req.categoryName().isBlank()) {
            category = categoryService.resolveOrCreateCategory(req.categoryName());
        }
        if (category != null) existing.setCategory(category);

        // Course code override (admin-only ideally)
        if (req.courseCode() != null && !req.courseCode().isBlank()) {
            String candidate = req.courseCode().trim();
            if (!candidate.equals(existing.getCourseCode()) && courseRepo.existsByCourseCode(candidate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Course code already exists");
            }
            existing.setCourseCode(candidate);
        }

        try {
            return courseRepo.save(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Constraint violation: " + ex.getMessage(), ex);
        }
    }

    /* ---------------- Patch ---------------- */

    @Transactional
    public Course patchCourse(String courseCode, PatchCourseRequest req, Users actingUser) {
        Course existing = findByCourseCodeOrThrow(courseCode);

        if (req.title() != null && !req.title().isBlank()) existing.setTitle(req.title().trim());
        if (req.description() != null) existing.setDescription(req.description().trim());

        if (req.categorySlug() != null && !req.categorySlug().isBlank()) {
            Category category = categoryService.findBySlugOrThrow(req.categorySlug());
            existing.setCategory(category);
        } else if (req.categoryName() != null && !req.categoryName().isBlank()) {
            Category category = categoryService.resolveOrCreateCategory(req.categoryName());
            existing.setCategory(category);
        }

        if (req.courseCode() != null && !req.courseCode().isBlank()) {
            String candidate = req.courseCode().trim();
            if (!candidate.equals(existing.getCourseCode()) && courseRepo.existsByCourseCode(candidate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Course code already exists");
            }
            existing.setCourseCode(candidate);
        }

        try {
            return courseRepo.save(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Constraint violation: " + ex.getMessage(), ex);
        }
    }

    /* ---------------- Delete ---------------- */

    @Transactional
    public void deleteByCourseCode(String courseCode) {
        Course existing = courseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        courseRepo.delete(existing);
    }
}
