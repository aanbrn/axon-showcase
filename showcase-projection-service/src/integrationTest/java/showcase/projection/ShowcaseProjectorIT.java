package showcase.projection;

import lombok.val;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.test.KafkaTestPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseFinishedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
@DirtiesContext
@Testcontainers(parallel = true)
@ExtendWith(OutputCaptureExtension.class)
class ShowcaseProjectorIT {

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version"))
                    .waitingFor(Wait.forListeningPort());

    @Container
    @ServiceConnection
    static final ElasticsearchContainer esViews =
            new ElasticsearchContainer("elasticsearch:" + System.getProperty("elasticsearch.image.version"))
                    .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private KafkaPublisher<?, ?> kafkaPublisher;

    private KafkaTestPublisher<ShowcaseEvent> kafkaTestPublisher;

    private IndexOperations showcaseIndexOperations;

    @BeforeEach
    void setUp() {
        kafkaTestPublisher =
                KafkaTestPublisher
                        .<ShowcaseEvent>builder()
                        .kafkaPublisher(kafkaPublisher)
                        .aggregateType("ShowcaseAggregate")
                        .aggregateIdentifierExtractor(ShowcaseEvent::getShowcaseId)
                        .build();

        if (showcaseIndexOperations == null) {
            showcaseIndexOperations = elasticsearchOperations.indexOps(ShowcaseEntity.class);
        }
        assertThat(showcaseIndexOperations.createWithMapping()).isTrue();
    }

    @AfterEach
    void tearDown() {
        assertThat(showcaseIndexOperations.delete()).isTrue();
    }

    @Test
    void showcaseScheduledEvent_singleEvent_insertsShowcaseIntoDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        await().until(() -> Optional.ofNullable(elasticsearchOperations.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.SCHEDULED)
                                    .isPresent());
    }

    @Test
    void showcaseScheduledEvent_sameEventTwice_logsWarning(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEventTwice(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        await().until(() -> output.getOut().contains(
                "On scheduled event, showcase with ID %s already exists".formatted(showcaseId)));
    }

    @Test
    void showcaseStartedEvent_singleEvent_updatesShowcaseInDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(startTime)
                        .duration(duration)
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(startedAt)
                        .build());

        await().until(() -> Optional.ofNullable(elasticsearchOperations.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.STARTED)
                                    .isPresent());
    }

    @Test
    void showcaseStartedEvent_sameEventTwice_logsWarning(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(startTime)
                        .duration(duration)
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEventTwice(
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(startedAt)
                        .build());

        await().until(() -> output.getOut().contains(
                "On started event, showcase with ID %s has unexpected status %s"
                        .formatted(showcaseId, ShowcaseStatus.STARTED)));
    }

    @Test
    void showcaseStartedEvent_nonExistingShowcase_logsWarning(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();
        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(startedAt)
                        .build());

        await().until(() -> output.getOut().contains(
                "On started event, showcase with ID %s is missing".formatted(showcaseId)));
    }

    @Test
    void showcaseFinishedEvent_singleEvent_updatesShowcaseInDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(startTime)
                        .duration(duration)
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(startedAt)
                        .build());

        val finishedAt = aShowcaseFinishedAt(startedAt, duration);

        kafkaTestPublisher.publishEvent(
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(finishedAt)
                        .build());

        await().until(() -> Optional.ofNullable(elasticsearchOperations.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.FINISHED)
                                    .isPresent());
    }

    @Test
    void showcaseFinishedEvent_sameEventTwice_logsWarning(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(startTime)
                        .duration(duration)
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(duration)
                        .startedAt(startedAt)
                        .build());

        val finishedAt = aShowcaseFinishedAt(startedAt, duration);

        kafkaTestPublisher.publishEventTwice(
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(finishedAt)
                        .build());

        await().until(() -> output.getOut().contains(
                "On finished event, showcase with ID %s has unexpected status %s"
                        .formatted(showcaseId, ShowcaseStatus.FINISHED)));
    }

    @Test
    void showcaseFinishedEvent_nonExistingShowcase_logsWarning(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();
        val startedAt = aShowcaseStartedAt(startTime);
        val finishedAt = aShowcaseFinishedAt(startedAt, duration);

        kafkaTestPublisher.publishEvent(
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(finishedAt)
                        .build());

        await().until(() -> output.getOut().contains(
                "On finished event, showcase with ID %s is missing".formatted(showcaseId)));
    }

    @Test
    void showcaseRemovedEvent_singleEvent_deletesShowcaseFromDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        await().until(() -> Optional.ofNullable(elasticsearchOperations.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.SCHEDULED)
                                    .isPresent());

        kafkaTestPublisher.publishEvent(
                ShowcaseRemovedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        await().until(() -> Optional.ofNullable(elasticsearchOperations.get(showcaseId, ShowcaseEntity.class))
                                    .isEmpty());
    }

    @Test
    void showcaseRemovedEvent_sameEventTwice_logsWarning(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(
                ShowcaseScheduledEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(scheduleTime))
                        .duration(aShowcaseDuration())
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                        .build());

        await().until(() -> Optional.ofNullable(elasticsearchOperations.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.SCHEDULED)
                                    .isPresent());

        kafkaTestPublisher.publishEventTwice(
                ShowcaseRemovedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .build());

        await().until(() -> output.getOut().contains(
                "On removed event, showcase with ID %s is missing".formatted(showcaseId)));
    }
}
