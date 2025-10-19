package showcase.api;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Component
class ShowcaseApiConfigurer implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(@NonNull BlockingExecutionConfigurer configurer) {
        configurer.setControllerMethodPredicate(__ -> true);
    }
}
