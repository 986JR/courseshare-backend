package backend.courseshare.controller;


import backend.courseshare.dto.auth.*;
import backend.courseshare.entity.RefreshToken;
import backend.courseshare.entity.Users;
import backend.courseshare.jwt.JwtUtil;
import backend.courseshare.service.AuthService;
import backend.courseshare.service.OtpService;
import backend.courseshare.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService,
                          JwtUtil jwtUtil, OtpService otpService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
    }

    /*
                   REGISTER
                 */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @RequestBody RegisterRequestDTO dto
    ) {
        RegisterResponseDTO created = authService.register(dto);
        URI location = URI.create("/api/users/" + created.publicId());
        return ResponseEntity.created(location).body(created);
    }

    /* =========================
       LOGIN (ACCESS + REFRESH)
    ========================= */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        //Extract device (User-Agent)
        String device = request.getHeader("User-Agent");

        //Extract IP address (proxy-safe)
        String ipAddress = extractClientIp(request);

        //Authenticate
        AuthTokens tokens = authService.login(dto, device, ipAddress);

        //Set refresh token cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(false)               // true in production (HTTPS)
                .path("/") // MUST start with /  i did this first  api/auth/refresh
                .sameSite("Lax")
                .maxAge(Duration.ofDays(2))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        //Return access token
        return ResponseEntity.ok(
                new LoginResponseDTO(
                        tokens.accessToken(),
                        "Bearer",
                        tokens.publicId()
                )
        );
    }


    /* =========================
       REFRESH TOKEN
    ========================= */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. Validate old refresh token
        RefreshToken oldToken = refreshTokenService.validate(refreshToken);
        Users user = oldToken.getUser();

        // 2. Rotate: revoke old token
        refreshTokenService.revoke(oldToken);

        // 3. Extract device + IP
        String device = request.getHeader("User-Agent");
        String ipAddress = extractClientIp(request);

        // 4. Create new refresh token
        RefreshToken newToken = refreshTokenService.create(user, device, ipAddress);

        // 5. Set cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newToken.getToken())
                .httpOnly(true)
                .secure(false) // true in prod (HTTPS)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(2))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 6. Issue new access token
        String accessToken = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(
                new LoginResponseDTO(accessToken, "Bearer", user.getPublicId())
        );
    }


    /* =========================
       LOGOUT
    ========================= */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response, LogoutRequestDTO dto) {

        if (refreshToken != null) {
            refreshTokenService.revokeByToken(refreshToken);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();
authService.logout(dto.getToken(),refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok().build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /*
    Reseting A Password
    */


    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email) throws Exception {
        otpService.sendsOtp(email);
        return "OTP sent to email";
    }

    @PostMapping("/validate-otp")
    public String validateOtp(@RequestParam String otp) throws Exception {
        otpService.validateOtp(otp);
        return "OTP is valid!";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String otp, @RequestParam String newPassword) throws Exception {
        otpService.resetPassword(otp,newPassword);
        return "Password Reseted";
    }




}



