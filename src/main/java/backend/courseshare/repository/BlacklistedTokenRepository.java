package backend.courseshare.repository;

import backend.courseshare.entity.JwtBlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<JwtBlacklistedToken, Long> {

    boolean existsByToken(String token);
    Optional<JwtBlacklistedToken> findByToken(String token);
    void deleteByExpiryAtBefore(Instant cutoff);
}
