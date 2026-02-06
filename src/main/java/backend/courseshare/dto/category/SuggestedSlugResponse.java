package backend.courseshare.dto.category;

import java.util.List;

public record SuggestedSlugResponse(
        String suggestedSlug,
        List<String> suggestions
) {}
