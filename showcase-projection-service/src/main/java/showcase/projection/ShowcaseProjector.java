package showcase.projection;

import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.elasticsearch.client.ResponseException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
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

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final IndexCoordinates showcaseIndex;

    ShowcaseProjector(@NonNull ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.showcaseIndex = elasticsearchTemplate.getIndexCoordinatesFor(ShowcaseEntity.class);
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
            elasticsearchTemplate.index(
                    new IndexQueryBuilder()
                            .withObject(scheduledShowcase)
                            .withOpType(OpType.CREATE)
                            .build(),
                    showcaseIndex);

            log.info("Scheduled showcase projected: {}", event);
        } catch (Exception e) {
            val responseException = findException(e, ResponseException.class);
            if (responseException.isPresent()
                        && responseException
                                   .get()
                                   .getResponse()
                                   .getStatusLine()
                                   .getStatusCode() == HttpStatus.CONFLICT.value()) {
                log.warn("On scheduled event, showcase with ID {} already exists", event.getShowcaseId());
            } else {
                throw e;
            }
        }
    }

    @EventHandler
    @CacheEvict(cacheNames = "showcases", key = "#event.showcaseId")
    void on(@NonNull ShowcaseStartedEvent event) {
        val scheduledShowcase = elasticsearchTemplate.get(event.getShowcaseId(), ShowcaseEntity.class, showcaseIndex);
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

        elasticsearchTemplate.update(startedShowcase, showcaseIndex);

        log.info("Started showcase projected: {}", event);
    }

    @EventHandler
    @CacheEvict(cacheNames = "showcases", key = "#event.showcaseId")
    void on(@NonNull ShowcaseFinishedEvent event) {
        val startedShowcase = elasticsearchTemplate.get(event.getShowcaseId(), ShowcaseEntity.class, showcaseIndex);
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

        elasticsearchTemplate.update(finishedShowcase, showcaseIndex);

        log.info("Finished showcase projected: {}", event);
    }

    @EventHandler
    @CacheEvict(cacheNames = "showcases", key = "#event.showcaseId")
    void on(@NonNull ShowcaseRemovedEvent event) {
        val request = DeleteRequest.of(r -> r.id(event.getShowcaseId()).index(showcaseIndex.getIndexName()));
        val response = elasticsearchTemplate.execute(client -> client.delete(request));
        if (response.result() == Result.NotFound) {
            log.warn("On removed event, showcase with ID {} is missing", event.getShowcaseId());
        }

        log.info("Removed showcase projected: {}", event);
    }
}
