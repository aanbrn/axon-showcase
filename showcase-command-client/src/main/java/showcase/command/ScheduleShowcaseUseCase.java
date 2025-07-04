package showcase.command;

import reactor.core.publisher.Mono;

public interface ScheduleShowcaseUseCase {

    Mono<Void> schedule(ScheduleShowcaseCommand command);
}
