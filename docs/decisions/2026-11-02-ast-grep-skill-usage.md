# Decision record — ast-grep skill usage

A dated record of a bounded exploration and its verdict. It answers whether the vendored `ast-grep` skill is being used
in proportion to the guidance that names it, and whether anything in the arrangement should change. It keeps the
measurement so the question is not re-derived from intuition. It is not a retrospective (it spans no delivery window)
and not an ADR (it binds no standing rule); `docs/adr/README.md` states the boundary, and
`docs/decisions/2026-10-29-inline-span-whitespace.md` is a sibling record of the same shape.

The question arose in a conversation rather than in `docs/ideas.md`. Three places touched the subject before this
record, none of them answering it: `openspec/specs/showcase/quality/agent-skills` requires the skill to be discoverable
and its copy to be verbatim, not that it be used; `docs/ideas.md` carries one parked entry about collapsing the four
declaration sites of the vendored sets, which is a different subject; and the audits of 2026-10-24 and 2026-10-31 say
"`ast-grep` usage matches its vendored skill" inside their advisory class, which is a reference-consistency check rather
than a usage measure. Grepped 2026-11-02.

## The question

`AGENTS.md`'s "Evidence and search" rule tells an agent to load the `ast-grep` skill and search structurally when the
answer depends on code structure. The vendored set is not gated the way sources are: that file's "Vendored agent skills"
bullet owns the declaration sites, and the `showcase/quality/agent-skills` spec's "Vendored skills do not affect the
application build" scenario states the build-time outcome. The question was whether the measured use of the skill, and
of the CLI it teaches, supports that arrangement or contradicts it.

## How the measurement was taken

