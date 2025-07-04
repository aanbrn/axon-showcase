package showcase.saga;

import com.github.kagkarlsson.scheduler.Scheduler;
import lombok.val;
import org.axonframework.deadline.dbscheduler.DbSchedulerBinaryDeadlineDetails;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import showcase.command.ShowcaseEvent;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;
import showcase.test.KafkaTestPublisher;

import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
@DirtiesContext
@Testcontainers(parallel = true)
class ShowcaseSagaApplicationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> dbSagas =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"));

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:" + System.getProperty("kafka.image.version"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaPublisher<?, ?> kafkaPublisher;

    @Autowired
    private Scheduler scheduler;

    private KafkaTestPublisher<ShowcaseEvent> kafkaTestPublisher;

    @BeforeEach
    void setUp() {
        kafkaTestPublisher =
                KafkaTestPublisher
                        .<ShowcaseEvent>builder()
                        .kafkaPublisher(kafkaPublisher)
                        .aggregateType("ShowcaseAggregate")
                        .aggregateIdentifierExtractor(ShowcaseEvent::getShowcaseId)
                        .build();
    }

    @Test
    void showcaseManagementSaga_handlesEventsAndManagesDeadlines() {
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

        await().until(() -> {
            for (val execution : scheduler.getScheduledExecutions()) {
                if (execution.getData() instanceof DbSchedulerBinaryDeadlineDetails deadlineDetails
                            && "startShowcase".equals(deadlineDetails.getD())) {
                    return true;
                }
            }
            return false;
        });

        kafkaTestPublisher.publishEvent(
                ShowcaseStartedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .duration(aShowcaseDuration())
                        .startedAt(Instant.now())
                        .build());

        await().until(() -> {
            for (val execution : scheduler.getScheduledExecutions()) {
                if (execution.getData() instanceof DbSchedulerBinaryDeadlineDetails deadlineDetails
                            && "finishShowcase".equals(deadlineDetails.getD())) {
                    return true;
                }
            }
            return false;
        });

        kafkaTestPublisher.publishEvent(
                ShowcaseFinishedEvent
                        .builder()
                        .showcaseId(showcaseId)
                        .finishedAt(Instant.now())
                        .build());

        await().until(() -> {
            for (val execution : scheduler.getScheduledExecutions()) {
                if (execution.getData() instanceof DbSchedulerBinaryDeadlineDetails deadlineDetails
                            && "finishShowcase".equals(deadlineDetails.getD())) {
                    return false;
                }
            }
            return true;
        });
    }
}
