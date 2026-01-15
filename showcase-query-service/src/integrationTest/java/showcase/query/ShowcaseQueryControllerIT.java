package showcase.query;

import lombok.val;
import org.apache.commons.lang3.RandomUtils;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import reactor.blockhound.BlockHound;
import showcase.projection.ShowcaseEntity;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROTOBUF;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.query.RandomQueryTestUtils.aShowcase;
import static showcase.query.RandomQueryTestUtils.aShowcaseStatus;
import static showcase.query.RandomQueryTestUtils.showcases;
import static showcase.test.RandomTestUtils.anAlphabeticString;
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

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

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
    void fetchList_noFiltering_respondsWithAllShowcasesSortedByShowcaseIdInReverseOrder() {
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
                                    .sorted(comparing(Showcase::showcaseId).reversed())
                                    .toList());
    }

    @Test
    void fetchList_titleToFilterBy_respondsWithMatchingShowcasesSortedByShowcaseIdInReverseOrder() {
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
                            .title(showcase.title())
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
    void fetchList_singleStatusToFilterBy_respondsWithMatchingShowcasesSortedByShowcaseIdInReverseOrder() {
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
                                    .filter(showcase -> showcase.status() == status)
                                    .sorted(comparing(Showcase::showcaseId).reversed())
                                    .toList());
    }

    @Test
    void fetchList_multipleStatusesToFilterBy_respondsWithMatchingShowcasesSortedByShowcaseIdInReverseOrder() {
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
                                    .filter(showcase -> showcase.status() == status1 || showcase.status() == status2)
                                    .sorted(comparing(Showcase::showcaseId).reversed())
                                    .toList());
    }

    @Test
    void fetchList_afterId_respondsWithSubsequentShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases =
                showcases()
                        .stream()
                        .sorted(Comparator.comparing(Showcase::showcaseId).reversed())
                        .toList();
        val afterIndex = RandomUtils.secure().randomInt(0, showcases.size());
        val afterId = showcases.get(afterIndex).showcaseId();

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
    void fetchList_size_respondsWithRequestedNumberOfShowcasesSortedByShowcaseIdInReverseOrder() {
        val showcases =
                showcases()
                        .stream()
                        .sorted(Comparator.comparing(Showcase::showcaseId).reversed())
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
    void fetchList_invalidQuery_respondsWithBadRequestStatusAndProblemInBody() {
        val query = FetchShowcaseListQuery
                            .builder()
                            .afterId(anAlphabeticString(10))
                            .size(0)
                            .build();

        webClient
                .post()
                .uri("/streaming-query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("about:blank")
                .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail").isEqualTo("Given query is not valid")
                .jsonPath("$.fieldErrors").isMap()
                .jsonPath("$.fieldErrors.afterId").isArray()
                .jsonPath("$.fieldErrors.afterId[0]").isNotEmpty()
                .jsonPath("$.fieldErrors.afterId[1]").doesNotHaveJsonPath()
                .jsonPath("$.fieldErrors.size").isArray()
                .jsonPath("$.fieldErrors.size[0]").isNotEmpty()
                .jsonPath("$.fieldErrors.size[1]").doesNotHaveJsonPath();
    }

    @Test
    void fetchById_existingShowcase_respondsWithRequestedShowcase() {
        val showcase = aShowcase();

        openSearchTemplate.save(
                requireNonNull(showcaseMapper.dtoToEntity(showcase)),
                showcaseIndexOperations.getIndexCoordinates());

        showcaseIndexOperations.refresh();

        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(showcase.showcaseId())
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
                .jsonPath("$.title").isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase())
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.instance").isEqualTo("/query");
    }

    @Test
    void fetchById_invalidQuery_respondsWithBadRequestStatusAndProblemInBody() {
        val query = FetchShowcaseByIdQuery
                            .builder()
                            .showcaseId(anAlphabeticString(10))
                            .build();

        webClient
                .post()
                .uri("/query")
                .contentType(APPLICATION_PROTOBUF)
                .bodyValue(queryMessageRequestMapper.messageToRequest(
                        new GenericStreamingQueryMessage<>(query, Showcase.class)))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("about:blank")
                .jsonPath("$.title").isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.detail").isEqualTo("Given query is not valid")
                .jsonPath("$.fieldErrors").isMap()
                .jsonPath("$.fieldErrors.showcaseId").isArray()
                .jsonPath("$.fieldErrors.showcaseId[0]").isNotEmpty()
                .jsonPath("$.fieldErrors.showcaseId[1]").doesNotHaveJsonPath();
    }
}
