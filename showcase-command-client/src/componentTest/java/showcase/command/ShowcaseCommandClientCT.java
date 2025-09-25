package showcase.command;

import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.NonNull;
import lombok.val;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.axonframework.messaging.RemoteExceptionDescription;
import org.axonframework.messaging.RemoteHandlingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;
import static org.axonframework.commandhandling.GenericCommandResultMessage.asCommandResultMessage;
import static org.axonframework.messaging.GenericMessage.asMessage;
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
import static showcase.command.ShowcaseCommandOperations.SHOWCASE_COMMAND_SERVICE;
import static showcase.test.RandomTestUtils.anAlphabeticString;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ShowcaseCommandClientCT {

    @SpringBootApplication
    static class TestApp {
    }

    @MockitoBean
    @SuppressWarnings("unused")
    private CommandBus commandBus;

    @Autowired
    private ShowcaseCommandClient showcaseCommandClient;

    @Test
    void scheduleShowcase_successfulCommandDispatch_succeeds() {
        // given:
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

        // when:
        val scheduleMono = showcaseCommandClient.schedule(command);

        // then:
        StepVerifier
                .create(scheduleMono)
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void scheduleShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        // given:
        val command = aScheduleShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        // when:
        val scheduleMono = showcaseCommandClient.schedule(command);

        // then:
        StepVerifier
                .create(scheduleMono)
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void startShowcase_successfulCommandDispatch_succeeds() {
        // given:
        val command = aStartShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        // when:
        val startMono = showcaseCommandClient.start(command);

        // then:
        StepVerifier
                .create(startMono)
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void startShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        // given:
        val command = aStartShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        // when:
        val startMono = showcaseCommandClient.start(command);

        // then:
        StepVerifier
                .create(startMono)
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void finishShowcase_successfulCommandDispatch_succeeds() {
        // ginen:
        val command = aFinishShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        // when:
        val finishMono = showcaseCommandClient.finish(command);

        // then:
        StepVerifier
                .create(finishMono)
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void finishShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        // given:
        val command = aFinishShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        // when:
        val finishMono = showcaseCommandClient.finish(command);

        // then:
        StepVerifier
                .create(finishMono)
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void removeShowcase_successfulCommandDispatch_succeeds() {
        // given:
        val command = aRemoveShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
                    return true;
                }));

        // when:
        val removeMono = showcaseCommandClient.remove(command);

        // then:
        StepVerifier
                .create(removeMono)
                .expectComplete()
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    void removeShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        // given:
        val command = aRemoveShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(
                any(),
                argThat(callback -> {
                    callback.onResult(asCommandMessage(command), asCommandResultMessage(
                            new CommandExecutionException(null, null, errorDetails)));
                    return true;
                }));

        // when:
        val removeMono = showcaseCommandClient.remove(command);

        // then:
        StepVerifier
                .create(removeMono)
                .expectErrorSatisfies(
                        t -> assertThat(t)
                                     .isExactlyInstanceOf(ShowcaseCommandException.class)
                                     .asInstanceOf(type(ShowcaseCommandException.class))
                                     .extracting(ShowcaseCommandException::getErrorDetails)
                                     .isEqualTo(errorDetails))
                .verify();

        verify(commandBus).dispatch(any(), any());
    }

    @Nested
    @ActiveProfiles("retry")
    @DirtiesContext
    class Retry {

        static List<Arguments> retryableErrors() {
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

        @MockitoBean(enforceOverride = true)
        @SuppressWarnings("unused")
        private CommandBus commandBus;

        @Autowired
        private ShowcaseCommandClient showcaseCommandClient;

        @Autowired
        private RetryRegistry retryRegistry;

        private int maxAttempts;

        private Duration timeout;

        @BeforeEach
        void setUp() {
            val retryConfig = retryRegistry.retry(SHOWCASE_COMMAND_SERVICE).getRetryConfig();

            maxAttempts = retryConfig.getMaxAttempts();
            timeout = IntStream.rangeClosed(1, maxAttempts)
                               .mapToLong(i -> retryConfig.getIntervalBiFunction()
                                                          .apply(i, Either.left(null)))
                               .mapToObj(Duration::ofMillis)
                               .reduce(Duration.ZERO, Duration::plus)
                               .plusSeconds(1);
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        void scheduleShowcase_retryableError_retriesAndFailsWithError(@NonNull Throwable error) {
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
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        void startShowcase_retryableError_retriesAndFailsWithError(@NonNull Throwable error) {
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
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        void finishShowcase_retryableError_retriesAndFailsWithError(@NonNull Throwable error) {
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
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        void removeShowcase_retryableError_retriesAndFailsWithError(@NonNull Throwable error) {
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
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }
    }
}
