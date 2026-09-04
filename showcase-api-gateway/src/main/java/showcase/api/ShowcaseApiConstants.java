// SPDX-License-Identifier: MIT
package showcase.api;

import lombok.experimental.UtilityClass;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;

/**
 * Holds the cache names used by the showcase API gateway.
 */
@UtilityClass
public class ShowcaseApiConstants {
    /**
     * The name of the cache backing {@link FetchShowcaseListQuery} queries.
     */
    public static final String FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME = FetchShowcaseListQuery.class.getSimpleName();

    /**
     * The name of the cache backing {@link FetchShowcaseByIdQuery} queries.
     */
    public static final String FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME = FetchShowcaseByIdQuery.class.getSimpleName();
}
