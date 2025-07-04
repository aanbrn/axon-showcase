package showcase.command;

import lombok.val;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
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
class ShowcaseCommandGatewayIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> dbEvents =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"));

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    ReactorCommandGateway commandGateway;

    @Test
    void scheduleShowcase_validCommand_emitsNothing() {
        StepVerifier
                .create(commandGateway.send(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .title(aShowcaseTitle())
                                .startTime(aShowcaseStartTime(Instant.now()))
                                .duration(aShowcaseDuration())
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void scheduleShowcase_duplicateCommand_emitsNothing() {
        val command = ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build();

        commandGateway.send(command)
                      .block();

        StepVerifier
                .create(commandGateway.send(command))
                .expectComplete()
                .verify();
    }

    @Test
    void scheduleShowcase_invalidCommand_emitsErrorWithCommandExecutionExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandGateway.send(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .title(aTooLongShowcaseTitle())
                                .startTime(Instant.now())
                                .duration(aTooShortShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Given command is not valid");
                                         assertThat(errorDetails.getMetaData())
                                                 .hasSize(4)
                                                 .containsKeys("showcaseId", "title", "startTime", "duration");
                                     }))
                .verify();
    }

    @Test
    void scheduleShowcase_reschedulingCommand_emitsErrorWithCommandExecutionException() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .title(aShowcaseTitle())
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase cannot be rescheduled");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void scheduleShowcase_alreadyUsedTitle_emitsErrorWithShowcaseCommandExceptionCausedByTitleInUseError() {
        val title = aShowcaseTitle();
        val scheduleTime = Instant.now();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(title)
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .title(title)
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.TITLE_IN_USE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Given title is in use already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void scheduleShowcase_alreadyRemovedShowcase_emitsErrorWithCommandExecutionExceptionWithIllegalStateError() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(scheduleTime))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(RemoveShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .title(aShowcaseTitle())
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase is removed already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void startShowcase_validCommand_emitsNothing() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void startShowcase_alreadyStartedShowcase_emitsNothing() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(StartShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void startShowcase_alreadyFinishedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByIllegalStateError() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(StartShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        commandGateway
                .send(FinishShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase is finished already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void startShowcase_alreadyRemovedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByNotFoundError() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(StartShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        commandGateway
                .send(RemoveShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase is removed already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void startShowcase_invalidCommand_emitsErrorWithCommandExecutionExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandGateway.send(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Given command is not valid");
                                         assertThat(errorDetails.getMetaData())
                                                 .hasSize(1)
                                                 .containsKey("showcaseId");
                                     }))
                .verify();
    }

    @Test
    void finishShowcase_validCommand_emitsNothing() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(StartShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void finishShowcase_alreadyFinishedShowcase_emitsNothing() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(StartShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        commandGateway
                .send(FinishShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void finishShowcase_invalidCommand_emitsErrorWithCommandExecutionExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandGateway.send(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Given command is not valid");
                                         assertThat(errorDetails.getMetaData())
                                                 .hasSize(1)
                                                 .containsKey("showcaseId");
                                     }))
                .verify();
    }

    @Test
    void finishShowcase_notStartedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByIllegalStateError() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase must be started first");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void finishShowcase_alreadyRemovedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByNotFoundError() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(RemoveShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase is removed already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }))
                .verify();
    }

    @Test
    void removeShowcase_validCommand_emitsNothing() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void removeShowcase_invalidCommand_emitsErrorWithCommandExecutionExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandGateway.send(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(CommandExecutionException.class)
                                     .asInstanceOf(type(CommandExecutionException.class))
                                     .extracting(CommandExecutionException::getDetails)
                                     .asInstanceOf(optional(ShowcaseCommandErrorDetails.class))
                                     .isPresent()
                                     .get()
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Given command is not valid");
                                         assertThat(errorDetails.getMetaData())
                                                 .hasSize(1)
                                                 .containsKey("showcaseId");
                                     }))
                .verify();
    }

    @Test
    void removeShowcase_alreadyRemovedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByNotFoundError() {
        val showcaseId = aShowcaseId();

        commandGateway
                .send(ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build())
                .block();

        commandGateway
                .send(RemoveShowcaseCommand
                              .builder()
                              .showcaseId(showcaseId)
                              .build())
                .block();

        StepVerifier
                .create(commandGateway.send(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }
}
