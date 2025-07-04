package showcase.query;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

@AutoConfiguration
@EnableConfigurationProperties(ShowcaseQueryClientProperties.class)
@ComponentScan
class ShowcaseQueryClientAutoConfiguration {

    @Bean
    CircuitBreakerConfigCustomizer showcaseQueryCircuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of(
                SHOWCASE_QUERY_SERVICE, builder -> builder.ignoreExceptions(ShowcaseQueryException.class));
    }

    @Bean
    @SuppressWarnings("unchecked")
    RetryConfigCustomizer showcaseQueryRetryConfigCustomizer() {
        return RetryConfigCustomizer.of(SHOWCASE_QUERY_SERVICE, builder -> {
            builder.retryOnException(new ShowcaseQueryRetryFilter());
        });
    }
}
