// SPDX-License-Identifier: MIT
package showcase.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.springframework.context.annotation.Import;
import showcase.api.diagnostics.StallDiagnosticsConfig;

@DisplayName("The stall diagnostics are registered")
class ThreadDumpRegistrationCT {

    @Test
    @DisplayName("All three extensions are on the service path and autodetection is enabled")
    void allDiagnosticsAreRegistered() throws Exception {
        assertThat(ServiceLoader.load(Extension.class))
                .as("the JFR starter, the failure-time dump and the during-run watchdog are all on the service path")
                .anyMatch(JfrSummaryExtension.class::isInstance)
                .anyMatch(ThreadDumpOnFailure.class::isInstance)
                .anyMatch(ThreadDumpWatchdog.class::isInstance);

        assertThat(platformProperties().getProperty("junit.jupiter.extensions.autodetection.enabled"))
                .isEqualTo("true");
    }

    @Test
    @DisplayName("Both gateway controller slices install the request recorder")
    void bothSlicesInstallTheRequestRecorder() throws Exception {
        assertThat(importsOf("showcase.api.rest.ShowcaseRestControllerCT"))
                .as("the REST slice imports the recorder's configuration")
                .contains(StallDiagnosticsConfig.class);
        assertThat(importsOf("showcase.api.telemetry.TelemetryControllerCT"))
                .as("the telemetry slice imports the recorder's configuration")
                .contains(StallDiagnosticsConfig.class);
    }

    private static List<Class<?>> importsOf(String sliceName) throws ClassNotFoundException {
        val slice = Class.forName(sliceName);
        assertThat(slice.isAnnotationPresent(Import.class))
                .as("%s carries an @Import", sliceName)
                .isTrue();
        return List.of(slice.getAnnotation(Import.class).value());
    }

    @Test
    @DisplayName("A dump's virtual threads are counted in both JDK shapes, and a thread merely named 'virtual' is not")
    void virtualThreadsInADumpAreCounted() {
        String dump = """
                #3 "main" WAITING 2026-11-01T02:11:29.726750Z
                    at java.base/java.lang.Object.wait0(Native Method)
                #30 "direct-of-virtual" virtual
                    at java.base/java.lang.VirtualThread.parkNanos(VirtualThread.java:787)
                #28 "" virtual
                    at java.base/java.lang.VirtualThread.parkNanos(VirtualThread.java:787)
                #26 "" virtual TIMED_WAITING 2026-11-01T02:11:29.729391Z
                    at java.base/java.lang.VirtualThread.park(VirtualThread.java:582)
                #28 "my virtual worker" RUNNABLE 2026-11-01T02:11:29.729391Z
                    at showcase.api.Whatever.run(Whatever.java:1)
                """;

        assertThat(ThreadDumpWatchdog.countVirtualThreads(dump))
                .as("the JDK 21 shape twice, the state-and-timestamp shape once, and never a thread merely named")
                .isEqualTo(3);
    }

    private static Properties platformProperties() throws Exception {
        Properties platformProperties = new Properties();
        try (InputStream properties =
                ThreadDumpRegistrationCT.class.getClassLoader().getResourceAsStream("junit-platform.properties")) {
            assertThat(properties)
                    .as("junit-platform.properties on the componentTest classpath")
                    .isNotNull();
            platformProperties.load(properties);
        }
        return platformProperties;
    }
}
