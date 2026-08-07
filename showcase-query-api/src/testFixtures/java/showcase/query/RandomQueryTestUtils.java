package showcase.query;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static showcase.command.RandomCommandTestUtils.aShowcaseDuration;
import static showcase.command.RandomCommandTestUtils.aShowcaseFinishedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseId;
import static showcase.command.RandomCommandTestUtils.aShowcaseScheduledAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartTime;
import static showcase.command.RandomCommandTestUtils.aShowcaseStartedAt;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.test.RandomTestUtils.anAlphabeticString;
import static showcase.test.RandomTestUtils.anElementOf;
import static showcase.test.RandomTestUtils.anEnum;

/**
 * Utility methods generating random query-related test values.
 */
@UtilityClass
public class RandomQueryTestUtils {
    /**
     * Picks a random showcase status excluding the given ones.
     *
     * @param except the statuses to exclude
     * @return a random non-excluded showcase status
     * @throws IllegalArgumentException if all statuses are excluded
     */
    public static ShowcaseStatus aShowcaseStatus(ShowcaseStatus... except) {
        if (except.length == 0) {
            return anEnum(ShowcaseStatus.class);
        }
        val statuses = new ArrayList<>(Set.of(ShowcaseStatus.values()));
        statuses.removeAll(List.of(except));
        if (statuses.isEmpty()) {
            throw new IllegalArgumentException("All statuses excluded");
        }
        return anElementOf(statuses);
    }

    /**
     * Picks a random query error code.
     *
     * @return a random query error code
     */
    public static ShowcaseQueryErrorCode aShowcaseQueryErrorCode() {
        return anEnum(ShowcaseQueryErrorCode.class);
    }

    /**
     * Generates a random query error message.
     *
     * @return a random query error message
     */
    public static String aShowcaseQueryErrorMessage() {
        return anAlphabeticString(32);
    }

    /**
     * Generates random query error details.
     *
     * @return random query error details
     */
    public static ShowcaseQueryErrorDetails aShowcaseQueryErrorDetails() {
        return ShowcaseQueryErrorDetails
                       .builder()
                       .errorCode(aShowcaseQueryErrorCode())
                       .errorMessage(aShowcaseQueryErrorMessage())
                       .build();
    }

    /**
     * Generates a random showcase with a random status.
     *
     * @return a random showcase
     */
    public static Showcase aShowcase() {
        return aShowcase(aShowcaseStatus());
    }

    /**
     * Generates a random showcase with the given status, filling in timestamps consistent with that status.
     *
     * @param status the status of the showcase
     * @return a random showcase
     */
    public static Showcase aShowcase(ShowcaseStatus status) {
        val scheduleTime = Instant.now();
        val startTime = aShowcaseStartTime(scheduleTime);
        val duration = aShowcaseDuration();
        val showcaseBuilder =
                Showcase.builder()
                        .showcaseId(aShowcaseId())
                        .title(aShowcaseTitle())
                        .startTime(startTime)
                        .duration(duration)
                        .status(status)
                        .scheduledAt(aShowcaseScheduledAt(scheduleTime));
        if (status == ShowcaseStatus.SCHEDULED) {
            return showcaseBuilder.build();
        }
        val startedAt = aShowcaseStartedAt(startTime);
        showcaseBuilder.startedAt(startedAt);
        if (status == ShowcaseStatus.STARTED) {
            return showcaseBuilder.build();
        }
        return showcaseBuilder
                       .finishedAt(aShowcaseFinishedAt(startedAt, duration))
                       .build();
    }

    /**
     * Generates a list of three showcases per status for the given statuses.
     *
     * @param statuses the statuses to generate showcases for, or all statuses when empty
     * @return a list of random showcases grouped by status
     */
    public static List<Showcase> showcases(ShowcaseStatus... statuses) {
        return Stream.of(statuses.length == 0 ? ShowcaseStatus.values() : statuses)
                     .flatMap(status -> IntStream.range(0, 3).mapToObj(__ -> status))
                     .map(RandomQueryTestUtils::aShowcase)
                     .toList();
    }
}
