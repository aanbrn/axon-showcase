// SPDX-License-Identifier: MIT
package showcase.api.shared;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static showcase.query.RandomQueryTestUtils.aShowcase;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("Showcase query cache component tests")
class ShowcaseQueryCacheCT {

    @Mock
    private ShowcaseQueryOperations queryOperations;

    private final Sinks.Many<ShowcaseEvent> eventSink = Sinks.many().multicast().directBestEffort();

    @Test
    @DisplayName("A removal event evicts the by-ID entry, so the next read re-queries")
    void removalEvent_evictsByIdCacheEntryThenReQueries() {
        val byIdCache = newByIdCache();
        val cache = new ShowcaseQueryCache(queryOperations, newListCache(), byIdCache, eventSink.asFlux());
        cache.evictOnShowcaseEvents();

        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery.builder()
                .showcaseId(showcase.showcaseId())
                .build();
        given(queryOperations.fetchById(query)).willReturn(Mono.just(showcase));
        byIdCache.put(showcase.showcaseId(), completedFuture(showcase));

        eventSink.tryEmitNext(ShowcaseRemovedEvent.builder()
                .showcaseId(showcase.showcaseId())
                .removedAt(Instant.now())
                .build());

        assertThat(byIdCache.getIfPresent(showcase.showcaseId())).isNull();
        assertThat(cache.fetchById(query).block()).isEqualTo(showcase);
        verify(queryOperations).fetchById(query);
    }

    @Test
    @DisplayName("A status-change event evicts the by-ID entry, so the next read re-queries")
    void statusChangeEvent_evictsByIdCacheEntryThenReQueries() {
        val byIdCache = newByIdCache();
        val cache = new ShowcaseQueryCache(queryOperations, newListCache(), byIdCache, eventSink.asFlux());
        cache.evictOnShowcaseEvents();

        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery.builder()
                .showcaseId(showcase.showcaseId())
                .build();
        given(queryOperations.fetchById(query)).willReturn(Mono.just(showcase));
        byIdCache.put(showcase.showcaseId(), completedFuture(showcase));

        eventSink.tryEmitNext(ShowcaseStartedEvent.builder()
                .showcaseId(showcase.showcaseId())
                .duration(Duration.ofMinutes(5))
                .startedAt(Instant.now())
                .build());

        assertThat(byIdCache.getIfPresent(showcase.showcaseId())).isNull();
        assertThat(cache.fetchById(query).block()).isEqualTo(showcase);
        verify(queryOperations).fetchById(query);
    }

    private static AsyncCache<FetchShowcaseListQuery, List<Showcase>> newListCache() {
        return Caffeine.newBuilder().maximumSize(100).buildAsync();
    }

    private static AsyncCache<String, Showcase> newByIdCache() {
        return Caffeine.newBuilder().maximumSize(100).buildAsync();
    }
}
