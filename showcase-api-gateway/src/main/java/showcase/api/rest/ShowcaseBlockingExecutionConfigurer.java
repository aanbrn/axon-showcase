// SPDX-License-Identifier: MIT
package showcase.api.rest;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Routes controller methods to the blocking scheduler since the downstream services are invoked in a blocking manner.
 */
@Component
class ShowcaseBlockingExecutionConfigurer implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
        configurer.setControllerMethodPredicate(__ -> true);
    }
}
