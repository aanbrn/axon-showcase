#!/bin/sh
# Tests for `scripts/setup-idea.sh`, the wrapper around the IntelliJ settings helper. POSIX sh, stdlib tools only.
#
# Run with `sh scripts/test-setup-idea.sh`. Each case builds a throwaway repository root — the wrapper, the helper and
# the committed `config/idea/` templates copied into it — plus a fabricated IntelliJ configuration and a stub launcher
# that records its own invocation, so no case touches this checkout's `.idea/`, the real SDK table, or the IDE. A stub
# `pgrep` on `PATH` decides whether the IDE counts as running, which is what makes the plugin install reachable.

set -eu

REPO="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d "${TMPDIR:-/tmp}/setup-idea-test.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT INT TERM

failures=0

fail() {
    echo "FAIL: $*" >&2
    failures=$((failures + 1))
}

new_tree() {
    tree="$TMP/$1"
    mkdir -p "$tree/scripts" "$tree/config"
    cp "$REPO/scripts/setup-idea.sh" "$tree/scripts/"
    cp "$REPO/scripts/ensure-idea-settings.py" "$tree/scripts/"
    cp -R "$REPO/config/idea" "$tree/config/idea"
    mkdir -p "$tree/bin"
    printf '#!/bin/sh\nexit 0\n' >"$tree/launcher"
    printf '#!/bin/sh\nprintf "%%s\\n" "$*" >>"%s/launcher.log"\nexit 0\n' "$tree" >"$tree/stub-idea"
    chmod +x "$tree/launcher" "$tree/stub-idea"
    printf '#!/bin/sh\nexit 1\n' >"$tree/bin/pgrep"
    chmod +x "$tree/bin/pgrep"
    ln -s "$(command -v python3)" "$tree/bin/python3"
    mkdir -p "$tree/home"
    printf '%s\n' "$tree"
}

write_config() {
    tree="$1"
    interpreter="$(python3 -c 'import sys; print(sys.executable)')"
    mkdir -p "$tree/ideaconf/options"
    {
        printf '<application><jdk version="2">'
        printf '<name value="Stub Python" />'
        printf '<type value="Python SDK" />'
        printf '<version value="Python 3.12" />'
        printf '<homePath value="%s" />' "$interpreter"
        printf '</jdk></application>'
    } >"$tree/ideaconf/options/jdk.table.xml"
}

run_wrapper() {
    tree="$1"
    shift
    HOME="$tree/home" IDEA_HOME="" IDEA_SEARCH_ROOTS="" PATH="$tree/bin:/usr/bin:/bin:/usr/sbin:/sbin" \
        bash "$tree/scripts/setup-idea.sh" "$@" >"$tree/out.log" 2>&1
}

check_module_written() {
    tree="$1"
    if [ ! -f "$tree/.idea/python/scripts.iml" ]; then
        fail "the Python module was not written in $tree"
        return
    fi
    grep -q 'jdkName="Stub Python"' "$tree/.idea/python/scripts.iml" ||
        fail "the module does not reference the registered SDK name"
}

assert_clause() {
    needle="$(printf '%s' "$2" | tr '\n' ' ' | tr -s ' ')"
    case "$1" in
    *"$needle"*) return 0 ;;
    *)
        echo "the closing section omits: $needle" >&2
        return 1
        ;;
    esac
}

closing_section() {
    awk '/^=== Next step in IntelliJ ===$/{found=1} found' "$1/out.log" | tr '\n' ' ' | tr -s ' '
}

# Asserts the run's closing action section: one heading, separated from the output above and after every stage and
# plugin line, each substantive clause present, and the formatter rule last. Line wrapping is not asserted.
closing_section_ok() {
    tree="$1"
    headings="$(grep -c '^=== Next step in IntelliJ ===$' "$tree/out.log" || true)"
    if [ "$headings" != "1" ]; then
        echo "the closing heading appeared $headings time(s)" >&2
        return 1
    fi
    awk '/^=== Next step in IntelliJ ===$/{if (prev != "") exit 1} {prev = $0}' "$tree/out.log" || {
        echo "no blank line separates the closing section from the output above it" >&2
        return 1
    }
    heading_line="$(grep -n '^=== Next step in IntelliJ ===$' "$tree/out.log" | cut -d: -f1)"
    last_stage="$(
        grep -n -E \
            '^(Python: |Plugins installed\.$|IntelliJ IDEA |Plugin installation failed)' \
            "$tree/out.log" | tail -1 | cut -d: -f1 || true
    )"
    if [ -z "$last_stage" ]; then
        echo "no stage or plugin line precedes the closing section" >&2
        return 1
    fi
    if [ "$heading_line" -le "$last_stage" ]; then
        echo "the closing section precedes the stage and plugin reporting" >&2
        return 1
    fi
    section="$(closing_section "$tree")"
    assert_clause "$section" \
        "Open the project after the Python stage reported CONFIGURED: the Python analysis module comes with it." ||
        return 1
    assert_clause "$section" \
        "If this project was open during the run, close its window, run the setup again, then open it." || return 1
    assert_clause "$section" \
        "Closing the project window is enough; only the plugin install needs the whole IDE closed." || return 1
    assert_clause "$section" "Reloading an open project does not add the module." || return 1
    assert_clause "$section" "If IntelliJ offers to load the Gradle project when it opens, accept." || return 1
    assert_clause "$section" \
        "The merged formatter and inspection settings take effect after a reload or a restart." || return 1
    last="$(tail -1 "$tree/out.log")"
    if [ "$last" != "The merged formatter and inspection settings take effect after a reload or a restart." ]; then
        echo "the formatter rule is not the closing section's last line" >&2
        return 1
    fi
}

