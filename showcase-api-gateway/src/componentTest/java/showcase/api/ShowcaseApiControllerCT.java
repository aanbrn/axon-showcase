package showcase.api;

import lombok.val;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
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

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.context.bean.override.mockito.MockReset.NONE;
import static showcase.api.ShowcaseApiConstants.FETCH_ALL_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_BY_ID_CACHE_NAME;
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
    static class TestConfig {

        @Bean
        HandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver() {
            return new ReactivePageableHandlerMethodArgumentResolver();
        }
    }

    @Autowired
    private WebTestClient webClient;

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    private ShowcaseCommandOperations showcaseCommandOperations;

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    private ShowcaseQueryOperations showcaseQueryOperations;

    @MockitoBean(answers = RETURNS_DEEP_STUBS, reset = NONE)
    private CacheManager cacheManager;

    @Test
    void scheduleShowcase_success_respondsWithCreatedStatusAndLocationHeaderAndEmptyBody() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        given(showcaseCommandOperations.schedule(any())).willReturn(Mono.empty());

        val response =
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

        verify(showcaseCommandOperations).schedule(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(requireNonNull(response).getShowcaseId())
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
                 .jsonPath("$.detail").isEqualTo("Invalid request content.")
                 .jsonPath("$.fieldErrors").isMap()
                 .jsonPath("$.fieldErrors.title").isArray()
                 .jsonPath("$.fieldErrors.startTime").isArray()
                 .jsonPath("$.fieldErrors.duration").isArray();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void scheduleShowcase_alreadyUsedTitle_respondsWithConflictStatusAndProblemInBody() {
        given(showcaseCommandOperations.schedule(any()))
                .willReturn(Mono.error(new ShowcaseCommandException(
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
                 .jsonPath("$.detail").isEqualTo("Invalid request parameter.")
                 .jsonPath("$.parameterErrors").isMap()
                 .jsonPath("$.parameterErrors.showcaseId").isArray()
                 .jsonPath("$.parameterErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.parameterErrors.showcaseId[1]").doesNotHaveJsonPath();

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

        verify(showcaseCommandOperations).start(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
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
                 .jsonPath("$.detail").isEqualTo("Invalid request parameter.")
                 .jsonPath("$.parameterErrors").isMap()
                 .jsonPath("$.parameterErrors.showcaseId").isArray()
                 .jsonPath("$.parameterErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.parameterErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void finishShowcase_axonFailure_respondsWithServiceUnavailableStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.finish(any()))
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

        verify(showcaseCommandOperations).finish(
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
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
                 .jsonPath("$.detail").isEqualTo("Invalid request parameter.")
                 .jsonPath("$.parameterErrors").isMap()
                 .jsonPath("$.parameterErrors.showcaseId").isArray()
                 .jsonPath("$.parameterErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.parameterErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void removeShowcase_axonFailure_respondsWithServiceUnavailableStatus() {
        val showcaseId = aShowcaseId();

        given(showcaseCommandOperations.remove(any()))
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

        verify(showcaseCommandOperations).remove(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
        verifyNoMoreInteractions(showcaseCommandOperations);
    }

    @Test
    void fetchAllShowcases_success_respondsWithOkStatusAndShowcasesInBody() {
        val showcases = showcases();
        val query = FetchShowcaseListQuery.builder().build();

        given(showcaseQueryOperations.fetchAll(query)).willReturn(Flux.fromIterable(showcases));

        webClient.get()
                 .uri("/showcases")
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBodyList(Showcase.class)
                 .isEqualTo(showcases);

        verify(showcaseQueryOperations).fetchAll(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        val fetchAllCache = cacheManager.getCache(FETCH_ALL_CACHE_NAME);
        verify(fetchAllCache).put(query, showcases);
        verifyNoMoreInteractions(fetchAllCache);
    }

    @Test
    void fetchAllShowcases_webClientFailure_respondsWithServiceUnavailableStatusAndProblemInBody() {
        val query = FetchShowcaseListQuery.builder().build();

        given(showcaseQueryOperations.fetchAll(query))
                .willReturn(Flux.error(WebClientResponseException.create(
                        anEnum(HttpStatus.class), anAlphabeticString(10), new HttpHeaders(), new byte[0], null, null)));

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

        verify(showcaseQueryOperations).fetchAll(query);
        verifyNoMoreInteractions(showcaseQueryOperations);

        val fetchAllCache = cacheManager.getCache(FETCH_ALL_CACHE_NAME);
        verify(fetchAllCache).get(query, List.class);
        verifyNoMoreInteractions(fetchAllCache);
    }

    @Test
    void fetchShowcaseById_success_respondsWithOkStatusAndShowcaseInBody() {
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

        val fetchByIdCache = cacheManager.getCache(FETCH_BY_ID_CACHE_NAME);
        verify(fetchByIdCache).put(query, showcase);
        verifyNoMoreInteractions(fetchByIdCache);
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
                 .jsonPath("$.detail").isEqualTo("Invalid request parameter.")
                 .jsonPath("$.parameterErrors").isMap()
                 .jsonPath("$.parameterErrors.showcaseId").isArray()
                 .jsonPath("$.parameterErrors.showcaseId[0]").isNotEmpty()
                 .jsonPath("$.parameterErrors.showcaseId[1]").doesNotHaveJsonPath();

        verifyNoInteractions(showcaseCommandOperations);
    }

    @Test
    void fetchShowcaseById_nonExistingShowcase_respondsWithNotFoundStatusAndProblemInBody() {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcaseId)
                            .build();

        given(showcaseQueryOperations.fetchById(query))
                .willReturn(Mono.error(new ShowcaseQueryException(
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

        val fetchByIdCache = cacheManager.getCache(FETCH_BY_ID_CACHE_NAME);
        verifyNoInteractions(fetchByIdCache);
    }

    @Test
    void fetchShowcaseById_webClientFailure_respondsWithServiceUnavailableStatusAndProblemInBody() {
        val showcaseId = aShowcaseId();
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcaseId)
                            .build();

        given(showcaseQueryOperations.fetchById(any()))
                .willReturn(Mono.error(WebClientResponseException.create(
                        anEnum(HttpStatus.class), anAlphabeticString(10), new HttpHeaders(), new byte[0], null, null)));

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

        val fetchByIdCache = cacheManager.getCache(FETCH_BY_ID_CACHE_NAME);
        verify(fetchByIdCache).get(query, Showcase.class);
        verifyNoMoreInteractions(fetchByIdCache);
    }
}
