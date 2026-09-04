// SPDX-License-Identifier: MIT
package showcase.api.rest;

import static io.github.resilience4j.circuitbreaker.CallNotPermittedException.createCallNotPermittedException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.security.web.server.header.CacheControlServerHttpHeadersWriter.CACHE_CONTRTOL_VALUE;
import static showcase.api.rest.ShowcaseRestApi.IDEMPOTENCY_KEY_HEADER;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.test.RandomTestUtils.anAlphabeticString;
import static showcase.test.RandomTestUtils.anEnum;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;
import showcase.api.ShowcaseApiErrorResolver;
import showcase.api.ShowcaseApiProperties;
import showcase.command.FinishShowcaseCommand;
import showcase.command.RemoveShowcaseCommand;
import showcase.command.ScheduleShowcaseCommand;
import showcase.command.ShowcaseCommandErrorCode;
import showcase.command.ShowcaseCommandErrorDetails;
import showcase.command.ShowcaseCommandException;
import showcase.command.ShowcaseCommandOperations;
import showcase.command.StartShowcaseCommand;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryErrorCode;
import showcase.query.ShowcaseQueryErrorDetails;
import showcase.query.ShowcaseQueryException;
import showcase.query.ShowcaseQueryOperations;

@WebFluxTest(ShowcaseRestController.class)
@DisplayName("Showcase API controller component tests")
class ShowcaseRestControllerCT {

