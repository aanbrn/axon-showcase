- [contradiction] scripts/update-gradle-wrapper.py:187-211 — a KeyboardInterrupt during a pass unwound the
  TemporaryDirectory, deleting the printed snapshot and leaving the wrapper half-rewritten with no recovery material.
- [docs-residue] docs/ideas.md:53-54 — the gradle-best-practices entry still said the command and pack carry the
  two-runs procedure, where they now carry a tool with a checksum and a rollback.
- [test-control] scripts/test-update-gradle-wrapper.py:93-99 — the fixture server answered every path, so the checksum
  URL's `-all.zip` form was unexercised.
- [test-control] scripts/test-update-gradle-wrapper.py — the empty-body refusal and the same-target re-run had no cases,
  though two Done notes claimed them.
- [test-control] scripts/test-update-gradle-wrapper.py:197 — the mode assertion pinned a mode no fixture ever changed,
  so it passed with the restore's chmod removed.
- [claim-missing] openspec/changes/pin-wrapper-distribution-checksum/design.md:132-141 — the byte-identical-jar claim
  that decides the completion criterion had no claims row.
- [contradiction] scripts/verify-wrapper-checksum.py:30 — the check accepted lowercase hex only, while the requirement
  and scenario say "64 hexadecimal characters".
- [missing-task] openspec/changes/pin-wrapper-distribution-checksum/tasks.md:110 — task 4.2 stayed open though its
  approval was recorded.
- [formatting] scripts/*.py — nine lines exceeded 120 characters in files Spotless does not own.
