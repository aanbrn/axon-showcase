package showcase.projection;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.opensearch.spring.boot.autoconfigure.RestClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.Optional;

/**
 * Application entry point for the showcase projection service.
 *
 * <p>Boots the Spring application and declares the beans wiring the Kafka message converter and the OpenSearch
 * REST client.
 */
@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties({ KafkaProperties.class, ShowcaseProjectorProperties.class })
class ShowcaseProjectionApplication {
    /**
     * Application entry point that disables the AxonIQ console message and starts the Spring context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseProjectionApplication.class, args);
    }

    /**
     * Builds the Kafka message converter used to deserialize consumed event messages.
     *
     * @param eventSerializer the Axon event serializer
     * @param configuration   the Axon configuration providing the upcaster chain
     * @return the configured Kafka message converter
     */
    @Bean
    KafkaMessageConverter<String, byte[]> kafkaMessageConverter(
            @Qualifier("eventSerializer") Serializer eventSerializer,
            org.axonframework.config.Configuration configuration) {
        return DefaultKafkaMessageConverter
                       .builder()
                       .serializer(eventSerializer)
                       .upcasterChain(Optional.ofNullable(configuration.upcasterChain())
                                              .orElseGet(EventUpcasterChain::new))
                       .build();
    }

    /**
     * Customizes the OpenSearch REST client connection pooling and idle connection eviction.
     *
     * @param maxConnections         the maximum total connections, {@code 0} keeps the client default
     * @param maxConnectionsPerRoute the maximum connections per route
     * @param evictIdleConnections   the duration after which idle connections are evicted
     * @return the customizer for the REST client builder
     */
    @Bean
    RestClientBuilderCustomizer openSearchRestClientBuilderCustomizer(
            @Value("${opensearch.max-connections}") int maxConnections,
            @Value("${opensearch.max-connections-per-route}") int maxConnectionsPerRoute,
            @Value("${opensearch.evict-idle-connections}") Duration evictIdleConnections) {
        return restClientBuilder -> restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (maxConnections > 0) {
                httpClientBuilder.setConnectionManager(
                        PoolingAsyncClientConnectionManagerBuilder
                                .create()
                                .setMaxConnTotal(maxConnections)
                                .setMaxConnPerRoute(maxConnectionsPerRoute)
                                .build());
            }
            return httpClientBuilder.evictIdleConnections(TimeValue.of(evictIdleConnections));
        });
    }

    /**
     * Registers the Blackbird Jackson module for faster reflective serialization.
     *
     * @return the customizer applying the Blackbird module to the object mapper
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }
}
