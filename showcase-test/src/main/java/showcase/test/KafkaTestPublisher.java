package showcase.test;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Publishes events to Kafka for testing purposes, tracking per-aggregate sequence numbers.
 *
 * @param <E> the event type published by this helper
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@NullUnmarked
public final class KafkaTestPublisher<E> {
    /**
     * The Kafka publisher sending the events.
     */
    @NonNull
    private final KafkaPublisher<?, ?> kafkaPublisher;

    /**
     * The aggregate type used for the domain events.
     */
    @NonNull
    private final String aggregateType;

    /**
     * Extracts the aggregate identifier from an event.
     */
    @NonNull
    private final Function<E, String> aggregateIdentifierExtractor;

    /**
     * Tracks the last sequence number per aggregate identifier.
     */
    private final Map<String, AtomicLong> sequenceNumbers = new ConcurrentHashMap<>();

    /**
     * Publishes a single event as a domain event message with an incremented sequence number.
     *
     * @param event the event to publish
     */
    @NullMarked
    public void publishEvent(E event) {
        val aggregateIdentifier =
                requireNonNull(aggregateIdentifierExtractor.apply(event), "Aggregate identifier is required");
        val lastSequenceNumber = sequenceNumbers.computeIfAbsent(aggregateIdentifier, __ -> new AtomicLong(-1));
        val eventMessage = new GenericDomainEventMessage<>(
                aggregateType, aggregateIdentifier, lastSequenceNumber.incrementAndGet(), event);
        new DefaultUnitOfWork<>(eventMessage).execute(() -> kafkaPublisher.send(eventMessage));
    }

    /**
     * Publishes the given event twice.
     *
     * @param event the event to publish
     */
    @NullMarked
    public void publishEventTwice(E event) {
        publishEvents(List.of(event, event));
    }

    /**
     * Publishes each of the given events as a domain event message.
     *
     * @param events the events to publish
     */
    @NullMarked
    public void publishEvents(List<? extends E> events) {
        events.forEach(this::publishEvent);
    }
}
