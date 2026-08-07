package showcase.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties bound to the {@code showcase.command} prefix.
 *
 * <p>Configures caching, snapshotting, and the post-migration exit behavior of the command service.
 */
@ConfigurationProperties("showcase.command")
@Data
@Validated
final class ShowcaseCommandProperties {
    /**
     * Configuration for a single in-memory cache.
     */
    @Data
    @AllArgsConstructor
    static final class Cache {
        /**
         * The maximum number of entries the cache may hold.
         */
        @Min(0)
        private long maximumSize;

        /**
         * The duration after which an entry is eligible for eviction when it has not been accessed.
         */
        @NotNull
        private Duration expiresAfterAccess;

        /**
         * The duration after which an entry is eligible for eviction regardless of access.
         */
        @NotNull
        private Duration expiresAfterWrite;
    }

    /**
     * Configuration for the aggregate snapshot trigger.
     */
    @Data
    @AllArgsConstructor
    static final class SnapshotTrigger {
        /**
         * The load time threshold after which an aggregate is snapshotted.
         */
        @NotNull
        private Duration loadTimeThreshold;
    }

    /**
     * Whether the application should exit after the Flyway migration completes.
     */
    private boolean exitAfterFlywayMigration;

    /**
     * The aggregate cache configuration.
     */
    @NotNull
    @Valid
    private Cache showcaseCache = new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5));

    /**
     * The saga cache configuration.
     */
    @NotNull
    @Valid
    private Cache sagaCache = new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5));

    /**
     * The saga associations cache configuration.
     */
    @NotNull
    @Valid
    private Cache sagaAssociationsCache = new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5));

    /**
     * The showcase snapshot trigger configuration.
     */
    @NotNull
    @Valid
    private SnapshotTrigger showcaseSnapshotTrigger = new SnapshotTrigger(Duration.ofMillis(500));
}
