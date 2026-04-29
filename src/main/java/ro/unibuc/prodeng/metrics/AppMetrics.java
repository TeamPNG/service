package ro.unibuc.prodeng.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import ro.unibuc.prodeng.repository.PhotoRepository;

@Component
public class AppMetrics {

    private final MeterRegistry meterRegistry;

    public AppMetrics(MeterRegistry meterRegistry, PhotoRepository photoRepository) {
        this.meterRegistry = meterRegistry;

        Gauge.builder("photo_count", photoRepository, PhotoRepository::count)
                .description("Current number of photos")
                .register(meterRegistry);
    }

    public Timer.Sample startInvocationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopInvocationTimer(Timer.Sample sample, String endpoint) {
        Timer timer = Timer.builder("invocation_duration_seconds")
                .description("Invocation latency for photo endpoints")
                .tag("endpoint", endpoint)
                .publishPercentileHistogram(true)
                .register(meterRegistry);
        sample.stop(timer);
    }

    public void incrementInvocationCount(String endpoint) {
        meterRegistry.counter("invocation_count_total", "endpoint", endpoint).increment();
    }
}
