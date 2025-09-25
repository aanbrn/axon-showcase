package showcase.tracing;

import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.springboot.autoconfig.OpenTelemetryAutoConfiguration;
import org.axonframework.tracing.LoggingSpanFactory;
import org.axonframework.tracing.MultiSpanFactory;
import org.axonframework.tracing.SpanFactory;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@AutoConfigureBefore(OpenTelemetryAutoConfiguration.class)
@Slf4j
class TracingAutoConfiguration {

    @Bean
    SpanFactory spanFactory(
            @Value("${axon.tracing.logging.enabled:false}") boolean tracingLoggingEnabled,
            OpenTelemetry openTelemetry) {
        val openTelemetrySpanFactory =
                OpenTelemetrySpanFactory
                        .builder()
                        .tracer(openTelemetry.getTracer("AxonFramework-OpenTelemetry"))
                        .contextPropagators(openTelemetry.getPropagators().getTextMapPropagator())
                        .build();
        if (tracingLoggingEnabled) {
            log.info("Axon Tracing Logging is ON");

            return new MultiSpanFactory(List.of(openTelemetrySpanFactory, LoggingSpanFactory.INSTANCE));
        } else {
            return openTelemetrySpanFactory;
        }
    }
}
