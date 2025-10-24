package showcase.command;

import lombok.val;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import showcase.command.ShowcaseTitleReservation.DuplicateTitleException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static showcase.command.RandomCommandTestUtils.aScheduleShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;
import static showcase.command.ShowcaseCommandMatchers.aCommandErrorDetailsWithErrorCode;
import static showcase.command.ShowcaseCommandMatchers.aCommandErrorDetailsWithErrorMessage;
import static showcase.command.ShowcaseCommandMatchers.aCommandErrorDetailsWithMetaData;

@ExtendWith(MockitoExtension.class)
@NullUnmarked
class ShowcaseAggregateCT {

    private AggregateTestFixture<ShowcaseAggregate> fixture;

    @Mock
    private ShowcaseTitleReservation showcaseTitleReservation;

    @BeforeEach
    void initFixture() {
        fixture = new AggregateTestFixture<>(ShowcaseAggregate.class);
        fixture.registerCommandHandlerInterceptor(new ShowcaseCommandMessageInterceptor<>());
        fixture.registerInjectableResource(showcaseTitleReservation);
    }

    @Test
    void scheduleShowcase_validCommand_emitsShowcaseScheduledEvent_updatesState() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.givenNoPriorActivity()
               .when(ScheduleShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .title(title)
                             .startTime(startTime)
                             .duration(duration)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectEvents(
                       ShowcaseScheduledEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .title(title)
                               .startTime(startTime)
                               .duration(duration)
                               .scheduledAt(fixture.currentTime())
                               .build())
               .expectState(it -> {
                   assertThat(it.getShowcaseId()).isEqualTo(showcaseId);
                   assertThat(it.getTitle()).isEqualTo(title);
                   assertThat(it.getStartTime()).isEqualTo(startTime);
                   assertThat(it.getDuration()).isEqualTo(duration);
                   assertThat(it.getStatus()).isEqualTo(ShowcaseStatus.SCHEDULED);
                   assertThat(it.getStartedAt()).isNull();
                   assertThat(it.getFinishedAt()).isNull();
               });
    }

    @Test
    void scheduleShowcase_duplicatedSchedule_executesHandlerSuccessfully_emitsNoEvents() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(title)
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .when(ScheduleShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .title(title)
                             .startTime(startTime)
                             .duration(duration)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectNoEvents()
               .expectState(it -> {
                   assertThat(it.getShowcaseId()).isEqualTo(showcaseId);
                   assertThat(it.getTitle()).isEqualTo(title);
                   assertThat(it.getStartTime()).isEqualTo(startTime);
                   assertThat(it.getDuration()).isEqualTo(duration);
                   assertThat(it.getStatus()).isEqualTo(ShowcaseStatus.SCHEDULED);
                   assertThat(it.getStartedAt()).isNull();
                   assertThat(it.getFinishedAt()).isNull();
               });
    }

    @Test
    void scheduleShowcase_invalidCommand_throwsShowcaseCommandExceptionWithInvalidCommandError() {
        fixture.givenNoPriorActivity()
               .when(ScheduleShowcaseCommand
                             .builder()
                             .showcaseId(anInvalidShowcaseId())
                             .title(aTooLongShowcaseTitle())
                             .startTime(fixture.currentTime())
                             .duration(aTooLongShowcaseDuration())
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionDetails(allOf(
                       aCommandErrorDetailsWithErrorCode(is(ShowcaseCommandErrorCode.INVALID_COMMAND)),
                       aCommandErrorDetailsWithErrorMessage(is("Given command is not valid")),
                       aCommandErrorDetailsWithMetaData(allOf(
                               aMapWithSize(4),
                               hasKey("showcaseId"),
                               hasKey("title"),
                               hasKey("startTime"),
                               hasKey("duration")))));
    }

    @Test
    void scheduleShowcase_reusedShowcaseId_throwsShowcaseCommandExceptionWithIllegalStateError() {
        val showcaseId = aShowcaseId();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(fixture.currentTime()))
                              .duration(aShowcaseDuration())
                              .scheduledAt(fixture.currentTime())
                              .build())
               .when(ScheduleShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .title(aShowcaseTitle())
                             .startTime(aShowcaseStartTime(fixture.currentTime()))
                             .duration(aShowcaseDuration())
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionDetails(
                       ShowcaseCommandErrorDetails
                               .builder()
                               .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                               .errorMessage("Showcase cannot be rescheduled")
                               .build());
    }

