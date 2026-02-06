package backend.courseshare.service;

import backend.courseshare.entity.RefreshToken;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${app.jwt.refresh.expiration}")
    private long refreshTokenDurationMs;

    @Value("${app.auth.max.sessions:3}")
    private int maxSessions;

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public RefreshToken create(Users user, String ip, String device) {
      //  repo.deleteByUser(user);
        long activeSessions = repo.countByUserAndRevokedFalse(user);

        if(activeSessions >= maxSessions) {
            List<RefreshToken> tokens = repo.findByUserAndRevokedFalse(user);
            tokens.sort(Comparator.comparing(RefreshToken::getCreatedAt));
            RefreshToken oldest = tokens.get(0);
            oldest.setRevoked(true);
            repo.save(oldest);
        }

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setRevoked(false);
        token.setCreatedAt(Instant.now());
        token.setDevice(device);
        token.setIpAddress(ip);

        return repo.save(token);
    }

    public RefreshToken validate(String token) {

        RefreshToken rt = repo.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        //REUSE DETECTED
        if (rt.isRevoked()) {
            // Kill all refresh tokens for this user
            repo.deleteAllByUser(rt.getUser());

            throw new RuntimeException("Refresh token reuse detected. All sessions revoked.");
        }

        // Normal expiration
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            rt.setRevoked(true);
            repo.save(rt);
            throw new RuntimeException("Refresh token expired");
        }

        return rt;
    }


    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repo.save(token);
    }

    public void revokeByToken(String token) {
        repo.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }
}
