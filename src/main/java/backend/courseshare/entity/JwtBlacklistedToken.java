package backend.courseshare.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "jwt_blacklisted_tokens", indexes = {
        @Index(name = "idx_jbt_token", columnList = "token", unique = true),
        @Index(name = "idx_jbt_expiry", columnList = "expiry_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtBlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000, unique = true)
    private String token;

    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    public JwtBlacklistedToken(String token, Instant expiryAt) {
        this.token = token;
        this.expiryAt = expiryAt;
    }

}
