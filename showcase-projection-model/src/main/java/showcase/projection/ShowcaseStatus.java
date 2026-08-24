// SPDX-License-Identifier: MIT
package showcase.projection;

/**
 * The lifecycle status of a showcase in the projection model.
 */
public enum ShowcaseStatus {
    /**
     * The showcase is scheduled, but not started yet.
     */
    SCHEDULED,

    /**
     * The showcase has been started and is running.
     */
    STARTED,

    /**
     * The showcase has been finished.
     */
    FINISHED
}
