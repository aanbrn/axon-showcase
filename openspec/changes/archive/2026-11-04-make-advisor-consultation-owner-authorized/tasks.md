# Tasks — make advisor consultation owner-authorized

The unit's record is `docs/units/make-advisor-consultation-owner-authorized/unit.md`.

## 1. Correct the sites that permit an agent-initiated consultation

- [x] 1.1 `AGENTS.md`, the technical-advisor bullet: replace "Invoke `/consult <question>`, or delegate directly with
      the same focused handoff, when a proposal, implementation, disagreement, or next step needs an independent check"
      with the owner-authorization rule — the agent states the question and proposes the consultation, and it runs only
      on the owner's `/consult` or explicit instruction. Keep "It is not an automatic review step and adds no gate."
- [x] 1.2 `.opencode/commands/consult.md`: delete "A calling agent may also delegate directly to `technical-advisor`
      with the same handoff; this command is the owner-facing entry point, not the only path." and state the
      precondition in the Trigger: the command and the owner's explicit instruction are the entry points, and an agent
      proposes a consultation rather than making one.
- [x] 1.3 `.opencode/agent/technical-advisor.md`: the `description` says "Use on demand via /consult or by direct
      delegation"; state the authorization instead. In the body, the handoff's "The owner has asked you one concrete
      decision question" gains the corollary that a handoff naming no owner authorization is refused as such.
- [x] 1.4 The handoff carries the authorization it rests on — the advisor definition's field list, `/consult`'s step 3,
      and the delta's handoff scenario — since the advisor sees only the handoff and could otherwise refuse every
      consultation, the owner's own included.
- [x] 1.5 `/consult`'s "Direct delegation" section is qualified by the owner's instruction, and its Trigger is split
      into the three authorizing forms with the agent's proposal duty stated separately.

## 2. Withdraw the parked ordering idea

- [x] 2.1 `docs/ideas.md`: remove the `## 2026-11-03` entry "Settle a corrected unit's shape with the advisor before its
      artifacts are written", and remove the section heading with its last entry (`verifyIdeasSections` fails an empty
      section). The reason the idea was wrong is recorded in this unit's `process-defects.md`.

## 3. Verify

- [x] 3.1 `grep -in "delegat" AGENTS.md .opencode/commands/consult.md .opencode/agent/technical-advisor.md` is read hit
      by hit: each delegation it names is either the owner-authorized case the rule admits or an unrelated rule (the
      advisor's "no recursive delegation"). The pattern is case-insensitive and the section heading is one of its hits,
      since a narrower one missed `consult.md`'s "an agent that delegates directly" and "**Direct delegation.**".
- [x] 3.2 `openspec validate --all` passes, and `./gradlew spotlessApply` / `spotlessCheck` run after the last edit.
- [x] 3.3 The review gate runs over the change, including the routing decision.

## Claims table

| Claim                                                                           | Source or command                                                                                                                                                  | Result                                                                                                         |
| ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------- |
| Four sites permitted an agent-initiated consultation                            | `git grep -in "delegate directly\|delegates directly\|direct delegation" ba3e0937 -- AGENTS.md .opencode/commands/consult.md .opencode/agent/technical-advisor.md` | `4` hits, each a permissive site: `AGENTS.md:1470`, `consult.md:10`, `consult.md:63`, `technical-advisor.md:5` |
| The spec states the direct-delegation permission in one clause and one scenario | `git grep -c "Direct delegation" ba3e0937 -- openspec/specs/showcase/quality/agent-skills/spec.md`                                                                 | `2` hits, both in "A consultation receives a focused handoff"                                                  |
| The requirement already forbids automatic consultation                          | `git grep -c "No automatic consultation" ba3e0937 -- openspec/specs/showcase/quality/agent-skills/spec.md`                                                         | `2` hits — the statement and its scenario — in "The consult command resolves the question"                     |
| The parked entry is the only one in its dated section                           | `git show ba3e0937:docs/ideas.md \| sed -n '/^## 2026-11-03/,/^## /p' \| grep -c '^- '`                                                                            | `1` entry, so removing it removes the heading                                                                  |
