## Context

Markdown is manually wrapped at 120 chars. Java/Kotlin use Spotless, the web-UI uses Prettier (`printWidth: 120`,
`singleQuote`, in `showcase-web-ui/.prettierrc`). The in-scope markdown is `docs/**/*.md`, `AGENTS.md`, `README.md`,
`openspec/specs/**/*.md`, and active `openspec/changes/*/` (~30 files); `openspec/changes/archive/` is excluded.

## Goals / Non-Goals

**Goals:**

- Automate markdown formatting with Prettier (`proseWrap: "always"`, `printWidth: 120`), gated in `check`.
- No new root npm project and no coupling to the web-UI's `node_modules` — Spotless provisions Prettier itself (into the
  Gradle build dir).

**Non-Goals:**

- No YAML formatting (Helm/workflows/docker-compose keep their conventions) — a separate decision.
- No reflow of `openspec/changes/archive/`.

## Decisions

### D1: Prettier options live inline in the Spotless config (no root `.prettierrc`)

The Prettier options are declared **inline** in `prettier().config(...)` in the root `spotless` block — the single
source of truth. There is **no root `.prettierrc`**: Spotless's `prettier()` step does not reliably auto-read one (see
the Key learning in D2), so a root config file would be dead weight that can drift from the gate.

`proseWrap: "always"` is the key setting — it forces markdown paragraph reflow, ending the manual convention.
`printWidth` is **120**, matching the repo's stated convention. Note that Prettier's `printWidth` is a preference, not a
hard limit: it never breaks an unbreakable token (a backtick span, URL, or inline code), so a backtick-dense prose line
can still exceed 120 — this is the accepted trade-off of automating markdown wrapping. The other keys mirror the web-UI
config for consistency.

### D2: Use the existing Spotless plugin (its built-in `prettier()` step)

The repo already uses **Spotless** (root `kotlinGradle` + ktfmt, plus per-module Java/Kotlin formatting). Spotless has a
built-in **`prettier()` formatter step** that provisions Prettier itself (managing the npm package and a `node_modules`
under the Gradle build dir — no coupling to the web-UI's `node_modules`). Add a `markdown` format to the root `spotless`
block:

```kotlin
spotless {
    format("markdown") {
        target("docs/**/*.md", "AGENTS.md", "README.md", "openspec/specs/**/*.md", "openspec/changes/**/*.md")
        targetExclude("openspec/changes/archive/**")
        prettier("3.9.6").config(
            mapOf(
                "printWidth" to 120,
                "proseWrap" to "always",
                "singleQuote" to true,
                "trailingComma" to "all",
                "semi" to true,
            )
        )
    }
    // existing kotlinGradle ...
}
```

**Key learning:** Spotless's `prettier()` step does **not** reliably auto-read a root `.prettierrc` for the markdown
format — it resolved a stale width in practice (a plain `prettier()` fell back to an unexpected width instead of the
intended 120). The config is therefore passed **inline** via `prettier().config(...)` (the authoritative source), and
the Prettier version is pinned via `prettier("3.9.6")` to match the web-UI. The target includes **active**
`openspec/changes/*/` (so in-flight change docs stay consistent with the gate); only the `archive/` historical record is
excluded.

Spotless provisions the Prettier npm package (pinned via `prettier("3.9.6")`), runs it in-process, and wires
`spotlessCheck`/`spotlessApply` automatically — already part of `check`. This is a battle-tested Gradle plugin (no new
dependency, no root npm install, no web-UI coupling).

### D3: One-time reflow + docs update

Run `./gradlew spotlessApply` once to reflow the ~30 in-scope markdown files, then the `spotlessCheck` gate keeps them
stable. Update AGENTS.md/README to replace the "manual 120-char wrapping" convention with the automated
Spotless/Prettier gate.

## Risks / Trade-offs

- **Reflow surprises** → `proseWrap: "always"` may reflow long inline code, tables, and `→`/`—` sequences in the first
  run. Mitigation: review the `spotlessApply` diff before committing; Prettier preserves code fences.
- **Prettier provisioning** → Spotless downloads Prettier into its own build cache (needs network on first run, like any
  dependency). Version pinned to match the web-UI's 3.9.6.
- **Spec reflow** → OpenSpec specs reflow to Prettier output; `openspec validate` must still pass (formatting-only, no
  semantic change).
