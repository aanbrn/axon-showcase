#!/usr/bin/env bash
set -euo pipefail

# Installs the IntelliJ formatter plugins so Reformat Code matches the Spotless
# style enforced by `spotlessApply`: palantir-java-format (Java) and ktfmt
# (Gradle Kotlin DSL).
# Usage: ./scripts/setup-idea.sh [path/to/idea-launcher]
# The IDE must be closed: installPlugins silently no-ops when the IDE is running
# (the launcher can't start a second instance), so the script aborts if it detects
# one. Works for any standard IntelliJ install.

PLUGIN_IDS=("palantir-java-format" "com.facebook.ktfmt_idea_plugin")

is_idea_running() {
    if pgrep -f "Contents/MacOS/idea" >/dev/null 2>&1 || pgrep -f "idea64.exe" >/dev/null 2>&1; then
        return 0
    fi
    return 1
}

find_idea() {
    if [ -n "${1:-}" ] && [ -x "$1" ]; then
        echo "$1"
        return
    fi
    if [ -n "${IDEA_HOME:-}" ]; then
        for launcher in "$IDEA_HOME/bin/idea" "$IDEA_HOME/bin/idea.sh" "$IDEA_HOME/bin/idea64.exe"; do
            if [ -x "$launcher" ]; then
                echo "$launcher"
                return
            fi
        done
    fi
    local app script
    for app in "$HOME"/Applications/IntelliJ\ IDEA*.app/Contents/MacOS/idea \
        /Applications/IntelliJ\ IDEA*.app/Contents/MacOS/idea; do
        if [ -x "$app" ]; then
            echo "$app"
            return
        fi
    done
    for script in "$HOME"/idea*/bin/idea.sh /opt/idea*/bin/idea.sh; do
        if [ -x "$script" ]; then
            echo "$script"
            return
        fi
    done
    for launcher in idea idea.sh idea64.exe; do
        if command -v "$launcher" >/dev/null 2>&1; then
            command -v "$launcher"
            return
        fi
    done
    echo "IntelliJ IDEA launcher not found. Pass its path as the first argument, or set IDEA_HOME." >&2
    return 1
}

idea_bin="$(find_idea "${1:-}")"
if is_idea_running; then
    echo "IntelliJ IDEA is running — close it (File → Exit) before installing, then re-run." >&2
    exit 1
fi
for plugin_id in "${PLUGIN_IDS[@]}"; do
    echo "Installing $plugin_id via launcher $idea_bin"
    "$idea_bin" installPlugins "$plugin_id"
done
echo "Done. Restart the IDE; the plugins are auto-enabled for this project via .idea/."