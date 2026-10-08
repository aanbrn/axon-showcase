// SPDX-License-Identifier: MIT
package showcase.api.shared;

import com.github.benmanes.caffeine.cache.AsyncCache;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import showcase.command.ShowcaseEvent;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryOperations;

/**
 * Read-through cache over the showcase query operations.
 *
 * <p>Serves a query from an in-memory cache when present and otherwise fetches it from the query service and caches the
 * result, coalescing concurrent reads for the same key. A showcase's by-ID entry is evicted when an event for it is
 * received. Shared by every inbound adapter of the gateway, so they reuse one cache instance.
 */
@Component
@RequiredArgsConstructor
public class ShowcaseQueryCache {
    /**
     * Operations for querying showcases from the read side.
     */
    private final ShowcaseQueryOperations queryOperations;

    /**
     * Cache for {@link FetchShowcaseListQuery} to its list of showcases.
     */
    private final AsyncCache<FetchShowcaseListQuery, List<Showcase>> fetchShowcaseListCache;

    /**
     * Cache for showcase ID to its {@link Showcase}.
     */
    private final AsyncCache<String, Showcase> fetchShowcaseByIdCache;

    /**
     * The shared domain-event stream used to evict changed showcases.
     */
    private final Flux<ShowcaseEvent> eventReceiver;

    /**
     * Evicts a showcase's by-ID cache entry when an event for it is received, so a subsequent read re-queries the read
     * model rather than serving a cached pre-change state.
     */
    @PostConstruct
    void evictOnShowcaseEvents() {
        eventReceiver.subscribe(event -> fetchShowcaseByIdCache.synchronous().invalidate(event.showcaseId()));
    }

    /**
     * Fetches a paginated list of showcases, serving a cached result when present.
     *
     * @param query the list query
     * @return the matching showcases
     */
    public Flux<Showcase> fetchList(FetchShowcaseListQuery query) {
        return Mono.deferContextual(ctx -> Mono.fromFuture(fetchShowcaseListCache.get(
                        query,
                        (key, executor) -> toNonNullFuture(
                                queryOperations.fetchList(key).collectList().contextWrite(ctx)))))
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * Fetches a single showcase by its ID, serving a cached result when present.
     *
     * @param query the by-ID query
     * @return the matching showcase
     */
    public Mono<Showcase> fetchById(FetchShowcaseByIdQuery query) {
        return Mono.deferContextual(ctx -> Mono.fromFuture(fetchShowcaseByIdCache.get(
                query.showcaseId(),
                (key, executor) ->
                        toNonNullFuture(queryOperations.fetchById(query).contextWrite(ctx)))));
    }

    /**
     * Adapts a {@link Mono} to a {@link CompletableFuture} whose value is never null, failing when the source emits no
     * value: the read-through sources always emit one, since a missing showcase is reported as an error and a list
     * query always emits a list.
     *
     * @param source the source mono
     * @param <T>    the emitted value type
     * @return the future completing with the value
     */
    private static <T> CompletableFuture<T> toNonNullFuture(Mono<T> source) {
        val future = new CompletableFuture<T>();
        source.switchIfEmpty(Mono.error(new NoSuchElementException("The source emitted no value")))
                .subscribe(future::complete, future::completeExceptionally);
        return future;
    }
}
