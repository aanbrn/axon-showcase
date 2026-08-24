// SPDX-License-Identifier: MIT
package showcase.query;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.val;
import one.util.streamex.StreamEx;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.messaging.interceptors.JSR303ViolationException;
import org.axonframework.messaging.unitofwork.UnitOfWork;

/**
 * Intercepts query handling to translate validation failures into {@link ShowcaseQueryException}s.
 *
 * @param <T> the message type handled by this interceptor
 */
@RequiredArgsConstructor
final class ShowcaseQueryMessageInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {
    /**
     * The interceptor performing bean validation on the query payload.
     */
    private final BeanValidationInterceptor<T> beanValidationInterceptor = new BeanValidationInterceptor<>();

    /**
     * Whether bean validation is applied to query payloads.
     */
    private final boolean validationEnabled;

    /**
     * Validates the query and maps validation violations to {@link ShowcaseQueryErrorCode#INVALID_QUERY} errors.
     *
     * <p>When validation is disabled, validation is skipped and the query proceeds without validation.
     *
     * @param unitOfWork       the unit of work for the query
     * @param interceptorChain the chain to proceed with
     * @return the result of the next interceptor in the chain
     * @throws Exception if the query processing fails
     */
    @Override
    public Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain) throws Exception {
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
            throw new ShowcaseQueryException(
                    ShowcaseQueryErrorDetails.builder()
                            .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                            .errorMessage("Given query is not valid")
                            .metaData(MetaData.from(fieldErrors))
                            .build(),
                    e);
        }
    }
}
