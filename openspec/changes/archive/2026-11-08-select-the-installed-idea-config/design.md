## Context

`scripts/ensure-idea-settings.py`'s Python stage collects every IntelliJ configuration directory under the platform's
configuration roots (`IntelliJIdea*` and `IdeaIC*`), reads each one's SDK table, and matches an entry to the tree's
interpreter. When more than one configuration registers that interpreter it reports `NEEDS_ACTION` and names
`--idea-config-dir` as the remedy — the right refusal for a genuine ambiguity, the wrong one for an upgrade.

Verified state on the reporting machine (2026-11-08): `~/Library/Application Support/JetBrains/` holds eight matching
directories — `IdeaIC2024.3`, `IdeaIC2025.1`, `IdeaIC2025.2`, and `IntelliJIdea2024.2`, `2024.3`, `2025.3`, `2026.1`,
`2026.3` — while `~/Applications/IntelliJ IDEA.app/Contents/Info.plist` reports `CFBundleShortVersionString` `2026.3`,
exactly the newest configuration's suffix. The suffix is the IDE's `major.minor`, and the bundle's product name carries
the configuration prefix (`IntelliJ IDEA.app` → `IntelliJIdea`, `IntelliJ IDEA CE.app` → `IdeaIC`), so an installed
bundle names the live configuration — provided the product is read too, since two products at one version share a
suffix.

The parked idea (`docs/ideas.md`, 2026-11-08) records the failure, the remedy it wants, and the debt it owes: a control
for the upgraded-IDE layout and a `MODIFIED` delta on this capability. This design adopts it.

It refines, rather than reverses, the archived decision that the configuration is "identified, never guessed from
ordering" (`openspec/changes/archive/2026-10-30-reproduce-python-ide-setup/design.md`, D3). That decision rejected
picking the newest configuration _directory_ because presence is not installation; this one orders configurations by
**installed bundles**, and refuses when that evidence does not single one out. Its recorded limitation — a contributor
with a custom `idea.config.path` — bounds this design too: automatic selection assumes the default configuration
location and cannot detect a custom one, so that contributor stays on the explicit `--idea-config-dir`.

**Goals:** an upgraded-IDE layout resolves without host cleanup; the choice is reported; a genuinely ambiguous layout
still refuses.

**Non-goals:** resolving an IDE version on Linux or Windows (the parked idea scopes the resolution to the macOS bundle);
changing the interpreter-matching rule, the module write, or the settings merge; removing the `--idea-config-dir` escape
hatch.

## Decisions

### D1 — Resolve the installed IDE(s) from app bundles, through an injectable root

- The bundle roots are a parameter, defaulting to `/Applications` and `~/Applications`, as the configuration `roots`
  already are; every test supplies its own fixture roots so no case reads the host's install. The platform is a
  parameter too, defaulting to `sys.platform`, so both branches are exercised on any runner.
- The setup resolves its launcher **before** the Python stage and passes it to the script (`--idea-bundle`), so one run
  targets one IDE: an explicit launcher argument or `IDEA_HOME` is what the plugin installation uses, and the Python
  stage prefers that bundle's pair over the newest installed one. `--idea-config-dir` still overrides the configuration
  search outright.
- A `*.app` whose basename begins `IntelliJ IDEA` yields a candidate pair: the product prefix — `IdeaIC` for a name
  carrying `CE`, `IntelliJIdea` otherwise — and the version read from `Contents/Info.plist`'s
  `CFBundleShortVersionString` with `plistlib`, which parses both the XML and the binary form.
- A bundle whose plist is missing, malformed, or carries no short version is skipped and the scan continues; if no
  candidate resolves, the stage falls back as D3 states and reports that no installed version resolved.
- A `--idea-bundle` value outside an app bundle — an `IDEA_HOME` or `PATH` launcher — is skipped the same way, so the
  installed-bundle scan still resolves one, and the search decides when none does.
- Pairs are ordered by version descending, and pairs at the same version are ordered by product name for determinism, so
  a run's outcome does not depend on directory order.

Alternatives rejected:

- **Pick the newest configuration directory without reading a bundle.** A stale directory can be the newest (a beta
  since removed, a version uninstalled), and presence is not installation.
