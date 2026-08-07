package showcase.command;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandBusSpanFactory;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.ConsistentHashChangeListener;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.JCacheAdapter;
import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.AggregateLoadTimeSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.SnapshotterSpanFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.SQLStateResolver;
import org.axonframework.extensions.jgroups.DistributedCommandBusProperties;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.micrometer.GlobalMetricRegistry;
import org.axonframework.micrometer.MessageCountingMonitor;
import org.axonframework.micrometer.MessageTimerMonitor;
import org.axonframework.modelling.saga.repository.CachingSagaStore;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore;
import org.axonframework.modelling.saga.repository.jdbc.PostgresSagaSqlSchema;
import org.axonframework.modelling.saga.repository.jdbc.SagaSqlSchema;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.monitoring.MultiMessageMonitor;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.axonframework.tracing.SpanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.cache.CacheManager;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static showcase.command.ShowcaseCommandConstants.SAGA_ASSOCIATIONS_CACHE_NAME;
import static showcase.command.ShowcaseCommandConstants.SAGA_CACHE_NAME;
import static showcase.command.ShowcaseCommandConstants.SHOWCASE_CACHE_NAME;

/**
 * Command-side Spring Boot application for the showcase CQRS service.
 *
 * <p>Configures Axon Framework components including event sourcing, distributed command bus via JGroups, saga
 * persistence, snapshot triggers, caching, and metrics instrumentation.
 */
@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties(ShowcaseCommandProperties.class)
@EnableCaching
@Slf4j
class ShowcaseCommandApplication {
    /**
     * Application entry point that disables the AxonIQ console message and starts the Spring context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseCommandApplication.class, args);
    }

    /**
     * Custom Flyway migration strategy that optionally exits the JVM after migration completes (used in container
     * startup).
     *
     * @param commandProperties  the command service properties
     * @param applicationContext the application context used to exit the JVM
     * @return the custom migration strategy
     */
    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy(
            ShowcaseCommandProperties commandProperties,
            ApplicationContext applicationContext) {
        return flyway -> {
            flyway.migrate();

            if (commandProperties.isExitAfterFlywayMigration()) {
                log.info("Exiting after flyway migration...");

                System.exit(SpringApplication.exit(applicationContext, () -> 0));
            }
        };
    }

    /**
     * Creates the JGroups connector factory for distributed command bus, setting system properties for tunnel, bind
     * address, and Kubernetes discovery before instantiating the factory bean.
     *
     * @param properties                   the distributed command bus properties
     * @param tcpPingHosts                 the TCP ping hosts
     * @param kubePingNamespace            the Kubernetes namespace
     * @param kubePingLabels               the Kubernetes labels
     * @param messageSerializer            the message serializer
     * @param localSegment                 the local command bus segment
     * @param routingStrategy              the routing strategy
     * @param consistentHashChangeListener the consistent hash change listener
     * @param spanFactory                  the span factory
     * @return the JGroups connector factory bean
     */
    @Bean
    JGroupsConnectorFactoryBean jgroupsConnectorFactoryBean(
            DistributedCommandBusProperties properties,
            @Value("${axon.distributed.jgroups.tcp-ping.hosts}") String tcpPingHosts,
            @Value("${axon.distributed.jgroups.kube-ping.namespace}") String kubePingNamespace,
            @Value("${axon.distributed.jgroups.kube-ping.labels}") String kubePingLabels,
            @Qualifier("messageSerializer") Serializer messageSerializer,
            @Qualifier("localSegment") CommandBus localSegment,
            RoutingStrategy routingStrategy,
            ObjectProvider<ConsistentHashChangeListener> consistentHashChangeListener,
            SpanFactory spanFactory
    ) {
        System.setProperty("jgroups.tunnel.gossip_router_hosts", properties.getJgroups().getGossip().getHosts());
        System.setProperty("jgroups.bind_addr", String.valueOf(properties.getJgroups().getBindAddr()));
        System.setProperty("jgroups.bind_port", String.valueOf(properties.getJgroups().getBindPort()));
        System.setProperty("jgroups.tcpping.initial_hosts", tcpPingHosts);
        System.setProperty("KUBERNETES_NAMESPACE", kubePingNamespace);
        System.setProperty("KUBERNETES_LABELS", kubePingLabels);

        JGroupsConnectorFactoryBean jGroupsConnectorFactoryBean = new JGroupsConnectorFactoryBean();
        jGroupsConnectorFactoryBean.setClusterName(properties.getJgroups().getClusterName());
        jGroupsConnectorFactoryBean.setLocalSegment(localSegment);
        jGroupsConnectorFactoryBean.setSerializer(messageSerializer);
        jGroupsConnectorFactoryBean.setConfiguration(properties.getJgroups().getConfigurationFile());
        consistentHashChangeListener.ifAvailable(jGroupsConnectorFactoryBean::setConsistentHashChangeListener);
        jGroupsConnectorFactoryBean.setRoutingStrategy(routingStrategy);
        jGroupsConnectorFactoryBean.setSpanFactory(spanFactory);
        return jGroupsConnectorFactoryBean;
    }

