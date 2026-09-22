# Tasks

## 1. Record the boundary rule

- [x] 1.1 Add the OUTCOME-ONLY half to the `AGENTS.md` promotion-gate bullet's pointer clause (the pointer rule itself
      already existed) and extend the matching `agent-skills` delta clause to cover the outcome-only case
- [x] 1.2 Confirm the `agent-skills` delta's `MODIFIED` block already states it (written in the specs artifact) — no
      further spec edit unless the wording drifts from 1.1

## 2. Move the configurer invariant into the specs

- [x] 2.1 Confirm the four delta blocks (`gateway/rest-api` ×2, `read-side/query-service` ×2) carry each main-spec
      requirement's full description and scenarios; the clause and the new scenario are the only additions
- [x] 2.2 Rewrite the `AGENTS.md` gotcha for the WebFlux blocking-execution configurers as a pointer: the rule ("do not
      remove the routing — it is what makes the error paths resolve"), a one-line why, and a reference to the specs that
      now own it; delete the 16-line measured narrative (it lives in the design and the spec scenarios)

- [x] 2.3 Retarget the `AGENTS.md` clause that used the blocking-execution bullet's evidence points as its example (the
      "check a mechanism against every measurement" gotcha) — it now cites the change's `design.md`, which carries both
      points, since the bullet no longer holds them

## 3. Verify

- [x] 3.1 `./gradlew spotlessApply` then `spotlessCheck` — the change-dir markdown and `AGENTS.md` are formatter-gated
- [x] 3.2 `openspec validate --all` passes (23 items: 22 specs + this change) and each `MODIFIED` block preserves its
      requirement's scenario count (5/5, 3+1, 2+1, 3+1, 2+1) — count them, do not trust the validator alone
- [x] 3.3 `grep -Ec "describe a rule's subject .*only as an outcome" AGENTS.md` >= 1, keyed to the NEW wording (the
      phrase is bolded, so a literal match fails) — the old "rationale is normative in a spec" phrase already matches at
      HEAD, so it would pass vacuously — and the configurer gotcha is a short pointer (rule + why + spec references),
      with no orphaned reference to the deleted narrative

## 4. Docs

- [x] 4.1 Check `AGENTS.md`'s "Docs refresh on change" bullet and `openspec/config.yaml`'s `context` for a fact this
      change moves; update in the same change if so
- [x] 4.2 Sweep `docs/ideas.md` for any entry this change implements or invalidates, and remove it if present
