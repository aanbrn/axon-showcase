package showcase.command;

import lombok.val;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.dbscheduler.DbSchedulerDeadlineManager;
import org.axonframework.eventhandling.EventBus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;

@SpringBootTest(webEnvironment = NONE)
@Testcontainers(parallel = true)
@DirtiesContext
@TestPropertySource(properties = {
        "axon.kafka.publisher.enabled=false",
        "showcase.command.validation-enabled=false"
})
@DisplayName("Showcase saga deadline integration tests")
class ShowcaseSagaDeadlinesIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> dbEvents =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"));

    @Autowired
    CommandGateway commandGateway;

    @Autowired
    EventBus eventBus;

    @Autowired
    DeadlineManager deadlineManager;

    @Test
    @DisplayName("The saga deadline manager is the persistent db-scheduler implementation")
    void deadlineManager_isRealDbSchedulerDeadlineManager() {
        assertThat(deadlineManager).isInstanceOf(DbSchedulerDeadlineManager.class);
    }

    @Test
    @DisplayName("A scheduled showcase is started and finished automatically by the saga deadlines")
    void scheduledShowcase_sagaDeadlines_startAndFinishAutomatically() throws Exception {
        val showcaseId = aShowcaseId();
        val startTime = Instant.now();
        val duration = Duration.ofSeconds(1);

        val startedFuture = new CompletableFuture<ShowcaseStartedEvent>();
        val finishedFuture = new CompletableFuture<ShowcaseFinishedEvent>();

        try (val ignored = eventBus.subscribe(events -> events.forEach(event -> {
            val payload = event.getPayload();
            if (payload instanceof ShowcaseStartedEvent started && showcaseId.equals(started.showcaseId())) {
                startedFuture.complete(started);
            }
            if (payload instanceof ShowcaseFinishedEvent finished && showcaseId.equals(finished.showcaseId())) {
                finishedFuture.complete(finished);
            }
        }))) {
            commandGateway.sendAndWait(
                    ScheduleShowcaseCommand
                            .builder()
                            .showcaseId(showcaseId)
                            .title(aShowcaseTitle())
                            .startTime(startTime)
                            .duration(duration)
                            .build());

            assertThat(startedFuture.get(15, TimeUnit.SECONDS).startedAt())
                    .isAfterOrEqualTo(startTime);

            assertThat(
                    finishedFuture
                            .get(duration.plusSeconds(60).toSeconds(), TimeUnit.SECONDS)
                            .finishedAt())
                    .isAfterOrEqualTo(startTime.plus(duration));
        }
    }
}