**Caller-reported.** The figures below come from three queries (`Q1`–`Q3`, reproduced in the unit's pull-request body)
against the local OpenCode database (`~/.local/share/opencode/opencode.db`, table `session_message`, read-only), not
from a repository artifact, and no second party verified the extraction. They are reproducible on this machine while the
database still holds the window.

- **Scope:** sessions whose `session_v2.directory` starts with `/Users/afanasyev/Development/axon-showcase`, so the
  trees of this repository and its worktrees only.
- **Window:** 2026-10-22 → 2026-11-02 10:26:41, split at 2026-10-26 — the day after the last recorded invocation. The
  first bound is the day the skill was vendored; the database's coverage for these trees begins 2026-07-28, and that
  earlier span contributes no figure here. The second window closes at the instant the recording session began, so no
  later session can move its figures.
- **Unit:** an `ast-grep`/`sg` `run` or `scan` invocation at a command position of a shell call, counted per occurrence
  — a regular expression over the JSON tool payload rather than a substring match, since a command that only quotes the
  phrase (a grep for it) is not an invocation. A search _decision_ is a shell call containing `rg`/`grep`, or a call to
  the native `grep` tool.
- **Excluded:** the session that recorded this measurement.
- **Superseded:** an earlier figure of "0.4% of shell calls" was derived with a pattern that under-matched `rg` over a
  five-day window and is not quoted here.

## What the measurement found

| Signal, this repository's trees only | 2026-10-22 … 10-25 | 2026-10-26 … 11-02 |
| ------------------------------------ | -----------------: | -----------------: |
| Shell calls containing `rg`/`grep`   |               4334 |               8957 |
| Native `grep` tool calls             |                878 |               2341 |
| Shell calls invoking `ast-grep`/`sg` |                 32 |                  0 |
| Shell calls, all tools               |               9524 |              20719 |

- The 32 calls carry 52 `run` searches, 11 `scan` invocations and two `run --help` probes, across six sessions on three
  days: 25 on 2026-10-22, 6 on 2026-10-24 and 1 on 2026-10-25.
- The searches use 22 distinct patterns, all single-node or simple relational ones — `@Bean`,
  `@ConfigurationProperties($$$ARGS)`, `catch ($E) { }`, `class $NAME implements HealthIndicator`,
  `useMutation({ $$$B })`, `except:`, `subprocess.$M($$$A, shell=True, $$$B)` — over `--lang` java (41), ts (6), tsx
  (1), python (3) and kotlin (1), which totals the 52 searches.
- The `skill` tool loaded `ast-grep` twice — 2026-10-22 12:45:04 and 2026-10-24 22:38:10, both recorded under
  `state.input.id` — and the skill's files were also read directly: four `read` calls on 2026-10-22 and two greps on
  2026-10-24. Across the tool's whole population some calls record the skill under `state.input.name` instead, so a
  count keyed on either field alone can under-report.
- In the second window, the sampled searches that name an annotation or a declaration (`@Scheduled`, `@PostMapping`,
  `void \|@Test\|class`, `@DisplayName`, `@Test`) are presence lookups that text answers adequately, and none of the
  sampled ones is a relational query.

## Four candidate explanations, and what the measurement does to each

- **The rule lets an agent satisfy it without loading the skill — refuted.** The "Evidence and search" rule in
  `AGENTS.md` reads "THEN load the `ast-grep` skill and run `ast-grep run -p '<pattern>' --lang <id>`", and the skill
  was loaded: two `skill`-tool loads and four direct reads.
- **The skill's description fails to match the question an agent actually asks — unconfirmed.** The description names
  "find specific code structures" and "find specific language constructs", which cover the rule's examples.
- **Structural search is rare here, so `rg` is the right tool by default — the observed tool choice is text-first, and
  the demand behind it is unmeasured.** `rg` and the native tools dominate the same window, and no sampled search is a
  relational query; whether an eligible structural question went unasked cannot be read from a tool-choice trace.
- **The native tools win on cost, or the model under-applies an ungated advisory — unconfirmed, and each needs its own
  evidence.** A cost comparison needs a matched task, one question answered with structure and without;
  under-application needs only an excerpt showing an eligible structural question answered through text-only searching,
  which none contains.

## What the measurement does not show

- **Whether an eligible structural search was answered wrongly.** A trace records the tool chosen, never the correctness
  of its answer. No such case was observed; none is refuted either.
- **Whether the cheap flash model, the skill's load cost, or its wording suppressed use.** Each needs a matched task or
  an instrumented run, and the repository has neither.
- **Whether the figures generalize.** They are one developer's sessions on one machine; the shape of the result, not the
  exact counts, is what the record relies on.

## The decision

Keep `AGENTS.md` and the vendored skill unchanged, and record this verdict.

- `AGENTS.md`'s "Evidence and search" rule stays as written: it already requires the load rather than merely naming the
  command, so there is no loophole to close.
- The vendored tree stays. The known costs are routine provenance upkeep — refreshing the recorded version, commit and
  hashes — and the declaration sites `AGENTS.md`'s "Vendored agent skills" bullet lists, with the build-time exclusion
  the spec's "Vendored skills do not affect the application build" scenario states. What an agent-side load costs is not
  measured here, and this record draws no conclusion about it.
- No gate measures search-tool choice, and none is proposed: a count of invocations would not show that a structural
  question was answered worse.

The alternatives considered and rejected:

- **Tailor the skill's trigger description** — the `agent-skills` spec requires a verbatim copy, and no evidence shows
  misrouting rather than low demand.
- **Rewrite the `AGENTS.md` rule so the skill is the only path** — it already is; the rule was misread in this
  exploration, not written ambiguously, and that misreading is what this record prevents recurring.
- **Retire the vendored set** — non-use alone does not establish that retention costs more than removal. This evidence
  does not establish the three grounds that would justify retirement (a maintenance burden beyond routine upkeep, a
  harmful incompatibility, a verified replacement); it does not rule them out either.

## Reopening condition

A later attempt may reopen this decision on **either** clause — a disjunction, any one of which reopens it.

1. **A text answer that was wrong or incomplete.** A session excerpt from a tree of this repository identifies a
   structure-dependent search answered incorrectly or incompletely through text-only searching.
2. **A concrete cost of retention.** An issue or change identifies work or an incompatibility attributable specifically
   to retaining the `ast-grep` vendored set, naming the affected paths.

- Neither clause authorises rewriting the guidance or retiring the set by itself.
- Routine upkeep — refreshing the recorded provenance when upstream publishes, and repeating the declaration sweep a
  third set would need — is the cost of keeping any vendored set and does not meet clause 2.

A reopening that reads the measurement as "the skill is unused, so remove it" fails clause 2, which is the reading this
record exists to prevent.
