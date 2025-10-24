package showcase.command;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
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

final class ShowcaseCommandMessageInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {

    private final BeanValidationInterceptor<T> beanValidationInterceptor = new BeanValidationInterceptor<>();

    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_HAS_CHECKED")
    @Override
    public @Nullable Object handle(UnitOfWork<? extends T> unitOfWork, InterceptorChain interceptorChain)
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
            throw new ShowcaseCommandException(
                    ShowcaseCommandErrorDetails
                            .builder()
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
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(ShowcaseCommandErrorCode.ILLEGAL_STATE)
                                .errorMessage("Showcase is removed already")
                                .build(),
                        e);
            } else {
                throw new ShowcaseCommandException(
                        ShowcaseCommandErrorDetails
                                .builder()
                                .errorCode(ShowcaseCommandErrorCode.NOT_FOUND)
                                .errorMessage("No showcase with given ID")
                                .build(),
                        e);
            }
        }
    }
}
