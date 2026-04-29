package ro.unibuc.prodeng.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {
    private final Counter commentCreatedCounter;
    private final Counter commentFailedCounter;
    private final Timer commentLookupTimer;
    private final AtomicInteger activeViewers = new AtomicInteger(0);
    private final Counter apiCallsCounter;

    public MetricsService(MeterRegistry registry) {
        // 1. Business Category: Number of valid comments created
        this.commentCreatedCounter = Counter.builder("app_comments_created_total")
            .description("Total number of comments created").register(registry);

        // 2. Error Category: Number of failed comment creations
        this.commentFailedCounter = Counter.builder("app_comments_creation_failed_total")
            .description("Total failed comment creations").register(registry);

        // 3. Performance Category: Time taken to look up comments
        this.commentLookupTimer = Timer.builder("app_comment_lookup_duration_seconds")
            .description("Time taken to look up comments").register(registry);

        // 4. Resource Category: Monitoring heap memory (Gauge)
        Gauge.builder("app_resource_heap_usage_ratio", Runtime.getRuntime(),
            r -> (double) (r.totalMemory() - r.freeMemory()) / r.maxMemory())
            .description("Ratio of used heap memory").register(registry);
            
        // 5. Domain-specific Category: Number of currently active viewers or API calls
        this.apiCallsCounter = Counter.builder("app_comment_api_calls_total")
            .description("Total domain-specific API calls for comments")
            .tag("version", "v1")
            .register(registry);

         // Extra Metric (Gauge) already present
         Gauge.builder("app_active_viewers", activeViewers, AtomicInteger::get)
            .description("Number of currently active viewers").register(registry);
    }

    // Methods to increment metrics
    public void recordCommentCreated() { commentCreatedCounter.increment(); }
    public void recordCommentFailed() { commentFailedCounter.increment(); }
    public void recordApiCall() { apiCallsCounter.increment(); }
    public Timer getCommentLookupTimer() { return commentLookupTimer; }
}