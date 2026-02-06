package backend.courseshare.controller;

import backend.courseshare.dto.course.CreateCourseRequest;
import backend.courseshare.dto.course.CourseDetailResponse;
import backend.courseshare.dto.course.CourseSummaryResponse;
import backend.courseshare.dto.course.PatchCourseRequest;
import backend.courseshare.dto.course.UpdateCourseRequest;
import backend.courseshare.entity.Course;
import backend.courseshare.entity.Users;
import backend.courseshare.security.CustomUserDetails;
import backend.courseshare.service.CourseService;
import backend.courseshare.service.Userservice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@Validated
public class CourseController {

    private final CourseService courseService;
    private final Userservice userservice;

    public CourseController(CourseService courseService, Userservice userservice) {
        this.courseService = courseService;
        this.userservice = userservice;
    }

    /**
     * POST /api/courses
     * Create a course. Authenticated users can create.
     */
    @PostMapping
    public ResponseEntity<CourseDetailResponse> create(
            @Valid @RequestBody CreateCourseRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal,
            HttpServletRequest request
    ) {

        System.out.println(">>> Course POST hit: URI=" + request.getRequestURI());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(">>> SecurityContext auth: " + auth);
        if (auth != null) {
            System.out.println(">>> Principal class: " + auth.getPrincipal().getClass().getName());
            System.out.println(">>> Authorities: " + auth.getAuthorities());
        }
        System.out.println(">>> Content-Type header: " + request.getHeader(HttpHeaders.CONTENT_TYPE));

        // get Users entity from principal
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Users creator = principal.getUser();

        Course created = courseService.createCourse(dto, creator);
        CourseDetailResponse body = toDetail(created);
        URI location = UriComponentsBuilder.fromPath("/api/courses/{code}")
                .buildAndExpand(created.getCourseCode())
                .encode()            // important: encodes reserved/illegal chars
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    /**
     * GET /api/courses?page=0&size=20
     * Public listing (paged).
     */
    @GetMapping
    public ResponseEntity<List<CourseSummaryResponse>> list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Page<Course> p = courseService.listCourses(PageRequest.of(Math.max(0, page), Math.max(1, size)));
        List<CourseSummaryResponse> out = p.stream().map(this::toSummary).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    /**
     * GET /api/courses/{courseCode}
     * Get course detail by course code.
     */
    @GetMapping("/{courseCode}")
    public ResponseEntity<CourseDetailResponse> get(@PathVariable String courseCode) {
        Course c = courseService.findByCourseCodeOrThrow(courseCode);
        return ResponseEntity.ok(toDetail(c));
    }

    /**
     * PUT /api/courses/{courseCode}
     * Full update. Protect with owner-or-admin authorization.
     */
    @PutMapping("/{courseCode}")
    @PreAuthorize("@ownershipEvaluator.isCourseOwner(#courseCode, principal)")
    public ResponseEntity<CourseDetailResponse> put(
            @PathVariable String courseCode,
            @Valid @RequestBody UpdateCourseRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) return ResponseEntity.status(401).build();
        Users actor = principal.getUser();
        Course updated = courseService.updateCourse(courseCode, dto, actor);
        return ResponseEntity.ok(toDetail(updated));
    }

    /**
     * PATCH /api/courses/{courseCode}
     * Partial update. Protect with owner-or-admin authorization (same as PUT).
     */
    @PatchMapping("/{courseCode}")
    @PreAuthorize("@ownershipEvaluator.isCourseOwner(#courseCode, principal)")
    public ResponseEntity<CourseDetailResponse> patch(
            @PathVariable String courseCode,
            @RequestBody PatchCourseRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) return ResponseEntity.status(401).build();
        Users actor = principal.getUser();
        Course patched = courseService.patchCourse(courseCode, dto, actor);
        return ResponseEntity.ok(toDetail(patched));
    }

    /**
     * DELETE /api/courses/{courseCode}
     * Delete a course. Protect with owner-or-admin authorization.

     */
    @DeleteMapping("/{courseCode}")
    @PreAuthorize("@ownershipEvaluator.isCourseOwner(#courseCode, principal)")
    public ResponseEntity<Void> delete(
            @PathVariable String courseCode,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        // optionally enforce authorization here or via @PreAuthorize
        courseService.deleteByCourseCode(courseCode);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- Mapping helpers ---------------- */

    private CourseDetailResponse toDetail(Course c) {
        String catSlug = c.getCategory() != null ? c.getCategory().getSlug() : null;
        String catName = c.getCategory() != null ? c.getCategory().getName() : null;
        String createdByPublicId = c.getCreatedBy() != null ? c.getCreatedBy().getPublicId() : null;
        Instant createdAt = c.getCreatedAt();
        return new CourseDetailResponse(c.getCourseCode(), c.getTitle(), c.getDescription(), catSlug, catName, createdByPublicId, createdAt);
    }

    private CourseSummaryResponse toSummary(Course c) {
        String catSlug = c.getCategory() != null ? c.getCategory().getSlug() : null;
        String catName = c.getCategory() != null ? c.getCategory().getName() : null;
        String createdByPublicId = c.getCreatedBy() != null ? c.getCreatedBy().getPublicId() : null;
        Instant createdAt = c.getCreatedAt();
        return new CourseSummaryResponse(c.getCourseCode(), c.getTitle(), catSlug, catName, createdByPublicId, createdAt);
    }
}
