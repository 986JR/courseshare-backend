package backend.courseshare.dto.auth;

import lombok.Data;

@Data
public class RefreshTokenResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";

    public RefreshTokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
