package showcase.command;

import lombok.val;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.messaging.DefaultInterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandler;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static showcase.command.RandomCommandTestUtils.aScheduleShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;

@DisplayName("Showcase command message interceptor component tests")
class ShowcaseCommandMessageInterceptorCT {

    @Test
    @DisplayName("An invalid command is rejected with an invalid-command error when validation is enabled")
    void handle_validationEnabled_invalidCommand_isRejectedWithInvalidCommand() {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(true);
        val message = aCommandMessageWithTooShortDuration();
        val handler = (MessageHandler<? super Message<?>>) __ -> null;
        val unitOfWork = DefaultUnitOfWork.startAndGet(message);
        val chain = new DefaultInterceptorChain<>(unitOfWork, List.of(), handler);

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, chain))
                .isInstanceOf(ShowcaseCommandException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseCommandException) it;
                    assertThat(exception.getErrorDetails().errorCode())
                            .isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                    assertThat(exception.getErrorDetails().errorMessage())
                            .isEqualTo("Given command is not valid");
                    assertThat(exception.getErrorDetails().metaData()).isNotEmpty();
                });
    }

    @Test
    @DisplayName("An invalid command proceeds without validation when validation is disabled")
    void handle_validationDisabled_invalidCommand_proceedsToHandler() throws Exception {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(false);
        val message = aCommandMessageWithTooShortDuration();
        val handledPayload = new AtomicReference<>();
        val handler = (MessageHandler<? super Message<?>>) msg -> {
            handledPayload.set(msg.getPayload());
            return null;
        };
        val unitOfWork = DefaultUnitOfWork.startAndGet(message);
        val chain = new DefaultInterceptorChain<>(unitOfWork, List.of(), handler);

        interceptor.handle(unitOfWork, chain);

        assertThat(handledPayload.get()).isInstanceOf(ScheduleShowcaseCommand.class);
    }

    @Test
    @DisplayName("An unknown aggregate is translated to a not-found error when validation is disabled")
    void handle_validationDisabled_unknownAggregate_isTranslatedToNotFound() {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(false);
        val message = GenericCommandMessage.asCommandMessage(aScheduleShowcaseCommand());
        val handler = (MessageHandler<? super Message<?>>) __ -> {
            throw new AggregateNotFoundException("unknown-showcase", "No showcase with given ID");
        };
        val unitOfWork = DefaultUnitOfWork.startAndGet(message);
        val chain = new DefaultInterceptorChain<>(unitOfWork, List.of(), handler);

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, chain))
                .isInstanceOf(ShowcaseCommandException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseCommandException) it;
                    assertThat(exception.getErrorDetails().errorCode())
                            .isEqualTo(ShowcaseCommandErrorCode.NOT_FOUND);
                    assertThat(exception.getErrorDetails().errorMessage())
                            .isEqualTo("No showcase with given ID");
                });
    }

    private static CommandMessage<?> aCommandMessageWithTooShortDuration() {
        return GenericCommandMessage.asCommandMessage(
                aScheduleShowcaseCommand().toBuilder().duration(aTooShortShowcaseDuration()).build());
    }
}
