package showcase.query;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import showcase.projection.ShowcaseEntity;
import showcase.projection.ShowcaseStatus;

import java.util.List;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.test.RandomTestUtils.anElementOf;

@SpringBootTest(webEnvironment = NONE)
@Testcontainers
class ShowcaseQueryClientIT {

    @SpringBootApplication
    static class TestApp {
    }

    static final Network network = Network.newNetwork();

    @Container
    @ServiceConnection
    static final ElasticsearchContainer esViews =
            new ElasticsearchContainer("elasticsearch:" + System.getProperty("elasticsearch.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-es-views"))
                    .withNetwork(network)
                    .withEnv("xpack.security.enabled", "false");

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> queryService =
            new GenericContainer<>("aanbrn/axon-showcase-query-service:" + System.getProperty("project.version"))
                    .dependsOn(esViews)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-query-service"))
                    .withNetwork(network)
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @DynamicPropertySource
    static void showcaseQueryClientProperties(DynamicPropertyRegistry registry) {
        registry.add("showcase.query.api-url", () -> "http://localhost:" + queryService.getFirstMappedPort());
    }

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ShowcaseQueryOperations showcaseQueryOperations;

    private IndexOperations showcaseIndexOperations;

    private List<Showcase> showcases;

    @BeforeEach
    void setUp() {
        if (showcaseIndexOperations == null) {
            showcaseIndexOperations = elasticsearchTemplate.indexOps(ShowcaseEntity.class);
        }
        if (!showcaseIndexOperations.exists()) {
            showcaseIndexOperations.createWithMapping();
        }

        showcases = showcases();

        elasticsearchTemplate.save(
                showcases.stream()
                         .map(showcase ->
                                      ShowcaseEntity
                                              .builder()
                                              .showcaseId(showcase.getShowcaseId())
                                              .title(showcase.getTitle())
                                              .startTime(showcase.getStartTime())
                                              .duration(showcase.getDuration())
                                              .status(ShowcaseStatus.valueOf(showcase.getStatus().name()))
                                              .startedAt(showcase.getStartedAt())
                                              .finishedAt(showcase.getFinishedAt())
                                              .scheduledAt(showcase.getScheduledAt())
                                              .build())
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();
    }

    @AfterEach
    void tearDown() {
        showcaseIndexOperations.delete();
    }

    @Test
    void fetchAll_noFiltering_emitsAllExistingShowcasesSortedByStartTime() {
        await().untilAsserted(
                () -> StepVerifier
                              .create(showcaseQueryOperations.fetchAll(
                                      FetchShowcaseListQuery
                                              .builder()
                                              .build()))
                              .expectNextSequence(
                                      showcases.stream()
                                               .sorted(comparing(Showcase::getStartTime).reversed())
                                               .toList())
                              .expectComplete()
                              .verify());
    }

    @Test
    void fetchAll_titleToFilterBy_emitsFilteredShowcasesSortedByStartTime() {
        val showcase = anElementOf(showcases);

        await().untilAsserted(
                () -> StepVerifier
                              .create(showcaseQueryOperations.fetchAll(
                                      FetchShowcaseListQuery
                                              .builder()
                                              .title(showcase.getTitle())
                                              .build()))
                              .expectNext(showcase)
                              .expectComplete()
                              .verify());
    }

    @Test
    void fetchAll_singleStatusToFilterBy_emitsFilteredShowcasesSortedByStartTime() {
        val status = aShowcaseStatus();

        await().untilAsserted(
                () -> StepVerifier
                              .create(showcaseQueryOperations.fetchAll(
                                      FetchShowcaseListQuery
                                              .builder()
                                              .status(status)
                                              .build()))
                              .expectNextSequence(
                                      showcases.stream()
                                               .filter(it -> it.getStatus() == status)
                                               .sorted(comparing(Showcase::getStartTime).reversed())
                                               .toList())
                              .expectComplete()
                              .verify());
    }

    @Test
    void fetchAll_multipleStatusesToFilterBy_emitsFilteredShowcasesSortedByStartTime() {
        val status1 = aShowcaseStatus();
        val status2 = aShowcaseStatus(status1);

        await().untilAsserted(
                () -> StepVerifier
                              .create(showcaseQueryOperations.fetchAll(
                                      FetchShowcaseListQuery
                                              .builder()
                                              .status(status1)
                                              .status(status2)
                                              .build()))
                              .expectNextSequence(
                                      showcases.stream()
                                               .filter(showcase -> showcase.getStatus() == status1
                                                                           || showcase.getStatus() == status2)
                                               .sorted(comparing(Showcase::getStartTime).reversed())
                                               .toList())
                              .expectComplete()
                              .verify());
    }

    @Test
    void fetchById_existingShowcase_emitsRequestedShowcase() {
        val showcase = anElementOf(showcases);

        await().untilAsserted(
                () -> StepVerifier
                              .create(showcaseQueryOperations.fetchById(
                                      FetchShowcaseByIdQuery
                                              .builder()
                                              .showcaseId(showcase.getShowcaseId())
                                              .build()))
                              .expectNext(showcase)
                              .expectComplete()
                              .verify());
    }

    @Test
    void fetchById_nonExistingShowcase_emitsErrorWithShowcaseQueryExceptionCausedByNotFoundError() {
        StepVerifier
                .create(showcaseQueryOperations.fetchById(
                        FetchShowcaseByIdQuery
                                .builder()
                                .showcaseId(aShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseQueryException.class)
                                     .asInstanceOf(type(ShowcaseQueryException.class))
                                     .extracting(ShowcaseQueryException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseQueryErrorDetails.class))
                                     .extracting(ShowcaseQueryErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseQueryErrorCode.class))
                                     .isEqualTo(ShowcaseQueryErrorCode.NOT_FOUND))
                .verify();
    }
}
