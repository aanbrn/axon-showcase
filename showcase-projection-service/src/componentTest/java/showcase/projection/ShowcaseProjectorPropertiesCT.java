// SPDX-License-Identifier: MIT
package showcase.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

@DisplayName("Showcase projector properties binding component tests")
class ShowcaseProjectorPropertiesCT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

    private final ApplicationContextRunner ymlContextRunner =
            contextRunner.withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    @DisplayName("All properties have their documented defaults when nothing is set")
    void allPropertiesHaveDocumentedDefaults() {
        contextRunner.run(context -> {
            val properties = context.getBean(ShowcaseProjectorProperties.class);
            assertThat(properties.getMinConcurrency()).isEqualTo(1);
            assertThat(properties.getMaxConcurrency()).isEqualTo(256);
            assertThat(properties.getBatch()).satisfies(batch -> {
                assertThat(batch.getMaxSize()).isEqualTo(100);
                assertThat(batch.getMaxTime()).isEqualTo(Duration.ofMillis(100));
                assertThat(batch.getBufferMaxSize()).isEqualTo(10000);
            });
            assertThat(properties.getRetry()).satisfies(retry -> {
                assertThat(retry.getMaxAttempts()).isEqualTo(3);
                assertThat(retry.getMinBackoff()).isEqualTo(Duration.ofMillis(100));
            });
            assertThat(properties.getRestart().getDelay()).isEqualTo(Duration.ofSeconds(10));
        });
    }

    @Test
    @DisplayName("The application.yml placeholders bind their documented defaults when nothing is set")
    void applicationYmlPlaceholdersBindDocumentedDefaults() {
        ymlContextRunner.run(context -> {
            val properties = context.getBean(ShowcaseProjectorProperties.class);
            assertThat(properties.getMinConcurrency()).isEqualTo(1);
            assertThat(properties.getMaxConcurrency()).isEqualTo(256);
            assertThat(properties.getBatch()).satisfies(batch -> {
                assertThat(batch.getMaxSize()).isEqualTo(100);
                assertThat(batch.getMaxTime()).isEqualTo(Duration.ofMillis(100));
                assertThat(batch.getBufferMaxSize()).isEqualTo(10000);
            });
            assertThat(properties.getRetry()).satisfies(retry -> {
                assertThat(retry.getMaxAttempts()).isEqualTo(3);
                assertThat(retry.getMinBackoff()).isEqualTo(Duration.ofMillis(100));
            });
            assertThat(properties.getRestart().getDelay()).isEqualTo(Duration.ofSeconds(10));
        });
    }

    @ParameterizedTest
    @MethodSource("envVarBindings")
    @DisplayName("An env-var-form property value overrides the default through the application.yml placeholder")
    void envVarFormPropertyOverridesDefault(
            Map<String, Object> envVars, Consumer<ShowcaseProjectorProperties> assertions) {
        ymlContextRunner
                .withInitializer(context -> context.getEnvironment()
                        .getPropertySources()
                        .addFirst(new SystemEnvironmentPropertySource("test-env-vars", envVars)))
                .run(context -> assertions.accept(context.getBean(ShowcaseProjectorProperties.class)));
    }

    @SuppressWarnings("CodeBlock2Expr")
    static List<Arguments> envVarBindings() {
        return List.of(
                argumentSet("PROJECTOR_MIN_CONCURRENCY", Map.of("PROJECTOR_MIN_CONCURRENCY", "2"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getMinConcurrency()).isEqualTo(2);
                        }),
                argumentSet("PROJECTOR_MAX_CONCURRENCY", Map.of("PROJECTOR_MAX_CONCURRENCY", "300"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getMaxConcurrency()).isEqualTo(300);
                        }),
                argumentSet("PROJECTOR_BATCH_MAX_SIZE", Map.of("PROJECTOR_BATCH_MAX_SIZE", "200"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getBatch().getMaxSize()).isEqualTo(200);
                        }),
                argumentSet("PROJECTOR_BATCH_MAX_TIME", Map.of("PROJECTOR_BATCH_MAX_TIME", "PT0.2S"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getBatch().getMaxTime()).isEqualTo(Duration.ofMillis(200));
                        }),
                argumentSet(
                        "PROJECTOR_BATCH_BUFFER_MAX_SIZE",
                        Map.of("PROJECTOR_BATCH_BUFFER_MAX_SIZE", "20000"),
                        (Consumer<ShowcaseProjectorProperties>) properties -> {
                            assertThat(properties.getBatch().getBufferMaxSize()).isEqualTo(20000);
                        }),
                argumentSet("PROJECTOR_RETRY_MAX_ATTEMPTS", Map.of("PROJECTOR_RETRY_MAX_ATTEMPTS", "5"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getRetry().getMaxAttempts()).isEqualTo(5);
                        }),
                argumentSet("PROJECTOR_RETRY_MIN_BACKOFF", Map.of("PROJECTOR_RETRY_MIN_BACKOFF", "PT0.2S"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getRetry().getMinBackoff()).isEqualTo(Duration.ofMillis(200));
                        }),
                argumentSet("PROJECTOR_RESTART_DELAY", Map.of("PROJECTOR_RESTART_DELAY", "PT20S"), (Consumer<
                                ShowcaseProjectorProperties>)
                        properties -> {
                            assertThat(properties.getRestart().getDelay()).isEqualTo(Duration.ofSeconds(20));
                        }));
    }

    @ParameterizedTest
    @MethodSource("invalidEnvVars")
    @DisplayName("An out-of-range env-var-form property value fails the context")
    void outOfRangeEnvVarFailsContext(Map<String, Object> envVars) {
        ymlContextRunner
                .withInitializer(context -> context.getEnvironment()
                        .getPropertySources()
                        .addFirst(new SystemEnvironmentPropertySource("test-env-vars", envVars)))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                });
    }

    static List<Arguments> invalidEnvVars() {
        return List.of(
                argumentSet("PROJECTOR_MIN_CONCURRENCY", Map.of("PROJECTOR_MIN_CONCURRENCY", "0")),
                argumentSet("PROJECTOR_MAX_CONCURRENCY", Map.of("PROJECTOR_MAX_CONCURRENCY", "0")),
                argumentSet("PROJECTOR_BATCH_MAX_SIZE", Map.of("PROJECTOR_BATCH_MAX_SIZE", "0")),
                argumentSet("PROJECTOR_BATCH_MAX_SIZE", Map.of("PROJECTOR_BATCH_MAX_SIZE", "1001")),
                argumentSet("PROJECTOR_BATCH_MAX_TIME", Map.of("PROJECTOR_BATCH_MAX_TIME", "PT0S")),
                argumentSet("PROJECTOR_BATCH_MAX_TIME", Map.of("PROJECTOR_BATCH_MAX_TIME", "PT2S")),
                argumentSet("PROJECTOR_BATCH_BUFFER_MAX_SIZE", Map.of("PROJECTOR_BATCH_BUFFER_MAX_SIZE", "999")),
                argumentSet("PROJECTOR_BATCH_BUFFER_MAX_SIZE", Map.of("PROJECTOR_BATCH_BUFFER_MAX_SIZE", "100001")),
                argumentSet("PROJECTOR_RETRY_MAX_ATTEMPTS", Map.of("PROJECTOR_RETRY_MAX_ATTEMPTS", "-1")),
                argumentSet("PROJECTOR_RETRY_MIN_BACKOFF", Map.of("PROJECTOR_RETRY_MIN_BACKOFF", "PT-1S")),
                argumentSet("PROJECTOR_RETRY_MIN_BACKOFF", Map.of("PROJECTOR_RETRY_MIN_BACKOFF", "PT2S")),
                argumentSet("PROJECTOR_RESTART_DELAY", Map.of("PROJECTOR_RESTART_DELAY", "PT0.5S")),
                argumentSet("PROJECTOR_RESTART_DELAY", Map.of("PROJECTOR_RESTART_DELAY", "PT61S")));
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseProjectorProperties.class)
    static class PropertiesConfig {}
}
