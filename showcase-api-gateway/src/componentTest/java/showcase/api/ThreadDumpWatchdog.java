// SPDX-License-Identifier: MIT
package showcase.api;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
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
 * 7 seconds — so the next occurrence of the gateway controller suites' stall names the thread that lost the completion
 * while it is still parked. The stall does not reproduce locally in the suites' shipped configuration, and a test that
 * finishes inside 3 seconds emits nothing, so a green run pays only the scheduled task's creation; the watchdog thread
 * is a daemon, so a stuck test can never keep the test JVM alive.
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

    /** The line shape a `jcmd` text dump gives a virtual thread. */
    private static final Pattern VIRTUAL_THREAD = Pattern.compile("^#\\d+ \".*\" virtual(\\s|$)");

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
        val snapshot = renderVirtualThreadAwareDump();
        if (snapshot.cancelled()) {
            return;
        }
        if (snapshot.incomplete() == null) {
            System.err.println("=== thread dump while running (" + countVirtualThreads(snapshot.dump())
                    + " virtual threads included): " + testName + " ===");
        } else {
            System.err.println("=== thread dump while running (platform threads only, the snapshot is INCOMPLETE: "
                    + snapshot.incomplete() + "): " + testName + " ===");
        }
        System.err.print(snapshot.dump());
    }

    /**
     * Renders the JVM's own thread dump, which includes virtual threads, by running this JDK's `jcmd`.
     *
     * @return the dump and, when it is incomplete, why — or a cancelled result when the test ended mid-snapshot
     */
    private static Snapshot renderVirtualThreadAwareDump() {
        val jcmd = Path.of(System.getProperty("java.home"), "bin", "jcmd");
        if (!Files.isExecutable(jcmd)) {
            return Snapshot.platformOnly("no jcmd at " + jcmd);
        }
        Path dumpDirectory;
        try {
            dumpDirectory = Files.createTempDirectory("thread-dump-watchdog-");
        } catch (IOException failure) {
            return Snapshot.platformOnly("jcmd failed: " + failure);
        }
        try {
            // jcmd refuses to overwrite an existing file, so the path is named inside a fresh directory.
            val dumpFile = dumpDirectory.resolve("dump.txt");
            val process = new ProcessBuilder(
                            jcmd.toString(),
                            Long.toString(ProcessHandle.current().pid()),
                            "Thread.dump_to_file",
                            "-format=text",
                            dumpFile.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            try {
                if (!process.waitFor(DUMP_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    return Snapshot.platformOnly("jcmd produced no dump within " + DUMP_COMMAND_TIMEOUT_SECONDS + "s");
                }
            } catch (InterruptedException cancelled) {
                // A slow test's snapshot must not outlive it: the child is killed, then the interrupt is restored.
                process.destroyForcibly();
                Thread.currentThread().interrupt();
                return Snapshot.cancelledSnapshot();
            }
            if (process.exitValue() != 0) {
                return Snapshot.platformOnly("jcmd exited " + process.exitValue());
            }
            val dump = Files.readString(dumpFile);
            if (dump.isBlank()) {
                return Snapshot.platformOnly("jcmd wrote an empty dump");
            }
            return Snapshot.complete(dump);
        } catch (IOException failure) {
            return Snapshot.platformOnly("jcmd failed: " + failure);
        } finally {
            deleteDumpDirectory(dumpDirectory);
        }
    }

    /**
     * Counts the virtual threads a `jcmd` text dump holds, so an absent request path is a number rather than an
     * inference.
     *
     * <p>The line shape is the JDK's: 21 writes `#<id> "<name>" virtual` and a newer JDK appends the state and a
     * timestamp, so the trailing marker is matched either way — and a platform thread merely named `virtual` is not
     * counted, because the marker must follow the closing quote. A JVM started with `-Djdk.trackAllThreads=false` lists
     * only the mounted virtual thread, which the count then shows as one rather than none.
     *
     * @param dump the `jcmd` dump text
     * @return how many virtual threads it lists
     */
    static long countVirtualThreads(String dump) {
        return dump.lines().filter(line -> VIRTUAL_THREAD.matcher(line).find()).count();
    }

    /**
     * Removes the snapshot's temporary directory and file, leaving a leftover only if the deletion fails.
     *
     * @param dumpDirectory the directory the dump was written into
     */
    private static void deleteDumpDirectory(Path dumpDirectory) {
        try {
            Files.deleteIfExists(dumpDirectory.resolve("dump.txt"));
            Files.deleteIfExists(dumpDirectory);
        } catch (IOException ignored) {
            // A leftover temp directory is not worth failing a diagnostic over.
        }
    }

    /** Renders the platform threads, the only threads `Thread.getAllStackTraces()` exposes. */
    private static String renderPlatformThreadDump() {
        val rendered = new StringBuilder();
        val threads = Thread.getAllStackTraces();
        threads.forEach((thread, stack) -> {
            rendered.append(thread.getName())
                    .append(" [")
                    .append(thread.getState())
                    .append("]\n");
            for (val element : stack) {
                rendered.append("    at ").append(element).append('\n');
            }
        });
        return rendered.toString();
    }

    /**
     * One snapshot outcome: the rendered threads, why it is incomplete when it is, and whether it was cancelled.
     *
     * @param dump the rendered thread dump
     * @param incomplete the reason the dump lacks virtual threads, or {@code null} when it is complete
     * @param cancelled whether the test ended before the snapshot could be taken, in which case nothing is printed
     */
    private record Snapshot(String dump, String incomplete, boolean cancelled) {

        static Snapshot complete(String dump) {
            return new Snapshot(dump, null, false);
        }

        static Snapshot platformOnly(String reason) {
            return new Snapshot(renderPlatformThreadDump(), reason, false);
        }

        static Snapshot cancelledSnapshot() {
            return new Snapshot("", null, true);
        }
    }
}
