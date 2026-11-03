# Process defects — classify-actionlint-required

### classify-actionlint-required#1 — a pipeline's exit status read as the control's verdict

- **Recorded** — 2026-11-03, by the unit's agent.
- **Stage** — implementation.
- **Expected** — the control's verdict read from the script's own exit status, which `AGENTS.md`'s CI section states: a
  `gh run watch … | tail -6; echo $?` reports `tail`'s status, not the run's.
- **Observed** — the control was run as `bash scripts/test-doctor.sh 2>&1 | tail -12; echo "control exit=$?"`, which
  printed `control exit=0` while the script had failed and printed the required-set mismatch. A later run used
  `echo "${PIPESTATUS[0]}"`, which printed an empty value because the shell is zsh and the array is bash's.
- **Evidence** — the two command lines and their output: `control exit=0` beside the script's own `FAIL:` line, whose
  `'compose docker docker-daemon java python3', not 'actionlint compose docker docker-daemon java python3'` names both
  required sets.
- **Consequence** — one reading said the control passed when it did not; re-run capturing the output into a variable,
  which reported `control exit=1`. No artifact carried the wrong value: the design's row 5 was derived from the
  declaration read, and the corrected run is what `tasks.md` 5.2 cites.
- **Knowledge boundary** — observed in this tree, on this unit; did not survey the repository's scripts or documents for
  the same pipeline-status pattern elsewhere.

- **Decision** — no-change: the CI rule ("never a pipeline's exit code") and `check-and-test-authoring`'s sink-status
  clause already require reading the subject's own status.
