package backend.courseshare.dto.alias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryAliasRequest(
        @NotBlank @Size(max = 150) String alias,
        @NotBlank String categorySlug
) {}
