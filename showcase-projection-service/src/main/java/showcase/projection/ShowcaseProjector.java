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
import org.springframework.data.elasticsearch.ResourceNotFoundException;
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
@ProcessingGroup("projector")
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
        try {
            openSearchTemplate.index(
                    new IndexQueryBuilder()
                            .withObject(
                                    ShowcaseEntity
                                            .builder()
                                            .showcaseId(event.getShowcaseId())
                                            .title(event.getTitle())
                                            .startTime(event.getStartTime())
                                            .duration(event.getDuration())
                                            .status(ShowcaseStatus.SCHEDULED)
                                            .scheduledAt(event.getScheduledAt())
                                            .build())
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
        try {
            openSearchTemplate.update(
                    ShowcaseEntity
                            .builder()
                            .showcaseId(event.getShowcaseId())
                            .duration(event.getDuration())
                            .status(ShowcaseStatus.STARTED)
                            .startedAt(event.getStartedAt())
                            .build(),
                    showcaseIndex);

            log.debug("Started showcase projected: {}", event);
        } catch (ResourceNotFoundException e) {
            log.warn("On started event, showcase with ID {} is missing", event.getShowcaseId());
        } catch (Exception e) {
            log.error("On started event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }

    @EventHandler
    void on(@NonNull ShowcaseFinishedEvent event) {
        try {
            openSearchTemplate.update(
                    ShowcaseEntity
                            .builder()
                            .showcaseId(event.getShowcaseId())
                            .status(ShowcaseStatus.FINISHED)
                            .finishedAt(event.getFinishedAt())
                            .build(),
                    showcaseIndex);

            log.debug("Finished showcase projected: {}", event);
        } catch (ResourceNotFoundException e) {
            log.warn("On finished event, showcase with ID {} is missing", event.getShowcaseId());
        } catch (Exception e) {
            log.error("On finished event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }

    @EventHandler
    void on(@NonNull ShowcaseRemovedEvent event) {
        try {
            val response =
                    openSearchTemplate.execute(
                            client -> client.delete(DeleteRequest.of(
                                    builder -> builder.id(event.getShowcaseId())
                                                      .routing(event.getShowcaseId())
                                                      .index(showcaseIndex.getIndexName()))));
            if (response.result() == Result.NotFound) {
                log.warn("On removed event, showcase with ID {} is missing", event.getShowcaseId());
                return;
            }

            log.debug("Removed showcase projected: {}", event);
        } catch (Exception e) {
            log.error("On removed event, failed to project showcase with ID {}", event.getShowcaseId(), e);
            throw e;
        }
    }
}
