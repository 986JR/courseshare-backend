package backend.courseshare.dto.auth;

import java.time.Instant;

public record RegisterResponseDTO(
        String publicId,
        String username,
        Instant createdAt
) {}
