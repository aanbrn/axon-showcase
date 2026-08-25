// SPDX-License-Identifier: MIT
package showcase.command;

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

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import lombok.val;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.axonframework.messaging.RemoteExceptionDescription;
import org.axonframework.messaging.RemoteHandlingException;
import org.axonframework.messaging.RemoteNonTransientHandlingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.blockhound.BlockHound;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayName("Showcase command client component tests")
class ShowcaseCommandClientCT {

    @SpringBootApplication
    static class TestApp {}

    @MockitoBean
    private CommandBus commandBus;

    @Autowired
    private ShowcaseCommandClient showcaseCommandClient;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @Test
    @DisplayName("Scheduling a showcase with a successful dispatch succeeds")
    void scheduleShowcase_successfulCommandDispatch_succeeds() {
        val command = ScheduleShowcaseCommand.builder()
                .showcaseId(aShowcaseId())
                .title(aShowcaseTitle())
                .startTime(aShowcaseStartTime(Instant.now()))
                .duration(aShowcaseDuration())
                .build();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
            return true;
        }));

        showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyComplete();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Scheduling a showcase with a failed dispatch fails with a showcase command exception")
    void scheduleShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        val command = aScheduleShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(
                    asCommandMessage(command),
                    asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
            return true;
        }));

        showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isExactlyInstanceOf(ShowcaseCommandException.class)
                .asInstanceOf(type(ShowcaseCommandException.class))
                .extracting(ShowcaseCommandException::getErrorDetails)
                .isEqualTo(errorDetails));

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName(
            "Scheduling a showcase with a failed dispatch carrying non-showcase details fails with the raw exception")
    void scheduleShowcase_failedDispatchWithNonShowcaseDetails_failsWithRawException() {
        val command = aScheduleShowcaseCommand();
        val commandExecutionException = new CommandExecutionException(null, null, anAlphabeticString(10));

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(asCommandMessage(command), asCommandResultMessage(commandExecutionException));
            return true;
        }));

        showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isEqualTo(commandExecutionException));

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Starting a showcase with a successful dispatch succeeds")
    void startShowcase_successfulCommandDispatch_succeeds() {
        val command = aStartShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
            return true;
        }));

        showcaseCommandClient.start(command).as(StepVerifier::create).verifyComplete();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Starting a showcase with a failed dispatch fails with a showcase command exception")
    void startShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        val command = aStartShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(
                    asCommandMessage(command),
                    asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
            return true;
        }));

        showcaseCommandClient.start(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isExactlyInstanceOf(ShowcaseCommandException.class)
                .asInstanceOf(type(ShowcaseCommandException.class))
                .extracting(ShowcaseCommandException::getErrorDetails)
                .isEqualTo(errorDetails));

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Finishing a showcase with a successful dispatch succeeds")
    void finishShowcase_successfulCommandDispatch_succeeds() {
        val command = aFinishShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
            return true;
        }));

        showcaseCommandClient.finish(command).as(StepVerifier::create).verifyComplete();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Finishing a showcase with a failed dispatch fails with a showcase command exception")
    void finishShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        val command = aFinishShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(
                    asCommandMessage(command),
                    asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
            return true;
        }));

        showcaseCommandClient.finish(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isExactlyInstanceOf(ShowcaseCommandException.class)
                .asInstanceOf(type(ShowcaseCommandException.class))
                .extracting(ShowcaseCommandException::getErrorDetails)
                .isEqualTo(errorDetails));

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Removing a showcase with a successful dispatch succeeds")
    void removeShowcase_successfulCommandDispatch_succeeds() {
        val command = aRemoveShowcaseCommand();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(asCommandMessage(command), asCommandResultMessage(asMessage(null)));
            return true;
        }));

        showcaseCommandClient.remove(command).as(StepVerifier::create).verifyComplete();

        verify(commandBus).dispatch(any(), any());
    }

    @Test
    @DisplayName("Removing a showcase with a failed dispatch fails with a showcase command exception")
    void removeShowcase_failedCommandDispatch_failsWithShowcaseCommandException() {
        val command = aRemoveShowcaseCommand();
        val errorDetails = aShowcaseCommandErrorDetails();

        willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
            callback.onResult(
                    asCommandMessage(command),
                    asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
            return true;
        }));

        showcaseCommandClient.remove(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                .isExactlyInstanceOf(ShowcaseCommandException.class)
                .asInstanceOf(type(ShowcaseCommandException.class))
                .extracting(ShowcaseCommandException::getErrorDetails)
                .isEqualTo(errorDetails));

        verify(commandBus).dispatch(any(), any());
    }

    @Nested
    @ActiveProfiles("retry")
    @DisplayName("Retry")
    class RetryBehavior {

        static List<Arguments> retryableErrors() {
            return List.of(
                    argumentSet("No Handler Error", new NoHandlerForCommandException(anAlphabeticString(10))),
                    argumentSet(
                            "Dispatch Error",
                            new CommandDispatchException(
                                    anAlphabeticString(10), new RuntimeException(anAlphabeticString(10)))),
                    argumentSet(
                            "Remote Transient Error",
                            new CommandExecutionException(
                                    anAlphabeticString(10),
                                    new RemoteHandlingException(RemoteExceptionDescription.describing(
                                            new RuntimeException(anAlphabeticString(10)))))));
        }

        @MockitoBean(enforceOverride = true)
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
                    .mapToLong(i -> retryConfig.getIntervalBiFunction().apply(i, Either.left(null)))
                    .mapToObj(Duration::ofMillis)
                    .reduce(Duration.ZERO, Duration::plus)
                    .plusSeconds(1);
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        @DisplayName("Scheduling a showcase with a retryable error retries and fails with the error")
        void scheduleShowcase_retryableError_retriesAndFailsWithError(Throwable error) {
            val command = aScheduleShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                return true;
            }));

            showcaseCommandClient
                    .schedule(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        @DisplayName("Starting a showcase with a retryable error retries and fails with the error")
        void startShowcase_retryableError_retriesAndFailsWithError(Throwable error) {
            val command = aStartShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                return true;
            }));

            showcaseCommandClient
                    .start(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        @DisplayName("Finishing a showcase with a retryable error retries and fails with the error")
        void finishShowcase_retryableError_retriesAndFailsWithError(Throwable error) {
            val command = aFinishShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                return true;
            }));

            showcaseCommandClient
                    .finish(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @ParameterizedTest
        @MethodSource("retryableErrors")
        @DisplayName("Removing a showcase with a retryable error retries and fails with the error")
        void removeShowcase_retryableError_retriesAndFailsWithError(Throwable error) {
            val command = aRemoveShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                return true;
            }));

            showcaseCommandClient
                    .remove(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                    .verify(timeout);

            verify(commandBus, times(maxAttempts)).dispatch(any(), any());
        }

        @Test
        @DisplayName("Scheduling a showcase with a showcase command exception is not retried")
        void scheduleShowcase_showcaseCommandException_isNotRetried() {
            val command = aScheduleShowcaseCommand();
            val errorDetails = aShowcaseCommandErrorDetails();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(
                        asCommandMessage(command),
                        asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
                return true;
            }));

            showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                    .isExactlyInstanceOf(ShowcaseCommandException.class)
                    .asInstanceOf(type(ShowcaseCommandException.class))
                    .extracting(ShowcaseCommandException::getErrorDetails)
                    .isEqualTo(errorDetails));

            verify(commandBus, times(1)).dispatch(any(), any());
        }

        static List<Arguments> nonTransientExceptions() {
            return List.of(
                    argumentSet(
                            "Non-Transient Exception",
                            new RemoteNonTransientHandlingException(RemoteExceptionDescription.describing(
                                    new RuntimeException(anAlphabeticString(10))))),
                    argumentSet(
                            "Non-Transient Cause",
                            new CommandExecutionException(
                                    anAlphabeticString(10),
                                    new RemoteNonTransientHandlingException(RemoteExceptionDescription.describing(
                                            new RuntimeException(anAlphabeticString(10)))))));
        }

        @ParameterizedTest
        @MethodSource("nonTransientExceptions")
        @DisplayName("Scheduling a showcase with a non-transient error is not retried and fails with the error")
        void scheduleShowcase_nonTransientError_isNotRetriedAndFailsWithError(Throwable error) {
            val command = aScheduleShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(asCommandMessage(command), asCommandResultMessage(error));
                return true;
            }));

            showcaseCommandClient
                    .schedule(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t).isEqualTo(error))
                    .verify(Duration.ofSeconds(1));

            verify(commandBus, times(1)).dispatch(any(), any());
        }
    }

    @Nested
    @ActiveProfiles("timelimiter")
    @DisplayName("Time limiter")
    class TimeLimiterBehavior {

        @MockitoBean(enforceOverride = true)
        private CommandBus commandBus;

        @Autowired
        private ShowcaseCommandClient showcaseCommandClient;

        @Test
        @DisplayName("Scheduling a showcase whose dispatch never completes fails with a timeout error")
        void scheduleShowcase_hangingDispatch_failsWithTimeoutError() {
            val command = aScheduleShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), any());

            showcaseCommandClient
                    .schedule(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t).isInstanceOf(TimeoutException.class))
                    .verify(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("Scheduling a showcase with a business error completes without timing out")
        void scheduleShowcase_businessError_completesWithoutTimeout() {
            val command = aScheduleShowcaseCommand();
            val errorDetails = aShowcaseCommandErrorDetails();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(
                        asCommandMessage(command),
                        asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
                return true;
            }));

            showcaseCommandClient
                    .schedule(command)
                    .as(StepVerifier::create)
                    .expectErrorSatisfies(t -> assertThat(t)
                            .isExactlyInstanceOf(ShowcaseCommandException.class)
                            .asInstanceOf(type(ShowcaseCommandException.class))
                            .extracting(ShowcaseCommandException::getErrorDetails)
                            .isEqualTo(errorDetails))
                    .verify(Duration.ofSeconds(5));
        }
    }

    @Nested
    @ActiveProfiles("circuitbreaker")
    @DisplayName("Circuit breaker")
    class CircuitBreakerBehavior {

        @MockitoBean(enforceOverride = true)
        private CommandBus commandBus;

        @Autowired
        private ShowcaseCommandClient showcaseCommandClient;

        @Autowired
        private CircuitBreakerRegistry circuitBreakerRegistry;

        @BeforeEach
        void resetCircuitBreaker() {
            circuitBreakerRegistry.circuitBreaker(SHOWCASE_COMMAND_SERVICE).reset();
        }

        @Test
        @DisplayName("Scheduling a showcase opens the circuit after repeated failures and then fails fast")
        void scheduleShowcase_repeatedFailures_openCircuitAndFailFast() {
            val command = aScheduleShowcaseCommand();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(
                        asCommandMessage(command),
                        asCommandResultMessage(new CommandExecutionException(anAlphabeticString(10), null)));
                return true;
            }));

            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(SHOWCASE_COMMAND_SERVICE);
            val minimumNumberOfCalls = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();

            for (int i = 0; i < minimumNumberOfCalls; i++) {
                showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyError();
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

            showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyErrorSatisfies(t -> assertThat(t)
                    .isInstanceOf(CallNotPermittedException.class));
        }

        @Test
        @DisplayName("Scheduling a showcase with business errors keeps the circuit closed")
        void scheduleShowcase_businessErrors_keepCircuitClosed() {
            val command = aScheduleShowcaseCommand();
            val errorDetails = aShowcaseCommandErrorDetails();

            willDoNothing().given(commandBus).dispatch(any(), argThat(callback -> {
                callback.onResult(
                        asCommandMessage(command),
                        asCommandResultMessage(new CommandExecutionException(null, null, errorDetails)));
                return true;
            }));

            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(SHOWCASE_COMMAND_SERVICE);
            val minimumNumberOfCalls = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();

            for (int i = 0; i < minimumNumberOfCalls; i++) {
                showcaseCommandClient.schedule(command).as(StepVerifier::create).verifyError();
            }

            assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }
    }
}