    @Configuration
    @ComponentScan(
            basePackages = "showcase.api.rest",
            excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ShowcaseRestConfiguration.class))
    @EnableConfigurationProperties(ShowcaseApiProperties.class)
    @ImportAutoConfiguration(TaskExecutionAutoConfiguration.class)
    static class TestConfig {

        @Bean
        ShowcaseApiErrorResolver showcaseApiErrorResolver(MessageSource messageSource) {
            return new ShowcaseApiErrorResolver(messageSource);
        }

        @Bean
        SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
            return http.csrf(CsrfSpec::disable)
                    .authorizeExchange(authorize -> authorize.anyExchange().permitAll())
                    .build();
        }

        @Bean
        AsyncCache<@NonNull FetchShowcaseListQuery, List<String>> fetchShowcaseListCache() {
            return Caffeine.newBuilder().maximumSize(100).buildAsync();
        }

        @Bean
        AsyncCache<@NonNull String, Showcase> fetchShowcaseByIdCache() {
            return Caffeine.newBuilder().maximumSize(100).buildAsync();
        }
    }

    static List<Arguments> commandAvailabilityFailures() {
        return List.of(
                argumentSet("Axon Error", new NoHandlerForCommandException(anAlphabeticString(10))),
                argumentSet(
                        "CircuitBreaker Error",
                        createCallNotPermittedException(CircuitBreaker.of(
                                ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE,
                                CircuitBreakerConfig.ofDefaults()))),
                argumentSet("Unknown Error", new Exception(anAlphabeticString(10))));
    }

    static List<Arguments> queryAvailabilityFailures() {
        return List.of(
                argumentSet(
                        "WebClient Error",
                        WebClientResponseException.create(
                                HttpStatus.BAD_GATEWAY.value(),
                                anAlphabeticString(10),
                                HttpHeaders.EMPTY,
                                ArrayUtils.EMPTY_BYTE_ARRAY,
                                null)),
                argumentSet(
                        "CircuitBreaker Error",
                        createCallNotPermittedException(CircuitBreaker.of(
                                ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE,
                                CircuitBreakerConfig.ofDefaults()))),
                argumentSet("Unknown Error", new Exception(anAlphabeticString(10))));
    }

    static List<Arguments> wrappedClientErrorStatuses() {
        return List.of(
                argumentSet(
                        "Command error",
                        new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                                .errorCode(ShowcaseCommandErrorCode.INVALID_COMMAND)
                                .errorMessage(anAlphabeticString(10))
                                .build()),
                        HttpStatus.BAD_REQUEST),
                argumentSet(
                        "Query error",
                        new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                                .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                                .errorMessage(anAlphabeticString(10))
                                .build()),
                        HttpStatus.BAD_REQUEST),
                argumentSet(
                        "Axon error",
                        new NoHandlerForCommandException(anAlphabeticString(10)),
                        HttpStatus.SERVICE_UNAVAILABLE),
                argumentSet(
                        "WebClient error",
                        WebClientResponseException.create(
                                anEnum(HttpStatus.class),
                                anAlphabeticString(32),
                                new HttpHeaders(),
                                new byte[0],
                                null,
                                null),
                        HttpStatus.SERVICE_UNAVAILABLE),
                argumentSet(
                        "Circuit breaker error",
                        createCallNotPermittedException(CircuitBreaker.of(
                                ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE, CircuitBreakerConfig.ofDefaults())),
                        HttpStatus.SERVICE_UNAVAILABLE),
                argumentSet("Timeout", new TimeoutException(), HttpStatus.GATEWAY_TIMEOUT));
    }

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private ShowcaseCommandOperations showcaseCommandOperations;

    @MockitoBean
    private ShowcaseQueryOperations showcaseQueryOperations;

    @Autowired
    private AsyncCache<@NonNull FetchShowcaseListQuery, List<String>> fetchShowcaseListCache;

    @Autowired
    private AsyncCache<@NonNull String, Showcase> fetchShowcaseByIdCache;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @BeforeEach
    void clearCaches() {
        fetchShowcaseListCache.synchronous().invalidateAll();
        fetchShowcaseByIdCache.synchronous().invalidateAll();
    }

    @Test
    @DisplayName("Scheduling a showcase responds with created status, a location header, and the showcase ID")
    void scheduleShowcase_success_respondsWithCreatedStatusAndLocationHeaderAndShowcaseIdInBody() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.empty());

        val scheduleResponse = webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .build())
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                .expectHeader()
                .value(HttpHeaders.LOCATION, location -> assertThat(location).startsWith("/showcases/"))
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody(ScheduleShowcaseResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(scheduleResponse).isNotNull();

        verify(showcaseCommandOperations)
                .schedule(ScheduleShowcaseCommand.builder()
                        .showcaseId(scheduleResponse.showcaseId())
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Scheduling a showcase with an invalid request responds with bad request and a problem in the body")
    void scheduleShowcase_invalidRequest_respondsWithBadRequestStatusAndProblemInBody() {
        webClient
                .post()
                .uri("/showcases")
                .bodyValue(Map.of())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.bodyErrors")
                .isMap()
                .jsonPath("$.bodyErrors.title")
                .isArray()
                .jsonPath("$.bodyErrors.startTime")
                .isArray()
                .jsonPath("$.bodyErrors.duration")
                .isArray();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Scheduling a showcase with an already used title responds with conflict and a problem in the body")
    void scheduleShowcase_alreadyUsedTitle_respondsWithConflictStatusAndProblemInBody() {
        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                        .errorCode(ShowcaseCommandErrorCode.TITLE_IN_USE)
                        .errorMessage("Given title is in use already")
                        .build())));

        webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.CONFLICT.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.CONFLICT.value())
                .jsonPath("$.detail")
                .isEqualTo("Given title is in use already");

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    @DisplayName("Scheduling a showcase with a command failure responds with the related status and a problem")
    void scheduleShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())));

        val expectedStatus =
                switch (errorCode) {
                    case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
                };

        webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(expectedStatus.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(expectedStatus.value())
                .jsonPath("$.detail")
                .isEqualTo(errorMessage);

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    @DisplayName("Scheduling a showcase on an availability failure responds with service unavailable and a problem")
    void scheduleShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(error));

        webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("wrappedClientErrorStatuses")
    @DisplayName("Scheduling a showcase on a wrapped client error responds with the related status")
    void scheduleShowcase_wrappedClientError_respondsWithRelatedStatus(Throwable cause, HttpStatus expectedStatus) {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(new RuntimeException(cause)));

        webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Scheduling a showcase with an aborted request responds with request timeout status and an empty body")
    void scheduleShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(new AbortedException(anAlphabeticString(10))));

        webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.REQUEST_TIMEOUT)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Scheduling a showcase with a timeout responds with accepted status and an idempotency key header")
    void scheduleShowcase_timeout_respondsWithAcceptedStatusAndIdempotencyKeyHeader() {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(new TimeoutException()));

        webClient
                .post()
                .uri("/showcases")
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isAccepted()
                .expectHeader()
                .value(IDEMPOTENCY_KEY_HEADER, idempotencyKey -> assertThat(idempotencyKey)
                        .isNotBlank());

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Scheduling a showcase with an invalid idempotency key responds with bad request and a problem")
    void scheduleShowcase_invalidIdempotencyKey_respondsWithBadRequestStatusAndProblemInBody() {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(new TimeoutException()));

        webClient
                .post()
                .uri("/showcases")
                .header(IDEMPOTENCY_KEY_HEADER, anAlphabeticString(10))
                .bodyValue(ScheduleShowcaseRequest.builder()
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.headerErrors")
                .isMap()
                .jsonPath("$.headerErrors.%s".formatted(IDEMPOTENCY_KEY_HEADER))
                .isArray();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Starting a showcase responds with OK status and an empty body")
    void startShowcase_success_respondsWithOkStatusAndEmptyBody() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.start(any())).willReturn(Mono.empty());

        webClient
                .put()
                .uri("/showcases/{showcaseId}/start", showcaseId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations)
                .start(StartShowcaseCommand.builder().showcaseId(showcaseId).build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Starting a showcase with an invalid showcase ID responds with bad request and a problem in the body")
    void startShowcase_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient
                .put()
                .uri("/showcases/{showcaseId}/start", anInvalidShowcaseId())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.pathErrors")
                .isMap()
                .jsonPath("$.pathErrors.showcaseId")
                .isArray()
                .jsonPath("$.pathErrors.showcaseId[0]")
                .isNotEmpty()
                .jsonPath("$.pathErrors.showcaseId[1]")
                .doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    @DisplayName("Starting a showcase with a command failure responds with the related status and a problem")
    void startShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.start(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())));

        val expectedStatus =
                switch (errorCode) {
                    case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
                };

        webClient
                .put()
                .uri("/showcases/{showcaseId}/start", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(expectedStatus.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(expectedStatus.value())
                .jsonPath("$.detail")
                .isEqualTo(errorMessage);

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    @DisplayName("Starting a showcase on an availability failure responds with service unavailable and a problem")
    void startShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.start(any())).willReturn(Mono.error(error));

        webClient
                .put()
                .uri("/showcases/{showcaseId}/start", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Starting a showcase with an aborted request responds with request timeout status and an empty body")
    void startShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.start(any()))
                .willReturn(Mono.error(new AbortedException(anAlphabeticString(10))));

        webClient
                .put()
                .uri("/showcases/{showcaseId}/start", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.REQUEST_TIMEOUT)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Starting a showcase with a timeout responds with accepted status")
    void startShowcase_timeout_respondsWithAcceptedStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.start(any())).willReturn(Mono.error(new TimeoutException()));

        webClient
                .put()
                .uri("/showcases/{showcaseId}/start", showcaseId)
                .exchange()
                .expectStatus()
                .isAccepted();

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Finishing a showcase responds with OK status and an empty body")
    void finishShowcase_success_respondsWithOkStatusAndEmptyBody() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.finish(any())).willReturn(Mono.empty());

        webClient
                .put()
                .uri("/showcases/{showcaseId}/finish", showcaseId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations)
                .finish(FinishShowcaseCommand.builder().showcaseId(showcaseId).build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Finishing a showcase with an invalid showcase ID responds with bad request and a problem in the body")
    void finishShowcase_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient
                .put()
                .uri("/showcases/{showcaseId}/finish", anInvalidShowcaseId())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.pathErrors")
                .isMap()
                .jsonPath("$.pathErrors.showcaseId")
                .isArray()
                .jsonPath("$.pathErrors.showcaseId[0]")
                .isNotEmpty()
                .jsonPath("$.pathErrors.showcaseId[1]")
                .doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    @DisplayName("Finishing a showcase with a command failure responds with the related status and a problem")
    void finishShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.finish(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())));

        val expectedStatus =
                switch (errorCode) {
                    case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
                };

        webClient
                .put()
                .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(expectedStatus.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(expectedStatus.value())
                .jsonPath("$.detail")
                .isEqualTo(errorMessage);

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    @DisplayName("Finishing a showcase on an availability failure responds with service unavailable and a problem")
    void finishShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.finish(any())).willReturn(Mono.error(error));

        webClient
                .put()
                .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Finishing a showcase with an aborted request responds with request timeout status and an empty body")
    void finishShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.finish(any()))
                .willReturn(Mono.error(new AbortedException(anAlphabeticString(10))));

        webClient
                .put()
                .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.REQUEST_TIMEOUT)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Finishing a showcase with a timeout responds with accepted status")
    void finishShowcase_timeout_respondsWithAcceptedStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.finish(any())).willReturn(Mono.error(new TimeoutException()));

        webClient
                .put()
                .uri("/showcases/{showcaseId}/finish", showcaseId)
                .exchange()
                .expectStatus()
                .isAccepted();

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Removing a showcase responds with OK status and an empty body")
    void removeShowcase_success_respondsWithOkStatusAndEmptyBody() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.remove(any())).willReturn(Mono.empty());

        webClient
                .delete()
                .uri("/showcases/{showcaseId}", showcaseId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations)
                .remove(RemoveShowcaseCommand.builder().showcaseId(showcaseId).build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Removing a showcase with an invalid showcase ID responds with bad request and a problem in the body")
    void removeShowcase_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient
                .delete()
                .uri("/showcases/{showcaseId}", anInvalidShowcaseId())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.pathErrors")
                .isMap()
                .jsonPath("$.pathErrors.showcaseId")
                .isArray()
                .jsonPath("$.pathErrors.showcaseId[0]")
                .isNotEmpty()
                .jsonPath("$.pathErrors.showcaseId[1]")
                .doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    @DisplayName("Removing a showcase with a command failure responds with the related status and a problem")
    void removeShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.remove(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(ShowcaseCommandErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())));

        val expectedStatus =
                switch (errorCode) {
                    case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
                };

        webClient
                .delete()
                .uri("/showcases/{showcaseId}", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(expectedStatus.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(expectedStatus.value())
                .jsonPath("$.detail")
                .isEqualTo(errorMessage);

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    @DisplayName("Removing a showcase on an availability failure responds with service unavailable and a problem")
    void removeShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.remove(any())).willReturn(Mono.error(error));

        webClient
                .delete()
                .uri("/showcases/{showcaseId}", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Removing a showcase with an aborted request responds with request timeout status and an empty body")
    void removeShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.remove(any()))
                .willReturn(Mono.error(new AbortedException(anAlphabeticString(10))));

        webClient
                .delete()
                .uri("/showcases/{showcaseId}", aShowcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.REQUEST_TIMEOUT)
                .expectBody()
                .isEmpty();

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Removing a showcase with a timeout responds with accepted status")
    void removeShowcase_timeout_respondsWithAcceptedStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.remove(any())).willReturn(Mono.error(new TimeoutException()));

        webClient
                .delete()
                .uri("/showcases/{showcaseId}", showcaseId)
                .exchange()
                .expectStatus()
                .isAccepted();

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Fetching the list puts showcases into caches and responds with OK status and showcases in the body")
    void fetchShowcaseList_success_putShowcasesIntoCachesAndRespondsWithOkStatusAndShowcasesInBody() {
        val showcases = showcases();
        val query = FetchShowcaseListQuery.builder().build();

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.fromIterable(showcases));

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/showcases").build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases);

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() -> {
            val cachedIds = fetchShowcaseListCache.getIfPresent(query);
            assertThat(cachedIds).isNotNull();
            assertThat(cachedIds.join())
                    .isEqualTo(showcases.stream().map(Showcase::showcaseId).toList());
        });
        await().untilAsserted(() -> {
            for (val showcase : showcases) {
                val cachedShowcase = fetchShowcaseByIdCache.getIfPresent(showcase.showcaseId());
                assertThat(cachedShowcase).isNotNull();
                assertThat(cachedShowcase.join()).isEqualTo(showcase);
            }
        });
    }

    @Test
    @DisplayName("Fetching the list with an invalid afterId responds with bad request status and a problem in the body")
    void fetchShowcaseList_invalidAfterId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/showcases")
                        .queryParam("afterId", anInvalidShowcaseId())
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.paramErrors")
                .isMap()
                .jsonPath("$.paramErrors.afterId")
                .isArray()
                .jsonPath("$.paramErrors.afterId[0]")
                .isNotEmpty()
                .jsonPath("$.paramErrors.afterId[1]")
                .doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @ValueSource(ints = {FetchShowcaseListQuery.MIN_SIZE - 1, FetchShowcaseListQuery.MAX_SIZE + 1})
    @DisplayName("Fetching the list with an invalid size responds with bad request status and a problem in the body")
    void fetchShowcaseList_invalidSize_respondsWithBadRequestStatusAndProblemInBody(int size) {
        webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/showcases").queryParam("size", size).build())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.paramErrors")
                .isMap()
                .jsonPath("$.paramErrors.size")
                .isArray()
                .jsonPath("$.paramErrors.size[0]")
                .isNotEmpty()
                .jsonPath("$.paramErrors.size[1]")
                .doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Fetching the list with a fallback cache hit logs the failure and responds with the cached result")
    void fetchShowcaseList_fallbackFetchShowcaseListCacheHit_logsFailureAndRespondsWithCachedResult(
            CapturedOutput output) {
        val showcases = showcases();
        val query = FetchShowcaseListQuery.builder().build();
        val failure = WebClientResponseException.create(
                anEnum(HttpStatus.class), anAlphabeticString(32), new HttpHeaders(), new byte[0], null, null);

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.error(failure));
        fetchShowcaseListCache.put(
                query,
                completedFuture(showcases.stream().map(Showcase::showcaseId).toList()));
        for (val showcase : showcases) {
            fetchShowcaseByIdCache.put(showcase.showcaseId(), completedFuture(showcase));
        }

        webClient
                .get()
                .uri("/showcases")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases);

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() ->
                assertThat(output).contains("Fallback on %s".formatted(query)).contains(failure.getMessage()));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Fetching the list with a fallback cache miss responds with service unavailable and a problem")
    void fetchShowcaseList_fallbackFetchShowcaseListCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
            CapturedOutput output) {
        val query = FetchShowcaseListQuery.builder().build();
        val failure = WebClientResponseException.create(
                anEnum(HttpStatus.class), anAlphabeticString(32), new HttpHeaders(), new byte[0], null, null);

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.error(failure));

        webClient
                .get()
                .uri("/showcases")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() -> assertThat(output)
                .doesNotContain("Fallback on %s".formatted(query))
                .contains(failure.getMessage()));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Fetching the list on a fallback fetch-by-ID cache miss fails with service unavailable")
    void fetchShowcaseList_fallbackFetchShowcaseByIdCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
            CapturedOutput output) {
        val query = FetchShowcaseListQuery.builder().build();
        val showcaseId = aShowcaseId();
        val failure = WebClientResponseException.create(
                anEnum(HttpStatus.class), anAlphabeticString(32), new HttpHeaders(), new byte[0], null, null);

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.error(failure));
        fetchShowcaseListCache.put(query, completedFuture(List.of(showcaseId)));

        webClient
                .get()
                .uri("/showcases")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() -> assertThat(output)
                .doesNotContain("Fallback on %s".formatted(query))
                .contains(failure.getMessage()));
    }

    @ParameterizedTest
    @EnumSource(ShowcaseQueryErrorCode.class)
    @DisplayName("Fetching the list with a query failure responds with the related status and a problem in the body")
    void fetchShowcaseList_queryFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseQueryErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseQueryOperations.fetchList(any()))
                .willReturn(Flux.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())));

        val expectedStatus =
                switch (errorCode) {
                    case INVALID_QUERY -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                };

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/showcases").build())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(expectedStatus.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(expectedStatus.value())
                .jsonPath("$.detail")
                .isEqualTo(errorMessage);

        verify(showcaseQueryOperations).fetchList(any());
        verifyNoMoreInteractions(showcaseQueryOperations);
    }

    @ParameterizedTest
    @MethodSource("queryAvailabilityFailures")
    @DisplayName("Fetching the list on an availability failure responds with service unavailable and a problem")
    void fetchShowcaseList_availabilityFailure_respondsWithRelatedStatusAndProblemInBody(Exception error) {
        given(showcaseQueryOperations.fetchList(any())).willReturn(Flux.error(error));

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/showcases").build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(any());
        verifyNoMoreInteractions(showcaseQueryOperations);
    }

    @Test
    @DisplayName("Fetching the list with a timeout responds with gateway timeout and a problem in the body")
    void fetchShowcaseList_timeout_respondsWithGatewayTimeoutAndProblemInBody() {
        given(showcaseQueryOperations.fetchList(any())).willReturn(Flux.error(new TimeoutException()));

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/showcases").build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
                .jsonPath("$.detail")
                .isEqualTo("Operation timeout exceeded.");

        verify(showcaseQueryOperations).fetchList(any());
        verifyNoMoreInteractions(showcaseQueryOperations);
    }

    @Test
    @DisplayName("Fetching by ID puts the showcase into cache and responds with OK status and the showcase in the body")
    void fetchShowcaseById_success_putsShowcaseIntoCacheAndRespondsWithOkStatusAndShowcaseInBody() {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery.builder()
                .showcaseId(showcase.showcaseId())
                .build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.just(showcase));

        webClient
                .get()
                .uri("/showcases/{showcaseId}", showcase.showcaseId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                .expectBody(Showcase.class)
                .isEqualTo(showcase);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() -> {
            val cachedShowcase = fetchShowcaseByIdCache.getIfPresent(showcase.showcaseId());
            assertThat(cachedShowcase).isNotNull();
            assertThat(cachedShowcase.join()).isEqualTo(showcase);
        });
    }

    @Test
    @DisplayName("Fetching by ID with an invalid showcase ID responds with bad request and a problem in the body")
    void fetchShowcaseById_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient
                .get()
                .uri("/showcases/{showcaseId}", anInvalidShowcaseId())
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail")
                .isEqualTo("Invalid request.")
                .jsonPath("$.pathErrors")
                .isMap()
                .jsonPath("$.pathErrors.showcaseId")
                .isArray()
                .jsonPath("$.pathErrors.showcaseId[0]")
                .isNotEmpty()
                .jsonPath("$.pathErrors.showcaseId[1]")
                .doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    @DisplayName("Fetching by ID with a non-existing showcase responds with not-found status and a problem in the body")
    void fetchShowcaseById_nonExistingShowcase_respondsWithNotFoundStatusAndProblemInBody() {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery.builder().showcaseId(showcaseId).build();

        given(showcaseQueryOperations.fetchById(query))
                .willReturn(Mono.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                        .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                        .errorMessage("No showcase with id")
                        .build())));

        webClient
                .get()
                .uri("/showcases/{showcaseId}", showcaseId)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.detail")
                .isEqualTo("No showcase with id");

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Fetching by ID with a fallback cache hit logs the failure and responds with the cached result")
    void fetchShowcaseById_fallbackFetchShowcaseByCacheHit_logsFailureAndRespondsWithCachedResult(
            CapturedOutput output) {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery.builder()
                .showcaseId(showcase.showcaseId())
                .build();
        val failure = WebClientResponseException.create(
                anEnum(HttpStatus.class), anAlphabeticString(32), new HttpHeaders(), new byte[0], null, null);

        given(showcaseQueryOperations.fetchById(any())).willReturn(Mono.error(failure));
        fetchShowcaseByIdCache.put(showcase.showcaseId(), completedFuture(showcase));

        webClient
                .get()
                .uri("/showcases/{showcaseId}", showcase.showcaseId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_JSON)
                .expectBody(Showcase.class)
                .isEqualTo(showcase);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() ->
                assertThat(output).contains("Fallback on %s".formatted(query)).contains(failure.getMessage()));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("Fetching by ID with a fallback cache miss responds with service unavailable and a problem")
    void fetchShowcaseById_fallbackFetchShowcaseByIdCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
            CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery.builder().showcaseId(showcaseId).build();
        val failure = WebClientResponseException.create(
                anEnum(HttpStatus.class), anAlphabeticString(32), new HttpHeaders(), new byte[0], null, null);

        given(showcaseQueryOperations.fetchById(any())).willReturn(Mono.error(failure));

        webClient
                .get()
                .uri("/showcases/{showcaseId}", showcaseId)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        await().untilAsserted(() -> assertThat(output)
                .doesNotContain("Fallback on %s".formatted(query))
                .contains(failure.getMessage()));
    }

    @ParameterizedTest
    @EnumSource(ShowcaseQueryErrorCode.class)
    @DisplayName("Fetching by ID with a query failure responds with the related status and a problem in the body")
    void fetchShowcaseById_queryFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseQueryErrorCode errorCode) {
        val query = FetchShowcaseByIdQuery.builder().showcaseId(aShowcaseId()).build();
        val errorMessage = anAlphabeticString(10);

        given(showcaseQueryOperations.fetchById(query))
                .willReturn(Mono.error(new ShowcaseQueryException(ShowcaseQueryErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorMessage)
                        .build())));

        val expectedStatus =
                switch (errorCode) {
                    case INVALID_QUERY -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                };

        webClient
                .get()
                .uri("/showcases/{showcaseId}", query.showcaseId())
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(expectedStatus.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(expectedStatus.value())
                .jsonPath("$.detail")
                .isEqualTo(errorMessage);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);
    }

    @ParameterizedTest
    @MethodSource("queryAvailabilityFailures")
    @DisplayName("Fetching by ID on an availability failure responds with service unavailable and a problem")
    void fetchShowcaseById_availabilityFailure_respondsWithRelatedStatusAndProblemInBody(Exception error) {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery.builder().showcaseId(showcaseId).build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.error(error));

        webClient
                .get()
                .uri("/showcases/{showcaseId}", showcaseId)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.detail")
                .doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);
    }

    @Test
    @DisplayName("Fetching by ID with a timeout responds with gateway timeout and a problem in the body")
    void fetchShowcaseById_timeout_respondsWithGatewayTimeoutAndProblemInBody() {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery.builder().showcaseId(showcaseId).build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.error(new TimeoutException()));

        webClient
                .get()
                .uri("/showcases/{showcaseId}", showcaseId)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo("about:blank")
                .jsonPath("$.title")
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase())
                .jsonPath("$.status")
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
                .jsonPath("$.detail")
                .isEqualTo("Operation timeout exceeded.");

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);
    }
}
