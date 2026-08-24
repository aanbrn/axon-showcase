// SPDX-License-Identifier: MIT
package showcase.api;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux configuration for the showcase API gateway.
 *
 * <p>Runs all controller methods on the blocking scheduler since the downstream services are invoked in a blocking
 * manner.
 */
@Component
class ShowcaseApiConfigurer implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
        configurer.setControllerMethodPredicate(__ -> true);
    }
}
