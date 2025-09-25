package showcase.query;

import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import showcase.projection.ShowcaseEntity;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.test.RandomTestUtils.anElementOf;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
class ShowcaseQueryControllerIT {

    @Container
    @ServiceConnection
    static final OpenSearchContainer<?> osViews =
            new OpenSearchContainer<>("opensearchproject/opensearch:" + System.getProperty("opensearch.image.version"));

    @Autowired
    private OpenSearchTemplate openSearchTemplate;

    @Autowired
    private ShowcaseMapper showcaseMapper;

    @Autowired
    private QueryMessageRequestMapper queryMessageRequestMapper;

    @Autowired
    private WebTestClient webClient;

    private IndexOperations showcaseIndexOperations;

    @BeforeEach
    void setUp() {
        if (showcaseIndexOperations == null) {
            showcaseIndexOperations = openSearchTemplate.indexOps(ShowcaseEntity.class);
        }

        assertThat(showcaseIndexOperations.exists()).isTrue();
    }

    @AfterEach
    void tearDown() {
        assertThat(showcaseIndexOperations.delete()).isTrue();
        assertThat(showcaseIndexOperations.createWithMapping()).isTrue();
    }

    @Test
    void fetchAll_noFiltering_respondsWithAllShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases = showcases();

        openSearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseListQuery.builder().build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.stream()
                                    .sorted(comparing(Showcase::getShowcaseId).reversed())
                                    .toList());
    }

    @Test
    void fetchAll_titleToFilterBy_respondsWithMatchingShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases = showcases();
        val showcase = anElementOf(showcases);

        openSearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseListQuery
                            .builder()
                            .title(showcase.getTitle())
                            .build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(List.of(showcase));
    }

    @Test
    void fetchAll_singleStatusToFilterBy_respondsWithMatchingShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases = showcases();
        val status = aShowcaseStatus();

        openSearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseListQuery
                            .builder()
                            .status(status)
                            .build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.stream()
                                    .filter(showcase -> showcase.getStatus() == status)
                                    .sorted(comparing(Showcase::getShowcaseId).reversed())
                                    .toList());
    }

    @Test
    void fetchAll_multipleStatusesToFilterBy_respondsWithMatchingShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases = showcases();
        val status1 = aShowcaseStatus();
        val status2 = aShowcaseStatus(status1);

        openSearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseListQuery
                            .builder()
                            .status(status1)
                            .status(status2)
                            .build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.stream()
                                    .filter(showcase -> showcase.getStatus() == status1
                                                                || showcase.getStatus() == status2)
                                    .sorted(comparing(Showcase::getShowcaseId).reversed())
                                    .toList());
    }

    @Test
    void fetchAll_afterId_respondsWithSubsequentShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases =
                showcases()
                        .stream()
                        .sorted(Comparator.comparing(Showcase::getShowcaseId).reversed())
                        .toList();
        val afterIndex = RandomUtils.secure().randomInt(0, showcases.size());
        val afterId = showcases.get(afterIndex).getShowcaseId();

        openSearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseListQuery
                            .builder()
                            .afterId(afterId)
                            .build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.subList(afterIndex + 1, showcases.size()));
    }

    @Test
    void fetchAll_size_respondsWithRequestedNumberOfShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases =
                showcases()
                        .stream()
                        .sorted(Comparator.comparing(Showcase::getShowcaseId).reversed())
                        .toList();
        val size = RandomUtils.secure().randomInt(1, showcases.size());

        openSearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseListQuery
                            .builder()
                            .size(size)
                            .build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.subList(0, size));
    }

    @Test
    void fetchById_existingShowcase_respondsWithRequestedShowcase() {
        val showcase = aShowcase();

        openSearchTemplate.save(
                showcaseMapper.dtoToEntity(showcase),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcase.getShowcaseId())
                            .build();

        webClient
                .post()
                .uri("/query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Showcase.class)
                .isEqualTo(showcase);
    }

    @Test
    void fetchById_nonExistingShowcase_respondsWithNotFoundProblem() {
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(aShowcaseId())
                            .build();

        webClient
                .post()
                .uri("/query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("about:blank")
                .jsonPath("$.title").isEqualTo("Not Found")
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.instance").isEqualTo("/query");
    }
}
