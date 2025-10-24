package showcase.query;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Component
class ShowcaseQueryConfigurer implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
        configurer.setControllerMethodPredicate(__ -> true);
    }
}
