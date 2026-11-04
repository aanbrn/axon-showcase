- [claim-unreproducible] docs/units/record-link-sweep-overreach/review-handover.md:20-22 — the formatting and gate rows
  were measured before the hand-over itself was written, and `spotlessMarkdownCheck` now fails on its re-wrapping.
- [claim-unreproducible]
  /private/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/record-link-sweep-overreach-pr.md:31 — the row's
  second command omits `--format=`, so it prints 22 header-bearing lines where the row says 16.
- [test-control] /private/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/record-link-sweep-overreach-pr.md:33
  — comparing the nine records with `origin/main` cannot fail while `HEAD` equals it, so the row does not discriminate
  the restored state from the unreverted one.
