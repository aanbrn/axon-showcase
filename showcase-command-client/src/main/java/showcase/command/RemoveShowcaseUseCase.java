// SPDX-License-Identifier: MIT
package showcase.command;

import reactor.core.publisher.Mono;

/**
 * Use case for removing a showcase.
 */
public interface RemoveShowcaseUseCase {
    /**
     * Removes a showcase.
     *
     * @param command the remove command to send
     * @return a mono completing when the showcase is removed
     */
    Mono<Void> remove(RemoveShowcaseCommand command);
}
