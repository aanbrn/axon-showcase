// SPDX-License-Identifier: MIT
package showcase.api;

import lombok.experimental.UtilityClass;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;

/**
 * Holds the cache configuration keys used by the showcase API gateway.
 */
@UtilityClass
public class ShowcaseApiConstants {
    /**
     * The configuration key for the cache backing {@link FetchShowcaseListQuery} queries.
     */
    public static final String FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME = FetchShowcaseListQuery.class.getSimpleName();

    /**
     * The configuration key for the cache backing {@link FetchShowcaseByIdQuery} queries.
     */
    public static final String FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME = FetchShowcaseByIdQuery.class.getSimpleName();
}
