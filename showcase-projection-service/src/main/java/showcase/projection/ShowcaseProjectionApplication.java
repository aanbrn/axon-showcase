package showcase.projection;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.opentelemetry.api.OpenTelemetry;
import lombok.val;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.ConsumerFactory;
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource;
import org.axonframework.serialization.Serializer;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.axonframework.tracing.LoggingSpanFactory;
import org.axonframework.tracing.MultiSpanFactory;
import org.axonframework.tracing.SpanFactory;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties(ShowcaseProjectionProperties.class)
class ShowcaseProjectionApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseProjectionApplication.class, args);
    }

    @Bean
    SubscribableKafkaMessageSource<String, byte[]> subscribableKafkaMessageSource(
            KafkaProperties kafkaProperties,
            ConsumerFactory<String, byte[]> consumerFactory,
            @Value("${axon.kafka.fetcher.consumer-count}") int consumerCount,
            Fetcher<String, byte[], EventMessage<?>> fetcher,
            @Qualifier("messageSerializer") Serializer messageSerializer,
            KafkaMessageConverter<String, byte[]> messageConverter) {
        return SubscribableKafkaMessageSource
                       .<String, byte[]>builder()
                       .topics(List.of(kafkaProperties.getDefaultTopic()))
                       .groupId(kafkaProperties.getClientId())
                       .consumerFactory(consumerFactory)
                       .consumerCount(consumerCount)
                       .fetcher(fetcher)
                       .serializer(messageSerializer)
                       .messageConverter(messageConverter)
                       .autoStart()
                       .build();
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(ShowcaseProjectionProperties projectionProperties) {
        val tags = projectionProperties
                           .getMetrics()
                           .getTags()
                           .stream()
                           .<Tag>map(t -> new ImmutableTag(t.getKey(), t.getValue()))
                           .toList();
        return meterRegistry -> meterRegistry.config().commonTags(tags);
    }

    @Bean
    SpanFactory spanFactory(ShowcaseProjectionProperties projectionProperties, OpenTelemetry openTelemetry) {
        val openTelemetrySpanFactory =
                OpenTelemetrySpanFactory
                        .builder()
                        .tracer(openTelemetry.getTracer("AxonFramework-OpenTelemetry"))
                        .contextPropagators(openTelemetry.getPropagators().getTextMapPropagator())
                        .build();
        if (projectionProperties.getTracing().isLogging()) {
            return new MultiSpanFactory(List.of(LoggingSpanFactory.INSTANCE, openTelemetrySpanFactory));
        } else {
            return openTelemetrySpanFactory;
        }
    }
}
