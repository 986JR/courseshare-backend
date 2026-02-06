package backend.courseshare.repository;

import backend.courseshare.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByOtpAndExpiredFalse(String otp);
}
