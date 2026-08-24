// SPDX-License-Identifier: MIT
package showcase.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

@DisplayName("Showcase query client properties binding component tests")
class ShowcaseQueryClientPropertiesCT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

    @Test
    @DisplayName("An env-var-form property value binds the query service URL")
    void envVarFormPropertyBindsApiUrl() {
        contextRunner
                .withInitializer(context -> context.getEnvironment()
                        .getPropertySources()
                        .addFirst(new SystemEnvironmentPropertySource(
                                "test-env-vars", Map.of("SHOWCASE_QUERY_API_URL", "http://query:8080"))))
                .run(context -> assertThat(context.getBean(ShowcaseQueryClientProperties.class)
                                .getApiUrl())
                        .isEqualTo("http://query:8080"));
    }

    @ParameterizedTest
    @MethodSource("invalidApiUrls")
    @DisplayName("An invalid env-var-form property value fails the context")
    void invalidApiUrlFailsContext(String envValue) {
        contextRunner
                .withInitializer(context -> context.getEnvironment()
                        .getPropertySources()
                        .addFirst(new SystemEnvironmentPropertySource(
                                "test-env-vars", Map.of("SHOWCASE_QUERY_API_URL", envValue))))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                });
    }

    static List<Arguments> invalidApiUrls() {
        return List.of(argumentSet("An empty URL", ""), argumentSet("A non-URL value", "not-a-url"));
    }

    @Configuration
    @EnableConfigurationProperties(ShowcaseQueryClientProperties.class)
    static class PropertiesConfig {}
}
