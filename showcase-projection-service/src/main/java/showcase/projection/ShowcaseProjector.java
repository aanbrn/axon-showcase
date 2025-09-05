package showcase.projection;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.opensearch.client.ResponseException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery.OpType;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import showcase.command.ShowcaseFinishedEvent;
import showcase.command.ShowcaseRemovedEvent;
import showcase.command.ShowcaseScheduledEvent;
import showcase.command.ShowcaseStartedEvent;

import static org.axonframework.common.ExceptionUtils.findException;

@Component
@ProcessingGroup("projectors")
@Slf4j
class ShowcaseProjector {

    private final OpenSearchTemplate openSearchTemplate;

    private final IndexCoordinates showcaseIndex;

    ShowcaseProjector(@NonNull OpenSearchTemplate openSearchTemplate) {
        this.openSearchTemplate = openSearchTemplate;
        this.showcaseIndex = openSearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
    }

    @EventHandler
    void on(@NonNull ShowcaseScheduledEvent event) {
        val scheduledShowcase =
                ShowcaseEntity
                        .builder()
                        .showcaseId(event.getShowcaseId())
                        .title(event.getTitle())
                        .startTime(event.getStartTime())
                        .duration(event.getDuration())
                        .status(ShowcaseStatus.SCHEDULED)
                        .scheduledAt(event.getScheduledAt())
                        .build();

        try {
            openSearchTemplate.index(
                    new IndexQueryBuilder()
                            .withObject(scheduledShowcase)
                            .withOpType(OpType.CREATE)
                            .build(),
                    showcaseIndex);

            log.debug("Scheduled showcase projected: {}", event);
        } catch (Exception e) {
            val responseException = findException(e, ResponseException.class);
            if (responseException.isPresent()
                        && responseException
                                   .get()
                                   .getResponse()
                                   .getStatusLine()
                                   .getStatusCode() == HttpStatus.CONFLICT.value()) {
                log.warn("On scheduled event, showcase with ID {} already exists", event.getShowcaseId());
                return;
            }

            log.error("On scheduled event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }

    @EventHandler
    void on(@NonNull ShowcaseStartedEvent event) {
        val scheduledShowcase = openSearchTemplate.get(event.getShowcaseId(), ShowcaseEntity.class, showcaseIndex);
        if (scheduledShowcase == null) {
            log.warn("On started event, showcase with ID {} is missing", event.getShowcaseId());
            return;
        }
        if (scheduledShowcase.getStatus() != ShowcaseStatus.SCHEDULED) {
            log.warn("On started event, showcase with ID {} has unexpected status {}",
                     scheduledShowcase.getShowcaseId(), scheduledShowcase.getStatus());
            return;
        }

        val startedShowcase =
                scheduledShowcase
                        .toBuilder()
                        .status(ShowcaseStatus.STARTED)
                        .startedAt(event.getStartedAt())
                        .build();

        try {
            openSearchTemplate.update(startedShowcase, showcaseIndex);

            log.debug("Started showcase projected: {}", event);
        } catch (Exception e) {
            log.error("On started event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }

    @EventHandler
    void on(@NonNull ShowcaseFinishedEvent event) {
        val startedShowcase = openSearchTemplate.get(event.getShowcaseId(), ShowcaseEntity.class, showcaseIndex);
        if (startedShowcase == null) {
            log.warn("On finished event, showcase with ID {} is missing", event.getShowcaseId());
            return;
        }
        if (startedShowcase.getStatus() != ShowcaseStatus.STARTED) {
            log.warn("On finished event, showcase with ID {} has unexpected status {}",
                     startedShowcase.getShowcaseId(), startedShowcase.getStatus());
            return;
        }

        val finishedShowcase =
                startedShowcase
                        .toBuilder()
                        .status(ShowcaseStatus.FINISHED)
                        .finishedAt(event.getFinishedAt())
                        .build();

        try {
            openSearchTemplate.update(finishedShowcase, showcaseIndex);

            log.debug("Finished showcase projected: {}", event);
        } catch (Exception e) {
            log.error("On finished event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }

    @EventHandler
    void on(@NonNull ShowcaseRemovedEvent event) {
        try {
            val request =
                    DeleteRequest.builder()
                                 .id(event.getShowcaseId())
                                 .index(showcaseIndex.getIndexName())
                                 .build();
            val response = openSearchTemplate.execute(client -> client.delete(request));
            if (response.result() == Result.NotFound) {
                log.warn("On removed event, showcase with ID {} is missing", event.getShowcaseId());
            }

            log.debug("Removed showcase projected: {}", event);
        } catch (Exception e) {
            log.error("On removed event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }
}
