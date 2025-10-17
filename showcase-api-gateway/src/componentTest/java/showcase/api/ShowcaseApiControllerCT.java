package showcase.api;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.NonNull;
import lombok.val;
import one.util.streamex.StreamEx;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
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
@ExtendWith(OutputCaptureExtension.class)
class ShowcaseApiControllerCT {

    @Configuration
    @ComponentScan(excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
    static class TestConfig {
    }

    @Autowired
    private WebTestClient webClient;

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    @SuppressWarnings("unused")
    private ShowcaseCommandOperations showcaseCommandOperations;

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    @SuppressWarnings("unused")
    private ShowcaseQueryOperations showcaseQueryOperations;

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    @SuppressWarnings("unused")
    private Cache<@NonNull FetchShowcaseListQuery, @NonNull List<@NonNull String>> fetchShowcaseListCache;

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    @SuppressWarnings("unused")
    private Cache<@NonNull String, @NonNull Showcase> fetchShowcaseByIdCache;

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
                        .showcaseId(scheduleResponse.getShowcaseId())
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
                 .jsonPath("$.title").isEqualTo("Bad Request")
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
                 .jsonPath("$.title").isEqualTo("Conflict")
                 .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
                 .jsonPath("$.detail").isEqualTo("Given title is in use already");

        verify(showcaseCommandOperations).schedule(any());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void scheduleShowcase_axonFailure_respondsWithServiceUnavailableStatusAndProblemInBody() {
        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(new NoHandlerForCommandException(anAlphabeticString(10))));

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
                 .jsonPath("$.title").isEqualTo("Service Unavailable")
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

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
                 .jsonPath("$.title").isEqualTo("Bad Request")
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
                 .jsonPath("$.title").isEqualTo("Bad Request")
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void startShowcase_axonFailure_respondsWithServiceUnavailableStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.start(any()))
                .willReturn(Mono.error(new CommandDispatchException(anAlphabeticString(10))));

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo("Service Unavailable")
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

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
                 .jsonPath("$.title").isEqualTo("Bad Request")
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void finishShowcase_axonFailure_respondsWithServiceUnavailableStatus() {
        val showcaseId = aShowcaseId();
        val command = FinishShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build();

        given(showcaseCommandOperations.finish(command))
                .willReturn(Mono.error(new NoHandlerForCommandException(anAlphabeticString(10))));

        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo("Service Unavailable")
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseCommandOperations).finish(command);
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
                 .jsonPath("$.title").isEqualTo("Bad Request")
                 .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                 .jsonPath("$.detail").isEqualTo("Invalid request.")
                 .jsonPath("$.pathErrors").isMap()
                 .jsonPath("$.pathErrors.showcaseId").isArray()
                 .jsonPath("$.pathErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.pathErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void removeShowcase_axonFailure_respondsWithServiceUnavailableStatus() {
        val showcaseId = aShowcaseId();
        val command = RemoveShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build();

        given(showcaseCommandOperations.remove(command))
                .willReturn(Mono.error(new CommandDispatchException(anAlphabeticString(10))));

        webClient.delete()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo("Service Unavailable")
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseCommandOperations).remove(command);
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
                .put(query, showcases.stream()
                                     .map(Showcase::getShowcaseId)
                                     .toList());
        verifyNoMoreInteractions(fetchShowcaseListCache);

        verify(fetchShowcaseByIdCache)
                .putAll(StreamEx.of(showcases)
                                .mapToEntry(Showcase::getShowcaseId, Function.identity())
                                .toMap());
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
                 .jsonPath("$.title").isEqualTo("Bad Request")
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
                 .jsonPath("$.title").isEqualTo("Bad Request")
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
    void fetchShowcaseList_failureFallbackCacheHit_logsFailureAndRespondsWithCachedResult(CapturedOutput output) {
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
                .willReturn(showcases.stream()
                                     .map(Showcase::getShowcaseId)
                                     .toList());
        for (val showcase : showcases) {
            given(fetchShowcaseByIdCache.getIfPresent(showcase.getShowcaseId())).willReturn(showcase);
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
            verify(fetchShowcaseByIdCache).getIfPresent(showcase.getShowcaseId());
        }
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        assertThat(output.getOut())
                .contains("Fallback on %s".formatted(query))
                .contains(failure.getMessage());
    }

    @Test
    void fetchShowcaseList_failureFallbackCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
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
                 .jsonPath("$.title").isEqualTo("Service Unavailable")
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchList(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseListCache).getIfPresent(query);
        verifyNoMoreInteractions(fetchShowcaseListCache);

        verifyNoInteractions(fetchShowcaseByIdCache);

        assertThat(output.getOut())
                .doesNotContain("Fallback on %s".formatted(query))
                .contains(failure.getMessage());
    }

    @Test
    void fetchShowcaseById_success_putsShowcaseIntoCacheAndRespondsWithOkStatusAndShowcaseInBody() {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcase.getShowcaseId())
                            .build();

        given(showcaseQueryOperations.fetchById(query)).willReturn(Mono.just(showcase));

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcase.getShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody(Showcase.class)
                 .isEqualTo(showcase);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseByIdCache).put(showcase.getShowcaseId(), showcase);
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
                 .jsonPath("$.title").isEqualTo("Bad Request")
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
                 .jsonPath("$.title").isEqualTo("Not Found")
                 .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                 .jsonPath("$.detail").isEqualTo("No showcase with id");

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verifyNoInteractions(fetchShowcaseByIdCache);
        verifyNoInteractions(fetchShowcaseListCache);
    }

    @Test
    void fetchShowcaseById_failureFallbackCacheHit_logsFailureAndRespondsWithCachedResult(CapturedOutput output) {
        val showcase = aShowcase();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcase.getShowcaseId())
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
        given(fetchShowcaseByIdCache.getIfPresent(showcase.getShowcaseId())).willReturn(showcase);

        webClient.get()
                 .uri("/showcases/{showcaseId}", showcase.getShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_JSON)
                 .expectBody(Showcase.class)
                 .isEqualTo(showcase);

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseByIdCache).getIfPresent(showcase.getShowcaseId());
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        verifyNoInteractions(fetchShowcaseListCache);

        assertThat(output.getOut())
                .contains("Fallback on %s".formatted(query))
                .contains(failure.getMessage());
    }

    @Test
    void fetchShowcaseById_failureFallbackCacheMiss_respondsWithServiceUnavailableStatusAndProblemInBody(
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
                 .jsonPath("$.title").isEqualTo("Service Unavailable")
                 .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                 .jsonPath("$.detail").doesNotHaveJsonPath();

        verify(showcaseQueryOperations).fetchById(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        verify(fetchShowcaseByIdCache).getIfPresent(showcaseId);
        verifyNoMoreInteractions(fetchShowcaseByIdCache);

        verifyNoInteractions(fetchShowcaseListCache);

        assertThat(output.getOut())
                .doesNotContain("Fallback on %s".formatted(query))
                .contains(failure.getMessage());
    }
}
