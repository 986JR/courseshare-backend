package backend.courseshare.jwt;

import backend.courseshare.security.CustomUserDetailsService;
import backend.courseshare.service.BlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final BlacklistService blacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, BlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        }

        // 🔹 No token → let Spring handle public endpoints
        if (token == null || token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        // 🔹 Blacklisted token → 401
        if (blacklistService.isBlacklisted(token)) {
            unauthorized(response, "Token has been revoked");
            return;
        }

        // 🔹 Invalid or expired token → 401
        if (!jwtUtil.validateToken(token)) {
            unauthorized(response, "Invalid or expired access token");
            return;
        }

        // 🔹 Valid token → authenticate
        try {
            String subject = jwtUtil.getSubject(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

            var auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (UsernameNotFoundException ex) {
            unauthorized(response, "User not found for token");
            return;
        } catch (Exception ex) {
            unauthorized(response, "Authentication failed");
            return;
        }

        //Continue only if authenticated
        chain.doFilter(request, response);
    }


    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("""
        {
          "error": "unauthorized",
          "message": "%s"
        }
    """.formatted(message));
    }

}
