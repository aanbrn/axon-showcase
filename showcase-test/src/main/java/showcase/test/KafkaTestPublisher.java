package showcase.test;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class KafkaTestPublisher<E> {

    @NonNull
    private final KafkaPublisher<?, ?> kafkaPublisher;

    @NonNull
    private final String aggregateType;

    @NonNull
    private final Function<E, String> aggregateIdentifierExtractor;

    private final Map<String, AtomicLong> sequenceNumbers = new ConcurrentHashMap<>();

    public void publishEvent(@NonNull E event) {
        val aggregateIdentifier =
                requireNonNull(aggregateIdentifierExtractor.apply(event), "Aggregate identifier is required");
        val lastSequenceNumber = sequenceNumbers.computeIfAbsent(aggregateIdentifier, __ -> new AtomicLong(-1));
        val eventMessage = new GenericDomainEventMessage<>(
                aggregateType, aggregateIdentifier, lastSequenceNumber.incrementAndGet(), event);
        new DefaultUnitOfWork<>(eventMessage).execute(() -> kafkaPublisher.send(eventMessage));
    }

    public void publishEventTwice(@NonNull E event) {
        publishEvents(List.of(event, event));
    }

    public void publishEvents(@NonNull List<? extends E> events) {
        events.forEach(this::publishEvent);
    }
}
