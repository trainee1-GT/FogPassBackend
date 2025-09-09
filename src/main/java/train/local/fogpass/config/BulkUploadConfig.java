package train.local.fogpass.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties({ FileStorageProperties.class, RetentionProperties.class })
public class BulkUploadConfig {

    @Bean(name = "bulkUploadExecutor")
    public ThreadPoolTaskExecutor bulkUploadExecutor(MeterRegistry registry,
                                                     RetentionProperties retentionProps,
                                                     org.springframework.core.env.Environment env) {
        int maxConcurrent = Integer.parseInt(env.getProperty("bulk-upload.max-concurrent-jobs", "4"));
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setThreadNamePrefix("bulk-upload-");
        exec.setCorePoolSize(maxConcurrent);
        exec.setMaxPoolSize(maxConcurrent);
        exec.setQueueCapacity(1000);
        exec.setAllowCoreThreadTimeOut(true);
        exec.initialize();
        registry.gauge("bulk_upload_executor_queue_size", exec.getThreadPoolExecutor().getQueue(), q -> q.size());
        return exec;
    }
}