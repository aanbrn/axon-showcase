// SPDX-License-Identifier: MIT
package showcase.command;

/**
 * Lifecycle states of a showcase.
 */
enum ShowcaseStatus {
    /**
     * A showcase has been scheduled, but not yet started.
     */
    SCHEDULED,

    /**
     * A showcase has been started and is in progress.
     */
    STARTED,

    /**
     * A showcase has been finished.
     */
    FINISHED
}
