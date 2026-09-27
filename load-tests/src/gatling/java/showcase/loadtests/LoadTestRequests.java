// SPDX-License-Identifier: MIT
package showcase.loadtests;

import java.util.List;

/**
 * The request names the load-test streams issue: the read and write requests the pass assertions and the baseline
 * reference cover, excluding the SSE connection whose response time is its long-lived hold.
 */
public final class LoadTestRequests {

    /**
     * The read and write request names the pass assertions and the baseline reference cover.
     */
    public static final List<String> READ_WRITE = List.of(
            "FetchShowcases",
            "FetchShowcase",
            "ScheduleShowcase",
            "PollShowcase",
            "StartShowcase",
            "FinishShowcase",
            "RemoveShowcase");

    /**
     * The private constructor preventing instantiation.
     */
    private LoadTestRequests() {}
}
