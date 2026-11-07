# Process defects — fix-vision-image-loading

### fix-vision-image-loading#1 — A screenshot in the repository root entered the index through `git add -A`

- **Recorded** — 2026-11-05, by the main agent.
- **Stage** — implementation.
- **Expected** — staging the unit's files would add only the definition, the change dir, and the unit's records.
- **Observed** — `scrape-final-review.png`, a 498,070-byte screenshot in the repository root, was staged by
  `git add -A`; no artifact named it and the design's verification serves the copy under `.playwright-mcp/`.
- **Evidence** — `git diff --cached --stat` listed `scrape-final-review.png | Bin 0 -> 498070 bytes`; the file's bytes
  matched `.playwright-mcp/scrape-final.png` (same SHA-1, 1600×1000); `grep` for its name across the change dir found
  nothing.
- **Consequence** — the file was removed from the index and the tree before any commit, and the rest of the unit stages
  explicit paths; a gate nearly missed it because the file sits just under `verifyLargeFiles`' 524,288-byte limit.
- **Knowledge boundary** — observed: the staged entry, its size and its identity with the probe image; assumed: that the
  browser session wrote the root copy, since its output root is not recorded in this session; not checked: which tool
  call wrote it.
- **Decision** — no-change: already covered by `AGENTS.md`'s `git add` gotcha and `review-gate` §5's explicit-add
  remedy.
