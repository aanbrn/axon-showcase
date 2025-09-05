package showcase.command;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.opentelemetry.api.OpenTelemetry;
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
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.eventsourcing.AggregateLoadTimeSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.SnapshotterSpanFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.SQLStateResolver;
import org.axonframework.extensions.jgroups.DistributedCommandBusProperties;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.axonframework.tracing.LoggingSpanFactory;
import org.axonframework.tracing.MultiSpanFactory;
import org.axonframework.tracing.SpanFactory;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.cache.CacheManager;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Executor;

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
    FlywayMigrationStrategy flywayMigrationStrategy(ShowcaseCommandProperties commandProperties) {
        return flyway -> {
            flyway.migrate();

            if (commandProperties.isExitAfterFlywayMigration()) {
                log.info("Exiting after flyway migration...");

                System.exit(0);
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
        };
    }

    @Bean
    Cache showcaseCache(CacheManager cacheManager) {
        return new JCacheAdapter(cacheManager.getCache(SHOWCASE_CACHE_NAME));
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
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(ShowcaseCommandProperties commandProperties) {
        val tags = commandProperties
                           .getMetrics()
                           .getTags()
                           .stream()
                           .<Tag>map(t -> new ImmutableTag(t.getKey(), t.getValue()))
                           .toList();
        return meterRegistry -> meterRegistry.config().commonTags(tags);
    }

    @Bean
    SpanFactory spanFactory(ShowcaseCommandProperties commandProperties, OpenTelemetry openTelemetry) {
        val openTelemetrySpanFactory =
                OpenTelemetrySpanFactory
                        .builder()
                        .tracer(openTelemetry.getTracer("AxonFramework-OpenTelemetry"))
                        .contextPropagators(openTelemetry.getPropagators().getTextMapPropagator())
                        .build();
        if (commandProperties.getTracing().isLogging()) {
            return new MultiSpanFactory(List.of(LoggingSpanFactory.INSTANCE, openTelemetrySpanFactory));
        } else {
            return openTelemetrySpanFactory;
        }
    }
}
