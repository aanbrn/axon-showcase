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

tree="$(new_tree helper-error)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir "$TMP/absent" "$tree/stub-idea"; then
    fail "a helper error exited zero"
fi
grep -q "Python: ERROR" "$tree/out.log" || fail "a helper error was not reported as ERROR"
grep -q "installPlugins" "$tree/launcher.log" 2>/dev/null ||
    fail "the plugin install was skipped after a helper error"

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

tree="$(new_tree no-launcher)"
write_config "$tree"
if run_wrapper "$tree" --idea-config-dir "$TMP/absent"; then
    fail "a helper error without a launcher exited zero"
fi
grep -q "launcher not found" "$tree/out.log" || fail "a missing launcher was not reported"
grep -q "Python: ERROR" "$tree/out.log" || fail "the helper error was lost without a launcher"

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
