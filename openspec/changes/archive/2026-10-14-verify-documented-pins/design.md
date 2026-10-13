# Design

## Context

See `proposal.md` — Why. `VerifyInstallCommandsTask` and its `InstallCommandRules` cover one pin shape: a
`helm install <name> <chartRef> --version <v>` command, asserted against the catalog's `chartRef → version` map. The
docs also state the **Paketo** pins in prose — `paketo-buildpacks/nginx@1.3.0`, `paketo-buildpacks/procfile@5.15.3` and
`builder-jammy-base:0.4.659`, in `AGENTS.md`'s Docker Images section — catalog-owned as `paketo-nginx`,
`paketo-procfile`, and `paketo-builder-jammy-base` (`gradle/libs.versions.toml`), and the 2026-10-12 buildpack bump left
two of them stale in a doc. Nothing asserts them.

## Goals / Non-Goals

**Goals:**

- Assert the catalog's **Paketo** pins (the builder and the buildpacks) wherever a live doc states them, since that is
  the set the miss showed is unchecked.
- Derive the doc set and the pin forms rather than listing them from intent, so the check's coverage tracks what the
  docs actually state.

**Non-Goals:**

- Asserting **every** version string a doc carries: the catalog holds dozens of coordinates (`spring-boot`, the Jackson
  BOMs, every chart) that the docs quote in many contexts, most of them deliberately historical. The check owns the
  Paketo pin set, which is bounded and mechanically matchable; widening it further is a separate decision.
- Asserting a historical or past-tense mention: the docs quote older Paketo versions legitimately (`AGENTS.md`'s
  `paketo-nginx` hold-back note and its buildpack-pin gotcha name `paketo-nginx` at `1.2.0`, and its Markdown-formatting
  note recounts a `paketo-buildpacks/procfile` / `5.15.0` reflow). The rule matches only the **adjacent pin forms** the
  docs use for a current statement (below), so those mentions — a catalog-key form, or a name and version separated by
  prose — are not matched.
- Replacing `verifyInstallCommands`: the `helm install` command shape stays its own check.

## Decisions

**A sibling task, not a widening of `verifyInstallCommands`.** The two parse different shapes — a structured command
line and a prose reference — so sharing one task would couple two rulesets; the sibling takes the same catalog pins as
an input, which is what makes the two consistent by construction.

**The rule matches two declared, adjacent forms, and the map is explicit.** The buildpack form is
`` `paketo-buildpacks/<name>@<version>` `` and the builder form is `` `builder-jammy-base:<version>` ``; the rule maps
each to its catalog key (`paketo-<name>`, `paketo-builder-jammy-base`) and compares the version. The mapping is declared
in the rules object, not inferred (the doc spelling and the catalog key differ by design). A matched `<version>` must
start with a digit — the `VERSION` constant both forms share — so a placeholder like `<version>` in `AGENTS.md`'s
buildpack-pin gotcha and a word token like `latest` are not matches, and a match whose version disagrees fails `check`,
naming the doc path, the pin as written, and the catalog version.

**The doc set is the live docs that state present-tense pins: `AGENTS.md` and `README.md`.** The version-bump sweep's
broader set (`openspec/config.yaml`, `docs/adr/`, `docs/ideas.md`) and the dated records (`docs/audits/`,
`docs/retrospectives/`) are excluded: a dated record legitimately quotes the version it ran under, and a historical
mention is not a pin statement. The task's `docs` input is that pair, named.

**A pin form with no catalog key fails too.** The rule never invents an expectation, and a doc naming a key the catalog
does not pin is exactly the inconsistency a single-sourcing gate exists to catch (the sibling install-command gate fails
on an unpinned chart), so an unowned key is a failure naming the key — not a silent pass.

## Risks / Trade-offs

- The docs could add a new spelling the rule does not match → the forms are stated once in the rules object and the
  unmatched pin is simply not asserted, so a new spelling is a rules change, visible in the fixtures first.
- The shared `VERSION` pattern's internals beyond the digit-start guard and the delimiter trim are broader than any
  catalog Paketo pin — every pin is a lowercase, digit-ending semver — so a mutation inside those branches changes no
  live verdict. That surface is **accepted and recorded here rather than closed by fixtures**: a fixture asserting the
  regex's own grammar would test the pattern, not the check's behaviour.
- This change's own artifacts name the catalog's current Paketo pins and the requirement's scenario count — the
  point-in-time subject the premise concerns, which the documented-numbers convention allows inside a change's own
  artifact; the owners (the catalog and the main spec) stay the source the implementation reads, so a bump is caught by
  the check rather than by this prose.
- A historical mention could use the adjacent form → the two existing cases do not (one uses the catalog-key form, one
  splits name from version), and the placeholder form is excluded by the digit-start rule; the test fixtures cover all
  three, so a future one is a fixture-driven rules change.
- The check is a `check` member → it must stay Docker-free and cacheable; it resolves nothing external, so it is.

## Migration Plan

None: an additive check; the docs' current Paketo pins already match.
