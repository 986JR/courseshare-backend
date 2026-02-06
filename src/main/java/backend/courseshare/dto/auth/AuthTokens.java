package backend.courseshare.dto.auth;
public record AuthTokens(
        String accessToken,
        String refreshToken,
        String publicId
) {}

