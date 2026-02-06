package backend.courseshare.repository;

import backend.courseshare.entity.Enrollment;
import backend.courseshare.entity.FileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileResourceRepository extends JpaRepository<FileResource,Long> {
    Optional<FileResource> findByPublicId(String publicId);
    List<FileResource> findByCourse_Id(Long courseId);
   // List<FileResource> findByCourse_CourseCode(String courseCode);
   // @EntityGraph(attributePaths = "course")
    Page<FileResource> findByCourse_CourseCode(String courseCode, Pageable pageable);

    @Query("""
    SELECT fr FROM FileResource fr
    JOIN fr.course c
    WHERE LOWER(fr.filename) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(fr.description) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :query, '%'))
""")
    Page<FileResource> searchFiles(String query, Pageable pageable);
    //OR LOWER(fr.course.title) LIKE LOWER(CONCAT('%', :query, '%'))

    List<FileResource> findByUploadedBy_PublicIdOrderByUploadedAtDesc(String publicId);

    Page<FileResource> findByUploadedBy_PublicIdOrderByUploadedAtDesc(
            String publicId, Pageable pageable
    );


}
