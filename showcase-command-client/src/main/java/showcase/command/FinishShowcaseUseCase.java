package showcase.command;

import reactor.core.publisher.Mono;

/**
 * Use case for finishing a started showcase.
 */
public interface FinishShowcaseUseCase {
    /**
     * Finishes a started showcase.
     *
     * @param command the finish command to send
     * @return a mono completing when the showcase is finished
     */
    Mono<Void> finish(FinishShowcaseCommand command);
}
