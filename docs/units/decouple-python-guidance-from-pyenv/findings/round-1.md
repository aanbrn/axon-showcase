- [premise] openspec/changes/decouple-python-guidance-from-pyenv/tasks.md:20 — `uv python pin` is not the same step as
  `pyenv local` for the interpreter the tooling runs (design.md D1 and D3, tasks 1.2 and 2.1), so the plan rests on an
  unverified premise about uv's `PATH`-level effect
- [claim-missing] openspec/changes/decouple-python-guidance-from-pyenv/design.md:22 — two repository-wide claims (no
  manifest, virtual environment, or lockfile; the scripts' imports at design.md:9) have no claims-table row
- [over-claim] openspec/changes/decouple-python-guidance-from-pyenv/design.md:69 — the per-kind dismissals' universal
  reasons do not hold for the record-file candidates the scan reports, and the `absolute` bullet under-counts the corpus
  claims
- [claim-unreproducible] openspec/changes/decouple-python-guidance-from-pyenv/design.md:78 — "five files" where the
  change edits four (the same count is in review-handover.md:14)
- [structured-writing] openspec/changes/decouple-python-guidance-from-pyenv/proposal.md:12 — two "What Changes" bullets
  each hide two independently actionable edits
- [formatting] openspec/changes/decouple-python-guidance-from-pyenv/tasks.md:61 — inline code spans carry
  wrapped-authoring whitespace (also tasks.md:63 and tasks.md:67), and line 67 is 133 characters
