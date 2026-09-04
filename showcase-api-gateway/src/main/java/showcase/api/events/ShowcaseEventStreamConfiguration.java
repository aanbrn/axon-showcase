// SPDX-License-Identifier: MIT
package showcase.api.events;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.serialization.Serializer;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import showcase.command.ShowcaseEvent;

/**
 * Configuration exposing the showcase domain event stream to clients.
 *
 * <p>Consumes showcase events from the Kafka topic the command service publishes to and exposes them as a reactive
 * {@link Flux} of {@link ShowcaseEventDto}. The consumer uses its own group so the projection service is unaffected.
 */
@Configuration
@NullMarked
@Slf4j
class ShowcaseEventStreamConfiguration {
    /**
     * Builds the Kafka message converter used to deserialize consumed event messages.
     *
     * @param eventSerializer the Axon event serializer
     * @return the configured Kafka message converter
     */
    @Bean
    KafkaMessageConverter<String, byte[]> liveEventsMessageConverter(
            @Qualifier("eventSerializer") Serializer eventSerializer) {
        return DefaultKafkaMessageConverter.builder()
                .serializer(eventSerializer)
                .build();
    }

    /**
     * Exposes the live showcase event stream as a hot {@link Flux}.
     *
     * <p>Subscribes to Kafka eagerly so consumption starts at application startup (not on the first SSE client), and
     * buffers emitted events so late-subscribing clients still receive them.
     *
     * @param kafkaProperties       the Kafka properties
     * @param kafkaMessageConverter the converter used to decode Kafka messages
     * @param eventMapper           the mapper used to convert domain events to SSE DTOs
     * @return a hot {@link Flux} of showcase events
     */
    @Bean
    Flux<ShowcaseEventDto> showcaseEventStream(
            KafkaProperties kafkaProperties,
            KafkaMessageConverter<String, byte[]> kafkaMessageConverter,
            ShowcaseEventMapper eventMapper) {
        val sink = Sinks.many().replay().<ShowcaseEventDto>limit(100);
        val receiver =
                KafkaReceiver.create(ReceiverOptions.<String, byte[]>create(kafkaProperties.buildConsumerProperties())
                        .subscription(List.of(kafkaProperties.getDefaultTopic())));
        receiver.receive()
                .map(kafkaMessageConverter::readKafkaMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EventMessage::getPayload)
                .filter(ShowcaseEvent.class::isInstance)
                .map(ShowcaseEvent.class::cast)
                .mapNotNull(eventMapper::toDto)
                .subscribe(
                        event -> sink.tryEmitNext(event).orThrow(),
                        error -> log.error("Live event stream failed: {}", error.getMessage()));
        return sink.asFlux();
    }
}
