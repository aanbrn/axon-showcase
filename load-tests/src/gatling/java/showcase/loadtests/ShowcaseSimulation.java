// SPDX-License-Identifier: MIT
package showcase.loadtests;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.doWhileDuring;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.stressPeakUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.sse;
import static io.gatling.javaapi.http.HttpDsl.status;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.val;

/**
 * The Gatling simulation exercising the deployed showcase pipeline: a read stream, a write-lifecycle stream, and an SSE
 * stream injected concurrently, with the profile selecting the injection curve and pass assertions. An unsupported
 * profile fails the run rather than falling back to another profile.
 */
@SuppressWarnings("unused")
public class ShowcaseSimulation extends Simulation {

    /**
     * The target host, the local cluster's gateway ingress hostname by default.
     */
    private static final String BASE_URL = property("baseUrl", "http://axon-showcase-api");

    /**
     * The injection profile and assertion set.
     */
    private static final String PROFILE = property("profile", "smoke").toLowerCase(Locale.ROOT);

    /**
     * The total workload rate in units per second.
     */
    private static final int RATE = Integer.parseInt(property("rate", "100"));

    /**
     * The read share of the workload rate.
     */
    private static final double RATIO = Double.parseDouble(property("ratio", "0.75"));

    /**
     * The run's plateau or ramp length.
     */
    private static final Duration DURATION = Duration.parse(property("duration", "PT10M"));

    /**
     * The number of SSE connections held open.
     */
    private static final int SSE_CONNECTIONS = Integer.parseInt(property("sseConnections", "10"));

    /**
     * The reference rate the performance profiles scale from.
     */
    private static final int KNEE_RATE = Integer.parseInt(property("kneeRate", "200"));

    /**
     * The pause between a stream's actions.
     */
    private static final Duration THINK_TIME = Duration.parse(property("thinkTime", "PT1S"));

    /**
     * The share of read iterations that fetch a showcase detail.
     */
    private static final double DETAIL_SHARE = Double.parseDouble(property("detailShare", "0.15"));

    /**
     * The share of write iterations that start their scheduled showcase.
     */
    private static final double START_SHARE = Double.parseDouble(property("startShare", "0.6"));

    /**
     * The share of started showcases that are finished before removal.
     */
    private static final double FINISH_SHARE = Double.parseDouble(property("finishShare", "0.5"));

    /**
     * The soak profile's plateau length.
     */
    private static final Duration HOLD = Duration.parse(property("hold", "PT2H"));

    /**
     * The 5-minute ramp and ramp-down shared by the average, soak, and stress profiles.
     */
    private static final Duration RAMP_5M = Duration.ofMinutes(5);

    /**
     * The stress profile's 10-minute ramp.
     */
    private static final Duration RAMP_10M = Duration.ofMinutes(10);

    /**
     * The 30-minute plateau shared by the average and stress profiles.
     */
    private static final Duration HOLD_30M = Duration.ofMinutes(30);

    /**
     * The spike profile's 2-minute burst.
     */
    private static final Duration SPIKE_UP = Duration.ofMinutes(2);

    /**
     * The spike profile's ramp-down after the burst.
     */
    private static final Duration SPIKE_DOWN = Duration.ofMinutes(1);

    /**
     * The breakpoint profile's 20-minute ramp.
     */
    private static final Duration BREAKPOINT_RAMP = Duration.ofMinutes(20);

    /**
     * The interval between write-lifecycle polls.
     */
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    /**
     * The window a write-lifecycle poll waits for the read model.
     */
    private static final Duration POLL_TIMEOUT = Duration.ofMinutes(5);

    /**
     * The quiet period an SSE connection holds for the smoke profile.
     */
    private static final Duration SSE_QUIET_PERIOD = Duration.ofSeconds(20);

    /**
     * The period an SSE connection holds open, matching the profile's run length (ramps plus hold).
     */
    private static final Duration SSE_HOLD =
            switch (PROFILE) {
                case "calibrate", "baseline" -> DURATION;
                case "average" -> RAMP_5M.plus(HOLD_30M).plus(RAMP_5M);
                case "soak" -> RAMP_5M.plus(HOLD).plus(RAMP_5M);
                case "stress" -> RAMP_10M.plus(HOLD_30M).plus(RAMP_5M);
                case "spike" -> SPIKE_UP.plus(SPIKE_DOWN);
                case "breakpoint" -> BREAKPOINT_RAMP;
                default -> SSE_QUIET_PERIOD;
            };

