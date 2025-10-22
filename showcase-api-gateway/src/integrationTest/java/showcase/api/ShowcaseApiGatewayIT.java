package showcase.api;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;
import static showcase.test.RandomTestUtils.anElementOf;

@Testcontainers(parallel = true)
@Execution(ExecutionMode.SAME_THREAD)
class ShowcaseApiGatewayIT {

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

    @BeforeEach
    void setUp() {
        webClient =
                WebTestClient
                        .bindToServer()
                        .baseUrl("http://localhost:" + apiGateway.getMappedPort(8080))
                        .build();
    }

    void removeShowcase(String showcaseId) {
        webClient.delete()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk();

        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isNotFound());
    }

    @Test
    void scheduleShowcase_validRequest_exposesScheduledShowcase() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

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
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(response).isNotNull();

        val showcaseId = response.showcaseId();

        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isOk()
                               .expectBody()
                               .jsonPath("$.showcaseId").isEqualTo(showcaseId)
                               .jsonPath("$.title").isEqualTo(title)
                               .jsonPath("$.startTime").isEqualTo(startTime)
                               .jsonPath("$.duration").isEqualTo(duration)
                               .jsonPath("$.status").value(ShowcaseStatus.class, equalTo(ShowcaseStatus.SCHEDULED))
                               .jsonPath("$.scheduledAt").isNotEmpty()
                               .jsonPath("$.startedAt").isEmpty()
                               .jsonPath("$.finishedAt").isEmpty());

        removeShowcase(showcaseId);
    }

    @Test
    void scheduleShowcase_alreadyUsedTitle_failsWithTitleInUseProblem() {
        val title = aShowcaseTitle();

        val response =
                webClient.post()
                         .uri("/showcases")
                         .bodyValue(Map.of(
                                 "title", title,
                                 "startTime", aShowcaseStartTime(Instant.now()),
                                 "duration", aShowcaseDuration()))
                         .exchange()
                         .expectStatus()
                         .isCreated()
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(response).isNotNull();

        val showcaseId = response.showcaseId();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "title", title,
                         "startTime", aShowcaseStartTime(Instant.now()),
                         "duration", aShowcaseDuration()))
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HTTP_CONFLICT)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.CONFLICT.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
                 .jsonPath("$.detail").isEqualTo("Given title is in use already")
                 .jsonPath("$.instance").isEqualTo("/showcases");

        removeShowcase(showcaseId);
    }

    @Test
    void startShowcase_existingShowcase_exposesStartedShowcase() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

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
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(response).isNotNull();

        val showcaseId = response.showcaseId();

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();

        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isOk()
                               .expectBody()
                               .jsonPath("$.showcaseId").isEqualTo(showcaseId)
                               .jsonPath("$.title").isEqualTo(title)
                               .jsonPath("$.startTime").isEqualTo(startTime)
                               .jsonPath("$.duration").isEqualTo(duration)
                               .jsonPath("$.status").value(ShowcaseStatus.class, equalTo(ShowcaseStatus.STARTED))
                               .jsonPath("$.scheduledAt").isNotEmpty()
                               .jsonPath("$.startedAt").isNotEmpty()
                               .jsonPath("$.finishedAt").isEmpty());

        removeShowcase(showcaseId);
    }

    @Test
    void startShowcase_nonExistingShowcase_failsWithNotFoundProblem() {
        webClient.put()
                 .uri("/showcases/{showcaseId}/start", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isNotFound()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                 .jsonPath("detail").isEqualTo("No showcase with given ID");
    }

    @Test
    void finishShowcase_existingShowcase_exposesFinishedShowcase() {
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

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
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(response).isNotNull();

        val showcaseId = response.showcaseId();

        webClient.put()
                 .uri("/showcases/{showcaseId}/start", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk();

        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();

        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isOk()
                               .expectBody()
                               .jsonPath("$.showcaseId").isEqualTo(showcaseId)
                               .jsonPath("$.title").isEqualTo(title)
                               .jsonPath("$.startTime").isEqualTo(startTime)
                               .jsonPath("$.duration").isEqualTo(duration)
                               .jsonPath("$.status").value(ShowcaseStatus.class, equalTo(ShowcaseStatus.FINISHED))
                               .jsonPath("$.scheduledAt").isNotEmpty()
                               .jsonPath("$.startedAt").isNotEmpty()
                               .jsonPath("$.finishedAt").isNotEmpty());

        removeShowcase(showcaseId);
    }

    @Test
    void finishShowcase_nonExistingShowcase_failsWithNotFoundProblem() {
        webClient.put()
                 .uri("/showcases/{showcaseId}/finish", aShowcaseId())
                 .exchange()
                 .expectStatus()
                 .isNotFound()
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase())
                 .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                 .jsonPath("detail").isEqualTo("No showcase with given ID");
    }

    @Test
    void removeShowcase_existingShowcase_doesNotExposeRemovedShowcase() {
        val response =
                webClient.post()
                         .uri("/showcases")
                         .bodyValue(Map.of(
                                 "title", aShowcaseTitle(),
                                 "startTime", aShowcaseStartTime(Instant.now()),
                                 "duration", aShowcaseDuration()))
                         .exchange()
                         .expectStatus()
                         .isCreated()
                         .expectBody(ScheduleShowcaseResponse.class)
                         .returnResult()
                         .getResponseBody();

        assertThat(response).isNotNull();

        val showcaseId = response.showcaseId();

        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isOk());

        webClient.delete()
                 .uri("/showcases/{showcaseId}", showcaseId)
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .isEmpty();

        await().untilAsserted(
                () -> webClient.get()
                               .uri("/showcases/{showcaseId}", showcaseId)
                               .exchange()
                               .expectStatus()
                               .isNotFound());
    }

    @Test
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

    @Nested
    class FetchingTests {

        private final List<String> showcaseIds = new ArrayList<>();

        @BeforeEach
        void setUp() {
            assertThat(showcaseIds).isEmpty();

            await().untilAsserted(
                    () -> webClient.get()
                                   .uri("/showcases")
                                   .exchange()
                                   .expectStatus()
                                   .isOk()
                                   .expectBodyList(Showcase.class)
                                   .hasSize(0));

            for (val status : ShowcaseStatus.values()) {
                val title = aShowcaseTitle();
                val startTime = aShowcaseStartTime(Instant.now());
                val duration = aShowcaseDuration();

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
                                 .expectBody(ScheduleShowcaseResponse.class)
                                 .returnResult()
                                 .getResponseBody();

                assertThat(response).isNotNull();

                val showcaseId = response.showcaseId();

                showcaseIds.add(showcaseId);

                await().untilAsserted(
                        () -> webClient.get()
                                       .uri("/showcases/{showcaseId}", showcaseId)
                                       .exchange()
                                       .expectStatus()
                                       .isOk()
                                       .expectBody(Showcase.class)
                                       .value(Showcase::status, equalTo(ShowcaseStatus.SCHEDULED)));

                if (status == ShowcaseStatus.SCHEDULED) {
                    continue;
                }

                webClient.put()
                         .uri("/showcases/{showcaseId}/start", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk();

                await().untilAsserted(
                        () -> webClient.get()
                                       .uri("/showcases/{showcaseId}", showcaseId)
                                       .exchange()
                                       .expectStatus()
                                       .isOk()
                                       .expectBody(Showcase.class)
                                       .value(Showcase::status, equalTo(ShowcaseStatus.STARTED)));

                if (status == ShowcaseStatus.STARTED) {
                    continue;
                }

                webClient.put()
                         .uri("/showcases/{showcaseId}/finish", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk();

                await().untilAsserted(
                        () -> webClient.get()
                                       .uri("/showcases/{showcaseId}", showcaseId)
                                       .exchange()
                                       .expectStatus()
                                       .isOk()
                                       .expectBody(Showcase.class)
                                       .value(Showcase::status, equalTo(ShowcaseStatus.FINISHED)));
            }

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

        @AfterEach
        void tearDown() {
            for (val showcaseId : showcaseIds) {
                webClient.delete()
                         .uri("/showcases/{showcaseId}", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk();

                await().untilAsserted(
                        () -> webClient.get()
                                       .uri("/showcases/{showcaseId}", showcaseId)
                                       .exchange()
                                       .expectStatus()
                                       .isNotFound());
            }

            showcaseIds.clear();
        }

        @Test
        void fetchList_noFiltering_exposesExistingShowcases() {
            webClient.get()
                     .uri("/showcases")
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .extracting(Showcase::showcaseId)
                                            .containsExactlyInAnyOrderElementsOf(showcaseIds));
        }

        @Test
        void fetchList_titleToFilterBy_exposesFilteredShowcases() {
            val showcases =
                    webClient.get()
                             .uri("/showcases")
                             .exchange()
                             .expectStatus()
                             .isOk()
                             .expectBodyList(Showcase.class)
                             .returnResult()
                             .getResponseBody();

            assertThat(showcases).isNotNull();

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
        void fetchList_singleStatusToFilterBy_exposesFilteredShowcases() {
            val status = aShowcaseStatus();

            webClient.get()
                     .uri("/showcases?status={status}", status)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .isNotEmpty()
                                            .allMatch(showcase -> showcase.status() == status));
        }

        @Test
        void fetchList_multipleStatusesToFilterBy_exposesFilteredShowcases() {
            val status1 = aShowcaseStatus();
            val status2 = aShowcaseStatus(status1);

            webClient.get()
                     .uri("/showcases?status={status1}&status={status2}", status1, status2)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .isNotEmpty()
                                            .allMatch(showcase -> showcase.status() == status1
                                                                          || showcase.status() == status2));
        }
    }
}
