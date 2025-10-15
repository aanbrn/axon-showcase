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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.kagkarlsson.scheduler.ExecutorUtils.defaultThreadFactoryWithPrefix;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Runtime.getRuntime;
import static showcase.command.ShowcaseCommandConstants.SAGA_ASSOCIATIONS_CACHE_NAME;
import static showcase.command.ShowcaseCommandConstants.SAGA_CACHE_NAME;
import static showcase.command.ShowcaseCommandConstants.SHOWCASE_CACHE_NAME;

@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@EnableConfigurationProperties(ShowcaseCommandProperties.class)
@EnableCaching
@Slf4j
class ShowcaseCommandApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseCommandApplication.class, args);
    }

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

    @Bean
    PersistenceExceptionResolver persistenceExceptionResolver() {
        return new SQLStateResolver();
    }

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

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

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

    @Bean
    Cache showcaseCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SHOWCASE_CACHE_NAME));
    }

    @Bean
    Cache sagaCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SAGA_CACHE_NAME));
    }

    @Bean
    Cache sagaAssociationsCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SAGA_ASSOCIATIONS_CACHE_NAME));
    }

    @Bean
    ConsistentHashChangeListener consistentHashChangeListener(Cache showcaseCache) {
        return __ -> showcaseCache.removeAll();
    }

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

    @Bean
    SagaSqlSchema sagaSqlSchema() {
        return new PostgresSagaSqlSchema();
    }

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

    @Bean
    DbSchedulerCustomizer dbSchedulerCustomizer(DbSchedulerProperties dbSchedulerProperties) {
        return new DbSchedulerCustomizer() {
            @Override
            public Optional<ExecutorService> executorService() {
                return Optional.of(
                        new ThreadPoolExecutor(
                                min(dbSchedulerProperties.getThreads(), getRuntime().availableProcessors()),
                                max(dbSchedulerProperties.getThreads(), getRuntime().availableProcessors()),
                                60L, TimeUnit.SECONDS,
                                new LinkedBlockingQueue<>(),
                                defaultThreadFactoryWithPrefix(Scheduler.THREAD_PREFIX + "-")));
            }
        };
    }

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
}
