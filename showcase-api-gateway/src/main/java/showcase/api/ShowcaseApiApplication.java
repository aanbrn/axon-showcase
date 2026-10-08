// SPDX-License-Identifier: MIT
package showcase.api;

import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.observation.ObservationRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.util.List;
import java.util.Optional;
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
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.jgroups.DistributedCommandBusProperties;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.axonframework.extensions.kafka.KafkaProperties;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.axonframework.springboot.autoconfig.UpdateCheckerAutoConfiguration;
import org.axonframework.tracing.SpanFactory;
import org.jspecify.annotations.NonNull;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import showcase.command.ShowcaseEvent;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;

/**
 * Application entry point for the showcase API gateway.
 *
 * <p>Boots the Spring application and declares the beans wiring the distributed command bus, the serialization and
 * OpenAPI customizers, the shared query caches and domain-event receiver, and the security configuration.
 */
@SpringBootApplication(exclude = UpdateCheckerAutoConfiguration.class)
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Showcase API Gateway",
                        description = "The showcase management REST API and the live showcase event stream."))
@EnableConfigurationProperties({ShowcaseApiProperties.class, KafkaProperties.class})
@EnableCaching
@Slf4j
class ShowcaseApiApplication {
    /**
     * Application entry point that disables the AxonIQ console message and starts the Spring context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        System.setProperty("disable-axoniq-console-message", "true");
        SpringApplication.run(ShowcaseApiApplication.class, args);
    }

    /**
     * Configures and exposes the JGroups connector used by the distributed command bus.
     *
     * <p>Reads the discovery and binding settings from {@link DistributedCommandBusProperties} and the injected
     * values, applies them as system properties, and wires the local segment, serializer, routing strategy,
     * and span factory into the connector.
     *
     * @param properties                   the JGroups distributed command bus properties
     * @param tcpPingHosts                 the initial TCP ping hosts
     * @param kubePingNamespace            the Kubernetes namespace used for KUBE_PING discovery
     * @param kubePingLabels               the Kubernetes labels used for KUBE_PING discovery
     * @param messageSerializer            the Axon message serializer
     * @param localSegment                 the local command bus segment
     * @param routingStrategy              the routing strategy for commands
     * @param consistentHashChangeListener the optional consistent hash change listener
     * @param spanFactory                  the tracing span factory
     * @return the configured JGroups connector factory bean
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
            SpanFactory spanFactory) {
        System.setProperty(
                "jgroups.tunnel.gossip_router_hosts",
                properties.getJgroups().getGossip().getHosts());
        System.setProperty(
                "jgroups.bind_addr", String.valueOf(properties.getJgroups().getBindAddr()));
        System.setProperty(
                "jgroups.bind_port", String.valueOf(properties.getJgroups().getBindPort()));
        System.setProperty("jgroups.tcpping.initial_hosts", tcpPingHosts);
        System.setProperty("KUBERNETES_NAMESPACE", kubePingNamespace);
        System.setProperty("KUBERNETES_LABELS", kubePingLabels);

        val jGroupsConnectorFactoryBean = new JGroupsConnectorFactoryBean();
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
     * Builds the primary distributed command bus that routes commands to the connected segments.
     *
     * @param axonConfiguration               the Axon configuration providing the span factory and message monitor
     * @param commandRouter                   the router distributing commands across segments
     * @param commandBusConnector             the connector to the given segments
     * @param distributedCommandBusProperties the properties holding the load factor
     * @return the configured distributed command bus
     */
    @Bean
    @Primary
    DistributedCommandBus distributedCommandBus(
            Configuration axonConfiguration,
            CommandRouter commandRouter,
            CommandBusConnector commandBusConnector,
            DistributedCommandBusProperties distributedCommandBusProperties) {
        val spanFactory = axonConfiguration.getComponent(CommandBusSpanFactory.class);
        val messagedMonitor = axonConfiguration.messageMonitor(DistributedCommandBus.class, "distributedCommandBus");
        val commandBus = DistributedCommandBus.builder()
                .commandRouter(commandRouter)
                .connector(commandBusConnector)
                .spanFactory(spanFactory)
                .messageMonitor(messagedMonitor)
                .build();
        commandBus.updateLoadFactor(distributedCommandBusProperties.getLoadFactor());
        return commandBus;
    }

    /**
     * Creates the asynchronous cache backing fetch-showcase-list queries.
     *
     * @param apiProperties the properties holding the cache configuration
     * @return the configured asynchronous cache
     */
    @Bean
    AsyncCache<FetchShowcaseListQuery, List<Showcase>> fetchShowcaseListCache(ShowcaseApiProperties apiProperties) {
        val cacheSettings = apiProperties.getCaches().get(FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME);
        if (cacheSettings == null) {
            throw new IllegalStateException(
                    "Settings for cache '%s' is missing".formatted(FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME));
        }
        return Caffeine.newBuilder()
                .maximumSize(cacheSettings.getMaximumSize())
                .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                .recordStats()
                .buildAsync();
    }

