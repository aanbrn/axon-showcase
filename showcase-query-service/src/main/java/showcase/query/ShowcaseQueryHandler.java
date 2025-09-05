package showcase.query;

import lombok.NonNull;
import lombok.val;
import org.axonframework.queryhandling.QueryHandler;
import org.opensearch.data.client.osc.ReactiveOpenSearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import showcase.projection.ShowcaseEntity;

@Component
class ShowcaseQueryHandler {

    private final ReactiveOpenSearchTemplate reactiveOpenSearchTemplate;

    private final ShowcaseMapper showcaseMapper;

    private final IndexCoordinates showcaseIndex;

    ShowcaseQueryHandler(
            @NonNull ReactiveOpenSearchTemplate reactiveOpenSearchTemplate,
            @NonNull ShowcaseMapper showcaseMapper) {
        this.reactiveOpenSearchTemplate = reactiveOpenSearchTemplate;
        this.showcaseMapper = showcaseMapper;
        this.showcaseIndex = reactiveOpenSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
    }

    @QueryHandler
    Flux<Showcase> handle(@NonNull FetchShowcaseListQuery query) {
        var criteria = new Criteria();
        val title = query.getTitle();
        if (title != null) {
            criteria = criteria.and("title").matches(title);
        }
        val statuses = query.getStatuses();
        criteria = switch (statuses.size()) {
            case 0 -> criteria;
            case 1 -> criteria.and("status").is(statuses.iterator().next());
            default -> criteria.and("status").in(statuses);
        };
        val criteriaQuery =
                CriteriaQuery
                        .builder(criteria)
                        .withPageable(query.getPageRequest().toPageable())
                        .build();
        return reactiveOpenSearchTemplate
                       .search(criteriaQuery, ShowcaseEntity.class, showcaseIndex)
                       .map(SearchHit::getContent)
                       .map(showcaseMapper::entityToDto);
    }

    @QueryHandler
    Mono<Showcase> handle(@NonNull FetchShowcaseByIdQuery query) {
        return reactiveOpenSearchTemplate
                       .get(query.getShowcaseId(), ShowcaseEntity.class, showcaseIndex)
                       .map(showcaseMapper::entityToDto);
    }
}
