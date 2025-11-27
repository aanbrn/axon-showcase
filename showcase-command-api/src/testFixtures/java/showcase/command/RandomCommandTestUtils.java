package showcase.command;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.axonframework.common.IdentifierFactory;

import java.time.Duration;
import java.time.Instant;

import static showcase.test.RandomTestUtils.anAlphabeticString;
import static showcase.test.RandomTestUtils.anEnum;

@UtilityClass
public class RandomCommandTestUtils {

    public static String aShowcaseId() {
        return IdentifierFactory.getInstance().generateIdentifier();
    }

    public static String aShowcaseTitle() {
        return RandomStringUtils.secure().nextAlphabetic(10, 20);
    }

    public static Instant aShowcaseStartTime(Instant scheduleTime) {
        return scheduleTime.plus(Duration.ofMinutes(RandomUtils.secure().randomLong(1, 10)));
    }

    public static Duration aShowcaseDuration() {
        return Duration.ofSeconds(RandomUtils.secure().randomLong(
                ShowcaseDuration.MIN_MINUTES * 60, ShowcaseDuration.MAX_MINUTES * 60 + 1));
    }

    public static Instant aShowcaseScheduledAt(Instant scheduleTime) {
        return scheduleTime.plusMillis(
                RandomUtils.secure().randomLong(
                        1, Duration.between(scheduleTime, Instant.now())
                                   .plusMillis(2)
                                   .toMillis()));
    }

    public static Instant aShowcaseStartedAt(Instant startTime) {
        return startTime.plusMillis(RandomUtils.secure().randomLong(1, 1000));
    }

    public static Instant aShowcaseFinishedAt(Instant startedAt, Duration duration) {
        return startedAt.plus(duration).plusMillis(RandomUtils.secure().randomLong(1, 1000));
    }

    public static String anInvalidShowcaseId() {
        return RandomStringUtils.secure().nextAlphanumeric(36);
    }

    public static String aTooLongShowcaseTitle() {
        return RandomStringUtils.secure().nextAlphabetic(ShowcaseTitle.MAX_LENGTH + 1);
    }

    public static Duration aTooShortShowcaseDuration() {
        return Duration.ofMinutes(ShowcaseDuration.MIN_MINUTES).minusSeconds(1);
    }

    public static Duration aTooLongShowcaseDuration() {
        return Duration.ofMinutes(ShowcaseDuration.MAX_MINUTES).plusSeconds(1);
    }

    public static ScheduleShowcaseCommand aScheduleShowcaseCommand() {
        return ScheduleShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .title(aShowcaseTitle())
                       .startTime(aShowcaseStartTime(Instant.now()))
                       .duration(aShowcaseDuration())
                       .build();
    }

    public static ScheduleShowcaseCommand aScheduleShowcaseCommand(Instant currentTime) {
        return ScheduleShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .title(aShowcaseTitle())
                       .startTime(aShowcaseStartTime(currentTime))
                       .duration(aShowcaseDuration())
                       .build();
    }

    public static StartShowcaseCommand aStartShowcaseCommand() {
        return StartShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .build();
    }

    public static FinishShowcaseCommand aFinishShowcaseCommand() {
        return FinishShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .build();
    }

    public static RemoveShowcaseCommand aRemoveShowcaseCommand() {
        return RemoveShowcaseCommand
                       .builder()
                       .showcaseId(aShowcaseId())
                       .build();
    }

    public static ShowcaseCommandErrorCode aShowcaseCommandErrorCode() {
        return anEnum(ShowcaseCommandErrorCode.class);
    }

    public static String aShowcaseCommandErrorMessage() {
        return anAlphabeticString(32);
    }

    public static ShowcaseCommandErrorDetails aShowcaseCommandErrorDetails() {
        return ShowcaseCommandErrorDetails
                       .builder()
                       .errorCode(aShowcaseCommandErrorCode())
                       .errorMessage(aShowcaseCommandErrorMessage())
                       .build();
    }
}
