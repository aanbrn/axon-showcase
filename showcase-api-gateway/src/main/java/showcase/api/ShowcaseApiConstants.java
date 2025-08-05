package showcase.api;

import lombok.experimental.UtilityClass;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;

@UtilityClass
class ShowcaseApiConstants {

    static final String FETCH_ALL_CACHE_NAME = FetchShowcaseListQuery.class.getSimpleName();

    static final String FETCH_BY_ID_CACHE_NAME = FetchShowcaseByIdQuery.class.getSimpleName();
}
