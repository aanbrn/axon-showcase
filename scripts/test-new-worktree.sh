#!/bin/sh
# Tests for `scripts/new-worktree.sh`, the wrapper that creates a unit's worktree and prepares it for IntelliJ in one
# operation. POSIX sh, stdlib tools only.
#
# Run with `sh scripts/test-new-worktree.sh`. Each case builds a throwaway bare origin, a primary clone carrying the
# wrapper and a stub `scripts/setup-idea.sh`, and drives the wrapper against it, so no case touches this checkout, its
# worktrees, or a real IntelliJ installation. The stub is the fixture's own file because the wrapper addresses it by
# path.
#
# NEW_WORKTREE_SHELL selects the interpreter the wrapper itself runs under (default `sh`), so a `dash` run can drive
# the wrapper too, not only the harness: `NEW_WORKTREE_SHELL=dash dash scripts/test-new-worktree.sh`.

set -eu

WRAPPER_SHELL=${NEW_WORKTREE_SHELL:-sh}

REPO=$(cd "$(dirname "$0")/.." && pwd)
TMP=$(mktemp -d "${TMPDIR:-/tmp}/new-worktree-test.XXXXXX")
trap 'rm -rf "$TMP"' EXIT INT TERM

failures=0

fail() {
    echo "FAIL: $*" >&2
    failures=$((failures + 1))
}

assert_contains() {
    grep -F -q -- "$2" "$1" || fail "$3 (missing: $2)"
}

assert_equals() {
    [ "$1" = "$2" ] || fail "$3 (got '$1', expected '$2')"
}

make_primary() {
    name=$1
    idea=$2
    stub=$3
    base="$TMP/$name"
    mkdir -p "$base"
    git init --bare -q "$base/origin.git"
    git init -q "$base/primary"
    git -C "$base/primary" config user.email test@example.com
    git -C "$base/primary" config user.name Test
    git -C "$base/primary" config commit.gpgsign false
    mkdir -p "$base/primary/scripts"
    cp "$REPO/scripts/new-worktree.sh" "$base/primary/scripts/"
    case "$stub" in
        configured)
            printf '#!/bin/sh\necho "Settings: CONFIGURED (created)"\n' >"$base/primary/scripts/setup-idea.sh"
            printf 'echo "Python: CONFIGURED — wrote module"\nexit 0\n' >>"$base/primary/scripts/setup-idea.sh"
            ;;
        skipped)
            printf '#!/bin/sh\necho "Settings: CONFIGURED (created)"\n' >"$base/primary/scripts/setup-idea.sh"
            printf 'echo "Python: SKIPPED — nothing to configure"\nexit 0\n' >>"$base/primary/scripts/setup-idea.sh"
            ;;
        failing)
            printf '#!/bin/sh\necho "Settings: CONFIGURED (created)"\n' >"$base/primary/scripts/setup-idea.sh"
            printf 'echo "Python: CONFIGURED — wrote module"\nexit 1\n' >>"$base/primary/scripts/setup-idea.sh"
            ;;
    esac
    chmod +x "$base/primary/scripts/new-worktree.sh" "$base/primary/scripts/setup-idea.sh"
    printf '3.12.16\n' >"$base/primary/.python-version"
    printf '.idea/\n.python-version\n' >"$base/primary/.gitignore"
    printf 'fixture\n' >"$base/primary/README.md"
    git -C "$base/primary" add -A
    git -C "$base/primary" commit -q -m init
    git -C "$base/primary" branch -M main
    git -C "$base/primary" remote add origin "$base/origin.git"
    git -C "$base/primary" push -q -u origin main
    if [ "$idea" = yes ]; then
        mkdir -p "$base/primary/.idea"
    fi
    printf '%s\n' "$base"
}

run_wrapper() {
    primary=$1
    log=$2
    shift 2
    status=0
    (cd "$primary" && "$WRAPPER_SHELL" ./scripts/new-worktree.sh "$@") >"$log" 2>&1 || status=$?
    printf '%s\n' "$status"
}

