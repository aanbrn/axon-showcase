package showcase.query;

import lombok.val;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.queryhandling.GenericQueryMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Showcase query message interceptor unit tests")
class ShowcaseQueryMessageInterceptorTests {

    @Mock
    private UnitOfWork<Message<?>> unitOfWork;

    @Mock
    private InterceptorChain interceptorChain;

    @Test
    @DisplayName("An invalid query is rejected with an invalid-query error when validation is enabled")
    void handle_validationEnabled_invalidQuery_isRejectedWithInvalidQuery() {
        val interceptor = new ShowcaseQueryMessageInterceptor<>(true);
        val message = aQueryMessageWithInvalidSize();
        doReturn(message).when(unitOfWork).getMessage();

        assertThatThrownBy(() -> interceptor.handle(unitOfWork, interceptorChain))
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

        interceptor.handle(unitOfWork, interceptorChain);

        verify(interceptorChain).proceed();
    }

    private static GenericQueryMessage<FetchShowcaseListQuery, ?> aQueryMessageWithInvalidSize() {
        val invalidQuery = FetchShowcaseListQuery.builder().size(0).build();
        return new GenericQueryMessage<>(invalidQuery, ResponseTypes.multipleInstancesOf(Showcase.class));
    }
}
