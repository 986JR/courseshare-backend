package backend.courseshare.security;

import backend.courseshare.entity.Users;
import backend.courseshare.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        // Try username -> email -> publicId
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .or(() -> userRepository.findByPublicId(principal))
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal));
    }

}
