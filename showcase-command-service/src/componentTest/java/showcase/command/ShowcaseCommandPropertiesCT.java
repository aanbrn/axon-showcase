package showcase.command;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Showcase command properties binding component tests")
class ShowcaseCommandPropertiesCT {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(PropertiesConfig.class);

    @Test
    @DisplayName("Command validation is enabled by default")
    void commandValidationEnabledByDefault() {
        contextRunner.run(context -> {
            val properties = context.getBean(ShowcaseCommandProperties.class);
            assertThat(properties.isValidationEnabled()).isTrue();
        });
    }

    @Test
    @DisplayName("An env-var-form property value of false disables command validation")
    void envVarFormPropertyDisablesCommandValidation() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource("test-env-vars",
                                                            Map.of("SHOWCASE_COMMAND_VALIDATION_ENABLED", "false"))))
                .run(context -> {
                    val properties = context.getBean(ShowcaseCommandProperties.class);
                    assertThat(properties.isValidationEnabled()).isFalse();
                });
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseCommandProperties.class)
    static class PropertiesConfig {
    }
}
