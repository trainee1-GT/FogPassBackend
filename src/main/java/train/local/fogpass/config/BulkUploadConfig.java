package train.local.fogpass.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class BulkUploadConfig {

    @Bean(name = "bulkUploadTaskExecutor")
    public TaskExecutor bulkUploadTaskExecutor(
            @Value("${bulk-upload.max-concurrent-jobs:3}") int coreSize,
            MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("bulk-upload-");
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(coreSize);
        executor.setQueueCapacity(coreSize * 2); // small bounded queue
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        // Optionally bind executor metrics if needed
        return executor;
    }
}