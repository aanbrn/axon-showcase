#!/usr/bin/env bash
set -euo pipefail

# Ensures the project's IntelliJ configuration matches the build formatter (Spotless): merges the
# committed settings — config/idea/*.xml and the test-tier naming inspection — into .idea/, and installs
# the palantir-java-format + ktfmt plugins. Safe to re-run: it reconciles a configuration that has drifted
# (never applied cleanly, or IntelliJ overwrote it). Applying the configuration needs neither the launcher nor a
# closed IDE — only the plugin install does, so a running (or missing) IDE skips just that step.
# Usage: ./scripts/setup-idea.sh [path/to/idea-launcher]
# Requires Python 3 (for the settings merge) and a Gradle-compatible JDK on PATH.

PLUGIN_IDS=("palantir-java-format" "com.facebook.ktfmt_idea_plugin")
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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
    return 1
}

echo "Applying the project IDE configuration (config/idea/ -> .idea/)..."
python3 "$SCRIPT_DIR/ensure-idea-settings.py"

idea_bin="$(find_idea "${1:-}" 2>/dev/null || true)"
if [ -z "$idea_bin" ]; then
    echo "IntelliJ IDEA launcher not found — skipped the plugin install; the configuration is applied."
    echo "Pass its path as the first argument, set IDEA_HOME, or install the plugins from the IDE."
elif is_idea_running; then
    echo "IntelliJ IDEA is running — skipped the plugin install; the configuration is applied."
    echo "Close it (File -> Exit) and re-run to install or refresh the palantir-java-format and ktfmt plugins."
else
    for plugin_id in "${PLUGIN_IDS[@]}"; do
        echo "Installing $plugin_id via launcher $idea_bin"
        "$idea_bin" installPlugins "$plugin_id"
    done
    echo "Plugins installed."
fi

echo "Done. In IntelliJ, apply the changes with File -> Reload All from Disk (or restart it) — IDEA does not"
echo "hot-reload .idea/inspectionProfiles/, so the settings take effect only after a reload or restart."
