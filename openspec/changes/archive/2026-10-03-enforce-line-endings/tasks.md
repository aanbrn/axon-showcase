# Tasks

## 1. The policy

- [x] 1.1 Add `.gitattributes` at the repository root with `* text=auto eol=lf` and `*.bat text eol=crlf` (the batch
      exception for `gradlew.bat`). Verify with
      `git check-attr -a README.md gradlew.bat     gradle/wrapper/gradle-wrapper.jar` showing `eol: lf` for the text
      file, `eol: crlf` for the batch file, and `text: auto` for the jar.
- [x] 1.2 Renormalize the one affected file: run `git add --renormalize gradlew.bat` and confirm
      `git diff --cached     --stat` shows only `gradlew.bat` changed. Verify `git ls-files --eol gradlew.bat` now
      reports `i/lf w/crlf` (index LF, working checkout CRLF — the correct axis; before this change it was
      `i/crlf w/crlf`), and that `git ls-files     --eol` still holds exactly 1776 `i/lf` (the 1774 plus `gradlew.bat`),
      one `i/-text`, and one `i/none`.
- [x] 1.3 Prove the policy in a scratch clone: add `.gitattributes`, then commit a CRLF text file and a `.bat`, and
      confirm `git ls-files --eol` reports the text file as `i/lf w/crlf` (git warns "CRLF will be replaced by LF") and
      the `.bat` as `i/lf w/crlf` (index LF, checkout CRLF) — so a Windows contributor cannot commit CRLF into a text
      file's stored blob. Record the commands and output; delete the scratch clone.

## 2. Documentation

- [x] 2.1 Document the policy (an addition, not a falsified-claim fix — verify no live doc asserts there is no
      `.gitattributes` or that Spotless is the only normalizer first): `AGENTS.md`'s Formatting convention and
      `README.md` if it describes formatting setup, naming the `.gitattributes` policy alongside Spotless. Remove the
      implemented idea from `docs/ideas.md` (the 2026-09-24 "Enforce line endings via `.gitattributes`" entry). Verify
      by reading the edited passages, `grep -n "gitattributes" AGENTS.md README.md` hitting the new prose, and the idea
      grep returning nothing.
- [x] 2.2 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck` and
      `openspec validate --changes`, and confirm all pass.

## 3. Verification

- [x] 3.1 Confirm the working tree is otherwise unchanged by the policy: `git status --porcelain` lists only the
      intended files (`gradlew.bat` plus the new `.gitattributes` and the doc edits), and no other tracked file shows as
      modified by the renormalization.
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm it stays green (the change adds no
      `check` member).
- [x] 3.3 Refresh the `commit-hygiene` capability's `## Purpose` in the **archive commit** (a delta cannot carry a
      Purpose, so it is edited directly when the change is archived): its sentence enumerates the tracked-file checks
      and must gain the line-ending policy alongside the others. Verify the archived main spec's Purpose names it.
- [x] 3.4 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task. Applied: one net-0 merge into the Formatting line-ending sub-bullet — its
      closing sentence gained the verification axis (`git ls-files --eol` reads index vs checkout; `check-attr` reports
      `text: set` for the explicit `*.bat` rule but `text: auto` for the catch-all); captured: enforce-line-endings.
