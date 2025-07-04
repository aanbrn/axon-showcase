package showcase.command;

import reactor.core.publisher.Mono;

public interface RemoveShowcaseUseCase {

    Mono<Void> remove(RemoveShowcaseCommand command);
}
