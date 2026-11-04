# Design — reject OpenSpec content the CLI would drop at archive

## Context

The parked entry (`docs/ideas.md`, 2026-10-18) recorded that `openspec validate --all` exits 0 while warning and
`openspec archive` drops a requirement whose delta header names an unknown operation. It proposed that the CI probe fail
on that warning.

A probe on the pinned CLI (1.16.0, installed by all three workflows) settles what still holds:

| Fixture                                                             | `validate --all`                                                                               | `archive --yes`                                                                    |
| ------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| A delta whose only section is `## DEPRECATED Requirements`          | exits **1**; `No delta sections found`                                                         | exits **1**; `Delta parsing found no operations … Aborted. No files were changed.` |
| The same malformed section beside one valid `## ADDED Requirements` | exits **0**; both warnings printed                                                             | exits **0**; the valid requirement lands, the malformed one is **dropped**         |
| One unowned prose paragraph beside a valid `## ADDED Requirements`  | exits **0**; `Text above the first "## " section is not carried into the main spec by archive` | exits **0**; the prose is dropped                                                  |

So the CLI refuses the first shape and silently drops the other two. The enforced class is therefore **content the CLI
would drop at archive** — a delta section that is not a recognised operation, and text outside any delta section — not
only a malformed section. The probe ran in a throwaway copy of `openspec/` under the session's scratch directory, never
against the repository.

## Decisions

1. **The predicate is a committed script, not an inline grep.** `scripts/verify-openspec-delta-guard.py` reads a
   `openspec validate` transcript and fails on a line matching "not a delta section" or "not carried into the main spec
   by archive", printing the matched line. A script can be unit-tested and carries its own contract; an inline `grep` in
   a workflow step is a decoration until someone re-reads it.
2. **The script owns both halves: the predicate and the probe.** `--validate-output <file>` applies the predicate to the
   gate's own run; `--probe` builds four fixtures in a temporary directory — the three loss-producing shapes and a fully
   valid control — and runs the pinned CLI over them. The valid control must validate and archive cleanly, which
   distinguishes a strict CLI from a broken fixture environment; a non-zero exit counts as the upstream fix only when
   the CLI's own rejection diagnostic is present; and the retirement note fires only when both loss-producing shapes are
   rejected, so a CLI bump that rewords the message fails the probe instead of silently disabling the gate. `--probe`
   runs in CI only, where the workflow installs the CLI; `check` wires only the predicate's test, so `check` keeps the
   prerequisites `AGENTS.md` declares and adds no dependency on the CLI.
3. **The gate's failure path keeps the diagnostic.** The step captures the validation output and, when
   `openspec validate --all` itself fails, prints the captured output and exits — a bare `validation=$(…)` under
   `bash -e` would swallow the CLI's own error.
4. **The test pins the predicate and the fixture shape.** `scripts/test-verify-openspec-delta-guard.py` covers the
   predicate with recorded outputs (both drop shapes, the corpus's long-requirement warning, a clean run) and the
   fixtures' construction, so the script's logic is exercised in `check` without the CLI; the CLI-dependent half is
   `--probe`, which CI runs.
5. **Both gates carry it.** The step is shared by the pull-request and push-to-`main` paths, so one edit covers both;
   the delta modifies both requirements.
6. **The workaround is reported upstream and carries its retirement.** The loss is the CLI's, so the change files the
   reproduction at `Fission-AI/OpenSpec` through the review gate, and records the reference with its `retires when`
   condition in `AGENTS.md`'s Continuous Integration section — the durable file the `upstreamReferences` report scans.
   The guard retires when the pinned CLI rejects both loss-producing shapes (the probe's mixed and prose fixtures then
   exit non-zero), which a CLI bump's probe run reports.
7. **The prose case is a policy choice, stated as one.** Rejecting text outside any delta section is conservative
   authoring guidance, not proof that the paragraph held an intended requirement; explanatory material belongs in
   `proposal.md` or `design.md`.

## Proposed step

The "Run quality gates" step replaces its bare `openspec validate --all` line with:

```bash
openspec_log="$RUNNER_TEMP/openspec-validate.log"
if ! openspec validate --all 2>&1 | tee "$openspec_log"; then
  exit 1
fi
python3 scripts/verify-openspec-delta-guard.py --validate-output "$openspec_log"
python3 scripts/verify-openspec-delta-guard.py --probe
```

The predicate failure names the matched line; the probe failure names the fixture whose shape the CLI no longer reports.

## Consequences

- Content the CLI would drop fails the pull request instead of being silently dropped at archive; a non-recognised
  section can leave an intended requirement unchanged, while stray prose is content the author left where the CLI does
  not apply it.
- The gate gains a script, its test, a Gradle test task in `check`, and two CI invocations; the corpus's existing
  long-requirement warnings are unaffected.
