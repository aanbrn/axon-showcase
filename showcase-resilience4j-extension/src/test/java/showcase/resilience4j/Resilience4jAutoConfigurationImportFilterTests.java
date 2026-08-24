// SPDX-License-Identifier: MIT
package showcase.resilience4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

@DisplayName("Resilience4j auto-configuration import filter tests")
class Resilience4jAutoConfigurationImportFilterTests {

    private static final String BULKHEAD_AUTOCONFIGURATION =
            "io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadAutoConfiguration";
    private static final String TIME_LIMITER_AUTOCONFIGURATION =
            "io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterAutoConfiguration";
    private static final String RATE_LIMITER_AUTOCONFIGURATION =
            "io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration";
    private static final String CIRCUIT_BREAKER_AUTOCONFIGURATION =
            "io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration";
    private static final String RETRY_AUTOCONFIGURATION =
            "io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration";

    @Test
    @DisplayName("Matching without an injected environment throws an illegal state exception")
    void match_environmentNotInjected_throwsIllegalStateException() {
        val filter = new Resilience4jAutoConfigurationImportFilter();

        assertThatThrownBy(() -> filter.match(new String[] {CIRCUIT_BREAKER_AUTOCONFIGURATION}, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("\"environment\" is required");
    }

    @Test
    @DisplayName("A disabled master flag excludes all feature auto-configurations")
    void match_masterDisabled_excludesAllFeatureAutoConfigurations() {
        val filter = newFilter(Map.of("resilience4j.enabled", false));

        val result = filter.match(allFeatureAutoConfigurations(), null);

        assertThat(result).containsExactly(false, false, false, false, false);
    }

    @Test
    @DisplayName("No flags set leaves all feature auto-configurations eligible")
    void match_noFlagsSet_allFeatureAutoConfigurationsEligible() {
        val filter = newFilter(Map.of());

        val result = filter.match(allFeatureAutoConfigurations(), null);

        assertThat(result).containsExactly(true, true, true, true, true);
    }

    @Test
    @DisplayName("A disabled circuit breaker flag excludes only the circuit breaker")
    void match_circuitBreakerDisabled_excludesOnlyCircuitBreaker() {
        val filter = newFilter(Map.of("resilience4j.circuitbreaker.enabled", false));

        val result = filter.match(allFeatureAutoConfigurations(), null);

        assertThat(result).containsExactly(true, true, true, false, true);
    }

    @Test
    @DisplayName("A disabled bulkhead flag excludes only the bulkhead")
    void match_bulkheadDisabled_excludesOnlyBulkhead() {
        val filter = newFilter(Map.of("resilience4j.bulkhead.enabled", false));

        val result = filter.match(allFeatureAutoConfigurations(), null);

        assertThat(result).containsExactly(false, true, true, true, true);
    }

    @Test
    @DisplayName("A non-Resilience4j class passes through")
    void match_nonResilience4jClass_passesThrough() {
        val filter = newFilter(Map.of("resilience4j.enabled", false));

        val result =
                filter.match(new String[] {"org.springframework.boot.autoconfigure.web.OtherAutoConfiguration"}, null);

        assertThat(result).containsExactly(true);
    }

    @Test
    @DisplayName("A circuit breaker class is gated by its feature flag")
    void match_circuitBreakerFqcn_isGatedByFeatureFlag() {
        val filter = newFilter(Map.of("resilience4j.circuitbreaker.enabled", false));

        val result = filter.match(new String[] {CIRCUIT_BREAKER_AUTOCONFIGURATION}, null);

        assertThat(result).containsExactly(false);
    }

    @Test
    @DisplayName("Null and empty class names are excluded")
    void match_nullAndEmptyClassNames_areExcluded() {
        val filter = newFilter(Map.of());

        val result = filter.match(new String[] {null, "", CIRCUIT_BREAKER_AUTOCONFIGURATION}, null);

        assertThat(result).containsExactly(false, false, true);
    }

    @Test
    @DisplayName("Spring factories lists the filter")
    void springFactories_listsTheFilter() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("META-INF/spring.factories")) {
            assertThat(in).isNotNull();
            val content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).contains("org.springframework.boot.autoconfigure.AutoConfigurationImportFilter");
            assertThat(content).contains("showcase.resilience4j.Resilience4jAutoConfigurationImportFilter");
        }
    }

    @Test
    @DisplayName("Configuration metadata declares all six properties")
    void configurationMetadata_declaresAllSixProperties() throws IOException {
        val propertyNames = new String[] {
            "resilience4j.enabled",
            "resilience4j.bulkhead.enabled",
            "resilience4j.timelimiter.enabled",
            "resilience4j.ratelimiter.enabled",
            "resilience4j.circuitbreaker.enabled",
            "resilience4j.retry.enabled"
        };
        try (InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("META-INF/additional-spring-configuration-metadata.json")) {
            assertThat(in).isNotNull();
            val content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (val propertyName : propertyNames) {
                assertThat(content).contains("\"" + propertyName + "\"");
                assertThat(content).contains("java.lang.Boolean");
                assertThat(content).contains("\"defaultValue\": true");
            }
        }
    }

    private Resilience4jAutoConfigurationImportFilter newFilter(Map<String, Boolean> flags) {
        val environment = mock(Environment.class);
        when(environment.getProperty(eq("resilience4j.enabled"), eq(Boolean.TYPE), eq(Boolean.TRUE)))
                .thenReturn(flags.getOrDefault("resilience4j.enabled", Boolean.TRUE));
        when(environment.getProperty(eq("resilience4j.bulkhead.enabled"), eq(Boolean.TYPE), eq(Boolean.TRUE)))
                .thenReturn(flags.getOrDefault("resilience4j.bulkhead.enabled", Boolean.TRUE));
        when(environment.getProperty(eq("resilience4j.timelimiter.enabled"), eq(Boolean.TYPE), eq(Boolean.TRUE)))
                .thenReturn(flags.getOrDefault("resilience4j.timelimiter.enabled", Boolean.TRUE));
        when(environment.getProperty(eq("resilience4j.ratelimiter.enabled"), eq(Boolean.TYPE), eq(Boolean.TRUE)))
                .thenReturn(flags.getOrDefault("resilience4j.ratelimiter.enabled", Boolean.TRUE));
        when(environment.getProperty(eq("resilience4j.circuitbreaker.enabled"), eq(Boolean.TYPE), eq(Boolean.TRUE)))
                .thenReturn(flags.getOrDefault("resilience4j.circuitbreaker.enabled", Boolean.TRUE));
        when(environment.getProperty(eq("resilience4j.retry.enabled"), eq(Boolean.TYPE), eq(Boolean.TRUE)))
                .thenReturn(flags.getOrDefault("resilience4j.retry.enabled", Boolean.TRUE));

        val filter = new Resilience4jAutoConfigurationImportFilter();
        filter.setEnvironment(environment);
        return filter;
    }

    private String[] allFeatureAutoConfigurations() {
        return new String[] {
            BULKHEAD_AUTOCONFIGURATION,
            TIME_LIMITER_AUTOCONFIGURATION,
            RATE_LIMITER_AUTOCONFIGURATION,
            CIRCUIT_BREAKER_AUTOCONFIGURATION,
            RETRY_AUTOCONFIGURATION
        };
    }
}
