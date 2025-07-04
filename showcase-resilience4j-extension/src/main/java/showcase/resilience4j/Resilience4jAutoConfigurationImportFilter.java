package showcase.resilience4j;

import lombok.Setter;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Resilience4jAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {

    private static final Predicate<String> BULKHEAD_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.bulkhead\\.autoconfigure\\..*Bulkhead" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    private static final Predicate<String> TIMELIMITER_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.timelimiter\\.autoconfigure\\..*TimeLimiter" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    private static final Predicate<String> RATELIMITER_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.ratelimiter\\.autoconfigure\\..*RateLimiter" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    private static final Predicate<String> CIRCUITBREAKER_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.circuitbreaker\\.autoconfigure\\." +
                                    ".*CircuitBreaker.*AutoConfiguration")
                   .asMatchPredicate();

    private static final Predicate<String> RETRY_AUTO_CONFIGURATION_MATCH_PREDICATE =
            Pattern.compile("io\\.github\\.resilience4j\\.springboot3\\.retry\\.autoconfigure\\..*Retry" +
                                    ".*AutoConfiguration")
                   .asMatchPredicate();

    @Setter
    private Environment environment;

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
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
