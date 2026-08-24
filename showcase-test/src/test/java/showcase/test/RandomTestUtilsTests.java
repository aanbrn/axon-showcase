// SPDX-License-Identifier: MIT
package showcase.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Random test utility tests")
class RandomTestUtilsTests {

    private static final String[] STRINGS = {"alpha", "beta", "gamma"};
    private static final List<String> STRING_LIST = List.of("alpha", "beta", "gamma");

    @Test
    @DisplayName("An alphabetic string has exactly the requested length and only letters")
    void anAlphabeticString_returnsStringOfRequestedLengthWithOnlyLetters() {
        val result = RandomTestUtils.anAlphabeticString(32);

        assertThat(result).hasSize(32).matches("[a-zA-Z]+");
    }

    @Test
    @DisplayName("An empty alphabetic string is returned for length zero")
    void anAlphabeticString_zeroLength_returnsEmptyString() {
        val result = RandomTestUtils.anAlphabeticString(0);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("A random element of an array is a member of the array")
    void anElementOf_array_returnsMemberOfArray() {
        val result = RandomTestUtils.anElementOf(STRINGS);

        assertThat(result).isIn((Object[]) STRINGS);
    }

    @Test
    @DisplayName("A random element of a list is a member of the list")
    void anElementOf_list_returnsMemberOfList() {
        val result = RandomTestUtils.anElementOf(STRING_LIST);

        assertThat(result).isIn(STRING_LIST);
    }

    @Test
    @DisplayName("An element of a single-element array is that element")
    void anElementOf_singleElementArray_returnsThatElement() {
        val result = RandomTestUtils.anElementOf(new String[] {"only"});

        assertThat(result).isEqualTo("only");
    }

    @Test
    @DisplayName("An element of a single-element list is that element")
    void anElementOf_singleElementList_returnsThatElement() {
        val result = RandomTestUtils.anElementOf(List.of("only"));

        assertThat(result).isEqualTo("only");
    }

    @Test
    @DisplayName("An empty array is rejected")
    void anElementOf_emptyArray_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> RandomTestUtils.anElementOf(new String[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument 'array' must not be empty");
    }

    @Test
    @DisplayName("An empty list is rejected")
    void anElementOf_emptyList_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> RandomTestUtils.anElementOf(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument 'list' must not be empty");
    }

    @Test
    @DisplayName("A random enum constant is one of the enum's values")
    void anEnum_returnsOneOfEnumValues() {
        val result = RandomTestUtils.anEnum(SampleEnum.class);

        assertThat(result).isIn((Object[]) SampleEnum.values());
    }

    @Test
    @DisplayName("An enum without constants is rejected")
    void anEnum_noConstants_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> RandomTestUtils.anEnum(EmptyEnum.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument 'enumClass' must have at least one enum constant");
    }

    private enum SampleEnum {
        FIRST,
        SECOND,
        THIRD
    }

    private enum EmptyEnum {}
}
