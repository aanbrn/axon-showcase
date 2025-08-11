package showcase.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.tracing.LoggingSpanFactory;
import org.axonframework.tracing.MultiSpanFactory;
import org.axonframework.tracing.SpanFactory;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import showcase.projection.ShowcaseEntity;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static showcase.query.ShowcaseQueryConstants.SHOWCASES_CACHE_NAME;

@SpringBootApplication
@EnableConfigurationProperties(ShowcaseQueryProperties.class)
@EnableCaching
@Slf4j
class ShowcaseQueryApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseQueryApplication.class, args);
    }

    @Bean
    @Order(0)
    @ConditionalOnProperty(
            prefix = "showcase.query",
            name = "index-initialization-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    InitializingBean elasticsearchIndexInitializer(
            ElasticsearchOperations elasticsearchOperations,
            ShowcaseQueryProperties queryProperties) {
        return () -> {
            for (val entityType : List.of(ShowcaseEntity.class)) {
                val indexOperations = elasticsearchOperations.indexOps(entityType);
                val indexName = indexOperations.getIndexCoordinates().getIndexName();

                log.info("Initializing index \"{}\"...", indexName);

                if (indexOperations.exists()) {
                    log.info("Index \"{}\" already exists, so putting mapping only...", indexName);

                    indexOperations.putMapping();
                } else {
                    log.info("Index \"{}\" does not exist yet, so creating with mapping...", indexName);

                    indexOperations.createWithMapping();
                }

                log.info("Successfully initialized index \"{}\"", indexName);
            }

            if (queryProperties.isExitAfterIndexInitialization()) {
                log.info("Exiting after index initialization...");

                System.exit(0);
            }
        };
    }

    @Bean
    @SuppressWarnings("resource")
    InitializingBean queryBusCustomizer(QueryBus queryBus) {
        return () -> queryBus.registerHandlerInterceptor(new ShowcaseQueryMessageInterceptor<>());
    }

    @Bean
    QueryMessageRequestMapper queryMessageRequestMapper(@Qualifier("messageSerializer") Serializer messageSerializer) {
        return new QueryMessageRequestMapper(messageSerializer);
    }

    @Bean
    ClientResourcesBuilderCustomizer redisClientResourcesBuilderCustomizer(
            ObservationRegistry observationRegistry,
            RedisProperties redisProperties) {
        return builder -> builder.tracing(new MicrometerTracing(observationRegistry, redisProperties.getClientName()));
    }

    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            ShowcaseQueryProperties queryProperties,
            ObjectMapper objectMapper) {
        return builder -> {
            val showcaseCacheSettings =
                    requireNonNull(queryProperties.getCaches().get(SHOWCASES_CACHE_NAME),
                                   "Settings for '%s' cache is missing".formatted(SHOWCASES_CACHE_NAME));

            builder.withCacheConfiguration(
                           SHOWCASES_CACHE_NAME,
                           RedisCacheConfiguration
                                   .defaultCacheConfig()
                                   .entryTtl(showcaseCacheSettings.getTimeToLive())
                                   .enableTimeToIdle()
                                   .serializeValuesWith(SerializationPair.fromSerializer(
                                           new Jackson2JsonRedisSerializer<>(objectMapper, Showcase.class))))
                   .enableStatistics();
        };
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(ShowcaseQueryProperties queryProperties) {
        val tags = queryProperties
                           .getMetrics()
                           .getTags()
                           .stream()
                           .<Tag>map(t -> new ImmutableTag(t.getKey(), t.getValue()))
                           .toList();
        return meterRegistry -> meterRegistry.config().commonTags(tags);
    }

    @Bean
    SpanFactory spanFactory(ShowcaseQueryProperties queryProperties, OpenTelemetry openTelemetry) {
        val openTelemetrySpanFactory =
                OpenTelemetrySpanFactory
                        .builder()
                        .tracer(openTelemetry.getTracer("AxonFramework-OpenTelemetry"))
                        .contextPropagators(openTelemetry.getPropagators().getTextMapPropagator())
                        .build();
        if (queryProperties.getTracing().isLogging()) {
            return new MultiSpanFactory(List.of(LoggingSpanFactory.INSTANCE, openTelemetrySpanFactory));
        } else {
            return openTelemetrySpanFactory;
        }
    }
}
