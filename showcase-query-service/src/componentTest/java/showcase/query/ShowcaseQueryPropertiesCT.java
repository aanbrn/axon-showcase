package showcase.query;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Showcase query properties binding component tests")
class ShowcaseQueryPropertiesCT {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(PropertiesConfig.class);

    @Test
    @DisplayName("Query validation is enabled by default")
    void queryValidationEnabledByDefault() {
        contextRunner.run(context -> {
            val properties = context.getBean(ShowcaseQueryProperties.class);
            assertThat(properties.isValidationEnabled()).isTrue();
        });
    }

    @Test
    @DisplayName("An env-var-form property value of false disables query validation")
    void envVarFormPropertyDisablesQueryValidation() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource("test-env-vars",
                                                            Map.of("SHOWCASE_QUERY_VALIDATION_ENABLED", "false"))))
                .run(context -> {
                    val properties = context.getBean(ShowcaseQueryProperties.class);
                    assertThat(properties.isValidationEnabled()).isFalse();
                });
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseQueryProperties.class)
    static class PropertiesConfig {
    }
}
