// SPDX-License-Identifier: MIT
package showcase.api;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.val;
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
 * the scheduled task's creation, and the watchdog thread is a daemon so a stuck test can never keep the test JVM alive.
 *
 * <p>The snapshot comes from `jcmd Thread.dump_to_file` because this application enables virtual threads
 * (`spring.threads.virtual.enabled`), which `Thread.getAllStackTraces()` does not return at all — a snapshot built from
 * it reports an idle JVM while the request sits on a virtual thread. The platform-thread dump stays as the fallback for
 * a JVM without a `jcmd` next to its `java` binary.
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

    /** Seconds the external dump command may take before it is abandoned. */
    private static final long DUMP_COMMAND_TIMEOUT_SECONDS = 15;

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
        try (val watchdog = new ScheduledThreadPoolExecutor(1, runnable -> {
            val thread = new Thread(runnable, "thread-dump-watchdog");
            thread.setDaemon(true);
            return thread;
        })) {
            watchdog.setRemoveOnCancelPolicy(true);
            val testName = extensionContext.getDisplayName();
            val snapshots = watchdog.scheduleAtFixedRate(
                    () -> dumpThreads(testName), FIRST_DUMP_SECONDS, FOLLOW_UP_SECONDS, TimeUnit.SECONDS);
            try {
                invocation.proceed();
            } finally {
                snapshots.cancel(true);
                watchdog.shutdownNow();
            }
        }
    }

    /**
     * Prints a snapshot of every thread, preferring the external dump that also covers virtual threads.
     *
     * @param testName the display name of the test that is still running
     */
    private static void dumpThreads(String testName) {
        val degradation = printVirtualThreadAwareDump();
        if (degradation == null) {
            System.err.println("=== thread dump while running (virtual threads included): " + testName + " ===");
        } else {
            System.err.println("=== thread dump while running (platform threads only, the snapshot is INCOMPLETE: "
                    + degradation + "): " + testName + " ===");
            printPlatformThreadDump();
        }
    }

    /**
     * Prints the JVM's own thread dump, which includes virtual threads, by running this JDK's `jcmd`.
     *
     * @return {@code null} when the dump was printed, otherwise why the snapshot is incomplete
     */
    private static String printVirtualThreadAwareDump() {
        val jcmd = Path.of(System.getProperty("java.home"), "bin", "jcmd");
        if (!Files.isExecutable(jcmd)) {
            return "no jcmd at " + jcmd;
        }
        Path dumpDirectory = null;
        Process process = null;
        try {
            dumpDirectory = Files.createTempDirectory("thread-dump-watchdog-");
            // jcmd refuses to overwrite an existing file, so the path is named inside a fresh directory.
            val dumpFile = dumpDirectory.resolve("dump.txt");
            process = new ProcessBuilder(
                            jcmd.toString(),
                            Long.toString(ProcessHandle.current().pid()),
                            "Thread.dump_to_file",
                            "-format=text",
                            dumpFile.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(DUMP_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "jcmd produced no dump within " + DUMP_COMMAND_TIMEOUT_SECONDS + "s";
            }
            if (process.exitValue() != 0) {
                return "jcmd exited " + process.exitValue();
            }
            val dump = Files.readString(dumpFile);
            if (dump.isBlank()) {
                return "jcmd wrote an empty dump";
            }
            System.err.print(dump);
            return null;
        } catch (InterruptedException cancelled) {
            // A slow test's snapshot must not outlive it: the child is killed, then the interrupt is restored.
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            return "the snapshot was cancelled";
        } catch (IOException failure) {
            return "jcmd failed: " + failure;
        } finally {
            if (dumpDirectory != null) {
                try {
                    Files.deleteIfExists(dumpDirectory.resolve("dump.txt"));
                    Files.deleteIfExists(dumpDirectory);
                } catch (IOException ignored) {
                    // A leftover temp directory is not worth failing a diagnostic over.
                }
            }
        }
    }

    /** Prints the platform threads, the only threads `Thread.getAllStackTraces()` exposes. */
    private static void printPlatformThreadDump() {
        val threads = Thread.getAllStackTraces();
        threads.forEach((thread, stack) -> {
            System.err.println(thread.getName() + " [" + thread.getState() + "]");
            for (val element : stack) {
                System.err.println("    at " + element);
            }
        });
    }
}
