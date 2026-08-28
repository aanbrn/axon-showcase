# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file
is a scratchpad, not a backlog of planned work.

## 2026-08-25

- ~~Add CI~~ — captured by the `merge-governance` change: `.github/workflows/ci.yml` runs a fast Docker-free gate on
  PRs (`check -PskipITs`, coverage gate disabled) and the full `check` on pushes to `main`, plus `openspec validate
  --all`. The required `build` check is enforced by the `main-required-checks` ruleset with no bypass actors. e2e
  cadence and Snyk scheduling remain open follow-ups.
