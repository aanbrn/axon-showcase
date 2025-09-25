package showcase.projection;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.AsyncFetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.ConsumerFactory;
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.OffsetCommitType;
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource;
import org.axonframework.serialization.Serializer;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.spring.boot.autoconfigure.RestClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.List;

@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
class ShowcaseProjectionApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseProjectionApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    Fetcher<?, ?, ?> kafkaFetcher(KafkaProperties kafkaProperties) {
        return AsyncFetcher
                       .builder()
                       .pollTimeout(kafkaProperties.getFetcher().getPollTimeout())
                       .offsetCommitType(OffsetCommitType.COMMIT_ASYNC)
                       .build();
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
    RestClientBuilderCustomizer openSearchRestClientBuilderCustomizer(
            @Value("${opensearch.max-connections}") int maxConnections,
            @Value("${opensearch.max-connections-per-route}") int maxConnectionsPerRoute,
            @Value("${opensearch.evict-expired-connections}") boolean evictExpiredConnections,
            @Value("${opensearch.evict-idle-connections}") Duration evictIdleConnections) {
        return new RestClientBuilderCustomizer() {

            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                builder.setConnectionManager(
                        PoolingAsyncClientConnectionManagerBuilder
                                .create()
                                .setMaxConnTotal(maxConnections)
                                .setMaxConnPerRoute(maxConnectionsPerRoute)
                                .build());
                if (evictExpiredConnections) {
                    builder.evictExpiredConnections();
                }
                builder.evictIdleConnections(TimeValue.of(evictIdleConnections));
            }

            @Override
            public void customize(RestClientBuilder builder) {
            }
        };
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }
}
