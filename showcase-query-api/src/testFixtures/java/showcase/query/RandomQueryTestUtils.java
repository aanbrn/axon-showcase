package showcase.query;

import lombok.NonNull;
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

@UtilityClass
public class RandomQueryTestUtils {

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

    public static ShowcaseQueryErrorCode aShowcaseQueryErrorCode() {
        return anEnum(ShowcaseQueryErrorCode.class);
    }

    public static String aShowcaseQueryErrorMessage() {
        return anAlphabeticString(32);
    }

    public static ShowcaseQueryErrorDetails aShowcaseQueryErrorDetails() {
        return ShowcaseQueryErrorDetails
                       .builder()
                       .errorCode(aShowcaseQueryErrorCode())
                       .errorMessage(aShowcaseQueryErrorMessage())
                       .build();
    }

    public static Showcase aShowcase() {
        return aShowcase(aShowcaseStatus());
    }

    public static Showcase aShowcase(@NonNull ShowcaseStatus status) {
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

    public static List<Showcase> showcases(ShowcaseStatus... statuses) {
        return Stream.of(statuses.length == 0 ? ShowcaseStatus.values() : statuses)
                     .flatMap(status -> IntStream.range(0, 3).mapToObj(__ -> status))
                     .map(RandomQueryTestUtils::aShowcase)
                     .toList();
    }
}
