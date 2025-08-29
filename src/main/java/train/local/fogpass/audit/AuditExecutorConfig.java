package train.local.fogpass.audit;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AuditExecutorConfig {

    @Bean(name = "auditTaskExecutor")
    public ThreadPoolTaskExecutor auditTaskExecutor(MeterRegistry meterRegistry,
                                                    @Value("${audit.executor.corePoolSize:2}") int corePoolSize,
                                                    @Value("${audit.executor.maxPoolSize:8}") int maxPoolSize,
                                                    @Value("${audit.executor.queueCapacity:1000}") int queueCapacity) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setThreadNamePrefix("audit-");
        exec.setCorePoolSize(corePoolSize);
        exec.setMaxPoolSize(maxPoolSize);
        exec.setQueueCapacity(queueCapacity);
        // Track rejected tasks
        final long[] rejectedCounter = {0};
        RejectedExecutionHandler reh = (r, executor) -> {
            rejectedCounter[0]++;
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, executor);
        };
        exec.setRejectedExecutionHandler(reh);
        exec.initialize();

        // Expose Micrometer gauges
        Gauge.builder("audit.executor.active", exec, e -> (double) e.getActiveCount()).register(meterRegistry);
        Gauge.builder("audit.executor.queue.size", exec, e -> (double) e.getThreadPoolExecutor().getQueue().size()).register(meterRegistry);
        Gauge.builder("audit.executor.pool.size", exec, e -> (double) e.getPoolSize()).register(meterRegistry);
        Gauge.builder("audit.executor.completed", exec, e -> (double) e.getThreadPoolExecutor().getCompletedTaskCount()).register(meterRegistry);
        Gauge.builder("audit.executor.rejected", () -> (double) rejectedCounter[0]).register(meterRegistry);

        return exec;
    }
}