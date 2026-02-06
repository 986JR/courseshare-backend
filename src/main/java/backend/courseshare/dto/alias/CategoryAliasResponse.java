package backend.courseshare.dto.alias;

public record CategoryAliasResponse(
        Long id,
        String alias,
        String categorySlug
) {}