    /**
     * The request names the pass assertions cover, excluding the SSE stream's long-lived connection.
     */
    private static final List<String> REQUEST_NAMES = List.of(
            "FetchShowcases",
            "FetchShowcase",
            "ScheduleShowcase",
            "PollShowcase",
            "StartShowcase",
            "FinishShowcase",
            "RemoveShowcase");

    /**
     * The HTTP protocol, deriving the host from the base URL and sharing connections.
     */
    private static final HttpProtocolBuilder PROTOCOL = http.baseUrl(BASE_URL).shareConnections();

    /**
     * The read stream: the showcase list, then a showcase's detail for the configured share of iterations.
     */
    private static final ChainBuilder READ = exec(http("FetchShowcases")
                    .get("/showcases")
                    .check(
                            status().is(200),
                            jsonPath("$[0].showcaseId").optional().saveAs("showcaseId")))
            .pause(THINK_TIME)
            .doIf(session -> session.contains("showcaseId") && Math.random() < DETAIL_SHARE)
            .then(exec(http("FetchShowcase")
                    .get(session -> "/showcases/" + session.getString("showcaseId"))
                    .check(status().in(200, 404))));

    /**
     * The read stream, stopping on the first failed step.
     */
    private static final ScenarioBuilder READ_SCENARIO =
            scenario("Read").exitBlockOnFail().on(READ);

    /**
     * The write-lifecycle chain: schedule, then start and finish for the configured shares, always removing.
     */
    private static final ChainBuilder WRITE = exec(session -> session.set("title", aShowcaseTitle())
                    .set("startTime", aShowcaseStartTime(Instant.now()))
                    .set("duration", aShowcaseDuration()))
            .exec(http("ScheduleShowcase")
                    .post("/showcases")
                    .asJson()
                    .body(StringBody("""
                            {"title":"#{title}","startTime":"#{startTime}","duration":"#{duration}"}"""))
                    .check(status().is(201), jsonPath("$.showcaseId").saveAs("showcaseId")))
            .pause(THINK_TIME)
            .doIf(session -> Math.random() < START_SHARE)
            .then(
                    doWhileDuring("#{queryStatus} != 200", POLL_TIMEOUT)
                            .on(
                                    pause(POLL_INTERVAL),
                                    exec(http("PollShowcase")
                                            .get(session -> "/showcases/" + session.getString("showcaseId"))
                                            .check(status().in(200, 404).saveAs("queryStatus")))),
                    exec(http("StartShowcase")
                            .put(session -> "/showcases/" + session.getString("showcaseId") + "/start")
                            .check(status().is(200))),
                    pause(THINK_TIME),
                    doIf(session -> Math.random() < FINISH_SHARE)
                            .then(
                                    doWhileDuring("#{showcaseStatus} != \"STARTED\"", POLL_TIMEOUT)
                                            .on(
                                                    pause(POLL_INTERVAL),
                                                    exec(http("PollShowcase")
                                                            .get(session ->
                                                                    "/showcases/" + session.getString("showcaseId"))
                                                            .check(
                                                                    status().in(200, 404),
                                                                    jsonPath("$.status")
                                                                            .saveAs("showcaseStatus")))),
                                    exec(http("FinishShowcase")
                                            .put(session -> "/showcases/" + session.getString("showcaseId") + "/finish")
                                            .check(status().is(200))),
                                    doWhileDuring("#{showcaseStatus} != \"FINISHED\"", POLL_TIMEOUT)
                                            .on(
                                                    pause(POLL_INTERVAL),
                                                    exec(http("PollShowcase")
                                                            .get(session ->
                                                                    "/showcases/" + session.getString("showcaseId"))
                                                            .check(
                                                                    status().in(200, 404),
                                                                    jsonPath("$.status")
                                                                            .saveAs("showcaseStatus"))))))
            .exec(http("RemoveShowcase")
                    .delete(session -> "/showcases/" + session.getString("showcaseId"))
                    .check(status().is(200)));

    /**
     * The write-lifecycle stream, stopping on the first failed step.
     */
    private static final ScenarioBuilder WRITE_SCENARIO =
            scenario("Write").exitBlockOnFail().on(WRITE);

    /**
     * The SSE stream: connect, await a showcase event, hold the connection, then close.
     */
    private static final ScenarioBuilder SSE_SCENARIO = scenario("Sse")
            .exec(sse("ShowcaseEvents")
                    .get("/events")
                    .await(Duration.ofSeconds(30))
                    .on(sse.checkMessage("showcaseEvent")
                            .matching(jsonPath("$.data.showcaseId").exists())
                            .check(jsonPath("$.data.type").exists())))
            .pause(SSE_HOLD)
            .exec(sse("ShowcaseEvents").close());

