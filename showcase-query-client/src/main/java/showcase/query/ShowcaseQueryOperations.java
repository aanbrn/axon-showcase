package showcase.query;

/**
 * Aggregates all showcase query use cases and exposes the query service name.
 */
public interface ShowcaseQueryOperations
        extends FetchShowcaseListUseCase,
                FetchShowcaseByIdUseCase {
    /**
     * The name of the showcase query service, used for Resilience4j configuration.
     */
    String SHOWCASE_QUERY_SERVICE = "showcase-query-service";
}
