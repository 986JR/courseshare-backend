package backend.courseshare.service;


import backend.courseshare.entity.OtpToken;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.OtpTokenRepository;
import backend.courseshare.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private  final EmailService emailService;

    public OtpService(OtpTokenRepository otpTokenRepository, UserRepository userRepository,
                      EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public String sendsOtp(String email) throws Exception {
        Users users = userRepository.findByEmail(email)
                .orElseThrow(()-> new Exception("User not Found"));

        String otp = generateOtp();
        OtpToken token = new  OtpToken();
        token.setOtp(otp);
        token.setUsers(users);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpTokenRepository.save(token);

        emailService.sendEmail(email,"Your OTP code From CourseShare","Your OTP is "+otp);

        return otp;
    }

    @Transactional
    public boolean validateOtp(String otp) throws Exception {
        OtpToken token = otpTokenRepository.findByOtpAndExpiredFalse(otp)
                .orElseThrow(()-> new Exception("Invalid or Expired OTP"));

        if(token.getExpiryTime().isBefore(LocalDateTime.now())) {
            token.setExpired(true);
            otpTokenRepository.save(token);
            throw  new Exception("Token Expired");

        }

//        token.setExpired(true);
//        otpTokenRepository.save(token);
        return true;
    }

    @Transactional
    public void resetPassword(String otp, String newPassword) {
        OtpToken token = otpTokenRepository.findByOtpAndExpiredFalse(otp)
                .orElseThrow(()-> new RuntimeException("Invalid OTP"));

        Users users = token.getUsers();
        String hashed;
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bcrypt =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        hashed = bcrypt.encode(newPassword);
        users.setPasswordHash(hashed);
        userRepository.save(users);

        token.setExpired(true);
        otpTokenRepository.save(token);

    }
}
