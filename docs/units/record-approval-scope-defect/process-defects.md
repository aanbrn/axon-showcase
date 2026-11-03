# Process defects — record-approval-scope-defect

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### record-approval-scope-defect#1 — A checklist tally was stated that the unit's own later review rounds falsified

- **Recorded** — 2026-11-01, by the writing session, during the quick-review loop.
- **Stage** — review (the quick loop, round 5).
- **Expected** — the review gate's rule for a unit's own status: state the mechanism and the pointer, never a count or a
  "closing" status the unit's remaining steps move, since the next round falsifies it.
- **Observed** — the saved pull-request body's review-gate checklist line stated that the loop had run three rounds with
  the closing one clean, giving each round's count; rounds 4 and 5 followed that line, and the round-5 review reported
  it as a contradiction with the ledger it cited.
- **Evidence** — quick-review round 5, finding 1 (`contradiction`), which quoted the checklist line against
  `docs/review-rounds.tsv`'s then-current rows. The line now states the loop's mechanism and points at the ledger, with
  no tally.
- **Consequence** — one review round; the line was replaced with the mechanism-and-pointer form, and no committed
  artifact carried the tally.
- **Knowledge boundary** — observed in the body and in the round's report; whether the line would have reached delivery
  undetected is not established, since the loop caught it before the manual pass.

- **Decision** — no-change: `review-gate` §1 already requires a unit's status line to state its mechanism rather than a
  status its own remaining steps move, and §2 assigns round state to `docs/review-rounds.tsv`; the line was repaired
  before delivery.

### record-approval-scope-defect#2 — A claims-table row certified one half of a two-fact claim

- **Recorded** — 2026-11-01, by the writing session, during the quick-review loop.
- **Stage** — review (the quick loop, round 4).
- **Expected** — the claims table's completeness rule: a row's evidence covers the claim it carries, so a sentence
  resting on two facts takes a row per fact, each with a source that produces it.
- **Observed** — the record's `Evidence` rested on two statements from `.opencode/skills/ship-change/SKILL.md` — the
  administrator `--admin` route, and the owner being a bypass actor with `bypass_mode: always` — while the table carried
  one row whose literal certified only `--admin`.
- **Evidence** — quick-review round 4, finding 1 (`claim-missing`), quoting that row against the record's `Evidence`
  sentence. The table now carries the `--admin` route and the bypass actor as separate rows.
- **Consequence** — one review round; the row was split, and two rows a later consult found to overclaim were narrowed
  to what their sources print.
- **Knowledge boundary** — observed in the table and the round's report; the narrowing happened after this record's
  first draft and is described here only as far as it touches this row.

- **Decision** — no-change: `review-gate` §1 already requires each fact's own source and forbids one part's command
  standing for the whole; the row was split into its two facts.

### record-approval-scope-defect#3 — A record stated the agent's own reading as the episode's explanation

- **Recorded** — 2026-11-01, by the writing session, after the owner's technical-advisor consult and before his
  approval.
