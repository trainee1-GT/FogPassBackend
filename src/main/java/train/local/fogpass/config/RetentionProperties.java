package train.local.fogpass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "data-lifecycle.retention")
@Data
public class RetentionProperties {
    private Rule defaultRule = new Rule();
    private Map<String, Rule> perAssetType = new HashMap<>();

    @Data
    public static class Rule {
        private int archiveAfterDays = 365;
        private int purgeAfterDays = 2555;
    }

    public Rule ruleFor(String assetType) {
        return perAssetType.getOrDefault(assetType, defaultRule);
    }
}