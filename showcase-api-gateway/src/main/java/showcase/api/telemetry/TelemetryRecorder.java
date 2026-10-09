// SPDX-License-Identifier: MIT
package showcase.api.telemetry;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Records client telemetry as Prometheus metrics.
 *
 * <p>A Core Web Vital is recorded into a distribution and a JavaScript error into a counter, tagged only with bounded
 * labels, so a forged or custom value cannot create unbounded metric cardinality. The free-form error message and
 * source are logged at a capped length rather than used as labels.
 */
@Component
@RequiredArgsConstructor
@Slf4j
final class TelemetryRecorder {
    /**
     * The distribution recording each Core Web Vital.
     */
    static final String VITALS_METRIC = "showcase.web.vitals";

    /**
     * The counter recording each JavaScript error.
     */
    static final String ERRORS_METRIC = "showcase.web.errors";

    /**
     * The label value recorded for a route outside the known application routes.
     */
    private static final String OTHER_ROUTE = "other";

    /**
     * The label value recorded for an unrecognized error type.
     */
    private static final String UNKNOWN_ERROR_TYPE = "Error";

    /**
     * The application routes recorded under their own label value.
     */
    private static final Set<String> KNOWN_ROUTES = Set.of("/");

    /**
     * The standard JavaScript error names recorded under their own label value.
     */
    private static final Set<String> KNOWN_ERROR_TYPES =
            Set.of("Error", "TypeError", "RangeError", "ReferenceError", "SyntaxError", "URIError", "EvalError");

    /**
     * The maximum length of a free-form value before it is logged.
     */
    private static final int MAX_LOGGED_LENGTH = 500;

    /**
     * The lower bound of a vital's histogram range.
     */
    private static final double MIN_EXPECTED_VALUE = 0.001;

    /**
     * The histogram's upper bound for a vital without a vital-specific range.
     */
    private static final double DEFAULT_MAX_EXPECTED_VALUE = 10_000;

    /**
     * The histogram's upper bound per vital name.
     */
    private static final Map<String, Double> MAX_EXPECTED_VALUES =
            Map.of("LCP", 10_000.0, "FCP", 10_000.0, "TTFB", 5_000.0, "INP", 1_000.0, "CLS", 1.0);

    /**
     * The registry the telemetry metrics are recorded into.
     */
    private final MeterRegistry meterRegistry;

    /**
     * Records a report's vitals and errors.
     *
     * @param report the report to record
     */
    void record(ClientTelemetryReport report) {
        val route = normalizeRoute(report.path());
        report.vitals()
                .forEach(vital -> DistributionSummary.builder(VITALS_METRIC)
                        .tags("metric", vital.name(), "rating", vital.rating(), "route", route)
                        .minimumExpectedValue(MIN_EXPECTED_VALUE)
                        .maximumExpectedValue(
                                MAX_EXPECTED_VALUES.getOrDefault(vital.name(), DEFAULT_MAX_EXPECTED_VALUE))
                        .publishPercentileHistogram()
                        .register(meterRegistry)
                        .record(vital.value()));
        report.errors().forEach(error -> {
            val type = normalizeErrorType(error.type());
            meterRegistry.counter(ERRORS_METRIC, "type", type, "route", route).increment();
            log.warn(
                    "Client telemetry error: type={}, source={}, message={}",
                    type,
                    truncate(error.source()),
                    truncate(error.message()));
        });
    }

    /**
     * Normalizes a route into a bounded label value: the query string is dropped, and a route outside the known
     * application routes becomes {@code other}.
     *
     * @param path the reported route
     * @return the normalized route label
     */
    private static String normalizeRoute(String path) {
        val queryStart = path.indexOf('?');
        val route = queryStart >= 0 ? path.substring(0, queryStart) : path;
        return KNOWN_ROUTES.contains(route) ? route : OTHER_ROUTE;
    }

    /**
     * Normalizes an error type into a bounded label value: a name the standard error constructors emit is kept, and
     * anything else becomes {@code Error}.
     *
     * @param type the reported error type, possibly {@code null}
     * @return the normalized error-type label
     */
    private static String normalizeErrorType(@Nullable String type) {
        return type != null && KNOWN_ERROR_TYPES.contains(type) ? type : UNKNOWN_ERROR_TYPE;
    }

    /**
     * Caps a free-form value before it is logged.
     *
     * @param value the value, possibly {@code null}
     * @return the value capped at the maximum logged length
     */
    private static String truncate(@Nullable String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= MAX_LOGGED_LENGTH ? value : value.substring(0, MAX_LOGGED_LENGTH);
    }
}
