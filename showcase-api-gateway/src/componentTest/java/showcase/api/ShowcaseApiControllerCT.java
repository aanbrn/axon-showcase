package showcase.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.AsyncCache;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.val;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static io.github.resilience4j.circuitbreaker.CallNotPermittedException.createCallNotPermittedException;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static showcase.api.ShowcaseApi.IDEMPOTENCY_KEY_HEADER;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.test.RandomTestUtils.anAlphabeticString;
import static showcase.test.RandomTestUtils.anEnum;

@WebFluxTest(ShowcaseApiController.class)
class ShowcaseApiControllerCT {

    @Configuration
    @ComponentScan(excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
    @ImportAutoConfiguration(TaskExecutionAutoConfiguration.class)
    static class TestConfig {
    }

    static List<Arguments> commandAvailabilityFailures() {
        return List.of(
                argumentSet("Axon Error", new NoHandlerForCommandException(anAlphabeticString(10))),
                argumentSet("CircuitBreaker Error",
                            createCallNotPermittedException(CircuitBreaker.of(
                                    ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE,
                                    CircuitBreakerConfig.ofDefaults()))),
                argumentSet("Unknown Error", new Exception(anAlphabeticString(10)))
        );
    }

    static List<Arguments> queryAvailabilityFailures() {
        return List.of(
                argumentSet("WebClient Error",
                            WebClientResponseException.create(
                                    HttpStatus.BAD_GATEWAY.value(), anAlphabeticString(10), null, null, null)),
                argumentSet("CircuitBreaker Error",
                            createCallNotPermittedException(CircuitBreaker.of(
                                    ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE,
                                    CircuitBreakerConfig.ofDefaults()))),
                argumentSet("Unknown Error", new Exception(anAlphabeticString(10)))
        );
    }

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private ShowcaseCommandOperations showcaseCommandOperations;

    @MockitoBean
    private ShowcaseQueryOperations showcaseQueryOperations;

    @MockitoBean
    private AsyncCache<@NonNull FetchShowcaseListQuery, List<String>> fetchShowcaseListCache;

    @MockitoBean
    private AsyncCache<@NonNull String, Showcase> fetchShowcaseByIdCache;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @Test
    void scheduleShowcase_success_respondsWithCreatedStatusAndLocationHeaderAndShowcaseIdInBody() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.empty());

        val scheduleResponse =
                webClient.post()
                         .uri("/showcases")
                         .bodyValue(ScheduleShowcaseRequest
                                            .builder()
                                            .title(title)
                                            .startTime(startTime)
                                            .duration(duration)
                                            .build())
                         .exchange()
                         .expectStatus()
                         .isCreated()
                         .expectHeader()
                         .value(HttpHeaders.LOCATION, startsWith("/showcases/"))
                         .expectHeader()
                         .contentTypeCompatibleWith(APPLICATION_JSON)
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(scheduleResponse).isNotNull();

