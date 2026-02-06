package backend.courseshare.dto.fileresource;

public record FileResourceResponse(
        String publicId,
        String fileName,
        String fileType,
        String description,
        String fileUrl,
        String uploadedByPublicId,
        String courseCode,
        String uploadedAt,
        Long Size
) {
}
