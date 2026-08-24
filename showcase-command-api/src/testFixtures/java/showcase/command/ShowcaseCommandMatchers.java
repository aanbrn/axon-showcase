// SPDX-License-Identifier: MIT
package showcase.command;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.axonframework.messaging.MetaData;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/**
 * Matchers for asserting on {@link ShowcaseCommandErrorDetails} values.
 */
@UtilityClass
public class ShowcaseCommandMatchers {
    /**
     * Creates a matcher checking the error code of the given error details.
     *
     * @param matcher the matcher for the error code
     * @return a matcher on the error details error code
     */
    public static Matcher<ShowcaseCommandErrorDetails> aCommandErrorDetailsWithErrorCode(
            Matcher<ShowcaseCommandErrorCode> matcher) {
        return new FeatureMatcher<>(matcher, "A command error details with error code", "errorCode") {
            @Override
            protected ShowcaseCommandErrorCode featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.errorCode();
            }
        };
    }

    /**
     * Creates a matcher checking the error message of the given error details.
     *
     * @param matcher the matcher for the error message
     * @return a matcher on the error details error message
     */
    public static Matcher<ShowcaseCommandErrorDetails> aCommandErrorDetailsWithErrorMessage(Matcher<String> matcher) {
        return new FeatureMatcher<>(matcher, "A command error details with error message", "errorCode") {
            @Override
            protected String featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.errorMessage();
            }
        };
    }

    /**
     * Creates a matcher checking the metadata of the given error details.
     *
     * @param matcher the matcher for the metadata
     * @return a matcher on the error details metadata
     */
    public static Matcher<ShowcaseCommandErrorDetails> aCommandErrorDetailsWithMetaData(Matcher<Map<?, ?>> matcher) {
        return new FeatureMatcher<>(matcher, "A command error details with metadata", "metaData") {
            @Override
            protected MetaData featureValueOf(ShowcaseCommandErrorDetails actual) {
                return actual.metaData();
            }
        };
    }
}
