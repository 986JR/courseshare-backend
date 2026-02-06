package backend.courseshare.dto.course;

import java.time.Instant;
// for lists
public record CourseSummaryResponse(
        String courseCode,
        String title,
        String categorySlug,
        String categoryName,
        String createdByPublicId,
        Instant createdAt
) {}
