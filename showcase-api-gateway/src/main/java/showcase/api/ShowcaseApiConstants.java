package showcase.api;

import lombok.experimental.UtilityClass;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;

@UtilityClass
class ShowcaseApiConstants {

    static final String FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME = FetchShowcaseListQuery.class.getSimpleName();

    static final String FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME = FetchShowcaseByIdQuery.class.getSimpleName();
}
