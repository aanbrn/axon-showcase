// SPDX-License-Identifier: MIT
package showcase.api.diagnostics;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordingStream;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * Records pinning, parked threads and CPU load as supplementary JVM-wide context, and says plainly when it has none.
 *
 * <p>The gateway controller suites stall on CI (`docs/ideas.md`, 2026-10-06). The per-request boundaries live in
 * {@link RequestPhases}; this class answers the surrounding JVM question — was the machine busy, and were virtual
 * threads pinned or parked — so a stall's reader can tell a request-path cause from a starved host. It is
 * supplementary: a JVM-wide figure cannot attribute a stall to a request, and the summary says so rather than implying
 * it.
 *
 * <p>Three properties keep it from misleading a reader or harming a run:
 * <ul>
 *   <li><strong>Off unless asked for.</strong> Recording is enabled only with {@code -Dshowcase.diagnostics.jfr=true};
 *       otherwise the summary says it is disabled, so a zero is never read as "nothing happened".
 *   <li><strong>Health is reported.</strong> The summary states whether the recording started, how many events and CPU
 *       samples it has, and why it is unavailable when it is — an absent observation is distinguishable from a zero.
 *   <li><strong>Contained.</strong> Aggregation state is bounded, and a failure to start or stream disables the summary
 *       with a reason instead of failing a test that has nothing to do with it. The counters are operational — they
 *       cover the whole suite and are never reset by tests, so a control's own events are part of the figures it reads.
 * </ul>
 *
 * <p>The per-virtual-thread start and end events are deliberately not recorded: they fire once per task, and a sampled
 * count could not attribute a thread to a request at all.
 */
public final class JfrSummary {

    /** The property that turns the recording on, so its cost is a deliberate choice rather than a default. */
    public static final String ENABLED_PROPERTY = "showcase.diagnostics.jfr";

    /** How many distinct park frames the summary keeps. */
    private static final int KEPT_FRAMES = 8;

    private static final Duration PARK_THRESHOLD = Duration.ofMillis(20);

    private static final AtomicLong PINNED = new AtomicLong();

    private static final AtomicLong PARKED = new AtomicLong();

    private static final AtomicLong CPU_SAMPLES = new AtomicLong();

    private static final AtomicLong EVENTS = new AtomicLong();

    private static final AtomicReference<Double> PEAK_MACHINE_CPU = new AtomicReference<>(0.0);

    private static final AtomicReference<Double> PEAK_JVM_CPU = new AtomicReference<>(0.0);

    private static final Map<String, Long> PARK_FRAMES = new ConcurrentHashMap<>();

    private static final AtomicReference<@Nullable String> UNAVAILABLE = new AtomicReference<>();

    private static final AtomicReference<@Nullable String> CLOSED = new AtomicReference<>();

    private static final @Nullable RecordingStream RECORDING = start();

    private JfrSummary() {}

    /** Starts the recording when the property asks for it; a failure is recorded as the reason it is unavailable. */
    private static @Nullable RecordingStream start() {
        if (!Boolean.getBoolean(ENABLED_PROPERTY)) {
            return null;
        }
        try {
            val recording = new RecordingStream();
            recording.enable("jdk.VirtualThreadPinned").withThreshold(PARK_THRESHOLD);
            recording.enable("jdk.ThreadPark").withThreshold(PARK_THRESHOLD);
            recording.enable("jdk.CPULoad").withPeriod(Duration.ofSeconds(1));
            recording.onEvent("jdk.VirtualThreadPinned", event -> {
                EVENTS.incrementAndGet();
                PINNED.incrementAndGet();
            });
            recording.onEvent("jdk.ThreadPark", event -> {
                EVENTS.incrementAndGet();
                PARKED.incrementAndGet();
                countFrames(event);
            });
            recording.onEvent("jdk.CPULoad", event -> {
                EVENTS.incrementAndGet();
                CPU_SAMPLES.incrementAndGet();
                increaseTo(PEAK_MACHINE_CPU, scalar(event, "machineTotal"));
                increaseTo(PEAK_JVM_CPU, scalar(event, "jvmUser") + scalar(event, "jvmSystem"));
            });
            recording.onError(failure ->
                    UNAVAILABLE.set("the stream failed: " + failure.getClass().getSimpleName()));
            recording.onClose(() -> CLOSED.set("closed"));
            recording.startAsync();
            Runtime.getRuntime().addShutdownHook(new Thread(recording::close, "jfr-summary-close"));
            return recording;
        } catch (RuntimeException failure) {
            UNAVAILABLE.set(
                    "the recording failed to start: " + failure.getClass().getSimpleName());
            return null;
        }
    }

