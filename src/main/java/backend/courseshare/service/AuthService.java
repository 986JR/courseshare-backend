package backend.courseshare.service;


import backend.courseshare.dto.auth.*;
import backend.courseshare.entity.RefreshToken;
import backend.courseshare.entity.Users;
import backend.courseshare.jwt.JwtUtil;
import backend.courseshare.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final Userservice userservice;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final BlacklistService blacklistService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public AuthService(Userservice userservice,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil, BlacklistService blacklistService,
                       RefreshTokenService refreshTokenService,
                       EmailService emailService) {
        this.userservice = userservice;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.blacklistService=blacklistService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    //Am connecting the dto to users entity
    public RegisterResponseDTO register(RegisterRequestDTO dto) {

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword());

        Users created = userservice.createUser(user);


        emailService.sendEmail(created.getEmail(),"Welcome To CourseShare","Thank You for Joining CourseShare," +
                "\nKeep Sharing and recommend our system to your friends\n\n check us on WhatsApp at https://wa.me/765681723" +
                "\n ShareMind "+created.getUsername());
        return new RegisterResponseDTO(
                created.getPublicId(),
                created.getUsername(),
                created.getCreated_At()
        );

    }

    public AuthTokens login(LoginRequestDTO dto, String device, String ipAddress) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        Users user = cud.getUser();

        String accessToken = jwtUtil.generateToken(user.getUsername());

        RefreshToken refreshToken =
                refreshTokenService.create(user, device, ipAddress);

        return new AuthTokens(
                accessToken,
                refreshToken.getToken(),
                user.getPublicId()
        );
    }



    public void logout(String rawAccessToken, String refreshTokenValue) {

        // blacklist access token
        if (rawAccessToken != null && !rawAccessToken.isBlank()) {
            blacklistService.blacklistToken(rawAccessToken);
        }

        // revoke refresh token
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenService.revokeByToken(refreshTokenValue);
        }
    }


    public LoginResponseDTO refresh(RefreshTokenRequestDTO dto) {

        RefreshToken refreshToken = refreshTokenService.validate(dto.getRefreshToken());

        Users user = refreshToken.getUser();

        // generate new access token
        String newAccessToken = jwtUtil.generateToken(user.getUsername());

        return new LoginResponseDTO(
                newAccessToken,
                "Bearer",
                user.getPublicId()
        );
    }


}
