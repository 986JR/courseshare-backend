package backend.courseshare.service;

import backend.courseshare.entity.JwtBlacklistedToken;
import backend.courseshare.jwt.JwtUtil;
import backend.courseshare.repository.BlacklistedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BlacklistService {
    private final BlacklistedTokenRepository repo;
    private final JwtUtil jwtUtil;

    public BlacklistService(BlacklistedTokenRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void blacklistToken(String token) {
        if(token == null || token.isBlank())  return;

        if(token.startsWith("Bearer ")) token = token.substring(7);

        if(repo.existsByToken(token)) return;

        Instant expiry = jwtUtil.getExpiration(token);
        if(expiry == null) {
            expiry = Instant.now().plusSeconds(60*5);
        }

        JwtBlacklistedToken bt = new JwtBlacklistedToken(token,expiry);
        repo.save(bt);
    }

    public boolean isBlacklisted(String token) {
        if(token == null || token.isBlank()) return false;
        if(token.startsWith("Bearer ")) token = token.substring(7);
        return repo.existsByToken(token);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpired() {
        repo.deleteByExpiryAtBefore(Instant.now());
    }


}
