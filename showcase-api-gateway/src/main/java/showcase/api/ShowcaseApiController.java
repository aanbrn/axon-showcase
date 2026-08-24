// SPDX-License-Identifier: MIT
package showcase.api;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.github.benmanes.caffeine.cache.AsyncCache;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.function.Predicates;
import org.axonframework.common.AxonException;
import org.axonframework.common.IdentifierFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.channel.AbortedException;
import showcase.command.FinishShowcaseCommand;
import showcase.command.RemoveShowcaseCommand;
import showcase.command.ScheduleShowcaseCommand;
import showcase.command.ShowcaseCommandException;
import showcase.command.ShowcaseCommandOperations;
import showcase.command.StartShowcaseCommand;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryException;
import showcase.query.ShowcaseQueryOperations;
import showcase.query.ShowcaseStatus;

/**
 * REST controller implementing the showcase management API.
 *
 * <p>Coordinates command and query operations, with in-memory caching as a fallback layer when downstream calls fail
 * transiently.
 */
@RestController
@RequestMapping("/showcases")
@RequiredArgsConstructor
@Slf4j
final class ShowcaseApiController implements ShowcaseApi {
    /**
     * Operations for dispatching showcase commands to the command side.
     */
    private final ShowcaseCommandOperations commandOperations;

    /**
     * Operations for querying showcases from the read side.
     */
    private final ShowcaseQueryOperations queryOperations;

    /**
     * Cache for {@link FetchShowcaseListQuery} → showcase IDs, used as a fallback on query errors.
     */
    private final AsyncCache<FetchShowcaseListQuery, List<String>> fetchShowcaseListCache;

    /**
     * Cache for showcase ID → {@link Showcase}, used as a fallback on query errors.
     */
    private final AsyncCache<String, Showcase> fetchShowcaseByIdCache;

    /**
     * Resolves Spring validation exceptions into per-parameter error maps on problem details.
     */
    private final ShowcaseApiErrorResolver errorResolver;

    /**
     * Schedules a new showcase.
     *
     * <p>An idempotency key is either taken from the request header or generate automatically. On timeout,
     * a {@code 202} is returned with the key so the client can retry.
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Override
    public Mono<ResponseEntity<ScheduleShowcaseResponse>> schedule(
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @RequestBody ScheduleShowcaseRequest request) {
        return Mono.justOrEmpty(idempotencyKey)
                .switchIfEmpty(
                        Mono.fromSupplier(() -> IdentifierFactory.getInstance().generateIdentifier())
                                .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(showcaseId -> commandOperations
                        .schedule(ScheduleShowcaseCommand.builder()
                                .showcaseId(showcaseId)
                                .title(request.title())
                                .startTime(request.startTime())
                                .duration(request.duration())
                                .build())
                        .thenReturn(ResponseEntity.created(fromUriString("/showcases/")
                                        .path(showcaseId)
                                        .build()
                                        .toUri())
                                .body(ScheduleShowcaseResponse.builder()
                                        .showcaseId(showcaseId)
                                        .build()))
                        .onErrorReturn(
                                TimeoutException.class,
                                ResponseEntity.accepted()
                                        .header(IDEMPOTENCY_KEY_HEADER, showcaseId)
                                        .build()));
    }

    /**
     * Starts a scheduled showcase.
     *
     * <p>Returns {@code 202} if the command times out, allowing the client to retry and check the final state later.
     */
    @PutMapping("/{showcaseId}/start")
    @Override
    public Mono<ResponseEntity<Void>> start(@PathVariable String showcaseId) {
        return commandOperations
                .start(StartShowcaseCommand.builder().showcaseId(showcaseId).build())
                .thenReturn(HttpStatus.OK)
                .onErrorReturn(TimeoutException.class, HttpStatus.ACCEPTED)
                .map(status -> ResponseEntity.status(status).build());
    }

    /**
     * Finishes a started showcase.
     *
     * <p>Returns {@code 202} if the command times out, allowing the client to retry and check the final state later.
     */
    @PutMapping("/{showcaseId}/finish")
    @Override
    public Mono<ResponseEntity<Void>> finish(@PathVariable String showcaseId) {
        return commandOperations
                .finish(FinishShowcaseCommand.builder().showcaseId(showcaseId).build())
                .thenReturn(HttpStatus.OK)
                .onErrorReturn(TimeoutException.class, HttpStatus.ACCEPTED)
                .map(status -> ResponseEntity.status(status).build());
    }

