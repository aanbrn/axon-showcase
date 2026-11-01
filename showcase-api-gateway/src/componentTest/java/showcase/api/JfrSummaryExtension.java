// SPDX-License-Identifier: MIT
package showcase.api;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import showcase.api.diagnostics.JfrSummary;

/**
 * Starts the JFR recording before the suite's first test, so a stall's events are captured from the beginning.
 *
 * <p>{@link JfrSummary} starts its recording when its class is first initialized, and a recording that starts at the
 * first failure has already lost the window that matters: a virtual thread that never ran, or a CPU peak during the
 * failing test, happened before the failure printed. This extension touches the class at suite start, which is the
 * cheapest way to make the start deterministic — the recording itself stays lazy and idempotent.
 *
 * <p>Registered by JUnit's extension autodetection beside the two failure-time diagnostics, so a suite that loads them
 * loads this one first.
 */
public class JfrSummaryExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        JfrSummary.ensureStarted();
    }
}
