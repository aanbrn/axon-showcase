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
    @DisplayName("Both extensions are on the service path and autodetection is enabled")
    void bothDiagnosticsAreRegistered() throws Exception {
        assertThat(ServiceLoader.load(Extension.class))
                .as("the failure-time dump and the during-run watchdog are both on the service path")
                .anyMatch(ThreadDumpOnFailure.class::isInstance)
                .anyMatch(ThreadDumpWatchdog.class::isInstance);

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