    /**
     * Removes a showcase, finishing it first if it has already started.
     *
     * <p>Returns {@code 202} if the command times out, allowing the client to retry and check the final state later.
     */
    @DeleteMapping("/{showcaseId}")
    @Override
    public Mono<ResponseEntity<Void>> remove(@PathVariable String showcaseId) {
        return commandOperations
                .remove(RemoveShowcaseCommand.builder().showcaseId(showcaseId).build())
                .thenReturn(HttpStatus.OK)
                .onErrorReturn(TimeoutException.class, HttpStatus.ACCEPTED)
                .map(status -> ResponseEntity.status(status).build());
    }

    /**
     * Fetches a paginated list of showcases, with in-memory cache fallback.
     *
     * <p>On a transient query error, the method attempts to serve cached IDs from {@link #fetchShowcaseListCache}
     * and then resolves each showcase from {@link #fetchShowcaseByIdCache}. If no cached data is available, the
     * error is propagated and a warning is logged.
     */
    @GetMapping
    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public Flux<Showcase> fetchList(
            @RequestParam(required = false) String title,
            @RequestParam(name = "status", required = false) List<ShowcaseStatus> statuses,
            @RequestParam(required = false) String afterId,
            @RequestParam(required = false, defaultValue = "" + FetchShowcaseListQuery.DEFAULT_SIZE) int size) {
        val query = FetchShowcaseListQuery.builder()
                .title(title)
                .statuses(statuses)
                .afterId(afterId)
                .size(size)
                .build();
        return queryOperations
                .fetchList(query)
                .doOnNext(showcase -> fetchShowcaseByIdCache.put(showcase.showcaseId(), completedFuture(showcase)))
                .collectList()
                .doOnNext(showcases -> fetchShowcaseListCache.put(
                        query,
                        completedFuture(
                                showcases.stream().map(Showcase::showcaseId).toList())))
                .flatMapIterable(Function.identity())
                .onErrorResume(
                        Predicate.not(ShowcaseQueryException.class::isInstance), t -> Flux.<String>create(sink -> {
                                    val future = fetchShowcaseListCache.getIfPresent(query);
                                    if (future != null) {
                                        future.thenAccept(showcaseIds -> {
                                            showcaseIds.forEach(sink::next);
                                            sink.complete();
                                        });
                                    } else {
                                        sink.error(t);
                                    }
                                })
                                .<Showcase>handle((showcaseId, sink) -> {
                                    val future = fetchShowcaseByIdCache.getIfPresent(showcaseId);
                                    if (future != null) {
                                        future.thenAccept(sink::next);
                                    } else {
                                        sink.error(t);
                                    }
                                })
                                .doOnComplete(() -> log.warn("Fallback on {}", query, t)));
    }

    /**
     * Fetches a single showcase by ID, with in-memory cache fallback.
     *
     * <p>On a transient query error, the method attempts to serve the showcase from {@link #fetchShowcaseByIdCache}.
     * If no cached entry exists, the error is propagated and a warning is logged.
     */
    @GetMapping("/{showcaseId}")
    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public Mono<Showcase> fetchById(@PathVariable String showcaseId) {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(showcaseId).build();
        return queryOperations
                .fetchById(query)
                .doOnNext(showcase -> fetchShowcaseByIdCache.put(showcaseId, completedFuture(showcase)))
                .onErrorResume(
                        Predicate.not(ShowcaseQueryException.class::isInstance), t -> Mono.<Showcase>create(sink -> {
                                    val future = fetchShowcaseByIdCache.getIfPresent(showcaseId);
                                    if (future != null) {
                                        future.thenAccept(sink::success);
                                    } else {
                                        sink.error(t);
                                    }
                                })
                                .doOnSuccess(__ -> log.warn("Fallback on {}", query, t)));
    }

