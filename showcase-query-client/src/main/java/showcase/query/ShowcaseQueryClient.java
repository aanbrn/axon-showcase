// SPDX-License-Identifier: MIT
package showcase.query;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF;
import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.Map;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive client fetching showcases from the query service over a protobuf transport, protected by Resilience4j
 * time limiter, circuit breaker, and retry.
 */
@Component
@TimeLimiter(name = SHOWCASE_QUERY_SERVICE)
@CircuitBreaker(name = SHOWCASE_QUERY_SERVICE)
@Retry(name = SHOWCASE_QUERY_SERVICE)
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
class ShowcaseQueryClient implements ShowcaseQueryOperations {
    /**
     * The WebClient used to call the query service.
     */
    private final WebClient webClient;

    /**
     * The mapper converting query messages to query requests.
     */
    private final QueryMessageRequestMapper queryMessageRequestMapper;

    /**
     * Creates the client, configuring the WebClient base URL and the message mapper.
     *
     * @param clientProperties the query client properties
     * @param webClientBuilder the WebClient builder
     * @param messageSerializer the message serializer
     */
    ShowcaseQueryClient(
            ShowcaseQueryClientProperties clientProperties,
            WebClient.Builder webClientBuilder,
            @Qualifier("messageSerializer") Serializer messageSerializer) {
        this.webClient = webClientBuilder.baseUrl(clientProperties.getApiUrl()).build();
        this.queryMessageRequestMapper = new QueryMessageRequestMapper(messageSerializer);
    }

    /**
     * Fetches a filtered list of showcases via the streaming query endpoint.
     *
     * @param query the list query to send
     * @return a flux of matching showcases
     */
    @Override
    public Flux<Showcase> fetchList(FetchShowcaseListQuery query) {
        return createQueryRequest(query, Showcase.class)
                .flatMapMany(queryRequest -> Flux.defer(() -> webClient
                        .post()
                        .uri("/streaming-query")
                        .contentType(APPLICATION_PROTOBUF)
                        .bodyValue(queryRequest)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::handleError)
                        .bodyToFlux(Showcase.class)))
                .checkpoint("ShowcaseQueryClient.fetchList(%s)".formatted(query));
    }

    /**
     * Fetches a single showcase by ID via the query endpoint.
     *
     * @param query the by-ID query to send
     * @return a mono of the matching showcase
     */
    @Override
    public Mono<Showcase> fetchById(FetchShowcaseByIdQuery query) {
        return createQueryRequest(query, Showcase.class)
                .flatMap(queryRequest -> Mono.defer(() -> webClient
                        .post()
                        .uri("/query")
                        .contentType(APPLICATION_PROTOBUF)
                        .bodyValue(queryRequest)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::handleError)
                        .bodyToMono(Showcase.class)))
                .checkpoint("ShowcaseQueryClient.fetchById(%s)".formatted(query));
    }

    /**
     * Creates the protobuf query request for the given query and expected response type.
     *
     * @param query the query object to send
     * @param responseType the expected response type
     * @return a mono of the encoded query request
     */
    @SuppressWarnings("SameParameterValue")
    private Mono<QueryRequest> createQueryRequest(Object query, Class<?> responseType) {
        return Mono.just(new GenericStreamingQueryMessage<>(query, responseType))
                .transformDeferredContextual((queryMessageMono, ctx) -> queryMessageMono.map(queryMessage -> {
                    val metaData = ctx.getOrDefault(MetaData.class, MetaData.emptyInstance());
                    return queryMessage.andMetaData(metaData);
                }))
                .map(queryMessageRequestMapper::messageToRequest);
    }

    /**
     * Translates an error response into a {@link ShowcaseQueryException} when it carries problem details, otherwise
     * delegates to the default WebClient exception.
     *
     * @param response the error response
     * @return a mono of the mapped exception
     */
    private Mono<? extends Throwable> handleError(ClientResponse response) {
        if (response.headers()
                .contentType()
                .filter(contentType -> contentType.isCompatibleWith(APPLICATION_PROBLEM_JSON))
                .isPresent()) {
            return switch (response.statusCode()) {
                case HttpStatus.BAD_REQUEST ->
                    response.bodyToMono(ProblemDetail.class).flatMap(problemDetail -> {
                        if (problemDetail.getDetail() != null
                                && problemDetail.getProperties() != null
                                && problemDetail.getProperties().containsKey("fieldErrors")) {
                            @SuppressWarnings("unchecked")
                            Map<String, ?> fieldErrors = (Map<String, ?>)
                                    problemDetail.getProperties().get("fieldErrors");
                            return Mono.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                                    .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                                    .errorMessage(problemDetail.getDetail())
                                    .metaData(MetaData.from(fieldErrors))
                                    .build()));
                        } else {
                            return response.createException();
                        }
                    });
                case HttpStatus.NOT_FOUND ->
                    response.bodyToMono(ProblemDetail.class).flatMap(problemDetail -> {
                        if (problemDetail.getDetail() != null) {
                            return Mono.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                                    .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                                    .errorMessage(problemDetail.getDetail())
                                    .build()));
                        } else {
                            return response.createException();
                        }
                    });
                default -> response.createException();
            };
        } else {
            return response.createException();
        }
    }
}
