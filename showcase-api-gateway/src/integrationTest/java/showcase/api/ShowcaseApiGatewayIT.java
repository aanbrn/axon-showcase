package showcase.api;

import com.redis.testcontainers.RedisContainer;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import showcase.query.Showcase;
import showcase.query.ShowcaseStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
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
    static final PostgreSQLContainer<?> dbSagas =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-db-sagas"))
                    .withNetwork(network)
                    .withDatabaseName("showcase-sagas");

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
    static final ElasticsearchContainer esViews =
            new ElasticsearchContainer("elasticsearch:" + System.getProperty("elasticsearch.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-es-views"))
                    .withNetwork(network)
                    .withEnv("xpack.security.enabled", "false");

    static final RedisContainer redis =
            new RedisContainer("redis:" + System.getProperty("redis.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-redis"))
                    .withNetwork(network);

    @Container
    @SuppressWarnings({ "resource", "unused" })
    static final GenericContainer<?> commandService =
            new GenericContainer<>("aanbrn/axon-showcase-command-service:" + System.getProperty("project.version"))
                    .dependsOn(dbEvents, kafka)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-command-service"))
                    .withNetwork(network)
                    .withEnv("DB_USER", dbEvents.getUsername())
                    .withEnv("DB_PASSWORD", dbEvents.getPassword())
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings({ "resource", "unused" })
    static final GenericContainer<?> sagaService =
            new GenericContainer<>("aanbrn/axon-showcase-saga-service:" + System.getProperty("project.version"))
                    .dependsOn(dbSagas, kafka)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-saga-service"))
                    .withNetwork(network)
                    .withEnv("DB_USER", dbSagas.getUsername())
                    .withEnv("DB_PASSWORD", dbSagas.getPassword())
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings({ "resource", "unused" })
    static final GenericContainer<?> projectionService =
            new GenericContainer<>("aanbrn/axon-showcase-projection-service:" + System.getProperty("project.version"))
                    .dependsOn(kafka, esViews, redis)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-projection-service"))
                    .withNetwork(network)
                    .withEnv("DB_USER", dbEvents.getUsername())
                    .withEnv("DB_PASSWORD", dbEvents.getPassword())
                    .withEnv("REDIS_CLUSTER_DYNAMIC_REFRESH_SOURCES", "off")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings({ "resource", "unused" })
    static final GenericContainer<?> queryService =
            new GenericContainer<>("aanbrn/axon-showcase-query-service:" + System.getProperty("project.version"))
                    .dependsOn(esViews, redis)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-query-service"))
                    .withNetwork(network)
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @Container
    @SuppressWarnings({ "resource", "unused" })
    static final GenericContainer<?> apiGateway =
            new GenericContainer<>("aanbrn/axon-showcase-api-gateway:" + System.getProperty("project.version"))
                    .dependsOn(commandService, queryService)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-api-gateway"))
                    .withNetwork(network)
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

    @AfterEach
    void tearDown() {
        while (true) {
            val showcases =
                    webClient.get()
                             .uri("/showcases")
                             .exchange()
                             .expectStatus()
                             .isOk()
                             .expectBodyList(Showcase.class)
                             .returnResult()
                             .getResponseBody();
            if (showcases == null || showcases.isEmpty()) {
                break;
            }

            for (val showcase : showcases) {
                webClient.delete()
                         .uri("/showcases/{showcaseId}", showcase.getShowcaseId())
                         .exchange()
                         .expectStatus()
                         .isOk();
            }
        }
    }

    @Test
    void scheduleShowcase_validRequest_exposesScheduledShowcase() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", title,
                         "startTime", startTime,
                         "duration", duration))
                 .exchange()
                 .expectStatus()
                 .isCreated()
                 .expectHeader()
                 .value("Location", equalTo("/showcases/" + showcaseId));

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
    }

    @Test
    void scheduleShowcase_duplicateSchedule_exposesScheduledShowcase() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", title,
                         "startTime", startTime,
                         "duration", duration))
                 .exchange()
                 .expectStatus()
                 .isCreated()
                 .expectHeader()
                 .value("Location", equalTo("/showcases/" + showcaseId));

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

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", title,
                         "startTime", startTime,
                         "duration", duration))
                 .exchange()
                 .expectStatus()
                 .isCreated()
                 .expectHeader()
                 .value("Location", equalTo("/showcases/" + showcaseId));
    }

    @Test
    void scheduleShowcase_reschedule_failsWithTitleInUseProblem() {
        val showcaseId = aShowcaseId();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", aShowcaseTitle(),
                         "startTime", aShowcaseStartTime(Instant.now()),
                         "duration", aShowcaseDuration()))
                 .exchange()
                 .expectStatus()
                 .isCreated();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", aShowcaseTitle(),
                         "startTime", aShowcaseStartTime(Instant.now()),
                         "duration", aShowcaseDuration()))
                 .exchange()
                 .expectStatus()
                 .isEqualTo(HTTP_CONFLICT)
                 .expectHeader()
                 .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                 .expectBody()
                 .jsonPath("$.type").isEqualTo("about:blank")
                 .jsonPath("$.title").isEqualTo("Conflict")
                 .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
                 .jsonPath("$.detail").isEqualTo("Showcase cannot be rescheduled")
                 .jsonPath("$.instance").isEqualTo("/showcases");
    }

    @Test
    void scheduleShowcase_alreadyUsedTitle_failsWithTitleInUseProblem() {
        val title = aShowcaseTitle();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", aShowcaseId(),
                         "title", title,
                         "startTime", aShowcaseStartTime(Instant.now()),
                         "duration", aShowcaseDuration()))
                 .exchange()
                 .expectStatus()
                 .isCreated();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", aShowcaseId(),
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
                 .jsonPath("$.title").isEqualTo("Conflict")
                 .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
                 .jsonPath("$.detail").isEqualTo("Given title is in use already")
                 .jsonPath("$.instance").isEqualTo("/showcases");
    }

    @Test
    void startShowcase_existingShowcase_exposesStartedShowcase() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", title,
                         "startTime", startTime,
                         "duration", duration))
                 .exchange()
                 .expectStatus()
                 .isCreated();

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
                 .jsonPath("$.title").isEqualTo("Not Found")
                 .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                 .jsonPath("detail").isEqualTo("No showcase with given ID");
    }

    @Test
    void finishShowcase_existingShowcase_exposesFinishedShowcase() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", title,
                         "startTime", startTime,
                         "duration", duration))
                 .exchange()
                 .expectStatus()
                 .isCreated();

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
                 .jsonPath("$.title").isEqualTo("Not Found")
                 .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                 .jsonPath("detail").isEqualTo("No showcase with given ID");
    }

    @Test
    void removeShowcase_existingShowcase_doesNotExposeRemovedShowcase() {
        val showcaseId = aShowcaseId();

        webClient.post()
                 .uri("/showcases")
                 .bodyValue(Map.of(
                         "showcaseId", showcaseId,
                         "title", aShowcaseTitle(),
                         "startTime", aShowcaseStartTime(Instant.now()),
                         "duration", aShowcaseDuration()))
                 .exchange()
                 .expectStatus()
                 .isCreated();

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

        @BeforeEach
        void setUp() {
            webClient.get()
                     .uri("/showcases")
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .hasSize(0);

            val statuses = List.of(ShowcaseStatus.values());

            for (val status : statuses) {
                val showcaseId = aShowcaseId();
                val title = aShowcaseTitle();
                val startTime = aShowcaseStartTime(Instant.now());
                val duration = aShowcaseDuration();

                webClient.post()
                         .uri("/showcases")
                         .bodyValue(Map.of(
                                 "showcaseId", showcaseId,
                                 "title", title,
                                 "startTime", startTime,
                                 "duration", duration))
                         .exchange()
                         .expectStatus()
                         .isCreated();

                if (status == ShowcaseStatus.SCHEDULED) {
                    continue;
                }

                webClient.put()
                         .uri("/showcases/{showcaseId}/start", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk();

                if (status == ShowcaseStatus.STARTED) {
                    continue;
                }

                webClient.put()
                         .uri("/showcases/{showcaseId}/finish", showcaseId)
                         .exchange()
                         .expectStatus()
                         .isOk();
            }

            await().untilAsserted(
                    () -> webClient.get()
                                   .uri("/showcases")
                                   .exchange()
                                   .expectStatus()
                                   .isOk()
                                   .expectBodyList(Showcase.class)
                                   .value(showcases -> assertThat(showcases)
                                                               .extracting(Showcase::getStatus)
                                                               .containsAll(statuses)));
        }

        @Test
        void fetchAll_noFiltering_exposesExistingShowcases() {
            webClient.get()
                     .uri("/showcases")
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .value(showcases ->
                                    assertThat(showcases)
                                            .isNotEmpty()
                                            .extracting(Showcase::getStatus)
                                            .containsAll(List.of(ShowcaseStatus.values())));
        }

        @Test
        void fetchAll_titleToFilterBy_exposesFilteredShowcases() {
            val showcases =
                    webClient.get()
                             .uri("/showcases")
                             .exchange()
                             .expectStatus()
                             .isOk()
                             .expectBodyList(Showcase.class)
                             .returnResult()
                             .getResponseBody();
            val showcase = anElementOf(requireNonNull(showcases));

            webClient.get()
                     .uri("/showcases?title={title}", showcase.getTitle())
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBodyList(Showcase.class)
                     .isEqualTo(List.of(showcase));
        }

        @Test
        void fetchAll_singleStatusToFilterBy_exposesFilteredShowcases() {
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
                                            .allMatch(showcase -> showcase.getStatus() == status));
        }

        @Test
        void fetchAll_multipleStatusesToFilterBy_exposesFilteredShowcases() {
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
                                            .allMatch(showcase -> showcase.getStatus() == status1
                                                                          || showcase.getStatus() == status2));
        }
    }
}
