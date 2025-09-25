package showcase.projection;

import lombok.val;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.Wait;
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
    static final OpenSearchContainer<?> osViews =
            new OpenSearchContainer<>("opensearchproject/opensearch:" + System.getProperty("opensearch.image.version"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OpenSearchTemplate openSearchTemplate;

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
            showcaseIndexOperations = openSearchTemplate.indexOps(ShowcaseEntity.class);
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

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.SCHEDULED)
                                    .isPresent());
    }

    @Test
    void showcaseScheduledEvent_sameEventTwice_logsError(CapturedOutput output) {
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

        await().until(() -> output.getOut().lines().anyMatch(line -> line.matches(
                (".+(ERROR).+(On ShowcaseScheduledEvent\\(showcaseId=%1$s\\), \\[Create\\] " +
                         "\\[version_conflict_engine_exception\\] \\[%1$s\\]: version conflict, document already " +
                         "exists).+").formatted(showcaseId))));
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

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.STARTED)
                                    .isPresent());
    }

    @Test
    void showcaseStartedEvent_nonExistingShowcase_logsError(CapturedOutput output) {
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

        await().until(() -> output.getOut().lines().anyMatch(line -> line.matches(
                (".+(ERROR).+(On ShowcaseStartedEvent\\(showcaseId=%1$s\\), \\[Update\\] " +
                         "\\[document_missing_exception\\] \\[%1$s\\]: document missing)").formatted(showcaseId))));
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

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.FINISHED)
                                    .isPresent());
    }

    @Test
    void showcaseFinishedEvent_nonExistingShowcase_logsError(CapturedOutput output) {
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

        await().until(() -> output.getOut().lines().anyMatch(line -> line.matches(
                (".+(ERROR).+(On ShowcaseFinishedEvent\\(showcaseId=%1$s\\), \\[Update\\] " +
                         "\\[document_missing_exception\\] \\[%1$s\\]: document missing)").formatted(showcaseId))));
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

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.SCHEDULED)
                                    .isPresent());

        kafkaTestPublisher.publishEvent(
                ShowcaseRemovedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .removedAt(Instant.now())
                        .build());

        await().until(() -> !openSearchTemplate.exists(showcaseId, ShowcaseEntity.class));
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

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                                    .filter(showcase -> showcase.getStatus() == ShowcaseStatus.SCHEDULED)
                                    .isPresent());

        kafkaTestPublisher.publishEventTwice(
                ShowcaseRemovedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .removedAt(Instant.now())
                        .build());

        await().until(() -> output.getOut().lines().anyMatch(line -> line.matches(
                (".+(WARN).+(On ShowcaseRemovedEvent\\(showcaseId=%1$s\\), \\[Delete\\] \\[not_found\\] \\[%1$s\\]: " +
                         "document missing)").formatted(showcaseId))));
    }
}
