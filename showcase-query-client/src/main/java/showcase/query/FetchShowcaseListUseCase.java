// SPDX-License-Identifier: MIT
package showcase.query;

import reactor.core.publisher.Flux;

/**
 * Use case for fetching a filtered list of showcases.
 */
public interface FetchShowcaseListUseCase {
    /**
     * Fetches showcases matching the given query.
     *
     * @param query the list query to send
     * @return a flux of matching showcases
     */
    Flux<Showcase> fetchList(FetchShowcaseListQuery query);
}
