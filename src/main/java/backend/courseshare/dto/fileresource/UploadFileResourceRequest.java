package backend.courseshare.dto.fileresource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadFileResourceRequest(
        @NotBlank String courseCode,
        @Size(max= 2500) String description
) {
}
