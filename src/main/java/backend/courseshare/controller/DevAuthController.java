package backend.courseshare.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class DevAuthController {
    @GetMapping("/dev/debug-auth")
    public Map<String, Object> debugAuth(Authentication authentication) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "authenticated", a != null && a.isAuthenticated(),
                "principal_class", a == null ? null : a.getPrincipal().getClass().getName(),
                "principal", a == null ? null : a.getPrincipal().toString(),
                "authorities", a == null ? null : a.getAuthorities()
        );
    }


}

