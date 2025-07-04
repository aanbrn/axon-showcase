package showcase.query;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import one.util.streamex.StreamEx;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.interceptors.JSR303ViolationException;
import org.axonframework.queryhandling.QueryMessage;
import org.axonframework.tracing.SpanFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF_VALUE;

@RestController
@RequiredArgsConstructor
@Slf4j
final class ShowcaseQueryController {

    @NonNull
    private ReactorQueryGateway queryGateway;

    @NonNull
    private QueryMessageRequestMapper queryMessageRequestMapper;

    @NonNull
    private SpanFactory spanFactory;

    @NonNull
    private MessageSource messageSource;

    @PostMapping(path = "/query", consumes = APPLICATION_PROTOBUF_VALUE)
    Mono<?> query(@RequestBody QueryRequest request) {
        final QueryMessage<?, ?> message;
        try {
            message = queryMessageRequestMapper.requestToMessage(request);
        } catch (ClassNotFoundException e) {
            log.warn("Unknown expected response type {} on query", request.getExpectedResponseType());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown expected response type.");
        }
        val span = spanFactory.createInternalSpan(() -> "ShowcaseQueryClient.query", message);
        return span.runSupplier(
                () -> queryGateway
                              .streamingQuery(message, message.getResponseType().getExpectedResponseType())
                              .next()
                              .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))));
    }

    @PostMapping(path = "/streaming-query", consumes = APPLICATION_PROTOBUF_VALUE)
    Flux<?> streamingQuery(@RequestBody QueryRequest request) {
        final QueryMessage<?, ?> message;
        try {
            message = queryMessageRequestMapper.requestToMessage(request);
        } catch (ClassNotFoundException e) {
            log.warn("Unknown expected response type {} on streaming query", request.getExpectedResponseType());

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown expected response type.");
        }
        val span = spanFactory.createInternalSpan(() -> "ShowcaseQueryClient.streamingQuery", message);
        return span.runSupplier(
                () -> queryGateway.streamingQuery(message, message.getResponseType().getExpectedResponseType()));
    }

    @ExceptionHandler
    ResponseEntity<ProblemDetail> handleJSR303ViolationException(JSR303ViolationException e) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        val violations = e.getViolations();
        if (!violations.isEmpty()) {
            problemDetail.setProperty("fieldErrors", new HashMap<>());
            for (val violation : violations) {
                //noinspection unchecked
                var fieldErrors = (Map<String, Object>) requireNonNull(problemDetail.getProperties())
                                                                .get("fieldErrors");
                val path =
                        StreamEx.of(violation.getPropertyPath().iterator())
                                .toList();
                for (int i = 0; i < path.size() - 1; i++) {
                    //noinspection ReassignedVariable,unchecked
                    fieldErrors = (Map<String, Object>) fieldErrors.compute(path.get(i).getName(), (k, v) -> {
                        //noinspection ReplaceNullCheck
                        if (v == null) {
                            return new HashMap<>();
                        } else {
                            return v;
                        }
                    });
                }
                //noinspection unchecked
                List<String> messages = (List<String>) fieldErrors.computeIfAbsent(
                        path.getLast().getName(), k -> new ArrayList<>());
                messages.add(violation.getMessage());
            }
        }
        return ResponseEntity.of(problemDetail).build();
    }
}
