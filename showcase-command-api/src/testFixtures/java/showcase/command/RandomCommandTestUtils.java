package showcase.command;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.axonframework.common.IdentifierFactory;

import java.time.Duration;
import java.time.Instant;

import static showcase.test.RandomTestUtils.anAlphabeticString;
import static showcase.test.RandomTestUtils.anEnum;

/**
 * Utility methods generating random command-related test values.
 */
@UtilityClass
public class RandomCommandTestUtils {
    /**
     * Generates a random showcase ID.
     *
     * @return a random showcase ID
     */
    public static String aShowcaseId() {
        return IdentifierFactory.getInstance().generateIdentifier();
    }

    /**
     * Generates a valid random showcase title.
     *
     * @return a random showcase title
     */
    public static String aShowcaseTitle() {
        return RandomStringUtils.secure().nextAlphabetic(10, 20);
    }

    /**
     * Generates a random start time after the given schedule time.
     *
     * @param scheduleTime the schedule time
     * @return a random start time
     */
    public static Instant aShowcaseStartTime(Instant scheduleTime) {
        return scheduleTime.plus(Duration.ofMinutes(RandomUtils.secure().randomLong(1, 10)));
    }

    /**
     * Generates a random valid showcase duration within the configured bounds.
     *
     * @return a random showcase duration
     */
    public static Duration aShowcaseDuration() {
        return Duration.ofSeconds(RandomUtils.secure().randomLong(
                ShowcaseDuration.MIN_MINUTES * 60, ShowcaseDuration.MAX_MINUTES * 60 + 1));
    }

    /**
     * Generates a random scheduled time at or after the given schedule time.
     *
     * @param scheduleTime the earliest possible scheduled time
     * @return a random scheduled time
     */
    public static Instant aShowcaseScheduledAt(Instant scheduleTime) {
        return scheduleTime.plusMillis(
                RandomUtils.secure().randomLong(
                        1, Duration.between(scheduleTime, Instant.now())
                                   .plusMillis(2)
                                   .toMillis()));
    }

    /**
     * Generates a random started time after the given start time.
     *
     * @param startTime the start time
     * @return a random started time
     */
    public static Instant aShowcaseStartedAt(Instant startTime) {
        return startTime.plusMillis(RandomUtils.secure().randomLong(1, 1000));
    }

    /**
     * Generates a random finished time after the started time plus the duration.
     *
     * @param startedAt the started time
     * @param duration the showcase duration
     * @return a random finished time
     */
    public static Instant aShowcaseFinishedAt(Instant startedAt, Duration duration) {
        return startedAt.plus(duration).plusMillis(RandomUtils.secure().randomLong(1, 1000));
    }

    /**
     * Generates a random invalid showcase ID.
     *
     * @return a random invalid showcase ID
     */
    public static String anInvalidShowcaseId() {
        return RandomStringUtils.secure().nextAlphanumeric(36);
    }

    /**
     * Generates a showcase title that exceeds the maximum allowed length.
     *
     * @return a too-long showcase title
     */
    public static String aTooLongShowcaseTitle() {
        return RandomStringUtils.secure().nextAlphabetic(ShowcaseTitle.MAX_LENGTH + 1);
    }

    /**
     * Generates a showcase duration shorter than the minimum allowed.
     *
     * @return a too-short showcase duration
     */
    public static Duration aTooShortShowcaseDuration() {
        return Duration.ofMinutes(ShowcaseDuration.MIN_MINUTES).minusSeconds(1);
    }

    /**
     * Generates a showcase duration longer than the maximum allowed.
     *
     * @return a too-long showcase duration
     */
    public static Duration aTooLongShowcaseDuration() {
        return Duration.ofMinutes(ShowcaseDuration.MAX_MINUTES).plusSeconds(1);
    }

    /**
     * Generates a valid random schedule showcase command using the current time.
     *
     * @return a random schedule command
     */
    public static ScheduleShowcaseCommand aScheduleShowcaseCommand() {
        return ScheduleShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .title(aShowcaseTitle())
                       .startTime(aShowcaseStartTime(Instant.now()))
                       .duration(aShowcaseDuration())
                       .build();
    }

    /**
     * Generates a valid random schedule showcase command using the given current time.
     *
     * @param currentTime the current reference time
     * @return a random schedule command
     */
    public static ScheduleShowcaseCommand aScheduleShowcaseCommand(Instant currentTime) {
        return ScheduleShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .title(aShowcaseTitle())
                       .startTime(aShowcaseStartTime(currentTime))
                       .duration(aShowcaseDuration())
                       .build();
    }

    /**
     * Generates a valid random start showcase command.
     *
     * @return a random start command
     */
    public static StartShowcaseCommand aStartShowcaseCommand() {
        return StartShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .build();
    }

    /**
     * Generates a valid random finish showcase command.
     *
     * @return a random finish command
     */
    public static FinishShowcaseCommand aFinishShowcaseCommand() {
        return FinishShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .build();
    }

    /**
     * Generates a valid random remove showcase command.
     *
     * @return a random remove command
     */
    public static RemoveShowcaseCommand aRemoveShowcaseCommand() {
        return RemoveShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .build();
    }

    /**
     * Picks a random command error code.
     *
     * @return a random command error code
     */
    public static ShowcaseCommandErrorCode aShowcaseCommandErrorCode() {
        return anEnum(ShowcaseCommandErrorCode.class);
    }

    /**
     * Generates a random command error message.
     *
     * @return a random command error message
     */
    public static String aShowcaseCommandErrorMessage() {
        return anAlphabeticString(32);
    }

    /**
     * Generates random command error details.
     *
     * @return random command error details
     */
    public static ShowcaseCommandErrorDetails aShowcaseCommandErrorDetails() {
        return ShowcaseCommandErrorDetails
                       .builder()
                       .errorCode(aShowcaseCommandErrorCode())
                       .errorMessage(aShowcaseCommandErrorMessage())
                       .build();
    }
}
