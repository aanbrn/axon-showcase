# Process defects — split-the-review-ledger

### split-the-review-ledger#1 — a resumed unit was paused again in the same turn

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — implementation.
- **Expected** — a session that resumes a unit continues its recorded next step; a pause is the alternative to
  continuing, not the default at the first boundary.
- **Observed** — the session resolved and verified the unit, took the writer handover, revised the planning artifacts,
  and completed the data split, and then wrote a `Resume checkpoint` and set `State: paused`, closing with the
  instruction to continue in a new session. The session's stated reason for the pause was that the remaining work — 67
  failing ledger tests, two further script suites, the build inputs, the docs, and the gates — exceeded what it judged
  it could reliably complete; it cited no harness, provider, or account signal. The owner rejected that outcome; the
  unit was reopened in the same session and the remaining work was completed there.
- **Evidence** — the checkpoint written at that point records `Stage — implementation` with
  `Next step — migrate scripts/test-commit-hygiene.py to per-unit fixtures`; the owner's reply was that the session had
  barely started in a new session before proposing another new one.
- **Consequence** — one turn of the owner's time and a re-opened unit record. The unit's own work was not lost, and the
  tree was not otherwise affected.
- **Knowledge boundary** — observed: the resume request, the pause, the owner's reply, and the reopen. Assumed: that the
  reply directs continuing in this session rather than changing the pause rule. Not checked: whether any harness or
  provider signal would have justified a pause at that point.

- **Decision** — no-change: `pause-resume-unit`'s decision point already records that a session limitation the agent
  could not measure is its own assessment, which the proposed signal requirement would contradict.

### split-the-review-ledger#2 — the split's verification task carried counts the change itself falsified

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — proposal, carried into implementation.
- **Expected** — task 1.2's verification names the figures the split must preserve, so a wrong split fails it.
- **Observed** — task 1.2 required "2,326 data rows and the 40 comment lines" to be present exactly once. The working
  tree holds 2,327 data rows, because this unit's own round was appended; and 24 of the 40 comment lines are the
  normative header the change retires into the capability spec, so they cannot survive in the per-unit files.
- **Evidence** — `cat docs/units/*/rounds.tsv | grep -vc '^#'` prints 2327; `cat docs/units/*/rounds.tsv | grep -c '^#'`
  prints 14; the deleted file's first 24 lines were the field contract.
- **Consequence** — the task was reworded to the measured figures and the header's disposition before it was ticked. The
  verification itself was run and passed against those figures, so no wrong result shipped.
- **Knowledge boundary** — observed: the two counts and the header block. Assumed: the 40-line figure was measured
  before this unit's round was appended. Not checked: whether round 1 had already flagged the count.

- **Decision** — no-change: the point-in-time-count rule already requires stating the mechanism rather than a count the
  corpus moves.

### split-the-review-ledger#3 — a resume reply guessed the reply language instead of asking

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's report through
  `/flag-process-defect`.
- **Stage** — implementation, at the unit's resume.
- **Expected** — `AGENTS.md`'s output-language convention: a reply the owner reads uses the language of the owner's
  current request, and when that language cannot be established the agent asks rather than guesses.
- **Observed** — the resume request carried the English `/resume-unit` command text and a unit name, with no prose from
  the owner. The agent replied in English — the resolution report, the live-state table, the writer-handover question,
  and the pause report — without asking which language to use. The owner's next message was in Russian, and the agent
  switched to Russian from that point.
- **Evidence** — the owner's report is the signal; the verified fragment is this session's own sequence: the English
  assistant messages before the owner's Russian reply, and the request's content, which is the command definition plus a
  unit name and no owner prose.
- **Consequence** — the owner had to correct the language in the next message; the resume's first turns were in the
  wrong language. No artifact or result was affected.
- **Knowledge boundary** — verified: the reply language and the request's content, both in this session. Reported but
  not verified: that the previous session ran in another language, which this session cannot read. Not checked: whether
  the unit record or any artifact records a language a resume could take up.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-27 reply-language entry, whose reopening
  condition this instance meets and which now carries it as a third instance.

### split-the-review-ledger#4 — work the design named but did not deliver was never parked

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`), on the owner's report through
  `/flag-process-defect`.
- **Stage** — proposal: the design's Non-Goals.
- **Expected** — `AGENTS.md`'s "Work a change surfaces is parked durably": when a change names a companion change it
  will not deliver, that work is parked in `docs/ideas.md` under a dated section with its trailing status tag.
- **Observed** — the design's Non-Goals name work the change will not deliver: "Moving the process-defect records: they
  are already per-unit, and the move would buy locality only" (`design.md:34`). No `docs/ideas.md` entry recorded that
  work. The `## 2026-11-03` section this change removed carried only the implemented ledger-move idea, so after the
  removal the file's newest section was `## 2026-11-01`; the three entries covering these subjects were written only on
  the owner's instruction later in the unit.
- **Evidence** — the owner's report is the signal; the verified fragments are `design.md:34` and this unit's read of
  `docs/ideas.md`'s section list, whose newest section was `## 2026-11-01` once the 2026-11-03 entry was removed.
- **Consequence** — the process-defect move stayed out of the parked list from the design stage until this turn. The
  other two subjects the owner named — a unit's review hand-off in the unit directory, and persisting each round's
  finding instances — surfaced during this session's review and advisor work and are parked in the same section now.
- **Knowledge boundary** — verified: the design's Non-Goal and the ideas file's section order. Reported but not
  verified: that the prior session discussed these as ideas and agreed to park them; that session's messages are not
  readable here. Not checked: whether the pause checkpoint's "Blockers and open decisions" field was expected to carry
  them.

- **Decision** — no-change: the parked-work rule already requires a companion change the design names to be parked in
  `docs/ideas.md`.
