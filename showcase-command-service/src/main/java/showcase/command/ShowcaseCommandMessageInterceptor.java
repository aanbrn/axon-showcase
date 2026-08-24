// SPDX-License-Identifier: MIT
package showcase.command;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.val;
import one.util.streamex.StreamEx;
import org.axonframework.eventsourcing.AggregateDeletedException;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.messaging.interceptors.JSR303ViolationException;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.jspecify.annotations.Nullable;

/**
 * Intercepts command handling to translate Axon and validation failures into {@link ShowcaseCommandException}s.
 *
 * @param <T> the message type handled by this interceptor
 */
@RequiredArgsConstructor
final class ShowcaseCommandMessageInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {
    /**
     * The interceptor performing bean validation on the command payload.
     */
    private final BeanValidationInterceptor<T> beanValidationInterceptor = new BeanValidationInterceptor<>();

    /**
     * Whether bean validation is applied to command payloads.
     */
    private final boolean validationEnabled;

    /**
     * Validates the command and maps known failures to {@link ShowcaseCommandException}s.
     *
     * <p>When validation is disabled, validation is skipped but error translation still applies. Validation violations
     * produce an {@link ShowcaseCommandErrorCode#INVALID_COMMAND} error, missing aggregates produce
     * {@link ShowcaseCommandErrorCode#NOT_FOUND} (except removes, which are ignored), and deleted aggregates produce
     * {@link ShowcaseCommandErrorCode#ILLEGAL_STATE}.
     *
     * @param unitOfWork       the unit of work for the command
     * @param interceptorChain the chain to proceed with
     * @return the result of the next interceptor in the chain
     * @throws Exception if the command processing fails
     */
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_HAS_CHECKED")
    @Override
    public @Nullable Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain)
            throws Exception {
        try {
            if (validationEnabled) {
                return beanValidationInterceptor.handle(unitOfWork, interceptorChain);
            }
            return interceptorChain.proceed();
        } catch (JSR303ViolationException e) {
            val fieldErrors = StreamEx.of(e.getViolations())
                    .mapToEntry(ConstraintViolation::getPropertyPath, ConstraintViolation::getMessage)
                    .mapKeys(Path::toString)
                    .collapseKeys()
                    .toMap();
            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails.builder()
                            .errorCode(ShowcaseCommandErrorCode.INVALID_COMMAND)
                            .errorMessage("Given command is not valid")
                            .metaData(MetaData.from(fieldErrors))
                            .build(),
                    e);
        } catch (AggregateNotFoundException e) {
            if (unitOfWork.getMessage().getPayload() instanceof RemoveShowcaseCommand) {
                return null;
            } else if (e instanceof AggregateDeletedException) {
                throw new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails.builder()
                                .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                                .errorMessage("Showcase is removed already")
                                .build(),
                        e);
            } else {
                throw new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails.builder()
                                .errorCode(ShowcaseCommandErrorCode.NOT_FOUND)
                                .errorMessage("No showcase with given ID")
                                .build(),
                        e);
            }
        }
    }
}
