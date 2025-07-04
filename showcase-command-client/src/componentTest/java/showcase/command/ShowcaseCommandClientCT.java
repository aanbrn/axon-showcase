package showcase.command;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.NonNull;
import lombok.val;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.axonframework.messaging.RemoteExceptionDescription;
import org.axonframework.messaging.RemoteHandlingException;
import org.axonframework.messaging.RemoteNonTransientHandlingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;
import static org.axonframework.commandhandling.GenericCommandResultMessage.asCommandResultMessage;
import static org.axonframework.messaging.GenericMessage.asMessage;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static showcase.command.RandomCommandTestUtils.aFinishShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aRemoveShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aScheduleShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aShowcaseCommandErrorDetails;
import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aStartShowcaseCommand;
import static showcase.command.ShowcaseCommandClient.SHOWCASE_COMMAND_SERVICE;
import static showcase.test.RandomTestUtils.anAlphabeticString;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ShowcaseCommandClientCT {

    @SpringBootApplication
    static class TestApp {
    }

    static List<Arguments> transientErrors() {
        return List.of(
                argumentSet(
                        "No Handler Error",
                        new NoHandlerForCommandException(anAlphabeticString(10))),
                argumentSet(
                        "Dispatch Error",
                        new CommandDispatchException(
                                anAlphabeticString(10), new RuntimeException(anAlphabeticString(10)))),
                argumentSet(
                        "Remote Transient Error",
                        new CommandExecutionException(
                                anAlphabeticString(10),
                                new RemoteHandlingException(
                                        RemoteExceptionDescription.describing(
                                                new RuntimeException(anAlphabeticString(10))))))
        );
    }

    static List<Arguments> nonTransientErrors() {
        return List.of(
                argumentSet(
                        "Remote Non-Transient Error",
                        new CommandExecutionException(
                                anAlphabeticString(10),
                                new RemoteNonTransientHandlingException(
                                        RemoteExceptionDescription.describing(
                                                new RuntimeException(anAlphabeticString(10))))))
        );
    }

    @MockitoBean
    @SuppressWarnings("unused")
    private CommandBus commandBus;

    @Autowired
    private ShowcaseCommandClient showcaseCommandClient;

    @Autowired(required = false)
    private RetryRegistry retryRegistry;

    @Test
    void scheduleShowcase_success_emitsShowcaseId() {
        val command =
                ScheduleShowcaseCommand
                        .builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(aShowcaseStartTime(Instant.now()))
                        .duration(aShowcaseDuration())
                        .build();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.schedule(command))
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void scheduleShowcase_showcaseCommandError_emitsErrorWithShowcaseCommandExceptionCausedByHappenedError() {
        val command = aScheduleShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.schedule(command))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("transientErrors")
    void scheduleShowcase_transientError_retriesAndEmitsError(@NonNull Throwable error) {
        assumeTrue(retryRegistry != null, "Retry is disabled");

        val command = aScheduleShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.schedule(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus, times(retryRegistry.retry(SHOWCASE_COMMAND_SERVICE).getRetryConfig().getMaxAttempts()))
                .dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("nonTransientErrors")
    void scheduleShowcase_nonTransientError_emitsError(@NonNull Throwable error) {
        val command = aScheduleShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.schedule(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void startShowcase_success_emitsNothing() {
        val command = aStartShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.start(command))
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void startShowcase_showcaseCommandError_emitsErrorWithShowcaseCommandExceptionCausedByHappenedError() {
        val command = aStartShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.start(command))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("transientErrors")
    void startShowcase_transientError_retriesAndEmitsError(@NonNull Throwable error) {
        assumeTrue(retryRegistry != null, "Retry is disabled");

        val command = aStartShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.start(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus, times(retryRegistry.retry(SHOWCASE_COMMAND_SERVICE).getRetryConfig().getMaxAttempts()))
                .dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("nonTransientErrors")
    void startShowcase_nonTransientError_emitsError(@NonNull Throwable error) {
        val command = aStartShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.start(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void finishShowcase_success_emitsNothing() {
        val command = aFinishShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.finish(command))
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void finishShowcase_showcaseCommandError_emitsErrorWithShowcaseCommandExceptionCausedByHappenedError() {
        val command = aFinishShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.finish(command))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("transientErrors")
    void finishShowcase_transientError_retriesAndEmitsError(@NonNull Throwable error) {
        assumeTrue(retryRegistry != null, "Retry is disabled");

        val command = aFinishShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.finish(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus, times(retryRegistry.retry(SHOWCASE_COMMAND_SERVICE).getRetryConfig().getMaxAttempts()))
                .dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("nonTransientErrors")
    void finishShowcase_nonTransientError_emitsError(@NonNull Throwable error) {
        val command = aFinishShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.finish(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void removeShowcase_success_emitsNothing() {
        val command = aRemoveShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.remove(command))
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void removeShowcase_showcaseCommandError_emitsErrorWithShowcaseCommandExceptionCausedByHappenedError() {
        val command = aRemoveShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.remove(command))
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("transientErrors")
    void removeShowcase_transientError_retriesAndEmitsError(@NonNull Throwable error) {
        assumeTrue(retryRegistry != null, "Retry is disabled");

        val command = aRemoveShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.remove(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus, times(retryRegistry.retry(SHOWCASE_COMMAND_SERVICE).getRetryConfig().getMaxAttempts()))
                .dispatch(any(), any());
    }

    @ParameterizedTest
    @MethodSource("nonTransientErrors")
    void removeShowcase_nonTransientError_emitsError(@NonNull Throwable error) {
        val command = aRemoveShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                    return true;
                }));

        StepVerifier
                .create(showcaseCommandClient.remove(command))
                .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }
}
