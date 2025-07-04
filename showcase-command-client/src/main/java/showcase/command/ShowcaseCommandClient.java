package showcase.command;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class ShowcaseCommandClient implements ShowcaseCommandOperations {

    @NonNull
    private final ReactorCommandGateway commandGateway;

    @TimeLimiter(name = SHOWCASE_COMMAND_SERVICE)
    @CircuitBreaker(name = SHOWCASE_COMMAND_SERVICE)
    @Retry(name = SHOWCASE_COMMAND_SERVICE)
    @Override
    public Mono<Void> schedule(@NonNull ScheduleShowcaseCommand command) {
        return sendCommand(command);
    }

    @TimeLimiter(name = SHOWCASE_COMMAND_SERVICE)
    @CircuitBreaker(name = SHOWCASE_COMMAND_SERVICE)
    @Retry(name = SHOWCASE_COMMAND_SERVICE)
    @Override
    public Mono<Void> start(@NonNull StartShowcaseCommand command) {
        return sendCommand(command);
    }

    @TimeLimiter(name = SHOWCASE_COMMAND_SERVICE)
    @CircuitBreaker(name = SHOWCASE_COMMAND_SERVICE)
    @Retry(name = SHOWCASE_COMMAND_SERVICE)
    @Override
    public Mono<Void> finish(@NonNull FinishShowcaseCommand command) {
        return sendCommand(command);
    }

    @TimeLimiter(name = SHOWCASE_COMMAND_SERVICE)
    @CircuitBreaker(name = SHOWCASE_COMMAND_SERVICE)
    @Retry(name = SHOWCASE_COMMAND_SERVICE)
    @Override
    public Mono<Void> remove(@NonNull RemoveShowcaseCommand command) {
        return sendCommand(command);
    }

    private Mono<Void> sendCommand(@NonNull ShowcaseCommand command) {
        return commandGateway
                       .<Void>send(command)
                       .onErrorMap(this::convertError);
    }

    private Throwable convertError(Throwable t) {
        if (t instanceof CommandExecutionException e
                    && e.getDetails().isPresent()
                    && e.getDetails().get() instanceof ShowcaseCommandErrorDetails errorDetails) {
            return new ShowcaseCommandException(errorDetails);
        } else {
            return t;
        }
    }
}
