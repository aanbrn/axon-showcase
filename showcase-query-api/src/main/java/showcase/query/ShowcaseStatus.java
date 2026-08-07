package showcase.query;

/**
 * The lifecycle status of a showcase.
 */
public enum ShowcaseStatus {
    /**
     * The showcase is scheduled, but not started yet.
     */
    SCHEDULED,

    /**
     * The showcase has been started and is running.
     */
    STARTED,

    /**
     * The showcase has been finished.
     */
    FINISHED
}
