package backend.courseshare.dto.review;

public record ReviewStatsResponse(
        double averageRating,
        long totalRatings
) {
}