    /**
     * Creates the asynchronous cache backing fetch-showcase-by-id queries.
     *
     * @param apiProperties the properties holding the cache configuration
     * @return the configured asynchronous cache
     */
    @Bean
    AsyncCache<String, Showcase> fetchShowcaseByIdCache(ShowcaseApiProperties apiProperties) {
        val cacheSettings = apiProperties.getCaches().get(FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME);
        if (cacheSettings == null) {
            throw new IllegalStateException(
                    "Settings for cache '%s' is missing".formatted(FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME));
        }
        return Caffeine.newBuilder()
                .maximumSize(cacheSettings.getMaximumSize())
                .expireAfterAccess(cacheSettings.getExpiresAfterAccess())
                .expireAfterWrite(cacheSettings.getExpiresAfterWrite())
                .recordStats()
                .buildAsync();
    }

    /**
     * Registers the asynchronous query caches with the {@link CaffeineCacheManager} under their cache names.
     *
     * @param fetchShowcaseListCache the fetch-showcase-list cache
     * @param fetchShowcaseByIdCache the fetch-showcase-by-id cache
     * @return the customizer registering the custom caches
     */
    @Bean
    @SuppressWarnings("unchecked")
    CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer(
            AsyncCache<?, ?> fetchShowcaseListCache, AsyncCache<?, ?> fetchShowcaseByIdCache) {
        return cacheManager -> {
            cacheManager.registerCustomCache(
                    "fetch-showcase-list-cache", (AsyncCache<@NonNull Object, Object>) fetchShowcaseListCache);
            cacheManager.registerCustomCache(
                    "fetch-showcase-by-id-cache", (AsyncCache<@NonNull Object, Object>) fetchShowcaseByIdCache);
        };
    }

    /**
     * Builds the Kafka message converter used to deserialize consumed records into Axon event messages.
     *
     * @param eventSerializer the Axon event serializer
     * @param configuration   the Axon configuration providing the upcaster chain
     * @return the configured Kafka message converter
     */
    @Bean
    KafkaMessageConverter<String, byte[]> kafkaMessageConverter(
            @Qualifier("eventSerializer") Serializer eventSerializer, Configuration configuration) {
        return DefaultKafkaMessageConverter.builder()
                .serializer(eventSerializer)
                .upcasterChain(
                        Optional.ofNullable(configuration.upcasterChain()).orElseGet(EventUpcasterChain::new))
                .build();
    }

    /**
     * Exposes the shared showcase domain-event stream as a hot {@link Flux}.
     *
     * <p>Subscribes to Kafka eagerly so consumption starts at application startup (not on the first subscriber), and
     * buffers emitted events so late subscribers still receive recent history.
     *
     * @param kafkaProperties       the Kafka properties
     * @param kafkaMessageConverter the converter used to decode Kafka messages
     * @param observationRegistry   the registry tracing the event consumption
     * @return a hot {@link Flux} of showcase domain events
     */
    @Bean
    Flux<ShowcaseEvent> showcaseEventReceiver(
            KafkaProperties kafkaProperties,
            KafkaMessageConverter<String, byte[]> kafkaMessageConverter,
            ObservationRegistry observationRegistry) {
        val sink = Sinks.many().replay().<ShowcaseEvent>limit(100);
        val receiver =
                KafkaReceiver.create(ReceiverOptions.<String, byte[]>create(kafkaProperties.buildConsumerProperties())
                        .withObservation(observationRegistry)
                        .subscription(List.of(kafkaProperties.getDefaultTopic())));
        receiver.receive()
                .map(kafkaMessageConverter::readKafkaMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(EventMessage::getPayload)
                .filter(ShowcaseEvent.class::isInstance)
                .map(ShowcaseEvent.class::cast)
                .subscribe(
                        event -> sink.tryEmitNext(event).orThrow(),
                        error -> log.error("Showcase event receiver failed: {}", error.getMessage()));
        return sink.asFlux();
    }

    /**
     * Registers the Blackbird Jackson module for faster reflective serialization.
     *
     * @return the customizer applying the Blackbird module to the object mapper
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new BlackbirdModule());
    }

    /**
     * Creates the customizer that reports the build's version in the served OpenAPI document, so the document does not
     * carry a version literal that drifts from the project version.
     *
     * @param buildProperties the build information generated by the build
     * @return the customizer setting the document's info version
     */
    @Bean
    OpenApiCustomizer openApiVersionCustomizer(BuildProperties buildProperties) {
        return openApi -> {
            val info = openApi.getInfo();
            if (info != null) {
                info.setVersion(buildProperties.getVersion());
            }
        };
    }

    /**
     * Defines the security filter chain permitting all requests.
     *
     * @param http the {@link ServerHttpSecurity} to configure
     * @return the configured security filter chain
     */
    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http.csrf(CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize.anyExchange().permitAll())
                .build();
    }
}
