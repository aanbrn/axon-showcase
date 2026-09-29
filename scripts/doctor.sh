#!/bin/sh
# shellcheck shell=sh

# Diagnoses whether this machine can run and verify the project: probes the toolchain the build, tests, and deployment
# depend on and prints one line per prerequisite with its status and, where it is not satisfied, how to resolve it.
# Strictly POSIX sh — no bash, no Java, no Gradle, no network — so it can report the build's own prerequisites, Java
# included, on any Unix and on a machine where nothing else runs yet. Diagnose only: it never installs or changes
# anything.
#
# Usage: ./scripts/doctor.sh [--all]
#   With no flag, exits non-zero when a required prerequisite is unsatisfied. With --all, exits non-zero when any
#   prerequisite (required or optional) is unsatisfied.
#
# Install hints are per-platform: the OS is detected (macOS or Debian/Ubuntu Linux) and the matching hint printed. On an
# unrecognized platform the tool is named without a hint rather than prescribed a command that would not run. The hint
# set matches the README's Prerequisites table, which scripts/test-doctor.sh keeps in step.

EXIT_OK=0
EXIT_UNSATISFIED=1

status_ok=satisfied
status_missing=missing
status_old=too-old
status_unknown=unknown

fail_required=0
fail_any=0

usage() {
    echo "Usage: $0 [--all]" >&2
    exit 2
}

check_all=0
for arg in "$@"; do
    case "$arg" in
    --all) check_all=1 ;;
    -h | --help) usage ;;
    *) usage ;;
    esac
done

# --- platform -------------------------------------------------------------------------------------------------------
# `platform` is one of: macos, debian, other.
detect_platform() {
    case "$(uname -s 2>/dev/null)" in
    Darwin) echo macos ;;
    Linux)
        if [ -r /etc/os-release ] && grep -qiE '^(ID|ID_LIKE)=.*(debian|ubuntu)' /etc/os-release; then
            echo debian
        else
            echo other
        fi
        ;;
    *) echo other ;;
    esac
}

platform=$(detect_platform)

# Prints the install hint for a tool on the detected platform, or an empty line when no hint is known. $1 is the tool's
# charter key; the macOS column matches the README's, the Debian/Ubuntu column does likewise.
install_hint() {
    tool=$1
    case "$platform:$tool" in
    macos:java) echo "brew install --cask temurin@21, or SDKMAN" ;;
    macos:docker) echo "Docker Desktop, or brew install colima docker" ;;
    macos:compose) echo "Docker Desktop, or brew install docker-compose" ;;
    macos:actionlint) echo "brew install actionlint" ;;
    macos:pack) echo "brew install buildpacks/tap/pack" ;;
    macos:helm) echo "brew install helm" ;;
    macos:kubectl) echo "kind, minikube, or colima with k3s" ;;
    macos:snyk) echo "brew install snyk/tap/snyk" ;;
    macos:python3) echo "ships with macOS Command Line Tools" ;;
    macos:gh) echo "brew install gh" ;;
    macos:opencode) echo "brew install anomalyco/tap/opencode-v2, or https://opencode.ai/v2" ;;
    debian:java) echo "sudo apt install openjdk-21-jdk, or SDKMAN" ;;
    debian:docker) echo "sudo apt install docker.io docker-compose-v2" ;;
    debian:compose) echo "sudo apt install docker-compose-v2" ;;
    debian:actionlint) echo "go install github.com/rhysd/actionlint/cmd/actionlint@latest, or a release binary" ;;
    debian:pack) echo "a release binary from buildpacks/pack" ;;
    debian:helm) echo "sudo snap install helm --classic, or a release binary" ;;
    debian:kubectl) echo "kind or minikube" ;;
    debian:snyk) echo "npm install -g snyk, or curl https://static.snyk.io/cli/latest/snyk-linux -o snyk" ;;
    debian:python3) echo "sudo apt install python3" ;;
    debian:gh) echo "sudo apt install gh, or a release binary from cli/cli" ;;
    debian:opencode) echo "curl -fsSL https://opencode.ai/install | bash, or https://opencode.ai/v2" ;;
    *) echo "" ;;
    esac
}

# --- version probes -------------------------------------------------------------------------------------------------
# Prints the first dotted-version token of an executable's output; returns non-zero when absent or unreadable. The
# arguments string is split on spaces so a multi-token invocation (`helm version --short`) works.
raw_version() {
    bin=$1
    vargs=$2
    command -v "$bin" >/dev/null 2>&1 || return 1
    # shellcheck disable=SC2086
    "$bin" $vargs 2>&1 | tr -d '\r' | grep -oE '[0-9]+(\.[0-9]+)+' | head -n 1
}

# --- reporting -----------------------------------------------------------------------------------------------------
# Emits one report line and updates the aggregate exit flags. $1 name, $2 class, $3 status, $4 detail, $5 remedy.
report() {
    name=$1
    class=$2
    st=$3
    detail=$4
    remedy=$5

    case "$st" in
    "$status_ok") printf '  %-14s %-9s %s\n' "$name" "$status_ok" "$detail" ;;
    "$status_missing") printf '  %-14s %-9s %s\n' "$name" "$status_missing" "$remedy" ;;
    "$status_old") printf '  %-14s %-9s %s — %s\n' "$name" "$status_old" "$detail" "$remedy" ;;
    *) printf '  %-14s %-9s %s\n' "$name" "$status_unknown" "$detail" ;;
    esac

    if [ "$st" != "$status_ok" ] && [ "$st" != "$status_unknown" ]; then
        fail_any=1
        [ "$class" = "required" ] && fail_required=1
    fi
    # An unknown version on a required, floored tool is a failure: a missing reading must not read as satisfied.
    if [ "$st" = "$status_unknown" ] && [ "$class" = "required" ]; then
        fail_any=1
        fail_required=1
    fi
}