echo "== a new unit: created, inherited, prepared =="
base=$(make_primary new-unit yes configured)
status=$(run_wrapper "$base/primary" "$base/out.log" my-unit)
assert_equals "$status" 0 "a prepared tree exits zero"
[ -d "$base/axon-showcase-my-unit" ] || fail "the sibling tree was not created"
assert_equals "$(git -C "$base/axon-showcase-my-unit" branch --show-current)" my-unit "the branch is the unit name"
assert_equals "$(cat "$base/axon-showcase-my-unit/.python-version")" 3.12.16 "the selection was inherited"
assert_contains "$base/out.log" "Python: CONFIGURED" "the setup stage line is reported"
assert_contains "$base/out.log" "Tree ready:" "the readiness line is reported"
if git -C "$base/axon-showcase-my-unit" rev-parse --abbrev-ref --symbolic-full-name "@{u}" >/dev/null 2>&1; then
    fail "a new unit's branch tracks an upstream"
fi

echo "== a primary without .idea/: nothing owed =="
base=$(make_primary no-idea no configured)
status=$(run_wrapper "$base/primary" "$base/out.log" bare-unit)
assert_equals "$status" 0 "a tree whose primary has no .idea/ exits zero"
[ -d "$base/axon-showcase-bare-unit" ] || fail "the tree was not created"
assert_contains "$base/out.log" "no IntelliJ preparation was owed" "the report says nothing was owed"

echo "== SKIPPED with exit zero is not ready =="
base=$(make_primary skipped-idea yes skipped)
status=$(run_wrapper "$base/primary" "$base/out.log" skipped-unit)
assert_equals "$status" 1 "a SKIPPED Python stage is not ready"
[ -d "$base/axon-showcase-skipped-unit" ] || fail "the tree was deleted or not created"
assert_contains "$base/out.log" "Tree NOT ready:" "the failure names the tree"
assert_contains "$base/out.log" "Python: SKIPPED" "the failure carries the stage line"

echo "== a CONFIGURED stage with a failing run keeps the tree =="
base=$(make_primary failing-idea yes failing)
status=$(run_wrapper "$base/primary" "$base/out.log" failing-unit)
assert_equals "$status" 1 "a failed setup run is not ready"
[ -d "$base/axon-showcase-failing-unit" ] || fail "the tree was deleted on a failed preparation"
assert_contains "$base/out.log" "setup exit: 1" "the failure reports the setup exit"

echo "== a continued branch containing '/' gets a sanitized destination =="
base=$(make_primary continue-branch yes configured)
git -C "$base/primary" switch -q -c "feature/sibling"
git -C "$base/primary" push -q -u origin "feature/sibling"
git -C "$base/primary" switch -q main
status=$(run_wrapper "$base/primary" "$base/out.log" --continue "feature/sibling")
assert_equals "$status" 0 "a continued branch exits zero when ready"
[ -d "$base/axon-showcase-feature-sibling" ] || fail "the sanitized destination is not used"
assert_equals "$(git -C "$base/axon-showcase-feature-sibling" branch --show-current)" "feature/sibling" \
    "the continued tree is on the named branch"

echo "== a tree carrying its own .python-version keeps it =="
base=$(make_primary tracked-selection no configured)
printf '.idea/\n' >"$base/primary/.gitignore"
git -C "$base/primary" add -f .python-version
git -C "$base/primary" commit -q -m "track the selection"
git -C "$base/primary" push -q origin main
git -C "$base/primary" switch -q -c "feature/tracked"
printf '3.12.1\n' >"$base/primary/.python-version"
git -C "$base/primary" commit -q -am "branch selection"
git -C "$base/primary" push -q -u origin "feature/tracked"
git -C "$base/primary" switch -q main
printf '3.12.16\n' >"$base/primary/.python-version"
status=$(run_wrapper "$base/primary" "$base/out.log" --continue "feature/tracked")
assert_equals "$status" 0 "the tracked-selection tree exits zero"
assert_equals "$(cat "$base/axon-showcase-feature-tracked/.python-version")" 3.12.1 \
    "the wrapper does not overwrite a tree's own selection"

echo "== the fetch reaches a newly pushed origin/main =="
base=$(make_primary fetch-main yes configured)
git clone -q "$base/origin.git" "$base/publisher"
git -C "$base/publisher" config user.email test@example.com
git -C "$base/publisher" config user.name Test
git -C "$base/publisher" checkout -q -B main origin/main
printf 'advanced\n' >>"$base/publisher/README.md"
git -C "$base/publisher" commit -q -am "advance"
git -C "$base/publisher" push -q origin main
expected=$(git -C "$base/publisher" rev-parse HEAD)
status=$(run_wrapper "$base/primary" "$base/out.log" fetched-unit)
assert_equals "$status" 0 "the fetched tree exits zero"
assert_equals "$(git -C "$base/axon-showcase-fetched-unit" rev-parse HEAD)" "$expected" \
    "the tree starts from the fetched origin/main"

