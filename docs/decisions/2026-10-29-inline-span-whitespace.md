# Decision record — the inline-code-span whitespace check

A dated record of a feasibility probe and its decision. The parked idea asked for a prototype before any gate was built,
the prototype blocked the rule, and the idea is closed here with its evidence instead of being left to be re-attempted.
It is not a window retrospective, so it lives in `docs/decisions/` rather than `docs/retrospectives/`: the digest names
the newest date-named file dated at or before `since` in that directory as the previous retrospective, and a decision
record cannot answer for a window.

## The question

`docs/ideas.md` asked for a markdown-integrity mode that fails a run of two or more spaces inside a single-line inline
code span: Prettier never rewrites a span's interior, `spotlessCheck` therefore stays green, and the 120-character check
measures length only. It recorded two reproductions — `fix-gateway-cors-allowed-headers` (#359) and
`bump-kps-chart-2026-10-25`'s five-space `kubectl get pods -n monitoring` span — and ended with its own guard:
"prototype over the gated set first, since one false-positive class would block it".

## The probe

- **Rule:** on each line outside a fenced block, inside each inline code span, a run of two or more spaces is an
  offender.
- **Scope:** the Spotless markdown target's files — 164 at probe time, 165 with this record — which is the set
  `verifyTableRows` reads: `docs/**`, `AGENTS.md`, `README.md`, `openspec/specs/**`, `openspec/changes/**`, the
  `.github/**` markdown, `SECURITY.md`, and the project-authored `.opencode/` markdown, with
  `openspec/changes/archive/**`, the generated `opsx-*` commands, the generated `openspec-*` skills, and the vendored
  `ast-grep` and `axon4to5-*` trees excluded.
- **Result:** ten hits, every one literal content the repository preserves on purpose.

| Location                                        | Span                                               | Why it must keep its spacing          |
| ----------------------------------------------- | -------------------------------------------------- | ------------------------------------- |
| `AGENTS.md:1270`                                | `paketo-buildpacks/procfile     5.15.0`            | the documented example of this defect |
| `AGENTS.md:1890`                                | `Note:  java  …`                                   | a quoted report line                  |
| `AGENTS.md:1891`                                | `^  <key> `                                        | a regex                               |
| `clarify-generalization-parked-match.md:170`    | `[repeated-class] 5 rows repeat a prior     class` | a quoted report line                  |
| `exempt-merge-commits-from-assignment.md:34-35` | two diff lines                                     | a quoted diff                         |
| `exempt-merge-commits-from-assignment.md:56`    | a quoted source line                               | literal source                        |
| `extend-owner-reply-language-scope.md:39`       | aligned output                                     | a quoted rendering                    |
| `retry-infra-verification-fetches.md:33`        | `-  -  0`                                          | a ledger-row excerpt                  |
| `scope-chart-repository-fetch.md:53`            | `7  contradiction  -  1`                           | a ledger-row excerpt                  |

Seven of the ten were verified by reading the quoted line; the three `AGENTS.md` spans were re-read in place. The
aggregate of ten was produced by the probe run and is not independently re-derived here.

## The decision

The rule is **rejected**. It detects repeated spaces but cannot establish whether the spaces are erroneous, and the
repository's own artifacts quote aligned output, diffs, regexes and ledger rows whose spacing is data. A "looks like a
command" narrowing does not separate the two — `paketo-buildpacks/procfile     5.15.0` has the target's shape.

This rejects **this predicate**, not machine checking in principle: the `Formatting` convention's instruction to
proofread inline-code content stays as the remedy, and no guidance sentence was added claiming a span's interior is
unverifiable.

## Reopening condition

A later attempt needs an independently justified way to tell content intended for normalization from literal evidence,
rather than exclusions keyed on today's filenames or examples — for instance an explicitly identified class of runnable
examples. Before it becomes a gate it would need:

- positive controls carrying the original defects (#359's span and the five-space `kubectl` span);
- negative controls preserving the documented example, the quoted output, the regex and the diffs;
- a coverage boundary the owner accepts.
