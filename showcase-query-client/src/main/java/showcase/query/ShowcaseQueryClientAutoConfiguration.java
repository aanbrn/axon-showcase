package showcase.query;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

/**
 * Auto-configuration registering Resilience4j customizers for the showcase query service.
 */
@AutoConfiguration
@EnableConfigurationProperties(ShowcaseQueryClientProperties.class)
@ComponentScan
class ShowcaseQueryClientAutoConfiguration {
    /**
     * Configures the circuit breaker to ignore {@link ShowcaseQueryException}s, which are business errors rather
     * than infrastructure failures.
     *
     * @return the circuit breaker configuration customizer
     */
    @Bean
    CircuitBreakerConfigCustomizer showcaseQueryCircuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of(
                SHOWCASE_QUERY_SERVICE, builder -> builder.ignoreExceptions(ShowcaseQueryException.class));
    }

    /**
     * Configures the retry mechanism to retry only retryable exceptions via the {@link ShowcaseQueryRetryFilter}.
     *
     * @return the retry configuration customizer
     */
    @Bean
    @SuppressWarnings("unchecked")
    RetryConfigCustomizer showcaseQueryRetryConfigCustomizer() {
        return RetryConfigCustomizer.of(SHOWCASE_QUERY_SERVICE, builder -> {
            builder.retryOnException(new ShowcaseQueryRetryFilter());
        });
    }
}