    @Test
    void scheduleShowcase_reusedTitle_throwsShowcaseCommandExceptionWithTitleInUseError() {
        val command = aScheduleShowcaseCommand(fixture.currentTime());

        doThrow(DuplicateTitleException.class).when(showcaseTitleReservation).save(command.title());

        fixture.givenNoPriorActivity()
               .when(command)
               .expectException(ShowcaseCommandException.class)
               .expectExceptionMessage("Given title is in use already")
               .expectExceptionDetails(
                       ShowcaseCommandErrorDetails
                               .builder()
                               .errorCode(ShowcaseCommandErrorCode.TITLE_IN_USE)
                               .errorMessage("Given title is in use already")
                               .build());
    }

    @Test
    void scheduleShowcase_alreadyRemoved_throwsShowcaseCommandExceptionWithIllegalStateError() {
        val showcaseId = aShowcaseId();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(fixture.currentTime()))
                              .duration(aShowcaseDuration())
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGiven(ShowcaseRemovedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .removedAt(fixture.currentTime())
                                 .build())
               .when(ScheduleShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .title(aShowcaseTitle())
                             .startTime(aShowcaseStartTime(fixture.currentTime()))
                             .duration(aShowcaseDuration())
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionDetails(
                       ShowcaseCommandErrorDetails
                               .builder()
                               .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                               .errorMessage("Showcase is removed already")
                               .build());
    }

