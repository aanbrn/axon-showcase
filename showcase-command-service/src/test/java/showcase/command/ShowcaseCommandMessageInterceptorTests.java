// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static showcase.command.RandomCommandTestUtils.aRemoveShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aScheduleShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aTooShortShowcaseDuration;

import lombok.val;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventsourcing.AggregateDeletedException;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Showcase command message interceptor unit tests")
class ShowcaseCommandMessageInterceptorTests {

    @Mock
    private UnitOfWork<Message<?>> unitOfWork;

    @Mock
    private InterceptorChain interceptorChain;

    @Test
    @DisplayName("An invalid command is rejected with an invalid-command error when validation is enabled")
    void handle_validationEnabled_invalidCommand_isRejectedWithInvalidCommand() {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(true);
        val message = aCommandMessageWithTooShortDuration();
        doReturn(message).when(unitOfWork).getMessage();

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, interceptorChain))
                .isInstanceOf(ShowcaseCommandException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseCommandException) it;
                    assertThat(exception.getErrorDetails().errorCode())
                            .isEqualTo(ShowcaseCommandErrorCode.INVALID_COMMAND);
                    assertThat(exception.getErrorDetails().errorMessage()).isEqualTo("Given command is not valid");
                    assertThat(exception.getErrorDetails().metaData()).isNotEmpty();
                });
    }

    @Test
    @DisplayName("An invalid command proceeds without validation when validation is disabled")
    void handle_validationDisabled_invalidCommand_proceedsToHandler() throws Exception {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(false);

        interceptor.handle(unitOfWork, interceptorChain);

        verify(interceptorChain).proceed();
    }

    @Test
    @DisplayName("An unknown aggregate is translated to a not-found error when validation is disabled")
    void handle_validationDisabled_unknownAggregate_isTranslatedToNotFound() throws Exception {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(false);
        val message = aCommandMessage();
        doReturn(message).when(unitOfWork).getMessage();
        when(interceptorChain.proceed())
                .thenThrow(new AggregateNotFoundException("unknown-showcase", "No showcase with given ID"));

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, interceptorChain))
                .isInstanceOf(ShowcaseCommandException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseCommandException) it;
                    assertThat(exception.getErrorDetails().errorCode()).isEqualTo(ShowcaseCommandErrorCode.NOT_FOUND);
                    assertThat(exception.getErrorDetails().errorMessage()).isEqualTo("No showcase with given ID");
                });
    }

    @Test
    @DisplayName("A deleted aggregate is translated to an illegal-state error when validation is disabled")
    void handle_validationDisabled_deletedAggregate_isTranslatedToIllegalState() throws Exception {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(false);
        val message = aCommandMessage();
        doReturn(message).when(unitOfWork).getMessage();
        when(interceptorChain.proceed()).thenThrow(new AggregateDeletedException("deleted-showcase"));

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, interceptorChain))
                .isInstanceOf(ShowcaseCommandException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseCommandException) it;
                    assertThat(exception.getErrorDetails().errorCode())
                            .isEqualTo(ShowcaseCommandErrorCode.ILLEGAL_STATE);
                    assertThat(exception.getErrorDetails().errorMessage()).isEqualTo("Showcase is removed already");
                });
    }

    @Test
    @DisplayName("A missing aggregate for a remove command is ignored when validation is disabled")
    void handle_validationDisabled_missingAggregateForRemove_isIgnored() throws Exception {
        val interceptor = new ShowcaseCommandMessageInterceptor<>(false);
        val message = aRemoveCommandMessage();
        doReturn(message).when(unitOfWork).getMessage();
        when(interceptorChain.proceed())
                .thenThrow(new AggregateNotFoundException("unknown-showcase", "No showcase with given ID"));

        assertThat(interceptor.handle(unitOfWork, interceptorChain)).isNull();
    }

    private static CommandMessage<?> aCommandMessage() {
        return org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage(aScheduleShowcaseCommand());
    }

    private static CommandMessage<?> aCommandMessageWithTooShortDuration() {
        return org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage(
                aScheduleShowcaseCommand().toBuilder()
                        .duration(aTooShortShowcaseDuration())
                        .build());
    }

    private static CommandMessage<?> aRemoveCommandMessage() {
        return org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage(aRemoveShowcaseCommand());
    }
}
