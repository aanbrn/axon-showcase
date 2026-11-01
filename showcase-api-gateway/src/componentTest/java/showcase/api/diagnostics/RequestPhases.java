// SPDX-License-Identifier: MIT
package showcase.api.diagnostics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Records how far each exchange got, so a stall names what is known about it rather than only that it stalled.
 *
 * <p>The gateway controller suites intermittently stall on CI with a `TimeoutException` at `WebTestClient.exchange()`
 * (`docs/ideas.md`, 2026-10-06), and the parked diagnosis asks for a correlated per-request sequence rather than
 * another snapshot. This class records the boundaries a slice can attribute to a request: admission at the filter and
 * the exchange's terminal, each stamped with {@link System#nanoTime()} and the subscribing thread's name and
 * virtual-thread identity. A row with no terminal is the shape a stall leaves, and a control asserts it.
 *
 * <p>Beside the rows it prints an <em>aggregate</em> line for the blocking scheduler the gateway routes every
 * controller method to (`ShowcaseBlockingExecutionConfigurer`), measured by decorating the executor Reactor registers
 * under the {@code boundedElastic} key: how many blocking tasks were submitted, how many are periodic, the longest wait
 * past a task's eligible time, and the longest run. The figures are aggregate because no request identity reaches the
 * scheduler from test scope — a version that attached a task by submitting thread attributed nothing — and each state
 * says whether it was observed: an unstarted or unreturned task renders {@code unavailable}, never a zero.
 *
 * <p>The rows are bounded, {@link #clear()} drops them between control tests, and the scheduler figures are left
 * operational across the run so a real failure prints the whole run's activity. The filter observes only: the exchange
 * it returns is the chain's own.
 */
public final class RequestPhases implements WebFilter, Ordered {

    /** How many completed exchanges are kept beside the still-open ones. */
    static final int KEPT_CLOSED = 32;

    /** The scheduler key Reactor registers the gateway's blocking executor under. */
    static final String BLOCKING_SCHEDULER = "boundedElastic";

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private static final Deque<RequestPhase> CLOSED = new ConcurrentLinkedDeque<>();

    private static final Map<Long, RequestPhase> OPEN = new ConcurrentHashMap<>();

    private static final AtomicLong SUBMITTED = new AtomicLong();

    private static final AtomicLong PERIODIC_TASKS = new AtomicLong();

    private static final AtomicLong QUEUE_SAMPLES = new AtomicLong();

    private static final AtomicLong RUN_SAMPLES = new AtomicLong();

    private static final AtomicLong MAX_QUEUE_MILLIS = new AtomicLong();

    private static final AtomicLong MAX_RUN_MILLIS = new AtomicLong();

    /**
     * Decorates the blocking scheduler's executor once, so every blocking task reports into the aggregate figures.
     *
     * <p>Safe to call more than once: Reactor keeps one decorator per scheduler key.
     */
    static void instrumentBlockingScheduler() {
        Schedulers.addExecutorServiceDecorator(
                BLOCKING_SCHEDULER, (scheduler, service) -> new TimedExecutorService(service));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        val phase = RequestPhase.arriving(exchange);
        OPEN.put(phase.id(), phase);
        return chain.filter(exchange)
                .doOnSuccess(ignored -> close(phase.id(), exchange, "OK", null))
                .doOnError(failure ->
                        close(phase.id(), exchange, "FAILED", failure.getClass().getSimpleName()))
                .doOnCancel(() -> close(phase.id(), exchange, "CANCELLED", null));
    }

    /** Drops the recorded rows, so a control's synthetic exchanges cannot outlive it; the aggregates stay. */
    static void clear() {
        OPEN.clear();
        CLOSED.clear();
    }

    /** Renders the open requests, then the most recent completed ones, as a failure-time table. */
    public static String render() {
        val open = new ArrayList<>(OPEN.values());
        open.sort(Comparator.comparingLong(RequestPhase::id));
        val closed = new ArrayList<>(CLOSED);
        return "=== request phases: " + open.size() + " open, " + closed.size() + " recent ===\n"
                + "=== blocking scheduler (aggregate; the executor Reactor registers under `" + BLOCKING_SCHEDULER
                + "`; no request identity reaches it, so no row carries a queue or run time): submitted="
                + SUBMITTED.get() + " periodic=" + PERIODIC_TASKS.get() + " queue " + queueFigure() + " run "
                + runFigure() + " ===\n"
                + open.stream()
                        .map(phase -> renderRow(phase, System.nanoTime()))
                        .reduce("", String::concat)
                + closed.stream()
                        .map(phase -> renderRow(phase, phase.endedNanos()))
                        .reduce("", String::concat);
    }

    /** Renders one row, with the elapsed time measured to {@code nowNanos} so an open request keeps counting. */
    static String renderRow(RequestPhase phase, long nowNanos) {
        val elapsedMillis = (nowNanos - phase.arrivedNanos()) / 1_000_000;
        return String.format(
                "%-9s #%d %-6s %-34s %6d ms  last=%-8s status=%-4s subscribed-on=%s virtual=%s%s%n",
                phase.terminal() == null ? "OPEN" : phase.terminal(),
                phase.id(),
                phase.method(),
                phase.path(),
                elapsedMillis,
                phase.lastBoundary(),
                phase.status() == null ? "-" : phase.status(),
                phase.threadName(),
                phase.virtual(),
                phase.failure() == null ? "" : " failure=" + phase.failure());
    }

    private static String queueFigure() {
        return queueFigure(QUEUE_SAMPLES.get(), MAX_QUEUE_MILLIS.get());
    }

    private static String runFigure() {
        return runFigure(RUN_SAMPLES.get(), MAX_RUN_MILLIS.get());
    }

    /**
     * Renders the queue figure for a sample count and maximum, so a control can assert both branches directly.
     *
     * @param samples how many task starts were observed
     * @param maxMillis the longest observed wait past a task's eligible time
     * @return the figure, or the reason it is unavailable
     */
    static String queueFigure(long samples, long maxMillis) {
        return samples == 0
                ? "unavailable (no task has started yet)"
                : "max=" + maxMillis + " ms over " + samples + " starts, from each task's eligible time";
    }

    /**
     * Renders the run figure for a sample count and maximum, so a control can assert both branches directly.
     *
     * @param samples how many task returns were observed
     * @param maxMillis the longest observed run
     * @return the figure, or the reason it is unavailable
     */
    static String runFigure(long samples, long maxMillis) {
        return samples == 0
                ? "unavailable (no task has returned yet)"
                : "max=" + maxMillis + " ms over " + samples + " returns";
    }

    private static void close(long id, ServerWebExchange exchange, String terminal, @Nullable String failure) {
        val current = OPEN.remove(id);
        if (current == null) {
            return;
        }
        CLOSED.addLast(current.closing(terminal, exchange.getResponse().getStatusCode(), failure, System.nanoTime()));
        while (CLOSED.size() > KEPT_CLOSED) {
            CLOSED.pollFirst();
        }
    }

    /** Reserves the next request number for a row the tests build directly. */
    static long nextId() {
        return SEQUENCE.incrementAndGet();
    }

    /** Notes a blocking task's submission, for the aggregate line only — no request identity reaches the scheduler. */
    static void submitted() {
        SUBMITTED.incrementAndGet();
    }

    /**
     * Notes that a submitted blocking task started, and how long it waited past the time it was eligible.
     *
     * @param eligibleNanos the monotonic timestamp the task became eligible to run
     * @param startedNanos the monotonic timestamp the task started
     */
    static void started(long eligibleNanos, long startedNanos) {
        QUEUE_SAMPLES.incrementAndGet();
        MAX_QUEUE_MILLIS.accumulateAndGet(Math.max(0, (startedNanos - eligibleNanos) / 1_000_000), Math::max);
    }

    /**
     * Notes that a blocking task returned, and how long it ran.
     *
     * @param ranNanos how long the task ran
     */
    static void returned(long ranNanos) {
        RUN_SAMPLES.incrementAndGet();
        MAX_RUN_MILLIS.accumulateAndGet(ranNanos / 1_000_000, Math::max);
    }

    /**
     * One exchange's phases: when it arrived, what it was, which thread subscribed it, and how it ended when it has.
     *
     * @param id the request's sequence number, ascending in arrival order
     * @param method the HTTP method
     * @param path the request path
     * @param arrivedNanos the monotonic timestamp of admission
     * @param threadName the name of the thread that subscribed the exchange
     * @param virtual whether that thread was a virtual thread
     * @param terminal the terminal outcome, or {@code null} while the exchange is open
     * @param status the response status code rendered at close time, or {@code null} when it was not set
     * @param failure the failing exception's simple name, or {@code null}
     * @param endedNanos the monotonic timestamp of the terminal, or {@code 0} while open
     */
    record RequestPhase(
            long id,
            String method,
            String path,
            long arrivedNanos,
            String threadName,
            boolean virtual,
            @Nullable String terminal,
            @Nullable String status,
            @Nullable String failure,
            long endedNanos) {

        static RequestPhase arriving(ServerWebExchange exchange) {
            val thread = Thread.currentThread();
            return new RequestPhase(
                    nextId(),
                    exchange.getRequest().getMethod().name(),
                    exchange.getRequest().getPath().value(),
                    System.nanoTime(),
                    thread.getName(),
                    thread.isVirtual(),
                    null,
                    null,
                    null,
                    0);
        }

        /** The boundary this request last reached: admission, or the terminal once the exchange closed. */
        String lastBoundary() {
            return terminal == null ? "ADMITTED" : "TERMINAL";
        }

        RequestPhase closing(String outcome, @Nullable Object statusCode, @Nullable String fail, long nowNanos) {
            return new RequestPhase(
                    id,
                    method,
                    path,
                    arrivedNanos,
                    threadName,
                    virtual,
                    outcome,
                    statusCode == null ? null : statusCode.toString(),
                    fail,
                    nowNanos);
        }
    }

    /** Delegates to the blocking scheduler's executor, stamping each task's submission, start and return. */
    private static final class TimedExecutorService extends java.util.concurrent.AbstractExecutorService
            implements ScheduledExecutorService {

        private final ScheduledExecutorService delegate;

        TimedExecutorService(ScheduledExecutorService delegate) {
            this.delegate = delegate;
        }

        private Runnable timed(Runnable task, long eligibleNanos) {
            return () -> {
                val startedNanos = System.nanoTime();
                started(eligibleNanos, startedNanos);
                try {
                    task.run();
                } finally {
                    returned(System.nanoTime() - startedNanos);
                }
            };
        }

        private <V> Callable<V> timed(Callable<V> task, long eligibleNanos) {
            return () -> {
                val startedNanos = System.nanoTime();
                started(eligibleNanos, startedNanos);
                try {
                    return task.call();
                } finally {
                    returned(System.nanoTime() - startedNanos);
                }
            };
        }

        @Override
        public void execute(Runnable command) {
            submitted();
            delegate.execute(timed(command, System.nanoTime()));
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            submitted();
            return delegate.schedule(timed(command, System.nanoTime() + unit.toNanos(delay)), delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            submitted();
            return delegate.schedule(timed(callable, System.nanoTime() + unit.toNanos(delay)), delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            submitted();
            PERIODIC_TASKS.incrementAndGet();
            return delegate.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(
                Runnable command, long initialDelay, long delay, TimeUnit unit) {
            submitted();
            PERIODIC_TASKS.incrementAndGet();
            return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }
    }
}
