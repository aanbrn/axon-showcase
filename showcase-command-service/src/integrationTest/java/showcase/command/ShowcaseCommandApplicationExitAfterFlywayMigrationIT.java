package showcase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@Testcontainers(parallel = true)
@DirtiesContext
@TestPropertySource(properties = {
        "showcase.command.exit-after-flyway-migration=true",
        "axon.kafka.publisher.enabled=false",
        "axon.distributed.jgroups.bind-port=17801",
        "axon.distributed.jgroups.cluster-name=axon-showcase-command-exit-it"
})
@DisplayName("Showcase command application exit after flyway migration integration tests")
class ShowcaseCommandApplicationExitAfterFlywayMigrationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer dbEvents =
            new PostgreSQLContainer("postgres:" + System.getProperty("postgres.image.version"));

    @MockitoBean
    private ShowcaseCommandApplication.ApplicationExitHandler exitHandler;

    @Test
    @DisplayName("The exit handler is invoked after the flyway migration")
    void exitHandler_isInvoked() {
        verify(exitHandler).exit(any(ApplicationContext.class));
    }
}
