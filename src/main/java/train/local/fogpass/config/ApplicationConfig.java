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
    public Object ensureLandmarksDir(FileStorageProperties props) {
        try {
            Path base = Path.of(props.getLandmarksPath());
            Files.createDirectories(base);
            log.info("Landmarks storage path is set to: {} (exists={})", base.toAbsolutePath(), Files.exists(base));
        } catch (Exception e) {
            log.error("Failed to prepare landmarks storage directory: {}", props.getLandmarksPath(), e);
        }
        return new Object();
    }
}
