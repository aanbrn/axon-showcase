// SPDX-License-Identifier: MIT
package showcase.projection;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Records the projector's operating state, its in-flight batch count, and when it last resolved a batch.
 *
 * <p>The state says what the projector has reached, not what it intends: `RUNNING` means the consumer has reached
 * partition assignment since startup or the latest stream failure, not that the broker is reachable or that progress
 * continues.
 */
@Component
class ShowcaseProjectorStateMonitor {
    /**
     * The projector's operating state.
     */
    enum State {
        /**
         * Not consuming: never started, starting without an assigned partition, or stopped.
         */
        STOPPED,

        /**
         * Consuming: the Kafka consumer holds partitions, so records can arrive.
         */
        RUNNING,

        /**
         * The stream failed and waits for its restart delay.
         */
        RETRYING,
    }

    /**
     * The current state.
     */
    @Getter
    private volatile State state = State.STOPPED;

    /**
     * The number of batches currently being written.
     */
    private final AtomicInteger inFlight = new AtomicInteger();

    /**
     * The epoch second of the last resolved batch, {@code 0} before the first one.
     */
    private final AtomicLong lastProgressEpochSecond = new AtomicLong();

    /**
     * The last stream failure that moved the projector into {@link State#RETRYING}, or {@code null} before one.
     */
    @Nullable
    private volatile String lastFailure;

    /**
     * The clock used to timestamp the last resolved batch.
     */
    private final Clock clock = Clock.systemUTC();

    /**
     * Creates the monitor and registers its gauges.
     *
     * @param meterRegistry the registry to which the gauges are registered
     */
    ShowcaseProjectorStateMonitor(MeterRegistry meterRegistry) {
        Gauge.builder("showcaseProjector.state", () -> switch (state) {
                    case STOPPED -> 0;
                    case RUNNING -> 1;
                    case RETRYING -> 2;
                })
                .description("Projector operating state (0 stopped, 1 running, 2 retrying)")
                .register(meterRegistry);
        Gauge.builder("showcaseProjector.processing", inFlight::get)
                .description("Batches currently being written")
                .register(meterRegistry);
        Gauge.builder("showcaseProjector.last.progress", lastProgressEpochSecond::get)
                .description("Epoch second of the last resolved batch, 0 before the first one")
                .baseUnit("seconds")
                .register(meterRegistry);
    }

    /**
     * Marks the projector as consuming, from the consumer's partition-assignment listener.
     */
    void running() {
        state = State.RUNNING;
    }

    /**
     * Marks the projector as waiting to restart after a stream failure.
     *
     * @param failure the failure that will restart the stream
     */
    void retrying(Throwable failure) {
        lastFailure =
                failure.getClass().getSimpleName() + (failure.getMessage() != null ? ": " + failure.getMessage() : "");
        state = State.RETRYING;
    }

    /**
     * Marks the projector as stopped.
     */
    void stopped() {
        state = State.STOPPED;
    }

    /**
     * Records that a batch's write has started.
     */
    void batchStarted() {
        inFlight.incrementAndGet();
    }

    /**
     * Records that a batch's write resolved, and stamps the progress time.
     */
    void batchResolved() {
        batchFailed();
        lastProgressEpochSecond.set(Instant.now(clock).getEpochSecond());
    }

    /**
     * Records that a batch's write ended without resolving — an error or a cancellation.
     */
    void batchFailed() {
        inFlight.updateAndGet(current -> current > 0 ? current - 1 : 0);
    }

    /**
     * Returns the last stream failure recorded for a retrying projector.
     *
     * @return the failure's description, or {@code null} before a retry
     */
    @Nullable
    String lastFailure() {
        return lastFailure;
    }

    /**
     * Returns the epoch second of the last resolved batch.
     *
     * @return the last progress epoch second, {@code 0} before the first batch
     */
    long lastProgressEpochSecond() {
        return lastProgressEpochSecond.get();
    }
}
