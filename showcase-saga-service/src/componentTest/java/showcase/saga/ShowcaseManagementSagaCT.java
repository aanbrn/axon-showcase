package showcase.saga;

import lombok.val;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import showcase.command.FinishShowcaseCommand;
import showcase.command.FinishShowcaseUseCase;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.command.StartShowcaseCommand;
import showcase.command.StartShowcaseUseCase;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

@ExtendWith(MockitoExtension.class)
class ShowcaseManagementSagaCT {

    private SagaTestFixture<ShowcaseManagementSaga> fixture;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private StartShowcaseUseCase startShowcaseUseCase;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private FinishShowcaseUseCase finishShowcaseUseCase;

    @BeforeEach
    void initFixture() {
        fixture = new SagaTestFixture<>(ShowcaseManagementSaga.class);
        fixture.registerResource(startShowcaseUseCase);
        fixture.registerResource(finishShowcaseUseCase);
    }

    @Test
    void showcaseScheduledEvent_scheduledShowcase_schedulesStartShowcaseDeadline() {
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
                                  .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                                  .build())
               .expectScheduledDeadlineMatching(startTime, allOf(
                       hasProperty("deadlineName", equalTo("startShowcase")),
                       hasProperty("payload", equalTo(showcaseId))))
               .expectActiveSagas(1);
    }

    @Test
    void startShowcaseDeadline_scheduledShowcase_startsShowcase() {
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
                                  .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                                  .build())
               .whenTimeAdvancesTo(startTime)
               .expectTriggeredDeadlinesWithName("startShowcase")
               .expectNoDispatchedCommands()
               .expectActiveSagas(1);

        val startCommand =
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        verify(startShowcaseUseCase).start(startCommand);
        verifyNoMoreInteractions(startShowcaseUseCase);
    }

    @Test
    void showcaseStartedEvent_startedShowcase_schedulesFinishShowcaseDeadline() throws Exception {
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
                               .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                               .build())
               .andThenTimeAdvancesTo(startTime)
               .whenPublishingA(
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(duration)
                               .startedAt(fixture.currentTime())
                               .build())
               .expectScheduledDeadlineMatching(duration, allOf(
                       hasProperty("deadlineName", equalTo("finishShowcase")),
                       hasProperty("payload", equalTo(showcaseId))))
               .expectActiveSagas(1);
    }

    @Test
    void finishShowcaseDeadline_startedShowcase_finishesShowcase() throws Exception {
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
                               .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                               .build())
               .andThenTimeAdvancesTo(startTime)
               .andThenAPublished(
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(duration)
                               .startedAt(startTime)
                               .build())
               .whenTimeElapses(duration)
               .expectTriggeredDeadlinesWithName("startShowcase", "finishShowcase")
               .expectNoDispatchedCommands()
               .expectActiveSagas(1);

        val finishCommand =
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        verify(finishShowcaseUseCase).finish(finishCommand);
        verifyNoMoreInteractions(finishShowcaseUseCase);
    }

    @Test
    void showcaseRemovedEvent_startedShowcase_cancelsDeadlines() {
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
                               .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                               .build())
               .whenPublishingA(
                       ShowcaseRemovedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .removedAt(fixture.currentTime())
                               .build())
               .expectNoScheduledDeadlines()
               .expectActiveSagas(0);
    }

    @Test
    void showcaseRemovedEvent_finishedShowcase_cancelsDeadlines() throws Exception {
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
                               .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                               .build())
               .andThenTimeAdvancesTo(startTime)
               .andThenAPublished(
                       ShowcaseStartedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .duration(duration)
                               .startedAt(fixture.currentTime())
                               .build())
               .whenPublishingA(
                       ShowcaseRemovedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .removedAt(fixture.currentTime())
                               .build())
               .expectNoScheduledDeadlines()
               .expectActiveSagas(0);
    }
}
