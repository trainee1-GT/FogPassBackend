package train.local.fogpass.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-storage")
@Getter
@Setter
public class FileStorageProperties {
    /**
     * Absolute root directory for Landmark files (environment specific).
     * Example: D:/Landmarks_Files
     */
    private String landmarksPath;
}