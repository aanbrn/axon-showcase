import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command lifecycle rules")
class CommandLifecycleRulesTests {

    private val model =
        "### Workflow: Unit lifecycle\n\n**Stages**\n\n| Stage | The work |\n| --- | --- |\n| **Begin** | x |\n| **Plan** | x |\n\n**Change entry points**\n\n- `/opsx-apply` implements.\n\n**Command declarations** — `cross-cutting`, `outside-unit`, `owner-invoked`, `procedure-invoked`, `scheduled`,\n`report-only`, `host operation`, `unit start`, `operation within an existing unit`.\n"

    private val exists: (String) -> Boolean = { it == ".opencode/skills/begin-unit/SKILL.md" }

    private fun check(
        commands: Map<String, String>,
        generated: Set<String> = emptySet<String>(),
        agentsFile: String = model,
    ): List<String> {
        val result: List<String> = CommandLifecycleRules.findings(commands, generated, agentsFile, exists)
        return result
    }

    private fun declaration(
        lifecycle: String = "Begin",
        invocation: String = "owner-invoked",
        effect: String = "unit start",
        owner: String = ".opencode/skills/begin-unit/SKILL.md",
    ) = "**Lifecycle:** $lifecycle | **Invocation:** $invocation | **Effect:** $effect | **Owner:** $owner\n"

    private fun anchors() =
        "owner's authorization\norigin/main\nuncommitted\nmanual review\nworkspace\ndirty changes\npull request\narchive\n"

    private fun unitStarting() = declaration() + "\n" + anchors()

    @Test
    @DisplayName("A conforming command and mapping are clean")
    fun conforming() {
        val findings = check(mapOf("begin-unit.md" to unitStarting()), setOf("opsx-apply.md"))

        assertThat(findings).isEmpty()
    }

    @Test
    @DisplayName("A command with no declaration is reported")
    fun missingDeclaration() {
        val findings = check(mapOf("begin-unit.md" to "No declaration here."))

        assertThat(findings).containsExactly("begin-unit.md: carries no lifecycle declaration.")
    }

    @Test
    @DisplayName("An unrecognized lifecycle value is reported")
    fun unrecognizedLifecycle() {
        val findings =
            check(mapOf("begin-unit.md" to unitStarting().replace("**Lifecycle:** Begin", "**Lifecycle:** Start")))

        assertThat(findings).containsExactly("begin-unit.md: declares the unrecognized lifecycle value 'Start'.")
    }

    @Test
    @DisplayName("An unrecognized invocation is reported")
    fun unrecognizedInvocation() {
        val findings = check(mapOf("begin-unit.md" to unitStarting().replace("owner-invoked", "agent-invoked")))

        assertThat(findings).containsExactly("begin-unit.md: declares the unrecognized invocation 'agent-invoked'.")
    }

    @Test
    @DisplayName("An unrecognized effect is reported")
    fun unrecognizedEffect() {
        val findings =
            check(mapOf("begin-unit.md" to unitStarting().replace("**Effect:** unit start", "**Effect:** mutates")))

        assertThat(findings).containsExactly("begin-unit.md: declares an unrecognized effect 'mutates'.")
    }

    @Test
    @DisplayName("An owner that does not resolve is reported")
    fun unresolvedOwner() {
        val findings =
            check(
                mapOf(
                    "begin-unit.md" to unitStarting().replace(".opencode/skills/begin-unit/SKILL.md", "missing.md"),
                ),
            )

        assertThat(findings).containsExactly("begin-unit.md: names an owner that does not resolve: 'missing.md'.")
    }

    @Test
    @DisplayName("A unit-starting command without an anchor is reported")
    fun missingAnchor() {
        val findings = check(mapOf("begin-unit.md" to declaration() + "\n" + anchors().replace("manual review\n", "")))

        assertThat(findings)
            .containsExactly("begin-unit.md: starts a unit and omits the preflight anchor 'manual review'.")
    }

    @Test
    @DisplayName("A generated command outside the mapping is reported")
    fun unmappedGenerated() {
        val findings = check(mapOf("begin-unit.md" to unitStarting()), setOf("opsx-sync.md"))

        assertThat(findings).containsExactly("opsx-sync.md: is generated and has no entry in the lifecycle mapping.")
    }

    @Test
    @DisplayName("An empty project-authored set is reported")
    fun emptySet() {
        val findings = check(emptyMap())

        assertThat(findings)
            .containsExactly("The derived project-authored command set is empty: the projection checks nothing.")
    }

    @Test
    @DisplayName("An absent model heading is reported")
    fun absentModel() {
        val findings = check(mapOf("begin-unit.md" to unitStarting()), agentsFile = "**Change entry points**\n")

        assertThat(findings)
            .contains("AGENTS.md carries no '### Workflow: Unit lifecycle' heading: the published model is absent.")
    }

    @Test
    @DisplayName("A vocabulary value the lifecycle section no longer states is reported")
    fun vocabularyRenamed() {
        val agentsFile = model.replace("`host operation`, ", "") + "\n### Other\n\nA `host operation` decoy.\n"

        val findings = check(mapOf("begin-unit.md" to unitStarting()), agentsFile = agentsFile)

        assertThat(findings)
            .contains(
                "AGENTS.md no longer states the vocabulary value 'host operation': the check would accept a retired value.",
            )
    }

    @Test
    @DisplayName("A stage renamed in the table is no longer accepted")
    fun stageRenamed() {
        val agentsFile = model.replace("| **Begin** | x |", "| **Start** | x |")

        val findings = check(mapOf("begin-unit.md" to unitStarting()), agentsFile = agentsFile)

        assertThat(findings).contains("begin-unit.md: declares the unrecognized lifecycle value 'Begin'.")
    }

    @Test
    @DisplayName("The generated set excludes the project-authored OpenSpec command")
    fun generatedSet() {
        assertThat(CommandLifecycleRules.generated(listOf("opsx-apply.md", "opsx-tool-update.md", "begin-unit.md")))
            .containsExactly("opsx-apply.md")
    }
}
