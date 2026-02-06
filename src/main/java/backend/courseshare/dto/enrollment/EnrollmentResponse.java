package backend.courseshare.dto.enrollment;

import java.time.Instant;

public record EnrollmentResponse(
        String publicId,
        String userPublicId,
        String courseCode,
        Instant enrolledAt
) {
}
