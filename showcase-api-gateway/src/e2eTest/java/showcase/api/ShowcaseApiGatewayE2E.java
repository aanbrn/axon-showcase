package showcase.api;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import showcase.query.Showcase;
import showcase.query.ShowcaseStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.setDefaultTimeout;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.security.web.server.header.CacheControlServerHttpHeadersWriter.CACHE_CONTRTOL_VALUE;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;
import static showcase.test.RandomTestUtils.anElementOf;

@Testcontainers(parallel = true)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("Showcase API gateway end-to-end tests")
class ShowcaseApiGatewayE2E {

    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(60);

    static final Network network = Network.newNetwork();

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> dbEvents =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-db-events"))
                    .withNetwork(network)
                    .withDatabaseName("showcase-events");

    @Container
    @SuppressWarnings("resource")
    static final KafkaContainer kafka =
            new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version")) {
                @Override
                public String getBootstrapServers() {
                    return "axon-showcase-kafka:9092";
                }
            }
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-kafka"))
                    .withNetwork(network);

    @Container
    @SuppressWarnings("resource")
    static final OpenSearchContainer<?> osViews =
            new OpenSearchContainer<>("opensearchproject/opensearch:" + System.getProperty("opensearch.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-os-views"))
                    .withNetwork(network);

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> commandService =
            new GenericContainer<>("aanbrn/axon-showcase-command-service:" + System.getProperty("project.version"))
                    .dependsOn(dbEvents, kafka)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-command-service"))
                    .withNetwork(network)
                    .withEnv("DB_USER", dbEvents.getUsername())
                    .withEnv("DB_PASSWORD", dbEvents.getPassword())
                    .withEnv("LOGGING_LEVEL_SHOWCASE_COMMAND", "DEBUG")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> projectionService =
            new GenericContainer<>("aanbrn/axon-showcase-projection-service:" + System.getProperty("project.version"))
                    .dependsOn(kafka, osViews)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-projection-service"))
                    .withNetwork(network)
                    .withEnv("DB_USER", dbEvents.getUsername())
                    .withEnv("DB_PASSWORD", dbEvents.getPassword())
                    .withEnv("REDIS_CLUSTER_DYNAMIC_REFRESH_SOURCES", "off")
                    .withEnv("LOGGING_LEVEL_SHOWCASE_PROJECTION", "DEBUG")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> queryService =
            new GenericContainer<>("aanbrn/axon-showcase-query-service:" + System.getProperty("project.version"))
                    .dependsOn(osViews)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-query-service"))
                    .withNetwork(network)
                    .withEnv("LOGGING_LEVEL_SHOWCASE_QUERY", "DEBUG")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> apiGateway =
            new GenericContainer<>("aanbrn/axon-showcase-api-gateway:" + System.getProperty("project.version"))
                    .dependsOn(commandService, queryService)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-api-gateway"))
                    .withNetwork(network)
                    .withEnv("LOGGING_LEVEL_SHOWCASE_API", "DEBUG")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    private WebTestClient webClient;
    private final List<String> createdShowcaseIds = new ArrayList<>();

    @BeforeAll
    static void setUpAwaitility() {
        setDefaultTimeout(AWAIT_TIMEOUT);
    }

    @BeforeEach
    void setUp() {
        webClient =
                WebTestClient
                        .bindToServer()
                        .baseUrl("http://localhost:" + apiGateway.getMappedPort(8080))
                        .build();
    }

    @AfterEach
    @SuppressWarnings("EmptyCatch")
    void tearDown() {
        for (val showcaseId : new ArrayList<>(createdShowcaseIds)) {
            try {
                webClient.delete()
                         .uri("/showcases/{showcaseId}", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk();
            } catch (RuntimeException ignored) {
            }
        }
        createdShowcaseIds.clear();
    }

    String scheduleShowcase() {
        return scheduleShowcase(aShowcaseTitle(), aShowcaseStartTime(Instant.now()), aShowcaseDuration());
    }

    String scheduleShowcase(String title, Instant startTime, Duration duration) {
        val response =
                webClient.post()
                         .uri("/showcases")
                         .bodyValue(Map.of(
                                 "title", title,
                                 "startTime", startTime,
                                 "duration", duration))
                         .exchange()
                         .expectStatus()
                         .isCreated()
                         .expectHeader()
                         .value(HttpHeaders.LOCATION, startsWith("/showcases/"))
                         .expectHeader()
                         .contentTypeCompatibleWith(APPLICATION_JSON)
                         .expectHeader()
                         .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(response).isNotNull();

        createdShowcaseIds.add(response.showcaseId());

        return response.showcaseId();
    }

    Showcase fetchShowcase(String showcaseId) {
        val body =
                webClient.get()
                         .uri("/showcases/{showcaseId}", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk()
                         .expectBody(Showcase.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(body).isNotNull();

        return body;
    }

    List<Showcase> fetchShowcases(String uriTemplate, Object... uriVariables) {
        val body =
                webClient.get()
                         .uri(uriTemplate, uriVariables)
                         .exchange()
                         .expectStatus()
                         .isOk()
                         .expectBodyList(Showcase.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(body).isNotNull();

        return body;
    }

    void awaitShowcase(String showcaseId, String title, Instant startTime, Duration duration, ShowcaseStatus status) {
        await().untilAsserted(() -> {
            val showcase = fetchShowcase(showcaseId);

            assertThat(showcase.showcaseId()).isEqualTo(showcaseId);
            assertThat(showcase.title()).isEqualTo(title);
            assertThat(showcase.startTime()).isEqualTo(startTime);
            assertThat(showcase.duration()).isEqualTo(duration);
            assertThat(showcase.status()).isEqualTo(status);
            assertShowcaseState(showcase, status);
        });
    }

    void awaitShowcaseStatus(String showcaseId, ShowcaseStatus status) {
        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isOk()
                               .expectBody(Showcase.class)
                               .value(Showcase::status, equalTo(status)));
    }

    void awaitShowcaseRemoved(String showcaseId) {
        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isNotFound());
    }

    void startShowcase(String showcaseId) {
        webClient.put()
                 .uri("/showcases/{showcaseId}/start", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();
    }

    void finishShowcase(String showcaseId) {
        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();
    }

    void removeShowcase(String showcaseId) {
        webClient.delete()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectHeader()
                 .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                 .expectBody()
                 .isEmpty();

        awaitShowcaseRemoved(showcaseId);

        createdShowcaseIds.remove(showcaseId);
    }

    void assertShowcaseState(Showcase showcase, ShowcaseStatus status) {
        assertThat(showcase.scheduledAt()).isNotNull();
        switch (status) {
            case SCHEDULED -> {
                assertThat(showcase.startedAt()).isNull();
                assertThat(showcase.finishedAt()).isNull();
            }
            case STARTED -> {
                assertThat(showcase.startedAt()).isNotNull();
                assertThat(showcase.finishedAt()).isNull();
            }
            case FINISHED -> {
                assertThat(showcase.startedAt()).isNotNull();
                assertThat(showcase.finishedAt()).isNotNull();
            }
        }
    }

    WebTestClient.BodyContentSpec assertProblemDetail(WebTestClient.ResponseSpec response, HttpStatus status,
                                                      String detail) {
        return response.expectStatus()
                       .isEqualTo(status.value())
                       .expectHeader()
                       .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                       .expectBody()
                       .jsonPath("$.type").isEqualTo("about:blank")
                       .jsonPath("$.title").isEqualTo(status.getReasonPhrase())
                       .jsonPath("$.status").isEqualTo(status.value())
                       .jsonPath("$.detail").isEqualTo(detail);
    }

    void assertInvalidShowcaseIdProblem(WebTestClient.ResponseSpec response) {
        assertProblemDetail(response, HttpStatus.BAD_REQUEST, "Invalid request.")
                .jsonPath("$.pathErrors").isMap()
                .jsonPath("$.pathErrors.showcaseId").isNotEmpty();
    }

    @Nested
    @DisplayName("Scheduling")
    class SchedulingTests {

        @Test
        @DisplayName("A valid request schedules and exposes the showcase")
        void scheduleShowcase_validRequest_exposesScheduledShowcase() {
            val title = aShowcaseTitle();
            val startTime = aShowcaseStartTime(Instant.now());
            val duration = aShowcaseDuration();

            val showcaseId = scheduleShowcase(title, startTime, duration);

            awaitShowcase(showcaseId, title, startTime, duration, ShowcaseStatus.SCHEDULED);
        }

        @Test
        @DisplayName("An already used title fails with a title-in-use problem")
        void scheduleShowcase_alreadyUsedTitle_failsWithTitleInUseProblem() {
            val title = aShowcaseTitle();

            scheduleShowcase(title, aShowcaseStartTime(Instant.now()), aShowcaseDuration());

            assertProblemDetail(
                    webClient.post()
                             .uri("/showcases")
                             .bodyValue(Map.of(
                                     "title", title,
                                     "startTime", aShowcaseStartTime(Instant.now()),
                                     "duration", aShowcaseDuration()))
                             .exchange(),
                    HttpStatus.CONFLICT,
                    "Given title is in use already")
                    .jsonPath("$.instance").isEqualTo("/showcases");
        }

        @ParameterizedTest
        @MethodSource("invalidScheduleRequests")
        @DisplayName("An invalid request fails with a validation problem")
        void scheduleShowcase_invalidRequest_failsWithValidationProblem(Map<String, Object> body, String field) {
            assertProblemDetail(
                    webClient.post()
                             .uri("/showcases")
                             .bodyValue(body)
                             .exchange(),
                    HttpStatus.BAD_REQUEST,
                    "Invalid request.")
                    .jsonPath("$.bodyErrors").isMap()
                    .jsonPath("$.bodyErrors." + field).isNotEmpty();
        }

        static Stream<Arguments> invalidScheduleRequests() {
            return Stream.of(
                    argumentSet("blank title", Map.of(
                            "title", "",
                            "startTime", aShowcaseStartTime(Instant.now()),
                            "duration", aShowcaseDuration()), "title"),
                    argumentSet("too long title", Map.of(
                            "title", aTooLongShowcaseTitle(),
                            "startTime", aShowcaseStartTime(Instant.now()),
                            "duration", aShowcaseDuration()), "title"),
                    argumentSet("past start time", Map.of(
                            "title", aShowcaseTitle(),
                            "startTime", Instant.now().minusSeconds(1),
                            "duration", aShowcaseDuration()), "startTime"),
                    argumentSet("too short duration", Map.of(
                            "title", aShowcaseTitle(),
                            "startTime", aShowcaseStartTime(Instant.now()),
                            "duration", aTooShortShowcaseDuration()), "duration"),
                    argumentSet("too long duration", Map.of(
                            "title", aShowcaseTitle(),
                            "startTime", aShowcaseStartTime(Instant.now()),
                            "duration", aTooLongShowcaseDuration()), "duration"));
        }
    }

    @Nested
    @DisplayName("Starting")
    class StartingTests {

        @Test
        @DisplayName("An existing showcase is started and exposed")
        void startShowcase_existingShowcase_exposesStartedShowcase() {
            val title = aShowcaseTitle();
            val startTime = aShowcaseStartTime(Instant.now());
            val duration = aShowcaseDuration();

            val showcaseId = scheduleShowcase(title, startTime, duration);

            startShowcase(showcaseId);

            awaitShowcase(showcaseId, title, startTime, duration, ShowcaseStatus.STARTED);
        }

        @Test
        @DisplayName("Starting a non-existing showcase fails with a not-found problem")
        void startShowcase_nonExistingShowcase_failsWithNotFoundProblem() {
            assertProblemDetail(
                    webClient.put()
                             .uri("/showcases/{showcaseId}/start", aShowcaseId())
                             .exchange(),
                    HttpStatus.NOT_FOUND,
                    "No showcase with given ID");
        }

        @Test
        @DisplayName("Starting a finished showcase fails with a conflict problem")
        void startShowcase_finishedShowcase_failsWithConflictProblem() {
            val showcaseId = scheduleShowcase();
            startShowcase(showcaseId);
            finishShowcase(showcaseId);
            awaitShowcaseStatus(showcaseId, ShowcaseStatus.FINISHED);

            assertProblemDetail(
                    webClient.put()
                             .uri("/showcases/{showcaseId}/start", showcaseId)
                             .exchange(),
                    HttpStatus.CONFLICT,
                    "Showcase is finished already");
        }

        @Test
        @DisplayName("Starting an already started showcase does not fail and keeps the original start time")
        void startShowcase_alreadyStarted_doesNotFail() {
            val showcaseId = scheduleShowcase();
            startShowcase(showcaseId);
            awaitShowcaseStatus(showcaseId, ShowcaseStatus.STARTED);

            val before = fetchShowcase(showcaseId);
            assertThat(before.startedAt()).isNotNull();

            startShowcase(showcaseId);

            val after = fetchShowcase(showcaseId);
            assertThat(after.startedAt()).isEqualTo(before.startedAt());
        }

        @Test
        @DisplayName("Starting with an invalid showcase ID fails with a validation problem")
        void startShowcase_invalidShowcaseId_failsWithValidationProblem() {
            assertInvalidShowcaseIdProblem(
                    webClient.put()
                             .uri("/showcases/{showcaseId}/start", anInvalidShowcaseId())
                             .exchange());
        }
    }

    @Nested
    @DisplayName("Finishing")
    class FinishingTests {

        @Test
        @DisplayName("An existing started showcase is finished and exposed")
        void finishShowcase_existingShowcase_exposesFinishedShowcase() {
            val title = aShowcaseTitle();
            val startTime = aShowcaseStartTime(Instant.now());
            val duration = aShowcaseDuration();

            val showcaseId = scheduleShowcase(title, startTime, duration);

            startShowcase(showcaseId);
            finishShowcase(showcaseId);

            awaitShowcase(showcaseId, title, startTime, duration, ShowcaseStatus.FINISHED);
        }

        @Test
        @DisplayName("Finishing a non-existing showcase fails with a not-found problem")
        void finishShowcase_nonExistingShowcase_failsWithNotFoundProblem() {
            assertProblemDetail(
                    webClient.put()
                             .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                             .exchange(),
                    HttpStatus.NOT_FOUND,
                    "No showcase with given ID");
        }

        @Test
        @DisplayName("Finishing a not-started showcase fails with a conflict problem")
        void finishShowcase_notStarted_failsWithConflictProblem() {
            val showcaseId = scheduleShowcase();

            assertProblemDetail(
                    webClient.put()
                             .uri("/showcases/{showcaseId}/finish", showcaseId)
                             .exchange(),
                    HttpStatus.CONFLICT,
                    "Showcase must be started first");
        }

        @Test
        @DisplayName("Finishing an already finished showcase does not fail and keeps the original finish time")
        void finishShowcase_alreadyFinished_doesNotFail() {
            val showcaseId = scheduleShowcase();
            startShowcase(showcaseId);
            finishShowcase(showcaseId);
            awaitShowcaseStatus(showcaseId, ShowcaseStatus.FINISHED);

            val before = fetchShowcase(showcaseId);
            assertThat(before.finishedAt()).isNotNull();

            finishShowcase(showcaseId);

            val after = fetchShowcase(showcaseId);
            assertThat(after.finishedAt()).isEqualTo(before.finishedAt());
        }

        @Test
        @DisplayName("Finishing with an invalid showcase ID fails with a validation problem")
        void finishShowcase_invalidShowcaseId_failsWithValidationProblem() {
            assertInvalidShowcaseIdProblem(
                    webClient.put()
                             .uri("/showcases/{showcaseId}/finish", anInvalidShowcaseId())
                             .exchange());
        }
    }

    @Nested
    @DisplayName("Removing")
    class RemovingTests {

        @Test
        @DisplayName("Removing an existing showcase hides it from fetches")
        void removeShowcase_existingShowcase_doesNotExposeRemovedShowcase() {
            val showcaseId = scheduleShowcase();
            awaitShowcaseStatus(showcaseId, ShowcaseStatus.SCHEDULED);

            removeShowcase(showcaseId);
        }

        @Test
        @DisplayName("Removing a non-existing showcase does not fail")
        void removeShowcase_nonExistingShowcase_doesNotFail() {
            val showcaseId = aShowcaseId();

            webClient.get()
                     .uri("/showcases/{showcaseId}", showcaseId)
                     .exchange()
                     .expectStatus()
                     .isNotFound();

            webClient.delete()
                     .uri("/showcases/{showcaseId}", showcaseId)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody()
                     .isEmpty();
        }
    }

    @Nested
    @DisplayName("Fetching by ID")
    class FetchingByIdTests {

        @Test
        @DisplayName("An existing showcase is returned with all fields")
        void fetchById_existingShowcase_returnsShowcase() {
            val title = aShowcaseTitle();
            val startTime = aShowcaseStartTime(Instant.now());
            val duration = aShowcaseDuration();

            val showcaseId = scheduleShowcase(title, startTime, duration);
            awaitShowcaseStatus(showcaseId, ShowcaseStatus.SCHEDULED);

            val showcase =
                    webClient.get()
                             .uri("/showcases/{showcaseId}", showcaseId)
                             .exchange()
                             .expectStatus()
                             .isOk()
                             .expectHeader()
                             .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                             .expectBody(Showcase.class)
                             .returnResult()
                             .getResponseBody();

            assertThat(showcase).isNotNull();
            assertThat(showcase.showcaseId()).isEqualTo(showcaseId);
            assertThat(showcase.title()).isEqualTo(title);
            assertThat(showcase.startTime()).isEqualTo(startTime);
            assertThat(showcase.duration()).isEqualTo(duration);
            assertThat(showcase.status()).isEqualTo(ShowcaseStatus.SCHEDULED);
            assertShowcaseState(showcase, ShowcaseStatus.SCHEDULED);
        }

        @Test
        @DisplayName("A non-existing showcase returns a not-found problem")
        void fetchById_nonExistingShowcase_returnsNotFound() {
            assertProblemDetail(
                    webClient.get()
                             .uri("/showcases/{showcaseId}", aShowcaseId())
                             .exchange(),
                    HttpStatus.NOT_FOUND,
                    "No showcase with given ID");
        }

        @Test
        @DisplayName("An invalid showcase ID returns a validation problem")
        void fetchById_invalidShowcaseId_returnsBadRequest() {
            assertInvalidShowcaseIdProblem(
                    webClient.get()
                             .uri("/showcases/{showcaseId}", anInvalidShowcaseId())
                             .exchange());
        }
    }

    @Nested
    @DisplayName("Fetching list")
    class FetchingListTests {

        private List<String> showcaseIds;

        @BeforeEach
        void setUp() {
            await().untilAsserted(
                    () -> webClient.get()
                                   .uri("/showcases")
                                   .exchange()
                                   .expectStatus()
                                   .isOk()
                                   .expectBodyList(Showcase.class)
                                   .hasSize(0));

            showcaseIds = seedShowcases();

            await().untilAsserted(
                    () -> webClient.get()
                                   .uri("/showcases")
                                   .exchange()
                                   .expectStatus()
                                   .isOk()
                                   .expectBodyList(Showcase.class)
                                   .value(showcases ->
                                                  assertThat(showcases)
                                                          .extracting(Showcase::showcaseId)
                                                          .containsExactlyInAnyOrderElementsOf(showcaseIds)));
        }

        List<String> seedShowcases() {
            val seededIds = new ArrayList<String>();

            for (val status : ShowcaseStatus.values()) {
                val showcaseId = scheduleShowcase();
                seededIds.add(showcaseId);

                awaitShowcaseStatus(showcaseId, ShowcaseStatus.SCHEDULED);

                if (status != ShowcaseStatus.SCHEDULED) {
                    startShowcase(showcaseId);
                    awaitShowcaseStatus(showcaseId, ShowcaseStatus.STARTED);
                }
                if (status == ShowcaseStatus.FINISHED) {
                    finishShowcase(showcaseId);
                    awaitShowcaseStatus(showcaseId, ShowcaseStatus.FINISHED);
                }
            }

            return List.copyOf(seededIds);
        }

        @Test
        @DisplayName("Fetching the list without filters exposes the existing showcases")
        void fetchList_noFiltering_exposesExistingShowcases() {
            webClient.get()
                     .uri("/showcases")
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .extracting(Showcase::showcaseId)
                                            .containsExactlyInAnyOrderElementsOf(showcaseIds));
        }

        @Test
        @DisplayName("Fetching the list filtered by title exposes the matching showcase")
        void fetchList_titleToFilterBy_exposesFilteredShowcases() {
            val showcases = fetchShowcases("/showcases");

            assertThat(showcases).isNotEmpty();

            val showcase = anElementOf(showcases);

            webClient.get()
                     .uri("/showcases?title={title}", showcase.title())
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .hasSize(1)
                     .contains(showcase);
        }

        @Test
        @DisplayName("Fetching the list filtered by a single status exposes matching showcases")
        void fetchList_singleStatusToFilterBy_exposesFilteredShowcases() {
            val status = aShowcaseStatus();

            webClient.get()
                     .uri("/showcases?status={status}", status)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .isNotEmpty()
                                            .allMatch(showcase -> showcase.status() == status));
        }

        @Test
        @DisplayName("Fetching the list filtered by multiple statuses exposes matching showcases")
        void fetchList_multipleStatusesToFilterBy_exposesFilteredShowcases() {
            val status1 = aShowcaseStatus();
            val status2 = aShowcaseStatus(status1);

            webClient.get()
                     .uri("/showcases?status={status1}&status={status2}", status1, status2)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectHeader()
                     .valueEquals(HttpHeaders.CACHE_CONTROL, CACHE_CONTRTOL_VALUE)
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .isNotEmpty()
                                            .allMatch(showcase -> showcase.status() == status1
                                                                          || showcase.status() == status2));
        }

        @Test
        @DisplayName("Fetching the list with a size parameter returns at most that many showcases")
        void fetchList_withSizeParameter_returnsLimitedResults() {
            webClient.get()
                     .uri("/showcases?size={size}", 1)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .hasSize(1);
        }

        @Test
        @DisplayName("Fetching the list with an afterId cursor returns the next page")
        void fetchList_withAfterIdCursor_returnsPaginatedResults() {
            val orderedIds =
                    fetchShowcases("/showcases").stream()
                                                .map(Showcase::showcaseId)
                                                .toList();

            assertThat(orderedIds).hasSizeGreaterThanOrEqualTo(2);

            val firstPage = fetchShowcases("/showcases?size={size}", 1);
            assertThat(firstPage).hasSize(1);

            val secondPage =
                    fetchShowcases("/showcases?size={size}&afterId={afterId}", 1, firstPage.getFirst().showcaseId());

            assertThat(secondPage).hasSize(1);
            assertThat(secondPage.getFirst().showcaseId()).isEqualTo(orderedIds.get(1));
            assertThat(secondPage.getFirst().showcaseId()).isNotEqualTo(firstPage.getFirst().showcaseId());
        }
    }
}
