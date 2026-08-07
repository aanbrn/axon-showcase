package showcase.query;

import reactor.core.publisher.Mono;

/**
 * Use case for fetching a single showcase by its ID.
 */
public interface FetchShowcaseByIdUseCase {
    /**
     * Fetches the showcase with the ID from the given query.
     *
     * @param query the by-ID query to send
     * @return a mono of the matching showcase
     */
    Mono<Showcase> fetchById(FetchShowcaseByIdQuery query);
}
