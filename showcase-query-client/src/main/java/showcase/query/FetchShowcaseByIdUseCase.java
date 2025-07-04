package showcase.query;

import reactor.core.publisher.Mono;

public interface FetchShowcaseByIdUseCase {

    Mono<Showcase> fetchById(FetchShowcaseByIdQuery query);
}
