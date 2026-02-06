package backend.courseshare.controller;

import backend.courseshare.dto.enrollment.CreateEnrollmentRequest;
import backend.courseshare.dto.enrollment.EnrollmentResponse;
import backend.courseshare.entity.Enrollment;
import backend.courseshare.entity.Users;
import backend.courseshare.security.CustomUserDetails;
import backend.courseshare.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    //Create enrollemnt
    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody CreateEnrollmentRequest req, @AuthenticationPrincipal CustomUserDetails principal) {
        Users acting = principal == null ? null : principal.getUser();
        var created = enrollmentService.createEnrollment(req,acting,true);
        URI location = URI.create("/api/enrollments/" + java.net.URLEncoder.encode(created.publicId(), StandardCharsets.UTF_8));
        return ResponseEntity.created(location).body(created);
    }

    //List my enrollments
    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments(@AuthenticationPrincipal CustomUserDetails principal) {
        if(principal == null ) return ResponseEntity.status(401).build();

        var list = enrollmentService.listByUserPublicId(principal.getPublicId());
        return ResponseEntity.ok(list);
    }

    //Admin: List All
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> listAll() {
        return ResponseEntity.ok(enrollmentService.listAll());
    }

    //Get single Enrollment By PublicId
    @GetMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> get(@PathVariable String publicId, @AuthenticationPrincipal CustomUserDetails principal) {
        var out = enrollmentService.findByPublicIdOrThrow(publicId);
        return ResponseEntity.ok(out);
    }

    //Deleting, but only owner or Admin
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> withdraw(@PathVariable String publicId , @AuthenticationPrincipal CustomUserDetails principal) {
    if(principal == null) return ResponseEntity.status(401).build();
    enrollmentService.deleteByPublicId(publicId, principal.getUser());
    return ResponseEntity.noContent().build();
    }
}