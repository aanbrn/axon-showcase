// SPDX-License-Identifier: MIT
package showcase.loadtests;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * The drift of a measured baseline reference against a recorded one: each read and write request's mean, 95th, and 99th
 * percentile response time, its delta, and whether it regresses beyond the configured tolerance. Also carries the write
 * decision — a regression beyond the tolerance withholds the recorded reference unless a refresh is intended, a
 * measurement with no figures always withholds it, and nothing to compare records the measurement.
 */
@Value
@Builder
@Accessors(fluent = true)
public class BaselineDrift {

    /**
     * The relative increase above a recorded figure that still passes, as a fraction.
     */
    public static final double DEFAULT_TOLERANCE = 0.5;

    /**
     * The drift floor for a mean response time, in milliseconds.
     */
    static final int FLOOR_MEAN_MS = 5;

    /**
     * The drift floor for a 95th-percentile response time, in milliseconds.
     */
    static final int FLOOR_P95_MS = 10;

    /**
     * The drift floor for a 99th-percentile response time, in milliseconds.
     */
    static final int FLOOR_P99_MS = 20;

    /**
     * The target the measured reference was recorded against.
     */
    String target;

    /**
     * Whether a recorded reference for this target exists to compare against.
     */
    boolean comparable;

    /**
     * The target the recorded reference names, absent when nothing was recorded.
     */
    @Nullable
    String recordedTarget;

    /**
     * The per-request figure deltas, in the measured reference's request order.
     */
    List<Figure> figures;

    /**
     * Whether the measured reference should be written, per the tolerance-gated policy.
     */
    boolean writeReference;

    /**
     * Whether the run measured no request figures at all.
     */
    boolean measuredEmpty;

    /**
     * Compares a measured reference against a recorded one.
     *
     * @param recorded the recorded reference, absent when none exists
     * @param measured the reference measured by this run
     * @param tolerance the relative increase above a recorded figure that still passes, as a fraction
     * @param refreshIntended whether a regression beyond the tolerance is accepted deliberately
     * @return the drift and its write decision
     */
    public static BaselineDrift compare(
            @Nullable BaselineReference recorded,
            BaselineReference measured,
            double tolerance,
            boolean refreshIntended) {
        val comparable = recorded != null && measured.target().equals(recorded.target());
        val measuredEmpty = measured.requests().isEmpty();
        val figures = new ArrayList<Figure>();
        if (recorded != null && comparable) {
            for (val request : requestOrder(recorded, measured)) {
                val measuredFigures = measured.requests().get(request);
                val recordedFigures = recorded.requests().get(request);
                addFigure(
                        figures,
                        request,
                        BaselineReference.MEAN,
                        measuredFigures == null ? null : measuredFigures.meanMs(),
                        recordedFigures == null ? null : recordedFigures.meanMs(),
                        FLOOR_MEAN_MS,
                        tolerance);
                addFigure(
                        figures,
                        request,
                        BaselineReference.P95,
                        measuredFigures == null ? null : measuredFigures.p95Ms(),
                        recordedFigures == null ? null : recordedFigures.p95Ms(),
                        FLOOR_P95_MS,
                        tolerance);
                addFigure(
                        figures,
                        request,
                        BaselineReference.P99,
                        measuredFigures == null ? null : measuredFigures.p99Ms(),
                        recordedFigures == null ? null : recordedFigures.p99Ms(),
                        FLOOR_P99_MS,
                        tolerance);
            }
        }
        val regressed = figures.stream().anyMatch(Figure::regressed);
        return BaselineDrift.builder()
                .target(measured.target())
                .comparable(comparable)
                .recordedTarget(recorded == null ? null : recorded.target())
                .figures(figures)
                .measuredEmpty(measuredEmpty)
                .writeReference(!measuredEmpty && (!comparable || !regressed || refreshIntended))
                .build();
    }

    /**
     * The request names to compare, the measured reference's first and any recorded-only names after.
     *
     * @param recorded the recorded reference
     * @param measured the measured reference
     * @return the request names in report order
     */
    private static List<String> requestOrder(BaselineReference recorded, BaselineReference measured) {
        val order = new LinkedHashSet<String>(measured.requests().keySet());
        order.addAll(recorded.requests().keySet());
        return List.copyOf(order);
    }