    /** Records a reason the summary should render as unavailable, so a control can exercise that branch. */
    static void noteUnavailableForTest(@Nullable String reason) {
        UNAVAILABLE.set(reason);
    }

    /** The current unavailable reason, so a control can restore the state it found rather than leaving its own. */
    static @Nullable String previousUnavailableReason() {
        return UNAVAILABLE.get();
    }

    /** Touches the class so the recording starts before a suite's first test, when the property enables it. */
    public static void ensureStarted() {}

    /** Renders the counters, the health of the recording, and the busiest park frames as a bounded summary. */
    public static String render() {
        val rendered = new StringBuilder("=== JFR summary (supplementary JVM-wide context) ===\n");
        if (RECORDING == null) {
            rendered.append("recording: ")
                    .append(
                            UNAVAILABLE.get() == null
                                    ? "disabled (enable with -D" + ENABLED_PROPERTY + "=true)"
                                    : "unavailable (" + UNAVAILABLE.get() + ")")
                    .append('\n');
            return rendered.toString();
        }
        if (UNAVAILABLE.get() != null) {
            rendered.append("recording: unavailable (")
                    .append(UNAVAILABLE.get())
                    .append(")\n");
            return rendered.toString();
        }
        if (CLOSED.get() != null) {
            rendered.append("recording: closed before the summary was read\n");
            return rendered.toString();
        }
        rendered.append("recording: running since the suite started, events=")
                .append(EVENTS.get())
                .append(" cpu samples=")
                .append(CPU_SAMPLES.get())
                .append('\n');
        rendered.append("pinned (>= ")
                .append(PARK_THRESHOLD.toMillis())
                .append(" ms): ")
                .append(PINNED.get())
                .append('\n');
        rendered.append("parked (>= ")
                .append(PARK_THRESHOLD.toMillis())
                .append(" ms): ")
                .append(PARKED.get())
                .append('\n');
        rendered.append(topFrames());
        if (PARK_FRAMES.size() >= KEPT_FRAMES) {
            rendered.append("    (the ranking is capped at ")
                    .append(KEPT_FRAMES)
                    .append(" distinct frames; later new frames are omitted, retained ones keep counting)\n");
        }
        rendered.append("peak CPU over the same window: ")
                .append(CPU_SAMPLES.get() == 0 ? "unavailable (no sample yet)" : cpuPeaks())
                .append('\n');
        return rendered.toString();
    }

    /** Stops the recording, for a suite that wants to release its thread explicitly. */
    public static void close() {
        if (RECORDING != null) {
            RECORDING.close();
        }
    }

    private static String cpuPeaks() {
        return "machine=" + percent(PEAK_MACHINE_CPU.get()) + " jvm=" + percent(PEAK_JVM_CPU.get());
    }

    private static double scalar(RecordedEvent event, String field) {
        return event.hasField(field) ? event.getFloat(field) : 0.0;
    }

    private static void increaseTo(AtomicReference<Double> peak, double candidate) {
        peak.accumulateAndGet(candidate, Math::max);
    }

    private static void countFrames(RecordedEvent event) {
        val stack = event.getStackTrace();
        if (stack == null) {
            return;
        }
        val javaFrames = javaFrames(stack);
        if (javaFrames.isEmpty()) {
            return;
        }
        val frame = javaFrames.getFirst();
        if (PARK_FRAMES.containsKey(frame)) {
            PARK_FRAMES.merge(frame, 1L, Long::sum);
            return;
        }
        if (PARK_FRAMES.size() < KEPT_FRAMES) {
            PARK_FRAMES.put(frame, 1L);
        }
    }

    private static String topFrames() {
        val rendered = new StringBuilder();
        PARK_FRAMES.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(KEPT_FRAMES)
                .forEach(frame -> rendered.append("    ")
                        .append(frame.getValue())
                        .append(" x ")
                        .append(frame.getKey())
                        .append('\n'));
        return rendered.toString();
    }

    private static java.util.List<String> javaFrames(RecordedStackTrace stack) {
        return stack.getFrames().stream()
                .filter(RecordedFrame::isJavaFrame)
                .map(RecordedFrame::getMethod)
                .map(method -> method.getType().getName() + "." + method.getName())
                .toList();
    }

    private static String percent(double fraction) {
        return String.format("%.0f%%", fraction * 100);
    }
}
