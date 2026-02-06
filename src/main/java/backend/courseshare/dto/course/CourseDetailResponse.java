package backend.courseshare.dto.course;

import java.time.Instant;

public record CourseDetailResponse(
        String courseCode,
        String title,
        String description,
        String categorySlug,
        String categoryName,
        String createdByPublicId,
        Instant createdAt
) {}
