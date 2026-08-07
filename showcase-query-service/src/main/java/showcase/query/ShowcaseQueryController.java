package showcase.query;

import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.Printer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.function.Predicates;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryBusSpanFactory;
import org.axonframework.queryhandling.QueryResponseMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static org.springframework.http.MediaType.APPLICATION_PROTOBUF_VALUE;

/**
 * REST controller exposing showcase queries over a protobuf transport.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
final class ShowcaseQueryController {
    /**
     * The query bus used to dispatch queries.
     */
    private final QueryBus queryBus;

    /**
     * The mapper converting query requests to query messages.
     */
    private final QueryMessageRequestMapper queryMessageRequestMapper;

    /**
     * The span factory used to propagate tracing context.
     */
    private final QueryBusSpanFactory spanFactory;

    /**
     * The printer formatting query requests into single-line text for debug output.
     */
    private final Printer queryRequestPrinter = TextFormat.debugFormatPrinter().emittingSingleLine(true);

    /**
     * Dispatches a query and returns the full response stream.
     *
     * @param queryRequest the query request
     * @return the flux of query responses
     */
    @PostMapping(path = "/streaming-query", consumes = APPLICATION_PROTOBUF_VALUE)
    Flux<?> streamingQuery(@RequestBody QueryRequest queryRequest) {
        return dispatchQuery(queryRequest)
                       .checkpoint("ShowcaseQueryController.streamingQuery(%s)".formatted(
                               queryRequestPrinter.printToString(queryRequest)));
    }

    /**
     * Dispatches a query and returns only the first response.
     *
     * @param queryRequest the query request
     * @return a mono of the first query response
     */
    @PostMapping(path = "/query", consumes = APPLICATION_PROTOBUF_VALUE)
    Mono<?> query(@RequestBody QueryRequest queryRequest) {
        return dispatchQuery(queryRequest)
                       .next()
                       .checkpoint("ShowcaseQueryController.query(%s)".formatted(
                               queryRequestPrinter.printToString(queryRequest)));
    }

    /**
     * Maps the query request to a message, propagates tracing context, and dispatches it on the query bus.
     *
     * @param queryRequest the query request to dispatch
     * @return a flux of query responses
     */
    private Flux<?> dispatchQuery(QueryRequest queryRequest) {
        return Mono.fromCallable(() -> queryMessageRequestMapper.requestToMessage(queryRequest))
                   .onErrorMap(ClassNotFoundException.class, e -> {
                       log.warn("Unknown expected response type {}", e.getMessage());

                       return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown expected response type");
                   })
                   .transformDeferredContextual((queryMessageMono, ctx) -> queryMessageMono.map(queryMessage -> {
                       val metaData = ctx.getOrDefault(MetaData.class, MetaData.emptyInstance());
                       return queryMessage.andMetaData(metaData);
                   }))
                   .map(spanFactory::propagateContext)
                   .flatMapMany(queryBus::streamingQuery)
                   .map(QueryResponseMessage::getPayload);
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
        return switch (errorDetails.errorCode()) {
            case INVALID_QUERY -> {
                val problemDetail = ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, errorDetails.errorMessage());
                problemDetail.setProperty("fieldErrors", errorDetails.metaData());
                yield problemDetail;
            }
            case NOT_FOUND -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.errorMessage());
        };
    }

    /**
     * Maps data access failures to a {@code 503 Service Unavailable}.
     *
     * @param e the data access exception to map
     * @return the problem detail for the exception
     */
    @ExceptionHandler
    private ProblemDetail handleDataAccessException(DataAccessException e) {
        log.error("Data access failure", e);

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

        return ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Operation timeout exceeded");
    }

    /**
     * Maps aborted inbound connections to a {@code 408 Request Timeout}.
     *
     * @param e the aborted exception to map
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
     * Fallback handler mapping any unhandled exception by unwrapping its cause chain.
     *
     * <p>Known exception types are delegated to their dedicated handlers, and unknown errors yield a generic
     * {@code 503 Service Unavailable}.
     *
     * @param e the exception to map
     * @param exchange the current server exchange
     * @return the mapped response body
     */
    @ExceptionHandler
    @SuppressWarnings("unused")
    private Object handleException(Exception e, ServerWebExchange exchange) {
        return switch (findCause(e, Predicates.<Throwable>falsePredicate()
                                              .or(ShowcaseQueryException.class::isInstance)
                                              .or(DataAccessException.class::isInstance)
                                              .or(TimeoutException.class::isInstance)
                                              .or(AbortedException.class::isInstance))
                               .orElse(e)) {
            case ShowcaseQueryException ex -> handleShowcaseQueryException(ex);
            case DataAccessException ex -> handleDataAccessException(ex);
            case TimeoutException ex -> handleTimeoutException(ex);
            case AbortedException ex -> handleAbortedException(ex, exchange);
            default -> {
                log.error("Unknown error", e);

                yield ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
            }
        };
    }

    /**
     * Walks the exception cause chain to find the first matching exception.
     *
     * @param t the exception to inspect
     * @param predicate the predicate testing candidate causes
     * @return the first matching exception in the chain, if any
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