    /**
     * Maps {@link ShowcaseCommandException} to structured problem details.
     *
     * @param e the command exception to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleShowcaseCommandException(ShowcaseCommandException e) {
        val errorDetails = e.getErrorDetails();
        val problemDetail =
                switch (errorDetails.errorCode()) {
                    case INVALID_COMMAND -> {
                        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails.errorMessage());
                        pd.setProperty("fieldErrors", errorDetails.metaData());
                        yield pd;
                    }
                    case NOT_FOUND ->
                        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.errorMessage());
                    case TITLE_IN_USE, ILLEGAL_STATE ->
                        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, errorDetails.errorMessage());
                };
        problemDetail.setProperty("code", errorDetails.errorCode());
        return problemDetail;
    }

    /**
     * Maps {@link ShowcaseQueryException} to structured problem details.
     *
     * @param e the query exception to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleShowcaseQueryException(ShowcaseQueryException e) {
        val errorDetails = e.getErrorDetails();
        val problemDetail =
                switch (errorDetails.errorCode()) {
                    case INVALID_QUERY -> {
                        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails.errorMessage());
                        pd.setProperty("fieldErrors", errorDetails.metaData());
                        yield pd;
                    }
                    case NOT_FOUND ->
                        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.errorMessage());
                };
        problemDetail.setProperty("code", errorDetails.errorCode());
        return problemDetail;
    }

    /**
     * Maps {@link HandlerMethodValidationException} to structured problem details with per-parameter error
     * breakdowns.
     *
     * @param e      the handler method validation exception to map
     * @param locale the locale used for error messages
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    @SuppressWarnings("unused")
    private ProblemDetail handleHandlerMethodValidationException(HandlerMethodValidationException e, Locale locale) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request.");
        errorResolver.resolve(e, locale, problemDetail);
        return problemDetail;
    }

    /**
     * Maps Axon Framework failures to a {@code 503 Service Unavailable}.
     *
     * @param e the Axon exception to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleAxonException(AxonException e) {
        log.error("AxonFramework failure", e);

        return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maps WebClient failures to a {@code 503 Service Unavailable}.
     *
     * @param e the WebClient exception to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleWebClientException(WebClientException e) {
        log.error("WebClient failure", e);

        return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maps Resilience4j circuit breaker rejections to a {@code 503 Service Unavailable}.
     *
     * @param e the circuit breaker rejection to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleCallNotPermittedException(CallNotPermittedException e) {
        log.error(e.getMessage());

        return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maps timeouts to a {@code 504 Gateway Timeout}.
     *
     * @param e the timeout exception to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleTimeoutException(TimeoutException e) {
        log.trace("Operation timeout exceeded", e);

        return ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Operation timeout exceeded.");
    }

    /**
     * Maps aborted inbound connections to a {@code 408 Request Timeout}.
     *
     * @param e        the aborted exception to map
     * @param exchange the current server exchange
     * @return a mono that completes the exchange response
     */
    @ExceptionHandler
    private Mono<Void> handleAbortedException(AbortedException e, ServerWebExchange exchange) {
        log.trace("Inbound connection aborted", e);

        exchange.getResponse().setStatusCode(HttpStatus.REQUEST_TIMEOUT);
        return exchange.getResponse().setComplete();
    }

    /**
     * Fallback handler mapping any unhandled exception to a {@code 503 Service Unavailable}.
     *
     * <p>The method unwraps the exception chain to find a known exception type and delegates to the corresponding
     * handler, or returns a generic {@code 503} for unknown errors.
     *
     * @param e        the exception to map
     * @param exchange the current server exchange
     * @param locale   the locale used for error messages
     * @return the mapped response body or {@code null}
     */
    @ExceptionHandler
    @SuppressWarnings("unused")
    private Object handleException(Exception e, ServerWebExchange exchange, Locale locale) {
        return switch (findCause(
                        e,
                        Predicates.<Throwable>falsePredicate()
                                .or(ShowcaseCommandException.class::isInstance)
                                .or(ShowcaseQueryException.class::isInstance)
                                .or(AxonException.class::isInstance)
                                .or(WebClientException.class::isInstance)
                                .or(CallNotPermittedException.class::isInstance)
                                .or(TimeoutException.class::isInstance)
                                .or(AbortedException.class::isInstance))
                .orElse(e)) {
            case ShowcaseCommandException ex -> handleShowcaseCommandException(ex);
            case ShowcaseQueryException ex -> handleShowcaseQueryException(ex);
            case AxonException ex -> handleAxonException(ex);
            case WebClientException ex -> handleWebClientException(ex);
            case CallNotPermittedException ex -> handleCallNotPermittedException(ex);
            case TimeoutException ex -> handleTimeoutException(ex);
            case AbortedException ex -> handleAbortedException(ex, exchange);
            default -> {
                log.error("Unknown error", e);

                yield ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
            }
        };
    }

    /**
     * Walks the exception cause chain to find the first throwable matching the given predicate.
     *
     * @param t         the throwable to inspect
     * @param predicate the predicate the cause must satisfy
     * @return the first matching cause, or an empty optional if none matches
     */
    private Optional<Throwable> findCause(Throwable t, Predicate<Throwable> predicate) {
        while (t != null) {
            if (predicate.test(t)) {
                return Optional.of(t);
            }
            t = t.getCause();
        }
        return Optional.empty();
    }
}
