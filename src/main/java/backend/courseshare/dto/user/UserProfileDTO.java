package backend.courseshare.dto.user;

import java.math.BigDecimal;

public record UserProfileDTO(
        String publicId,
        String username,
        String email,
        String role,
        long uploadsCount,
        long enrollmentsCount,
        BigDecimal averageRating
) {}
