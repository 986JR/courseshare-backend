package backend.courseshare.config;

import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    private final String apiKey;

    public SendGridConfig(org.springframework.core.env.Environment env) {
        this.apiKey = env.getProperty("sendgrid.api.key");
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(apiKey);
    }
}

