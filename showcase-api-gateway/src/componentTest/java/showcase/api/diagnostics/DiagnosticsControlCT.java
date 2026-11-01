// SPDX-License-Identifier: MIT
package showcase.api.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.regex.Pattern;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Proves that the stall diagnostics fire, so a failure carrying none of their output is a finding rather than a
 * silence.
 *
 * <p>The instruments only produce output on a stall, and the suite does not stall locally in its shipped configuration,
 * so a green suite says nothing about them:
 * this control drives a request through a real slice, parks a virtual thread while holding a monitor, and asserts the
 * rendered output. The failure paths it exercises are the ones a CI failure reads — a completed exchange, a failed
 * exchange, an exchange that never reached a terminal, and the JFR events behind carrier pinning.
 */
@WebFluxTest
@AutoConfigureWebTestClient
@DisplayName("The stall diagnostics produce their output")
@Import(StallDiagnosticsConfig.class)
class DiagnosticsControlCT {

    @Configuration
    static class TestConfig {

        @Bean
        ProbeController probeController() {
            return new ProbeController();
        }

        @Bean
        WebFluxConfigurer blockingExecution() {
            return new WebFluxConfigurer() {
                @Override
                public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
                    configurer.setControllerMethodPredicate(__ -> true);
                }
            };
        }

