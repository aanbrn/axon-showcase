package showcase.command;

/**
 * Aggregates all showcase use cases and exposes the command service name.
 */
public interface ShowcaseCommandOperations
        extends ScheduleShowcaseUseCase,
                StartShowcaseUseCase,
                FinishShowcaseUseCase,
                RemoveShowcaseUseCase {
    /**
     * The name of the showcase command service, used for Resilience4j configuration.
     */
    String SHOWCASE_COMMAND_SERVICE = "showcase-command-service";
}