- **Stage** — review (after the quick loop's closing round, before the manual pass).
- **Expected** — the record workflow's "Diagnose nothing, propose nothing": a record carries observed facts, a cause
  belongs to the generalization, and a self-report about why the agent acted is an account rather than an established
  cause.
- **Observed** — the merged unit's record `#3` was first titled so that it presented the owner's reply as an approval
  reply, and its `Observed` stated the agent's reading of that reply as the episode's explanation; both were rewritten
  before anything was committed, so the superseded text exists only in the session.
- **Evidence** — the advisor consult's first confirmed problem, against `AGENTS.md`'s record workflow. The title now
  names the delivery steps and the absent approval, `Observed` states the sequence and the two negatives, and the
  reading sits in `Knowledge boundary` attributed as the agent's account.
- **Consequence** — one consult and an edit to the title, `Observed`, `Evidence`, and `Knowledge boundary`; nothing had
  been committed, so no shipped artifact carried the cause framing.
- **Knowledge boundary** — observed in the artifact and the consult's report; whether a reader would have taken the
  title as a finding about the owner's reply is not established.

- **Decision** — no-change: the record workflow's step 3 already forbids a diagnosed cause and step 2 attributes an
  account as the agent's own; the title and fields were rewritten before anything was committed.

### record-approval-scope-defect#4 — Two claims-table rows asserted more than their results showed

- **Recorded** — 2026-11-01, by the writing session, after the owner's technical-advisor consult and before his
  approval.
- **Stage** — review (after the quick loop's closing round, before the manual pass).
- **Expected** — a claims row's evidence covers the claim it carries: its Result shows what the Claim asserts, and a
  narrowed Claim is reconciled with the artifact's own sentences in the same edit.
- **Observed** — two rows were narrowed or added without that re-derivation: the PR row asserted that the required
  approving review had never been given, from a field that reports only the current review state, and the bypass row
  asserted that the exception applied to that actor from a literal identifying no actor; narrowing the first left the
  record's own `reviewDecision` sentence with no row at all.
- **Evidence** — the advisor follow-up consult's two confirmed problems, and quick-review round 7's four findings
  (`claim-missing`, `over-claim`, `claim-unreproducible`, `missing-task`) against the rows. The rows now carry
  `"login":"aanbrn"`, `[]` for the review list, and the identity span from `ship-change`.
- **Consequence** — one consult cycle and one further review round, which re-found the class after rounds 4–6 had edited
  the table row by row.
- **Knowledge boundary** — observed in the rows and in the two reports; the reviewer's statement that the four round-7
  findings share one root cause is its own account, not an established cause.

- **Decision** — no-change: `review-gate` §1's literal-represents-the-full-claim rule and §5's failure-handling rule
  ("delete the unneeded fact rather than add the row") already determine the action; the rows were narrowed to what
  their sources print.

### record-approval-scope-defect#5 — A review round's report arrived and its ledger row was not appended

- **Recorded** — 2026-11-01, by the writing session, during the quick-review loop.
- **Stage** — review (between the quick loop's round 7 and round 8).
- **Expected** — the review gate's rule for a round whose report has arrived: append its ledger row and run
  `--unique-rounds --expect <unit>:<round>` and `--session-continuity` immediately, whether or not the row was appended,
  since a check keyed on the append cannot see an append that never happened.
- **Observed** — round 7's report arrived with four findings and no row was appended; round 8's invocation then ran
  against a ledger ending at round 6, and its report reported the missing row as a finding (`missing-task`).
- **Evidence** — quick-review round 8, finding 1, quoting
  `python3 scripts/commit-hygiene.py --unique-rounds --expect record-approval-scope-defect:7` → "has no row for round 7,
  the round whose report arrived"; the ledger held rows 1–6 only.
- **Consequence** — one further review round; both rows were appended and the checks re-run, and the round that first
  named the claims-table repetitions had been the one missing from the record.
- **Knowledge boundary** — observed in the ledger and the round's report; whether the omission would have survived to
  the manual pass is not established, since `--expect` names it at the boundary.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 reviewer-wrapper entry records the deferred
  ledger row, and neither of its `Revisit when:` triggers is met here (this row was recovered, so no `Acknowledged gap`
  row exists); `review-gate` §2 already requires the immediate check.

### record-approval-scope-defect#6 — A Russian advisor report was relayed to the owner in English

- **Recorded** — 2026-11-01, by the writer of a later unit, at the owner's report that this session's relay had switched
  language.
- **Stage** — review, while the unit applied a technical-advisor consultation's findings.
- **Expected** — `AGENTS.md`'s output-language convention: an agent-facing report is English, and so is the hand-off an
  agent sends another agent, while a reply the owner reads uses the language of the owner's current request and follows
  a later switch.
- **Observed** — the owner's request in that turn was Russian. The hand-off to `technical-advisor` asked for an
  owner-facing report in Russian, and the advisor answered in Russian; the two owner-facing messages that followed were
  English, and the session's later messages were Russian again.
- **Evidence** — that turn's stored messages: the owner's request and the advisor's reply are Russian, the two
  owner-facing messages after the reply are English, and the messages after them are Russian.
- **Consequence** — in one turn the owner read a Russian verdict and an English summary of it; no artifact was affected.
- **Knowledge boundary** — observed the stored messages and their language; the owner's screen was not observed, and
  whether other turns of that session carried the same pattern was not surveyed.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-27 reply-language entry already carries this
  record as its second instance and directs compliance investigation rather than another wording expansion.

### record-approval-scope-defect#7 — An absence was asserted about the session's record, which the store contradicted

- **Recorded** — 2026-11-01, by the writing session, after the owner named the session store.
- **Stage** — merge (after the unit merged; the episode belongs to a turn of the merged unit).
- **Expected** —
  - a claim about a surface outside the repository has "no config file or commit behind it, so its evidence is a query
    whose output you have", run before the claim is stated;
  - a search that under-matches "returns a plausible empty or partial result", so an empty reading is evidence only when
    the search can be shown to hit a known positive.
- **Observed** — the owner reported that an advisor's Russian report had been relayed to him in English. The agent
  answered that it saw no such message in this session and asked for a pointer; the owner then named the session store,
  which holds the rows the questioned relay produced.
- **Evidence** — the `session_message` rows of `ses_e695b53b9ffeCh1sn44PGihjod`: seq 3189 is the request that opened the
  turn, and seq 3240 and seq 3349 are the two owner-facing messages that answered it — the rows whose content and
  language `#6` records.
- **Consequence** — the owner's question was answered with a denial and a request for a pointer; a later unit recorded
  the language defect (`#6`), and this record carries the false absence beside it.
- **Knowledge boundary** — observed the store rows and their order. The agent's own account of its check is that it
  re-read its rendered replies and queried no store before answering; that account was not traced, so nothing shows a
  query was skipped, and an under-matching query would have produced the same denial. The session's rendered transcript
  was not inspected, and whether the same pattern occurs in other sessions was not surveyed.

- **Decision** — no-change: the out-of-repository-claim rule requires the query before the claim and a negative claim to
  name the variants examined, and the under-matching-search rule requires a known positive; the store is a surface the
  repository's own records query.
