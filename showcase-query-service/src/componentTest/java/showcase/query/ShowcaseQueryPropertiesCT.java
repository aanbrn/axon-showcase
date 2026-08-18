package showcase.query;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

@DisplayName("Showcase query properties binding component tests")
class ShowcaseQueryPropertiesCT {

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
            val properties = context.getBean(ShowcaseQueryProperties.class);
            assertThat(properties.isIndexInitializationEnabled()).isTrue();
            assertThat(properties.isExitAfterIndexInitialization()).isFalse();
            assertThat(properties.isValidationEnabled()).isTrue();
        });
    }

    @Test
    @DisplayName("The application.yml placeholders bind their documented defaults when nothing is set")
    void applicationYmlPlaceholdersBindDocumentedDefaults() {
        ymlContextRunner.run(context -> {
            val properties = context.getBean(ShowcaseQueryProperties.class);
            assertThat(properties.isIndexInitializationEnabled()).isTrue();
            assertThat(properties.isExitAfterIndexInitialization()).isFalse();
            assertThat(properties.isValidationEnabled()).isTrue();
        });
    }

    @ParameterizedTest
    @MethodSource("envVarBindings")
    @DisplayName("An env-var-form property value overrides the default through the application.yml placeholder")
    void envVarFormPropertyOverridesDefault(Map<String, Object> envVars, Consumer<ShowcaseQueryProperties> assertions) {
        ymlContextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource("test-env-vars", envVars)))
                .run(context -> assertions.accept(context.getBean(ShowcaseQueryProperties.class)));
    }

    @SuppressWarnings("CodeBlock2Expr")
    static List<Arguments> envVarBindings() {
        return List.of(
                argumentSet("INDEX_INITIALIZATION_ENABLED",
                            Map.of("INDEX_INITIALIZATION_ENABLED", "false"),
                            (Consumer<ShowcaseQueryProperties>) properties -> {
                                assertThat(properties.isIndexInitializationEnabled()).isFalse();
                            }),
                argumentSet("EXIT_AFTER_INDEX_INITIALIZATION",
                            Map.of("EXIT_AFTER_INDEX_INITIALIZATION", "true"),
                            (Consumer<ShowcaseQueryProperties>) properties -> {
                                assertThat(properties.isExitAfterIndexInitialization()).isTrue();
                            }),
                argumentSet("SHOWCASE_QUERY_VALIDATION_ENABLED",
                            Map.of("SHOWCASE_QUERY_VALIDATION_ENABLED", "false"),
                            (Consumer<ShowcaseQueryProperties>) properties -> {
                                assertThat(properties.isValidationEnabled()).isFalse();
                            })
        );
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseQueryProperties.class)
    static class PropertiesConfig {
    }
}
