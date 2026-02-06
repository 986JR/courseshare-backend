package backend.courseshare.dto.category;

import java.time.Instant;
public record CategoryResponse(
        String slug,
        String name,
        String description,
        Instant createdAt
) {}
