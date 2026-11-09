# Process defects — stabilize-deployment-smoke-startup

### stabilize-deployment-smoke-startup#1 — Findings files written outside the instance shape

- **Recorded** — 2026-11-09, by the implementing agent.
- **Stage** — implementation.
- **Expected** — the review-round findings files follow the entry shape the review-gate skill states, one entry per
  finding as `- [<class>] <location> — <claim>`.
- **Observed** — both `findings/round-*.md` files written during the proposal stage carried a `# Findings — …` heading,
  and three entries put a multi-token location (`tasks.md:34-37,42-48; design.md:…; …`) where the shape's location is
  one token.
- **Evidence** — `python3 scripts/commit-hygiene.py --finding-instances` printed
  `round 5 carries 1 line(s) outside the instance shape` and `round 8 carries 3 line(s) outside the instance shape`.
- **Consequence** — two `./gradlew check` runs went red on `:verifyFindingInstances` (2m14s and 1m36s) before the files
  parsed; the files were rewritten in the same unit and the check then passed.
- **Knowledge boundary** — I did not run `--finding-instances` before invoking `check`, and I did not verify whether the
  skill's wording constrains the location to a single token.
