package showcase.query;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.NonNull;
import lombok.val;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.GenericQueryMessage;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.queryhandling.QueryMessage;
import org.axonframework.queryhandling.StreamingQueryMessage;
import org.axonframework.serialization.Serializer;
import org.axonframework.tracing.SpanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_PROTOBUF;

@Component
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
class ShowcaseQueryClient implements ShowcaseQueryOperations {

    private static final ParameterizedTypeReference<Page<Showcase>> SHOWCASE_PAGE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;

    private final QueryMessageRequestMapper queryMessageRequestMapper;

    private final SpanFactory spanFactory;

    ShowcaseQueryClient(
            @NonNull ShowcaseQueryClientProperties clientProperties,
            @NonNull WebClient.Builder webClientBuilder,
            @NonNull @Qualifier("messageSerializer") Serializer messageSerializer,
            @NonNull SpanFactory spanFactory) {
        this.webClient =
                webClientBuilder
                        .baseUrl(clientProperties.getApiUrl())
                        .build();
        this.queryMessageRequestMapper = new QueryMessageRequestMapper(messageSerializer);
        this.spanFactory = spanFactory;
    }

    @TimeLimiter(name = SHOWCASE_QUERY_SERVICE)
    @CircuitBreaker(name = SHOWCASE_QUERY_SERVICE)
    @Retry(name = SHOWCASE_QUERY_SERVICE)
    @Override
    public Flux<Showcase> fetchAll(@NonNull FetchShowcaseListQuery query) {
        val queryMessage = new GenericStreamingQueryMessage<>(query, Showcase.class);
        val span = spanFactory.createDispatchSpan(() -> "ShowcaseQueryClient.fetchAll", queryMessage);
        return span.runSupplier(() -> fetchAll(queryMessage));
    }

    @TimeLimiter(name = SHOWCASE_QUERY_SERVICE)
    @CircuitBreaker(name = SHOWCASE_QUERY_SERVICE)
    @Retry(name = SHOWCASE_QUERY_SERVICE)
    @Override
    public Mono<Showcase> fetchById(@NonNull FetchShowcaseByIdQuery query) {
        val queryMessage = new GenericQueryMessage<>(query, ResponseTypes.instanceOf(Showcase.class));
        val span = spanFactory.createDispatchSpan(() -> "ShowcaseQueryClient.fetchById", queryMessage);
        return span.runSupplier(() -> fetchById(queryMessage));
    }

    private Flux<Showcase> fetchAll(
            @NonNull StreamingQueryMessage<FetchShowcaseListQuery, Showcase> queryMessage) {
        return webClient.post()
                        .uri("/streaming-query")
                        .contentType(APPLICATION_PROTOBUF)
                        .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                        .retrieve()
                        .bodyToFlux(Showcase.class);
    }

    private Mono<Showcase> fetchById(@NonNull QueryMessage<FetchShowcaseByIdQuery, Showcase> queryMessage) {
        return webClient.post()
                        .uri("/query")
                        .contentType(APPLICATION_PROTOBUF)
                        .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response -> {
                            if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(new ShowcaseQueryException(
                                        ShowcaseQueryErrorDetails
                                                .builder()
                                                .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                                                .errorMessage("No showcase with given ID")
                                                .build()));
                            } else {
                                return response.createException();
                            }
                        })
                        .bodyToMono(Showcase.class);
    }
}
