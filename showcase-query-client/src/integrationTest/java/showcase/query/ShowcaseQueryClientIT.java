package showcase.query;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
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
    @SuppressWarnings("resource")
    static final OpenSearchContainer<?> osViews =
            new OpenSearchContainer<>("opensearchproject/opensearch:" + System.getProperty("opensearch.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-os-views"))
                    .withNetwork(network);

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> queryService =
            new GenericContainer<>("aanbrn/axon-showcase-query-service:" + System.getProperty("project.version"))
                    .dependsOn(osViews)
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
    private OpenSearchTemplate openSearchTemplate;

    @Autowired
    private ShowcaseQueryOperations showcaseQueryOperations;

    private IndexOperations showcaseIndexOperations;

    private List<Showcase> showcases;

    @BeforeEach
    void setUp() {
        if (showcaseIndexOperations == null) {
            showcaseIndexOperations = openSearchTemplate.indexOps(ShowcaseEntity.class);
        }
        if (!showcaseIndexOperations.exists()) {
            showcaseIndexOperations.createWithMapping();
        }

        showcases = showcases();

        openSearchTemplate.save(
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
    void fetchList_noFiltering_succeedsWithListExistingShowcasesSortedByStartTime() {
        // given:
        val expected =
                this.showcases
                        .stream()
                        .sorted(comparing(Showcase::getShowcaseId).reversed())
                        .toList();
        val query =
                FetchShowcaseListQuery
                        .builder()
                        .build();

        await().untilAsserted(() -> {
            // when:
            val fetchListMono = showcaseQueryOperations.fetchList(query);

            // then:
            StepVerifier
                    .create(fetchListMono)
                    .expectNextSequence(expected)
                    .expectComplete()
                    .verify();
        });
    }

    @Test
    void fetchList_titleToFilterBy_succeedsWithMatchingShowcasesSortedByStartTime() {
        // given:
        val expected = anElementOf(showcases);
        val query =
                FetchShowcaseListQuery
                        .builder()
                        .title(expected.getTitle())
                        .build();

        await().untilAsserted(() -> {
            // when:
            val fetchListMono = showcaseQueryOperations.fetchList(query);

            // then:
            StepVerifier
                    .create(fetchListMono)
                    .expectNext(expected)
                    .expectComplete()
                    .verify();
        });
    }

    @Test
    void fetchList_singleStatusToFilterBy_succeedsWithMatchingShowcasesSortedByStartTime() {
        // given:
        val status = aShowcaseStatus();
        val expected =
                showcases
                        .stream()
                        .filter(it -> it.getStatus() == status)
                        .sorted(comparing(Showcase::getShowcaseId).reversed())
                        .toList();
        val query =
                FetchShowcaseListQuery
                        .builder()
                        .status(status)
                        .build();

        await().untilAsserted(() -> {
            // when:
            val fetchListMono = showcaseQueryOperations.fetchList(query);

            // then:
            StepVerifier
                    .create(fetchListMono)
                    .expectNextSequence(expected)
                    .expectComplete()
                    .verify();
        });
    }

    @Test
    void fetchList_multipleStatusesToFilterBy_succeedsWithMatchingShowcasesSortedByStartTime() {
        // given:
        val status1 = aShowcaseStatus();
        val status2 = aShowcaseStatus(status1);
        val query =
                FetchShowcaseListQuery
                        .builder()
                        .status(status1)
                        .status(status2)
                        .build();
        val expected =
                showcases
                        .stream()
                        .filter(showcase -> showcase.getStatus() == status1 || showcase.getStatus() == status2)
                        .sorted(comparing(Showcase::getShowcaseId).reversed())
                        .toList();

        await().untilAsserted(() -> {
            // when:
            val fetchListMono = showcaseQueryOperations.fetchList(query);

            // then:
            StepVerifier
                    .create(fetchListMono)
                    .expectNextSequence(expected)
                    .expectComplete()
                    .verify();
        });
    }

    @Test
    void fetchById_existingShowcase_succeedWithRequestedShowcase() {
        // given:
        val expected = anElementOf(showcases);
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(expected.getShowcaseId())
                        .build();

        await().untilAsserted(() -> {
            // when:
            val fetchByIdMono = showcaseQueryOperations.fetchById(query);

            // then:
            StepVerifier
                    .create(fetchByIdMono)
                    .expectNext(expected)
                    .expectComplete()
                    .verify();
        });
    }

    @Test
    void fetchById_nonExistingShowcase_failsWithNotFoundError() {
        // given:
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(aShowcaseId())
                        .build();

        // when:
        val fetchByIdMono = showcaseQueryOperations.fetchById(query);

        // then:
        StepVerifier
                .create(fetchByIdMono)
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseQueryException.class)
                                     .asInstanceOf(type(ShowcaseQueryException.class))
                                     .extracting(ShowcaseQueryException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseQueryErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseQueryErrorCode.NOT_FOUND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("No showcase with given ID");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }
}
