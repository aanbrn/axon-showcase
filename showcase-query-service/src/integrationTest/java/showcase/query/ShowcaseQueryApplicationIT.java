package showcase.query;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import showcase.projection.ShowcaseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Showcase query application integration tests")
class ShowcaseQueryApplicationIT {

    @Container
    @ServiceConnection
    static final OpenSearchContainer<?> osViews =
            new OpenSearchContainer<>("opensearchproject/opensearch:" + System.getProperty("opensearch.image.version"));

    @Autowired
    private OpenSearchTemplate openSearchTemplate;

    @Autowired
    private WebTestClient webClient;

    @Test
    @DisplayName("The health endpoint reports UP when OpenSearch is healthy")
    void healthEndpoint_reportsUp() {
        webClient.get()
                 .uri("/actuator/health")
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("The health endpoint reports the OpenSearch health component with its cluster details")
    void healthEndpoint_reportsOpenSearchHealthComponentWithDetails() {
        webClient.get()
                 .uri("/actuator/health")
                 .exchange()
                 .expectStatus()
                 .isOk()
                 .expectBody()
                 .jsonPath("$.components.openSearch.status").isEqualTo("UP")
                 .jsonPath("$.components.openSearch.details.cluster_name").isNotEmpty()
                 .jsonPath("$.components.openSearch.details.number_of_nodes").isNotEmpty();
    }

    @Test
    @DisplayName("The index initializer created the showcase index with the expected name")
    void indexInitializer_createdShowcaseIndexWithExpectedName() {
        val indexOperations = openSearchTemplate.indexOps(ShowcaseEntity.class);
        assertThat(indexOperations.exists()).isTrue();
        assertThat(indexOperations.getIndexCoordinates().getIndexName()).isEqualTo("showcases");
    }

    @Nested
    @TestPropertySource(properties = "showcase.query.index-initialization-enabled=false")
    @DisplayName("When index initialization is disabled")
    class IndexInitializationDisabled {

        @Autowired(required = false)
        @Qualifier("opensearchIndexInitializer")
        private InitializingBean opensearchIndexInitializer;

        @Test
        @DisplayName("The index initializer bean is not created")
        void indexInitializerBean_isNotCreated() {
            assertThat(opensearchIndexInitializer).isNull();
        }
    }

    @Nested
    @TestPropertySource(properties = "showcase.query.exit-after-index-initialization=true")
    @DisplayName("When exit after index initialization is enabled")
    class ExitAfterIndexInitialization {

        @MockitoBean
        private ShowcaseQueryApplication.ApplicationExitHandler exitHandler;

        @Test
        @DisplayName("The exit handler is invoked after the index is initialized")
        void exitHandler_isInvoked() {
            verify(exitHandler).exit(any(org.springframework.context.ApplicationContext.class));
        }
    }
}
