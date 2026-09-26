// SPDX-License-Identifier: MIT
package showcase.loadtests;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.details;
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

/**
 * The Gatling simulation exercising the deployed showcase pipeline: a read stream, a write-lifecycle stream, and an SSE
 * stream injected concurrently, with the profile selecting the injection curve and pass assertions.
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
     * The interval between write-lifecycle polls.
     */
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    /**
     * The window a write-lifecycle poll waits for the read model.
     */
    private static final Duration POLL_TIMEOUT = Duration.ofMinutes(5);

    /**
     * The quiet period an SSE connection holds for outside the measurement profiles.
     */
    private static final Duration SSE_QUIET_PERIOD = Duration.ofSeconds(20);

    /**
     * The period an SSE connection holds open, matching the run for the long profiles.
     */
    private static final Duration SSE_HOLD =
            switch (PROFILE) {
                case "calibrate", "baseline", "average", "soak", "stress", "spike", "breakpoint" -> DURATION;
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
     * The read stream: the showcase list, then a showcase's detail when one was returned.
     */
    private static final ChainBuilder READ = exec(http("FetchShowcases")
                    .get("/showcases")
                    .check(
                            status().is(200),
                            jsonPath("$[0].showcaseId").optional().saveAs("showcaseId")))
            .doIf(session -> session.contains("showcaseId"))
            .then(exec(http("FetchShowcase")
                    .get(session -> "/showcases/" + session.getString("showcaseId"))
                    .check(status().in(200, 404))));

    /**
     * The read stream, stopping on the first failed step.
     */
    private static final ScenarioBuilder READ_SCENARIO =
            scenario("Read").exitBlockOnFail().on(READ);

    /**
     * The write-lifecycle chain: schedule, then poll-start-finish-remove each until the read model catches up.
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
            .doWhileDuring("#{queryStatus} != 200", POLL_TIMEOUT)
            .on(
                    pause(POLL_INTERVAL),
                    exec(http("PollShowcase")
                            .get(session -> "/showcases/" + session.getString("showcaseId"))
                            .check(status().in(200, 404).saveAs("queryStatus"))))
            .exec(http("StartShowcase")
                    .put(session -> "/showcases/" + session.getString("showcaseId") + "/start")
                    .check(status().is(200)))
            .doWhileDuring("#{showcaseStatus} != \"STARTED\"", POLL_TIMEOUT)
            .on(
                    pause(POLL_INTERVAL),
                    exec(http("PollShowcase")
                            .get(session -> "/showcases/" + session.getString("showcaseId"))
                            .check(status().in(200, 404), jsonPath("$.status").saveAs("showcaseStatus"))))
            .exec(http("FinishShowcase")
                    .put(session -> "/showcases/" + session.getString("showcaseId") + "/finish")
                    .check(status().is(200)))
            .doWhileDuring("#{showcaseStatus} != \"FINISHED\"", POLL_TIMEOUT)
            .on(
                    pause(POLL_INTERVAL),
                    exec(http("PollShowcase")
                            .get(session -> "/showcases/" + session.getString("showcaseId"))
                            .check(status().in(200, 404), jsonPath("$.status").saveAs("showcaseStatus"))))
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
                    rampUsersPerSec(0).to(200 * share).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(200 * share).during(Duration.ofMinutes(30)),
                    rampUsersPerSec(200 * share).to(0).during(Duration.ofMinutes(5))
                };
            case "soak" ->
                new OpenInjectionStep[] {
                    rampUsersPerSec(0).to(200 * share).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(200 * share).during(Duration.ofHours(8)),
                    rampUsersPerSec(200 * share).to(0).during(Duration.ofMinutes(5))
                };
            case "stress" ->
                new OpenInjectionStep[] {
                    rampUsersPerSec(0).to(400 * share).during(Duration.ofMinutes(10)),
                    constantUsersPerSec(400 * share).during(Duration.ofMinutes(30)),
                    rampUsersPerSec(400 * share).to(0).during(Duration.ofMinutes(5))
                };
            case "spike" ->
                new OpenInjectionStep[] {
                    stressPeakUsers((int) Math.round(4000 * share)).during(Duration.ofMinutes(2)),
                    rampUsersPerSec(4000 * share).to(0).during(Duration.ofMinutes(1))
                };
            case "breakpoint" ->
                new OpenInjectionStep[] {rampUsersPerSec(0).to(40000 * share).during(Duration.ofHours(2))};
            case "calibrate" ->
                new OpenInjectionStep[] {rampUsersPerSec(0).to(RATE * share).during(DURATION)};
            case "baseline" ->
                new OpenInjectionStep[] {constantUsersPerSec(RATE * share).during(DURATION)};
            default -> new OpenInjectionStep[] {atOnceUsers((int) Math.round(3 * share))};
        };
    }

    /**
     * The pass assertions for the profile, scoped to the read and write requests rather than the SSE stream.
     *
     * @return the assertions for the profile
     */
    private static List<Assertion> assertions() {
        if ("calibrate".equals(PROFILE)) {
            return List.of();
        }
        List<Assertion> result = new ArrayList<>();
        switch (PROFILE) {
            case "average", "stress", "spike", "breakpoint", "soak" -> {
                for (String name : REQUEST_NAMES) {
                    result.add(details(name).responseTime().mean().lte(100));
                    result.add(details(name).responseTime().percentile(95.0).lte(500));
                    result.add(details(name).responseTime().percentile(99.0).lte(1000));
                    result.add(details(name).successfulRequests().percent().gte(99.99));
                }
            }
            case "baseline" -> {
                for (String name : REQUEST_NAMES) {
                    result.add(details(name).responseTime().percentile(95.0).lte(500));
                    result.add(details(name).responseTime().percentile(99.0).lte(1000));
                    result.add(details(name).failedRequests().count().is(0L));
                }
            }
            default -> result.add(global().failedRequests().count().is(0L));
        }
        return result;
    }
}
