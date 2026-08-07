package showcase.query;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Configures WebFlux to execute blocking controller methods on the bounded-elastic scheduler.
 */
@Component
class ShowcaseQueryConfigurer implements WebFluxConfigurer {
    /**
     * Marks every controller method as blocking so it runs off the event loop.
     *
     * @param configurer the blocking execution configurer
     */
    @Override
    public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
        configurer.setControllerMethodPredicate(__ -> true);
    }
}
