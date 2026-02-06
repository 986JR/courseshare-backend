package backend.courseshare.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserPatchDTO(
        @Size(min = 3, max = 100) String username,
        @Email
        String email,
        @Size(min = 6)
        String password,
        String role
) {
}
