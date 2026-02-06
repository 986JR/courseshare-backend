package backend.courseshare.service;

import backend.courseshare.dto.enrollment.CreateEnrollmentRequest;
import backend.courseshare.dto.enrollment.EnrollmentResponse;
import backend.courseshare.entity.Course;
import backend.courseshare.entity.Enrollment;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.EnrollmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepo;
    private final Userservice userservice;
    private final CourseService courseService;

    public EnrollmentService(EnrollmentRepository enrollmentRepo,
                             Userservice userservice,
                             CourseService courseService) {
        this.enrollmentRepo = enrollmentRepo;
        this.userservice = userservice;
        this.courseService = courseService;
    }

    private String generateEnrollmentPublicId() {
        return "EN" +java.time.Year.now().toString().substring(2) + "-" + userservice.randomPart(4);
    }

    @Transactional
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest req, Users actingUser, boolean allowAdminCreateForOthers) {
        String courseCode = req.courseCode().trim();
        String userPublicId = req.userPublicId() == null ? null : req.userPublicId().trim();

        Course course = courseService.findByCourseCodeOrThrow(courseCode);

        Users targetUser;
        if(userPublicId == null || userPublicId.isBlank() || (actingUser != null && userPublicId.equals(actingUser.getPublicId()))) {
            if(actingUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
            targetUser = actingUser;
        }
        else {
            if(!userservice.isAdmin(actingUser) && !allowAdminCreateForOthers) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not Allowed to enroll other users");
            }
            targetUser = userservice.findByPublicIdOrThrow(userPublicId);
        }

        //Prevent Duplicaates
        if(enrollmentRepo.existsByUser_IdAndCourse_Id(targetUser.getId(),course.getId())) {
            throw  new ResponseStatusException(HttpStatus.CONFLICT,"User Already enrolled in this course");
        }

        //create
        String publicId = generateUniqueEnrollmentPublicId();
        Enrollment e = new Enrollment(publicId,targetUser,course);
        Enrollment saved = enrollmentRepo.save(e);

        return toDto(saved);
    }

    public EnrollmentResponse toDto(Enrollment e) {
        return new EnrollmentResponse(
                e.getPublicId(),
                e.getUser().getPublicId(),
                e.getCourse().getCourseCode(),
                e.getEnrolledAt()
        );
    }

    private String generateUniqueEnrollmentPublicId() {
        for(int i=0; i<10; i++) {
            String candidate = generateEnrollmentPublicId();
            if(!enrollmentRepo.findByPublicId(candidate).isPresent()) return candidate;
        }
        throw new IllegalStateException("Unable to genarate unique enrollment public Id");
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listByUserPublicId(String userPublicId) {
        return enrollmentRepo.findByUser_PublicId(userPublicId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listAll() {
        return enrollmentRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse findByPublicIdOrThrow(String publicId) {
        Enrollment e = enrollmentRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not Found"));
        return toDto(e);
    }

    @Transactional
    public void deleteByPublicId(String publicId, Users actingUser) {
        Enrollment e = enrollmentRepo.findByPublicId(publicId)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Enrollment Found"));

        if(!e.getUser().getId().equals(actingUser.getId()) && !userservice.isAdmin(actingUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not Allowed To perfome This Action");
        }
        enrollmentRepo.delete(e);
    }


}
