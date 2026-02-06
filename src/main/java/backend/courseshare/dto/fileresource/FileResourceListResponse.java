package backend.courseshare.dto.fileresource;

import java.util.List;

public record FileResourceListResponse(
        List<FileResourceResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
