package showcase.saga;

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
import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.jgroups.DistributedCommandBusProperties;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.consumer.ConsumerFactory;
import org.axonframework.extensions.kafka.eventhandling.consumer.Fetcher;
import org.axonframework.extensions.kafka.eventhandling.consumer.subscribable.SubscribableKafkaMessageSource;
import org.axonframework.modelling.saga.repository.CachingSagaStore;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore;
import org.axonframework.modelling.saga.repository.jdbc.PostgresSagaSqlSchema;
import org.axonframework.modelling.saga.repository.jdbc.SagaSqlSchema;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.axonframework.springboot.autoconfig.JdbcAutoConfiguration;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.cache.CacheManager;
import javax.sql.DataSource;
import java.util.List;
import java.util.OptionalLong;

import static showcase.saga.ShowcaseSagaConstants.SAGA_ASSOCIATIONS_CACHE_NAME;
import static showcase.saga.ShowcaseSagaConstants.SAGA_CACHE_NAME;

@SpringBootApplication(exclude = { JdbcAutoConfiguration.class, UpdateCheckerAutoConfiguration.class })
@EnableConfigurationProperties(ShowcaseSagaProperties.class)
@EnableCaching
@Slf4j
class ShowcaseSagaApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseSagaApplication.class, args);
    }

    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy(ShowcaseSagaProperties sagaProperties) {
        return flyway -> {
            flyway.migrate();

            if (sagaProperties.isExitAfterFlywayMigration()) {
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
        return commandBus;
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

    @Bean
    JCacheManagerCustomizer jCacheManagerCustomizer(ShowcaseSagaProperties sagaProperties) {
        return cacheManager -> {
            cacheManager.createCache(
                    SAGA_CACHE_NAME,
                    new CaffeineConfiguration<>()
                            .setMaximumSize(OptionalLong.of(
                                    sagaProperties
                                            .getSagaCache()
                                            .getMaximumSize()))
                            .setExpireAfterAccess(OptionalLong.of(
                                    sagaProperties
                                            .getSagaCache()
                                            .getExpiresAfterAccess()
                                            .toNanos()))
                            .setExpireAfterWrite(OptionalLong.of(
                                    sagaProperties
                                            .getSagaCache()
                                            .getExpiresAfterWrite()
                                            .toNanos())));
            cacheManager.enableStatistics(SAGA_CACHE_NAME, true);

            cacheManager.createCache(
                    SAGA_ASSOCIATIONS_CACHE_NAME,
                    new CaffeineConfiguration<>()
                            .setMaximumSize(OptionalLong.of(
                                    sagaProperties
                                            .getSagaAssociationsCache()
                                            .getMaximumSize()))
                            .setExpireAfterAccess(OptionalLong.of(
                                    sagaProperties
                                            .getSagaAssociationsCache()
                                            .getExpiresAfterAccess()
                                            .toNanos()))
                            .setExpireAfterWrite(OptionalLong.of(
                                    sagaProperties
                                            .getSagaAssociationsCache()
                                            .getExpiresAfterWrite()
                                            .toNanos())));
            cacheManager.enableStatistics(SAGA_ASSOCIATIONS_CACHE_NAME, true);
        };
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
    @ConditionalOnMissingBean
    public ConnectionProvider connectionProvider(DataSource dataSource) {
        return new UnitOfWorkAwareConnectionProviderWrapper(new SpringDataSourceConnectionProvider(dataSource));
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
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(ShowcaseSagaProperties sagaProperties) {
        val tags = sagaProperties
                           .getMetrics()
                           .getTags()
                           .stream()
                           .<Tag>map(t -> new ImmutableTag(t.getKey(), t.getValue()))
                           .toList();
        return meterRegistry -> meterRegistry.config().commonTags(tags);
    }

    @Bean
    SpanFactory spanFactory(ShowcaseSagaProperties sagaProperties, OpenTelemetry openTelemetry) {
        val openTelemetrySpanFactory =
                OpenTelemetrySpanFactory
                        .builder()
                        .tracer(openTelemetry.getTracer("AxonFramework-OpenTelemetry"))
                        .contextPropagators(openTelemetry.getPropagators().getTextMapPropagator())
                        .build();
        if (sagaProperties.getTracing().isLogging()) {
            return new MultiSpanFactory(List.of(LoggingSpanFactory.INSTANCE, openTelemetrySpanFactory));
        } else {
            return openTelemetrySpanFactory;
        }
    }
}
