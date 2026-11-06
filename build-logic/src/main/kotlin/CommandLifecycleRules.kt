import java.io.File

/**
 * The rules of the command lifecycle projection: every project-authored command states the lifecycle entry it serves,
 * its values come from the published vocabulary, its owner resolves, a unit-starting command carries the lifecycle
 * preflight's anchors, and every generated entry is mapped in the lifecycle section.
 *
 * The object is pure so `build-logic/src/test` can exercise each predicate with a known-bad case; that build has no
 * `gradleTestKit()`, so a task's Gradle wiring is verified by running `check` instead. The claim is narrow on purpose:
 * completeness, values, references, anchors, and mapping coverage, never a command's behaviour.
 */
object CommandLifecycleRules {

    /** The heading the stage table lives under. */
    const val STAGES_HEADING = "**Stages**"

    /** The values that stand in for a stage. */
    val SCOPES = setOf("cross-cutting", "outside-unit")

    /** The permitted invocation values. */
    val INVOCATIONS = setOf("owner-invoked", "procedure-invoked", "scheduled")

    /** The permitted effect values. */
    val EFFECTS = setOf("report-only", "host operation", "unit start", "operation within an existing unit")

    /** The effect that obliges a command to state the lifecycle preflight. */
    const val UNIT_START = "unit start"

    /** The anchors a unit-starting command states, each matched case-insensitively. */
    val PREFLIGHT_ANCHORS =
        listOf(
            "authorization",
            "origin/main",
            "uncommitted",
            "manual review",
            "workspace",
            "dirty",
            "pull request",
            "archive",
        )

    /** The prefix a generated OpenSpec command's file name carries. */
    const val GENERATED_PREFIX = "opsx-"

    /** The generated command whose definition the repository authors rather than a generator. */
    const val PROJECT_AUTHORED_GENERATED_PREFIX = "opsx-tool-update"

    /** The heading the published model lives under. */
    const val MODEL_HEADING = "### Workflow: Unit lifecycle"

    /** The block that maps the generated entries to their stages. */
    const val MAPPING_HEADING = "**Change entry points**"

    /** One parsed declaration. */
    data class Declaration(
        val lifecycle: List<String>,
        val invocation: List<String>,
        val effects: List<String>,
        val owner: String,
    )

    /** Parses the declaration line, or returns null when the text carries none. */
    fun parse(text: String): Declaration? {
        val lines = text.lines()
        val start = lines.indexOfFirst { it.trimStart().startsWith("**Lifecycle:**") }
        if (start < 0) return null
        val collected = mutableListOf<String>()
        for (index in start until lines.size) {
            val current = lines[index]
            if (collected.isNotEmpty() && current.isBlank()) break
            collected += current.trim()
        }
        val line = collected.joinToString(" ")
        val values =
            line
                .split('|')
                .map { it.trim() }
                .filter { it.startsWith("**") }
                .associate { field ->
                    val label = field.substringAfter("**").substringBefore(":**").trim()
                    val value = field.substringAfter(":**").trim().removeSuffix(".").trim()
                    label to value
                }
        return Declaration(
            lifecycle = split(values["Lifecycle"]),
            invocation = split(values["Invocation"]),
            effects = split(values["Effect"]),
            owner = values["Owner"].orEmpty().trim().trim('`'),
        )
    }

