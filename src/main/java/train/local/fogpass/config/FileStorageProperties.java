package train.local.fogpass.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Backed by application.yml file-storage.*
 */
@ConfigurationProperties(prefix = "file-storage")
@Getter
@Setter
public class FileStorageProperties {
    /** Root folder for all file storage buckets */
    private String root;
    /** Where newly uploaded files are stored */
    private String uploads;
    /** Where successfully processed files can be moved */
    private String processed;
    /** Where error artifacts can be stored */
    private String errors;
    /** Where archived files are moved per retention policy */
    private String archive;
    /** Path where landmark files are stored */
    private String landmarksPath;
}