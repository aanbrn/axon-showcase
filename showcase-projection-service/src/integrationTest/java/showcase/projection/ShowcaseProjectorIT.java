// SPDX-License-Identifier: MIT
package showcase.projection;

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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.val;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.opensearch.testcontainers.OpenSearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import reactor.blockhound.BlockHound;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.test.KafkaTestPublisher;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
@Testcontainers(parallel = true)
@DisplayName("Showcase projector integration tests")
class ShowcaseProjectorIT {

    private static final String EVENTS_TOPIC = "axon-showcase-events";

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version"))
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094");

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

    @Test
    @DisplayName("The service bakes its build identity and exposes the info endpoint")
    void buildIdentity_isBakedAndExposed(ApplicationContext context) {
        val build = context.getBean(BuildProperties.class);
        assertThat(build.getVersion()).isEqualTo(System.getProperty("project.version"));
        assertThat(build.getTime()).isNull();
        assertThat(context.getEnvironment().getProperty("management.endpoints.web.exposure.include"))
                .contains("info");
    }

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @BeforeEach
    void setUp() {
        kafkaTestPublisher = KafkaTestPublisher.<ShowcaseEvent>builder()
                .kafkaPublisher(kafkaPublisher)
                .aggregateType("ShowcaseAggregate")
                .aggregateIdentifierExtractor(ShowcaseEvent::showcaseId)
                .build();

        if (showcaseIndexOperations == null) {
            showcaseIndexOperations = openSearchTemplate.indexOps(ShowcaseEntity.class);
        }
        assertThat(showcaseIndexOperations.exists()).isFalse();
        assertThat(showcaseIndexOperations.createWithMapping()).isTrue();
        assertThat(showcaseIndexOperations.exists()).isTrue();
    }

    private void publishMessage(EventMessage<?> message) {
        new DefaultUnitOfWork<>(message).execute(() -> {
            @SuppressWarnings("unchecked")
            val publisher = (KafkaPublisher<String, byte[]>) kafkaPublisher;
            publisher.send(message);
        });
    }

    private AdminClient adminClient() {
        val properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(AdminClientConfig.CLIENT_ID_CONFIG, "showcase-projector-it");
        return AdminClient.create(properties);
    }

    private List<TopicPartition> topicPartitions(AdminClient admin) throws Exception {
        return admin
                .describeTopics(List.of(EVENTS_TOPIC))
                .allTopicNames()
                .get(10, TimeUnit.SECONDS)
                .get(EVENTS_TOPIC)
                .partitions()
                .stream()
                .map(partition -> new TopicPartition(EVENTS_TOPIC, partition.partition()))
                .toList();
    }

    private Map<TopicPartition, Long> logEndOffsets() throws Exception {
        try (val admin = adminClient()) {
            val partitions = topicPartitions(admin);
            val specs = partitions.stream().collect(Collectors.toMap(Function.identity(), __ -> OffsetSpec.latest()));
            return admin.listOffsets(specs).all().get(10, TimeUnit.SECONDS).entrySet().stream()
                    .collect(Collectors.toMap(
                            Entry::getKey, entry -> entry.getValue().offset()));
        }
    }

