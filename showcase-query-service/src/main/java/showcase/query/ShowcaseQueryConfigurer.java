package showcase.query;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Component
class ShowcaseQueryConfigurer implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(@NonNull BlockingExecutionConfigurer configurer) {
        configurer.setControllerMethodPredicate(__ -> true);
    }
}
