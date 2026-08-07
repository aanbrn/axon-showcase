package showcase.command;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static showcase.command.ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE;

/**
 * Auto-configuration registering Resilience4j customizers for the showcase command service.
 */
@AutoConfiguration
@ComponentScan
class ShowcaseCommandClientAutoConfiguration {
    /**
     * Configures the circuit breaker to ignore {@link ShowcaseCommandException}s, which are business errors rather
     * than infrastructure failures.
     *
     * @return the circuit breaker configuration customizer
     */
    @Bean
    CircuitBreakerConfigCustomizer showcaseCommandCircuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of(
                SHOWCASE_COMMAND_SERVICE, builder -> builder.ignoreExceptions(ShowcaseCommandException.class));
    }

    /**
     * Configures the retry mechanism to retry only retryable exceptions via the {@link ShowcaseCommandRetryFilter}.
     *
     * @return the retry configuration customizer
     */
    @Bean
    @SuppressWarnings("unchecked")
    RetryConfigCustomizer showcaseCommandRetryConfigCustomizer() {
        return RetryConfigCustomizer.of(SHOWCASE_COMMAND_SERVICE, builder -> {
            builder.retryOnException(new ShowcaseCommandRetryFilter());
        });
    }
}
