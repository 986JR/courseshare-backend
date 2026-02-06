package backend.courseshare.dev;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class DevController {
    @PostMapping("/dev/test-post")
    public ResponseEntity<Object> testPost(@RequestBody(required = false) String body,
                                           Authentication auth,
                                           HttpServletRequest req) {
        System.out.println("DEV POST HIT URI=" + req.getRequestURI());
        System.out.println("Dev Auth: " + auth);
        System.out.println("Dev Principal: " + (auth == null ? null : auth.getPrincipal().getClass().getName()));
        System.out.println("Dev Authorities: " + (auth == null ? null : auth.getAuthorities()));
        System.out.println("Dev Content-Type: " + req.getHeader("Content-Type"));
        System.out.println("Dev Body: " + body);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
