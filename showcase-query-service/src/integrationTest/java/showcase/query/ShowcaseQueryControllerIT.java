package showcase.query;

import lombok.val;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import showcase.projection.ShowcaseEntity;

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
    static final ElasticsearchContainer esViews =
            new ElasticsearchContainer("elasticsearch:" + System.getProperty("elasticsearch.image.version"))
                    .withEnv("xpack.security.enabled", "false");

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

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
            showcaseIndexOperations = elasticsearchTemplate.indexOps(ShowcaseEntity.class);
        }

        assertThat(showcaseIndexOperations.exists()).isTrue();
    }

    @AfterEach
    void tearDown() {
        assertThat(showcaseIndexOperations.delete()).isTrue();
        assertThat(showcaseIndexOperations.createWithMapping()).isTrue();
    }

    @Test
    void fetchAll_noFiltering_respondsWithAllShowcasesSortedByStartTime() {
        val showcases = showcases();

        elasticsearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val queryMessage =
                new GenericStreamingQueryMessage<>(
                        FetchShowcaseListQuery.builder().build(),
                        Showcase.class);

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.stream()
                                    .sorted(comparing(Showcase::getStartTime).reversed())
                                    .toList());
    }

    @Test
    void fetchAll_titleToFilterBy_respondsWithMatchingShowcasesSortedByStartTime() {
        val showcases = showcases();
        val showcase = anElementOf(showcases);

        elasticsearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val queryMessage =
                new GenericStreamingQueryMessage<>(
                        FetchShowcaseListQuery
                                .builder()
                                .title(showcase.getTitle())
                                .build(),
                        Showcase.class);

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(List.of(showcase));
    }

    @Test
    void fetchAll_singleStatusToFilterBy_respondsWithMatchingShowcasesSortedByStartTime() {
        val showcases = showcases();
        val status = aShowcaseStatus();

        elasticsearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val queryMessage =
                new GenericStreamingQueryMessage<>(
                        FetchShowcaseListQuery
                                .builder()
                                .status(status)
                                .build(),
                        Showcase.class);

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.stream()
                                    .filter(showcase -> showcase.getStatus() == status)
                                    .sorted(comparing(Showcase::getStartTime).reversed())
                                    .toList());
    }

    @Test
    void fetchAll_multipleStatusesToFilterBy_respondsWithMatchingShowcasesSortedByStartTime() {
        val showcases = showcases();
        val status1 = aShowcaseStatus();
        val status2 = aShowcaseStatus(status1);

        elasticsearchTemplate.save(
                showcases.stream()
                         .map(showcaseMapper::dtoToEntity)
                         .toList(),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val queryMessage =
                new GenericStreamingQueryMessage<>(
                        FetchShowcaseListQuery
                                .builder()
                                .status(status1)
                                .status(status2)
                                .build(),
                        Showcase.class);

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Showcase.class)
                .isEqualTo(showcases.stream()
                                    .filter(showcase -> showcase.getStatus() == status1
                                                                || showcase.getStatus() == status2)
                                    .sorted(comparing(Showcase::getStartTime).reversed())
                                    .toList());
    }

    @Test
    void fetchById_existingShowcase_respondsWithRequestedShowcase() {
        val showcase = aShowcase();

        elasticsearchTemplate.save(
                showcaseMapper.dtoToEntity(showcase),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val queryMessage =
                new GenericStreamingQueryMessage<>(
                        FetchShowcaseByIdQuery
                                .builder()
                                .showcaseId(showcase.getShowcaseId())
                                .build(),
                        Showcase.class);

        webClient
                .post()
                .uri("/query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Showcase.class)
                .isEqualTo(showcase);
    }

    @Test
    void fetchById_nonExistingShowcase_respondsWithNotFoundProblem() {
        val queryMessage =
                new GenericStreamingQueryMessage<>(
                        FetchShowcaseByIdQuery
                                .builder()
                                .showcaseId(aShowcaseId())
                                .build(),
                        Showcase.class);

        webClient
                .post()
                .uri("/query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(queryMessage))
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