# Checks a tool with an optional version floor.
# $1 name, $2 class, $3 binary, $4 version-args, $5 minimum major (empty = presence only)
check_tool() {
    name=$1
    class=$2
    bin=$3
    vargs=$4
    min_major=$5
    remedy=$(install_hint "$name")

    if ! command -v "$bin" >/dev/null 2>&1; then
        # No known hint for this platform: name the tool plainly rather than prescribing a command that would not run.
        [ -n "$remedy" ] || remedy="not installed (install '$bin' for your platform)"
        report "$name" "$class" "$status_missing" "" "$remedy"
        return
    fi

    version=$(raw_version "$bin" "$vargs") || version=""
    if [ -z "$min_major" ]; then
        if [ -n "$version" ]; then
            report "$name" "$class" "$status_ok" "$version" ""
        else
            report "$name" "$class" "$status_ok" "installed" ""
        fi
        return
    fi

    major=${version%%.*}
    case "$major" in
    '' | *[!0-9]*) major="" ;;
    esac
    if [ -z "$version" ] || [ -z "$major" ]; then
        report "$name" "$class" "$status_unknown" "version unreadable" ""
        return
    fi
    if [ "$major" -ge "$min_major" ]; then
        report "$name" "$class" "$status_ok" "$version" ""
    else
        [ -n "$remedy" ] || remedy="upgrade $bin to >= $min_major"
        report "$name" "$class" "$status_old" "$version (needs >= $min_major)" "$remedy"
    fi
}

echo "Toolchain doctor — checking what this machine needs to build, test, and deploy axon-showcase"
echo "(platform: $platform)"
echo

echo "Required:"
check_tool "java" required java "--version" 21
check_tool "docker" required docker "--version" ""
if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    compose_version=$(raw_version docker "compose version") || compose_version=""
    compose_major=${compose_version%%.*}
    case "$compose_major" in
    '' | *[!0-9]*) compose_major=0 ;;
    esac
    if [ "$compose_major" -ge 2 ]; then
        report "compose" required "$status_ok" "$compose_version" ""
    else
        compose_hint=$(install_hint compose)
        [ -n "$compose_hint" ] || compose_hint="not up to date (install a Compose v2 for your platform)"
        report "compose" required "$status_old" "$compose_version (needs >= 2)" "$compose_hint"
    fi
else
    compose_hint=$(install_hint compose)
    [ -n "$compose_hint" ] || compose_hint="not installed (install a Compose v2 for your platform)"
    report "compose" required "$status_missing" "" "$compose_hint"
fi

echo
echo "Optional:"
check_tool "actionlint" optional actionlint "--version" ""
check_tool "pack" optional pack "--version" ""
check_tool "helm" optional helm "version --short" 4
check_tool "kubectl" optional kubectl "version --client" ""
check_tool "snyk" optional snyk "--version" ""
check_tool "python3" optional python3 "--version" 3
check_tool "gh" optional gh "--version" ""
check_tool "opencode" optional opencode "--version" ""

echo
echo "Repo state:"
# Anchor the hooks path to the repo root: install-git-hooks.sh sets core.hooksPath to the *relative*
# `scripts/git-hooks`, which only resolves against the current working directory — so a doctor run from a subdirectory
# would misreport.
repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || repo_root=""
hooks_path=$(git config --get core.hooksPath 2>/dev/null) || hooks_path=""
hooks_resolved=""
case "$hooks_path" in
/*) hooks_resolved=$hooks_path ;;
'') hooks_resolved="" ;;
*) hooks_resolved="${repo_root:-.}/$hooks_path" ;;
esac
if [ -n "$hooks_resolved" ] && [ -x "$hooks_resolved/pre-commit" ]; then
    report "git-hooks" required "$status_ok" "$hooks_path" ""
else
    report "git-hooks" required "$status_missing" "" "./scripts/install-git-hooks.sh"
fi

if command -v docker >/dev/null 2>&1; then
    if docker info >/dev/null 2>&1; then
        report "docker-daemon" required "$status_ok" "reachable" ""
    else
        report "docker-daemon" required "$status_missing" "" "start the Docker daemon (Docker Desktop, or colima start)"
    fi
else
    report "docker-daemon" required "$status_missing" "" "install Docker first"
fi

echo
if [ "$fail_required" -eq 0 ]; then
    echo "Result: every required prerequisite is satisfied."
else
    echo "Result: at least one required prerequisite is unsatisfied (see above)."
fi
if [ "$fail_any" -ne 0 ] && [ "$fail_required" -eq 0 ]; then
    echo "Some optional prerequisites are missing — the build and tests do not need them."
fi

if [ "$check_all" -eq 1 ]; then
    [ "$fail_any" -eq 0 ] && exit "$EXIT_OK" || exit "$EXIT_UNSATISFIED"
fi
[ "$fail_required" -eq 0 ] && exit "$EXIT_OK" || exit "$EXIT_UNSATISFIED"