        verify(showcaseCommandOperations).schedule(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(scheduleResponse.showcaseId())
                        .title(title)
                        .startTime(startTime)
                        .duration(duration)
                        .build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void scheduleShowcase_invalidRequest_respondsWithBadRequestStatusAndProblemInBody() {
        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.bodyErrors").isMap()
                 .jsonPath("$.bodyErrors.title").isArray()
                 .jsonPath("$.bodyErrors.startTime").isArray()
                 .jsonPath("$.bodyErrors.duration").isArray();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void scheduleShowcase_alreadyUsedTitle_respondsWithConflictStatusAndProblemInBody() {
        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(
                        new ShowcaseCommandException(
                                ShowcaseCommandErrorDetails
                                        .builder()
                                        .errorCode(ShowcaseCommandErrorCode.TITLE_IN_USE)
                                        .errorMessage("Given title is in use already")
                                        .build())));

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(ScheduleShowcaseRequest
                                    .builder()
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
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.CONFLICT.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
                 .jsonPath("$.detail").isEqualTo("Given title is in use already");

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    void scheduleShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(
            ShowcaseCommandErrorCode errorCode) {

        try {
            var s = objectMapper.writeValueAsString(
                    ScheduleShowcaseRequest
                            .builder()
                            .title(aShowcaseTitle())
                            .startTime(aShowcaseStartTime(Instant.now()))
                            .duration(aShowcaseDuration())
                            .build());
            var request = objectMapper.readValue(s, ScheduleShowcaseRequest.class);
            System.out.println(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build())));

        val expectedStatus = switch (errorCode) {
            case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
        };

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(ScheduleShowcaseRequest
                                    .builder()
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
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(expectedStatus.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(expectedStatus.value())
                 .jsonPath("$.detail").isEqualTo(errorMessage);

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    void scheduleShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(error));

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(ScheduleShowcaseRequest
                                    .builder()
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
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void scheduleShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(
                new AbortedException(anAlphabeticString(10))));

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(ScheduleShowcaseRequest
                                    .builder()
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
    void scheduleShowcase_timeout_respondsWithAcceptedStatusAndIdempotencyKeyHeader() {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(new TimeoutException()));

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(ScheduleShowcaseRequest
                                    .builder()
                                    .title(aShowcaseTitle())
                                    .startTime(aShowcaseStartTime(Instant.now()))
                                    .duration(aShowcaseDuration())
                                    .build())
                 .exchange()
                 .expectStatus()
                 .isAccepted()
                 .expectHeader()
                 .value(IDEMPOTENCY_KEY_HEADER, idempotencyKey -> assertThat(idempotencyKey).isNotBlank());

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void scheduleShowcase_invalidIdempotencyKey_respondsWithBadRequestStatusAndProblemInBody() {
        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.error(new TimeoutException()));

        webClient.post()
                 .uri("/showcases")
                 .header(IDEMPOTENCY_KEY_HEADER, anAlphabeticString(10))
                 .bodyValue(ScheduleShowcaseRequest
                                    .builder()
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
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.headerErrors").isMap()
                 .jsonPath("$.headerErrors.%s".formatted(IDEMPOTENCY_KEY_HEADER)).isArray();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void startShowcase_success_respondsWithOkStatusAndEmptyBody() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.start(any())).willReturn(Mono.empty());

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();

        verify(showcaseCommandOperations).start(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void startShowcase_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient.put()
                 .uri("/showcases/{showcaseId}/start", anInvalidShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    void startShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.start(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build())));

        val expectedStatus = switch (errorCode) {
            case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
        };

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(expectedStatus)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(expectedStatus.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(expectedStatus.value())
                 .jsonPath("$.detail").isEqualTo(errorMessage);

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    void startShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.start(any())).willReturn(Mono.error(error));

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void startShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.start(any())).willReturn(Mono.error(
                new AbortedException(anAlphabeticString(10))));

        webClient.put()
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
    void startShowcase_timeout_respondsWithAcceptedStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.start(any())).willReturn(Mono.error(new TimeoutException()));

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isAccepted();

        verify(showcaseCommandOperations).start(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void finishShowcase_success_respondsWithOkStatusAndEmptyBody() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.finish(any())).willReturn(Mono.empty());

        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();

        verify(showcaseCommandOperations).finish(
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void finishShowcase_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", anInvalidShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    void finishShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.finish(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build())));

        val expectedStatus = switch (errorCode) {
            case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
        };

        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(expectedStatus)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(expectedStatus.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(expectedStatus.value())
                 .jsonPath("$.detail").isEqualTo(errorMessage);

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    void finishShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.finish(any())).willReturn(Mono.error(error));

        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void finishShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.finish(any())).willReturn(Mono.error(
                new AbortedException(anAlphabeticString(10))));

        webClient.put()
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
    void finishShowcase_timeout_respondsWithAcceptedStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.finish(any())).willReturn(Mono.error(new TimeoutException()));

        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isAccepted();

        verify(showcaseCommandOperations).finish(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void removeShowcase_success_respondsWithOkStatusAndEmptyBody() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.remove(any())).willReturn(Mono.empty());

        webClient.delete()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();

        verify(showcaseCommandOperations).remove(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void removeShowcase_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient.delete()
                 .uri("/showcases/{showcaseId}", anInvalidShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @EnumSource(ShowcaseCommandErrorCode.class)
    void removeShowcase_commandFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseCommandErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseCommandOperations.remove(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build())));

        val expectedStatus = switch (errorCode) {
            case INVALID_COMMAND -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TITLE_IN_USE, ILLEGAL_STATE -> HttpStatus.CONFLICT;
        };

        webClient.delete()
                 .uri("/showcases/{showcaseId}", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(expectedStatus)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(expectedStatus.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(expectedStatus.value())
                 .jsonPath("$.detail").isEqualTo(errorMessage);

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @ParameterizedTest
    @MethodSource("commandAvailabilityFailures")
    void removeShowcase_availabilityFailure_respondsWithServiceUnavailableStatusAndProblemInBody(Exception error) {
        given(showcaseCommandOperations.remove(any())).willReturn(Mono.error(error));

        webClient.delete()
                 .uri("/showcases/{showcaseId}", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void removeShowcase_abortedRequest_respondsWithRequestTimeoutStatusAndEmptyBody() {
        given(showcaseCommandOperations.remove(any())).willReturn(Mono.error(
                new AbortedException(anAlphabeticString(10))));

        webClient.delete()
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
    void removeShowcase_timeout_respondsWithAcceptedStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.remove(any())).willReturn(Mono.error(new TimeoutException()));

        webClient.delete()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isAccepted();

        verify(showcaseCommandOperations).remove(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void fetchShowcaseList_success_putShowcasesIntoCachesAndRespondsWithOkStatusAndShowcasesInBody() {
        val showcases = showcases();
        val query = FetchShowcaseListQuery.builder().build();

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.fromIterable(showcases));

        webClient.get()
                 .uri(uriBuilder ->
                              uriBuilder.path("/showcases")
                                        .build())
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBodyList(Showcase.class)
                 .isEqualTo(showcases);

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseListCache)
                .put(eq(query), assertArg(future -> assertThat(future).isCompletedWithValueMatching(
                        value -> Objects.equals(value, showcases.stream()
                                                                .map(Showcase::showcaseId)
                                                                .toList()))));
        verifyNoMoreInteractions(fetchShowcaseListCache);

        for (val showcase : showcases) {
            verify(fetchShowcaseByIdCache)
                    .put(eq(showcase.showcaseId()), assertArg(future -> assertThat(future).isCompletedWithValueMatching(
                            value -> Objects.equals(value, showcase))));
        }
        verifyNoMoreInteractions(fetchShowcaseByIdCache);
    }

    @Test
    void fetchShowcaseList_invalidAfterId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient.get()
                 .uri(uriBuilder -> uriBuilder.path("/showcases")
                                              .queryParam("afterId", anInvalidShowcaseId())
                                              .build())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.paramErrors").isMap()
                 .jsonPath("$.paramErrors.afterId").isArray()
                 .jsonPath("$.paramErrors.afterId[0]").isNotEmpty()
                 .jsonPath("$.paramErrors.afterId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
        verifyNoInteractions(fetchShowcaseListCache);
        verifyNoInteractions(fetchShowcaseByIdCache);
    }

    @ParameterizedTest
    @ValueSource(ints = { FetchShowcaseListQuery.MIN_SIZE - 1, FetchShowcaseListQuery.MAX_SIZE + 1 })
    void fetchShowcaseList_invalidSize_respondsWithBadRequestStatusAndProblemInBody(int size) {
        webClient.get()
                 .uri(uriBuilder -> uriBuilder.path("/showcases")
                                              .queryParam("size", size)
                                              .build())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.paramErrors").isMap()
                 .jsonPath("$.paramErrors.size").isArray()
                 .jsonPath("$.paramErrors.size[0]").isNotEmpty()
                 .jsonPath("$.paramErrors.size[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
        verifyNoInteractions(fetchShowcaseListCache);
        verifyNoInteractions(fetchShowcaseByIdCache);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void fetchShowcaseList_fallbackFetchShowcaseListCacheHit_logsFailureAndRespondsWithCachedResult(
            CapturedOutput output) {
        val showcases = showcases();
        val query = FetchShowcaseListQuery.builder().build();
        val failure =
                WebClientResponseException.create(
                        anEnum(HttpStatus.class),
                        anAlphabeticString(32),
                        new HttpHeaders(),
                        new byte[0],
                        null,
                        null);

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.error(failure));
        given(fetchShowcaseListCache.getIfPresent(query))
                .willReturn(completedFuture(showcases.stream()
                                                     .map(Showcase::showcaseId)
                                                     .toList()));
        for (val showcase : showcases) {
            given(fetchShowcaseByIdCache.getIfPresent(showcase.showcaseId())).willReturn(completedFuture(showcase));
        }

        webClient.get()
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

        verify(fetchShowcaseListCache).getIfPresent(query);
        verifyNoMoreInteractions(fetchShowcaseListCache);

        for (val showcase : showcases) {
            verify(fetchShowcaseByIdCache).getIfPresent(showcase.showcaseId());
        }
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        await().untilAsserted(
                () -> assertThat(output)
                              .contains("Fallback on %s".formatted(query))
                              .contains(failure.getMessage()));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void fetchShowcaseList_fallbackFetchShowcaseListCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
            CapturedOutput output) {
        val query = FetchShowcaseListQuery.builder().build();
        val failure =
                WebClientResponseException.create(
                        anEnum(HttpStatus.class),
                        anAlphabeticString(32),
                        new HttpHeaders(),
                        new byte[0],
                        null,
                        null);

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.error(failure));

        webClient.get()
                 .uri("/showcases")
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseListCache).getIfPresent(query);
        verifyNoMoreInteractions(fetchShowcaseListCache);

        verifyNoInteractions(fetchShowcaseByIdCache);

        await().untilAsserted(
                () -> assertThat(output)
                              .doesNotContain("Fallback on %s".formatted(query))
                              .contains(failure.getMessage()));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void fetchShowcaseList_fallbackFetchShowcaseByIdCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
            CapturedOutput output) {
        val query = FetchShowcaseListQuery.builder().build();
        val showcaseId = aShowcaseId();
        val failure =
                WebClientResponseException.create(
                        anEnum(HttpStatus.class),
                        anAlphabeticString(32),
                        new HttpHeaders(),
                        new byte[0],
                        null,
                        null);

        given(showcaseQueryOperations.fetchList(query)).willReturn(Flux.error(failure));
        given(fetchShowcaseListCache.getIfPresent(query)).willReturn(completedFuture(List.of(showcaseId)));

        webClient.get()
                 .uri("/showcases")
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseListCache).getIfPresent(query);
        verifyNoMoreInteractions(fetchShowcaseListCache);

        verify(fetchShowcaseByIdCache).getIfPresent(showcaseId);
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        await().untilAsserted(
                () -> assertThat(output)
                              .doesNotContain("Fallback on %s".formatted(query))
                              .contains(failure.getMessage()));
    }

    @ParameterizedTest
    @EnumSource(ShowcaseQueryErrorCode.class)
    void fetchShowcaseList_queryFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseQueryErrorCode errorCode) {
        val errorMessage = anAlphabeticString(10);

        given(showcaseQueryOperations.fetchList(any())).willReturn(Flux.error(
                new ShowcaseQueryException(
                        ShowcaseQueryErrorDetails
                                .builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build())));

        val expectedStatus = switch (errorCode) {
            case INVALID_QUERY -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };

        webClient.get()
                 .uri(uriBuilder ->
                              uriBuilder.path("/showcases")
                                        .build())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(expectedStatus)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(expectedStatus.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(expectedStatus.value())
                 .jsonPath("$.detail").isEqualTo(errorMessage);

        verify(showcaseQueryOperations).fetchList(any());
        verifyNoMoreInteractions(showcaseQueryOperations);

        verifyNoInteractions(fetchShowcaseListCache);
        verifyNoInteractions(fetchShowcaseByIdCache);
    }

    @ParameterizedTest
    @MethodSource("queryAvailabilityFailures")
    void fetchShowcaseList_availabilityFailure_respondsWithRelatedStatusAndProblemInBody(Exception error) {
        given(showcaseQueryOperations.fetchList(any())).willReturn(Flux.error(error));

        webClient.get()
                 .uri(uriBuilder ->
                              uriBuilder.path("/showcases")
                                        .build())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(any());
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseListCache).getIfPresent(any());
        verifyNoMoreInteractions(fetchShowcaseListCache);

        verifyNoInteractions(fetchShowcaseByIdCache);
    }

    @Test
    void fetchShowcaseList_timeout_respondsWithGatewayTimeoutAndProblemInBody() {
        given(showcaseQueryOperations.fetchList(any())).willReturn(Flux.error(new TimeoutException()));

        webClient.get()
                 .uri(uriBuilder ->
                              uriBuilder.path("/showcases")
                                        .build())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
                 .jsonPath("$.detail").isEqualTo("Operation timeout exceeded.");

        verify(showcaseQueryOperations).fetchList(any());
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseListCache).getIfPresent(any());
        verifyNoMoreInteractions(fetchShowcaseListCache);

        verifyNoInteractions(fetchShowcaseByIdCache);
    }

    @Test
    void fetchShowcaseById_success_putsShowcaseIntoCacheAndRespondsWithOkStatusAndShowcaseInBody() {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcase.showcaseId())
                            .build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.just(showcase));

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcase.showcaseId())
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody(Showcase.class)
                 .isEqualTo(showcase);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseByIdCache)
                .put(eq(showcase.showcaseId()), assertArg(future -> assertThat(future).isCompletedWithValueMatching(
                        value -> Objects.equals(value, showcase))));
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        verifyNoInteractions(fetchShowcaseListCache);
    }

    @Test
    void fetchShowcaseById_invalidShowcaseId_respondsWithBadRequestStatusAndProblemInBody() {
        webClient.get()
                 .uri("/showcases/{showcaseId}", anInvalidShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isBadRequest()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
        verifyNoInteractions(fetchShowcaseByIdCache);
        verifyNoInteractions(fetchShowcaseListCache);
    }

    @Test
    void fetchShowcaseById_nonExistingShowcase_respondsWithNotFoundStatusAndProblemInBody() {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcaseId)
                            .build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.error(
                new ShowcaseQueryException(
                        ShowcaseQueryErrorDetails
                                .builder()
                                .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                                .errorMessage("No showcase with id")
                                .build())));

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isNotFound()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                 .jsonPath("$.detail").isEqualTo("No showcase with id");

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verifyNoInteractions(fetchShowcaseByIdCache);
        verifyNoInteractions(fetchShowcaseListCache);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void fetchShowcaseById_fallbackFetchShowcaseByCacheHit_logsFailureAndRespondsWithCachedResult(
            CapturedOutput output) {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcase.showcaseId())
                            .build();
        val failure =
                WebClientResponseException.create(
                        anEnum(HttpStatus.class),
                        anAlphabeticString(32),
                        new HttpHeaders(),
                        new byte[0],
                        null,
                        null);

        given(showcaseQueryOperations.fetchById(any())).willReturn(Mono.error(failure));
        given(fetchShowcaseByIdCache.getIfPresent(showcase.showcaseId())).willReturn(completedFuture(showcase));

        webClient.get()
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

        verify(fetchShowcaseByIdCache).getIfPresent(showcase.showcaseId());
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        verifyNoInteractions(fetchShowcaseListCache);

        await().untilAsserted(
                () -> assertThat(output)
                              .contains("Fallback on %s".formatted(query))
                              .contains(failure.getMessage()));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void fetchShowcaseById_fallbackFetchShowcaseByIdCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
            CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcaseId)
                            .build();
        val failure =
                WebClientResponseException.create(
                        anEnum(HttpStatus.class),
                        anAlphabeticString(32),
                        new HttpHeaders(),
                        new byte[0],
                        null,
                        null);

        given(showcaseQueryOperations.fetchById(any())).willReturn(Mono.error(failure));

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseByIdCache).getIfPresent(showcaseId);
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        verifyNoInteractions(fetchShowcaseListCache);

        await().untilAsserted(
                () -> assertThat(output)
                              .doesNotContain("Fallback on %s".formatted(query))
                              .contains(failure.getMessage()));
    }

    @ParameterizedTest
    @EnumSource(ShowcaseQueryErrorCode.class)
    void fetchShowcaseById_queryFailure_respondsWithRelatedStatusAndProblemInBody(ShowcaseQueryErrorCode errorCode) {
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build();
        val errorMessage = anAlphabeticString(10);

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.error(
                new ShowcaseQueryException(
                        ShowcaseQueryErrorDetails
                                .builder()
                                .errorCode(errorCode)
                                .errorMessage(errorMessage)
                                .build())));

        val expectedStatus = switch (errorCode) {
            case INVALID_QUERY -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };

        webClient.get()
                 .uri("/showcases/{showcaseId}", query.showcaseId())
                 .exchange()
                 .expectStatus()
                 .isEqualTo(expectedStatus)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(expectedStatus.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(expectedStatus.value())
                 .jsonPath("$.detail").isEqualTo(errorMessage);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verifyNoInteractions(fetchShowcaseListCache);
        verifyNoInteractions(fetchShowcaseByIdCache);
    }

    @ParameterizedTest
    @MethodSource("queryAvailabilityFailures")
    void fetchShowcaseById_availabilityFailure_respondsWithRelatedStatusAndProblemInBody(Exception error) {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcaseId)
                            .build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.error(error));

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verifyNoInteractions(fetchShowcaseListCache);

        verify(fetchShowcaseByIdCache).getIfPresent(showcaseId);
        verifyNoMoreInteractions(fetchShowcaseByIdCache);
    }

    @Test
    void fetchShowcaseById_timeout_respondsWithGatewayTimeoutAndProblemInBody() {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcaseId)
                            .build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.error(new TimeoutException()));

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
                 .jsonPath("$.detail").isEqualTo("Operation timeout exceeded.");

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verifyNoInteractions(fetchShowcaseListCache);

        verify(fetchShowcaseByIdCache).getIfPresent(showcaseId);
        verifyNoMoreInteractions(fetchShowcaseByIdCache);
    }
}
