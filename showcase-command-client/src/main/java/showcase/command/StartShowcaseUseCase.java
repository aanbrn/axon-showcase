package showcase.command;

import reactor.core.publisher.Mono;

public interface StartShowcaseUseCase {

    Mono<Void> start(StartShowcaseCommand command);
}
