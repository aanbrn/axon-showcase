// SPDX-License-Identifier: MIT
package showcase.loadtests;

import io.gatling.charts.stats.GeneralStats;
import io.gatling.charts.stats.LogFileData;
import io.gatling.charts.stats.LogFileReader;
import io.gatling.commons.stats.Status;
import io.gatling.core.config.GatlingConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import lombok.val;
import scala.Option;

/**
 * Records the baseline reference from a Gatling simulation log: the plateau's mean, 95th, and 99th percentile response
 * time per read and write request, with the derivation policy the below-knee profiles apply. Compares the measurement
 * against the reference recorded for the target it measured, reports the deltas, and writes the reference unless a
 * figure regresses beyond the tolerance with no refresh intended.
 */
public final class BaselineStats {

    /**
     * The multiple of a baseline value a derived threshold allows.
     */
    private static final int DEFAULT_FACTOR = 5;

    /**
     * The floor the derived mean threshold never goes below, in milliseconds.
     */
    private static final int DEFAULT_FLOOR_MEAN_MS = 50;

    /**
     * The floor the derived 95th-percentile threshold never goes below, in milliseconds.
     */
    private static final int DEFAULT_FLOOR_P95_MS = 100;

    /**
     * The floor the derived 99th-percentile threshold never goes below, in milliseconds.
     */
    private static final int DEFAULT_FLOOR_P99_MS = 200;

    /**
     * The Gatling status whose responses form the baseline.
     */
    private static final String OK = "OK";

    /**
     * The private constructor preventing instantiation.
     */
    private BaselineStats() {}

    /**
     * Compares the baseline reference derived from the given simulation log against the one recorded for the target,
     * reports the deltas, and writes the reference per the tolerance-gated policy.
     *
     * @param args the simulation log path, the reference path, the target the log was recorded against, whether a
     *     refresh is intended, and the tolerance as a percentage
     * @throws IOException if the log cannot be read or the reference cannot be written
     */
    public static void main(String[] args) throws IOException {
        val logPath = args[0];
        val output = Path.of(args[1]);
        val target = args[2];
        val refreshIntended = args.length > 3 && isTruthy(args[3]);
        val tolerance = args.length > 4 ? Double.parseDouble(args[4]) / 100.0 : BaselineDrift.DEFAULT_TOLERANCE;

        val data = new LogFileReader(new File(logPath), GatlingConfiguration.load()).read();
        val measured = measure(data, target);
        val recorded = Files.exists(output) ? BaselineReference.parse(Files.readString(output)) : null;
        val drift = BaselineDrift.compare(recorded, measured, tolerance, refreshIntended);
        System.out.print(drift.report());

        if (drift.writeReference()) {
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            Files.writeString(output, measured.render());
            System.out.printf("wrote %s for %s%n", output, target);
        } else if (drift.measuredEmpty()) {
            System.err.printf("withheld %s: the run measured no request figures%n", output);
            System.exit(1);
        } else {
            System.err.printf(
                    "withheld %s: regressed %s%n",
                    output,
                    drift.regressedFigures().stream()
                            .map(BaselineDrift.Figure::label)
                            .toList());
            System.exit(1);
        }
    }

    /**
     * Reads whether a refresh is intended from a truthy argument value.
     *
     * @param value the argument value
     * @return true when the value is {@code true} or {@code 1}
     */
    private static boolean isTruthy(String value) {
        return "1".equals(value) || Boolean.parseBoolean(value);
    }

    /**
     * Builds the reference measured by this run from the simulation log.
     *
     * @param data the simulation log's data
     * @param target the target the log was recorded against
     * @return the measured reference
     */
    private static BaselineReference measure(LogFileData data, String target) {
        val requests = new LinkedHashMap<String, BaselineReference.Figures>();
        for (val name : LoadTestRequests.READ_WRITE) {
            val stats = data.requestGeneralStats(Option.apply(name), Option.empty(), Option.apply(Status.apply(OK)));
            if (stats.isDefined()) {
                val general = stats.get();
                requests.put(
                        name,
                        BaselineReference.Figures.builder()
                                .meanMs(general.mean())
                                .p95Ms(percentile(general, 95.0))
                                .p99Ms(percentile(general, 99.0))
                                .build());
            }
        }
        return BaselineReference.builder()
                .target(target)
                .recordedAt(Instant.now())
                .factor(DEFAULT_FACTOR)
                .floorMeanMs(DEFAULT_FLOOR_MEAN_MS)
                .floorP95Ms(DEFAULT_FLOOR_P95_MS)
                .floorP99Ms(DEFAULT_FLOOR_P99_MS)
                .requests(requests)
                .build();
    }

    /**
     * Reads a percentile from the gathered statistics.
     *
     * @param stats the gathered statistics
     * @param percentile the percentile to read, between 0 and 100
     * @return the percentile's value in milliseconds
     */
    private static int percentile(GeneralStats stats, double percentile) {
        return ((Number) stats.percentile().apply(percentile)).intValue();
    }
}
