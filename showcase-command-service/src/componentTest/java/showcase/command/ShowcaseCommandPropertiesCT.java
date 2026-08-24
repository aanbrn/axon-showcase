// SPDX-License-Identifier: MIT
package showcase.command;

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

@DisplayName("Showcase command properties binding component tests")
class ShowcaseCommandPropertiesCT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

    private final ApplicationContextRunner ymlContextRunner =
            contextRunner.withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    @DisplayName("All properties have their documented defaults when nothing is set")
    void allPropertiesHaveDocumentedDefaults() {
        contextRunner.run(context -> {
            val properties = context.getBean(ShowcaseCommandProperties.class);
            assertThat(properties.isExitAfterFlywayMigration()).isFalse();
            assertThat(properties.isValidationEnabled()).isTrue();
            assertThat(properties.getShowcaseCache()).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(1000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofMinutes(10));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofMinutes(5));
            });
            assertThat(properties.getSagaCache()).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(1000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofMinutes(10));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofMinutes(5));
            });
            assertThat(properties.getSagaAssociationsCache()).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(1000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofMinutes(10));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofMinutes(5));
            });
            assertThat(properties.getShowcaseSnapshotTrigger().getLoadTimeThreshold())
                    .isEqualTo(Duration.ofMillis(500));
        });
    }

    @Test
    @DisplayName("The application.yml placeholders bind their documented defaults when nothing is set")
    void applicationYmlPlaceholdersBindDocumentedDefaults() {
        ymlContextRunner.run(context -> {
            val properties = context.getBean(ShowcaseCommandProperties.class);
            assertThat(properties.getSagaCache().getMaximumSize()).isEqualTo(1000);
            assertThat(properties.getSagaCache().getExpiresAfterAccess()).isEqualTo(Duration.ofMinutes(10));
            assertThat(properties.getSagaCache().getExpiresAfterWrite()).isEqualTo(Duration.ofMinutes(5));
            assertThat(properties.getSagaAssociationsCache().getMaximumSize()).isEqualTo(1000);
            assertThat(properties.getSagaAssociationsCache().getExpiresAfterAccess())
                    .isEqualTo(Duration.ofMinutes(10));
            assertThat(properties.getSagaAssociationsCache().getExpiresAfterWrite())
                    .isEqualTo(Duration.ofMinutes(5));
            assertThat(properties.getShowcaseSnapshotTrigger().getLoadTimeThreshold())
                    .isEqualTo(Duration.ofMillis(500));
        });
    }

    @ParameterizedTest
    @MethodSource("envVarBindings")
    @DisplayName("An env-var-form property value overrides the default through the application.yml placeholder")
    void envVarFormPropertyOverridesDefault(
            Map<String, Object> envVars, Consumer<ShowcaseCommandProperties> assertions) {
        ymlContextRunner
                .withInitializer(context -> context.getEnvironment()
                        .getPropertySources()
                        .addFirst(new SystemEnvironmentPropertySource("test-env-vars", envVars)))
                .run(context -> assertions.accept(context.getBean(ShowcaseCommandProperties.class)));
    }

    @SuppressWarnings("CodeBlock2Expr")
    static List<Arguments> envVarBindings() {
        return List.of(
                argumentSet("EXIT_AFTER_FLYWAY_MIGRATION", Map.of("EXIT_AFTER_FLYWAY_MIGRATION", "true"), (Consumer<
                                ShowcaseCommandProperties>)
                        properties -> {
                            assertThat(properties.isExitAfterFlywayMigration()).isTrue();
                        }),
                argumentSet(
                        "SHOWCASE_COMMAND_VALIDATION_ENABLED",
                        Map.of("SHOWCASE_COMMAND_VALIDATION_ENABLED", "false"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.isValidationEnabled()).isFalse();
                        }),
                argumentSet("SHOWCASE_CACHE_MAX_SIZE", Map.of("SHOWCASE_CACHE_MAX_SIZE", "2000"), (Consumer<
                                ShowcaseCommandProperties>)
                        properties -> {
                            assertThat(properties.getShowcaseCache().getMaximumSize())
                                    .isEqualTo(2000);
                        }),
                argumentSet(
                        "SHOWCASE_CACHE_EXPIRES_AFTER_ACCESS",
                        Map.of("SHOWCASE_CACHE_EXPIRES_AFTER_ACCESS", "PT20M"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getShowcaseCache().getExpiresAfterAccess())
                                    .isEqualTo(Duration.ofMinutes(20));
                        }),
                argumentSet(
                        "SHOWCASE_CACHE_EXPIRES_AFTER_WRITE",
                        Map.of("SHOWCASE_CACHE_EXPIRES_AFTER_WRITE", "PT15M"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getShowcaseCache().getExpiresAfterWrite())
                                    .isEqualTo(Duration.ofMinutes(15));
                        }),
                argumentSet("SAGA_CACHE_MAX_SIZE", Map.of("SAGA_CACHE_MAX_SIZE", "2000"), (Consumer<
                                ShowcaseCommandProperties>)
                        properties -> {
                            assertThat(properties.getSagaCache().getMaximumSize())
                                    .isEqualTo(2000);
                        }),
                argumentSet(
                        "SAGA_CACHE_EXPIRES_AFTER_ACCESS",
                        Map.of("SAGA_CACHE_EXPIRES_AFTER_ACCESS", "PT20M"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getSagaCache().getExpiresAfterAccess())
                                    .isEqualTo(Duration.ofMinutes(20));
                        }),
                argumentSet(
                        "SAGA_CACHE_EXPIRES_AFTER_WRITE",
                        Map.of("SAGA_CACHE_EXPIRES_AFTER_WRITE", "PT15M"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getSagaCache().getExpiresAfterWrite())
                                    .isEqualTo(Duration.ofMinutes(15));
                        }),
                argumentSet(
                        "SAGA_ASSOCIATIONS_CACHE_MAX_SIZE",
                        Map.of("SAGA_ASSOCIATIONS_CACHE_MAX_SIZE", "2000"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getSagaAssociationsCache().getMaximumSize())
                                    .isEqualTo(2000);
                        }),
                argumentSet(
                        "SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_ACCESS",
                        Map.of("SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_ACCESS", "PT20M"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getSagaAssociationsCache().getExpiresAfterAccess())
                                    .isEqualTo(Duration.ofMinutes(20));
                        }),
                argumentSet(
                        "SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_WRITE",
                        Map.of("SAGA_ASSOCIATIONS_CACHE_EXPIRES_AFTER_WRITE", "PT15M"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getSagaAssociationsCache().getExpiresAfterWrite())
                                    .isEqualTo(Duration.ofMinutes(15));
                        }),
                argumentSet(
                        "SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD",
                        Map.of("SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD", "PT1S"),
                        (Consumer<ShowcaseCommandProperties>) properties -> {
                            assertThat(properties.getShowcaseSnapshotTrigger().getLoadTimeThreshold())
                                    .isEqualTo(Duration.ofSeconds(1));
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
                argumentSet("SHOWCASE_CACHE_MAX_SIZE", Map.of("SHOWCASE_CACHE_MAX_SIZE", "-1")),
                argumentSet("SAGA_CACHE_MAX_SIZE", Map.of("SAGA_CACHE_MAX_SIZE", "-1")),
                argumentSet("SAGA_ASSOCIATIONS_CACHE_MAX_SIZE", Map.of("SAGA_ASSOCIATIONS_CACHE_MAX_SIZE", "-1")));
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseCommandProperties.class)
    static class PropertiesConfig {}
}
