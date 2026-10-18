# Tasks

## 1. Correct the config's per-artifact rules

- [x] 1.1 `openspec/config.yaml`, `rules.specs` — replace `DEPRECATED` with `RENAMED` and name the `FROM:`/`TO:` form.
      Verify: the rule names `RENAMED` and `grep -c DEPRECATED openspec/config.yaml` is 0. **Done.**
- [x] 1.2 `rules.proposal` — align its count clause to `rules.design`'s wording; add the single-sourcing clause; add the
      set-widening clause; widen the `Impact` rule past build/tests/deployment. Verify: the two count clauses are
      byte-identical, and the three clauses appear under `rules.proposal`. **Done** — the three count clauses compare
      identical, and `rules.proposal` now carries 9 rules.
- [x] 1.3 `rules.tasks` — add the single-sourcing clause and the count clause. Verify: both appear under `rules.tasks`.
      **Done** — `rules.tasks` carries 9 rules, the two clauses included.

## 2. Verification

- [x] 2.1 The config still loads as the CLI reads it. Verify: `openspec new change openspec-config-probe` (then remove
      the probe) prints no "must be an array of strings" and no "could not parse" line, and
      `openspec instructions <artifact> --json` returns each artifact's own rules with no warning on stderr. **Done** —
      the re-run is clean (`proposal: 9`, `specs: 5`, `design: 8`, `tasks: 9` rules). It first **caught a defect this
      change introduced**: the `FROM:/TO:` spelling carried an unquoted `: `, so YAML read that rule as a mapping and
      the CLI dropped the whole `specs` rule set (`specs: 0 rules`); quoting the scalar fixed it.
      `openspec validate --all` did **not** catch it — only the probe did.
- [x] 2.2 `openspec validate --all` and `./gradlew spotlessApply spotlessCheck` pass. Verify: run them, and the manual
      120-character check is clean over the changed set. **Done** — `spotlessCheck` green, `openspec validate --all`
      27/27 and the change valid (zero deltas accepted under `skip_specs`); the only >120 lines are the formatter-owned
      `design.md` claims-table rows.
- [x] 2.3 The change touches only the `rules` section. Verify: `git diff openspec/config.yaml` shows no edit to the
      `context:` block, the `schema:` line, or the `operations` guidance. **Done** — `git diff -U0` shows four hunks,
      all inside `rules:`.

## 3. The change's own records

- [x] 3.1 Record the pre-write derivation pass: the claims table is in `design.md`, its rows derived from the CLI's own
      files, the two scratch probes, the pre-change config, and `docs/ideas.md`, with the `scripts/claim-scan.py`
      candidates each resolved into a row or dismissed beside the table. **Done** — the table carries 7 rows (the scan
      surfaced two more: the probe's shape-only scope and the narrowed `Impact` rule) and the remaining candidates are
      the listed dismissals.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — the loop converged (clean
      at round 3); rounds 4–5 re-checked the post-loop capture edits, and every round's row is in
      `docs/review-rounds.tsv`.
- [x] 3.3 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. **Done** — one
      durable addition (the per-artifact `rules` injection fact, merged into the docs-refresh bullet's config clause),
      one authoring-system recommendation and one check-routed item (both parked in `docs/ideas.md`), and three lessons
      collapsed as already covered. Applied net `AGENTS.md` delta: +1 line (`git diff -U0 origin/main -- AGENTS.md`).
      **No retirements.**
- [x] 3.4 `docs/ideas.md` — remove the two entries this change implements (the `rules.tasks`/`rules.proposal`
      single-sourcing clause and the `rules.proposal` set-widening clause), and park the follow-ups this change surfaces
      — widening `ci.yml`'s OpenSpec probe to fail on the CLI's "is not a delta section" warning (which subsumes the
      weaker config-content park this task first named), and a committed check of the config's cross-artifact clause
      parity. Verify: the two entries are gone and each new one carries its trailing status tag. **Done** — both entries
      removed and both follow-ups parked under `## 2026-10-18`.
- [x] 3.5 Ask the user for the manual review pass once 3.2 is clean and 3.3 is done; do not commit or archive before
      they approve. **Done** — requested.
