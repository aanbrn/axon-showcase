// SPDX-License-Identifier: MIT
package showcase.command;

import reactor.core.publisher.Mono;

/**
 * Use case for starting a scheduled showcase.
 */
public interface StartShowcaseUseCase {
    /**
     * Starts a scheduled showcase.
     *
     * @param command the start command to send
     * @return a mono completing when the showcase is started
     */
    Mono<Void> start(StartShowcaseCommand command);
}
