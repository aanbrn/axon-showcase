package showcase.command;

import lombok.experimental.UtilityClass;
import org.axonframework.messaging.MetaData;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.Map;

@UtilityClass
public class ShowcaseCommandMatchers {

    public static Matcher<ShowcaseCommandErrorDetails> aCommandErrorDetailsWithErrorCode(
            Matcher<ShowcaseCommandErrorCode> matcher) {
        return new FeatureMatcher<>(matcher, "A command error details with error code", "errorCode") {
            @Override
            protected ShowcaseCommandErrorCode featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.errorCode();
            }
        };
    }

    public static Matcher<ShowcaseCommandErrorDetails> aCommandErrorDetailsWithErrorMessage(Matcher<String> matcher) {
        return new FeatureMatcher<>(matcher, "A command error details with error message", "errorCode") {
            @Override
            protected String featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.errorMessage();
            }
        };
    }

    public static Matcher<ShowcaseCommandErrorDetails> aCommandErrorDetailsWithMetaData(Matcher<Map<?, ?>> matcher) {
        return new FeatureMatcher<>(matcher, "A command error details with metadata", "metaData") {
            @Override
            protected MetaData featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.metaData();
            }
        };
    }
}
