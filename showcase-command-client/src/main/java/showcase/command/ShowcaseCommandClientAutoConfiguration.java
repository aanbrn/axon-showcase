package showcase.command;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static showcase.command.ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE;

@AutoConfiguration
@ComponentScan
class ShowcaseCommandClientAutoConfiguration {

    @Bean
    CircuitBreakerConfigCustomizer showcaseCommandCircuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of(
                SHOWCASE_COMMAND_SERVICE, builder -> builder.ignoreExceptions(ShowcaseCommandException.class));
    }

    @Bean
    @SuppressWarnings("unchecked")
    RetryConfigCustomizer showcaseCommandRetryConfigCustomizer() {
        return RetryConfigCustomizer.of(SHOWCASE_COMMAND_SERVICE, builder -> {
            builder.retryOnException(new ShowcaseCommandRetryFilter());
        });
    }
}
