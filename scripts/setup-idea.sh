#!/usr/bin/env bash
set -euo pipefail

# Installs the palantir-java-format IntelliJ plugin into a local IntelliJ IDEA, so
# Reformat Code matches the Spotless/palantir style enforced by `spotlessApply`.
# Usage: ./scripts/setup-idea.sh [path/to/idea-launcher]
# Run with the IDE closed, then restart it. Works for any standard IntelliJ install.

PLUGIN_ID="${PLUGIN_ID:-palantir-java-format}"

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
    for launcher in idea idea.sh idea64.exe; do
        if command -v "$launcher" >/dev/null 2>&1; then
            command -v "$launcher"
            return
        fi
    done
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
    echo "IntelliJ IDEA launcher not found. Pass its path as the first argument, or set IDEA_HOME." >&2
    return 1
}

idea_bin="$(find_idea "${1:-}")"
echo "Installing $PLUGIN_ID via launcher $idea_bin"
"$idea_bin" installPlugins "$PLUGIN_ID"
echo "Done. Restart the IDE; the plugin is auto-enabled for this project via .idea/palantir-java-format.xml."