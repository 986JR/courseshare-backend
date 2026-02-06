package backend.courseshare.dto.review;

public record ReviewResponse(
        Long id,
        String reviewerPublicId,
        String reviewerUsernsame,
        Integer rating,
        String comment,
        String createdAt,
        String updatedAt
) {
}
