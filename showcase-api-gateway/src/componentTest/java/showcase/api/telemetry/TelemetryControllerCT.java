// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import showcase.api.ShowcaseApiErrorResolver;

@WebFluxTest(TelemetryController.class)
@DisplayName("Client telemetry controller component tests")
class TelemetryControllerCT {

    @Configuration
    @ComponentScan(
            useDefaultFilters = false,
            includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TelemetryController.class))
    static class TestConfig {

        @Bean
        ShowcaseApiErrorResolver showcaseApiErrorResolver(MessageSource messageSource) {
            return new ShowcaseApiErrorResolver(messageSource);
        }

        @Bean
        SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
            return http.csrf(CsrfSpec::disable)
                    .authorizeExchange(authorize -> authorize.anyExchange().permitAll())
                    .build();
        }

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        TelemetryRecorder telemetryRecorder(MeterRegistry meterRegistry) {
            return new TelemetryRecorder(meterRegistry);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void resetMetrics() {
        meterRegistry.clear();
    }

    static List<Arguments> malformedReports() {
        return List.of(
                argumentSet(
                        "unknown vital name",
                        report("vitals", List.of(Map.of("name", "BOGUS", "rating", "good", "value", 1.0)))),
                argumentSet(
                        "unknown vital rating",
                        report("vitals", List.of(Map.of("name", "LCP", "rating", "bad", "value", 1.0)))),
                argumentSet(
                        "negative vital value",
                        report("vitals", List.of(Map.of("name", "LCP", "rating", "good", "value", -1.0)))),
                argumentSet(
                        "too many vitals",
                        report(
                                "vitals",
                                Stream.generate(() ->
                                                Map.<String, Object>of("name", "LCP", "rating", "good", "value", 1.0))
                                        .limit(11)
                                        .toList())),
                argumentSet(
                        "over-long error field",
                        report("errors", List.of(Map.of("type", "Error", "message", "x".repeat(501))))),
                argumentSet(
                        "too many errors",
                        report(
                                "errors",
                                Stream.generate(() -> Map.<String, Object>of("type", "Error", "message", "boom"))
                                        .limit(21)
                                        .toList())));
    }

    static List<Arguments> nullLists() {
        return List.of(argumentSet("vitals", "vitals"), argumentSet("errors", "errors"));
    }

    @ParameterizedTest
    @MethodSource("nullLists")
    @DisplayName("A null vitals or errors list is treated as empty")
    void report_nullList_isTreatedAsEmpty(String key) {
        webTestClient
                .post()
                .uri("/telemetry")
                .contentType(APPLICATION_JSON)
                .bodyValue(report(key, null))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(meterRegistry.find(TelemetryRecorder.VITALS_METRIC).summaries())
                .isEmpty();
        assertThat(meterRegistry.find(TelemetryRecorder.ERRORS_METRIC).counters())
                .isEmpty();
    }

    @Test
    @DisplayName("A valid report is recorded and answered with 204")
    void report_validReport_isRecordedAndAnsweredWithNoContent() {
        webTestClient
                .post()
                .uri("/telemetry")
                .contentType(APPLICATION_JSON)
                .bodyValue(Map.of(
                        "path",
                        "/",
                        "vitals",
                        List.of(Map.of("name", "LCP", "rating", "good", "value", 1234.0)),
                        "errors",
                        List.of(Map.of(
                                "type", "TypeError",
                                "message", "boom",
                                "source", "app.js",
                                "line", 1,
                                "column", 2))))
                .exchange()
                .expectStatus()
                .isNoContent();

        assertThat(meterRegistry
                        .get(TelemetryRecorder.VITALS_METRIC)
                        .tag("metric", "LCP")
                        .tag("rating", "good")
                        .tag("route", "/")
                        .summary()
                        .count())
                .isEqualTo(1);
        assertThat(meterRegistry
                        .get(TelemetryRecorder.ERRORS_METRIC)
                        .tag("type", "TypeError")
                        .tag("route", "/")
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @ParameterizedTest
    @MethodSource("malformedReports")
    @DisplayName("A malformed report is rejected with 400 and records nothing")
    void report_malformedReport_isRejectedAndRecordsNothing(Map<String, Object> body) {
        webTestClient
                .post()
                .uri("/telemetry")
                .contentType(APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.bodyErrors")
                .isMap();

        assertThat(meterRegistry.find(TelemetryRecorder.VITALS_METRIC).summaries())
                .isEmpty();
        assertThat(meterRegistry.find(TelemetryRecorder.ERRORS_METRIC).counters())
                .isEmpty();
    }

    private static Map<String, Object> report(String key, Object value) {
        val body = new HashMap<String, Object>();
        body.put("path", "/");
        body.put(key, value);
        return body;
    }
}
