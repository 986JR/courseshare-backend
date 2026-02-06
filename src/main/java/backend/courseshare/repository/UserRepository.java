package backend.courseshare.repository;

import backend.courseshare.dto.user.UserProfileDTO;
import backend.courseshare.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    boolean existsByPublicId(String publicId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByPublicId(String publicId);

    // Custom Aggregated User Profile Data
    @Query(value = """
        SELECT u.public_id AS publicId,
               u.username AS username,
               u.email AS email,
               u.role AS role,

               -- uploads count
               (SELECT COUNT(*) FROM file_resources f WHERE f.uploaded_by = u.id) AS uploadsCount,

               -- enrollment count
               (SELECT COUNT(*) FROM enrollments e WHERE e.user_id = u.id) AS enrollmentsCount,

               -- average rating for all files uploaded by the user
               COALESCE((
                    SELECT AVG(r.rating) 
                    FROM file_resource_reviews r
                    JOIN file_resources f ON r.file_id = f.id
                    WHERE f.uploaded_by = u.id
               ),0) AS averageRating

        FROM users u
        WHERE u.public_id = :publicId
        """,
            nativeQuery = true)
    Optional<UserProfileDTO> getFullProfile(String publicId);

}
