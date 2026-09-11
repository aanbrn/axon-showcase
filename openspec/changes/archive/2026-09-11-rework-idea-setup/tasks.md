## 1. Merge-based reconcile

- [x] 1.1 Generalize — and rename — `scripts/ensure-idea-inspection.py` to `scripts/ensure-idea-settings.py`: a
      settings-merge script that upserts the `config/idea/*.xml` components (palantir-java-format, ktfmt,
      codeStyleConfig) and the test-tier naming inspection into the corresponding `.idea/` files, preserving all other
      content
- [x] 1.2 Rework `scripts/setup-idea.sh`: apply the configuration merge first — it needs neither the launcher nor a
      closed IDE — then attempt the plugin install via the detected launcher only when the IDE is closed, warning and
      skipping otherwise; report explicitly what was restored, left unchanged, or skipped (a manual run is the primary,
      token-free path and should be self-explanatory)

## 2. Agent command

- [x] 2.1 Add `.opencode/skills/setup-idea/SKILL.md` and `.opencode/commands/setup-idea.md` — detect state, apply the
      configuration, gate the plugin install on the IDE being closed, explain the restart (mirroring
      `setup-agent-tools`)
- [x] 2.2 Add `/setup-idea` to the README slash-command table

## 3. Docs

- [x] 3.1 README "Formatting and IDE Setup": keep `./scripts/setup-idea.sh` as the primary path, describe the reconcile
      behavior, note that re-running after drift restores the configuration (and that IDEA applies it on File → Reload
      All from Disk or restart), correct the "run the setup script once (IDE closed, then restart)" line (the
      configuration now applies regardless; only the plugin install is gated on a closed IDE), and mention `/setup-idea`
      as an optional convenience
- [x] 3.2 AGENTS.md: update the setup-idea references (script behavior, the agent command, the merge rationale),
      including the Prerequisites Python-3 note (`scripts/setup-idea.sh`'s inspection-profile upsert), which the renamed
      merge script changes

## 4. Spec

- [x] 4.1 Update the `ide-config` delta (MODIFIED "The setup script configures a formatter-matched IDE") and sync the
      main spec at archive

## 5. Verify

- [x] 5.1 Run the reworked setup against a drifted `.idea` (blank a managed option, add an unrelated one) and confirm
      the managed option is restored while the unrelated content is preserved
- [x] 5.2 Verify the IDE-running path: the configuration is applied and the plugin install is skipped with a warning
      (the IDE-closed install path cannot be exercised while the IDE is running — confirm it by inspection)
- [x] 5.3 Confirm the `/setup-idea` command is a thin wrapper over the script whose wording matches the script's
      behavior
- [x] 5.4 `./gradlew spotlessCheck` and `openspec validate --all` pass
