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

# --- the probe declaration ------------------------------------------------------------------------------------------
# Every probe is DECLARED here, one row per probe, and this is the single owner of the probe set: the diagnostic drives
# each probe by key from its row, and scripts/test-doctor.sh reads the table rather than inferring the set from this
# file's source text — so a probe cannot exist without a row, and none can be invisible to that check.
#
# Columns, pipe-separated:  key | class | kind | readme | binary | version-args | minimum-major
#   class  — required | optional: what an unsatisfied probe does to the default run's exit code
#   kind   — tool | state: a probe's documentation and install-hint needs (a tool is documented and hinted, a state
#            step is neither) — never how it is implemented, so `compose` is a tool with no binary of its own
#   readme — yes | no: whether the README's Prerequisites table documents the probe (`no` for `gh`, which the README
#            covers in prose, and for the state steps)
# The last three columns are empty for probes not probed through a binary.
probe_table='
java|required|tool|yes|java|--version|21
docker|required|tool|yes|docker|--version|
compose|required|tool|yes|||
actionlint|optional|tool|yes|actionlint|--version|
pack|optional|tool|yes|pack|--version|
helm|optional|tool|yes|helm|version --short|4
kubectl|optional|tool|yes|kubectl|version --client|
snyk|optional|tool|yes|snyk|--version|
python3|optional|tool|yes|python3|--version|3
gh|optional|tool|no|gh|--version|
opencode|optional|tool|yes|opencode|--version|
git-hooks|optional|state|no|||
docker-daemon|required|state|no|||
'

# Fails when a declared row is malformed, so a typo cannot make a probe invisible — a blank or duplicated key, a wrong
# column count, or a value outside what a column allows would otherwise drop the row from what a person sees and what a
# state step or an implementation is run. Shared with scripts/test-doctor.sh, which invokes this function rather than
# restating the rules (two hand-written copies would drift).
validate_probe_table() {
    printf '%s\n' "$probe_table" | awk -F'|' '
        $0 == "" { next }
        NF < 7 { printf "probe row has %d of 7 columns: [%s]\n", NF, $0; bad = 1; next }
        NF > 7 { printf "probe row has %d of 7 columns (too many): [%s]\n", NF, $0; bad = 1; next }
        $1 == "" { printf "probe row has a blank key: [%s]\n", $0; bad = 1; next }
        # The key must be matchable as a whole word anywhere an assertion interpolates it (the check greps for it, and
        # the doctor prints it), so it is constrained to a plain identifier: a whitespace or glob/ERE metacharacter key
        # would make those matches vacuous or wrong rather than naming the probe.
        $1 !~ /^[a-z0-9][a-z0-9-]*$/ {
            printf "probe key [%s] is not a plain identifier (expected [a-z0-9][a-z0-9-]*)\n", $1; bad = 1
        }
        seen[$1]++ == 1 { printf "probe key %s is declared more than once\n", $1; bad = 1 }
        $2 != "required" && $2 != "optional" {
            printf "probe %s has class %s, expected required|optional\n", $1, $2; bad = 1
        }
        $3 != "tool" && $3 != "state" { printf "probe %s has kind %s, expected tool|state\n", $1, $3; bad = 1 }
        $4 != "yes" && $4 != "no" { printf "probe %s has readme %s, expected yes|no\n", $1, $4; bad = 1 }
        $3 == "state" && ($5 != "" || $6 != "" || $7 != "") {
            printf "probe %s is a state step but carries tool facts\n", $1; bad = 1
        }
        # A floor must be a number when present: an unset floor means "presence only", and a non-numeric one would make
        # the version comparison error rather than compare.
        $7 != "" && $7 !~ /^[0-9]+$/ {
            printf "probe %s has minimum-major %s, expected a number or empty\n", $1, $7; bad = 1
        }
        # A binary implies the probe is version-checked; a tool row with a binary but no version arguments would run the
        # binary with no arguments, which is not a version read.
        $5 != "" && $6 == "" {
            printf "probe %s names binary %s but no version arguments\n", $1, $5; bad = 1
        }
        END { exit bad }
    ' || {
        echo "the doctor's probe_table is malformed (see above)" >&2
        exit 1
    }
}

