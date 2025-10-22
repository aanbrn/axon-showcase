package showcase.command;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.axonframework.messaging.MetaData;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.Map;

@UtilityClass
public class ShowcaseCommandErrorDetailsMatchers {

    public static FeatureMatcher<ShowcaseCommandErrorDetails, ShowcaseCommandErrorCode> hasErrorCode(
            @NonNull Matcher<ShowcaseCommandErrorCode> matcher) {
        return new FeatureMatcher<>(matcher, "An error details with error code", "errorCode") {
            @Override
            protected ShowcaseCommandErrorCode featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.errorCode();
            }
        };
    }

    public static FeatureMatcher<ShowcaseCommandErrorDetails, String> hasErrorMessage(
            @NonNull Matcher<String> matcher) {
        return new FeatureMatcher<>(matcher, "An error details with error message", "errorCode") {
            @Override
            protected String featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.errorMessage();
            }
        };
    }

    public static FeatureMatcher<ShowcaseCommandErrorDetails, Map<?, ?>> hasMetaData(
            @NonNull Matcher<Map<?, ?>> matcher) {
        return new FeatureMatcher<>(matcher, "An error details with metadata", "metaData") {
            @Override
            protected MetaData featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.metaData();
            }
        };
    }
}
