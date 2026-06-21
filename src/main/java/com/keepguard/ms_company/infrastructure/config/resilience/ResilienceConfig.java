package com.keepguard.ms_company.infrastructure.config.resilience;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienceConfig {

    private final MeterRegistry meterRegistry;

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        TaggedCircuitBreakerMetrics
            .ofCircuitBreakerRegistry(registry)
            .bindTo(meterRegistry);
        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();
        TaggedRetryMetrics
            .ofRetryRegistry(registry)
            .bindTo(meterRegistry);
        return registry;
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
        TaggedBulkheadMetrics
            .ofBulkheadRegistry(registry)
            .bindTo(meterRegistry);
        return registry;
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
        TaggedRateLimiterMetrics
            .ofRateLimiterRegistry(registry)
            .bindTo(meterRegistry);
        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();
        TaggedTimeLimiterMetrics
            .ofTimeLimiterRegistry(registry)
            .bindTo(meterRegistry);
        return registry;
    }
}
