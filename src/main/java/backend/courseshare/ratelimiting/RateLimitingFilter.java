package backend.courseshare.ratelimiting;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitingFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);

        if(bucket.tryConsume(1)) {
            filterChain.doFilter(request,response);
        }
        else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                    "error" : Too many requests",
                    "message" : "Please try again later
                    }""");
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String forwarded = request.getHeader("x-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()){
            return forwarded.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}
