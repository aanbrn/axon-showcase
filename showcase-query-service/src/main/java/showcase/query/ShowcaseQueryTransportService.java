// SPDX-License-Identifier: MIT
package showcase.query;

import static io.grpc.Status.DEADLINE_EXCEEDED;
import static io.grpc.Status.INTERNAL;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.NOT_FOUND;
import static io.grpc.Status.UNAVAILABLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.function.Predicates;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryBusSpanFactory;
import org.springframework.dao.DataAccessException;
import reactor.core.publisher.Mono;

/**
 * Serves the generic gRPC query transport: reconstructs the Axon query message from the request, dispatches it on the
 * query bus, and streams each response.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
final class ShowcaseQueryTransportService extends ShowcaseQueryTransportGrpc.ShowcaseQueryTransportImplBase {
    /**
     * The metadata key carrying the serialized field errors of an invalid query.
     */
    private static final Metadata.Key<byte[]> FIELD_ERRORS_KEY =
            Metadata.Key.of("field-errors-bin", Metadata.BINARY_BYTE_MARSHALLER);

    /**
     * The query bus used to dispatch queries.
     */
    private final QueryBus queryBus;

    /**
     * The mapper converting query requests and responses to and from their Protobuf representations.
     */
    private final QueryMessageMapper queryMessageMapper;

    /**
     * The span factory used to propagate tracing context.
     */
    private final QueryBusSpanFactory spanFactory;

    /**
     * The mapper serializing the field errors trailer.
     */
    private final ObjectMapper objectMapper;

    /**
     * Dispatches the query and streams each response, mapping a failure to a gRPC status.
     *
     * @param queryRequest     the query request to dispatch
     * @param responseObserver the observer receiving the streamed responses
     */
    @Override
    public void dispatch(QueryRequest queryRequest, StreamObserver<QueryResponse> responseObserver) {
        Mono.fromCallable(() -> queryMessageMapper.requestToMessage(queryRequest))
                .onErrorMap(
                        ClassNotFoundException.class,
                        e -> INVALID_ARGUMENT
                                .withDescription("Unknown expected response type")
                                .asRuntimeException())
                .transformDeferredContextual((queryMessageMono, ctx) -> queryMessageMono.map(queryMessage ->
                        queryMessage.andMetaData(ctx.getOrDefault(MetaData.class, MetaData.emptyInstance()))))
                .map(spanFactory::propagateContext)
                .flatMapMany(queryBus::streamingQuery)
                .map(queryMessageMapper::messageToResponse)
                .subscribe(
                        responseObserver::onNext,
                        error -> responseObserver.onError(translate(error)),
                        responseObserver::onCompleted);
    }

    /**
     * Translates a query failure into a gRPC status error.
     *
     * @param error the failure
     * @return the gRPC status error
     */
    private StatusRuntimeException translate(Throwable error) {
        if (error instanceof StatusRuntimeException statusError) {
            return statusError;
        }
        val cause = findCause(
                        error,
                        Predicates.<Throwable>falsePredicate()
                                .or(ShowcaseQueryException.class::isInstance)
                                .or(DataAccessException.class::isInstance)
                                .or(TimeoutException.class::isInstance))
                .orElse(error);
        return switch (cause) {
            case ShowcaseQueryException queryException -> translateQueryException(queryException);
            case DataAccessException dataAccessException ->
                UNAVAILABLE.withDescription(description(dataAccessException)).asRuntimeException();
            case TimeoutException timeoutException -> {
                log.debug("Query handling timed out", timeoutException);
                yield DEADLINE_EXCEEDED
                        .withDescription("Operation timeout exceeded")
                        .asRuntimeException();
            }
            default -> INTERNAL.withDescription(description(error)).asRuntimeException();
        };
    }

    /**
     * Translates a query exception into a gRPC status error carrying its error code.
     *
     * @param exception the query exception
     * @return the gRPC status error
     */
    private StatusRuntimeException translateQueryException(ShowcaseQueryException exception) {
        val errorDetails = exception.getErrorDetails();
        return switch (errorDetails.errorCode()) {
            case INVALID_QUERY -> {
                val trailers = new Metadata();
                trailers.put(FIELD_ERRORS_KEY, serializeFieldErrors(errorDetails.metaData()));
                yield INVALID_ARGUMENT
                        .withDescription(errorDetails.errorMessage())
                        .asRuntimeException(trailers);
            }
            case NOT_FOUND ->
                NOT_FOUND.withDescription(errorDetails.errorMessage()).asRuntimeException();
        };
    }

    /**
     * Serializes the field errors metadata for the field errors trailer.
     *
     * @param metaData the field errors metadata
     * @return the serialized field errors
     */
    @SneakyThrows
    private byte[] serializeFieldErrors(MetaData metaData) {
        return objectMapper.writeValueAsBytes(metaData);
    }

    /**
     * Returns the error's message, or its class name when it has none.
     *
     * @param error the error
     * @return the error's description
     */
    private static String description(Throwable error) {
        return Optional.ofNullable(error.getMessage()).orElse(error.getClass().getSimpleName());
    }

    /**
     * Walks the exception cause chain to find the first throwable matching the given predicate.
     *
     * @param throwable the throwable to inspect
     * @param predicate the predicate the cause must satisfy
     * @return the first matching cause, or an empty optional if none matches
     */
    private Optional<Throwable> findCause(Throwable throwable, Predicate<Throwable> predicate) {
        Throwable current = throwable;
        while (current != null) {
            if (predicate.test(current)) {
                return Optional.of(current);
            }
            current = current.getCause();
        }
        return Optional.empty();
    }
}