echo "== a remote-only branch is continued through git's DWIM =="
base=$(make_primary remote-only yes configured)
git clone -q "$base/origin.git" "$base/publisher"
git -C "$base/publisher" config user.email test@example.com
git -C "$base/publisher" config user.name Test
git -C "$base/publisher" checkout -q -B main origin/main
git -C "$base/publisher" checkout -q -b "feature/remote-only"
printf 'remote only\n' >"$base/publisher/BRANCH.md"
git -C "$base/publisher" add -A
git -C "$base/publisher" commit -q -m "remote-only branch"
git -C "$base/publisher" push -q -u origin "feature/remote-only"
expected=$(git -C "$base/publisher" rev-parse HEAD)
if git -C "$base/primary" rev-parse --verify --quiet "refs/heads/feature/remote-only" >/dev/null; then
    fail "the fixture left a local branch in the primary"
fi
status=$(run_wrapper "$base/primary" "$base/out.log" --continue "feature/remote-only")
assert_equals "$status" 0 "a remote-only branch is continued"
[ -d "$base/axon-showcase-feature-remote-only" ] || fail "the remote-only tree was not created"
assert_equals "$(git -C "$base/axon-showcase-feature-remote-only" branch --show-current)" "feature/remote-only" \
    "the remote-only tree is on the named branch"
assert_equals "$(git -C "$base/axon-showcase-feature-remote-only" rev-parse --abbrev-ref --symbolic-full-name "@{u}")" \
    "origin/feature/remote-only" "the remote-only tree tracks the remote branch"
assert_equals "$(git -C "$base/axon-showcase-feature-remote-only" rev-parse HEAD)" "$expected" \
    "the remote-only tree starts at the published commit"

echo "== the primary is resolved from the script, not the caller's cwd =="
base=$(make_primary other-cwd yes configured)
status=0
(cd "$TMP" && "$WRAPPER_SHELL" "$base/primary/scripts/new-worktree.sh" cwd-unit) >"$base/out.log" 2>&1 || status=$?
assert_equals "$status" 0 "an invocation from another directory succeeds"
[ -d "$base/axon-showcase-cwd-unit" ] || fail "the tree was not created from another cwd"

echo "== an existing destination is refused =="
base=$(make_primary occupied yes configured)
mkdir -p "$base/axon-showcase-taken"
status=$(run_wrapper "$base/primary" "$base/out.log" taken)
assert_equals "$status" 2 "an existing destination is a usage error"
assert_contains "$base/out.log" "already exists" "the refusal names the reason"

echo "== a refused git operation exits 2 and creates no tree =="
base=$(make_primary git-failure yes configured)
git -C "$base/primary" remote set-url origin "$base/missing-origin.git"
status=$(run_wrapper "$base/primary" "$base/out.log" no-origin-unit)
assert_equals "$status" 2 "a failed fetch is a refused invocation"
[ ! -e "$base/axon-showcase-no-origin-unit" ] || fail "a refused invocation created a tree"
assert_contains "$base/out.log" "git fetch failed" "the refusal names the failed fetch"

base=$(make_primary branch-busy yes configured)
git -C "$base/primary" switch -q -c busy
git -C "$base/primary" switch -q main
git -C "$base/primary" worktree add -q "$base/elsewhere" busy
status=$(run_wrapper "$base/primary" "$base/out.log" --continue busy)
assert_equals "$status" 2 "a branch checked out elsewhere is a refused invocation"
[ ! -e "$base/axon-showcase-busy" ] || fail "a refused --continue created a tree"
assert_contains "$base/out.log" "git worktree add failed" "the refusal names the failed add"

echo "== usage errors =="
base=$(make_primary usage yes configured)
status=$(run_wrapper "$base/primary" "$base/out.log")
assert_equals "$status" 2 "a missing argument is a usage error"
status=$(run_wrapper "$base/primary" "$base/out.log" --continue)
assert_equals "$status" 2 "--continue without a branch is a usage error"

if [ "$failures" -ne 0 ]; then
    echo "$failures new-worktree control(s) failed" >&2
    exit 1
fi

echo "All new-worktree controls passed."
