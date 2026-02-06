package backend.courseshare.dto.review;

import java.util.List;

public record PagedReviewListRepsonse(
        List<ReviewResponse> iteems,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
