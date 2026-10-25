// SPDX-License-Identifier: MIT
package showcase.api;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * Dumps every thread's stack trace while a test is still running, so a stall shows the state before the timeout rather
 * than only what remains after it.
 *
 * <p>`ThreadDumpOnFailure` prints the JVM's threads when a test fails, which is after `WebTestClient` has given up: the
 * dump of a stalled request then shows an idle JVM with no thread in the request path, and says nothing about what the
 * request was waiting for. This extension takes bounded snapshots during the test instead — at 3 seconds and then every
 * 7 seconds — so the next occurrence of the gateway controller suites' CI-only stall names the thread that lost the
 * completion while it is still parked. A test that finishes inside 3 seconds emits nothing, so a green run pays only
 * the scheduled task's creation.
 *
 * <p>Registered by JUnit's extension autodetection (the `Extension` service file in this source set beside
 * `ThreadDumpOnFailure`), so it observes every test in the suite rather than only the classes that name it. Both hooks
 * are implemented: JUnit routes `@ParameterizedTest` cases through `interceptTestTemplateMethod`, which a plain
 * `interceptTestMethod` override never sees.
 */
public class ThreadDumpWatchdog implements InvocationInterceptor {

    /** Seconds a test may run before the first snapshot. */
    private static final long FIRST_DUMP_SECONDS = 3;

    /** Seconds between the snapshots that follow the first one. */
    private static final long FOLLOW_UP_SECONDS = 7;

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        runWithWatchdog(invocation, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        runWithWatchdog(invocation, extensionContext);
    }

    /**
     * Runs one test invocation with the snapshot schedule armed, cancelling it when the invocation returns.
     *
     * @param invocation the test invocation to proceed with
     * @param extensionContext the context the running test's display name is read from
     * @throws Throwable whatever the invocation throws, unchanged
     */
    private static void runWithWatchdog(Invocation<Void> invocation, ExtensionContext extensionContext)
            throws Throwable {
        ScheduledThreadPoolExecutor watchdog =
                new ScheduledThreadPoolExecutor(1, runnable -> new Thread(runnable, "thread-dump-watchdog"));
        watchdog.setRemoveOnCancelPolicy(true);
        String testName = extensionContext.getDisplayName();
        ScheduledFuture<?> snapshots = watchdog.scheduleAtFixedRate(
                () -> dumpThreads(testName), FIRST_DUMP_SECONDS, FOLLOW_UP_SECONDS, TimeUnit.SECONDS);
        try {
            invocation.proceed();
        } finally {
            snapshots.cancel(true);
            watchdog.shutdownNow();
        }
    }

    /**
     * Prints every thread's state and stack trace, tagged with the test it was observed in.
     *
     * @param testName the display name of the test that is still running
     */
    private static void dumpThreads(String testName) {
        System.err.println("=== thread dump while running: " + testName + " ===");
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        threads.forEach((thread, stack) -> {
            System.err.println(thread.getName() + " [" + thread.getState() + "]");
            for (StackTraceElement element : stack) {
                System.err.println("    at " + element);
            }
        });
    }
}
