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

    public MetricsService(MeterRegistry registry) {
        // 1. Business: Totale commenti creati
        this.commentCreatedCounter = Counter.builder("app_comments_created_total")
            .description("Total number of comments created").register(registry);

        // 2. Error: Fallimenti nella creazione commenti
        this.commentFailedCounter = Counter.builder("app_comments_creation_failed_total")
            .description("Total failed comment creations").register(registry);

        // 3. Performance: Tempo di caricamento commenti
        this.commentLookupTimer = Timer.builder("app_comment_lookup_duration_seconds")
            .description("Time taken to look up comments").register(registry);

        // 4. Resource: Utenti che stanno visualizzando commenti (esempio)
        Gauge.builder("app_active_viewers", activeViewers, AtomicInteger::get)
            .description("Number of currently active viewers").register(registry);
            
        // 5. Domain-specific: Metrica per le chiamate API custom
        Counter.builder("app_comment_api_calls_total")
            .tag("version", "v1")
            .register(registry);
    }

    public void recordCommentCreated() { commentCreatedCounter.increment(); }
    public void recordCommentFailed() { commentFailedCounter.increment(); }
    public Timer getCommentLookupTimer() { return commentLookupTimer; }
}