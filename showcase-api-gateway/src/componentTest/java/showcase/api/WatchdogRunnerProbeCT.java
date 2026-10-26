// SPDX-License-Identifier: MIT
package showcase.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Runner diagnostic control")
class WatchdogRunnerProbeCT {

    @Test
    @DisplayName("A parked virtual thread appears in the watchdog dump on this runner")
    void parkedVirtualThreadAppearsInTheDump() throws Exception {
        Thread.ofVirtual().name("vt-runner-control").start(() -> {
            try {
                Thread.sleep(120_000);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
        Thread.sleep(6_000);
    }
}
