package showcase.command;

import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnector;
import org.axonframework.extensions.jgroups.commandhandling.JGroupsConnectorFactoryBean;
import org.axonframework.micrometer.GlobalMetricRegistry;
import org.axonframework.modelling.saga.repository.CachingSagaStore;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.SagaSqlSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@Testcontainers(parallel = true)
@DirtiesContext
@TestPropertySource(properties = {
        "axon.kafka.publisher.enabled=false",
        "axon.distributed.jgroups.bind-port=17800",
        "axon.distributed.jgroups.cluster-name=axon-showcase-command-it"
})
@DisplayName("Showcase command application integration tests")
class ShowcaseCommandApplicationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer dbEvents =
            new PostgreSQLContainer("postgres:" + System.getProperty("postgres.image.version"));

    @Autowired
    private JGroupsConnectorFactoryBean jgroupsConnectorFactoryBean;

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private DistributedCommandBus distributedCommandBus;

    @Autowired
    private SagaStore<Object> sagaStore;

    @Autowired
    private SagaSqlSchema sagaSqlSchema;

    @Autowired
    private SnapshotTriggerDefinition snapshotTrigger;

    @Autowired
    private DbSchedulerCustomizer dbSchedulerCustomizer;

    @Autowired
    private GlobalMetricRegistry globalMetricRegistry;

    @Autowired
    private ShowcaseDbSchedulerMetrics showcaseDbSchedulerMetrics;

    @Autowired
    private PersistenceExceptionResolver persistenceExceptionResolver;

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
    @DisplayName("The saga store is a caching saga store backed by JDBC")
    void sagaStore_isCachingSagaStore() {
        assertThat(sagaStore).isInstanceOf(CachingSagaStore.class);
    }

    @Test
    @DisplayName("The saga SQL schema is wired")
    void sagaSqlSchema_isWired() {
        assertThat(sagaSqlSchema).isNotNull();
    }

    @Test
    @DisplayName("The snapshot trigger is wired")
    void snapshotTrigger_isWired() {
        assertThat(snapshotTrigger).isNotNull();
    }

    @Test
    @DisplayName("The DB scheduler customizer is wired")
    void dbSchedulerCustomizer_isWired() {
        assertThat(dbSchedulerCustomizer).isNotNull();
    }

    @Test
    @DisplayName("The global metric registry is wired")
    void globalMetricRegistry_isWired() {
        assertThat(globalMetricRegistry).isNotNull();
    }

    @Test
    @DisplayName("The DB scheduler metrics are wired")
    void showcaseDbSchedulerMetrics_isWired() {
        assertThat(showcaseDbSchedulerMetrics).isNotNull();
    }

    @Test
    @DisplayName("The persistence exception resolver is wired")
    void persistenceExceptionResolver_isWired() {
        assertThat(persistenceExceptionResolver).isNotNull();
    }
}