# Control mutations: each perturbs the surface `closing_section_ok` reads, so a passing assertion after one of them
# means the assertion does not discriminate.
remove_block() {
    python3 - "$1/scripts/setup-idea.sh" <<'PY'
import sys

path = sys.argv[1]
text = open(path, encoding="utf-8").read()
start = text.index('echo "=== Next step in IntelliJ ==="')
line_start = text.rindex("\n", 0, start) + 1
prev_end = line_start - 1
prev_start = text.rindex("\n", 0, prev_end) + 1
if text[prev_start:prev_end] == "echo":
    start = prev_start
end = text.index('echo "The merged formatter and inspection settings', start)
end = text.index("\n", end) + 1
open(path, "w", encoding="utf-8").write(text[:start] + text[end:])
PY
}

move_block() {
    python3 - "$1/scripts/setup-idea.sh" <<'PY'
import sys

path = sys.argv[1]
text = open(path, encoding="utf-8").read()
start = text.index('echo "=== Next step in IntelliJ ==="')
line_start = text.rindex("\n", 0, start) + 1
prev_end = line_start - 1
prev_start = text.rindex("\n", 0, prev_end) + 1
if text[prev_start:prev_end] == "echo":
    start = prev_start
end = text.index('echo "The merged formatter and inspection settings', start)
end = text.index("\n", end) + 1
block = text[start:end]
rest = text[:start] + text[end:]
anchor = rest.index('idea_bin="$(find_idea')
open(path, "w", encoding="utf-8").write(rest[:anchor] + block + rest[anchor:])
PY
}

remove_clause() {
    tree="$1"
    needle="$2"
    grep -v -F "$needle" "$tree/scripts/setup-idea.sh" >"$tree/scripts/setup-idea.sh.tmp"
    mv "$tree/scripts/setup-idea.sh.tmp" "$tree/scripts/setup-idea.sh"
    chmod +x "$tree/scripts/setup-idea.sh"
}

remove_open_clause() {
    remove_clause "$1" "Open the project after the Python stage reported CONFIGURED"
}

remove_recovery_clause() {
    remove_clause "$1" "close its window, run the setup again"
}

remove_window_clause() {
    remove_clause "$1" "only the plugin install needs the whole IDE closed"
}

remove_reload_clause() {
    remove_clause "$1" "Reloading an open project does not add the module"
}

remove_gradle_clause() {
    remove_clause "$1" "load the Gradle project when it opens"
}

remove_settings_clause() {
    remove_clause "$1" "take effect after a reload or a restart"
}

check_control() {
    name="$1"
    mutate="$2"
    expect="$3"
    tree="$(new_tree "control-$name")"
    write_config "$tree"
    "$mutate" "$tree"
    if run_wrapper "$tree" --idea-config-dir "$tree/ideaconf" "$tree/stub-idea"; then :; fi
    if output="$(closing_section_ok "$tree" 2>&1)"; then
        fail "the closing-section check passed when the block was $name"
    fi
    case "$output" in
    *"$expect"*) ;;
    *) fail "the $name control failed for an unexpected reason: $output" ;;
    esac
}

tree="$(new_tree configured)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir "$tree/ideaconf" "$tree/stub-idea"; then
    :
else
    fail "a configured run exited non-zero"
fi
grep -q "Python: CONFIGURED" "$tree/out.log" || fail "a configured run did not report CONFIGURED"
check_module_written "$tree"
grep -q "installPlugins" "$tree/launcher.log" 2>/dev/null || fail "the plugin install was not attempted"
grep -q "Python: CONFIGURED" "$tree/out.log" || fail "the Python stage's line is missing"
closing_section_ok "$tree" || fail "the closing section is incomplete or misplaced in a configured run"

tree="$(new_tree existing-module-list)"
write_config "$tree"
mkdir -p "$tree/.idea"
printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<project version="4">' \
    '  <component name="ProjectModuleManager">' \
    '    <modules>' \
    '      <module fileurl="file://$PROJECT_DIR$/existing.iml" filepath="$PROJECT_DIR$/existing.iml" />' \
    '    </modules>' \
    '  </component>' \
    '</project>' >"$tree/.idea/modules.xml"
