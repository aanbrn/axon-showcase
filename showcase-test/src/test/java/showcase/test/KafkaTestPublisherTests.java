// SPDX-License-Identifier: MIT
package showcase.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import lombok.val;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.extensions.kafka.eventhandling.producer.KafkaPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("Kafka test publisher tests")
class KafkaTestPublisherTests {

    private static final String AGGREGATE_TYPE = "ShowcaseAggregate";
    private static final String AGGREGATE_ID = "aggregate-1";

    @Test
    @DisplayName("A published event is sent as a domain message with the configured aggregate type and identifier")
    void publishEvent_sendsDomainMessageWithAggregateTypeAndIdentifier() {
        val kafkaPublisher = mock(KafkaPublisher.class);
        val event = new Object();
        val publisher = publisher(kafkaPublisher, AGGREGATE_ID);

        publisher.publishEvent(event);

        val message = sentMessage(kafkaPublisher);
        assertThat(message.getType()).isEqualTo(AGGREGATE_TYPE);
        assertThat(message.getAggregateIdentifier()).isEqualTo(AGGREGATE_ID);
        assertThat(message.getSequenceNumber()).isEqualTo(0);
        assertThat(message.getPayload()).isSameAs(event);
    }

    @Test
    @DisplayName("Sequence numbers increment per aggregate identifier")
    void publishEvent_sameAggregate_incrementsSequenceNumber() {
        val kafkaPublisher = mock(KafkaPublisher.class);
        val publisher = publisher(kafkaPublisher, AGGREGATE_ID);

        publisher.publishEvent(new Object());
        publisher.publishEvent(new Object());

        val messages = sentMessages(kafkaPublisher);
        assertThat(messages).hasSize(2);
        assertThat(messages)
                .extracting(GenericDomainEventMessage::getSequenceNumber)
                .containsExactly(0L, 1L);
    }

    @Test
    @DisplayName("Sequence numbers are tracked independently per aggregate identifier")
    void publishEvent_differentAggregates_restartSequenceNumbers() {
        val kafkaPublisher = mock(KafkaPublisher.class);
        val firstAggregate = publisher(kafkaPublisher, "aggregate-1");
        val secondAggregate = publisher(kafkaPublisher, "aggregate-2");

        firstAggregate.publishEvent(new Object());
        secondAggregate.publishEvent(new Object());

        val messages = sentMessages(kafkaPublisher);
        assertThat(messages)
                .extracting(GenericDomainEventMessage::getSequenceNumber)
                .containsExactly(0L, 0L);
    }

    @Test
    @DisplayName("Publishing the same event twice sends two messages")
    void publishEventTwice_sendsTwoMessages() {
        val kafkaPublisher = mock(KafkaPublisher.class);
        val publisher = publisher(kafkaPublisher, AGGREGATE_ID);
        val event = new Object();

        publisher.publishEventTwice(event);

        val messages = sentMessages(kafkaPublisher);
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(GenericDomainEventMessage::getPayload).containsExactly(event, event);
        assertThat(messages)
                .extracting(GenericDomainEventMessage::getSequenceNumber)
                .containsExactly(0L, 1L);
    }

    @Test
    @DisplayName("Publishing a list of events sends one message per event")
    void publishEvents_sendsOneMessagePerEvent() {
        val kafkaPublisher = mock(KafkaPublisher.class);
        val publisher = publisher(kafkaPublisher, AGGREGATE_ID);
        val firstEvent = new Object();
        val secondEvent = new Object();

        publisher.publishEvents(List.of(firstEvent, secondEvent));

        val messages = sentMessages(kafkaPublisher);
        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(GenericDomainEventMessage::getPayload).containsExactly(firstEvent, secondEvent);
    }

    @Test
    @DisplayName("A null aggregate identifier from the extractor is rejected")
    void publishEvent_nullAggregateIdentifier_throwsNullPointerException() {
        val kafkaPublisher = mock(KafkaPublisher.class);
        val publisher = KafkaTestPublisher.<Object>builder()
                .kafkaPublisher(kafkaPublisher)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateIdentifierExtractor(__ -> null)
                .build();

        assertThatNullPointerException().isThrownBy(() -> publisher.publishEvent(new Object()));
    }

    private KafkaTestPublisher<Object> publisher(KafkaPublisher<?, ?> kafkaPublisher, String aggregateIdentifier) {
        return KafkaTestPublisher.<Object>builder()
                .kafkaPublisher(kafkaPublisher)
                .aggregateType(AGGREGATE_TYPE)
                .aggregateIdentifierExtractor(__ -> aggregateIdentifier)
                .build();
    }

    @SuppressWarnings("unchecked")
    private GenericDomainEventMessage<Object> sentMessage(KafkaPublisher<?, ?> kafkaPublisher) {
        val captor = ArgumentCaptor.forClass(GenericDomainEventMessage.class);
        verify(kafkaPublisher).send(captor.capture());
        return (GenericDomainEventMessage<Object>) captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<GenericDomainEventMessage<Object>> sentMessages(KafkaPublisher<?, ?> kafkaPublisher) {
        val captor = ArgumentCaptor.forClass(GenericDomainEventMessage.class);
        verify(kafkaPublisher, times(2)).send(captor.capture());
        return (List<GenericDomainEventMessage<Object>>) (List<?>) captor.getAllValues();
    }
}
