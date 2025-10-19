package showcase.query;

import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.Printer;
import lombok.NonNull;
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
import reactor.core.scheduler.Schedulers;
import reactor.netty.channel.AbortedException;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static org.springframework.http.MediaType.APPLICATION_PROTOBUF_VALUE;

@RestController
@RequiredArgsConstructor
@Slf4j
final class ShowcaseQueryController {

    @NonNull
    private final QueryBus queryBus;

    @NonNull
    private final QueryMessageRequestMapper queryMessageRequestMapper;

    @NonNull
    private final QueryBusSpanFactory spanFactory;

    private final Printer queryRequestPrinter = TextFormat.debugFormatPrinter().emittingSingleLine(true);

    @PostMapping(path = "/streaming-query", consumes = APPLICATION_PROTOBUF_VALUE)
    Flux<?> streamingQuery(@RequestBody QueryRequest queryRequest) {
        return dispatchQuery(queryRequest)
                       .checkpoint("ShowcaseQueryController.streamingQuery(%s)".formatted(
                               queryRequestPrinter.printToString(queryRequest)));
    }

    @PostMapping(path = "/query", consumes = APPLICATION_PROTOBUF_VALUE)
    Mono<?> query(@RequestBody QueryRequest queryRequest) {
        return dispatchQuery(queryRequest)
                       .next()
                       .checkpoint("ShowcaseQueryController.query(%s)".formatted(
                               queryRequestPrinter.printToString(queryRequest)));
    }

    private Flux<?> dispatchQuery(QueryRequest queryRequest) {
        return Mono.fromCallable(() -> queryMessageRequestMapper.requestToMessage(queryRequest))
                   .subscribeOn(Schedulers.boundedElastic())
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

    @ExceptionHandler
    private Mono<ProblemDetail> handleShowcaseQueryException(ShowcaseQueryException e) {
        return Mono.fromSupplier(() -> {
            val errorDetails = e.getErrorDetails();
            return switch (errorDetails.getErrorCode()) {
                case INVALID_QUERY -> {
                    val problemDetail = ProblemDetail.forStatusAndDetail(
                            HttpStatus.BAD_REQUEST, errorDetails.getErrorMessage());
                    problemDetail.setProperty("fieldErrors", errorDetails.getMetaData());
                    yield problemDetail;
                }
                case NOT_FOUND -> ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND, errorDetails.getErrorMessage());
            };
        });
    }

    @ExceptionHandler
    private Mono<ProblemDetail> handleDataAccessException(DataAccessException e) {
        return Mono.fromSupplier(() -> {
            log.error("Data access failure", e);

            return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        });
    }

    @ExceptionHandler
    private Mono<ProblemDetail> handleTimeoutException(TimeoutException e) {
        return Mono.fromSupplier(() -> {
            log.trace("Operation timeout exceeded", e);

            return ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Operation timeout exceeded");
        });
    }

    @ExceptionHandler
    private Mono<Void> handleAbortedException(AbortedException e, ServerWebExchange exchange) {
        return Mono.defer(() -> {
            log.trace("Inbound connection aborted", e);

            exchange.getResponse().setStatusCode(HttpStatus.REQUEST_TIMEOUT);
            return exchange.getResponse().setComplete();
        });
    }

    @ExceptionHandler
    private Mono<?> handleException(Exception e, ServerWebExchange exchange) {
        return switch (findCause(e, Predicates.<Throwable>truePredicate()
                                              .or(ShowcaseQueryException.class::isInstance)
                                              .or(DataAccessException.class::isInstance)
                                              .or(TimeoutException.class::isInstance)
                                              .or(AbortedException.class::isInstance))
                               .orElse(e)) {
            case ShowcaseQueryException ex -> handleShowcaseQueryException(ex);
            case DataAccessException ex -> handleDataAccessException(ex);
            case TimeoutException ex -> handleTimeoutException(ex);
            case AbortedException ex -> handleAbortedException(ex, exchange);
            default -> Mono.fromSupplier(() -> {
                log.error("Unknown error", e);

                return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
            });
        };
    }

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
