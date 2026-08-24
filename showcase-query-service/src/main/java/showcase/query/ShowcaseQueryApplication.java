// SPDX-License-Identifier: MIT
package showcase.query;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import java.time.Duration;
import java.util.List;
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

/**
 * Application entry point for the query service.
 */
@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties(ShowcaseQueryProperties.class)
@Slf4j
class ShowcaseQueryApplication {
    /**
     * Terminates the JVM after the index is initialized when exit-after is enabled.
     */
    @FunctionalInterface
    interface ApplicationExitHandler {
        /**
         * Exits the application, closing the given context and terminating the JVM.
         *
         * @param applicationContext the application context to close
         */
        void exit(ApplicationContext applicationContext);
    }

    /**
     * Application entry point that disables the AxonIQ console message and starts the Spring context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseQueryApplication.class, args);
    }

    /**
     * Creates the index initializer that creates the OpenSearch index with its mapping on startup.
     *
     * @param openSearchTemplate the OpenSearch template used to manage the index
     * @param queryProperties    the query service properties
     * @param applicationContext the application context used to exit the JVM
     * @return an initializing bean creating the index
     */
    @Bean
    @Order(0)
    @ConditionalOnProperty(
            prefix = "showcase.query",
            name = "index-initialization-enabled",
            havingValue = "true",
            matchIfMissing = true)
    InitializingBean opensearchIndexInitializer(
            OpenSearchTemplate openSearchTemplate,
            ShowcaseQueryProperties queryProperties,
            ApplicationContext applicationContext,
            ApplicationExitHandler exitHandler) {
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

                exitHandler.exit(applicationContext);
            }
        };
    }

    /**
     * Provides the JVM exit action invoked when index initialization completes with exit-after enabled.
     *
     * @return the exit handler, closing the context and terminating the JVM by default
     */
    @Bean
    ApplicationExitHandler applicationExitHandler() {
        return applicationContext -> System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    /**
     * Registers the query message interceptor on the query bus.
     *
     * @param queryBus        the query bus to customize
     * @param queryProperties the query service properties
     * @return an initializing bean registering the interceptor
     */
    @Bean
    @SuppressWarnings("resource")
    InitializingBean queryBusCustomizer(QueryBus queryBus, ShowcaseQueryProperties queryProperties) {
        return () -> queryBus.registerHandlerInterceptor(
                new ShowcaseQueryMessageInterceptor<>(queryProperties.isValidationEnabled()));
    }

    /**
     * Creates the mapper converting query requests to query messages.
     *
     * @param messageSerializer the message serializer
     * @return the query message request mapper
     */
    @Bean
    QueryMessageRequestMapper queryMessageRequestMapper(@Qualifier("messageSerializer") Serializer messageSerializer) {
        return new QueryMessageRequestMapper(messageSerializer);
    }

    /**
     * Customizes the OpenSearch REST client with connection pool and idle-eviction settings.
     *
     * @param maxConnections         the total maximum connections, or {@code 0} to keep the default
     * @param maxConnectionsPerRoute the maximum connections per route
     * @param evictIdleConnections   the idle connection eviction duration
     * @return the REST client builder customizer
     */
    @Bean
    RestClientBuilderCustomizer openSearchRestClientBuilderCustomizer(
            @Value("${opensearch.max-connections}") int maxConnections,
            @Value("${opensearch.max-connections-per-route}") int maxConnectionsPerRoute,
            @Value("${opensearch.evict-idle-connections}") Duration evictIdleConnections) {
        return restClientBuilder -> restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (maxConnections > 0) {
                httpClientBuilder.setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(maxConnections)
                        .setMaxConnPerRoute(maxConnectionsPerRoute)
                        .build());
            }
            return httpClientBuilder.evictIdleConnections(TimeValue.of(evictIdleConnections));
        });
    }

    /**
     * Registers the Jackson Blackbird module for efficient serialization.
     *
     * @return the customizer registering the Blackbird module
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

    /**
     * Creates a reactive health indicator reporting the OpenSearch cluster status.
     *
     * @param openSearchClient the reactive OpenSearch client
     * @return the OpenSearch health indicator
     */
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
