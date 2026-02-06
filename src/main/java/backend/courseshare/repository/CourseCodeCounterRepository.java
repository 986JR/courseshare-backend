package backend.courseshare.repository;

import backend.courseshare.entity.CourseCodeCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CourseCodeCounterRepository extends JpaRepository<CourseCodeCounter, Long> {

    Optional<CourseCodeCounter> findByPrefix(String prefix);

    // Return the row with PESSIMISTIC_WRITE lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseCodeCounter c where c.prefix = :prefix")
    Optional<CourseCodeCounter> findByPrefixForUpdate(@Param("prefix") String prefix);
}
