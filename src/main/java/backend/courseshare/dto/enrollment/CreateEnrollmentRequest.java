package backend.courseshare.dto.enrollment;

import jakarta.validation.constraints.NotBlank;

public record CreateEnrollmentRequest(
        @NotBlank String userPublicId,
        @NotBlank String courseCode) {

}
