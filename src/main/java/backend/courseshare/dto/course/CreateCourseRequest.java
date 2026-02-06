package backend.courseshare.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank @Size(max = 300) String title,
        @Size(max = 5000) String description,
        String categorySlug,
        String categoryName,
        @Size(max = 100) String courseCode
) {}
