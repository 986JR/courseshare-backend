package backend.courseshare.repository;

import backend.courseshare.entity.FileResourceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileResourceReviewRepository extends JpaRepository<FileResourceReview, Long> {
    boolean existsByUser_IdAndFileId(Long userId, Long fileId);

    Optional<FileResourceReview> findByUser_IdAndFile_Id(Long userId, Long fileId);

    Page<FileResourceReview> findByFile_Id(Long fileId, Pageable pageable);



    List<FileResourceReview> findByFile_IdOrderByCreatedAtDesc(Long fileId);



    @Query("SELECT AVG(r.rating) FROM FileResourceReview r WHERE r.file.id = :fileId AND r.rating IS NOT NULL")
    Double findAverageRatingByFileId(Long fileId);

    @Query("SELECT COUNT(r) FROM FileResourceReview r WHERE r.file.id = :fileId AND r.rating IS NOT NULL")
    Long countRatingsByFileId(Long fileId);
}
