- [claim-unreproducible] docs/units/stabilize-deployment-smoke-startup/deep-review.md:5 — the recorded derivation no
  longer reproduced: the router printed `ROUTING: RECOMMENDED` with `[repeated-class]`, because the unit's own rounds 3
  and 4 each carried a repeated class, while the record still stated `NOT_NEEDED` with no signal and the owner's
  run-or-skip as not applicable.
- [stale-base] origin/main@0deca079 — the branch sat on `21b100bf` behind `origin/main` after #871, so
  `git diff --name-status origin/main` reported another unit's files as deleted.