- **Compare the bundle's full version (`2026.3.1`) with the directory suffix.** The suffixes are `major.minor`, so a
  patch release would never match.

### D2 — The installed pair narrows the configuration candidates; the SDK match is unchanged

The SDK match runs exactly as today — the entry must be the interpreter's own install, by the capability's rule. What
the pair adds is which configurations are candidates first:

| The pair's configuration                     | Action                                                                                                                               |
| -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| readable and registers the interpreter's SDK | select it, and write the module from that table                                                                                      |
| readable, but registers no matching SDK      | `NEEDS_ACTION` naming that configuration and the SDK to register in it, plus `--idea-config-dir` for a custom path — no fallback     |
| readable, registers the interpreter twice    | `NEEDS_ACTION` naming the interpreter, the configuration, and the keep-one remedy — the SDK ambiguity the capability already refuses |
| absent at the default location               | `NEEDS_ACTION` naming that no configuration exists there yet, plus `--idea-config-dir` for a custom path                             |

**No fallback to another configuration.** The module writes the SDK's _name_ (`jdkName`), which resolves only in the
configuration the IDE actually reads; a name taken from a configuration the installed IDE does not read would be an
unresolvable reference, and reporting it would not make `CONFIGURED` true. The stage stops and names the registration
the contributor owes in the identified configuration — the same diagnosis the capability already gives for a missing
SDK, aimed at the right table.

Alternatives rejected:

- **Fall back to a configuration that has the SDK.** It writes a name the installed IDE cannot resolve (above).
- **Select the version's configuration unconditionally and report the gap.** Same defect, with a `CONFIGURED` line that
  claims a resolvable reference it does not have.

### D3 — Precedence: explicit, then launcher, then newest, then unique, then refused

| Order | Condition                                                         | Action                                                            |
| ----- | ----------------------------------------------------------------- | ----------------------------------------------------------------- |
| 1     | `--idea-config-dir` is given                                      | consult only that directory, as today                             |
| 2     | the launcher the setup resolved names a pair registering the SDK  | use it and report the configuration, the product, and the version |
| 3     | no launcher pair applies; a pair, newest first, registers the SDK | use it and report the same                                        |
| 4     | a pair applies but its configuration is absent or lacks the SDK   | `NEEDS_ACTION` per D2, naming that configuration — not the search |
| 5     | no bundle pair applies, exactly one configuration matches         | use it, as today                                                  |
| 6     | no bundle pair applies, several configurations match              | refuse and name them, as today                                    |
| 7     | no bundle pair applies, no configuration matches                  | refuse as today                                                   |

Order 4 is a stop, not a skip: a pair that resolves the IDE but not the SDK is a state the contributor fixes in that
configuration, and searching elsewhere would write a name it cannot resolve (D2).

**The same-version tie is refused when no launcher resolved and both register the interpreter.** When two products at
one version are installed — Ultimate and Community side by side — and both register the interpreter, with no launcher
naming one, the tool cannot know which IDE the contributor uses, so the refusal names both. When exactly one registers
it, that one is the answer (order 3); when neither does, the missing-SDK stop applies. A launcher the setup resolved
settles the tie: invoking that IDE is the contributor's choice, and order 2 follows it. The idea's remedy is the
upgrade, not a product guess.

**Automatic selection assumes the default configuration location.** The pair comes from the bundle's basename and
`Info.plist` alone, so it does not establish whether the IDE has been pointed elsewhere: a contributor with a custom
`idea.config.path` and a default-named bundle still yields a pair, and the stage **cannot detect** that customization.
Such a contributor supplies `--idea-config-dir`, which overrides the search. The same bound covers a renamed bundle
whose basename does not begin `IntelliJ IDEA` and an EAP layout the launcher search does not find: those leave no pair
and take orders 5–7, exactly as today.

### D4 — The resolution is macOS-only, and the stage says so

The bundle scan is gated on a platform parameter that defaults to `sys.platform`, so the suite forces both branches on
any runner and asserts the non-macOS one consults no bundle. On another platform a several-configuration layout keeps
today's refusal, and the report names the platform limitation rather than implying a choice exists. The delta states the
boundary, so a future platform's resolution is its own change.

