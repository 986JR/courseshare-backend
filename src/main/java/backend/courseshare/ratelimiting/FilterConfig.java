package backend.courseshare.ratelimiting;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter(
            RateLimitingFilter filter
    ) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1);
        return registration;
    }
}
