// SPDX-License-Identifier: MIT
package showcase.api;

import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import showcase.api.diagnostics.JfrSummary;
import showcase.api.diagnostics.RequestPhases;

/**
 * Prints the request phases, the JFR summary and the platform threads' stack traces when a test fails.
 *
 * <p>`ShowcaseRestControllerCT` intermittently fails with a `TimeoutException` at the `WebTestClient` response timeout
 * on a loaded CI runner — a whole-slice stall the failure text does not explain, and which does not reproduce locally
 * in this suite's shipped configuration
 * (`docs/ideas.md`, 2026-10-06). This extension is the failure-time half of the diagnosis: the request table says which
 * exchanges arrived and which never reached a terminal, the JFR summary says what the JVM was doing (pinning, parks,
 * and the CPU load peak), and the thread dump that follows puts the state it can see in the output the runner
 * captures. The order matters: the two answerable questions come first, the unanswerable snapshot
 * last.
 *
 * <p>Registered by JUnit's extension autodetection (`junit-platform.properties` plus the `Extension` service file in
 * this source set), so it observes every test in the suite rather than only the classes that name it.
 */
public class ThreadDumpOnFailure implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.err.print(RequestPhases.render());
        System.err.print(JfrSummary.render());
        System.err.println("=== thread dump after failure: " + context.getDisplayName() + " ===");
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        threads.forEach((thread, stack) -> {
            System.err.println(thread.getName() + " [" + thread.getState() + "]");
            for (StackTraceElement element : stack) {
                System.err.println("    at " + element);
            }
        });
    }
}