        @Bean
        SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
            return http.csrf(CsrfSpec::disable)
                    .authorizeExchange(authorize -> authorize.anyExchange().permitAll())
                    .build();
        }
    }

    @RestController
    static class ProbeController {

        @GetMapping("/probe")
        Mono<String> probe() {
            return Mono.just("ok");
        }

        @GetMapping("/probe-failure")
        Mono<String> probeFailure() {
            return Mono.error(new IllegalStateException("the probe's deliberate failure"));
        }

        @GetMapping("/probe-stall")
        Mono<String> probeStall() {
            return stall.asMono();
        }

        @GetMapping("/probe-slow")
        Mono<String> probeSlow() {
            return Mono.fromCallable(() -> {
                Thread.sleep(Duration.ofMillis(700));
                return "slow";
            });
        }
    }

    /** The stalled request's source, replaced per test and released in cleanup so it cannot leak past the class. */
    private static Sinks.Empty<String> stall = Sinks.empty();

    @Autowired
    private WebTestClient webClient;

    @BeforeEach
    void forgetRecordedExchanges() {
        RequestPhases.clear();
        stall = Sinks.empty();
    }

    @AfterEach
    void releaseTheStalledSource() {
        stall.tryEmitEmpty();
    }

    @Test
    @DisplayName("A completed request appears in the table with its terminal and elapsed time")
    void aCompletedRequestIsRendered() {
        webClient.get().uri("/probe").exchange().expectStatus().isOk();

        assertThat(RequestPhases.render())
                .as("a completed request renders its terminal boundary")
                .contains("GET")
                .contains("/probe")
                .contains("OK")
                .containsPattern("OK +#\\d+ GET +/probe[^\\n]*last=TERMINAL");
    }

    @Test
    @DisplayName("A failed request appears in the table with the failure's name")
    void aFailedRequestIsRendered() {
        webClient
                .get()
                .uri("/probe-failure")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError();

        assertThat(RequestPhases.render())
                .as("a failed exchange is rendered as such, not as still open")
                .contains("/probe-failure")
                .contains("FAILED")
                .contains("IllegalStateException");
    }

    /**
     * Subscribes a request whose handler never completes and asserts the row names the boundary it reached: the client
     * gives up at its own timeout, and the row must still be readable.
     */
    @Test
    @DisplayName("A subscribed request that never completes is rendered with its last reached boundary")
    void aStalledRequestIsRenderedWithItsLastBoundary() {
        WebTestClient shortTimeout =
                webClient.mutate().responseTimeout(Duration.ofMillis(400)).build();

        assertThatThrownBy(() -> shortTimeout.get().uri("/probe-stall").exchange())
                .as("the client gives up at its own timeout, which is the shape a CI stall produces")
                .hasMessageContaining("Timeout on blocking read");

        assertThat(RequestPhases.render())
                .as("the stalled request's row: admitted, never terminal, and nothing after that boundary")
                .containsPattern("OPEN +#\\d+ GET +/probe-stall[^\\n]*last=ADMITTED");
    }

    /**
     * Registers an open exchange through the filter and asserts the rendered table shows it: the chain is never
     * subscribed, so the exchange arrives and never reaches a terminal, which is the shape a stall leaves behind.
     */
    @Test
    @DisplayName("The scheduler figures render an unavailable state for no observation and a counted state otherwise")
    void theSchedulerFiguresDistinguishUnavailableFromMeasured() {
        assertThat(RequestPhases.queueFigure(0, 0))
                .as("no observed start is stated, never a zero")
                .startsWith("unavailable");
        assertThat(RequestPhases.runFigure(0, 0))
                .as("no observed return is stated, never a zero")
                .startsWith("unavailable");

        assertThat(RequestPhases.queueFigure(2, 15))
                .isEqualTo("max=15 ms over 2 starts, from each task's eligible time");
        assertThat(RequestPhases.runFigure(3, 702)).isEqualTo("max=702 ms over 3 returns");
    }

    @Test
    @DisplayName("A blocking request moves the scheduler aggregates and the request's row closes at its terminal")
    void aBlockingRequestMovesTheSchedulerAggregates() {
        val before = RequestPhases.render();

        webClient.get().uri("/probe-slow").exchange().expectStatus().isOk();

        assertThat(RequestPhases.render())
                .as("the aggregate figures move over their baseline, since the run's counters are not reset")
                .isNotEqualTo(before)
                .containsPattern("queue max=\\d+ ms over [1-9]\\d* starts")
                .containsPattern("run max=[1-9]\\d* ms over [1-9]\\d* returns")
                .containsPattern("OK +#\\d+ GET +/probe-slow[^\\n]*last=TERMINAL");
    }

    @Test
    @DisplayName("A request that never reached a terminal is rendered as open, still counting")
    void anOpenRequestIsRendered() {
        val exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/showcases").build());

        new RequestPhases().filter(exchange, ignored -> Mono.never());

        assertThat(RequestPhases.render())
                .as("an open request is the row a stall leaves behind")
                .contains("OPEN")
                .contains("POST")
                .contains("/showcases");

        RequestPhases.clear();
        assertThat(RequestPhases.render())
                .as("and the control's synthetic row does not outlive it")
                .doesNotContain("/showcases");
    }

    @Test
    @DisplayName("A failed recording renders its reason rather than a closed or zero-valued summary")
    void aFailedRecordingRendersItsReason() {
        val previous = JfrSummary.previousUnavailableReason();
        try {
            JfrSummary.noteUnavailableForTest("the stream failed: IllegalStateException");

            assertThat(JfrSummary.render())
                    .as("the failure's reason outranks a later closure and any zero counter")
                    .contains("recording: unavailable (the stream failed: IllegalStateException)");
        } finally {
            JfrSummary.noteUnavailableForTest(previous);
        }
    }

    @Test
    @DisabledIfSystemProperty(named = JfrSummary.ENABLED_PROPERTY, matches = "true")
    @DisplayName("Without the property the JFR summary says it is disabled rather than rendering zeroes")
    void theJfrSummaryDistinguishesAbsentObservations() {
        assertThat(JfrSummary.render())
                .as("an absent recording is stated, not rendered as a zero counter")
                .contains("recording: disabled (enable with -Pdiagnostics.jfr=true, which the build forwards as -D"
                        + JfrSummary.ENABLED_PROPERTY + "=true)");
    }

    @Test
    @EnabledIfSystemProperty(named = JfrSummary.ENABLED_PROPERTY, matches = "true")
    @DisplayName("The JFR summary counts a pinning event and a park caused by the control's own virtual thread")
    void theJfrSummaryRecordsPinningAndParking() {
        val pinnedBefore = countOf(JfrSummary.render(), "pinned");
        val parkedBefore = countOf(JfrSummary.render(), "parked");

        val monitor = new Object();
        Thread.ofVirtual().name("pinning-control").start(() -> {
            synchronized (monitor) {
                try {
                    Thread.sleep(Duration.ofMillis(200));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(countOf(JfrSummary.render(), "pinned"))
                        .as("the counter rises above the run's baseline, so an ambient event cannot satisfy it")
                        .isGreaterThan(pinnedBefore));
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(countOf(JfrSummary.render(), "parked"))
                        .as("the control's own monitor-blocked sleep is parked")
                        .isGreaterThan(parkedBefore));
    }

    /** Reads one counter out of the rendered summary, so the assertion reads what a failure would print. */
    private static long countOf(String summary, String counter) {
        val matcher = Pattern.compile(counter + " \\(>= 20 ms\\): (\\d+)").matcher(summary);
        assertThat(matcher.find())
                .as("the summary states the %s counter", counter)
                .isTrue();
        return Long.parseLong(matcher.group(1));
    }
}
