import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Version ordering")
class VersionsTests {

    @Test
    @DisplayName("A higher numeric segment is newer, in any position")
    fun higherSegmentIsNewer() {
        assertThat(Versions.isNewer("1.3", "1.2.9")).isTrue()
        assertThat(Versions.isNewer("2.0", "1.99")).isTrue()
        assertThat(Versions.isNewer("1.2.9", "1.3")).isFalse()
    }

    @Test
    @DisplayName("An equivalent spelling is not newer in either direction")
    fun equivalentSpellingIsNotNewer() {
        assertThat(Versions.isNewer("1.2.0", "1.2")).isFalse()
        assertThat(Versions.isNewer("1.2", "1.2.0")).isFalse()
        assertThat(Versions.isNewer("5.15.0", "5.15")).isFalse()
        assertThat(Versions.isNewer("5.15", "5.15.0")).isFalse()
    }

    @Test
    @DisplayName("A leading v is ignored on either side")
    fun leadingVIgnored() {
        assertThat(Versions.isNewer("v1.1307.3", "v1.1307.2")).isTrue()
        assertThat(Versions.isNewer("1.1307.3", "v1.1307.2")).isTrue()
        assertThat(Versions.isNewer("v1.1307.2", "1.1307.3")).isFalse()
        assertThat(Versions.isNewer("v1.2.0", "1.2.0")).isFalse()
    }

    @Test
    @DisplayName("A major-only version compares against a padded one")
    fun majorOnlyComparesPadded() {
        assertThat(Versions.isNewer("2", "1.9")).isTrue()
        assertThat(Versions.isNewer("2", "2.0")).isFalse()
        assertThat(Versions.isNewer("2.0", "2")).isFalse()
    }

    @Test
    @DisplayName("A non-numeric version declares no segments")
    fun nonNumericDeclaresNoSegments() {
        assertThat(Versions.numericParts("RELEASE")).isEmpty()
        assertThat(Versions.leadingInteger("RELEASE")).isNull()
        assertThat(Versions.isNewer("RELEASE", "1.0")).isFalse()
    }

    @Test
    @DisplayName("leadingInteger reads the first segment of a v-prefixed version")
    fun leadingIntegerReadsFirstSegment() {
        assertThat(Versions.leadingInteger("v2.5.1")).isEqualTo(2)
        assertThat(Versions.leadingInteger("91.4.0")).isEqualTo(91)
    }

    @Test
    @DisplayName("The highest version prefers the full spelling among numerically equal tags")
    fun highestPrefersFullSpelling() {
        assertThat(Versions.highest(listOf("5.14.9", "5.15", "5.15.0"))).isEqualTo("5.15.0")
        assertThat(Versions.highest(listOf("5.15"))).isEqualTo("5.15")
        assertThat(Versions.highest(listOf("5", "5.15", "5.15.0", "latest"))).isEqualTo("5.15.0")
        assertThat(Versions.highest(emptyList())).isNull()
    }

    @Test
    @DisplayName("compare orders by number before spelling")
    fun compareOrdersByNumberFirst() {
        assertThat(Versions.compare("1.2.1", "1.2")).isPositive()
        assertThat(Versions.compare("1.2", "1.2.0")).isNegative()
        assertThat(Versions.compare("1.2.0", "1.2.0")).isZero()
        assertThat(Versions.compare("2", "1.99")).isPositive()
    }
}
