# Tasks

The unit's record is `docs/units/select-the-installed-idea-config/unit.md`.

## 1. Implementation

- [x] 1.1 `scripts/ensure-idea-settings.py`: resolve the installed IDE candidates from app bundles through injectable
      bundle roots (default `/Applications` and `~/Applications`) and a platform parameter (default `sys.platform`) —
      each `IntelliJ IDEA*` bundle yields its product prefix (`IdeaIC` for a `CE` name, `IntelliJIdea` otherwise) and
      its `CFBundleShortVersionString` read with `plistlib`; an unreadable, version-less, or missing plist is skipped
      and the scan continues; pairs are ordered by version descending and by product name at equal versions (D1).
- [x] 1.2 `scripts/setup-idea.sh`: resolve the launcher before the Python stage and pass the bundle it targets
      (`--idea-bundle`), so one run configures one IDE; the Python stage reads that bundle from the launcher path or the
      app bundle (D1, D3 order 2).
- [x] 1.3 `scripts/test-setup-idea.sh`: the wrapper's own isolated control covers the new order — the launcher is
      resolved before the Python stage, `--idea-bundle` carries the resolved bundle, and the plugin installation names
      the same IDE the Python stage selected.
- [x] 1.4 `scripts/ensure-idea-settings.py`: select the launcher's pair first, then the newest pair whose configuration
      registers the resolved interpreter, after an explicit `--idea-config-dir` and before the unique-match rule; a pair
      that applies but lacks the SDK stops with the registration owed in that configuration and `--idea-config-dir`
      named for a custom path — whether or not any other configuration matches — and the SDK match itself stays
      unchanged (D2, D3).
- [x] 1.5 `scripts/ensure-idea-settings.py`: gate the bundle scan on the platform parameter; report the selected
      configuration and its pair, or the configuration at the default location and the registration it owes with
      `--idea-config-dir` named for a custom path, or that no installed bundle resolved; refuse a same-version tie
      across two products and a several-configuration layout with no bundle resolving it (D3, D4, D5).
- [x] 1.6 `scripts/test-ensure-idea-settings.py`: a known-bad and a known-good case per rule, each supplying its own
      fixture bundle roots, configuration roots, and platform:
  - the upgraded layout (two configurations registering the interpreter, one bundle naming one of them) selects that
    configuration and writes the module from its SDK table, and the line names the configuration, the product, the
    version, and the bundle;
  - the launcher the setup resolved selects its bundle's configuration ahead of a newer installed one, and the line
    names the configuration and bundle it used;
  - a unique match with no bundle installing resolves as today, and the line says no installed bundle resolved;
  - no bundle and several configurations keeps today's refusal, and the line says no installed bundle resolved;
  - no bundle and no matching configuration refuses, and the line says no installed bundle resolved;
  - an unreadable plist is skipped, the next bundle is used, and the run still configures;
  - a matched configuration that registers no SDK for the interpreter stops with `NEEDS_ACTION`, names that
    configuration, the registration owed, and `--idea-config-dir` for a custom path, and writes no module — the other
    configuration's SDK name is untouched;
  - the same stop holds when no other configuration registers the interpreter at all, so the search cannot be reached;
  - a matched configuration that is absent at the default location stops with `NEEDS_ACTION` reporting that no
    configuration exists there yet and naming `--idea-config-dir` for a custom path, and writes no module, with and
    without another configuration matching;
  - two products at one version both registering the interpreter is refused, and the line names both;
  - two bundles at different versions try the newest first, and the line names the version used;
  - an explicit `--idea-config-dir` still wins;
  - a forced non-darwin platform consults no bundle, refuses a several-configuration layout, and names the limitation.
- [x] 1.7 `openspec/changes/select-the-installed-idea-config/specs/showcase/quality/ide-config/spec.md`: the `MODIFIED`
      requirement states the bundle-based identification, the no-fallback stop, the same-version refusal, the launcher
      precedence, and the macOS boundary; its scenarios cover each.
- [x] 1.8 `docs/ideas.md`: remove the implemented entry. Sweep the live docs the change makes stale — `README.md`'s
      description of the several-configuration refusal, and the `setup-idea` skill's block if it states the same.

## 2. Verification

- [x] 2.1 `openspec validate --all` passes with the delta at its mirrored capability path.
- [x] 2.2 `python3 scripts/test-ensure-idea-settings.py` passes.
- [x] 2.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes.
- [x] 2.4 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit.
- [x] 2.5 Run the pre-write derivation pass and record the claims table in `design.md` after the last edit.

## 3. Review gate

- [x] 3.1 Run the proposal quick review and fix every finding until it reports no new observations.
- [x] 3.2 Run the implementation quick review and fix every finding until it reports no new observations.
- [x] 3.3 Derive the routing decision with `python3 scripts/review-routing.py`, record it and the pass's result in
      `docs/units/select-the-installed-idea-config/deep-review.md`, and run the pass it selects.
- [ ] 3.4 Ask the owner for the manual review pass and end the turn there.
- [x] 3.5 Write `docs/units/select-the-installed-idea-config/process-defects.md`, or state that none was observed.

## 4. Archive

- [x] 4.1 Refresh the `showcase/quality/ide-config` Purpose if the change falsifies it, and tick this task in the
      archived `tasks.md`.
- [ ] 4.2 Run `openspec archive select-the-installed-idea-config --yes` and stage both the change-dir move and the
      `openspec/specs/**` sync.
