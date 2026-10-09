// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Client telemetry recorder unit tests")
class TelemetryRecorderTests {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final TelemetryRecorder recorder = new TelemetryRecorder(meterRegistry);

    @Test
    @DisplayName("A vital is recorded with its name, rating, and route")
    void record_vital_isRecordedWithItsNameRatingAndRoute() {
        recorder.record(reportWithVital(
                ClientVital.builder().name("LCP").rating("good").value(1234.0).build()));

        val summary = meterRegistry
                .get(TelemetryRecorder.VITALS_METRIC)
                .tag("metric", "LCP")
                .tag("rating", "good")
                .tag("route", "/")
                .summary();
        assertThat(summary.count()).isEqualTo(1);
        assertThat(summary.totalAmount()).isEqualTo(1234.0);
    }

    @Test
    @DisplayName("A route's query string is dropped before it is recorded")
    void record_routeQueryStringIsDropped() {
        recorder.record(ClientTelemetryReport.builder()
                .path("/?a=b")
                .vital(ClientVital.builder()
                        .name("CLS")
                        .rating("good")
                        .value(0.05)
                        .build())
                .build());

        assertThat(meterRegistry
                        .find(TelemetryRecorder.VITALS_METRIC)
                        .tag("route", "/")
                        .summary())
                .isNotNull();
    }

    @Test
    @DisplayName("An unknown route is recorded as other")
    void record_unknownRouteIsRecordedAsOther() {
        recorder.record(ClientTelemetryReport.builder()
                .path("/forged")
                .vital(ClientVital.builder()
                        .name("CLS")
                        .rating("good")
                        .value(0.05)
                        .build())
                .build());

        assertThat(meterRegistry
                        .find(TelemetryRecorder.VITALS_METRIC)
                        .tag("route", "other")
                        .summary())
                .isNotNull();
    }

    @Test
    @DisplayName("A known error type is recorded as-is")
    void record_knownErrorTypeIsRecordedAsIs() {
        recorder.record(reportWithError(error("TypeError")));

        assertThat(meterRegistry
                        .get(TelemetryRecorder.ERRORS_METRIC)
                        .tag("type", "TypeError")
                        .tag("route", "/")
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("An unrecognized error type is recorded as Error")
    void record_unrecognizedErrorTypeIsRecordedAsError() {
        recorder.record(reportWithError(error("CustomError")));

        assertThat(meterRegistry
                        .get(TelemetryRecorder.ERRORS_METRIC)
                        .tag("type", "Error")
                        .tag("route", "/")
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("A free-form message is not used as a metric label")
    void record_freeFormMessageIsNotAMetricLabel() {
        recorder.record(ClientTelemetryReport.builder()
                .path("/")
                .error(ClientError.builder()
                        .type("TypeError")
                        .message("a-secret-message")
                        .source("app.js")
                        .build())
                .build());

        val counter = meterRegistry
                .get(TelemetryRecorder.ERRORS_METRIC)
                .tag("type", "TypeError")
                .tag("route", "/")
                .counter();
        assertThat(counter.getId().getTags()).extracting(Tag::getKey).containsExactlyInAnyOrder("type", "route");
    }

    private static ClientTelemetryReport reportWithVital(ClientVital vital) {
        return ClientTelemetryReport.builder().path("/").vital(vital).build();
    }

    private static ClientTelemetryReport reportWithError(ClientError error) {
        return ClientTelemetryReport.builder().path("/").error(error).build();
    }

    private static ClientError error(String type) {
        return ClientError.builder().type(type).message("boom").source("app.js").build();
    }
}
