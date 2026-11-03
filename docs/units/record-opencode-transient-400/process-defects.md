# record-opencode-transient-400

### record-opencode-transient-400#1 — A drafted public claim carried an interval no capture supported

Recorded: 2026-10-28 by the unit's session.

Stage: review.

Expected: a claim drafted for publication states only what the captured evidence shows, since a public comment cannot be
retracted the way a repository claim can.

Observed: the first draft of the `#51201` reply said a successful retry came "a 200 four seconds later". The source it
drew from records six retries at 12:14:19Z, all returning the stub, and then "The next attempt at 12:14:35Z, with a
slightly changed body, returned 200" — a 16 s interval, and one longer than the 8.9 s failure window the same report
gives.

Evidence: the pre-publication review of `reply.md`, finding H1 — "'then a 200 four seconds later' is not in the source
and contradicts the numbers beside it".

Consequence: the figure itself was corrected before publication, so no post carried it. The qualifying clause did ship:
the published `#50761` comment says the 12:14:35Z attempt "returned 200" without "with a slightly changed body", and the
follow-up comment on that issue (issuecomment-6463878609) restores it.

Knowledge boundary: the interval and the qualifier come from the quoted comment in `anomalyco/opencode#51201`, not from
a capture this unit made; the review's line anchor is not resolvable against `reply.md` as it now stands, since the
sentence moved in a later rewrite.

- **Decision** — no-change: the doc-claim rule already requires a claim to match its source and its strength, and the
  pre-write pass re-reads every external-artifact claim from the artifact.

### record-opencode-transient-400#2 — A draft attributed a persisted record to the wrong version

Recorded: 2026-10-28 by the unit's session.

Stage: review.

Expected: a measurement is attributed to the version and record that actually holds it, since a reader can check the
cited thread.

Observed: a draft of the `#50761` comment said this unit's own sessions "failed with the 31-byte body". The thread
records that on 2.0.15 the body "is dropped when the error is persisted" and quotes no record, quotes the persisted
record — `{"type":"provider.invalid-request","message":"Provider request failed with HTTP 400","status":400}` — for
**2.0.18**, and reports the body itself persisted only later, on 2.0.21.

Evidence: the pre-publication review of `comment-50761.md`, finding 1 — "The draft asserts the author's own failures
'failed with the 31-byte body' … the supplied facts say the opposite for the author".

Consequence: the finding's first half was repaired before publication by naming the client-side capture on 2.0.15, but
the replacement sentence dated the quoted record to that version too. The misattribution therefore shipped: the
published `#50761` comment says "That version's persisted error record kept only …", and the follow-up comment on the
same issue (issuecomment-6463878609) corrects it.

Knowledge boundary: recorded from the pre-publication review's report, the published comment, and the `#51201` text; not
re-derived from a local error record.

- **Decision** — no-change: the doc-claim rule and the pre-write pass already require the quoted record and its version
  to be read from the entry that states them; the record's evidence retains both published corrections.

### record-opencode-transient-400#3 — The unit's artifacts were written before the review gate's pre-write pass

Recorded: 2026-10-28 by the unit's session.

Stage: implementation.

Expected: the review gate's first stage is the pre-write derivation pass and its claims table, run from the sources
before the unit's artifacts are written, since the pass derives each claim class from its evidence rather than from a
draft.

Observed: the `AGENTS.md` bullet and this file were written from the session's discussion, and the `review-gate` skill
was loaded only when the review loop began, after both edits.

Evidence: in this unit's own command sequence the two writes precede the `skill` invocation for
`.opencode/skills/review-gate/SKILL.md`; the claims table the pass owes is to be recorded on the pull-request checklist,
which does not exist yet.

Consequence: the pass was run afterwards over the written artifacts, so a claim it would have derived from its source
was instead checked against text already drafted. Nothing was published or committed in that state.

Knowledge boundary: recorded from this session's own command order; no claim about whether the pass would have caught
anything the review rounds later did.

- **Decision** — no-change: the pre-write pass is defined over the unit's artifacts, so running it after they are
  drafted is the stated procedure, and one occurrence with nothing published is below the threshold for changing that
  placement.
