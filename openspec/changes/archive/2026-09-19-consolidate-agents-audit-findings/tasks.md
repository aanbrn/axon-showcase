# Tasks — consolidate the agents-auditor's reconciliation findings

## 1. The two merges

- [x] 1.1 Collapse the Docker Images NGINX hold-back paragraph (`AGENTS.md`, "Docker Images") to the config-relevant
      facts plus a pointer to the host-state gotcha. **Keep**: the held-back version, "identical build with `1.2.0`
      succeeds and x86 CI builds `1.2.1` fine", the cause being unsettled, the "**this holds a machine working, it does
      not report a repo-wide defect**" scoping (which appears nowhere else — the host-state gotcha frames the opposite,
      "a host explanation exonerates no pin"), the close-out, and the `buildpackUpdates` note. **Drop**: the
      `io.buildpacks.buildpackage.metadata` signature and the AArch64-in-amd64 symptom, both of which a sibling gotcha
      already owns. Reword the close-out to refer to "the tracked upstream issue" rather than repeating the `#1340`
      markdown link. The paragraph keeps `#1340` as a bare identifier pointing at the gotcha; only the `issues/1340`
      _hyperlink_ is dropped (it occurred once, in that paragraph), so the reference now survives as an identifier in
      three places. Done — the paragraph now points at the two sibling gotchas by name and keeps the machine-scoping
      sentence verbatim; verified by flattened grep that the scoping sentence has one home and the `#1340` link no
      longer appears in it.
- [x] 1.2 In the "Never chain an edit to a commit" gotcha, replace its re-derivation of the _staged-files_ remedy with a
      cross-reference to the "`git add <dir>` / `git add -A` can sweep untracked generated artifacts" gotcha, which owns
      that `git diff --cached --name-only` check. **Keep** the second gotcha's own _content_ check — reading whether the
      edit actually landed (`git diff --cached`, `git show --stat`) — which the first does not cover, since a names-only
      check cannot see a no-opped edit. Done — the bullet now says "Inspect the staged set before committing — the
      `git add <dir>` gotcha above owns that files check — and read the diff's _content_, not just its file list".

## 2. The removal

- [x] 2.1 Fold `mustInstallAfter`/`mustUninstallAfter` into the "Helm release order" rule as the mechanism that defines
      the order, then delete the `helmInstallToLocal` bullet (its `tags "*"` clause governs no decision and git
      preserves it). Done — the rule reads "… → axon-showcase, declared by `mustInstallAfter`/`mustUninstallAfter` in
      `build.gradle.kts`. Uninstall in reverse.", and the bullet is deleted.

## 3. The structural item

- [x] 3.1 The auditor proposed promoting the "A check is evidence only once it has been shown to fail" gotcha to a `###`
      subsection. Done as a **revert to the bullet form**: the promotion was tried, then undone, because a `###` heading
      mid-`## Gotchas` scopes every following gotcha to the subsection (there is no closing heading), and the heading's
      subtitle duplicated the block's own lead sentence. The block is unchanged in shape (one bold-lead parent with its
      seven bold-lead sub-modes) — its sub-modes were already independently greppable, so the proposed improvement was
      unnecessary. The promotion also exposed a real defect (a nested list whose items the dedent left adjacent to the
      following gotchas); reverting to the bullet form restores the correct list structure, and the blank lines the diff
      adds are cosmetic.

## 4. The auditor definition

- [x] 4.1 Read `.opencode/agent/agents-auditor.md` for a genuine self-repetition and trim it if one exists; if none
      does, leave the file untouched and record that in this task. Do **not** edit it to "fix" the multi-artifact
      bullet's mention of a trigger command. Found and fixed a real one: the "do not edit" rule was stated twice (the
      report line's "Report, do not edit." and a standalone "Never modify any file" paragraph). It is now stated once,
      in the Method line. The trigger pair was left alone, as this task requires.

## 5. Verification

- [x] 5.1 Anchors lose no evidence, checked per anchor: the `io.buildpacks.buildpackage.metadata` signature and the
      AArch64 symptom each have one home (the sibling gotchas), the Docker paragraph still carries the machine-scoping
      sentence, the `#1340` reference resolves from the gotcha, and `mustInstallAfter` now lives in the release-order
      rule. Done — flattened grep confirms one home each; `git diff --cached` legitimately appears twice (files vs
      content check).
- [x] 5.2 `./gradlew spotlessApply` then `./gradlew spotlessCheck` green, and no line over 120 in the touched
      non-formatter files. Done.
- [x] 5.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green, and `openspec validate --all` with the change
      validating as `skip_specs`. Done — `check` green, `openspec validate --all` 23/23.
- [x] 5.4 Re-ran the `/audit-agents` pass as its own control. All four prior items are resolved (the two merges, the
      removal, and the structural proposal — resolved as a revert — the auditor confirmed each and verified the anchors
      survived). It also caught one **regression** the promotion introduced: the subsection's nested list was never
      closed, so ~40 following gotchas rendered under it — the reason task 3.1 was reverted. The second regression it
      named (a heading subtitle duplicating the lead) is moot once the heading is gone. Re-run after the revert: no
      findings remain.
