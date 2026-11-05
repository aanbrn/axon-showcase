// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.SmartLifecycle;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Verifies the backlog sampler's observation against a real broker.
 *
 * <p>Each case drives the sampler's own {@code sample()} so the assertion follows the mechanism rather than a timer, and
 * the broker state is produced and committed by plain Kafka clients: the sampler observes offsets, so nothing here needs
 * the projector or OpenSearch. Every case gets its own topic and group, so the offsets one case leaves behind cannot
 * decide another's expectation.
 */
@Testcontainers
@DisplayName("Showcase projector backlog sampler integration tests")
class ShowcaseProjectorBacklogIT {

    /**
     * The partitions each case's topic is created with.
     */
    private static final int PARTITIONS = 2;

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version"))
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094");

    private final List<SmartLifecycle> started = new ArrayList<>();

    private String topic;

    private String group;

    @BeforeEach
    void createTopic() throws Exception {
        topic = "axon-showcase-events-" + UUID.randomUUID();
        group = "showcase-projector-" + UUID.randomUUID();
        try (var admin = admin()) {
            admin.createTopics(List.of(new NewTopic(topic, PARTITIONS, (short) 1)))
                    .all()
                    .get();
        }
    }

    @AfterEach
    void stopSamplers() {
        started.forEach(SmartLifecycle::stop);
        started.clear();
    }

    @Test
    @DisplayName("A segment the group committed reports no backlog")
    void committedSegment_reportsZero() throws Exception {
        produce(0, 2);
        consumeAndCommit(2);
        var sampler = sampler(group);

        awaitSample(sampler);

        assertThat(sampler.maxResolvedDistance()).isZero();
        assertThat(sampler.unresolvedPartitions()).isZero();
    }

    @Test
    @DisplayName("Records produced while nothing consumes report their count as the backlog")
    void unconsumedRecords_reportTheBacklog() throws Exception {
        produce(0, 2);
        consumeAndCommit(2);
        produce(0, 3);
        var sampler = sampler(group);

        awaitSample(sampler);

        assertThat(sampler.maxResolvedDistance()).isEqualTo(3L);
        assertThat(sampler.observedEpochSecond()).isPositive();
    }

