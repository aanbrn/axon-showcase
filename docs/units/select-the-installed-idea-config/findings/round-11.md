- [test-control] scripts/setup-idea.sh:110-118 — the empty `idea_bundle_args` expansion aborts under bash 3.2 with
  `set -u`, and no case covered the no-launcher/no-explicit run
- [coherence] scripts/ensure-idea-settings.py — a launcher outside a bundle, or with an unreadable plist, stopped
  instead of falling through to the scan
- [coherence] scripts/ensure-idea-settings.py — the same-version tie refusal was unreachable whenever the wrapper passed
  a launcher
- [missing-task] openspec/changes/select-the-installed-idea-config/tasks.md — the completed tasks were unticked
