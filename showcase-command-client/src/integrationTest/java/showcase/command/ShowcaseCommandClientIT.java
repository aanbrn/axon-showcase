package showcase.command;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
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
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
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
@Testcontainers(parallel = true)
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
    @SuppressWarnings("resource")
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
    static void jgroupsProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.distributed.jgroups.gossip.hosts",
                     () -> "localhost[%d]".formatted(commandService.getMappedPort(12001)));
    }

    @SpringBootApplication
    static class TestApp {
    }

    @Autowired
    private ShowcaseCommandOperations commandOperations;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @BeforeEach
    void awaitUntilClusterFormed(CapturedOutput output) {
        await().untilAsserted(
                () -> assertThat(output).matches("(?s).*axon-showcase-command-service.+joined the cluster.*"));
    }

    @Test
    void scheduleShowcase_validCommand_succeeds() {
        val scheduleCommand =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build();

        commandOperations
                .schedule(scheduleCommand)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void scheduleShowcase_repeatedSchedule_succeeds() {
        val scheduleCommand =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build();

        Mono.when(commandOperations.schedule(scheduleCommand),
                  commandOperations.schedule(scheduleCommand))
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void scheduleShowcase_invalidCommand_failsWithInvalidCommandError() {
        val invalidScheduleCommand =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .title(aTooLongShowcaseTitle())
                        .startTime(Instant.now())
                        .duration(aTooShortShowcaseDuration())
                        .build();

        commandOperations
                .schedule(invalidScheduleCommand)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
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
                                     }));
    }

    @Test
    void scheduleShowcase_alreadyRemoved_failsWithIllegalStateError() {
        val showcaseId = aShowcaseId();
        val now = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(now))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .remove(RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .block();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(showcaseId)
                                  .title(aShowcaseTitle())
                                  .startTime(aShowcaseStartTime(now))
                                  .duration(aShowcaseDuration())
                                  .build())
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase is removed already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));
    }

    @Test
    void scheduleShowcase_reusedTitle_failsWithTitleInUseError() {
        val title = aShowcaseTitle();
        val now = Instant.now();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(aShowcaseId())
                                  .title(title)
                                  .startTime(aShowcaseStartTime(now))
                                  .duration(aShowcaseDuration())
                                  .build())
                .block();

        commandOperations
                .schedule(ScheduleShowcaseCommand
                                  .builder()
                                  .showcaseId(aShowcaseId())
                                  .title(title)
                                  .startTime(aShowcaseStartTime(now))
                                  .duration(aShowcaseDuration())
                                  .build())
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.TITLE_IN_USE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Given title is in use already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));
    }

    @Test
    void startShowcase_scheduledShowcase_succeeds() {
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
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void startShowcase_repeatedStart_succeeds() {
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

        val startCommand =
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();

        Mono.when(commandOperations.start(startCommand),
                  commandOperations.start(startCommand))
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void startShowcase_finishedShowcase_failsWithIllegalStateError() {
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

        commandOperations
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase is finished already");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));
    }

    @Test
    void startShowcase_nonExistingShowcase_failsWithNotFoundError() {
        val showcaseId = aShowcaseId();

        commandOperations
                .start(StartShowcaseCommand
                               .builder()
                               .showcaseId(showcaseId)
                               .build())
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.NOT_FOUND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("No showcase with given ID");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));
    }

    @Test
    void startShowcase_invalidCommand_failsWithInvalidCommandError() {
        val invalidStartCommand =
                StartShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .build();

        commandOperations
                .start(invalidStartCommand)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
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
                                     }));
    }

    @Test
    void finishShowcase_startedShowcase_succeeds() {
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
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void finishShowcase_repeatedFinish_succeeds() {
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

        commandOperations
                .finish(FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void finishShowcase_notStartedShowcase_failsWithIllegalStateError() {
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
                .finish(FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("Showcase must be started first");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));
    }

    @Test
    void finishShowcase_nonExistingShowcase_failsWithNotFoundError() {
        val showcaseId = aShowcaseId();

        commandOperations
                .finish(FinishShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .asInstanceOf(type(ShowcaseCommandErrorDetails.class))
                                     .satisfies(errorDetails -> {
                                         assertThat(errorDetails.getErrorCode())
                                                 .isEqualTo(ShowcaseCommandErrorCode.NOT_FOUND);
                                         assertThat(errorDetails.getErrorMessage())
                                                 .isEqualTo("No showcase with given ID");
                                         assertThat(errorDetails.getMetaData()).isEmpty();
                                     }));
    }

    @Test
    void finishShowcase_invalidCommand_failsWithInvalidCommandError() {
        val invalidFinishCommand =
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .build();

        commandOperations
                .finish(invalidFinishCommand)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
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
                                     }));
    }

    @Test
    void removeShowcase_existingShowcase_succeeds() {
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
                .remove(RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void removeShowcase_repeatedRemove_succeeds() {
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

        val removeCommand =
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build();

        Mono.when(commandOperations.remove(removeCommand),
                  commandOperations.remove(removeCommand))
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    void removeShowcase_nonExistingShowcase_succeeds() {
        val showcaseId = aShowcaseId();

        commandOperations
                .remove(RemoveShowcaseCommand
                                .builder()
                                .showcaseId(showcaseId)
                                .build())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void removeShowcase_invalidCommand_failsWithInvalidCommandError() {
        val invalidRemoveCommand =
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .build();

        commandOperations
                .remove(invalidRemoveCommand)
                .as(StepVerifier::create)
                .verifyErrorSatisfies(
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
                                     }));
    }
}
