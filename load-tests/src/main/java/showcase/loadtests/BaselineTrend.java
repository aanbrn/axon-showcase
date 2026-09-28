// SPDX-License-Identifier: MIT
package showcase.loadtests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Reports the dated load-test records as a chronological series: each record's date, target, knee, operating point,
 * plateau duration, and the plateau's mean, 95th, and 99th percentile response time and mean throughput. A record whose
 * figures cannot be read is reported as unreadable rather than failing the view.
 */
public final class BaselineTrend {

    /**
     * The filename pattern carrying a record's date and optional same-day slug.
     */
    private static final Pattern FILENAME = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})(?:-(.+))?\\.md");

    /**
     * The record bullet naming the target measured.
     */
    private static final String TARGET_BULLET = "- Target:";

    /**
     * The record bullet naming the calibration knee.
     */
    private static final String KNEE_BULLET = "- Knee:";

    /**
     * The record bullet naming the operating point.
     */
    private static final String OPERATING_POINT_BULLET = "- Operating point:";

    /**
     * The record bullet naming the plateau duration.
     */
    private static final String PLATEAU_DURATION_BULLET = "- Plateau duration:";

    /**
     * The Gatling summary line carrying the plateau's mean response time.
     */
    private static final String SUMMARY_MEAN = "> mean response time";

    /**
     * The Gatling summary line carrying the plateau's 95th-percentile response time.
     */
    private static final String SUMMARY_P95 = "> response time 95th";

    /**
     * The Gatling summary line carrying the plateau's 99th-percentile response time.
     */
    private static final String SUMMARY_P99 = "> response time 99th";

    /**
     * The Gatling summary line carrying the plateau's mean throughput.
     */
    private static final String SUMMARY_THROUGHPUT = "> mean throughput";

    /**
     * The private constructor preventing instantiation.
     */
    private BaselineTrend() {}

    /**
     * Prints the trend over the records under the given directory.
     *
     * @param args the records directory
     * @throws IOException if the directory cannot be read
     */
    public static void main(String[] args) throws IOException {
        System.out.println(report(Path.of(args[0])));
    }

    /**
     * Reads the dated records under a directory, ordered by date and then by same-day slug.
     *
     * @param directory the records directory
     * @return the parsed records in date order
     * @throws IOException if the directory or a record cannot be read
     */
    static List<Entry> read(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        val entries = new ArrayList<Entry>();
        try (val paths = Files.list(directory)) {
            for (val path : paths.toList()) {
                val matcher = FILENAME.matcher(path.getFileName().toString());
                if (matcher.matches()) {
                    entries.add(parse(path, LocalDate.parse(matcher.group(1)), slugOf(matcher.group(2))));
                }
            }
        }
        entries.sort(Comparator.comparing(Entry::date).thenComparing(Entry::slug));
        return entries;
    }

    /**
     * Renders the trend over the records under a directory.
     *
     * @param directory the records directory
     * @return the trend report
     * @throws IOException if the directory or a record cannot be read
     */
    static String report(Path directory) throws IOException {
        val entries = read(directory);
        if (entries.isEmpty()) {
            return "baseline trend: no dated records under %s; nothing to trend\n".formatted(directory);
        }
        val text = new StringBuilder("baseline trend: %d record(s)\n".formatted(entries.size()));
        for (val entry : entries) {
            text.append("  ")
                    .append(entry.date())
                    .append(entry.slug().isEmpty() ? "" : " (" + entry.slug() + ")")
                    .append("  target: ")
                    .append(describe(entry.target()))
                    .append('\n');
            if (!entry.readable()) {
                text.append("    unreadable: no plateau figures\n");
                continue;
            }
            text.append("    knee: ").append(describe(entry.knee())).append('\n');
            text.append("    operating point: ")
                    .append(describe(entry.operatingPoint()))
                    .append('\n');
            text.append("    plateau: ")
                    .append(describe(entry.plateauDuration()))
                    .append('\n');
            text.append("    mean: ")
                    .append(entry.meanMs())
                    .append(" ms, p95: ")
                    .append(entry.p95Ms())
                    .append(" ms, p99: ")
                    .append(entry.p99Ms())
                    .append(" ms, throughput: ")
                    .append(entry.throughputRps() == null ? "unrecorded" : entry.throughputRps() + " rps")
                    .append('\n');
        }
        return text.toString();
    }

    /**
     * Parses one record file.
     *
     * @param path the record file
     * @param date the record's date, from its filename
     * @param slug the record's same-day slug, empty when the filename carries none
     * @return the parsed record
     * @throws IOException if the record cannot be read
     */
    private static Entry parse(Path path, LocalDate date, String slug) throws IOException {
        String target = null;
        String knee = null;
        String operatingPoint = null;
        String plateauDuration = null;
        Integer meanMs = null;
        Integer p95Ms = null;
        Integer p99Ms = null;
        Double throughputRps = null;
        for (val line : Files.readAllLines(path)) {
            val trimmed = line.strip();
            if (target == null && trimmed.startsWith(TARGET_BULLET)) {
                target = bulletValue(trimmed, TARGET_BULLET);
            } else if (knee == null && trimmed.startsWith(KNEE_BULLET)) {
                knee = bulletValue(trimmed, KNEE_BULLET);
            } else if (operatingPoint == null && trimmed.startsWith(OPERATING_POINT_BULLET)) {
                operatingPoint = bulletValue(trimmed, OPERATING_POINT_BULLET);
            } else if (plateauDuration == null && trimmed.startsWith(PLATEAU_DURATION_BULLET)) {
                plateauDuration = bulletValue(trimmed, PLATEAU_DURATION_BULLET);
            } else if (meanMs == null && trimmed.startsWith(SUMMARY_MEAN)) {
                meanMs = firstTableInt(trimmed);
            } else if (p95Ms == null && trimmed.startsWith(SUMMARY_P95)) {
                p95Ms = firstTableInt(trimmed);
            } else if (p99Ms == null && trimmed.startsWith(SUMMARY_P99)) {
                p99Ms = firstTableInt(trimmed);
            } else if (throughputRps == null && trimmed.startsWith(SUMMARY_THROUGHPUT)) {
                throughputRps = firstTableDouble(trimmed);
            }
        }
        return Entry.builder()
                .date(date)
                .slug(slug)
                .path(path)
                .target(target)
                .knee(knee)
                .operatingPoint(operatingPoint)
                .plateauDuration(plateauDuration)
                .meanMs(meanMs)
                .p95Ms(p95Ms)
                .p99Ms(p99Ms)
                .throughputRps(throughputRps)
                .build();
    }

    /**
     * Reads a bullet's text after its prefix.
     *
     * @param line the trimmed line
     * @param prefix the bullet prefix
     * @return the bullet's value
     */
    private static String bulletValue(String line, String prefix) {
        return line.substring(prefix.length()).strip();
    }

    /**
     * Reads the first numeric column of a Gatling summary line.
     *
     * @param line the trimmed summary line
     * @return the value, absent when the line carries no number
     */
    private static @Nullable Integer firstTableInt(String line) {
        val cell = firstCell(line);
        if (cell == null) {
            return null;
        }
        try {
            return Integer.valueOf(cell.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reads the first numeric column of a Gatling summary line as a decimal.
     *
     * @param line the trimmed summary line
     * @return the value, absent when the line carries no number
     */
    private static @Nullable Double firstTableDouble(String line) {
        val cell = firstCell(line);
        if (cell == null) {
            return null;
        }
        try {
            return Double.valueOf(cell.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reads the first value column of a pipe-separated summary line.
     *
     * @param line the trimmed summary line
     * @return the cell's text, absent when the line has no value column
     */
    private static @Nullable String firstCell(String line) {
        val start = line.indexOf('|');
        if (start < 0) {
            return null;
        }
        val end = line.indexOf('|', start + 1);
        return (end < 0 ? line.substring(start + 1) : line.substring(start + 1, end)).strip();
    }

    /**
     * Reads a filename's optional same-day slug.
     *
     * @param slug the captured slug, absent when the filename carries none
     * @return the slug, empty when absent
     */
    private static String slugOf(@Nullable String slug) {
        return slug == null ? "" : slug;
    }

    /**
     * A value's text or a marker for its absence.
     *
     * @param value the value, absent when unrecorded
     * @return the value or a marker
     */
    private static String describe(@Nullable String value) {
        return value == null ? "unrecorded" : value;
    }

    /**
     * One dated load-test record: its date and slug, and the figures the report reads from it.
     */
    @Value
    @Builder
    @Accessors(fluent = true)
    public static class Entry {

        /**
         * The record's date, from its filename.
         */
        LocalDate date;

        /**
         * The record's same-day slug, empty when the filename carries none.
         */
        String slug;

        /**
         * The record's file path.
         */
        Path path;

        /**
         * The target measured, absent when the record carries no target bullet.
         */
        @Nullable
        String target;

        /**
         * The calibration knee, absent when unrecorded.
         */
        @Nullable
        String knee;

        /**
         * The operating point, absent when unrecorded.
         */
        @Nullable
        String operatingPoint;

        /**
         * The plateau duration, absent when unrecorded.
         */
        @Nullable
        String plateauDuration;

        /**
         * The plateau's mean response time in milliseconds, absent when unreadable.
         */
        @Nullable
        Integer meanMs;

        /**
         * The plateau's 95th-percentile response time in milliseconds, absent when unreadable.
         */
        @Nullable
        Integer p95Ms;

        /**
         * The plateau's 99th-percentile response time in milliseconds, absent when unreadable.
         */
        @Nullable
        Integer p99Ms;

        /**
         * The plateau's mean throughput in requests per second, absent when unrecorded.
         */
        @Nullable
        Double throughputRps;

        /**
         * Whether the record's plateau figures are all readable.
         *
         * @return true when the mean, 95th, and 99th percentile are present
         */
        public boolean readable() {
            return meanMs != null && p95Ms != null && p99Ms != null;
        }
    }
}
