package showcase.command;

import lombok.val;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

class ShowcaseSagaCT {

    private SagaTestFixture<ShowcaseSaga> fixture;

    @BeforeEach
    void initFixture() {
        fixture = new SagaTestFixture<>(ShowcaseSaga.class);
    }

    @Test
    void showcaseScheduledEvent_success_schedulesStartShowcaseDeadline() {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);

        fixture.whenAggregate(showcaseId)
               .publishes(ShowcaseScheduledEvent
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(startTime)
                                  .duration(aShowcaseDuration())
                                  .scheduledAt(scheduleTime)
                                  .build())
               .expectScheduledDeadlineMatching(startTime, allOf(
                       hasProperty("deadlineName", equalTo("startShowcase")),
                       hasProperty("payload", equalTo(showcaseId))))
               .expectActiveSagas(1);
    }

    @Test
    void startShowcaseDeadline_success_startsShowcase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);

        fixture.givenAggregate(showcaseId)
               .published(ShowcaseScheduledEvent
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(startTime)
                                  .duration(aShowcaseDuration())
                                  .scheduledAt(scheduleTime)
                                  .build())
               .whenTimeAdvancesTo(startTime)
               .expectDispatchedCommands(
                       StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
               .expectActiveSagas(1);
    }

    @Test
    void startShowcaseDeadline_alreadyStarted_startsShowcase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);

        fixture.givenAggregate(showcaseId)
               .published(
                       ShowcaseScheduledEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .title(aShowcaseTitle())
                               .startTime(startTime)
                               .duration(aShowcaseDuration())
                               .scheduledAt(scheduleTime)
                               .build(),
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(aShowcaseDuration())
                               .startedAt(startTime)
                               .build())
               .whenTimeAdvancesTo(startTime)
               .expectNoDispatchedCommands()
               .expectActiveSagas(1);
    }

    @Test
    void showcaseStartedEvent_success_schedulesFinishShowcaseDeadline() throws Exception {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        fixture.givenAggregate(showcaseId)
               .published(
                       ShowcaseScheduledEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .title(aShowcaseTitle())
                               .startTime(startTime)
                               .duration(duration)
                               .scheduledAt(scheduleTime)
                               .build())
               .andThenTimeAdvancesTo(startTime)
               .whenPublishingA(
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(duration)
                               .startedAt(startTime)
                               .build())
               .expectScheduledDeadlineMatching(startTime.plus(duration), allOf(
                       hasProperty("deadlineName", equalTo("finishShowcase")),
                       hasProperty("payload", equalTo(showcaseId))))
               .expectActiveSagas(1);
    }

    @Test
    void finishShowcaseDeadline_success_finishesShowcaseAndEndsSaga() throws Exception {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        fixture.givenAggregate(showcaseId)
               .published(
                       ShowcaseScheduledEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .title(aShowcaseTitle())
                               .startTime(startTime)
                               .duration(duration)
                               .scheduledAt(scheduleTime)
                               .build())
               .andThenTimeAdvancesTo(startTime)
               .andThenAPublished(
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(duration)
                               .startedAt(startTime)
                               .build())
               .whenTimeAdvancesTo(startTime.plus(duration))
               .expectDispatchedCommands(
                       FinishShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
               .expectActiveSagas(0);
    }

    @Test
    void showcaseFinishedEvent_success_endsSaga() throws Exception {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        fixture.givenAggregate(showcaseId)
               .published(
                       ShowcaseScheduledEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .title(aShowcaseTitle())
                               .startTime(startTime)
                               .duration(duration)
                               .scheduledAt(scheduleTime)
                               .build())
               .andThenTimeAdvancesTo(startTime)
               .andThenAPublished(
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(duration)
                               .startedAt(startTime)
                               .build())
               .andThenTimeElapses(duration)
               .whenPublishingA(
                       ShowcaseFinishedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .finishedAt(startTime.plus(duration))
                               .build())
               .expectActiveSagas(0);
    }

    @Test
    void showcaseRemovedEvent_success_endsSaga() {
        val showcaseId = aShowcaseId();
        val scheduleTime = fixture.currentTime();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        fixture.givenAggregate(showcaseId)
               .published(
                       ShowcaseScheduledEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .title(aShowcaseTitle())
                               .startTime(startTime)
                               .duration(duration)
                               .scheduledAt(scheduleTime)
                               .build())
               .whenPublishingA(
                       ShowcaseRemovedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .removedAt(scheduleTime)
                               .build())
               .expectActiveSagas(0);
    }
}
