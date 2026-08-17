package showcase.query;

import lombok.val;
import org.axonframework.messaging.DefaultInterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.queryhandling.GenericQueryMessage;
import org.axonframework.queryhandling.QueryMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Showcase query message interceptor component tests")
class ShowcaseQueryMessageInterceptorCT {

    @Test
    @DisplayName("An invalid query is rejected with an invalid-query error when validation is enabled")
    void handle_validationEnabled_invalidQuery_isRejectedWithInvalidQuery() {
        val interceptor = new ShowcaseQueryMessageInterceptor<>(true);
        val message = aQueryMessageWithInvalidSize();
        val handler = (MessageHandler<? super Message<?>>) __ -> null;
        val unitOfWork = DefaultUnitOfWork.startAndGet(message);
        val chain = new DefaultInterceptorChain<>(unitOfWork, List.of(), handler);

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, chain))
                .isInstanceOf(ShowcaseQueryException.class)
                .satisfies(it -> {
                    val exception = (ShowcaseQueryException) it;
                    assertThat(exception.getErrorDetails().errorCode())
                            .isEqualTo(ShowcaseQueryErrorCode.INVALID_QUERY);
                    assertThat(exception.getErrorDetails().errorMessage())
                            .isEqualTo("Given query is not valid");
                    assertThat(exception.getErrorDetails().metaData()).isNotEmpty();
                });
    }

    @Test
    @DisplayName("An invalid query proceeds without validation when validation is disabled")
    void handle_validationDisabled_invalidQuery_proceedsToHandler() throws Exception {
        val interceptor = new ShowcaseQueryMessageInterceptor<>(false);
        val message = aQueryMessageWithInvalidSize();
        val handledPayload = new AtomicReference<>();
        val handler = (MessageHandler<? super Message<?>>) msg -> {
            handledPayload.set(msg.getPayload());
            return null;
        };
        val unitOfWork = DefaultUnitOfWork.startAndGet(message);
        val chain = new DefaultInterceptorChain<>(unitOfWork, List.of(), handler);

        interceptor.handle(unitOfWork, chain);

        assertThat(handledPayload.get()).isInstanceOf(FetchShowcaseListQuery.class);
    }

    private static QueryMessage<FetchShowcaseListQuery, ?> aQueryMessageWithInvalidSize() {
        val invalidQuery = FetchShowcaseListQuery.builder().size(0).build();
        return new GenericQueryMessage<>(invalidQuery, ResponseTypes.multipleInstancesOf(Showcase.class));
    }
}
