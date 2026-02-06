package backend.courseshare.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @Min(1) @Max(5) Integer rating,
        @Size(max = 4000) String comment
) {
}
