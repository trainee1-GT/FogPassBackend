package train.local.fogpass.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import train.local.fogpass.audit.ChangeLogUtil;

@Configuration
@EnableScheduling
public class ApplicationConfig {

    @Bean
    public ChangeLogUtil changeLogUtil(ObjectMapper objectMapper) {
        return new ChangeLogUtil(objectMapper);
    }
}
