// SPDX-License-Identifier: MIT
package showcase.api;

import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties bound to the {@code showcase.api} prefix.
 *
 * <p>Configures the in-memory caches and CORS settings used by the showcase API gateway.
 */
@ConfigurationProperties("showcase.api")
@Data
@Validated
public final class ShowcaseApiProperties {
    /**
     * Configuration for a single in-memory cache.
     */
    @Data
    @AllArgsConstructor
    public static final class Cache {
        /**
         * The maximum number of entries the cache may hold.
         */
        @Min(0)
        private long maximumSize;

        /**
         * The duration after which an entry is eligible for eviction when it has not been accessed.
         */
        @NotNull
        @DurationMin(nanos = 0)
        private Duration expiresAfterAccess;

        /**
         * The duration after which an entry is eligible for eviction regardless of access.
         */
        @NotNull
        @DurationMin(nanos = 0)
        private Duration expiresAfterWrite;
    }

    /**
     * Cross-origin resource sharing settings.
     */
    @Data
    public static final class Cors {
        /**
         * The origins allowed to call the gateway, or empty to disable CORS.
         */
        @NotNull
        private List<@NotBlank @URL String> allowedOrigins = List.of("http://localhost:5173", "http://localhost:4173");
    }

    /**
     * The caches configured by name, keyed by the cache name and holding its Cache settings.
     */
    @NotNull
    @Valid
    private Map<@NotBlank String, @NotNull @Valid Cache> caches = Map.of(
            FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME, new Cache(10000, Duration.ofMinutes(10), Duration.ofMinutes(5)),
            FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME, new Cache(100000, Duration.ofMinutes(10), Duration.ofMinutes(5)));

    /**
     * The cross-origin configuration for the web UI.
     */
    @NotNull
    @Valid
    private Cors cors = new Cors();
}
