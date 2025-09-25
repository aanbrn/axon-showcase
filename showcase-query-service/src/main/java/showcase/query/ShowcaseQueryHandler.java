package showcase.query;

import lombok.NonNull;
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

    ShowcaseQueryHandler(
            @NonNull ReactiveOpenSearchTemplate openSearchTemplate,
            @NonNull ShowcaseMapper showcaseMapper) {
        this.openSearchTemplate = openSearchTemplate;
        this.showcaseMapper = showcaseMapper;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
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
                        .withSort(Sort.by(Direction.DESC, "showcaseId"))
                        .withSearchAfter(
                                Optional.<Object>ofNullable(query.getAfterId())
                                        .map(List::of)
                                        .orElse(null))
                        .withMaxResults(query.getSize())
                        .build();
        return openSearchTemplate
                       .search(criteriaQuery, ShowcaseEntity.class, showcaseIndex)
                       .map(SearchHit::getContent)
                       .map(showcaseMapper::entityToDto);
    }

    @QueryHandler
    Mono<Showcase> handle(@NonNull FetchShowcaseByIdQuery query) throws ShowcaseQueryException {
        return openSearchTemplate
                       .get(query.getShowcaseId(), ShowcaseEntity.class, showcaseIndex)
                       .map(showcaseMapper::entityToDto)
                       .switchIfEmpty(Mono.error(
                               () -> new ShowcaseQueryException(
                                       ShowcaseQueryErrorDetails
                                               .builder()
                                               .errorCode(ShowcaseQueryErrorCode.NOT_FOUND)
                                               .errorMessage("No showcase with given ID")
                                               .build())));
    }
}