    /**
     * Builds the primary {@link DistributedCommandBus} with the command router, connector, span factory, and message
     * monitor. Registers a message interceptor for command handling.
     *
     * @param axonConfiguration               the Axon configuration
     * @param commandRouter                   the command router
     * @param commandBusConnector             the command bus connector
     * @param distributedCommandBusProperties the distributed command bus properties
     * @return the primary distributed command bus
     */
    @Bean
    @Primary
    @SuppressWarnings("resource")
    public DistributedCommandBus distributedCommandBus(
            Configuration axonConfiguration,
            CommandRouter commandRouter,
            CommandBusConnector commandBusConnector,
            DistributedCommandBusProperties distributedCommandBusProperties) {
        val spanFactory = axonConfiguration.getComponent(CommandBusSpanFactory.class);
        val messagedMonitor = axonConfiguration.messageMonitor(DistributedCommandBus.class, "distributedCommandBus");
        val commandBus =
                DistributedCommandBus
                        .builder()
                        .commandRouter(commandRouter)
                        .connector(commandBusConnector)
                        .spanFactory(spanFactory)
                        .messageMonitor(messagedMonitor)
                        .build();
        commandBus.updateLoadFactor(distributedCommandBusProperties.getLoadFactor());
        commandBus.registerHandlerInterceptor(new ShowcaseCommandMessageInterceptor<>());
        return commandBus;
    }

    /**
     * Resolves SQL state codes from PostgreSQL for Axon's persistence exception handling.
     *
     * @return the SQL state persistence exception resolver
     */
    @Bean
    PersistenceExceptionResolver persistenceExceptionResolver() {
        return new SQLStateResolver();
    }

