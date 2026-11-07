- [coherence] openspec/changes/track-agent-session-continuity/design.md:146-147 — the backfill bound is not carried into
  D5 or the proposal's hand-over bullet, which still state the unbounded version
- [coherence] openspec/changes/track-agent-session-continuity/design.md:165 — D6 records the forced replacement before
  starting it, while the round-6 delta starts it from the hand-over and records the row at outcome time
- [over-claim] openspec/changes/track-agent-session-continuity/design.md:166 — the wrong-identifier cost is bounded to
  one restart, but when the wrong value equals the resuming session's identifier the honest replacement is rejected by
  the parentage rule
