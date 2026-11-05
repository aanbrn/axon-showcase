- [test-control] scripts/test-update-gradle-wrapper.py:194,204 — the mode assertion chmodded the jar before the run, so
  it pinned the pre-run mode rather than what the failing pass changed.
- [test-control] scripts/test-update-gradle-wrapper.py — the interruption branch had no case; deleting it left the suite
  green.
- [test-control] scripts/update-gradle-wrapper.py:41 — the production checksum URL constant was unexercised, since every
  fetch case passed `--checksum-url`.
- [test-control] scripts/verify-wrapper-checksum.py:30 — the widened upper/lower-case acceptance had no case, so a
  regression to lowercase-only passed.