    @Test
    @DisplayName("Consuming and committing the backlog drains it to zero")
    void consumedBacklog_drainsToZero() throws Exception {
        commitBaseline();
        produce(0, 5);
        var sampler = sampler(group);
        awaitSample(sampler);
        assertThat(sampler.maxResolvedDistance()).isEqualTo(5L);

        consumeAndCommit(5);
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            sampler.sample();
            assertThat(sampler.maxResolvedDistance()).isZero();
        });
    }

    @Test
    @DisplayName("A group that never committed reports unresolved partitions and no distance")
    void neverCommittedGroup_reportsUnresolvedPartitions() throws Exception {
        produce(0, 3);
        var sampler = sampler(group);

        awaitSample(sampler);

        assertThat(sampler.maxResolvedDistance()).isNull();
        assertThat(sampler.unresolvedPartitions()).isEqualTo(PARTITIONS);
        assertThat(sampler.observedEpochSecond()).isPositive();
    }

    @Test
    @DisplayName("An unreachable broker counts a failed sample and publishes no distance")
    void unreachableBroker_countsAFailureWithoutPublishing() throws Exception {
        produce(0, 1);
        var properties = properties(group);
        properties.setBootstrapServers(List.of("localhost:1"));
        var registry = new SimpleMeterRegistry();
        var sampler = new ShowcaseProjectorBacklogSampler(properties, properties(), registry);
        sampler.start();
        started.add(sampler);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            sampler.sample();
            assertThat(failedSamples(registry))
                    .as("a failed sample increments the counter")
                    .isPositive();
        });

        assertThat(sampler.maxResolvedDistance()).isNull();
        assertThat(sampler.observedEpochSecond()).isZero();
    }

    /**
     * Reads the failed-sample counter from the registry.
     *
     * @param registry the registry the sampler reports to
     * @return the counter's value, or zero when the series is absent
     */
    private static double failedSamples(SimpleMeterRegistry registry) {
        var counter = registry.find(ShowcaseProjectorBacklogSampler.FAILED).counter();
        return counter == null ? 0d : counter.count();
    }

    /**
     * Samples until the sampler completes a successful sample, so a transient admin failure is retried rather than
     * deciding the assertion.
     *
     * @param sampler the sampler to drive
     */
    private static void awaitSample(ShowcaseProjectorBacklogSampler sampler) {
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            sampler.sample();
            assertThat(sampler.observedEpochSecond())
                    .as("a successful sample moves the observation time")
                    .isPositive();
        });
    }

    /**
     * Creates and starts a sampler reading this case's topic and group.
     *
     * @param group the consumer group to sample
     * @return the started sampler
     */
    private ShowcaseProjectorBacklogSampler sampler(String group) {
        var sampler = new ShowcaseProjectorBacklogSampler(properties(group), properties(), new SimpleMeterRegistry());
        sampler.start();
        started.add(sampler);
        return sampler;
    }

    /**
     * Builds the projector configuration with a short sample timeout, so an unreachable broker fails fast.
     *
     * @return the projector properties this case uses
     */
    private static ShowcaseProjectorProperties properties() {
        var properties = new ShowcaseProjectorProperties();
        properties.getBacklog().setTimeout(Duration.ofMillis(500));
        return properties;
    }

    /**
     * Builds Kafka properties pointing at the container and this case's topic and group.
     *
     * @param group the consumer group to sample
     * @return the properties the sampler derives its admin client from
     */
    private KafkaProperties properties(String group) {
        var properties = new KafkaProperties();
        properties.setBootstrapServers(List.of(kafka.getBootstrapServers()));
        properties.setDefaultTopic(topic);
        properties.setClientId("showcase-projector-it");
        properties.getConsumer().getProperties().put(ConsumerConfig.GROUP_ID_CONFIG, group);
        return properties;
    }

    /**
     * Builds the consumer properties both committing cases read with.
     *
     * @return the consumer properties for this case's topic and group
     */
    private Properties consumerProperties() {
        var consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return consumerProperties;
    }

    /**
     * Creates an admin client over the container.
     *
     * @return the admin client
     */
    private static AdminClient admin() {
        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()));
    }

    /**
     * Produces the given number of records to one partition of this case's topic.
     *
     * @param partition the partition to produce to
     * @param count the number of records
     */
    private void produce(int partition, int count) throws Exception {
        var producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        try (var producer = new KafkaProducer<String, byte[]>(producerProperties)) {
            for (var index = 0; index < count; index++) {
                producer.send(new ProducerRecord<>(topic, partition, null, new byte[] {1}))
                        .get();
            }
            producer.flush();
        }
    }

    /**
     * Consumes the next records of one partition of this case's topic and commits the offsets synchronously.
     *
     * @param count the number of records to consume
     */
    private void consumeAndCommit(int count) {
        var consumerProperties = consumerProperties();
        var first = new TopicPartition(topic, 0);
        var second = new TopicPartition(topic, 1);
        try (var consumer = new KafkaConsumer<String, byte[]>(consumerProperties)) {
            consumer.assign(List.of(first, second));
            var consumed = 0;
            while (consumed < count) {
                var records = consumer.poll(Duration.ofSeconds(5));
                if (records.isEmpty()) {
                    throw new IllegalStateException("Produced " + count + " records but polled " + consumed);
                }
                consumed += records.count();
            }
            consumer.commitSync(Map.of(
                    first, new OffsetAndMetadata(consumer.position(first)),
                    second, new OffsetAndMetadata(consumer.position(second))));
        }
    }

    /**
     * Commits a zero offset for both partitions, so the group's backlog is observable before anything is produced.
     */
    private void commitBaseline() {
        var consumerProperties = consumerProperties();
        var first = new TopicPartition(topic, 0);
        var second = new TopicPartition(topic, 1);
        try (var consumer = new KafkaConsumer<String, byte[]>(consumerProperties)) {
            consumer.assign(List.of(first, second));
            consumer.commitSync(Map.of(first, new OffsetAndMetadata(0L), second, new OffsetAndMetadata(0L)));
        }
    }
}
