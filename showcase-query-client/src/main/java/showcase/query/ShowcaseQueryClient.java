package showcase.query;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.NonNull;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.queryhandling.StreamingQueryMessage;
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

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF;
import static showcase.query.ShowcaseQueryOperations.SHOWCASE_QUERY_SERVICE;

@Component
@TimeLimiter(name = SHOWCASE_QUERY_SERVICE)
@CircuitBreaker(name = SHOWCASE_QUERY_SERVICE)
@Retry(name = SHOWCASE_QUERY_SERVICE)
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
class ShowcaseQueryClient implements ShowcaseQueryOperations {

    private final WebClient webClient;

    private final QueryMessageRequestMapper queryMessageRequestMapper;

    ShowcaseQueryClient(
            @NonNull ShowcaseQueryClientProperties clientProperties,
            @NonNull WebClient.Builder webClientBuilder,
            @NonNull @Qualifier("messageSerializer") Serializer messageSerializer) {
        this.webClient =
                webClientBuilder
                        .baseUrl(clientProperties.getApiUrl())
                        .build();
        this.queryMessageRequestMapper = new QueryMessageRequestMapper(messageSerializer);
    }

    @Override
    public Flux<Showcase> fetchList(@NonNull FetchShowcaseListQuery query) {
        return createQueryMessage(query, Showcase.class).flatMapMany(
                queryMessage -> webClient.post()
                                         .uri("/streaming-query")
                                         .contentType(APPLICATION_PROTOBUF)
                                         .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                                         .retrieve()
                                         .onStatus(HttpStatusCode::isError, this::handleError)
                                         .bodyToFlux(Showcase.class));
    }

    @Override
    public Mono<Showcase> fetchById(@NonNull FetchShowcaseByIdQuery query) {
        return createQueryMessage(query, Showcase.class).flatMap(
                message -> webClient.post()
                                    .uri("/query")
                                    .contentType(APPLICATION_PROTOBUF)
                                    .bodyValue(queryMessageRequestMapper.messageToRequest(message))
                                    .retrieve()
                                    .onStatus(HttpStatusCode::isError, this::handleError)
                                    .bodyToMono(Showcase.class));
    }

    @SuppressWarnings("SameParameterValue")
    private <Q, R> Mono<StreamingQueryMessage<Q, R>> createQueryMessage(Q query, Class<R> responseType) {
        return Mono.just(new GenericStreamingQueryMessage<>(query, responseType))
                   .transformDeferredContextual((queryMessageMono, ctx) -> queryMessageMono.map(queryMessage -> {
                       val metaData = ctx.getOrDefault(MetaData.class, MetaData.emptyInstance());
                       return queryMessage.andMetaData(metaData);
                   }));
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        if (response.headers()
                    .contentType()
                    .filter(contentType -> contentType.isCompatibleWith(APPLICATION_PROBLEM_JSON))
                    .isPresent()) {
            return switch (response.statusCode()) {
                case HttpStatus.BAD_REQUEST -> response.bodyToMono(ProblemDetail.class).flatMap(
                        problemDetail -> {
                            if (problemDetail.getDetail() != null
                                        && problemDetail.getProperties() != null
                                        && problemDetail.getProperties().containsKey("fieldErrors")) {
                                @SuppressWarnings("unchecked") Map<String, ?> fieldErrors =
                                        (Map<String, ?>) problemDetail.getProperties().get("fieldErrors");
                                return Mono.error(
                                        new ShowcaseQueryException(
                                                ShowcaseQueryErrorDetails
                                                        .builder()
                                                        .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                                                        .errorMessage(problemDetail.getDetail())
                                                        .metaData(MetaData.from(fieldErrors))
                                                        .build()));
                            } else {
                                return response.createException();
                            }
                        });
                case HttpStatus.NOT_FOUND -> response.bodyToMono(ProblemDetail.class).flatMap(
                        problemDetail -> {
                            if (problemDetail.getDetail() != null) {
                                return Mono.error(
                                        new ShowcaseQueryException(
                                                ShowcaseQueryErrorDetails
                                                        .builder()
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
