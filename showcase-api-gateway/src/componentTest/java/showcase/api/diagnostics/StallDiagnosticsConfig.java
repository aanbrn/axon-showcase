// SPDX-License-Identifier: MIT
package showcase.api.diagnostics;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Installs the stall diagnostics in a component-test slice.
 *
 * <p>Both gateway controller slices import this configuration rather than declaring the filter themselves, so the
 * recorder is wired in one place and a slice that forgets it is visible as a missing import: the registration control
 * asserts that each slice carries it, because a slice without the filter would keep passing while its failure output
 * silently lost the request table.
 */
@TestConfiguration(proxyBeanMethods = false)
public class StallDiagnosticsConfig {

    /**
     * Installs the recorder and the blocking scheduler's timing decorator, so a failure prints the request table
     * beside the scheduler's aggregate figures.
     *
     * @return the filter bean the slices admit their exchanges through
     */
    @Bean
    RequestPhases requestPhases() {
        RequestPhases.instrumentBlockingScheduler();
        return new RequestPhases();
    }
}
