package showcase.api;

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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
import static showcase.api.ShowcaseApiConstants.FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;

@DisplayName("Showcase API properties binding component tests")
class ShowcaseApiPropertiesCT {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
                    .withUserConfiguration(PropertiesConfig.class);

    private final ApplicationContextRunner ymlContextRunner =
            contextRunner.withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    @DisplayName("All properties have their documented defaults when nothing is set")
    void allPropertiesHaveDocumentedDefaults() {
        contextRunner.run(context -> {
            val properties = context.getBean(ShowcaseApiProperties.class);
            assertThat(properties.getCaches()).hasSize(2);
            assertThat(cacheFor(properties, FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME)).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(1000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofMinutes(10));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofMinutes(5));
            });
            assertThat(cacheFor(properties, FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME)).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(1000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofMinutes(10));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofMinutes(5));
            });
        });
    }

    @Test
    @DisplayName("The application.yml placeholders bind their documented defaults and an env var overrides through " +
                         "the placeholder")
    void applicationYmlPlaceholdersBindDefaultsAndEnvVarOverrides() {
        ymlContextRunner.run(context -> {
            val properties = context.getBean(ShowcaseApiProperties.class);
            assertThat(cacheFor(properties, FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME)).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(100000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofHours(24));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofHours(12));
            });
            assertThat(cacheFor(properties, FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME)).satisfies(cache -> {
                assertThat(cache.getMaximumSize()).isEqualTo(1000000);
                assertThat(cache.getExpiresAfterAccess()).isEqualTo(Duration.ofHours(24));
                assertThat(cache.getExpiresAfterWrite()).isEqualTo(Duration.ofHours(12));
            });
            assertThat(context.getEnvironment().getProperty("showcase.query.api-url"))
                    .isEqualTo("http://localhost:8084");
        });

        ymlContextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource("test-env-vars",
                                                            Map.of("SHOWCASE_QUERY_SERVICE_URL",
                                                                   "http://override:8080"))))
                .run(context -> assertThat(context.getEnvironment().getProperty("showcase.query.api-url"))
                                        .isEqualTo("http://override:8080"));
    }

    @ParameterizedTest
    @MethodSource("envVarBindings")
    @DisplayName("An env-var-form property value overrides the default through the application.yml placeholder")
    void envVarFormPropertyOverridesDefault(Map<String, Object> envVars, Consumer<ShowcaseApiProperties> assertions) {
        ymlContextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource("test-env-vars", envVars)))
                .run(context -> assertions.accept(context.getBean(ShowcaseApiProperties.class)));
    }

    @SuppressWarnings("CodeBlock2Expr")
    static List<Arguments> envVarBindings() {
        val listCache = FETCH_SHOWCASE_LIST_QUERY_CACHE_NAME;
        val byIdCache = FETCH_SHOWCASE_BY_ID_QUERY_CACHE_NAME;
        return List.of(
                argumentSet("FETCH_SHOWCASE_LIST_QUERY_CACHE_MAX_SIZE",
                            Map.of("FETCH_SHOWCASE_LIST_QUERY_CACHE_MAX_SIZE", "2000"),
                            (Consumer<ShowcaseApiProperties>) properties -> {
                                assertThat(cacheFor(properties, listCache).getMaximumSize()).isEqualTo(2000);
                            }),
                argumentSet("FETCH_SHOWCASE_LIST_QUERY_CACHE_EXPIRES_AFTER_ACCESS",
                            Map.of("FETCH_SHOWCASE_LIST_QUERY_CACHE_EXPIRES_AFTER_ACCESS", "PT20M"),
                            (Consumer<ShowcaseApiProperties>) properties -> {
                                assertThat(cacheFor(properties, listCache).getExpiresAfterAccess())
                                        .isEqualTo(Duration.ofMinutes(20));
                            }),
                argumentSet("FETCH_SHOWCASE_LIST_QUERY_CACHE_EXPIRES_AFTER_WRITE",
                            Map.of("FETCH_SHOWCASE_LIST_QUERY_CACHE_EXPIRES_AFTER_WRITE", "PT15M"),
                            (Consumer<ShowcaseApiProperties>) properties -> {
                                assertThat(cacheFor(properties, listCache).getExpiresAfterWrite())
                                        .isEqualTo(Duration.ofMinutes(15));
                            }),
                argumentSet("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_MAX_SIZE",
                            Map.of("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_MAX_SIZE", "2000"),
                            (Consumer<ShowcaseApiProperties>) properties -> {
                                assertThat(cacheFor(properties, byIdCache).getMaximumSize()).isEqualTo(2000);
                            }),
                argumentSet("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_EXPIRES_AFTER_ACCESS",
                            Map.of("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_EXPIRES_AFTER_ACCESS", "PT20M"),
                            (Consumer<ShowcaseApiProperties>) properties -> {
                                assertThat(cacheFor(properties, byIdCache).getExpiresAfterAccess())
                                        .isEqualTo(Duration.ofMinutes(20));
                            }),
                argumentSet("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_EXPIRES_AFTER_WRITE",
                            Map.of("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_EXPIRES_AFTER_WRITE", "PT15M"),
                            (Consumer<ShowcaseApiProperties>) properties -> {
                                assertThat(cacheFor(properties, byIdCache).getExpiresAfterWrite())
                                        .isEqualTo(Duration.ofMinutes(15));
                            })
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEnvVars")
    @DisplayName("An out-of-range env-var-form property value fails the context")
    void outOfRangeEnvVarFailsContext(Map<String, Object> envVars) {
        ymlContextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource("test-env-vars", envVars)))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                });
    }

    static List<Arguments> invalidEnvVars() {
        return List.of(
                argumentSet("FETCH_SHOWCASE_LIST_QUERY_CACHE_MAX_SIZE",
                            Map.of("FETCH_SHOWCASE_LIST_QUERY_CACHE_MAX_SIZE", "-1")),
                argumentSet("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_MAX_SIZE",
                            Map.of("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_MAX_SIZE", "-1")),
                argumentSet("FETCH_SHOWCASE_LIST_QUERY_CACHE_EXPIRES_AFTER_ACCESS",
                            Map.of("FETCH_SHOWCASE_LIST_QUERY_CACHE_EXPIRES_AFTER_ACCESS", "PT-1S")),
                argumentSet("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_EXPIRES_AFTER_ACCESS",
                            Map.of("FETCH_SHOWCASE_BY_ID_QUERY_CACHE_EXPIRES_AFTER_ACCESS", "PT-1S"))
        );
    }

    private static ShowcaseApiProperties.Cache cacheFor(ShowcaseApiProperties properties, String name) {
        assertThat(properties.getCaches()).containsKey(name);
        return properties.getCaches().get(name);
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseApiProperties.class)
    static class PropertiesConfig {
    }
}