    {
        setUp(
                        READ_SCENARIO.injectOpen(injectionSteps(RATIO)),
                        WRITE_SCENARIO.injectOpen(injectionSteps(1 - RATIO)),
                        SSE_SCENARIO.injectOpen(atOnceUsers(SSE_CONNECTIONS)))
                .assertions(assertions())
                .protocols(PROTOCOL);
    }

    /**
     * Reads a system property, falling back to the given default.
     *
     * @param name the property name
     * @param defaultValue the value to use when the property is unset
     * @return the property value or the default
     */
    private static String property(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    /**
     * The injection steps for a stream at the given share of the workload rate.
     *
     * @param share the stream's share of the total workload rate
     * @return the injection steps for the profile
     */
    private static OpenInjectionStep[] injectionSteps(double share) {
        return switch (PROFILE) {
            case "average" ->
                new OpenInjectionStep[] {
                    rampUsersPerSec(0).to(0.6 * KNEE_RATE * share).during(RAMP_5M),
                    constantUsersPerSec(0.6 * KNEE_RATE * share).during(HOLD_30M),
                    rampUsersPerSec(0.6 * KNEE_RATE * share).to(0).during(RAMP_5M)
                };
            case "soak" ->
                new OpenInjectionStep[] {
                    rampUsersPerSec(0).to(0.6 * KNEE_RATE * share).during(RAMP_5M),
                    constantUsersPerSec(0.6 * KNEE_RATE * share).during(HOLD),
                    rampUsersPerSec(0.6 * KNEE_RATE * share).to(0).during(RAMP_5M)
                };
            case "stress" ->
                new OpenInjectionStep[] {
                    rampUsersPerSec(0).to(0.9 * KNEE_RATE * share).during(RAMP_10M),
                    constantUsersPerSec(0.9 * KNEE_RATE * share).during(HOLD_30M),
                    rampUsersPerSec(0.9 * KNEE_RATE * share).to(0).during(RAMP_5M)
                };
            case "spike" ->
                new OpenInjectionStep[] {
                    stressPeakUsers((int) Math.round(1.5 * KNEE_RATE * share)).during(SPIKE_UP),
                    rampUsersPerSec(1.5 * KNEE_RATE * share).to(0).during(SPIKE_DOWN)
                };
            case "breakpoint" ->
                new OpenInjectionStep[] {
                    rampUsersPerSec(0).to(1.5 * KNEE_RATE * share).during(BREAKPOINT_RAMP)
                };
            case "calibrate" ->
                new OpenInjectionStep[] {rampUsersPerSec(0).to(RATE * share).during(DURATION)};
            case "baseline" ->
                new OpenInjectionStep[] {constantUsersPerSec(RATE * share).during(DURATION)};
            case "smoke" -> new OpenInjectionStep[] {atOnceUsers((int) Math.round(3 * share))};
            default -> throw unknownProfile();
        };
    }

    /**
     * The pass assertions for the profile, scoped to the read and write requests rather than the SSE stream.
     *
     * @return the assertions for the profile
     */
    private static List<Assertion> assertions() {
        if ("calibrate".equals(PROFILE) || "spike".equals(PROFILE) || "breakpoint".equals(PROFILE)) {
            return List.of();
        }
        List<Assertion> result = new ArrayList<>();
        switch (PROFILE) {
            case "average", "stress", "soak" -> {
                for (val name : REQUEST_NAMES) {
                    result.add(details(name).responseTime().mean().lte(100));
                    result.add(details(name).responseTime().percentile(95.0).lte(500));
                    result.add(details(name).responseTime().percentile(99.0).lte(1000));
                    result.add(details(name).successfulRequests().percent().gte(99.99));
                }
            }
            case "baseline" -> {
                for (val name : REQUEST_NAMES) {
                    result.add(details(name).responseTime().percentile(95.0).lte(500));
                    result.add(details(name).responseTime().percentile(99.0).lte(1000));
                    result.add(details(name).failedRequests().count().is(0L));
                }
            }
            case "smoke" -> result.add(global().failedRequests().count().is(0L));
            default -> throw unknownProfile();
        }
        return result;
    }

    /**
     * Builds the failure for an unsupported profile name.
     *
     * @return the exception naming the unsupported profile
     */
    private static IllegalArgumentException unknownProfile() {
        return new IllegalArgumentException("Unsupported load-test profile: " + PROFILE);
    }
}
