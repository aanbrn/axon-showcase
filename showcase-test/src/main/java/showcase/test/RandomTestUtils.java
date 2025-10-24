package showcase.test;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

@UtilityClass
public final class RandomTestUtils {

    private static final RandomUtils random = RandomUtils.secure();

    private static final RandomStringUtils randomString = RandomStringUtils.secure();

    public static String anAlphabeticString(int length) {
        return randomString.nextAlphabetic(length);
    }

    public static <E> E anElementOf(E[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Argument 'array' must not be empty");
        }
        return array[random.randomInt(0, array.length)];
    }

    public static <E> E anElementOf(List<E> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Argument 'list' must not be empty");
        }
        return list.get(random.randomInt(0, list.size()));
    }

    public static <E extends Enum<E>> E anEnum(Class<E> enumClass) {
        val enumConstants = enumClass.getEnumConstants();
        if (enumConstants.length == 0) {
            throw new IllegalArgumentException("Argument 'enumClass' must have at least one enum constant");
        }
        return anElementOf(enumConstants);
    }
}
