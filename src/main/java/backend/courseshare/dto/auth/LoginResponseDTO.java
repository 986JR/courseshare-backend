package backend.courseshare.dto.auth;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String accessToken;
   // private String refreshToken;
    private String tokenType = "Bearer";
    private String publicId;

    public LoginResponseDTO(String accessToken, /*String refreshToken,*/ String tokenType, String publicId) {
        this.accessToken = accessToken;
       // this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.publicId = publicId;
    }
}
