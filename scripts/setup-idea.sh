#!/usr/bin/env bash
set -euo pipefail

# Ensures the project's IntelliJ configuration matches the build formatters and analyzes the repository's Python
# scripts: merges the committed settings — config/idea/*.xml (incl. the web module's Prettier) and the test-tier naming
# inspection — into .idea/, reconciles the Python analysis module for this working tree, and installs the
# palantir-java-format + ktfmt plugins. Safe to re-run: it reconciles a configuration that has drifted (never applied
# cleanly, or IntelliJ overwrote it). Applying the configuration needs neither the launcher nor a closed IDE — only the
# plugin install does, and a skipped install is a legitimate outcome rather than a failure.
# Usage: ./scripts/setup-idea.sh [--idea-config-dir <path>] [path/to/idea-launcher]
# IDEA_SEARCH_ROOTS (colon-separated) overrides the directories searched for an installed IDEA, so a non-standard
# install location can be pointed at and an isolated run can disable the search.
# Requires Python 3.12+ (the build's declared minimum) and a Gradle-compatible JDK on PATH.

PLUGIN_IDS=("palantir-java-format" "com.facebook.ktfmt_idea_plugin")
search_roots="$(printf '%s' "${IDEA_SEARCH_ROOTS-$HOME/Applications:/Applications:$HOME:/opt}" | tr ':' ' ')"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

idea_config_dir=""
idea_config_dir_given=false
launcher_argument=""

while [ "$#" -gt 0 ]; do
    case "$1" in
        --idea-config-dir)
            if [ "$#" -lt 2 ]; then
                echo "error: --idea-config-dir needs a path" >&2
                exit 2
            fi
            idea_config_dir="$2"
            idea_config_dir_given=true
            shift 2
            ;;
        --idea-config-dir=*)
            idea_config_dir="${1#*=}"
            idea_config_dir_given=true
            shift
            ;;
        -*)
            echo "error: unknown option $1" >&2
            exit 2
            ;;
        *)
            launcher_argument="$1"
            shift
            ;;
    esac
done

if [ "$idea_config_dir_given" = true ] && [ -z "$idea_config_dir" ]; then
    echo "error: --idea-config-dir needs a path" >&2
    exit 2
fi

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
    local root app script
    for root in $search_roots; do
        for app in "$root"/IntelliJ\ IDEA*.app/Contents/MacOS/idea; do
            if [ -x "$app" ]; then
                echo "$app"
                return
            fi
        done
        for script in "$root"/idea*/bin/idea.sh; do
            if [ -x "$script" ]; then
                echo "$script"
                return
            fi
        done
    done
    for launcher in idea idea.sh idea64.exe; do
        if command -v "$launcher" >/dev/null 2>&1; then
            command -v "$launcher"
            return
        fi
    done
    return 1
}

echo "Applying the project IDE configuration (config/idea/ -> .idea/, plus the Python analysis module)..."

status=0
if [ "$idea_config_dir_given" = true ]; then
    python3 "$SCRIPT_DIR/ensure-idea-settings.py" --idea-config-dir "$idea_config_dir" || status=1
else
    python3 "$SCRIPT_DIR/ensure-idea-settings.py" || status=1
fi

idea_bin="$(find_idea "${launcher_argument:-}" 2>/dev/null || true)"
if [ -z "$idea_bin" ]; then
    echo "IntelliJ IDEA launcher not found — skipped the plugin install."
    echo "Pass its path as the first argument, set IDEA_HOME, or install the plugins from the IDE."
elif is_idea_running; then
    echo "IntelliJ IDEA is running — skipped the plugin install."
    echo "Close it (File -> Exit) and re-run to install or refresh the palantir-java-format and ktfmt plugins."
else
    install_failed=false
    for plugin_id in "${PLUGIN_IDS[@]}"; do
        echo "Installing $plugin_id via launcher $idea_bin"
        if "$idea_bin" installPlugins "$plugin_id"; then
            :
        else
            echo "Plugin installation failed for $plugin_id" >&2
            install_failed=true
            status=1
        fi
    done
    if [ "$install_failed" = true ]; then
        echo "Plugin installation failed; see the stage lines above for what was applied."
    else
        echo "Plugins installed."
    fi
fi

echo
echo "=== Next step in IntelliJ ==="
echo
echo "Open the project after the Python stage reported CONFIGURED: the Python analysis module comes with it."
echo "If this project was open during the run, close its window, run the setup again, then open it."
echo "Closing the project window is enough; only the plugin install needs the whole IDE closed."
echo "Reloading an open project does not add the module."
echo "If IntelliJ offers to load the Gradle project when it opens, accept."
echo "The merged formatter and inspection settings take effect after a reload or a restart."
exit "$status"
