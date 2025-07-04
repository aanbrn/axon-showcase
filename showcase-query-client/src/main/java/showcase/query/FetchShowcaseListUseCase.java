package showcase.query;

import reactor.core.publisher.Flux;

public interface FetchShowcaseListUseCase {

    Flux<Showcase> fetchAll(FetchShowcaseListQuery query);
}
