package showcase.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties("showcase.command")
@Data
@Validated
final class ShowcaseCommandProperties {

    @Data
    @AllArgsConstructor
    static final class Cache {

        @Min(0)
        private long maximumSize;

        @NotNull
        private Duration expiresAfterAccess;

        @NotNull
        private Duration expiresAfterWrite;
    }

    @Data
    @AllArgsConstructor
    static final class SnapshotTrigger {

        @NotNull
        private Duration loadTimeThreshold;
    }

    private boolean exitAfterFlywayMigration;

    @NotNull
    @Valid
    private Cache showcaseCache = new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5));

    @NotNull
    @Valid
    private SnapshotTrigger showcaseSnapshotTrigger = new SnapshotTrigger(Duration.ofMillis(500));
}
