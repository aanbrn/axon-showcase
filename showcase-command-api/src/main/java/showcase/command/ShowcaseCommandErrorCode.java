package showcase.command;

/**
 * Error codes describing why a showcase command failed.
 */
public enum ShowcaseCommandErrorCode {
    /**
     * The command is not valid.
     */
    INVALID_COMMAND,

    /**
     * The given title is already in use.
     */
    TITLE_IN_USE,

    /**
     * The requested showcase does not exist.
     */
    NOT_FOUND,

    /**
     * The showcase is in an illegal state for the requested operation.
     */
    ILLEGAL_STATE
}
