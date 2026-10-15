// SPDX-License-Identifier: MIT
package showcase.api;

import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Dumps every thread's stack trace to standard error when a test fails.
 *
 * <p>`ShowcaseRestControllerCT` intermittently fails with a `TimeoutException` at the `WebTestClient` response timeout
 * on a loaded CI runner — a whole-slice stall the failure text does not explain, and which does not reproduce locally
 * (`docs/ideas.md`, 2026-10-06). Printing the JVM's full thread state on failure puts it in the output the runner
 * captures, so the next occurrence names what is stuck rather than only that the request timed out.
 *
 * <p>Registered by JUnit's extension autodetection (`junit-platform.properties` plus the `Extension` service file in
 * this source set), so it observes every test in the suite rather than only the classes that name it.
 */
public class ThreadDumpOnFailure implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
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
