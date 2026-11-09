- [test-control] tasks.md:50-53 — Control C repeated control A under `baseline`, where the initial check fails first
  (the same warm-up-only stream) and aggregates under the same `showcaseEvent` name, so an implementation that excluded
  the warm-up only from the initial check would still pass it; the mid-hold check's exclusion stayed unexercised.