# Every declared row, one per line.
probe_rows() {
    printf '%s\n' "$probe_table" | awk -F'|' 'NF == 7 { print }'
}

# Prints a probe's row for a key, failing when the key is not declared.
probe_row() {
    printf '%s\n' "$probe_table" |
        awk -F'|' -v key="$1" 'NF == 7 && $1 == key { print; found = 1 } END { if (!found) exit 1 }'
}

# Prints one column of a probe's row ($1 key, $2 column). Propagates a missing key rather than piping into awk: a
# pipeline would report awk's status, silently yielding an empty name and class for a typo.
probe_field() {
    row=$(probe_row "$1") || return 1
    printf '%s\n' "$row" | awk -F'|' -v n="$2" '{ print $n }'
}

# Prints every declared key, in declaration order — the probe set, stated once.
probe_keys() {
    probe_rows | awk -F'|' '{ print $1 }'
}

# Reports a probe by KEY, taking its name and class from the declaration, so a report-shaped probe's logic supplies only
# the status, detail, and remedy and cannot state a name or class the table does not carry.
# $1 key, $2 status, $3 detail, $4 remedy
probe_report() {
    key=$1
    st=$2
    detail=$3
    remedy=$4
    name=$(probe_field "$key" 1) || {
        echo "probe_report: undeclared probe '$key'" >&2
        exit 1
    }
    class=$(probe_field "$key" 2)
    report "$name" "$class" "$st" "$detail" "$remedy"
}

# Runs every declared `tool` probe of one class from its row, so a tool probe is defined by its row and nothing restates
# it. Reads the rows in the current shell (a pipe would run the loop in a subshell and lose check_tool's side effects on
# the exit flags). $1 the class to run (required|optional)
run_declared_tools() {
    rows=$(probe_rows | awk -F'|' -v class="$1" '$3 == "tool" && $2 == class && $5 != "" { print }')
    old_ifs=$IFS
    IFS='
'
    for row in $rows; do
        IFS='|'
        set -- $row
        IFS=$old_ifs
        check_tool "$1" "$2" "$5" "$6" "$7"
    done
    IFS=$old_ifs
}

validate_probe_table

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
run_declared_tools required
if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    compose_version=$(raw_version docker "compose version") || compose_version=""
    compose_major=${compose_version%%.*}
    case "$compose_major" in
    '' | *[!0-9]*) compose_major=0 ;;
    esac
    if [ "$compose_major" -ge 2 ]; then
        probe_report "compose" "$status_ok" "$compose_version" ""
    else
        compose_hint=$(install_hint compose)
        [ -n "$compose_hint" ] || compose_hint="not up to date (install a Compose v2 for your platform)"
        probe_report "compose" "$status_old" "$compose_version (needs >= 2)" "$compose_hint"
    fi
else
    compose_hint=$(install_hint compose)
    [ -n "$compose_hint" ] || compose_hint="not installed (install a Compose v2 for your platform)"
    probe_report "compose" "$status_missing" "" "$compose_hint"
fi

echo
echo "Optional:"
run_declared_tools optional

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
# The hooks are advisory, not required: a clone that has not run install-git-hooks.sh still builds and tests (the hooks
# guard commits, not the default build-and-test path), so an uninstalled hook is reported without failing the run.
if [ -n "$hooks_resolved" ] && [ -x "$hooks_resolved/pre-commit" ]; then
    probe_report "git-hooks" "$status_ok" "$hooks_path" ""
else
    probe_report "git-hooks" "$status_missing" "" "./scripts/install-git-hooks.sh"
fi

if command -v docker >/dev/null 2>&1; then
    if docker info >/dev/null 2>&1; then
        probe_report "docker-daemon" "$status_ok" "reachable" ""
    else
        probe_report "docker-daemon" "$status_missing" "" "start the Docker daemon (Docker Desktop, or colima start)"
    fi
else
    probe_report "docker-daemon" "$status_missing" "" "install Docker first"
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
