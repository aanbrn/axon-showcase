import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Upstream reference states")
class UpstreamReferenceStateTests {

    @Test
    @DisplayName("An open reference is reported as open")
    fun openReferenceIsReportedAsOpen() {
        assertThat(UpstreamReferenceState.parse("open||")).isEqualTo("open")
    }

    @Test
    @DisplayName("A completed closure reports its disposition beside the closed date")
    fun completedClosureReportsItsDisposition() {
        assertThat(UpstreamReferenceState.parse("closed|completed|2026-09-29T18:37:10Z"))
            .isEqualTo("closed (completed) 2026-09-29")
    }

    @Test
    @DisplayName("A dropped fix reports its not_planned disposition")
    fun notPlannedClosureReportsItsDisposition() {
        assertThat(UpstreamReferenceState.parse("closed|not_planned|2026-10-11T19:14:20Z"))
            .isEqualTo("closed (not_planned) 2026-10-11")
    }

    @Test
    @DisplayName("A duplicate closure reports its disposition rather than reading like a fix")
    fun duplicateClosureReportsItsDisposition() {
        assertThat(UpstreamReferenceState.parse("closed|duplicate|2026-03-27T19:11:56Z"))
            .isEqualTo("closed (duplicate) 2026-03-27")
    }

    @Test
    @DisplayName("A closure the API gave no reason for states the date alone")
    fun closureWithoutReasonStatesTheDateAlone() {
        assertThat(UpstreamReferenceState.parse("closed||2026-10-05T06:43:43Z")).isEqualTo("closed 2026-10-05")
    }

    @Test
    @DisplayName("The trailing newline of a real gh payload does not reach the state")
    fun trailingNewlineIsTrimmed() {
        assertThat(UpstreamReferenceState.parse("closed|completed|2026-09-20T18:43:48Z\n"))
            .isEqualTo("closed (completed) 2026-09-20")
    }

    @Test
    @DisplayName("A closed payload carrying no date renders without one rather than as unresolved")
    fun closureWithoutDateRendersWithoutOne() {
        assertThat(UpstreamReferenceState.parse("closed||")).isEqualTo("closed")
    }

    @Test
    @DisplayName("An empty answer is unresolved rather than an empty or partial state")
    fun emptyAnswerIsUnresolved() {
        assertThat(UpstreamReferenceState.parse("")).isEqualTo("unresolved")
        assertThat(UpstreamReferenceState.parse("  \n")).isEqualTo("unresolved")
    }

    @Test
    @DisplayName("An unrecognized state token is unresolved, so an API change surfaces")
    fun unrecognizedStateIsUnresolved() {
        assertThat(UpstreamReferenceState.parse("transferred||2026-10-11T19:14:20Z")).isEqualTo("unresolved")
        assertThat(UpstreamReferenceState.parse("nonsense")).isEqualTo("unresolved")
    }
}
