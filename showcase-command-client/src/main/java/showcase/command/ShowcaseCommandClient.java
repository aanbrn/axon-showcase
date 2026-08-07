package showcase.command;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static showcase.command.ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE;

/**
 * Reactive client sending showcase commands through the Axon reactor command gateway, protected by Resilience4j
 * time limiter, circuit breaker, and retry.
 */
@Component
@RequiredArgsConstructor
@TimeLimiter(name = SHOWCASE_COMMAND_SERVICE)
@CircuitBreaker(name = SHOWCASE_COMMAND_SERVICE)
@Retry(name = SHOWCASE_COMMAND_SERVICE)
class ShowcaseCommandClient implements ShowcaseCommandOperations {
    /**
     * The reactor command gateway used to dispatch commands.
     */
    private final ReactorCommandGateway commandGateway;

    /**
     * Sends a schedule command.
     *
     * @param command the schedule command to send
     * @return a mono completing when the command is handled
     */
    @Override
    public Mono<Void> schedule(ScheduleShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.schedule(%s)".formatted(command));
    }

    /**
     * Sends a start command.
     *
     * @param command the start command to send
     * @return a mono completing when the command is handled
     */
    @Override
    public Mono<Void> start(StartShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.start(%s)".formatted(command));
    }

    /**
     * Sends a finish command.
     *
     * @param command the finish command to send
     * @return a mono completing when the command is handled
     */
    @Override
    public Mono<Void> finish(FinishShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.finish(%s)".formatted(command));
    }

    /**
     * Sends a remove command.
     *
     * @param command the remove command to send
     * @return a mono completing when the command is handled
     */
    @Override
    public Mono<Void> remove(RemoveShowcaseCommand command) {
        return sendCommand(command).checkpoint("ShowcaseCommandClient.remove(%s)".formatted(command));
    }

    /**
     * Dispatches the command on a bounded-elastic scheduler and maps {@link CommandExecutionException}s carrying
     * {@link ShowcaseCommandErrorDetails} to {@link ShowcaseCommandException}s.
     *
     * @param command the command to send
     * @return a mono completing when the command is handled
     */
    private Mono<Void> sendCommand(ShowcaseCommand command) {
        return commandGateway
                       .<Void>send(command)
                       .subscribeOn(Schedulers.boundedElastic())
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
