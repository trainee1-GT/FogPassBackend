package train.local.fogpass.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties({FileStorageProperties.class})
public class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public Object ensureStorageBuckets(FileStorageProperties props) {
        try {
            // Ensure all configured buckets exist
            Path uploads = Path.of(props.getUploads());
            Path processed = Path.of(props.getProcessed());
            Path errors = Path.of(props.getErrors());
            Path archive = Path.of(props.getArchive());

            Files.createDirectories(uploads);
            Files.createDirectories(processed);
            Files.createDirectories(errors);
            Files.createDirectories(archive);

            log.info("Storage buckets ready: uploads={}, processed={}, errors={}, archive={}",
                    uploads.toAbsolutePath(), processed.toAbsolutePath(), errors.toAbsolutePath(), archive.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to prepare storage buckets using file-storage.* properties", e);
        }
        return new Object();
    }
}
