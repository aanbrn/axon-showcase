package showcase.resilience4j;

import lombok.Setter;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * Auto-configuration import filter that conditionally disables Resilience4j features based on configuration
 * properties.
 *
 * <p>Each feature (bulkhead, time limiter, rate limiter, circuit breaker, retry) is only imported when both the
 * overall {@code resilience4j.enabled} and the feature-specific flag are enabled.
 */
public class Resilience4jAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {
    /**
     * Matches Resilience4j bulkhead auto-configuration classes.
     */
    private static final Predicate<String> BULKHEAD_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.bulkhead\\.autoconfigure\\..*Bulkhead" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    /**
     * Matches Resilience4j time limiter auto-configuration classes.
     */
    private static final Predicate<String> TIMELIMITER_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.timelimiter\\.autoconfigure\\..*TimeLimiter" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    /**
     * Matches Resilience4j rate limiter auto-configuration classes.
     */
    private static final Predicate<String> RATELIMITER_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.ratelimiter\\.autoconfigure\\..*RateLimiter" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    /**
     * Matches Resilience4j circuit breaker auto-configuration classes.
     */
    private static final Predicate<String> CIRCUITBREAKER_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.circuitbreaker\\.autoconfigure\\." +
                                    ".*CircuitBreaker.*AutoConfiguration")
                   .asMatchPredicate();

    /**
     * Matches Resilience4j retry auto-configuration classes.
     */
    private static final Predicate<String> RETRY_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.retry\\.autoconfigure\\..*Retry" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    /**
     * The environment used to resolve the Resilience4j feature flags.
     */
    @Nullable
    @Setter
    private Environment environment;

    /**
     * Determines which auto-configuration classes should be selected.
     *
     * <p>Resilience4j auto-configurations are excluded when the corresponding feature flag is disabled.
     *
     * @param autoConfigurationClasses  the auto-configuration class names to filter
     * @param autoConfigurationMetadata the auto-configuration metadata
     * @return an array where each {@code true} entry keeps the corresponding class
     */
    @Override
    public boolean[] match(@Nullable String[] autoConfigurationClasses,
                           AutoConfigurationMetadata autoConfigurationMetadata) {
        checkState(Objects.nonNull(environment), "\"environment\" is required");

        val resilienceEnabled = environment.getProperty("resilience4j.enabled", Boolean.TYPE, Boolean.TRUE);
        val bulkheadEnabled = environment.getProperty("resilience4j.bulkhead.enabled", Boolean.TYPE, Boolean.TRUE);
        val threadPoolBulkheadEnabled = environment.getProperty(
                "resilience4j.thread-pool-bulkhead.enabled", Boolean.TYPE, Boolean.TRUE);
        val timeLimiterEnabled = environment.getProperty(
                "resilience4j.timelimiter.enabled", Boolean.TYPE, Boolean.TRUE);
        val rateLimiterEnabled = environment.getProperty(
                "resilience4j.ratelimiter.enabled", Boolean.TYPE, Boolean.TRUE);
        val circuitBreakerEnabled = environment.getProperty(
                "resilience4j.circuitbreaker.enabled", Boolean.TYPE, Boolean.TRUE);
        val retryEnabled = environment.getProperty("resilience4j.retry.enabled", Boolean.TYPE, Boolean.TRUE);
        val result = new boolean[autoConfigurationClasses.length];

        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            val clazz = autoConfigurationClasses[i];
            if (clazz == null || clazz.isEmpty()) {
                continue;
            }
            result[i] = switch (clazz) {
                case String s when BULKHEAD_AUTO_CONFIGURATION_MATCH_PREDICATE.test(s) ->
                        resilienceEnabled && bulkheadEnabled && threadPoolBulkheadEnabled;
                case String s when TIMELIMITER_AUTO_CONFIGURATION_MATCH_PREDICATE.test(s) ->
                        resilienceEnabled && timeLimiterEnabled;
                case String s when RATELIMITER_AUTO_CONFIGURATION_MATCH_PREDICATE.test(s) ->
                        resilienceEnabled && rateLimiterEnabled;
                case String s when CIRCUITBREAKER_AUTO_CONFIGURATION_MATCH_PREDICATE.test(s) ->
                        resilienceEnabled && circuitBreakerEnabled;
                case String s when RETRY_AUTO_CONFIGURATION_MATCH_PREDICATE.test(s) ->
                        resilienceEnabled && retryEnabled;
                default -> true;
            };
        }

        return result;
    }
}
