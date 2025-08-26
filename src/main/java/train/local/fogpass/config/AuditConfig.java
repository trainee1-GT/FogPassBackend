package train.local.fogpass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import train.local.fogpass.security.UserPrincipal;

import java.util.Optional;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    public static class AuditorAwareImpl implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                return Optional.of(userPrincipal.getUsername());
            }
            
            return Optional.of("system");
        }
    }
}