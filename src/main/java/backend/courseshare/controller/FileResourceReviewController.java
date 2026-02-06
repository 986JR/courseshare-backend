package backend.courseshare.controller;

import backend.courseshare.dto.review.PagedReviewListRepsonse;

import backend.courseshare.dto.review.ReviewRequest;
import backend.courseshare.dto.review.ReviewResponse;
import backend.courseshare.dto.review.ReviewStatsResponse;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.UserRepository;
import backend.courseshare.security.CustomUserDetails;
import backend.courseshare.service.FileResourceReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files/{publicId}/reviews")
public class FileResourceReviewController {

    private final FileResourceReviewService reviewService;
    private final UserRepository userRepository;

    public FileResourceReviewController(FileResourceReviewService reviewService,
                                        UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }


    // 1. Add or Update a Review
    @PostMapping
    public ResponseEntity<ReviewResponse> addOrUpdateReview(
            @PathVariable("publicId") String filePublicId,
            @RequestBody ReviewRequest request,
            Authentication authentication
    ) {


        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();


        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        ReviewResponse response = reviewService.addOrUpdateReview(filePublicId, user, request);

        return ResponseEntity.ok(response);
    }


    //List Reviews
    @GetMapping
    public ResponseEntity<PagedReviewListRepsonse> listReviews(
            @PathVariable String publicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(reviewService.listReviews(publicId, page, size));
    }

    //Get Stats
    @GetMapping("/stats")
    public ResponseEntity<ReviewStatsResponse> getStats(@PathVariable String publicId) {
        return ResponseEntity.ok(reviewService.getStats(publicId));
    }
}
