package showcase.saga;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties("showcase.saga")
@Data
@Validated
final class ShowcaseSagaProperties {

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

    private boolean exitAfterFlywayMigration;

    @NotNull
    @Valid
    private Cache sagaCache = new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5));

    @NotNull
    @Valid
    private Cache sagaAssociationsCache = new Cache(1000, Duration.ofMinutes(10), Duration.ofMinutes(5));
}
