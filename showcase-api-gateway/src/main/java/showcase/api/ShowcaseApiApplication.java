package showcase.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.opentelemetry.api.OpenTelemetry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandBusSpanFactory;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.ConsistentHashChangeListener;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.config.Configuration;
import org.axonframework.extensions.jgroups.DistributedCommandBusProperties;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.axonframework.serialization.Serializer;
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
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import showcase.query.PageRequest;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static showcase.api.ShowcaseApiConstants.FETCH_ALL_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_BY_ID_CACHE_NAME;

@SpringBootApplication
@EnableConfigurationProperties(ShowcaseApiProperties.class)
@EnableCaching
@Slf4j
class ShowcaseApiApplication {

    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseApiApplication.class, args);
    }

    @Bean
    public JGroupsConnectorFactoryBean jgroupsConnectorFactoryBean(
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
    CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer(ShowcaseApiProperties apiProperties) {
        return cacheManager -> {
            cacheManager.setAllowNullValues(false);
            for (val cacheName : List.of(FETCH_ALL_CACHE_NAME, FETCH_BY_ID_CACHE_NAME)) {
                val cacheSettings = requireNonNull(apiProperties.getCaches().get(cacheName),
                                                   "Setting for '%s' cache is missing".formatted(cacheName));
                cacheManager.registerCustomCache(
                        cacheName,
                        Caffeine.newBuilder()
                                .maximumSize(cacheSettings.getMaximumSize())
                                .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                                .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                                .recordStats()
                                .buildAsync());
            }
        };
    }

    @Bean
    HandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver() {
        val resolver = new ReactivePageableHandlerMethodArgumentResolver() {
            @Override
            public @NonNull Pageable resolveArgumentValue(
                    @NonNull MethodParameter parameter,
                    @NonNull BindingContext bindingContext,
                    @NonNull ServerWebExchange exchange) {
                val pageable = super.resolveArgumentValue(parameter, bindingContext, exchange);
                return org.springframework.data.domain.PageRequest.of(
                        Math.min(pageable.getPageNumber(), PageRequest.MAX_PAGE_NUMBER),
                        pageable.getPageSize(),
                        pageable.getSort());
            }
        };
        resolver.setMaxPageSize(PageRequest.MAX_PAGE_SIZE);
        return resolver;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(ShowcaseApiProperties apiProperties) {
        val tags = apiProperties
                           .getMetrics()
                           .getTags()
                           .stream()
                           .<Tag>map(t -> new ImmutableTag(t.getKey(), t.getValue()))
                           .toList();
        return meterRegistry -> meterRegistry.config().commonTags(tags);
    }

    @Bean
    SpanFactory spanFactory(ShowcaseApiProperties apiProperties, OpenTelemetry openTelemetry) {
        val openTelemetrySpanFactory =
                OpenTelemetrySpanFactory
                        .builder()
                        .tracer(openTelemetry.getTracer("AxonFramework-OpenTelemetry"))
                        .contextPropagators(openTelemetry.getPropagators().getTextMapPropagator())
                        .build();
        if (apiProperties.getTracing().isLogging()) {
            return new MultiSpanFactory(List.of(LoggingSpanFactory.INSTANCE, openTelemetrySpanFactory));
        } else {
            return openTelemetrySpanFactory;
        }
    }
}
