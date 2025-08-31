package showcase.loadtests;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import lombok.val;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.doWhileDuring;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.exitHere;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.percent;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.randomSwitchOrElse;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.stressPeakUsers;
import static io.gatling.javaapi.core.OpenInjectionStep.atOnceUsers;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

@SuppressWarnings("unused")
public class ShowcaseSimulation extends Simulation {

    private static final HttpRequestActionBuilder scheduleShowcase =
            http("ScheduleShowcase")
                    .post("/showcases")
                    .asJson()
                    .body(StringBody("""
                                             {
                                                "showcaseId": "#{showcaseId}",
                                                "title": "#{title}",
                                                "startTime": "#{startTime}",
                                                "duration": "#{duration}"
                                             }"""));

    private static final HttpRequestActionBuilder startShowcase =
            http("StartShowcase").put(session -> "/showcases/%s/start".formatted(session.getString("showcaseId")));

    private static final HttpRequestActionBuilder finishShowcase =
            http("FinishShowcase").put(session -> "/showcases/%s/finish".formatted(session.getString("showcaseId")));

    private static final HttpRequestActionBuilder removeShowcase =
            http("RemoveShowcase").delete(session -> "/showcases/%s".formatted(session.getString("showcaseId")));

    private static final HttpRequestActionBuilder fetchShowcases = http("FetchShowcases").get("/showcases");

    private static final HttpRequestActionBuilder fetchShowcase =
            http("FetchShowcase").get(session -> "/showcases/%s".formatted(session.getString("showcaseId")));

    private static final ScenarioBuilder scenario =
            scenario("Showcase")
                    .exitBlockOnFail()
                    .on(exec(session -> session.set("showcaseId", aShowcaseId())
                                               .set("title", aShowcaseTitle())
                                               .set("startTime", aShowcaseStartTime(Instant.now()))
                                               .set("duration", aShowcaseDuration())),
                        scheduleShowcase.check(status().is(201)),
                        randomSwitchOrElse().on(percent(95).then(exitHere())).orElse(
                                doWhileDuring("#{responseStatus} != 200", Duration.ofMinutes(5))
                                        .on(pause(Duration.ofMillis(500)),
                                            fetchShowcase.check(status().in(200, 404).saveAs("responseStatus"))),
                                startShowcase.check(status().is(200)),
                                randomSwitchOrElse().on(percent(95).then(exitHere())).orElse(
                                        doWhileDuring("#{showcaseStatus} != \"STARTED\"", Duration.ofMinutes(5))
                                                .on(pause(Duration.ofMillis(500)),
                                                    fetchShowcase.check(
                                                            status().in(200, 404),
                                                            jsonPath("$.status").saveAs("showcaseStatus"))),
                                        finishShowcase.check(status().is(200)),
                                        randomSwitchOrElse().on(percent(95).then(exitHere())).orElse(
                                                doWhileDuring("#{showcaseStatus} != \"FINISHED\"",
                                                              Duration.ofMinutes(5))
                                                        .on(pause(Duration.ofMillis(500)),
                                                            fetchShowcase.check(
                                                                    status().in(200, 404),
                                                                    jsonPath("$.status").saveAs("showcaseStatus"))),
                                                removeShowcase.check(status().is(200))))));

    {
        val testType = System.getProperty("testType", "smoke");

        val injectionProfile = switch (testType) {
            case "average" -> scenario.injectOpen(
                    rampUsersPerSec(0).to(150).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(150).during(Duration.ofMinutes(30)),
                    rampUsersPerSec(150).to(0).during(Duration.ofMinutes(5)));
            case "stress" -> scenario.injectOpen(
                    rampUsersPerSec(0).to(200).during(Duration.ofMinutes(10)),
                    constantUsersPerSec(200).during(Duration.ofMinutes(30)),
                    rampUsersPerSec(200).to(0).during(Duration.ofMinutes(5)));
            case "spike" -> scenario.injectOpen(
                    stressPeakUsers(200).during(Duration.ofMinutes(2)),
                    rampUsersPerSec(200).to(0).during(Duration.ofMinutes(1)));
            case "breakpoint" -> scenario.injectOpen(
                    rampUsersPerSec(0).to(300).during(Duration.ofHours(2)));
            case "soak" -> scenario.injectOpen(
                    rampUsersPerSec(0).to(150).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(150).during(Duration.ofHours(8)),
                    rampUsersPerSec(150).to(0).during(Duration.ofMinutes(5)));
            default -> scenario.injectOpen(atOnceUsers(3));
        };

        val assertions = switch (testType) {
            case "average", "stress", "spike", "breakpoint", "soak" -> List.of(
                    global().responseTime().percentile(95.0).lte(500),
                    global().responseTime().percentile(99.0).lte(1000),
                    global().successfulRequests().percent().gte(99.99));
            default -> List.of(global().failedRequests().count().is(0L));
        };

        val protocol = http.baseUrl("http://localhost")
                           .header("Host", "axon-showcase");

        setUp(injectionProfile)
                .assertions(assertions)
                .protocols(protocol);
    }
}
