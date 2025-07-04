package showcase.query;

public interface ShowcaseQueryOperations
        extends FetchShowcaseListUseCase,
                FetchShowcaseByIdUseCase {

    String SHOWCASE_QUERY_SERVICE = "showcase-query-service";
}
