// SPDX-License-Identifier: MIT
package showcase.test;

import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

/**
 * Utility methods generating random test values.
 */
@UtilityClass
public final class RandomTestUtils {
    /**
     * Secure random number generator.
     */
    private static final RandomUtils RANDOM = RandomUtils.secure();

    /**
     * Secure random string generator.
     */
    private static final RandomStringUtils RANDOM_STRING = RandomStringUtils.secure();

    /**
     * Generates a random alphabetic string of the given length.
     *
     * @param length the desired length of the string
     * @return a random alphabetic string
     */
    public static String anAlphabeticString(int length) {
        return RANDOM_STRING.nextAlphabetic(length);
    }

    /**
     * Picks a random element from the given array.
     *
     * @param array the array to pick from
     * @param <E>   the element type
     * @return a random element of the array
     * @throws IllegalArgumentException if the array is empty
     */
    public static <E> E anElementOf(E[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Argument 'array' must not be empty");
        }
        return array[RANDOM.randomInt(0, array.length)];
    }

    /**
     * Picks a random element from the given list.
     *
     * @param list the list to pick from
     * @param <E>  the element type
     * @return a random element of the list
     * @throws IllegalArgumentException if the list is empty
     */
    public static <E> E anElementOf(List<E> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Argument 'list' must not be empty");
        }
        return list.get(RANDOM.randomInt(0, list.size()));
    }

    /**
     * Picks a random enum constant of the given enum type.
     *
     * @param enumClass the enum class to pick from
     * @param <E>       the enum type
     * @return a random enum constant
     * @throws IllegalArgumentException if the enum has no constants
     */
    public static <E extends Enum<E>> E anEnum(Class<E> enumClass) {
        val enumConstants = enumClass.getEnumConstants();
        if (enumConstants.length == 0) {
            throw new IllegalArgumentException("Argument 'enumClass' must have at least one enum constant");
        }
        return anElementOf(enumConstants);
    }
}
