package showcase.command;

import reactor.core.publisher.Mono;

public interface FinishShowcaseUseCase {

    Mono<Void> finish(FinishShowcaseCommand command);
}
