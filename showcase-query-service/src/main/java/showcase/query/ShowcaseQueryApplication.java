package showcase.query;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.opensearch.client.opensearch._types.HealthStatus;
import org.opensearch.data.client.osc.OpenSearchTemplate;
import org.opensearch.data.client.osc.ReactiveOpenSearchClient;
import org.opensearch.spring.boot.autoconfigure.RestClientBuilderCustomizer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;
import showcase.projection.ShowcaseEntity;

import java.time.Duration;
import java.util.List;

@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties(ShowcaseQueryProperties.class)
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
    InitializingBean opensearchIndexInitializer(
            OpenSearchTemplate openSearchTemplate,
            ShowcaseQueryProperties queryProperties,
            ApplicationContext applicationContext) {
        return () -> {
            for (val entityType : List.of(ShowcaseEntity.class)) {
                val indexOperations = openSearchTemplate.indexOps(entityType);
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

                System.exit(SpringApplication.exit(applicationContext, () -> 0));
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

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

    @Bean
    ReactiveHealthIndicator openSearchHealthIndicator(ReactiveOpenSearchClient openSearchClient) {
        return new AbstractReactiveHealthIndicator("OpenSearch health check failed") {
            @Override
            protected Mono<Health> doHealthCheck(Health.Builder builder) {
                return openSearchClient.cluster().health((b) -> b).map(response -> {
                    if (!response.timedOut()) {
                        HealthStatus status = response.status();
                        builder.status((HealthStatus.Red == status) ? Status.OUT_OF_SERVICE : Status.UP);
                        builder.withDetail("cluster_name", response.clusterName());
                        builder.withDetail("status", response.status().jsonValue());
                        builder.withDetail("number_of_nodes", response.numberOfNodes());
                        builder.withDetail("number_of_data_nodes", response.numberOfDataNodes());
                        builder.withDetail("active_primary_shards", response.activePrimaryShards());
                        builder.withDetail("active_shards", response.activeShards());
                        builder.withDetail("relocating_shards", response.relocatingShards());
                        builder.withDetail("initializing_shards", response.initializingShards());
                        builder.withDetail("unassigned_shards", response.unassignedShards());
                        builder.withDetail("delayed_unassigned_shards", response.delayedUnassignedShards());
                        builder.withDetail("number_of_pending_tasks", response.numberOfPendingTasks());
                        builder.withDetail("number_of_in_flight_fetch", response.numberOfInFlightFetch());
                        builder.withDetail("task_max_waiting_in_queue_millis", response.taskMaxWaitingInQueueMillis());
                        builder.withDetail("active_shards_percent_as_number", response.activeShardsPercentAsNumber());
                        return builder.build();
                    }
                    return builder.down().build();
                });
            }
        };
    }
}
