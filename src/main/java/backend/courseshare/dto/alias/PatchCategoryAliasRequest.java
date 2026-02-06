package backend.courseshare.dto.alias;

import jakarta.validation.constraints.Size;

public record PatchCategoryAliasRequest(
        @Size(max = 150) String alias,
        String categorySlug
) {}