    private fun split(value: String?): List<String> =
        value.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }

    /** The stage names the published stage table carries, read from its rows. */
    fun stages(agentsFile: String): Set<String> {
        val start = agentsFile.indexOf(STAGES_HEADING)
        if (start < 0) return emptySet()
        val end =
            agentsFile.indexOf("\n**", start + STAGES_HEADING.length).let { if (it < 0) agentsFile.length else it }
        return Regex("(?m)^\\|\\s*\\*\\*(.+?)\\*\\*")
            .findAll(agentsFile.substring(start, end))
            .map { it.groupValues[1].trim() }
            .toSet()
    }

    /** The lifecycle section's text, from its heading to the next third-level heading. */
    fun lifecycleSection(agentsFile: String): String {
        val start = agentsFile.indexOf(MODEL_HEADING)
        if (start < 0) return ""
        val end =
            agentsFile.indexOf("\n### ", start + MODEL_HEADING.length).let { if (it < 0) agentsFile.length else it }
        return agentsFile.substring(start, end)
    }

    /** Every vocabulary value the lifecycle section states, so a rename there fails rather than diverging. */
    fun vocabularyTokens(agentsFile: String): Set<String> {
        val section = lifecycleSection(agentsFile)
        return (SCOPES + INVOCATIONS + EFFECTS).filter { section.contains(it) }.toSet()
    }

    /** The heading names `AGENTS.md` publishes, without their leading markers. */
    fun headings(agentsFile: String): Set<String> =
        agentsFile.lineSequence().filter { it.startsWith("#") }.map { it.trimStart('#').trim() }.toSet()

    /** The generated file names the lifecycle section's mapping names. */
    fun mappedGenerated(agentsFile: String): Set<String> {
        val start = agentsFile.indexOf(MAPPING_HEADING)
        if (start < 0) return emptySet()
        val end =
            agentsFile.indexOf("\n**", start + MAPPING_HEADING.length).let { if (it < 0) agentsFile.length else it }
        return Regex("/opsx-[a-z-]+")
            .findAll(agentsFile.substring(start, end))
            .map { it.value.removePrefix("/") + ".md" }
            .toSet()
    }

    /** Whether the named owner resolves to a tracked path or to a heading the lifecycle publishes. */
    fun resolves(owner: String, headings: Set<String>, exists: (String) -> Boolean): Boolean {
        val quoted = Regex("AGENTS\\.md\\s+\"(.+)\"").find(owner)
        if (quoted != null) return quoted.groupValues[1] in headings
        return owner.isNotBlank() && exists(owner)
    }

    /** Every finding the projection carries, one per defect, each naming the command it belongs to. */
    fun findings(
        projectAuthored: Map<String, String>,
        generated: Set<String>,
        agentsFile: String,
        exists: (String) -> Boolean,
    ): List<String> {
        val findings = mutableListOf<String>()
        if (!agentsFile.contains(MODEL_HEADING)) {
            findings += "AGENTS.md carries no '$MODEL_HEADING' heading: the published model is absent."
        }
        if (projectAuthored.isEmpty()) {
            findings += "The derived project-authored command set is empty: the projection checks nothing."
        }
        val headings = headings(agentsFile)
        val stages = stages(agentsFile)
        val missingVocabulary = (SCOPES + INVOCATIONS + EFFECTS) - vocabularyTokens(agentsFile)
        missingVocabulary.sorted().forEach {
            findings += "AGENTS.md no longer states the vocabulary value '$it': the check would accept a retired value."
        }
        projectAuthored.forEach { (name, text) ->
            val declaration = parse(text)
            if (declaration == null) {
                findings += "$name: carries no lifecycle declaration."
                return@forEach
            }
            if (declaration.lifecycle.isEmpty()) findings += "$name: declares no lifecycle value."
            declaration.lifecycle
                .filter { it !in stages && it !in SCOPES }
                .forEach {
                    findings += "$name: declares the unrecognized lifecycle value '$it'."
                }
            if (declaration.invocation.isEmpty()) findings += "$name: declares no invocation."
            declaration.invocation
                .filter { it !in INVOCATIONS }
                .forEach {
                    findings += "$name: declares the unrecognized invocation '$it'."
                }
            if (declaration.effects.size != 1 || declaration.effects.first() !in EFFECTS) {
                findings += "$name: declares an unrecognized effect '${declaration.effects.joinToString(", ")}'."
            }
            if (!resolves(declaration.owner, headings, exists)) {
                findings += "$name: names an owner that does not resolve: '${declaration.owner}'."
            }
            if (declaration.effects.contains(UNIT_START)) {
                PREFLIGHT_ANCHORS.filterNot { text.contains(it, ignoreCase = true) }
                    .forEach {
                        findings += "$name: starts a unit and omits the preflight anchor '$it'."
                    }
            }
        }
        val mapped = mappedGenerated(agentsFile)
        generated
            .filterNot { it in mapped }
            .sorted()
            .forEach {
                findings += "$it: is generated and has no entry in the lifecycle mapping."
            }
        return findings
    }

    /** The generated commands in a file set: the OpenSpec entries the repository does not author. */
    fun generated(commands: Collection<String>): Set<String> =
        commands.filter { it.startsWith(GENERATED_PREFIX) && !it.startsWith(PROJECT_AUTHORED_GENERATED_PREFIX) }.toSet()

    /** Reads a file's text, or throws naming the path when it cannot. */
    fun read(file: File): String = file.readText()
}
