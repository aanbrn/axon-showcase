package showcase.query;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryBusSpanFactory;
import org.axonframework.queryhandling.QueryResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @PostMapping(path = "/streaming-query", consumes = APPLICATION_PROTOBUF_VALUE)
    Flux<?> streamingQuery(@RequestBody QueryRequest queryRequest) {
        return dispatchQuery(queryRequest);
    }

    @PostMapping(path = "/query", consumes = APPLICATION_PROTOBUF_VALUE)
    Mono<?> query(@RequestBody QueryRequest queryRequest) {
        return dispatchQuery(queryRequest).next();
    }

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

    @ExceptionHandler
    private ProblemDetail handleShowcaseQueryException(ShowcaseQueryException e) {
        val errorDetails = e.getErrorDetails();
        return switch (errorDetails.getErrorCode()) {
            case INVALID_QUERY -> {
                val problemDetail = ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST, errorDetails.getErrorMessage());
                problemDetail.setProperty("fieldErrors", errorDetails.getMetaData());
                yield problemDetail;
            }
            case NOT_FOUND -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.getErrorMessage());
        };
    }
}
