import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Upstream reference scope")
class UpstreamReferenceScopeTests {

    @Test
    @DisplayName("The root durable artifacts and ADRs are in scope")
    fun rootArtifactsAndAdrsAreInScope() {
        assertThat(UpstreamReferenceScope.includes("AGENTS.md")).isTrue()
        assertThat(UpstreamReferenceScope.includes("README.md")).isTrue()
        assertThat(UpstreamReferenceScope.includes("docs/ideas.md")).isTrue()
        assertThat(UpstreamReferenceScope.includes("docs/adr/0001-example.md")).isTrue()
        assertThat(UpstreamReferenceScope.includes("docs/adr/nested/0001-example.md")).isTrue()
    }

    @Test
    @DisplayName("A project-authored skill is in scope")
    fun projectAuthoredSkillIsInScope() {
        assertThat(UpstreamReferenceScope.includes(".opencode/skills/jvm-development/SKILL.md")).isTrue()
    }

    @Test
    @DisplayName("Vendored and generated skills are out of scope")
    fun vendoredAndGeneratedSkillsAreOutOfScope() {
        assertThat(UpstreamReferenceScope.includes(".opencode/skills/axon4to5-migrate-code/SKILL.md")).isFalse()
        assertThat(UpstreamReferenceScope.includes(".opencode/skills/opsx-probe/SKILL.md")).isFalse()
        assertThat(UpstreamReferenceScope.includes(".opencode/skills/openspec-propose/SKILL.md")).isFalse()
    }

    @Test
    @DisplayName("A file that only resembles a skill is out of scope")
    fun resemblingFilesAreOutOfScope() {
        assertThat(UpstreamReferenceScope.includes(".opencode/skills/jvm-development/README.md")).isFalse()
        assertThat(UpstreamReferenceScope.includes("showcase-web-ui/src/App.tsx")).isFalse()
        assertThat(UpstreamReferenceScope.includes("docs/audits/2026-10-19.md")).isFalse()
    }
}
