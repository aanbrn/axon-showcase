// SPDX-License-Identifier: MIT
package showcase.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import showcase.projection.ShowcaseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("Showcase query handler component tests")
class ShowcaseQueryHandlerCT {

    private static final String SHOWCASE_ID = "33gkCN0UNn3Kzr3x7iuDaVT6sZi";

    private static final IndexCoordinates SHOWCASE_INDEX = IndexCoordinates.of("showcases");

    @Mock
    private ReactiveOpenSearchTemplate openSearchTemplate;

    private ShowcaseQueryHandler handler;

    @BeforeEach
    void setUp() {
        when(openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class)).thenReturn(SHOWCASE_INDEX);
        handler = new ShowcaseQueryHandler(openSearchTemplate, new ShowcaseMapperImpl(), ObservationRegistry.create());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Handling the list query filters by title and status and maps entities to DTOs")
    void handle_listQuery_returnsMappedShowcases() {
        val entity = ShowcaseEntity.builder()
                .showcaseId(SHOWCASE_ID)
                .title("My Showcase")
                .startTime(java.time.Instant.parse("2026-08-01T10:00:00Z"))
                .duration(java.time.Duration.ofMinutes(5))
                .status(showcase.projection.ShowcaseStatus.SCHEDULED)
                .scheduledAt(java.time.Instant.parse("2026-08-01T09:00:00Z"))
                .build();
        val hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn(entity);
        when(openSearchTemplate.search(any(), eq(ShowcaseEntity.class), eq(SHOWCASE_INDEX)))
                .thenReturn(Flux.just(hit));

        val query = FetchShowcaseListQuery.builder()
                .title("My Showcase")
                .statuses(List.of(ShowcaseStatus.SCHEDULED))
                .size(10)
                .build();

        val result = handler.handle(query).collectList().block();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().showcaseId()).isEqualTo(SHOWCASE_ID);
        assertThat(result.getFirst().title()).isEqualTo("My Showcase");
    }

    @Test
    @DisplayName("Handling the by-ID query returns the mapped showcase when found")
    void handle_byIdQuery_found_returnsMappedShowcase() {
        val entity = ShowcaseEntity.builder()
                .showcaseId(SHOWCASE_ID)
                .title("My Showcase")
                .startTime(java.time.Instant.parse("2026-08-01T10:00:00Z"))
                .duration(java.time.Duration.ofMinutes(5))
                .status(showcase.projection.ShowcaseStatus.SCHEDULED)
                .scheduledAt(java.time.Instant.parse("2026-08-01T09:00:00Z"))
                .build();
        when(openSearchTemplate.get(eq(SHOWCASE_ID), eq(ShowcaseEntity.class), eq(SHOWCASE_INDEX)))
                .thenReturn(Mono.just(entity));

        val query = FetchShowcaseByIdQuery.builder().showcaseId(SHOWCASE_ID).build();

        val result = handler.handle(query).block();
        assertThat(result).isNotNull();

        assertThat(result.showcaseId()).isEqualTo(SHOWCASE_ID);
    }

    @Test
    @DisplayName("Handling the by-ID query errors with NOT_FOUND when no showcase exists")
    void handle_byIdQuery_missing_throwsNotFound() {
        when(openSearchTemplate.get(eq(SHOWCASE_ID), eq(ShowcaseEntity.class), eq(SHOWCASE_INDEX)))
                .thenReturn(Mono.empty());

        val query = FetchShowcaseByIdQuery.builder().showcaseId(SHOWCASE_ID).build();

        assertThatThrownBy(() -> handler.handle(query).block())
                .isInstanceOf(ShowcaseQueryException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseQueryException) it;
                    assertThat(exception.getErrorDetails().errorCode()).isEqualTo(ShowcaseQueryErrorCode.NOT_FOUND);
                });
    }
}
