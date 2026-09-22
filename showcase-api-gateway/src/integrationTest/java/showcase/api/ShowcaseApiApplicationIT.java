// SPDX-License-Identifier: MIT
package showcase.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnector;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axon.distributed.jgroups.bind-port=17800",
            "axon.distributed.jgroups.cluster-name=axon-showcase-it"
        })
@DirtiesContext
@DisplayName("Showcase API application integration tests")
class ShowcaseApiApplicationIT {

    @Autowired
    private JGroupsConnectorFactoryBean jgroupsConnectorFactoryBean;

    @Autowired
    private DistributedCommandBus distributedCommandBus;

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private CaffeineCacheManager cacheManager;

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Autowired
    private com.github.benmanes.caffeine.cache.AsyncCache<?, ?> fetchShowcaseListCache;

    @Autowired
    private com.github.benmanes.caffeine.cache.AsyncCache<?, ?> fetchShowcaseByIdCache;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("The JGroups connector factory bean produces a JGroups connector")
    void jgroupsConnectorFactoryBean_producesConnector() {
        assertThat(jgroupsConnectorFactoryBean.getObject()).isInstanceOf(JGroupsConnector.class);
    }

    @Test
    @DisplayName("The distributed command bus is the primary command bus")
    void distributedCommandBus_isPrimaryCommandBus() {
        assertThat(commandBus).isSameAs(distributedCommandBus);
    }

    @Test
    @DisplayName("The caffeine cache manager registers the showcase caches")
    void cacheManager_registersShowcaseCaches() {
        assertThat(cacheManager.getCacheNames())
                .containsExactlyInAnyOrder("fetch-showcase-list-cache", "fetch-showcase-by-id-cache");
    }

    @Test
    @DisplayName("The security filter chain is wired")
    void securityFilterChain_isWired() {
        assertThat(securityWebFilterChain).isNotNull();
    }

    @Test
    @DisplayName("The fetch-showcase-list and fetch-showcase-by-id caches are wired")
    void caches_areWired() {
        assertThat(fetchShowcaseListCache).isNotNull();
        assertThat(fetchShowcaseByIdCache).isNotNull();
    }

    @Test
    @DisplayName("A preflight from the standalone UI origin is granted the headers the UI sends")
    void corsPreflight_fromUiOrigin_isGrantedUiHeaders() {
        WebTestClient client =
                WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        client.options()
                .uri("/showcases")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,idempotency-key,traceparent")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .value(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, value -> assertThat(value)
                        .containsIgnoringCase("content-type")
                        .containsIgnoringCase("idempotency-key")
                        .containsIgnoringCase("traceparent"));
    }
}
