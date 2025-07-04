package showcase.query;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.NonNull;
import lombok.val;
import one.util.streamex.StreamEx;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.messaging.interceptors.JSR303ViolationException;
import org.axonframework.messaging.unitofwork.UnitOfWork;

final class ShowcaseQueryMessageInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {

    private final BeanValidationInterceptor<T> beanValidationInterceptor = new BeanValidationInterceptor<>();

    @Override
    public Object handle(@NonNull UnitOfWork<? extends T> unitOfWork, @NonNull InterceptorChain interceptorChain)
            throws Exception {
        try {
            return beanValidationInterceptor.handle(unitOfWork, interceptorChain);
        } catch (JSR303ViolationException e) {
            val fieldErrors =
                    StreamEx.of(e.getViolations())
                            .mapToEntry(ConstraintViolation::getPropertyPath,
                                        ConstraintViolation::getMessage)
                            .mapKeys(Path::toString)
                            .collapseKeys()
                            .toMap();
            throw new ShowcaseQueryException(
                    ShowcaseQueryErrorDetails
                            .builder()
                            .errorCode(ShowcaseQueryErrorCode.INVALID_QUERY)
                            .errorMessage("Given query is not valid")
                            .metaData(MetaData.from(fieldErrors))
                            .build(),
                    e);
        }
    }
}
