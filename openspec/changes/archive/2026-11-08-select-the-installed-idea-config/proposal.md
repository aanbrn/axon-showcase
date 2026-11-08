## Why

A contributor who upgrades IntelliJ keeps the previous version's configuration directory beside the new one. When both
register this tree's interpreter — IntelliJ copies SDK entries forward, so the upgraded configuration inherits them —
the setup's Python stage finds two matching configurations, refuses with `NEEDS_ACTION`, and reports `Tree NOT ready` on
every `new-worktree.sh` run until the stale directory is moved away or `--idea-config-dir` is passed. The remedy is host
cleanup for a state the tool can resolve for the default install and configuration layout: an installed bundle names the
product and version whose configuration is the live one.

## What Changes

- `scripts/ensure-idea-settings.py` resolves the installed IntelliJ IDEA bundles from an injectable root parameter
  (`/Applications` and `~/Applications` by default) and reads each bundle's product name and `Contents/Info.plist`'s
  `CFBundleShortVersionString` with `plistlib`; an unreadable or version-less plist is skipped.
- The setup resolves its launcher before the Python stage and passes the bundle it targets, so one run configures one
  IDE; the stage prefers that bundle's configuration, then the newest, after an explicit `--idea-config-dir` and before
  the unique-match rule, and reports the configuration and the pair it matched.
- A matched configuration that lacks the interpreter's SDK stops the stage with the registration owed there, and one
  that registers it twice is refused with the keep-one remedy — never a fallback to another configuration's SDK name,
  which the installed IDE could not resolve.
- A same-version tie across the two products with no launcher naming one, and a several-configuration layout no bundle
  resolves, still report the contributor's action — the states the tool cannot choose for the contributor.
- The version resolution is macOS-only, as the parked idea states; other platforms keep today's rule and the stage names
  the limitation.
- Automatic selection assumes the default configuration location: a custom `idea.config.path` is not detected, so that
  contributor passes `--idea-config-dir`, which the stop messages name.
- `scripts/test-ensure-idea-settings.py` gains the upgraded-IDE control and the stop, tie, multi-bundle, explicit-
  directory, unreadable-plist, launcher, and non-macOS cases, each with its own fixture bundle and configuration roots;
  `scripts/test-setup-idea.sh` gains the wrapper's own control for the launcher-first order and the single IDE target.
- The `showcase/quality/ide-config` capability gains a `MODIFIED` requirement: the configuration is identified by an
  installed bundle's product and version, with the choice reported.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/ide-config` — the requirement "An SDK the IDE cannot resolve is reported, not written" gains the
  bundle-based identification, its scenarios for the upgrade, the launcher's precedence, a missing configuration, the
  missing SDK (with and without another configuration matching), the identified configuration's duplicate entry, the
  launcher-less same-version tie, and the platform boundary, and its "several configurations qualify" refusal narrows to
  the case no bundle resolves exactly one of them.
