// SPDX-License-Identifier: MIT
package showcase.api.events;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.val;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.test.KafkaTestPublisher;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(parallel = true)
@DirtiesContext
@DisplayName("Showcase live event stream integration tests")
class ShowcaseLiveEventStreamIT {

    private static final ParameterizedTypeReference<ServerSentEvent<ShowcaseEventDto>> SHOWCASE_EVENT_STREAM_TYPE =
            new ParameterizedTypeReference<>() {};

    @Container
    @ServiceConnection
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version"))
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private KafkaPublisher<?, ?> kafkaPublisher;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient webClient;

    private KafkaTestPublisher<ShowcaseEvent> kafkaTestPublisher;

    @BeforeEach
    void setUp() {
        webClient = webClientBuilder.baseUrl("http://localhost:" + port).build();
        kafkaTestPublisher = KafkaTestPublisher.<ShowcaseEvent>builder()
                .kafkaPublisher(kafkaPublisher)
                .aggregateType("ShowcaseAggregate")
                .aggregateIdentifierExtractor(ShowcaseEvent::showcaseId)
                .build();
    }

    @Test
    @DisplayName("A scheduled event published to Kafka is streamed over SSE")
    void scheduledEvent_publishedToKafka_isStreamedOverSse() {
        val showcaseId = "1";
        val event = ShowcaseScheduledEvent.builder()
                .showcaseId(showcaseId)
                .title("Demo")
                .startTime(Instant.parse("2026-09-02T10:00:00Z"))
                .duration(Duration.ofMinutes(5))
                .scheduledAt(Instant.parse("2026-09-02T09:00:00Z"))
                .build();

        val received = openEventStream();

        kafkaTestPublisher.publishEvent(event);

        await().atMost(Duration.ofSeconds(30)).until(() -> receivedScheduledEvent(received, showcaseId));
    }

    @Test
    @DisplayName("Subsequent events are delivered on the same open SSE stream")
    void subsequentEvents_areDeliveredOnTheSameOpenStream() {
        val firstShowcaseId = "1";

        val received = openEventStream();

        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(firstShowcaseId)
                .title("First")
                .startTime(Instant.parse("2026-09-02T10:00:00Z"))
                .duration(Duration.ofMinutes(5))
                .scheduledAt(Instant.parse("2026-09-02T09:00:00Z"))
                .build());
        await().atMost(Duration.ofSeconds(30)).until(() -> receivedScheduledEvent(received, firstShowcaseId));

        val secondShowcaseId = "2";
        kafkaTestPublisher.publishEvent(ShowcaseScheduledEvent.builder()
                .showcaseId(secondShowcaseId)
                .title("Second")
                .startTime(Instant.parse("2026-09-02T11:00:00Z"))
                .duration(Duration.ofMinutes(5))
                .scheduledAt(Instant.parse("2026-09-02T10:00:00Z"))
                .build());
        await().atMost(Duration.ofSeconds(30)).until(() -> receivedScheduledEvent(received, secondShowcaseId));
    }

    private boolean receivedScheduledEvent(List<ShowcaseEventDto> events, String showcaseId) {
        for (val dto : events) {
            if (dto.showcaseId().equals(showcaseId) && dto.type().equals("SCHEDULED")) {
                return true;
            }
        }
        return false;
    }

    private List<ShowcaseEventDto> openEventStream() {
        val received = new CopyOnWriteArrayList<ShowcaseEventDto>();
        webClient
                .get()
                .uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(SHOWCASE_EVENT_STREAM_TYPE)
                .mapNotNull(ServerSentEvent::data)
                .subscribe(received::add);
        return received;
    }
}
