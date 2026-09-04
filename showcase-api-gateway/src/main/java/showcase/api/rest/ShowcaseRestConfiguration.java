// SPDX-License-Identifier: MIT
package showcase.api.rest;

import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import showcase.api.ShowcaseApiProperties;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;

/**
 * Configuration for the showcase REST controller's query-side caches.
 */
@Configuration
class ShowcaseRestConfiguration {
    /**
     * Creates the asynchronous cache backing fetch-showcase-list queries.
     *
     * @param apiProperties the properties holding the cache configuration
     * @return the configured asynchronous cache
     */
    @Bean
    AsyncCache<FetchShowcaseListQuery, List<String>> fetchShowcaseListCache(ShowcaseApiProperties apiProperties) {
        val cacheSettings = apiProperties.getCaches().get(FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME);
        if (cacheSettings == null) {
            throw new IllegalStateException(
                    "Settings for cache '%s' is missing".formatted(FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME));
        }
        return Caffeine.newBuilder()
                .maximumSize(cacheSettings.getMaximumSize())
                .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                .recordStats()
                .buildAsync();
    }

    /**
     * Creates the asynchronous cache backing fetch-showcase-by-id queries.
     *
     * @param apiProperties the properties holding the cache configuration
     * @return the configured asynchronous cache
     */
    @Bean
    AsyncCache<String, Showcase> fetchShowcaseByIdCache(ShowcaseApiProperties apiProperties) {
        val cacheSettings = apiProperties.getCaches().get(FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME);
        if (cacheSettings == null) {
            throw new IllegalStateException(
                    "Settings for cache '%s' is missing".formatted(FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME));
        }
        return Caffeine.newBuilder()
                .maximumSize(cacheSettings.getMaximumSize())
                .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                .recordStats()
                .buildAsync();
    }

    /**
     * Registers the asynchronous caches with the {@link CaffeineCacheManager} under their cache names.
     *
     * @param fetchShowcaseListCache the fetch-showcase-list cache
     * @param fetchShowcaseByIdCache the fetch-showcase-by-id cache
     * @return the customizer registering the custom caches
     */
    @Bean
    @SuppressWarnings("unchecked")
    CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer(
            AsyncCache<?, ?> fetchShowcaseListCache, AsyncCache<?, ?> fetchShowcaseByIdCache) {
        return cacheManager -> {
            cacheManager.registerCustomCache(
                    "fetch-showcase-list-cache", (AsyncCache<@NonNull Object, Object>) fetchShowcaseListCache);
            cacheManager.registerCustomCache(
                    "fetch-showcase-by-id-cache", (AsyncCache<@NonNull Object, Object>) fetchShowcaseByIdCache);
        };
    }
}
