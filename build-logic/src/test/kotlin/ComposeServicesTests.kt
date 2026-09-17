import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compose service mapping")
class ComposeServicesTests {

    @Test
    @DisplayName("A service module maps to the compose service named without its showcase- prefix")
    fun serviceModulesMapToTheirComposeService() {
        assertThat(composeServiceFor("showcase-api-gateway")).isEqualTo("api-gateway")
        assertThat(composeServiceFor("showcase-command-service")).isEqualTo("command-service")
        assertThat(composeServiceFor("showcase-query-service")).isEqualTo("query-service")
        assertThat(composeServiceFor("showcase-projection-service")).isEqualTo("projection-service")
        assertThat(composeServiceFor("showcase-web-ui")).isEqualTo("web-ui")
    }

    @Test
    @DisplayName("A module that ships no compose service maps to nothing")
    fun modulesWithoutAServiceMapToNothing() {
        assertThat(composeServiceFor("showcase-command-api")).isNull()
        assertThat(composeServiceFor("showcase-command-client")).isNull()
        assertThat(composeServiceFor("showcase-test")).isNull()
        assertThat(composeServiceFor("load-tests")).isNull()
        assertThat(composeServiceFor("platform")).isNull()
    }
}
