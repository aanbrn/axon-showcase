// SPDX-License-Identifier: MIT
package showcase.query;

import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.serialization.Serializer;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive client fetching showcases from the query service over the generic gRPC query transport, protected by
 * Resilience4j time limiter, circuit breaker, and retry.
 */
@Component
@TimeLimiter(name = SHOWCASE_QUERY_SERVICE)
@CircuitBreaker(name = SHOWCASE_QUERY_SERVICE)
@Retry(name = SHOWCASE_QUERY_SERVICE)
class ShowcaseQueryClient implements ShowcaseQueryOperations, DisposableBean {
    /**
     * The metadata key carrying the serialized field errors of an invalid query.
     */
    private static final Metadata.Key<byte[]> FIELD_ERRORS_KEY =
            Metadata.Key.of("field-errors-bin", Metadata.BINARY_BYTE_MARSHALLER);

    /**
     * The type of the deserialized field errors trailer.
     */
    private static final TypeReference<Map<String, ?>> FIELD_ERRORS_TYPE = new TypeReference<>() {};

    /**
     * The gRPC channel used to call the query service.
     */
    private final ManagedChannel channel;

    /**
     * The mapper converting query messages to requests and responses to payloads.
     */
    private final QueryMessageMapper queryMessageMapper;

    /**
     * The mapper deserializing the field errors trailer.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates the client, opening a gRPC channel to the query service target and building the message mapper.
     *
     * @param clientProperties  the query client properties
     * @param messageSerializer the message serializer
     * @param objectMapper      the mapper deserializing the field errors trailer
     */
    ShowcaseQueryClient(
            ShowcaseQueryClientProperties clientProperties,
            @Qualifier("messageSerializer") Serializer messageSerializer,
            ObjectMapper objectMapper) {
        this.channel = ManagedChannelBuilder.forTarget(clientProperties.getTarget())
                .usePlaintext()
                .build();
        this.queryMessageMapper = new QueryMessageMapper(messageSerializer);
        this.objectMapper = objectMapper;
    }

    /**
     * Shuts the gRPC channel down when the client bean is destroyed.
     */
    @Override
    public void destroy() {
        channel.shutdownNow();
    }

    /**
     * Fetches a filtered list of showcases over the gRPC query RPC.
     *
     * @param query the list query to send
     * @return a flux of matching showcases
     */
    @Override
    public Flux<Showcase> fetchList(FetchShowcaseListQuery query) {
        return dispatch(query, Showcase.class).checkpoint("ShowcaseQueryClient.fetchList(%s)".formatted(query));
    }

    /**
     * Fetches a single showcase by ID over the gRPC query RPC.
     *
     * @param query the by-ID query to send
     * @return a mono of the matching showcase
     */
    @Override
    public Mono<Showcase> fetchById(FetchShowcaseByIdQuery query) {
        return dispatch(query, Showcase.class).next().checkpoint("ShowcaseQueryClient.fetchById(%s)".formatted(query));
    }

    /**
     * Builds the query request, dispatches it over the RPC, and decodes each response payload.
     *
     * @param query        the query object to send
     * @param responseType the expected response type
     * @param <T>          the response type
     * @return a flux of decoded responses
     */
    private <T> Flux<T> dispatch(Object query, Class<T> responseType) {
        return Mono.just(new GenericStreamingQueryMessage<>(query, responseType))
                .transformDeferredContextual((queryMessageMono, ctx) -> queryMessageMono.map(queryMessage -> {
                    val metaData = ctx.getOrDefault(MetaData.class, MetaData.emptyInstance());
                    return queryMessage.andMetaData(metaData);
                }))
                .map(queryMessageMapper::messageToRequest)
                .flatMapMany(this::stream)
                .map(response -> queryMessageMapper.payloadFromResponse(response, responseType));
    }

    /**
     * Streams the query responses from the gRPC query RPC.
     *
     * @param request the query request to send
     * @return a flux of query responses
     */
    private Flux<QueryResponse> stream(QueryRequest request) {
        return Flux.create(sink -> ShowcaseQueryTransportGrpc.newStub(channel)
                .dispatch(request, new StreamObserver<>() {
                    @Override
                    public void onNext(QueryResponse response) {
                        sink.next(response);
                    }

                    @Override
                    public void onError(Throwable error) {
                        sink.error(translate(error));
                    }

                    @Override
                    public void onCompleted() {
                        sink.complete();
                    }
                }));
    }

    /**
     * Translates a gRPC status error into a {@link ShowcaseQueryException} for a business error, or returns the
     * original error.
     *
     * @param error the gRPC error
     * @return the translated error
     */
    private Throwable translate(Throwable error) {
        if (!(error instanceof StatusRuntimeException statusError)) {
            return error;
        }
        val status = statusError.getStatus();
        val description = status.getDescription();
        if (Status.Code.INVALID_ARGUMENT.equals(status.getCode()) && description != null) {
            val fieldErrors = readFieldErrors(statusError.getTrailers());
            if (fieldErrors != null) {
                return new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                        .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                        .errorMessage(description)
                        .metaData(MetaData.from(fieldErrors))
                        .build());
            }
        } else if (Status.Code.NOT_FOUND.equals(status.getCode()) && description != null) {
            return new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                    .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                    .errorMessage(description)
                    .build());
        }
        return error;
    }

    /**
     * Deserializes the field errors trailer, or returns null when it is absent.
     *
     * @param trailers the response trailers
     * @return the field errors, or null when absent
     */
    @SneakyThrows(IOException.class)
    private @Nullable Map<String, ?> readFieldErrors(@Nullable Metadata trailers) {
        if (trailers == null) {
            return null;
        }
        val fieldErrors = trailers.get(FIELD_ERRORS_KEY);
        return fieldErrors == null ? null : objectMapper.readValue(fieldErrors, FIELD_ERRORS_TYPE);
    }
}
