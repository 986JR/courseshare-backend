package backend.courseshare.security;

import backend.courseshare.entity.Course;
import backend.courseshare.repository.CourseRepository;
import backend.courseshare.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ownershipEvaluator")
@RequiredArgsConstructor
public class OwnershipEvaluator {

    private final CourseRepository courseRepository;

    public boolean isCourseOwner(String courseCode, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Get logged in user
        Object principalObj = authentication.getPrincipal();

        String currentUserPublicId;
        if (principalObj instanceof Users user) {
            currentUserPublicId = user.getPublicId();
        } else if (principalObj instanceof org.springframework.security.core.userdetails.User userDetails) {

            currentUserPublicId = userDetails.getUsername();
        } else {
            return false;
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }


        Course course = courseRepository.findByCourseCode(courseCode).orElse(null);
        if (course == null) {
            return false;
        }

        // Comparing the course owner with current user
        return course.getCreatedBy().getPublicId().equals(currentUserPublicId);
    }
}
