package showcase.query;

/**
 * Error codes describing why a showcase query failed.
 */
public enum ShowcaseQueryErrorCode {
    /**
     * The query is not valid.
     */
    INVALID_QUERY,

    /**
     * The requested showcase does not exist.
     */
    NOT_FOUND
}
