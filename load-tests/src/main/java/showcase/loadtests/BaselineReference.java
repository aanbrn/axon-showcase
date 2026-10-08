// SPDX-License-Identifier: MIT
package showcase.loadtests;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * The load-test baseline reference: the operating point the plateau ran at and the plateau's mean, 95th, and 99th
 * percentile response time per read and write request, with the derivation policy the below-knee profiles apply and the
 * target it was measured against. Parses and renders the properties text the {@code baselineStats} task writes.
 */
@Value
@Builder
@Accessors(fluent = true)
public class BaselineReference {

    /**
     * The comment header the reference file carries.
     */
    public static final String HEADER =
            "# The load-test baseline reference: the operating point the plateau ran at and its response times.\n"
                    + "# A baseline run writes it; the below-knee profiles derive their thresholds from it as\n"
                    + "# max(floor, factor x baseline), and ignore it when its target is not theirs.\n";

    /**
     * The metric key of a request's mean response time.
     */
    public static final String MEAN = "meanMs";

    /**
     * The metric key of a request's 95th-percentile response time.
     */
    public static final String P95 = "p95Ms";

    /**
     * The metric key of a request's 99th-percentile response time.
     */
    public static final String P99 = "p99Ms";

    /**
     * The target the reference was measured against.
     */
    String target;

    /**
     * The instant the reference was recorded, absent when a hand-written reference omits it.
     */
    @Nullable
    Instant recordedAt;

    /**
     * The operating point the plateau ran at, in workload units per second, absent when a hand-written or pre-change
     * reference omits it.
     */
    @Nullable
    Integer operatingPoint;

    /**
     * The multiple of a baseline value a derived threshold allows.
     */
    int factor;

    /**
     * The floor the derived mean threshold never goes below, in milliseconds.
     */
    int floorMeanMs;

    /**
     * The floor the derived 95th-percentile threshold never goes below, in milliseconds.
     */
    int floorP95Ms;

    /**
     * The floor the derived 99th-percentile threshold never goes below, in milliseconds.
     */
    int floorP99Ms;

    /**
     * The per-request figures, in the order the reference records them.
     */
    Map<String, Figures> requests;

    /**
     * A request's mean, 95th, and 99th percentile response times, each absent when the reference omits it.
     */
    @Value
    @Builder
    @Accessors(fluent = true)
    public static class Figures {

        /**
         * The request's mean response time in milliseconds, absent when unrecorded.
         */
        @Nullable
        Integer meanMs;

        /**
         * The request's 95th-percentile response time in milliseconds, absent when unrecorded.
         */
        @Nullable
        Integer p95Ms;

        /**
         * The request's 99th-percentile response time in milliseconds, absent when unrecorded.
         */
        @Nullable
        Integer p99Ms;
    }

    /**
     * Parses a reference from its properties text, preserving the order the request keys appear in.
     *
     * @param text the reference's properties text
     * @return the parsed reference
     */
    public static BaselineReference parse(String text) {
        val parsed = new LinkedHashMap<String, Map<String, Integer>>();
        var target = "";
        Instant recordedAt = null;
        Integer operatingPoint = null;
        var factor = 0;
        var floorMeanMs = 0;
        var floorP95Ms = 0;
        var floorP99Ms = 0;
        for (val line : text.split("\n", -1)) {
            val trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            val separator = trimmed.indexOf('=');
            if (separator < 0) {
                continue;
            }
            val key = trimmed.substring(0, separator);
            val value = trimmed.substring(separator + 1).strip();
            switch (key) {
                case "target" -> target = value;
                case "recordedAt" -> recordedAt = Instant.parse(value);
                case "operatingPoint" -> operatingPoint = Integer.parseInt(value);
                case "factor" -> factor = Integer.parseInt(value);
                case "floorMeanMs" -> floorMeanMs = Integer.parseInt(value);
                case "floorP95Ms" -> floorP95Ms = Integer.parseInt(value);
                case "floorP99Ms" -> floorP99Ms = Integer.parseInt(value);
                default -> {
                    val dot = key.lastIndexOf('.');
                    if (dot > 0) {
                        parsed.computeIfAbsent(key.substring(0, dot), ignored -> new LinkedHashMap<>())
                                .put(key.substring(dot + 1), Integer.parseInt(value));
                    }
                }
            }
        }
        val requests = new LinkedHashMap<String, Figures>();
        for (val entry : parsed.entrySet()) {
            val figures = entry.getValue();
            requests.put(
                    entry.getKey(),
                    Figures.builder()
                            .meanMs(figures.get(MEAN))
                            .p95Ms(figures.get(P95))
                            .p99Ms(figures.get(P99))
                            .build());
        }
        return BaselineReference.builder()
                .target(target)
                .recordedAt(recordedAt)
                .operatingPoint(operatingPoint)
                .factor(factor)
                .floorMeanMs(floorMeanMs)
                .floorP95Ms(floorP95Ms)
                .floorP99Ms(floorP99Ms)
                .requests(requests)
                .build();
    }

    /**
     * Renders the reference in the properties format the {@code baselineStats} task writes.
     *
     * @return the reference's properties text
     */
    public String render() {
        val text = new StringBuilder(HEADER);
        text.append("target=").append(target).append('\n');
        if (recordedAt != null) {
            text.append("recordedAt=").append(recordedAt).append('\n');
        }
        if (operatingPoint != null) {
            text.append("operatingPoint=").append(operatingPoint).append('\n');
        }
        text.append("factor=").append(factor).append('\n');
        text.append("floorMeanMs=").append(floorMeanMs).append('\n');
        text.append("floorP95Ms=").append(floorP95Ms).append('\n');
        text.append("floorP99Ms=").append(floorP99Ms).append('\n');
        for (val entry : requests.entrySet()) {
            val figures = entry.getValue();
            append(text, entry.getKey(), MEAN, figures.meanMs());
            append(text, entry.getKey(), P95, figures.p95Ms());
            append(text, entry.getKey(), P99, figures.p99Ms());
        }
        return text.toString();
    }

    /**
     * Appends a request figure to the rendered reference when it is recorded.
     *
     * @param text the rendered reference
     * @param request the request name
     * @param metric the metric key
     * @param value the figure, absent when unrecorded
     */
    private static void append(StringBuilder text, String request, String metric, @Nullable Integer value) {
        if (value != null) {
            text.append(request)
                    .append('.')
                    .append(metric)
                    .append('=')
                    .append(value)
                    .append('\n');
        }
    }
}
