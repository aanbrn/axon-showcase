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

import static showcase.command.ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE;

@Component
@RequiredArgsConstructor
@TimeLimiter(name = SHOWCASE_COMMAND_SERVICE)
@CircuitBreaker(name = SHOWCASE_COMMAND_SERVICE)
@Retry(name = SHOWCASE_COMMAND_SERVICE)
class ShowcaseCommandClient implements ShowcaseCommandOperations {

    @NonNull
    private final ReactorCommandGateway commandGateway;

    @Override
    public Mono<Void> schedule(@NonNull ScheduleShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.schedule(%s)".formatted(command));
    }

    @Override
    public Mono<Void> start(@NonNull StartShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.start(%s)".formatted(command));
    }

    @Override
    public Mono<Void> finish(@NonNull FinishShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.finish(%s)".formatted(command));
    }

    @Override
    public Mono<Void> remove(@NonNull RemoveShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.remove(%s)".formatted(command));
    }

    private Mono<Void> sendCommand(@NonNull ShowcaseCommand command) {
        return commandGateway
                       .<Void>send(command)
                       .onErrorMap(CommandExecutionException.class, e -> {
                           if (e.getDetails().isPresent()
                                       && e.getDetails().get() instanceof ShowcaseCommandErrorDetails errorDetails) {
                               return new ShowcaseCommandException(errorDetails);
                           } else {
                               return e;
                           }
                       });
    }
}
