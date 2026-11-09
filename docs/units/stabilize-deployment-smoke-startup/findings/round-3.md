- [claim-unreproducible] docs/units/stabilize-deployment-smoke-startup/deep-review.md:10-13 — the record states the
  unit's diff is "CI configuration and workflow shell only" and ascribes `NOT_NEEDED` to it, but the cited tool
  classifies `.github/` and `helm/` as `INFRASTRUCTURE` and returns `REQUIRED` for them; the command printed
  `NOT_NEEDED` only because those edits do not exist yet, and task 3.5 asserted the stale outcome as a literal.
