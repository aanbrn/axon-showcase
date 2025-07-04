package showcase.command;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.anInvalidShowcaseId;

@SpringBootTest(webEnvironment = NONE)
@DirtiesContext
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class ShowcaseCommandClientIT {

    static final Network network = Network.newNetwork();

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> dbEvents =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"))
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-db-events"))
                    .withNetwork(network)
                    .withDatabaseName("showcase-events");

    @Container
    @SuppressWarnings("resource")
    static final KafkaContainer kafka =
            new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version")) {
                @Override
                public String getBootstrapServers() {
                    return "axon-showcase-kafka:9092";
                }
            }
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-kafka"))
                    .withNetwork(network);

    @Container
    @SuppressWarnings({ "resource", "unused" })
    static final GenericContainer<?> commandService =
            new GenericContainer<>("aanbrn/axon-showcase-command-service:" + System.getProperty("project.version"))
                    .dependsOn(dbEvents, kafka)
                    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("axon-showcase-command-service"))
                    .withNetwork(network)
                    .withEnv("DB_USER", dbEvents.getUsername())
                    .withEnv("DB_PASSWORD", dbEvents.getPassword())
                    .withEnv("JGROUPS_CONFIG_FILE", "tunnel.xml")
                    .withEnv("JGROUPS_GOSSIP_AUTO_START", "true")
                    .withEnv("JGROUPS_GOSSIP_HOSTS", "axon-showcase-command-service[12001]")
                    .withExposedPorts(8080, 12001)
                    .waitingFor(Wait.forHttp("/actuator/health")
                                    .forPort(8080)
                                    .forStatusCode(200))
                    .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("axon.distributed.jgroups.gossip.hosts",
                     () -> "localhost[%d]".formatted(commandService.getMappedPort(12001)));
    }

    @SpringBootApplication
    static class TestApp {
    }

    @Autowired
    private ShowcaseCommandOperations commandOperations;

    @BeforeEach
    void awaitUntilClusterFormed(CapturedOutput output) {
        await().until(() -> output.getOut().matches("(?s).*axon-showcase-command-service.+joined.*"));
    }

    @Test
    void scheduleShowcase_validCommand_emitsShowcaseId() {
        StepVerifier
                .create(commandOperations.schedule(
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
    void scheduleShowcase_duplicateCommand_emitsShowcaseId() {
        val command = ScheduleShowcaseCommand
                              .builder()
                              .showcaseId(aShowcaseId())
                              .title(aShowcaseTitle())
                              .startTime(aShowcaseStartTime(Instant.now()))
                              .duration(aShowcaseDuration())
                              .build();

        commandOperations.schedule(command)
                         .block();

        StepVerifier
                .create(commandOperations.schedule(command))
                .expectComplete()
                .verify();
    }

    @Test
    void scheduleShowcase_invalidCommand_emitsErrorWithShowcaseCommandExceptionCausedByJSR303ViolationException() {
        StepVerifier
                .create(commandOperations.schedule(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .title(aTooLongShowcaseTitle())
                                .startTime(Instant.now())
                                .duration(aTooShortShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
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
    void scheduleShowcase_alreadyRemoved_emitsErrorWithShowcaseCommandExceptionCausedByShowcaseIdInUseError() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(scheduleTime))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .remove(RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .block();

        StepVerifier
                .create(commandOperations.schedule(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .title(aShowcaseTitle())
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .extracting(ShowcaseCommandErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseCommandErrorCode.class))
                                     .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE))
                .verify();
    }

    @Test
    void scheduleShowcase_reusedTitle_emitsErrorWithShowcaseCommandExceptionCausedByTitleInUseError() {
        val title = aShowcaseTitle();
        val scheduleTime = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(aShowcaseId())
                                  .title(title)
                                  .startTime(aShowcaseStartTime(scheduleTime))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        StepVerifier
                .create(commandOperations.schedule(
                        ScheduleShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .title(title)
                                .startTime(aShowcaseStartTime(scheduleTime))
                                .duration(aShowcaseDuration())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .extracting(ShowcaseCommandErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseCommandErrorCode.class))
                                     .isEqualTo(ShowcaseCommandErrorCode.TITLE_IN_USE))
                .verify();
    }

    @Test
    void startShowcase_scheduledShowcase_emitsNothing() {
        val showcaseId = aShowcaseId();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(Instant.now()))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        StepVerifier
                .create(commandOperations.start(
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

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(Instant.now()))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .block();

        StepVerifier
                .create(commandOperations.start(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void startShowcase_finishedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByIllegalStateError() {
        val showcaseId = aShowcaseId();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(Instant.now()))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .block();

        commandOperations
                .finish(FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .block();

        StepVerifier
                .create(commandOperations.start(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .extracting(ShowcaseCommandErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseCommandErrorCode.class))
                                     .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE))
                .verify();
    }

    @Test
    void startShowcase_nonExistingShowcase_emitsErrorWithShowcaseCommandExceptionCausedByNotFoundError() {
        StepVerifier
                .create(commandOperations.start(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .extracting(ShowcaseCommandErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseCommandErrorCode.class))
                                     .isEqualTo(ShowcaseCommandErrorCode.NOT_FOUND))
                .verify();
    }

    @Test
    void startShowcase_invalidCommand_emitsErrorWithShowcaseCommandExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandOperations.start(
                        StartShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
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
    void finishShowcase_startedShowcase_emitsNothing() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(scheduleTime))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .block();

        StepVerifier
                .create(commandOperations.finish(
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
        val scheduleTime = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(scheduleTime))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .block();

        commandOperations
                .finish(FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .block();

        StepVerifier
                .create(commandOperations.finish(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void finishShowcase_notStartedShowcase_emitsErrorWithShowcaseCommandExceptionCausedByIllegalStateError() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(scheduleTime))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        StepVerifier
                .create(commandOperations.finish(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .extracting(ShowcaseCommandErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseCommandErrorCode.class))
                                     .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE))
                .verify();
    }

    @Test
    void finishShowcase_nonExistingShowcase_emitsErrorWithShowcaseCommandExceptionCausedByNotFoundError() {
        StepVerifier
                .create(commandOperations.finish(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .extracting(ShowcaseCommandErrorDetails::getErrorCode)
                                     .asInstanceOf(type(ShowcaseCommandErrorCode.class))
                                     .isEqualTo(ShowcaseCommandErrorCode.NOT_FOUND))
                .verify();
    }

    @Test
    void finishShowcase_invalidCommand_emitsErrorWithShowcaseCommandExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandOperations.finish(
                        FinishShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
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
    void removeShowcase_existingShowcase_emitsNothing() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(scheduleTime))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        StepVerifier
                .create(commandOperations.remove(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void removeShowcase_nonExistingShowcase_emitsNothing() {
        StepVerifier
                .create(commandOperations.remove(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(aShowcaseId())
                                .build()))
                .expectComplete()
                .verify();
    }

    @Test
    void removeShowcase_invalidCommand_emitsErrorWithShowcaseCommandExceptionWithInvalidCommandError() {
        StepVerifier
                .create(commandOperations.remove(
                        RemoveShowcaseCommand
                                .builder()
                                .showcaseId(anInvalidShowcaseId())
                                .build()))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
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
}
