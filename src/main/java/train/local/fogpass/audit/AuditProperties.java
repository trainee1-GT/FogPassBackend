package train.local.fogpass.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "audit")
@Getter
@Setter
public class AuditProperties {
    private Set<String> maskedFields = new HashSet<>();
    private Retention retention = new Retention();

    @Getter
    @Setter
    public static class Retention {
        private int days = 1825;
    }
}