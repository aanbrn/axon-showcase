package showcase.projection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties bound to the {@code showcase.projector} prefix.
 *
 * <p>Configures the projection batching, retry, and restart behavior.
 */
@ConfigurationProperties("showcase.projector")
@Data
@Validated
class ShowcaseProjectorProperties {
    /**
     * Configuration for batching consumed events before writing to OpenSearch.
     */
    @Data
    @AllArgsConstructor
    static class Batch {
        /**
         * The maximum number of events per batch.
         */
        @Min(1)
        @Max(1_000)
        private int maxSize;

        /**
         * The maximum time to wait before flushing a batch.
         */
        @NotNull
        @DurationMin(millis = 1)
        @DurationMax(millis = 1_000)
        private Duration maxTime;

        /**
         * The maximum number of buffered events before backpressure is applied.
         */
        @Min(1_000)
        @Max(100_000)
        private int bufferMaxSize;
    }

    /**
     * Configuration for retrying transient OpenSearch failures.
     */
    @Data
    @AllArgsConstructor
    static class Retry {
        /**
         * The maximum number of retry attempts.
         */
        @Min(0)
        private int maxAttempts;

        /**
         * The minimum backoff between retries.
         */
        @NotNull
        @DurationMin(millis = 0)
        @DurationMax(millis = 1_000)
        private Duration minBackoff;
    }

    /**
     * Configuration for restarting the projection stream after a failure.
     */
    @Data
    @AllArgsConstructor
    static class Restart {
        /**
         * The delay before the projection stream is restarted.
         */
        @NotNull
        @DurationMin(seconds = 1)
        @DurationMax(seconds = 60)
        private Duration delay;
    }

    /**
     * The minimum number of concurrent partition groups.
     */
    @Min(1)
    private int minConcurrency = 1;

    /**
     * The maximum number of concurrent partition groups.
     */
    @Min(1)
    private int maxConcurrency = 256;

    /**
     * The batching configuration.
     */
    @NotNull
    @Valid
    private Batch batch = new Batch(100, Duration.ofMillis(100), 10_000);

    /**
     * The retry configuration.
     */
    @NotNull
    @Valid
    private Retry retry = new Retry(3, Duration.ofMillis(100));

    /**
     * The restart configuration.
     */
    @NotNull
    @Valid
    private Restart restart = new Restart(Duration.ofSeconds(10));
}