    /**
     * Adds a request's figure to the drift when either side records it.
     *
     * @param figures the collected figures
     * @param request the request name
     * @param metric the metric key
     * @param measured the measured figure, absent when the run did not record it
     * @param recorded the recorded figure, absent when the reference did not record it
     * @param floor the metric's drift floor in milliseconds
     * @param tolerance the relative increase above the recorded figure that still passes
     */
    private static void addFigure(
            List<Figure> figures,
            String request,
            String metric,
            @Nullable Integer measured,
            @Nullable Integer recorded,
            int floor,
            double tolerance) {
        if (measured == null && recorded == null) {
            return;
        }
        val threshold = recorded == null ? null : Math.max(floor, (int) Math.round(recorded * (1 + tolerance)));
        val regressed = measured != null && threshold != null && measured > threshold;
        figures.add(Figure.builder()
                .request(request)
                .metric(metric)
                .recordedMs(recorded)
                .measuredMs(measured)
                .thresholdMs(threshold)
                .regressed(regressed)
                .build());
    }

    /**
     * Whether any figure regresses beyond the tolerance.
     *
     * @return true when at least one figure regresses
     */
    public boolean regressed() {
        return figures.stream().anyMatch(Figure::regressed);
    }

    /**
     * The figures that regress beyond the tolerance.
     *
     * @return the regressed figures
     */
    public List<Figure> regressedFigures() {
        return figures.stream().filter(Figure::regressed).toList();
    }

    /**
     * The drift as a human-readable report.
     *
     * @return the report text
     */
    public String report() {
        if (measuredEmpty) {
            return "baseline drift: no request figures were measured; withholding the reference\n";
        }
        if (!comparable) {
            return recordedTarget == null
                    ? "baseline drift: no reference recorded for %s; nothing to compare\n".formatted(target)
                    : "baseline drift: the reference is recorded for %s, not %s; nothing to compare\n"
                            .formatted(recordedTarget, target);
        }
        val text = new StringBuilder("baseline drift against %s:\n".formatted(target));
        for (val figure : figures) {
            text.append("  ").append(figure.describe()).append('\n');
        }
        if (writeReference) {
            text.append(
                    regressed()
                            ? "  regressed beyond the tolerance; writing the reference with the regression accepted\n"
                            : "  within the tolerance; writing the reference\n");
        } else {
            text.append("  beyond the tolerance; withholding the reference\n");
        }
        return text.toString();
    }

    /**
     * A request's figure: its recorded and measured values, their delta, the threshold it faced, and whether it
     * regressed.
     */
    @Value
    @Builder
    @Accessors(fluent = true)
    public static class Figure {

        /**
         * The request name.
         */
        String request;

        /**
         * The metric key, one of the reference's figure suffixes.
         */
        String metric;

        /**
         * The recorded figure in milliseconds, absent when the reference did not record it.
         */
        @Nullable
        Integer recordedMs;

        /**
         * The measured figure in milliseconds, absent when the run did not record it.
         */
        @Nullable
        Integer measuredMs;

        /**
         * The threshold a measured figure may not exceed, absent when there is no recorded figure.
         */
        @Nullable
        Integer thresholdMs;

        /**
         * Whether the measured figure exceeds the threshold.
         */
        boolean regressed;

        /**
         * The measured minus recorded figure, absent when either side is unrecorded.
         *
         * @return the delta in milliseconds
         */
        public @Nullable Integer deltaMs() {
            return recordedMs == null || measuredMs == null ? null : measuredMs - recordedMs;
        }

        /**
         * The figure's label, as the reference records it.
         *
         * @return the label
         */
        public String label() {
            return request + "." + metric;
        }

        /**
         * The figure as a human-readable line.
         *
         * @return the description
         */
        public String describe() {
            if (recordedMs == null || measuredMs == null) {
                return "%s: %s -> %s".formatted(label(), describe(recordedMs), describe(measuredMs));
            }
            return "%s: %d -> %d ms (delta %+d, threshold %d ms)%s"
                    .formatted(
                            label(),
                            recordedMs,
                            measuredMs,
                            measuredMs - recordedMs,
                            thresholdMs,
                            regressed ? " REGRESSED" : "");
        }

        /**
         * A figure's value or its absence.
         *
         * @param value the figure, absent when unrecorded
         * @return the value in milliseconds or a marker for its absence
         */
        private static String describe(@Nullable Integer value) {
            return value == null ? "unrecorded" : value + " ms";
        }
    }
}
