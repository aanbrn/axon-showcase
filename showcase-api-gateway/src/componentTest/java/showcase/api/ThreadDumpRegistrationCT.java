// SPDX-License-Identifier: MIT
package showcase.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Properties;
import java.util.ServiceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;

@DisplayName("The thread-dump diagnostic is registered")
class ThreadDumpRegistrationCT {

    @Test
    @DisplayName("The extension is on the service path and autodetection is enabled")
    void theDiagnosticIsRegistered() throws Exception {
        assertThat(ServiceLoader.load(Extension.class)).anyMatch(ThreadDumpOnFailure.class::isInstance);

        Properties platformProperties = new Properties();
        try (InputStream properties = getClass().getClassLoader().getResourceAsStream("junit-platform.properties")) {
            assertThat(properties)
                    .as("junit-platform.properties on the componentTest classpath")
                    .isNotNull();
            platformProperties.load(properties);
        }
        assertThat(platformProperties.getProperty("junit.jupiter.extensions.autodetection.enabled"))
                .isEqualTo("true");
    }
}
