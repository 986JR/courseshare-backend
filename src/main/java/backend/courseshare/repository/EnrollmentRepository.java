package backend.courseshare.repository;

import backend.courseshare.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUser_IdAndCourse_Id(Long userId, Long courseId);
    Optional<Enrollment> findByPublicId(String publicId);
    List<Enrollment> findByUser_PublicId(String userPublicId);
    List<Enrollment> findByCourse_CourseCode(String courseCode);
    Optional<Enrollment> findByUser_IdAndCourse_Id(Long userId, Long courseId);
}
