package backend.courseshare.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String slug,
        @Size(max = 2000) String description
) {}
