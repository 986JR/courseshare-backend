package backend.courseshare.service;


import backend.courseshare.dto.review.PagedReviewListRepsonse;
import backend.courseshare.dto.review.ReviewRequest;
import backend.courseshare.dto.review.ReviewResponse;
import backend.courseshare.dto.review.ReviewStatsResponse;
import backend.courseshare.entity.FileResource;
import backend.courseshare.entity.FileResourceReview;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.FileResourceRepository;
import backend.courseshare.repository.FileResourceReviewRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FileResourceReviewService {

    private final FileResourceRepository fileResourceRepository;
    private final FileResourceReviewRepository reviewRepository;

    public FileResourceReviewService(FileResourceRepository fileResourceRepository, FileResourceReviewRepository reviewRepository) {
        this.fileResourceRepository = fileResourceRepository;
        this.reviewRepository = reviewRepository;
    }

    //Add andd Update
    public ReviewResponse addOrUpdateReview(String filePublicId, Users users, ReviewRequest request) {

        FileResource file = fileResourceRepository.findByPublicId(filePublicId).orElseThrow(() -> new IllegalArgumentException("File Not Found Ex"));

        FileResourceReview review =reviewRepository.findByUser_IdAndFile_Id(users.getId(), file.getId()).orElseGet(()-> {
            FileResourceReview r = new FileResourceReview();
            r.setUser(users);
            r.setFile(file);

            return  r;
        });

        //Update
        review.setRating(request.rating());
        review.setComment(request.comment());

        FileResourceReview saved = reviewRepository.save(review);

        return new ReviewResponse(
                saved.getId(),
                saved.getUser().getPublicId(),
                saved.getUser().getUsername(),
                saved.getRating(),
                saved.getComment(),
                saved.getCreatedAt().toString(),
                saved.getUpdatedAt().toString()
        );


    }

    public PagedReviewListRepsonse listReviews(String filePublicId, int page, int size) {

        FileResource file = fileResourceRepository.findByPublicId(filePublicId).orElseThrow(() -> new IllegalArgumentException("File not found"));

        Page<FileResourceReview> reviewPage = reviewRepository.findByFile_Id(file.getId(), PageRequest.of(page,size));

        return new PagedReviewListRepsonse(
                reviewPage.map(r ->
                        new ReviewResponse(
                                r.getId(),
                                r.getUser().getPublicId(),
                                r.getUser().getUsername(),
                                r.getRating(),
                                r.getComment(),
                                r.getCreatedAt().toString(),
                                r.getUpdatedAt().toString()
                        )
                ).toList(),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast()
        );
    }

    public ReviewStatsResponse getStats(String filePublicId) {

        FileResource file = fileResourceRepository
                .findByPublicId(filePublicId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        Double avg = reviewRepository.findAverageRatingByFileId(file.getId());
        Long count = reviewRepository.countRatingsByFileId(file.getId());

        return new ReviewStatsResponse(
                avg == null ? 0.0 : avg,
                count == null ? 0 : count
        );
    }


}
