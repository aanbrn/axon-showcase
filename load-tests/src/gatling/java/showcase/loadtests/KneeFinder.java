// SPDX-License-Identifier: MIT
package showcase.loadtests;

import io.gatling.charts.stats.CountsVsTimePlot;
import io.gatling.charts.stats.LogFileData;
import io.gatling.charts.stats.LogFileReader;
import io.gatling.charts.stats.PercentilesVsTimePlot;
import io.gatling.commons.stats.Status;
import io.gatling.core.config.GatlingConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import scala.Option;
import scala.jdk.javaapi.CollectionConverters;

/**
 * Derives the load knee — the workload rate at which the response time departs the flat baseline — from a Gatling
 * simulation log, and writes the knee and the operating point below it as properties.
 */
public final class KneeFinder {

    /**
     * The request names that delimit a workload unit: one list fetch per read iteration and one remove per completed
     * write lifecycle.
     */
    private static final List<String> UNIT_REQUESTS = List.of("FetchShowcases", "RemoveShowcase");

    /**
     * The number of leading buckets treated as warm-up and excluded from the baseline.
     */
    private static final int WARM_UP_BUCKETS = 3;

    /**
     * The number of consecutive buckets over the threshold that confirm the knee.
     */
    private static final int CONFIRMATION_BUCKETS = 3;

    /**
     * The factor above the baseline p95 that marks the departure.
     */
    private static final double DEPARTURE_FACTOR = 3.0;

    /**
     * The absolute departure below which a response time is not considered a departure.
     */
    private static final int MIN_ABSOLUTE_DEPARTURE_MS = 50;

    /**
     * The fraction of the knee at which the operating point sits.
     */
    private static final double OPERATING_POINT_FRACTION = 0.6;

    /**
     * The Gatling status whose responses form the latency series.
     */
    private static final String OK = "OK";

    /**
     * The private constructor preventing instantiation.
     */
    private KneeFinder() {}

    /**
     * Writes the knee properties derived from the given simulation log.
     *
     * @param args the simulation log path and the properties output path
     * @throws IOException if the log cannot be read or the properties cannot be written
     */
    public static void main(String[] args) throws IOException {
        LogFileData data = new LogFileReader(new File(args[0]), GatlingConfiguration.load()).read();

        Map<Integer, Integer> unitsPerSecond = new TreeMap<>();
        for (String request : UNIT_REQUESTS) {
            for (CountsVsTimePlot counts : CollectionConverters.asJava(
                    data.numberOfRequestsPerSecond(Option.apply(request), Option.empty()))) {
                unitsPerSecond.merge(counts.time(), counts.oks() + counts.kos(), Integer::sum);
            }
        }
        Map<Integer, Integer> p95PerSecond = new TreeMap<>();
        for (PercentilesVsTimePlot plot : CollectionConverters.asJava(
                data.responseTimePercentilesOverTime(Status.apply(OK), Option.empty(), Option.empty()))) {
            if (plot.percentiles().isDefined()) {
                p95PerSecond.put(plot.time(), plot.percentiles().get().percentile95());
            }
        }

        List<Integer> times = List.copyOf(unitsPerSecond.keySet());
        int baselineP95 = times.stream()
                .limit(WARM_UP_BUCKETS)
                .mapToInt(time -> p95PerSecond.getOrDefault(time, Integer.MAX_VALUE))
                .min()
                .orElse(0);
        int threshold = Math.max(MIN_ABSOLUTE_DEPARTURE_MS, (int) Math.round(baselineP95 * DEPARTURE_FACTOR));

        int knee = 0;
        int consecutive = 0;
        int candidateRate = 0;
        for (int i = WARM_UP_BUCKETS; i < times.size(); i++) {
            int time = times.get(i);
            int units = unitsPerSecond.getOrDefault(time, 0);
            if (units > 0 && p95PerSecond.getOrDefault(time, 0) >= threshold) {
                if (consecutive == 0) {
                    candidateRate = units;
                }
                consecutive++;
                if (consecutive >= CONFIRMATION_BUCKETS) {
                    knee = candidateRate;
                    break;
                }
            } else {
                consecutive = 0;
            }
        }
        boolean measured = knee != 0;
        if (!measured) {
            knee = unitsPerSecond.values().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);
        }
        int operatingPoint = Math.max(1, (int) Math.round(knee * OPERATING_POINT_FRACTION));

        Path output = Path.of(args[1]);
        Files.createDirectories(output.getParent());
        Files.writeString(
                output,
                "knee=%d%noperatingPoint=%d%nmeasured=%s%nbaselineP95Ms=%d%n"
                        .formatted(knee, operatingPoint, measured, baselineP95));
        System.out.printf(
                "knee=%d units/s (%s), operatingPoint=%d units/s, baselineP95=%d ms%n",
                knee, measured ? "measured" : "ceiling", operatingPoint, baselineP95);
    }
}