if run_wrapper "$tree" --idea-config-dir "$tree/ideaconf" "$tree/stub-idea"; then
    :
else
    fail "a run against an existing module list exited non-zero"
fi
grep -q "Python: CONFIGURED" "$tree/out.log" || fail "a run against an existing module list was not reported"
closing_section_ok "$tree" || fail "the closing section changed when a module list already existed"

tree="$(new_tree helper-error)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir "$TMP/absent" "$tree/stub-idea"; then
    fail "a helper error exited zero"
fi
grep -q "Python: ERROR" "$tree/out.log" || fail "a helper error was not reported as ERROR"
grep -q "installPlugins" "$tree/launcher.log" 2>/dev/null ||
    fail "the plugin install was skipped after a helper error"
closing_section_ok "$tree" || fail "the closing section is incomplete or misplaced after a helper error"

tree="$(new_tree ide-running)"
write_config "$tree"
printf '#!/bin/sh\nexit 0\n' >"$tree/bin/pgrep"
chmod +x "$tree/bin/pgrep"
if run_wrapper "$tree" --idea-config-dir "$TMP/absent" "$tree/stub-idea"; then
    fail "a helper error with a running IDE exited zero"
fi
grep -q "Python: ERROR" "$tree/out.log" || fail "the helper error was lost when the IDE was running"
grep -q "skipped the plugin install" "$tree/out.log" || fail "a running IDE was not reported as a skip"
if [ -f "$tree/launcher.log" ]; then
    fail "the launcher was invoked while the IDE was running"
fi
closing_section_ok "$tree" || fail "the closing section is incomplete or misplaced when the install is skipped"

tree="$(new_tree no-launcher)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir "$TMP/absent"; then
    fail "a helper error without a launcher exited zero"
fi
grep -q "launcher not found" "$tree/out.log" || fail "a missing launcher was not reported"
grep -q "Python: ERROR" "$tree/out.log" || fail "the helper error was lost without a launcher"
closing_section_ok "$tree" || fail "the closing section is incomplete or misplaced without a launcher"

check_control block-removed remove_block "the closing heading appeared 0 time(s)"
check_control block-moved move_block "the closing section precedes the stage and plugin reporting"
check_control open-clause-removed remove_open_clause \
    "omits: Open the project after the Python stage reported CONFIGURED"
check_control recovery-clause-removed remove_recovery_clause "omits: If this project was open during the run"
check_control window-clause-removed remove_window_clause "omits: Closing the project window is enough"
check_control reload-clause-removed remove_reload_clause "omits: Reloading an open project does not add the module"
check_control gradle-clause-removed remove_gradle_clause "omits: If IntelliJ offers to load the Gradle project"
check_control settings-clause-removed remove_settings_clause "omits: The merged formatter and inspection settings"

tree="$(new_tree launcher-failure)"
write_config "$tree"
printf '#!/bin/sh\nexit 9\n' >"$tree/failing-idea"
chmod +x "$tree/failing-idea"
if run_wrapper "$tree" --idea-config-dir "$tree/ideaconf" "$tree/failing-idea"; then
    fail "a failing plugin install exited zero"
fi
grep -q "Plugin installation failed" "$tree/out.log" || fail "a failing plugin install was not reported"
if grep -q "Plugins installed." "$tree/out.log"; then
    fail "a failing plugin install still reported success"
fi
closing_section_ok "$tree" || fail "the closing section is incomplete or misplaced after a failed install"

tree="$(new_tree helper-error-and-launcher-failure)"
write_config "$tree"
printf '#!/bin/sh\nexit 9\n' >"$tree/failing-idea"
chmod +x "$tree/failing-idea"
if run_wrapper "$tree" --idea-config-dir "$TMP/absent" "$tree/failing-idea"; then
    fail "a helper error with a failing install exited zero"
fi
grep -q "Plugin installation failed" "$tree/out.log" || fail "the install failure was not reported"
if grep -q "was still applied" "$tree/out.log"; then
    fail "a failed Python stage was reported as an applied configuration"
fi
closing_section_ok "$tree" ||
    fail "the closing section is incomplete or misplaced after a stage error and a failed install"

tree="$(new_tree missing-flag-value)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir; then
    fail "an option without its value exited zero"
fi
grep -q "needs a path" "$tree/out.log" || fail "an option without its value was not reported"

tree="$(new_tree empty-flag-value)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir= "$tree/stub-idea"; then
    fail "an empty option value exited zero"
fi
grep -q "needs a path" "$tree/out.log" || fail "an empty option value was not reported"
if [ -f "$tree/launcher.log" ]; then
    fail "an empty option value fell back to a search and installed plugins"
fi

if [ "$failures" -ne 0 ]; then
    echo "$failures check(s) failed" >&2
    exit 1
fi
echo "setup-idea.sh: all cases passed"
