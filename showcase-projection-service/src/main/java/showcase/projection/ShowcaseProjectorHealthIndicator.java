// SPDX-License-Identifier: MIT
package showcase.projection;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Reports the projector's operating state through the actuator.
 *
 * <p>A projector that is retrying or stopped is not healthy: the service consumes events and serves no HTTP traffic of
 * its own, so readiness reports `DOWN` before initial assignment and during a stream-restart wait. It never joins the
 * liveness group, so a stream failure fails a rollout instead of restarting the container; broker-client retries that
 * emit no pipeline error do not change the state.
 */
@Component("projectorHealthIndicator")
@RequiredArgsConstructor
class ShowcaseProjectorHealthIndicator implements HealthIndicator {
    /**
     * The monitor whose state is reported.
     */
    private final ShowcaseProjectorStateMonitor projectorStateMonitor;

    /**
     * Builds the health status from the projector's state.
     *
     * @return the health status, with the state and the last progress in the details
     */
    @Override
    public Health health() {
        val state = projectorStateMonitor.getState();
        val health = state == ShowcaseProjectorStateMonitor.State.RUNNING ? Health.up() : Health.down();
        health.withDetail("state", state.name().toLowerCase(Locale.ROOT));
        health.withDetail("lastProgressEpochSecond", projectorStateMonitor.lastProgressEpochSecond());
        if (state == ShowcaseProjectorStateMonitor.State.RETRYING && projectorStateMonitor.lastFailure() != null) {
            health.withDetail("lastFailure", projectorStateMonitor.lastFailure());
        }
        return health.build();
    }
}
