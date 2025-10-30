package showcase.api;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.axonframework.tracing.SpanFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;

import java.util.List;

import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;

@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
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
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

    @Bean
    AsyncCache<FetchShowcaseListQuery, List<String>> fetchShowcaseListCache(
            ShowcaseApiProperties apiProperties) {
        val cacheSettings = apiProperties.getCaches().get(FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME);
        if (cacheSettings == null) {
            throw new IllegalStateException("Settings for cache '%s' is missing"
                                                    .formatted(FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME));
        }
        return Caffeine.newBuilder()
                       .maximumSize(cacheSettings.getMaximumSize())
                       .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                       .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                       .recordStats()
                       .buildAsync();
    }

    @Bean
    AsyncCache<String, Showcase> fetchShowcaseByIdCache(
            ShowcaseApiProperties apiProperties) {
        val cacheSettings = apiProperties.getCaches().get(FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME);
        if (cacheSettings == null) {
            throw new IllegalStateException("Settings for cache '%s' is missing"
                                                    .formatted(FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME));
        }
        return Caffeine.newBuilder()
                       .maximumSize(cacheSettings.getMaximumSize())
                       .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                       .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                       .recordStats()
                       .buildAsync();
    }

    @Bean
    @SuppressWarnings("unchecked")
    CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer(
            AsyncCache<?, ?> fetchShowcaseListCache,
            AsyncCache<?, ?> fetchShowcaseByIdCache) {
        return cacheManager -> {
            cacheManager.registerCustomCache(
                    "fetch-showcase-list-cache", (AsyncCache<@NonNull Object, Object>) fetchShowcaseListCache);
            cacheManager.registerCustomCache(
                    "fetch-showcase-by-id-cache", (AsyncCache<@NonNull Object, Object>) fetchShowcaseByIdCache);
        };
    }
}
