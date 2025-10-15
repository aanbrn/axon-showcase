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

@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties({ KafkaProperties.class, ShowcaseProjectorProperties.class })
class ShowcaseProjectionApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseProjectionApplication.class, args);
    }

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
            if (evictIdleConnections != null) {
                httpClientBuilder.evictIdleConnections(TimeValue.of(evictIdleConnections));
            }
            return httpClientBuilder;
        });
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }
}
