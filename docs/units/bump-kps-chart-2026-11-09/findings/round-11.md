- [stale-base] docs/units/bump-kps-chart-2026-11-09/review-handover.md — the tree was behind origin/main `634bc3b5`
  (#873), whose diff reverts work the unit did not touch, and the local `docs/ideas.md` edit overlaps that merge.
- [claim-unreproducible] design.md:16 — the sentence says the check's own run takes the target, but the run reports
  `92.17.0 -> 93.1.0` and `92.22.0` comes from the `--versions` listing.
- [missing-task] tasks.md:84 — the upstream report task has no step recording the published reference and its
  `retires when` clause in the parked idea entry, which `upstreamReferences` scans.
- [claim-missing] design.md:130 — the "no CRD apply is owed" conclusion rests on the chart's CRD manifests being
  identical between `92.17.0` and `92.22.0`, a fact the claims table states nowhere.
- [wording] design.md:106 — "the held row, no longer the pin" contradicts its literal: the report after the bump reads
  `92.22.0 -> 93.1.0`, whose left side is the new pin.
