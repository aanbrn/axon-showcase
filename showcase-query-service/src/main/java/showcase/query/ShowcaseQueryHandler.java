package showcase.query;

import io.micrometer.observation.ObservationRegistry;
import lombok.val;
import org.axonframework.queryhandling.QueryHandler;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import reactor.core.observability.SignalListenerFactory;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import showcase.projection.ShowcaseEntity;

import java.util.List;
import java.util.Optional;

@Component
class ShowcaseQueryHandler {

    private final ReactiveOpenSearchTemplate openSearchTemplate;

    private final ShowcaseMapper showcaseMapper;

    private final IndexCoordinates showcaseIndex;

    private final SignalListenerFactory<Showcase, ?> observationListenerFactory;

    ShowcaseQueryHandler(
            ReactiveOpenSearchTemplate openSearchTemplate,
            ShowcaseMapper showcaseMapper,
            ObservationRegistry observationRegistry) {
        this.openSearchTemplate = openSearchTemplate;
        this.showcaseMapper = showcaseMapper;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
        this.observationListenerFactory = Micrometer.observation(observationRegistry);
    }

    @QueryHandler
    Flux<Showcase> handle(FetchShowcaseListQuery query) {
        var criteria = new Criteria();
        val title = query.title();
        if (title != null) {
            criteria = criteria.and("title").matches(title);
        }
        val statuses = query.statuses();
        criteria = switch (statuses.size()) {
            case 0 -> criteria;
            case 1 -> criteria.and("status").is(statuses.iterator().next());
            default -> criteria.and("status").in(statuses);
        };
        val criteriaQuery =
                CriteriaQuery
                        .builder(criteria)
                        .withSort(Sort.by(Direction.DESC, "showcaseId"))
                        .withSearchAfter(
                                Optional.<Object>ofNullable(query.afterId())
                                        .map(List::of)
                                        .orElse(null))
                        .withMaxResults(query.size())
                        .withRequestCache(true)
                        .build();
        return openSearchTemplate
                       .search(criteriaQuery, ShowcaseEntity.class, showcaseIndex)
                       .name("fetch-showcase-list")
                       .map(SearchHit::getContent)
                       .map(showcaseMapper::entityToDto)
                       .tap(observationListenerFactory)
                       .checkpoint("ShowcaseQueryHandler.handle(%s)".formatted(query));
    }

    @QueryHandler
    Mono<Showcase> handle(FetchShowcaseByIdQuery query) throws ShowcaseQueryException {
        return openSearchTemplate
                       .get(query.showcaseId(), ShowcaseEntity.class, showcaseIndex)
                       .name("fetch-showcase-by-id")
                       .map(showcaseMapper::entityToDto)
                       .tap(observationListenerFactory)
                       .switchIfEmpty(Mono.error(
                               () -> new ShowcaseQueryException(
                                       ShowcaseQueryErrorDetails
                                               .builder()
                                               .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                                               .errorMessage("No showcase with given ID")
                                               .build())))
                       .checkpoint("ShowcaseQueryHandler.handle(%s)".formatted(query));
    }
}