    /**
     * Creates the snapshotter that periodically snapshots aggregates to reduce event store replay overhead.
     *
     * @param configuration            the Axon configuration providing repositories
     * @param eventStore               the event store to snapshot from
     * @param transactionManager       the transaction manager
     * @param executor                 the executor used for snapshotting
     * @param parameterResolverFactory the parameter resolver factory
     * @param handlerDefinition        the handler definition
     * @param spanFactory              the snapshotter span factory
     * @return the aggregate snapshotter
     */
    @Bean
    SpringAggregateSnapshotter aggregateSnapshotter(
            Configuration configuration,
            EventStore eventStore,
            TransactionManager transactionManager,
            Executor executor,
            ParameterResolverFactory parameterResolverFactory,
            HandlerDefinition handlerDefinition,
            SnapshotterSpanFactory spanFactory) {
        return SpringAggregateSnapshotter
                       .builder()
                       .repositoryProvider(configuration::repository)
                       .eventStore(eventStore)
                       .transactionManager(transactionManager)
                       .executor(executor)
                       .parameterResolverFactory(parameterResolverFactory)
                       .handlerDefinition(handlerDefinition)
                       .spanFactory(spanFactory)
                       .build();
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
     * Configures Caffeine caches for aggregates, sagas, and saga associations with sizes and expiration policies
     * from application properties.
     *
     * @param commandProperties the command service properties
     * @return the customizer creating the Caffeine caches
     */
    @Bean
    JCacheManagerCustomizer jCacheManagerCustomizer(ShowcaseCommandProperties commandProperties) {
        return cacheManager -> {
            cacheManager.createCache(
                    SHOWCASE_CACHE_NAME,
                    new CaffeineConfiguration<>()
                            .setMaximumSize(OptionalLong.of(
                                    commandProperties
                                            .getShowcaseCache()
                                            .getMaximumSize()))
                            .setExpireAfterAccess(OptionalLong.of(
                                    commandProperties
                                            .getShowcaseCache()
                                            .getExpiresAfterAccess()
                                            .toNanos()))
                            .setExpireAfterWrite(OptionalLong.of(
                                    commandProperties
                                            .getShowcaseCache()
                                            .getExpiresAfterWrite()
                                            .toNanos())));
            cacheManager.enableStatistics(SHOWCASE_CACHE_NAME, true);

            cacheManager.createCache(
                    SAGA_CACHE_NAME,
                    new CaffeineConfiguration<>()
                            .setMaximumSize(OptionalLong.of(
                                    commandProperties
                                            .getSagaCache()
                                            .getMaximumSize()))
                            .setExpireAfterAccess(OptionalLong.of(
                                    commandProperties
                                            .getSagaCache()
                                            .getExpiresAfterAccess()
                                            .toNanos()))
                            .setExpireAfterWrite(OptionalLong.of(
                                    commandProperties
                                            .getSagaCache()
                                            .getExpiresAfterWrite()
                                            .toNanos())));
            cacheManager.enableStatistics(SAGA_CACHE_NAME, true);

            cacheManager.createCache(
                    SAGA_ASSOCIATIONS_CACHE_NAME,
                    new CaffeineConfiguration<>()
                            .setMaximumSize(OptionalLong.of(
                                    commandProperties
                                            .getSagaAssociationsCache()
                                            .getMaximumSize()))
                            .setExpireAfterAccess(OptionalLong.of(
                                    commandProperties
                                            .getSagaAssociationsCache()
                                            .getExpiresAfterAccess()
                                            .toNanos()))
                            .setExpireAfterWrite(OptionalLong.of(
                                    commandProperties
                                            .getSagaAssociationsCache()
                                            .getExpiresAfterWrite()
                                            .toNanos())));
            cacheManager.enableStatistics(SAGA_ASSOCIATIONS_CACHE_NAME, true);
        };
    }

    /**
     * Wraps the {@link ShowcaseCommandConstants#SHOWCASE_CACHE_NAME} Caffeine cache as an Axon {@link Cache}.
     *
     * @param cacheManager the JCache cache manager
     * @return the Axon cache wrapping the showcase cache
     */
    @Bean
    Cache showcaseCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SHOWCASE_CACHE_NAME));
    }

    /**
     * Wraps the {@link ShowcaseCommandConstants#SAGA_CACHE_NAME} Caffeine cache as an Axon {@link Cache}.
     *
     * @param cacheManager the JCache cache manager
     * @return the Axon cache wrapping the saga cache
     */
    @Bean
    Cache sagaCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SAGA_CACHE_NAME));
    }

    /**
     * Wraps the {@link ShowcaseCommandConstants#SAGA_ASSOCIATIONS_CACHE_NAME} Caffeine cache as an Axon {@link Cache}.
     *
     * @param cacheManager the JCache cache manager
     * @return the Axon cache wrapping the saga associations cache
     */
    @Bean
    Cache sagaAssociationsCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SAGA_ASSOCIATIONS_CACHE_NAME));
    }

    /**
     * Clears all caches whenever the consistent hash ring changes (e.g., on node join/leave).
     *
     * @param showcaseCache the showcase cache to clear
     * @return the listener clearing the cache on ring changes
     */
    @Bean
    ConsistentHashChangeListener consistentHashChangeListener(Cache showcaseCache) {
        return __ -> showcaseCache.removeAll();
    }

    /**
     * Defines the snapshot trigger that decides when aggregates should be snapshotted based on the number of events
     * since the last snapshot.
     *
     * @param snapshotter       the snapshotter used to take snapshots
     * @param commandProperties the command service properties
     * @return the snapshot trigger definition
     */
    @Bean
    SnapshotTriggerDefinition showcaseSnapshotTrigger(
            Snapshotter snapshotter, ShowcaseCommandProperties commandProperties) {
        return new AggregateLoadTimeSnapshotTriggerDefinition(
                snapshotter,
                commandProperties
                        .getShowcaseSnapshotTrigger()
                        .getLoadTimeThreshold()
                        .toMillis());
    }

    /**
     * Provides the PostgreSQL-specific SQL schema for saga persistence.
     *
     * @return the PostgreSQL saga SQL schema
     */
    @Bean
    SagaSqlSchema sagaSqlSchema() {
        return new PostgresSagaSqlSchema();
    }

    /**
     * Builds the saga store with JDBC persistence, Caffeine caching for saga data, and a separate cache for saga
     * associations.
     *
     * @param connectionProvider    the JDBC connection provider
     * @param serializer            the serializer used for saga data
     * @param schema                the saga SQL schema
     * @param sagaCache             the cache for saga data
     * @param sagaAssociationsCache the cache for saga associations
     * @return the caching saga store
     */
    @Bean
    public SagaStore<Object> sagaStore(
            ConnectionProvider connectionProvider,
            Serializer serializer,
            SagaSqlSchema schema,
            Cache sagaCache,
            Cache sagaAssociationsCache) {
        val sagaStore =
                JdbcSagaStore
                        .builder()
                        .connectionProvider(connectionProvider)
                        .sqlSchema(schema)
                        .serializer(serializer)
                        .build();
        return CachingSagaStore
                       .builder()
                       .delegateSagaStore(sagaStore)
                       .sagaCache(sagaCache)
                       .associationsCache(sagaAssociationsCache)
                       .build();
    }

    /**
     * Customizes the DB Scheduler to use virtual threads for concurrent task execution.
     *
     * @param dbSchedulerProperties the DB Scheduler properties
     * @return the customizer providing the virtual-thread executor
     */
    @Bean
    DbSchedulerCustomizer dbSchedulerCustomizer(DbSchedulerProperties dbSchedulerProperties) {
        return new DbSchedulerCustomizer() {
            @Override
            public Optional<ExecutorService> executorService() {
                return Optional.of(newFixedThreadPool(
                        dbSchedulerProperties.getThreads(),
                        Thread.ofVirtual()
                              .name(Scheduler.THREAD_PREFIX, 0)
                              .factory()));
            }
        };
    }

    /**
     * Wraps the Micrometer {@link MeterRegistry} with Axon's {@link GlobalMetricRegistry}, wiring message counting
     * and timers for the event bus.
     *
     * @param meterRegistry the Micrometer meter registry
     * @return the Axon global metric registry
     */
    @Bean
    GlobalMetricRegistry globalMetricRegistry(MeterRegistry meterRegistry) {
        return new GlobalMetricRegistry(meterRegistry) {
            @Override
            public MessageMonitor<? super EventMessage<?>> registerEventBus(
                    String eventBusName, Function<Message<?>, Iterable<Tag>> tagsBuilder) {
                return new MultiMessageMonitor<>(
                        MessageCountingMonitor.buildMonitor(eventBusName, meterRegistry, tagsBuilder),
                        MessageTimerMonitor
                                .builder()
                                .meterNamePrefix(eventBusName)
                                .meterRegistry(meterRegistry)
                                .tagsBuilder(tagsBuilder)
                                .build());
            }
        };
    }

    /**
     * Registers a {@link ShowcaseDbSchedulerMetrics} bean for exposing DB Scheduler metrics through Micrometer.
     *
     * @param meterRegistry the Micrometer meter registry
     * @return the DB Scheduler metrics bean
     */
    @Bean
    ShowcaseDbSchedulerMetrics showcaseDbSchedulerMetrics(MeterRegistry meterRegistry) {
        return ShowcaseDbSchedulerMetrics
                       .builder()
                       .meterRegistry(meterRegistry)
                       .build();
    }
}