    @Test
    void startShowcase_validCommand_emitsShowcaseStartedEvent_updatesState() {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGivenCurrentTime(startTime)
               .when(StartShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectEvents(ShowcaseStartedEvent
                                     .builder()
                                     .showcaseId(showcaseId)
                                     .duration(duration)
                                     .startedAt(fixture.currentTime())
                                     .build())
               .expectState(it -> {
                   assertThat(it.getStatus()).isEqualTo(ShowcaseStatus.STARTED);
                   assertThat(it.getStartedAt()).isNotNull();
                   assertThat(it.getFinishedAt()).isNull();
               });
    }

    @Test
    void startShowcase_alreadyStartedShowcase_succeedsWithNoEvents() {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGiven(ShowcaseStartedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .startedAt(fixture.currentTime())
                                 .duration(duration)
                                 .build())
               .when(StartShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectNoEvents();
    }

    @Test
    void startShowcase_invalidCommand_throwsShowcaseCommandExceptionWithInvalidCommandError() {
        fixture.givenNoPriorActivity()
               .when(StartShowcaseCommand
                             .builder()
                             .showcaseId(anInvalidShowcaseId())
                             .build())
               .expectExceptionDetails(allOf(
                       aCommandErrorDetailsWithErrorCode(is(ShowcaseCommandErrorCode.INVALID_COMMAND)),
                       aCommandErrorDetailsWithErrorMessage(is("Given command is not valid")),
                       aCommandErrorDetailsWithMetaData(allOf(aMapWithSize(1), hasKey("showcaseId")))));
    }

    @Test
    void startShowcase_finishedShowcase_throwsShowcaseCommandExceptionWithIllegalStateError() {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGivenCurrentTime(startTime)
               .andGiven(ShowcaseStartedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .duration(duration)
                                 .startedAt(fixture.currentTime())
                                 .build())
               .andGivenCurrentTime(startTime.plus(duration))
               .andGiven(ShowcaseFinishedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .finishedAt(fixture.currentTime())
                                 .build())
               .when(StartShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionMessage("Showcase is finished already")
               .expectExceptionDetails(
                       ShowcaseCommandErrorDetails
                               .builder()
                               .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                               .errorMessage("Showcase is finished already")
                               .build());
    }

    @Test
    void startShowcase_nonExistingShowcase_throwsShowcaseCommandExceptionWithInvalidCommandError() {
        fixture.givenNoPriorActivity()
               .when(StartShowcaseCommand
                             .builder()
                             .showcaseId(aShowcaseId())
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionDetails(allOf(
                       aCommandErrorDetailsWithErrorCode(is(ShowcaseCommandErrorCode.NOT_FOUND)),
                       aCommandErrorDetailsWithErrorMessage(is("No showcase with given ID")),
                       aCommandErrorDetailsWithMetaData(anEmptyMap())));
    }

    @Test
    void finishShowcase_validCommand_emitsShowcaseFinishedEvent_updatesState() {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGivenCurrentTime(startTime)
               .andGiven(ShowcaseStartedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .duration(duration)
                                 .startedAt(fixture.currentTime())
                                 .build())
               .andGivenCurrentTime(startTime.plus(duration))
               .when(FinishShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectEvents(ShowcaseFinishedEvent
                                     .builder()
                                     .showcaseId(showcaseId)
                                     .finishedAt(fixture.currentTime())
                                     .build())
               .expectState(it -> {
                   assertThat(it.getStatus()).isEqualTo(ShowcaseStatus.FINISHED);
                   assertThat(it.getFinishedAt()).isNotNull();
               });
    }

    @Test
    void finishShowcase_alreadyFinishedShowcase_succeedsWithNoEvents() {
        val showcaseId = aShowcaseId();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(fixture.currentTime()))
                              .duration(aShowcaseDuration())
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGiven(ShowcaseFinishedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .finishedAt(fixture.currentTime())
                                 .build())
               .when(FinishShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectNoEvents();
    }

    @Test
    void finishShowcase_invalidCommand_throwsShowcaseCommandExceptionWithInvalidCommandError() {
        fixture.givenNoPriorActivity()
               .when(FinishShowcaseCommand
                             .builder()
                             .showcaseId(anInvalidShowcaseId())
                             .build())
               .expectExceptionDetails(allOf(
                       aCommandErrorDetailsWithErrorCode(is(ShowcaseCommandErrorCode.INVALID_COMMAND)),
                       aCommandErrorDetailsWithErrorMessage(is("Given command is not valid")),
                       aCommandErrorDetailsWithMetaData(allOf(aMapWithSize(1), hasKey("showcaseId")))));
    }

    @Test
    void finishShowcase_nonExistingShowcase_throwsShowcaseCommandExceptionWithInvalidCommandError() {
        fixture.givenNoPriorActivity()
               .when(FinishShowcaseCommand
                             .builder()
                             .showcaseId(aShowcaseId())
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionDetails(allOf(
                       aCommandErrorDetailsWithErrorCode(is(ShowcaseCommandErrorCode.NOT_FOUND)),
                       aCommandErrorDetailsWithErrorMessage(is("No showcase with given ID")),
                       aCommandErrorDetailsWithMetaData(anEmptyMap())));
    }

    @Test
    void finishShowcase_notStartedShowcase_throwsShowcaseCommandExceptionWithIllegalStateError() {
        val showcaseId = aShowcaseId();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(fixture.currentTime()))
                              .duration(aShowcaseDuration())
                              .scheduledAt(fixture.currentTime())
                              .build())
               .when(FinishShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectException(ShowcaseCommandException.class)
               .expectExceptionMessage("Showcase must be started first")
               .expectExceptionDetails(
                       ShowcaseCommandErrorDetails
                               .builder()
                               .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                               .errorMessage("Showcase must be started first")
                               .build());
    }

    @Test
    void removeShowcase_scheduledShowcase_emitsShowcaseRemovedEvent_marksDeleted() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(title)
                              .startTime(aShowcaseStartTime(fixture.currentTime()))
                              .duration(aShowcaseDuration())
                              .scheduledAt(fixture.currentTime())
                              .build())
               .when(RemoveShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectEvents(ShowcaseRemovedEvent
                                     .builder()
                                     .showcaseId(showcaseId)
                                     .removedAt(fixture.currentTime())
                                     .build())
               .expectMarkedDeleted();

        verify(showcaseTitleReservation).delete(title);
        verifyNoMoreInteractions(showcaseTitleReservation);
    }

    @Test
    void removeShowcase_startedShowcase_emitsShowcaseFinishedEvent_publishesShowcaseRemovedEvent_updatesState_marksDeleted() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(title)
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGivenCurrentTime(startTime)
               .andGiven(ShowcaseStartedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .duration(duration)
                                 .startedAt(fixture.currentTime())
                                 .build())
               .andGivenCurrentTime(startTime.plus(duration.dividedBy(2)))
               .when(RemoveShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectEvents(
                       ShowcaseFinishedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .finishedAt(fixture.currentTime())
                               .build(),
                       ShowcaseRemovedEvent
                               .builder()
                               .showcaseId(showcaseId)
                               .removedAt(fixture.currentTime())
                               .build())
               .expectState(it -> {
                   assertThat(it.getStatus()).isEqualTo(ShowcaseStatus.FINISHED);
                   assertThat(it.getFinishedAt()).isNotNull();
               })
               .expectMarkedDeleted();

        verify(showcaseTitleReservation).delete(title);
        verifyNoMoreInteractions(showcaseTitleReservation);
    }

    @Test
    void removeShowcase_finishedShowcase_emitsShowcaseRemovedEvent_marksDeleted() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(title)
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGivenCurrentTime(startTime)
               .andGiven(ShowcaseStartedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .duration(duration)
                                 .startedAt(fixture.currentTime())
                                 .build())
               .andGivenCurrentTime(startTime.plus(duration))
               .andGiven(ShowcaseFinishedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .finishedAt(fixture.currentTime())
                                 .build())
               .when(RemoveShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectEvents(ShowcaseRemovedEvent
                                     .builder()
                                     .showcaseId(showcaseId)
                                     .removedAt(fixture.currentTime())
                                     .build())
               .expectMarkedDeleted();

        verify(showcaseTitleReservation).delete(title);
        verifyNoMoreInteractions(showcaseTitleReservation);
    }

    @Test
    void removeShowcase_alreadyRemovedShowcase_succeedsWithNoEvents() {
        val showcaseId = aShowcaseId();
        val title = aShowcaseTitle();
        val startTime = aShowcaseStartTime(fixture.currentTime());
        val duration = aShowcaseDuration();

        fixture.given(ShowcaseScheduledEvent
                              .builder()
                              .showcaseId(showcaseId)
                              .title(title)
                              .startTime(startTime)
                              .duration(duration)
                              .scheduledAt(fixture.currentTime())
                              .build())
               .andGivenCurrentTime(startTime)
               .andGiven(ShowcaseStartedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .duration(duration)
                                 .startedAt(fixture.currentTime())
                                 .build())
               .andGivenCurrentTime(startTime.plus(duration))
               .andGiven(ShowcaseFinishedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .finishedAt(fixture.currentTime())
                                 .build())
               .andGiven(ShowcaseRemovedEvent
                                 .builder()
                                 .showcaseId(showcaseId)
                                 .removedAt(fixture.currentTime())
                                 .build())
               .when(RemoveShowcaseCommand
                             .builder()
                             .showcaseId(showcaseId)
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectNoEvents();

        verifyNoInteractions(showcaseTitleReservation);
    }

    @Test
    void removeShowcase_nonExistingShowcase_succeedsWithNoEvents() {
        fixture.givenNoPriorActivity()
               .when(RemoveShowcaseCommand
                             .builder()
                             .showcaseId(aShowcaseId())
                             .build())
               .expectSuccessfulHandlerExecution()
               .expectNoEvents();
    }

    @Test
    void removeShowcase_invalidCommand_throwsShowcaseCommandExceptionWithInvalidCommandError() {
        fixture.givenNoPriorActivity()
               .when(RemoveShowcaseCommand
                             .builder()
                             .showcaseId(anInvalidShowcaseId())
                             .build())
               .expectExceptionDetails(allOf(
                       aCommandErrorDetailsWithErrorCode(is(ShowcaseCommandErrorCode.INVALID_COMMAND)),
                       aCommandErrorDetailsWithErrorMessage(is("Given command is not valid")),
                       aCommandErrorDetailsWithMetaData(allOf(aMapWithSize(1), hasKey("showcaseId")))));
    }
}