    private Map<TopicPartition, Long> committedOffsets() throws Exception {
        try (val admin = adminClient()) {
            return admin
                    .listConsumerGroupOffsets("showcase-projector")
                    .partitionsToOffsetAndMetadata()
                    .get(10, TimeUnit.SECONDS)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Entry::getKey, entry -> entry.getValue().offset()));
        }
    }

    private void awaitConsumed(TopicPartition partition, long endOffset) {
        await().untilAsserted(() ->
                assertThat(committedOffsets().getOrDefault(partition, 0L)).isGreaterThanOrEqualTo(endOffset));
    }

    private TopicPartition advancedPartition(Map<TopicPartition, Long> before, Map<TopicPartition, Long> after) {
        return after.keySet().stream()
                .filter(partition -> after.get(partition) - before.getOrDefault(partition, 0L) == 2L)
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void tearDown() {
        assertThat(showcaseIndexOperations.exists()).isTrue();
        assertThat(showcaseIndexOperations.delete()).isTrue();
        assertThat(showcaseIndexOperations.exists()).isFalse();
    }

    @Test
    @DisplayName("A scheduled event inserts the showcase into the database")
    void showcaseScheduledEvent_singleEvent_insertsShowcaseIntoDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(scheduleTime))
                .duration(aShowcaseDuration())
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.SCHEDULED)
                .isPresent());
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("A redelivered scheduled event converges without an error and the stream continues")
    void showcaseScheduledEvent_sameEventTwice_convergesWithoutError(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        kafkaTestPublisher.publishEventTwice(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(startTime)
                .duration(duration)
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());
        kafkaTestPublisher.publishEvent(ShowcaseStartedEvent.builder()
                .showcaseId(showcaseId)
                .duration(duration)
                .startedAt(aShowcaseStartedAt(startTime))
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.STARTED)
                .isPresent());

        assertThat(output.getOut()).doesNotContain("ERROR");
    }

    @Test
    @DisplayName("A started event updates the showcase in the database")
    void showcaseStartedEvent_singleEvent_updatesShowcaseInDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();
        val title = aShowcaseTitle();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(title)
                .startTime(startTime)
                .duration(duration)
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(ShowcaseStartedEvent.builder()
                .showcaseId(showcaseId)
                .duration(duration)
                .startedAt(startedAt)
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.STARTED)
                .isPresent());
        assertThat(openSearchTemplate.get(showcaseId, ShowcaseEntity.class)).satisfies(showcase -> {
            assertThat(showcase.title()).isEqualTo(title);
            assertThat(showcase.startedAt()).isEqualTo(startedAt);
        });
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("A started event for a non-existing showcase logs an error")
    void showcaseStartedEvent_nonExistingShowcase_logsError(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();
        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(ShowcaseStartedEvent.builder()
                .showcaseId(showcaseId)
                .duration(duration)
                .startedAt(startedAt)
                .build());

        await().until(() -> output.getOut()
                .lines()
                .anyMatch(line -> line.matches(
                        ".+(ERROR).+Bulk item failed: \\[Update] \\[%s] document_missing_exception: .+document missing"
                                .formatted(showcaseId))));
    }

    @Test
    @DisplayName("A finished event updates the showcase in the database")
    void showcaseFinishedEvent_singleEvent_updatesShowcaseInDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(startTime)
                .duration(duration)
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(ShowcaseStartedEvent.builder()
                .showcaseId(showcaseId)
                .duration(duration)
                .startedAt(startedAt)
                .build());

        val finishedAt = aShowcaseFinishedAt(startedAt, duration);

        kafkaTestPublisher.publishEvent(ShowcaseFinishedEvent.builder()
                .showcaseId(showcaseId)
                .finishedAt(finishedAt)
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.FINISHED)
                .isPresent());
        assertThat(openSearchTemplate.get(showcaseId, ShowcaseEntity.class)).satisfies(showcase -> {
            assertThat(showcase.startedAt()).isEqualTo(startedAt);
            assertThat(showcase.duration()).isEqualTo(duration);
            assertThat(showcase.finishedAt()).isEqualTo(finishedAt);
        });
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("A finished event for a non-existing showcase logs an error")
    void showcaseFinishedEvent_nonExistingShowcase_logsError(CapturedOutput output) {
        val showcaseId = aShowcaseId();
        val startTime = aShowcaseStartTime(Instant.now());
        val duration = aShowcaseDuration();
        val startedAt = aShowcaseStartedAt(startTime);
        val finishedAt = aShowcaseFinishedAt(startedAt, duration);

        kafkaTestPublisher.publishEvent(ShowcaseFinishedEvent.builder()
                .showcaseId(showcaseId)
                .finishedAt(finishedAt)
                .build());

        await().until(() -> output.getOut()
                .lines()
                .anyMatch(line -> line.matches(
                        ".+(ERROR).+Bulk item failed: \\[Update] \\[%s] document_missing_exception: .+document missing"
                                .formatted(showcaseId))));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("A message with a non-showcase payload is reported as ignored")
    void nonShowcasePayload_isReportedAsIgnored(CapturedOutput output) {
        val message = new GenericDomainEventMessage<>("OtherAggregate", "other-1", 0L, "not-a-showcase-event");

        publishMessage(message);

        await().until(() -> output.getOut().contains("Skipped event message with payload type"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("A showcase event without aggregate headers is reported as a failure")
    void showcaseEventWithoutSequence_isReportedAsFailure(CapturedOutput output) {
        val message = GenericEventMessage.asEventMessage(ShowcaseScheduledEvent.builder()
                .showcaseId(aShowcaseId())
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(Instant.now()))
                .duration(aShowcaseDuration())
                .scheduledAt(Instant.now())
                .build());

        publishMessage(message);

        await().until(() -> output.getOut().contains("Skipped event message without an aggregate sequence"));
    }

    @Test
    @DisplayName("A created document carries its aggregate sequence")
    void showcaseScheduledEvent_singleEvent_storesTheAggregateSequence() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(scheduleTime))
                .duration(aShowcaseDuration())
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .map(ShowcaseEntity::sequence)
                .filter(sequence -> sequence == 0L)
                .isPresent());
    }

    @Test
    @DisplayName("A replayed older event does not regress the document")
    void showcaseScheduledEvent_replayedOlderEvent_doesNotRegressTheDocument() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();
        val scheduledEvent = ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(startTime)
                .duration(duration)
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build();

        val startedAt = aShowcaseStartedAt(startTime);

        kafkaTestPublisher.publishEvent(scheduledEvent);
        kafkaTestPublisher.publishEvent(ShowcaseStartedEvent.builder()
                .showcaseId(showcaseId)
                .duration(duration)
                .startedAt(startedAt)
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.STARTED)
                .isPresent());

        kafkaTestPublisher.publishEventWithSequence(
                ShowcaseStartedEvent.builder()
                        .showcaseId(showcaseId)
                        .duration(duration.plusMinutes(30))
                        .startedAt(startedAt.minusSeconds(60))
                        .build(),
                0L);
        val finishedEvent = ShowcaseFinishedEvent.builder()
                .showcaseId(showcaseId)
                .finishedAt(aShowcaseFinishedAt(startedAt, duration))
                .build();
        kafkaTestPublisher.publishEvent(finishedEvent);

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.FINISHED)
                .isPresent());
        assertThat(openSearchTemplate.get(showcaseId, ShowcaseEntity.class)).satisfies(showcase -> {
            assertThat(showcase.sequence()).isEqualTo(2L);
            assertThat(showcase.duration()).isEqualTo(duration);
            assertThat(showcase.startedAt()).isEqualTo(startedAt);
        });
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("An identically redelivered record converges without an error")
    void showcaseScheduledEvent_identicalRecordTwice_convergesWithoutError(CapturedOutput output) throws Exception {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();
        val before = logEndOffsets();

        kafkaTestPublisher.publishSameMessageTwice(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(scheduleTime))
                .duration(aShowcaseDuration())
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        await().until(() -> openSearchTemplate.exists(showcaseId, ShowcaseEntity.class));
        val after = logEndOffsets();
        val partition = advancedPartition(before, after);
        awaitConsumed(partition, after.get(partition));
        assertThat(output.getOut()).doesNotContain("ERROR");
    }

    @Test
    @DisplayName("A removed event deletes the showcase from the database")
    void showcaseRemovedEvent_singleEvent_deletesShowcaseFromDatabase() {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(scheduleTime))
                .duration(aShowcaseDuration())
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.SCHEDULED)
                .isPresent());

        kafkaTestPublisher.publishEvent(ShowcaseRemovedEvent.builder()
                .showcaseId(showcaseId)
                .removedAt(Instant.now())
                .build());

        await().until(() -> !openSearchTemplate.exists(showcaseId, ShowcaseEntity.class));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("A redelivered removed event converges without a warning or an error")
    void showcaseRemovedEvent_sameEventTwice_convergesWithoutWarningOrError(CapturedOutput output) throws Exception {
        val showcaseId = aShowcaseId();
        val scheduleTime = Instant.now();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(scheduleTime))
                .duration(aShowcaseDuration())
                .scheduledAt(aShowcaseScheduledAt(scheduleTime))
                .build());

        await().until(() -> Optional.ofNullable(openSearchTemplate.get(showcaseId, ShowcaseEntity.class))
                .filter(showcase -> showcase.status() == ShowcaseStatus.SCHEDULED)
                .isPresent());

        val before = logEndOffsets();
        kafkaTestPublisher.publishEventTwice(ShowcaseRemovedEvent.builder()
                .showcaseId(showcaseId)
                .removedAt(Instant.now())
                .build());

        await().until(() -> !openSearchTemplate.exists(showcaseId, ShowcaseEntity.class));
        val after = logEndOffsets();
        val partition = advancedPartition(before, after);
        awaitConsumed(partition, after.get(partition));
        assertThat(output.getOut()).doesNotContain("WARN", "ERROR");
    }
}
