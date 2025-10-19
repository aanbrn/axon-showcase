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
    static void jgroupsProperties(DynamicPropertyRegistry registry) {
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
        await().until(() -> output.getOut().matches("(?s).*axon-showcase-command-service.+joined the cluster.*"));
    }

    @Test
    void scheduleShowcase_validCommand_succeeds() {
        // given:
        val scheduleCommand =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build();

        // when:
        val scheduleMono = commandOperations.schedule(scheduleCommand);

        // then:
        StepVerifier
                .create(scheduleMono)
                .verifyComplete();
    }

    @Test
    void scheduleShowcase_repeatedSchedule_succeeds() {
        // given:
        val scheduleCommand =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build();

        // when:
        val doubleScheduleMono = Mono.when(
                commandOperations.schedule(scheduleCommand),
                commandOperations.schedule(scheduleCommand));

        // then:
        StepVerifier
                .create(doubleScheduleMono)
                .verifyComplete();
    }

    @Test
    void scheduleShowcase_invalidCommand_failsWithInvalidCommandError() {
        // given:
        val invalidScheduleCommand =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .title(aTooLongShowcaseTitle())
                        .startTime(Instant.now())
                        .duration(aTooShortShowcaseDuration())
                        .build();

        // when:
        val scheduleMono = commandOperations.schedule(invalidScheduleCommand);

        // then:
        StepVerifier
                .create(scheduleMono)
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
        // given:
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

        // when:
        val scheduleMono =
                commandOperations
                        .schedule(ScheduleShowcaseCommand
                                          .builder()
                                          .showcaseId(showcaseId)
                                          .title(aShowcaseTitle())
                                          .startTime(aShowcaseStartTime(now))
                                          .duration(aShowcaseDuration())
                                          .build());

        // then:
        StepVerifier
                .create(scheduleMono)
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
        // given:
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

        // when:
        val scheduleMono =
                commandOperations
                        .schedule(ScheduleShowcaseCommand
                                          .builder()
                                          .showcaseId(aShowcaseId())
                                          .title(title)
                                          .startTime(aShowcaseStartTime(now))
                                          .duration(aShowcaseDuration())
                                          .build());

        // then:
        StepVerifier
                .create(scheduleMono)
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
        // given:
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

        // when:
        val startMono =
                commandOperations
                        .start(StartShowcaseCommand
                                       .builder()
                                       .showcaseId(showcaseId)
                                       .build());

        // then:
        StepVerifier
                .create(startMono)
                .verifyComplete();
    }

    @Test
    void startShowcase_repeatedStart_succeeds() {
        // given:
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

        // when:
        val doubleStartMono = Mono.when(
                commandOperations.start(startCommand),
                commandOperations.start(startCommand));

        // then:
        StepVerifier
                .create(doubleStartMono)
                .verifyComplete();
    }

    @Test
    void startShowcase_finishedShowcase_failsWithIllegalStateError() {
        // given:
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

        // when:
        val startMono =
                commandOperations
                        .start(StartShowcaseCommand
                                       .builder()
                                       .showcaseId(showcaseId)
                                       .build());

        // then:
        StepVerifier
                .create(startMono)
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
        // given:
        val showcaseId = aShowcaseId();

        // when:
        val startMono =
                commandOperations
                        .start(StartShowcaseCommand
                                       .builder()
                                       .showcaseId(showcaseId)
                                       .build());

        // then:
        StepVerifier
                .create(startMono)
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
        // given:
        val invalidStartCommand =
                StartShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .build();

        // when:
        val startMono = commandOperations.start(invalidStartCommand);

        // then:
        StepVerifier
                .create(startMono)
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
        // given:
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

        // when:
        val finishMono =
                commandOperations
                        .finish(FinishShowcaseCommand
                                        .builder()
                                        .showcaseId(showcaseId)
                                        .build());

        // then:
        StepVerifier
                .create(finishMono)
                .verifyComplete();
    }

    @Test
    void finishShowcase_repeatedFinish_succeeds() {
        // given:
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

        // when:
        val finishMono =
                commandOperations
                        .finish(FinishShowcaseCommand
                                        .builder()
                                        .showcaseId(showcaseId)
                                        .build());

        // then:
        StepVerifier
                .create(finishMono)
                .verifyComplete();
    }

    @Test
    void finishShowcase_notStartedShowcase_failsWithIllegalStateError() {
        // given:
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

        // when:
        val finishMono =
                commandOperations
                        .finish(FinishShowcaseCommand
                                        .builder()
                                        .showcaseId(showcaseId)
                                        .build());

        // then:
        StepVerifier
                .create(finishMono)
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
        // given:
        val showcaseId = aShowcaseId();

        // when:
        val finishMono =
                commandOperations
                        .finish(FinishShowcaseCommand
                                        .builder()
                                        .showcaseId(showcaseId)
                                        .build());

        // then:
        StepVerifier
                .create(finishMono)
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
        // given:
        val invalidFinishCommand =
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .build();

        // when:
        val finishMono = commandOperations.finish(invalidFinishCommand);

        // then:
        StepVerifier
                .create(finishMono)
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
        // given:
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

        // when:
        val removeMono =
                commandOperations
                        .remove(RemoveShowcaseCommand
                                        .builder()
                                        .showcaseId(showcaseId)
                                        .build());

        // then:
        StepVerifier
                .create(removeMono)
                .verifyComplete();
    }

    @Test
    void removeShowcase_repeatedRemove_succeeds() {
        // given:
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

        // when:
        val doubleRemoveMono = Mono.when(
                commandOperations.remove(removeCommand),
                commandOperations.remove(removeCommand));

        // then:
        StepVerifier
                .create(doubleRemoveMono)
                .verifyComplete();
    }

    @Test
    void removeShowcase_nonExistingShowcase_succeeds() {
        // given:
        val showcaseId = aShowcaseId();

        // when:
        val removeMono =
                commandOperations
                        .remove(RemoveShowcaseCommand
                                        .builder()
                                        .showcaseId(showcaseId)
                                        .build());

        // then:
        StepVerifier
                .create(removeMono)
                .verifyComplete();
    }

    @Test
    void removeShowcase_invalidCommand_failsWithInvalidCommandError() {
        // given:
        val invalidRemoveCommand =
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(anInvalidShowcaseId())
                        .build();

        // when:
        val removeMono = commandOperations.remove(invalidRemoveCommand);

        // then:
        StepVerifier
                .create(removeMono)
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