### D5 — Every resolution and every stop is reported

The stage's line names the selected configuration and the pair it matched — the product prefix, the version, and the
bundle path — or, when it stopped, the configuration at the default location and the registration it owes, with
`--idea-config-dir` named as the route for a custom configuration path. When no bundle resolves and the search decides
(orders 5–7), the line says that no installed bundle resolved. A silent outcome would hide the half-migrated state the
idea exists to surface.

## Risks

- **A bundle the scan does not find (a non-standard install location).** The stage falls back to the unique-match rule
  and then the refusal, no worse than today, and the report says no installed version resolved.
- **Two bundles at different versions.** The newest is tried first and reported; a contributor who wants the older one
  selects it explicitly.
- **A version-matched configuration whose SDK table is stale.** D2 stops with the registration owed in that
  configuration and writes no new module, so the setup adds no unresolvable reference while the contributor fixes the
  one the report names; a module an earlier run wrote is left as it is.
- **An unreadable plist.** Skipped per D1; the fallback and its report keep the stage's contract.

## Amended clauses

| Requirement                                 | Clause before                                         | Clause after                                                                                                                         | Why                                           |
| ------------------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------- |
| `ide-config` SDK identification             | identified — explicitly selected, or uniquely matched | explicitly selected, matched to an installed IntelliJ IDEA bundle's product and version, or uniquely matched; the choice is reported | An upgrade leaves two matching configurations |
| `ide-config` several-configurations refusal | several configurations qualify and none was selected  | several configurations qualify, none was selected, and no installed bundle resolves exactly one of them                              | The version resolves what the refusal covers  |

## Claims table

Base revision: `origin/main` `0727fb8b72905e3536481c98431fc6327e3d102d` (`git merge-base HEAD origin/main`).

| Claim                                                                                             | Source                                                                                                                                                         | Reproduces                                                                                                                                                    |
| ------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The stage collects every `IntelliJIdea*`/`IdeaIC*` directory                                      | `scripts/ensure-idea-settings.py`'s `config_candidates` and `IDE_CONFIG_PREFIXES`                                                                              | the prefix tuple and the directory loop                                                                                                                       |
| Several matching entries report `NEEDS_ACTION`                                                    | the same file's `python_stage`, its `len(matches) > 1` branch                                                                                                  | the branch's message and its explicit `--idea-config-dir` remedy                                                                                              |
| The bundle's short version is readable with `plistlib`                                            | `/usr/libexec/PlistBuddy -c "Print :CFBundleShortVersionString" "$HOME/Applications/IntelliJ IDEA.app/Contents/Info.plist"` and `python3 -c "import plistlib"` | `2026.3`; the module imports                                                                                                                                  |
| The configuration suffix is the IDE's `major.minor`, and the prefix follows the product           | `ls ~/Library/Application Support/JetBrains` and the bundle's name                                                                                             | eight directories, `IdeaIC2024.3`–`IdeaIC2025.2` and `IntelliJIdea2024.2`–`2026.3`; the bundle is `IntelliJ IDEA.app`                                         |
| The duplicated SDK registration is the recorded incident                                          | `docs/ideas.md` 2026-11-08 entry (the owner-reported upgrade state), and this host's SDK tables                                                                | the duplicate `snapscreen-infra-as-code` registration sits in `IntelliJIdea2024.2`/`2024.3`; the live `IntelliJIdea2026.3` holds this tree's interpreter once |
| The refusal is specified, so the delta modifies rather than adds                                  | `openspec/specs/showcase/quality/ide-config/spec.md`, "An SDK the IDE cannot resolve…"                                                                         | the scenario "A missing or ambiguous match needs the contributor's action"                                                                                    |
| The setup resolves its launcher from the argument or `IDEA_HOME`, and runs the Python stage first | `scripts/setup-idea.sh`'s `find_idea` and its invocation order                                                                                                 | `find_idea` honours the argument then `IDEA_HOME`; the Python stage's call precedes `idea_bin`'s                                                              |
| The module writes the SDK's name, not its interpreter path                                        | `scripts/ensure-idea-settings.py`'s `ensure_module_file`                                                                                                       | the `jdkName` attribute is set from the selected entry's name                                                                                                 |
