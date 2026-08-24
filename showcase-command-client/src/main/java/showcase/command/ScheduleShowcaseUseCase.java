// SPDX-License-Identifier: MIT
package showcase.command;

import reactor.core.publisher.Mono;

/**
 * Use case for scheduling a new showcase.
 */
public interface ScheduleShowcaseUseCase {
    /**
     * Schedules a new showcase.
     *
     * @param command the schedule command to send
     * @return a mono completing when the showcase is scheduled
     */
    Mono<Void> schedule(ScheduleShowcaseCommand command);
}
