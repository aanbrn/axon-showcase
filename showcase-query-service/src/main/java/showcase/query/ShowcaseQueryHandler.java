package showcase.query;

import lombok.NonNull;
import lombok.val;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
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

    static final String SHOWCASES_CACHE = "showcases";

    private final ReactiveElasticsearchTemplate elasticsearchTemplate;

    private final ShowcaseMapper showcaseMapper;

    private final IndexCoordinates showcaseIndex;

    ShowcaseQueryHandler(
            @NonNull ReactiveElasticsearchTemplate elasticsearchTemplate,
            @NonNull ShowcaseMapper showcaseMapper) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.showcaseMapper = showcaseMapper;
        this.showcaseIndex = elasticsearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
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
        return elasticsearchTemplate
                       .search(criteriaQuery, ShowcaseEntity.class, showcaseIndex)
                       .map(SearchHit::getContent)
                       .map(showcaseMapper::entityToDto);
    }

    @QueryHandler
    @Cacheable(cacheNames = SHOWCASES_CACHE, key = "#query.showcaseId", unless = "#result == null")
    Mono<Showcase> handle(@NonNull FetchShowcaseByIdQuery query) {
        return elasticsearchTemplate
                       .get(query.getShowcaseId(), ShowcaseEntity.class, showcaseIndex)
                       .map(showcaseMapper::entityToDto);
    }
}
