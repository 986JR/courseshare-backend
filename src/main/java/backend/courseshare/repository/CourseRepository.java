package backend.courseshare.repository;

import backend.courseshare.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {


    @Query(value = """
        SELECT COALESCE(MAX(CAST(SUBSTRING(course_code FROM 4 FOR 3) AS INTEGER)), 0)
        FROM courses
        WHERE course_code LIKE :prefix || '%'
        """, nativeQuery = true)
    int findMaxSequenceForPrefix(@Param("prefix") String prefix);

    // Optional: direct find by courseCode
    boolean existsByCourseCode(String courseCode);

    Optional<Course> findByCourseCode(String courseCode);
}
