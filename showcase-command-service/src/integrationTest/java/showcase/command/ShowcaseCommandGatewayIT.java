package showcase.command;

import lombok.val;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.lang.Runtime.getRuntime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

@SpringBootTest(webEnvironment = NONE)
@Testcontainers(parallel = true)
@DirtiesContext
@TestPropertySource(properties = "axon.kafka.publisher.enabled=false")
@DisplayName("Showcase command gateway integration tests")
class ShowcaseCommandGatewayIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> dbEvents =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"));

    @Autowired
    CommandGateway commandGateway;

    @Test
    @DisplayName("Scheduling a showcase with a valid command succeeds")
    void scheduleShowcase_validCommand_success() {
        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());
    }

    @Test
    @DisplayName("Scheduling a duplicate command succeeds")
    void scheduleShowcase_duplicateCommand_success() {
        val command = ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build();

        commandGateway.sendAndWait(command);

        commandGateway.sendAndWait(command);
    }

    @Test
    @DisplayName("Scheduling a showcase with an invalid command throws an exception with an invalid-command error")
    void scheduleShowcase_invalidCommand_throwsCommandExecutionExceptionWithInvalidCommandError() {
        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .title(aTooLongShowcaseTitle())
                                .startTime(Instant.now())
                                .duration(aTooShortShowcaseDuration())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Given command is not valid");
                    assertThat(errorDetails.metaData())
                            .hasSize(4)
                            .containsKeys("showcaseId", "title", "startTime", "duration");
                });
    }

    @Test
    @DisplayName("Rescheduling an existing showcase throws an exception")
    void scheduleShowcase_reschedulingCommand_throwsCommandExecutionException() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .title(aShowcaseTitle())
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Showcase cannot be rescheduled");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Scheduling a showcase with an already used title throws an exception with a title-in-use error")
    void scheduleShowcase_alreadyUsedTitle_throwsShowcaseCommandExceptionCausedByTitleInUseError() {
        val title = aShowcaseTitle();
        val scheduleTime = Instant.now();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(title)
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .title(title)
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.TITLE_IN_USE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Given title is in use already");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Scheduling an already removed showcase throws an exception with an illegal-state error")
    void scheduleShowcase_alreadyRemovedShowcase_throwsCommandExecutionExceptionWithIllegalStateError() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .title(aShowcaseTitle())
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Showcase is removed already");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Starting a showcase with a valid command succeeds")
    void startShowcase_validCommand_success() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }

    @Test
    @DisplayName("Starting an already started showcase succeeds")
    void startShowcase_alreadyStartedShowcase_success() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        CompletableFuture
                .allOf(IntStream.rangeClosed(1, getRuntime().availableProcessors())
                                .mapToObj(__ -> commandGateway.send(
                                        StartShowcaseCommand
                                                .builder()
                                                .showcaseId(showcaseId)
                                                .build()))
                                .toArray(length -> new CompletableFuture<?>[length]))
                .join();
    }

    @Test
    @DisplayName("Starting an already finished showcase throws an exception with an illegal-state error")
    void startShowcase_alreadyFinishedShowcase_throwsShowcaseCommandExceptionCausedByIllegalStateError() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        commandGateway.sendAndWait(
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Showcase is finished already");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Starting an already removed showcase throws an exception with a not-found error")
    void startShowcase_alreadyRemovedShowcase_throwsShowcaseCommandExceptionCausedByNotFoundError() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        commandGateway.sendAndWait(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Showcase is removed already");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Starting a showcase with an invalid command throws an exception with an invalid-command error")
    void startShowcase_invalidCommand_throwsCommandExecutionExceptionWithInvalidCommandError() {
        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Given command is not valid");
                    assertThat(errorDetails.metaData())
                            .hasSize(1)
                            .containsKey("showcaseId");
                });
    }

    @Test
    @DisplayName("Finishing a showcase with a valid command succeeds")
    void finishShowcase_validCommand_success() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        commandGateway.sendAndWait(
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }

    @Test
    @DisplayName("Finishing an already finished showcase succeeds")
    void finishShowcase_alreadyFinishedShowcase_success() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        CompletableFuture
                .allOf(IntStream.rangeClosed(1, getRuntime().availableProcessors())
                                .mapToObj(__ -> commandGateway.send(
                                        FinishShowcaseCommand
                                                .builder()
                                                .showcaseId(showcaseId)
                                                .build()))
                                .toArray(length -> new CompletableFuture<?>[length]))
                .join();
    }

    @Test
    @DisplayName("Finishing a showcase with an invalid command throws an exception with an invalid-command error")
    void finishShowcase_invalidCommand_throwsCommandExecutionExceptionWithInvalidCommandError() {
        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Given command is not valid");
                    assertThat(errorDetails.metaData())
                            .hasSize(1)
                            .containsKey("showcaseId");
                });
    }

    @Test
    @DisplayName("Finishing a not-started showcase throws an exception with an illegal-state error")
    void finishShowcase_notStartedShowcase_throwsShowcaseCommandExceptionCausedByIllegalStateError() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Showcase must be started first");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Finishing an already removed showcase throws an exception with a not-found error")
    void finishShowcase_alreadyRemovedShowcase_throwsShowcaseCommandExceptionCausedByNotFoundError() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Showcase is removed already");
                    assertThat(errorDetails.metaData()).isEmpty();
                });
    }

    @Test
    @DisplayName("Removing a showcase with a valid command succeeds")
    void removeShowcase_validCommand_success() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }

    @Test
    @DisplayName("Removing a showcase with an invalid command throws an exception with an invalid-command error")
    void removeShowcase_invalidCommand_throwsCommandExecutionExceptionWithInvalidCommandError() {
        assertThatThrownBy(
                () -> commandGateway.sendAndWait(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .isExactlyInstanceOf(CommandExecutionException.class)
                .asInstanceOf(type(CommandExecutionException.class))
                .extracting(CommandExecutionException::getDetails)
                .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                .isPresent()
                .get()
                .satisfies(errorDetails -> {
                    assertThat(errorDetails.errorCode()).isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                    assertThat(errorDetails.errorMessage()).isEqualTo("Given command is not valid");
                    assertThat(errorDetails.metaData())
                            .hasSize(1)
                            .containsKey("showcaseId");
                });
    }

    @Test
    @DisplayName("Removing an already removed showcase throws an exception with a not-found error")
    void removeShowcase_alreadyRemovedShowcase_throwsShowcaseCommandExceptionCausedByNotFoundError() {
        val showcaseId = aShowcaseId();

        commandGateway.sendAndWait(
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build());

        commandGateway.sendAndWait(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        commandGateway.sendAndWait(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }
}
