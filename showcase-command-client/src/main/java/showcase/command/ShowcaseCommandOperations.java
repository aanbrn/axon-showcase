package showcase.command;

public interface ShowcaseCommandOperations
        extends ScheduleShowcaseUseCase,
                StartShowcaseUseCase,
                FinishShowcaseUseCase,
                RemoveShowcaseUseCase {

    String SHOWCASE_COMMAND_SERVICE = "showcase-command-service";
}
