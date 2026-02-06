package backend.courseshare.repository;

import backend.courseshare.entity.RefreshToken;
import backend.courseshare.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
    void deleteByUser(Users user);
    void deleteAllByUser(Users user);
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalse(Users user);

    long countByUserAndRevokedFalse(Users user);

    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.revoked=true where rt.user = :user")
    void revokeAllByUser(@Param("user") Users user);

}
