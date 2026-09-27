// SPDX-License-Identifier: MIT
package showcase.loadtests;

import io.gatling.charts.stats.GeneralStats;
import io.gatling.charts.stats.LogFileReader;
import io.gatling.commons.stats.Status;
import io.gatling.core.config.GatlingConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import lombok.val;
import scala.Option;

/**
 * Records the baseline reference from a Gatling simulation log: the plateau's mean, 95th, and 99th percentile response
 * time per read and write request, with the derivation policy the below-knee profiles apply.
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
     * Writes the baseline reference derived from the given simulation log.
     *
     * @param args the simulation log path, the reference output path, and the target the log was recorded against
     * @throws IOException if the log cannot be read or the reference cannot be written
     */
    public static void main(String[] args) throws IOException {
        val data = new LogFileReader(new File(args[0]), GatlingConfiguration.load()).read();
        val reference = new StringBuilder();
        reference.append(
                "# The load-test baseline reference: the plateau's response times per read and write request.\n");
        reference.append("# A baseline run writes it; the below-knee profiles derive their thresholds from it as\n");
        reference.append("# max(floor, factor x baseline), and ignore it when its target is not theirs.\n");
        reference.append("target=").append(args[2]).append('\n');
        reference.append("recordedAt=").append(Instant.now()).append('\n');
        reference.append("factor=").append(DEFAULT_FACTOR).append('\n');
        reference.append("floorMeanMs=").append(DEFAULT_FLOOR_MEAN_MS).append('\n');
        reference.append("floorP95Ms=").append(DEFAULT_FLOOR_P95_MS).append('\n');
        reference.append("floorP99Ms=").append(DEFAULT_FLOOR_P99_MS).append('\n');
        for (val name : LoadTestRequests.READ_WRITE) {
            val stats = data.requestGeneralStats(Option.apply(name), Option.empty(), Option.apply(Status.apply(OK)));
            if (stats.isDefined()) {
                val general = stats.get();
                reference.append(name).append(".meanMs=").append(general.mean()).append('\n');
                reference
                        .append(name)
                        .append(".p95Ms=")
                        .append(percentile(general, 95.0))
                        .append('\n');
                reference
                        .append(name)
                        .append(".p99Ms=")
                        .append(percentile(general, 99.0))
                        .append('\n');
            }
        }
        val output = Path.of(args[1]);
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        Files.writeString(output, reference.toString());
        System.out.printf("wrote %s for %s%n", output, args[2]);
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
