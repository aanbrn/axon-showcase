- [claim-missing] design.md:88-89 — "The load's read stream tolerates a non-empty list (it seeds one only when the list
  is empty)" is a load-bearing assumption of the gate's warm-up write, with no claims-table row and no source.
- [claim-unreproducible] design.md:56-57 — "The scheduler already places them at `250m`" is false for
  `projectionService`, which the `ci` values leave at the chart's `100m` default, so the rejection of lower CPU rests on
  an inventory that omits the killed service already below `250m`.
- [test-control] tasks.md:23-25 — the gate's failing-case control offers a connection with no frames or a closed port,
  neither of which exercises the design's named false positive (keep-alive comment bytes without a `data:` frame), so a
  gate keyed on any byte would pass the control.
- [claim-unreproducible] design.md:102 — the row's Result ("the line prints, once in the progress block and once in the
  final report") does not match its Source: the grep matches the `Global:` and `showcaseEvent:` assertion lines, not one
  line twice.
