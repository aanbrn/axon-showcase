# Unit: park-the-download-retry-idea

Workspace:
`unit=park-the-download-retry-idea; path=/Users/afanasyev/Development/axon-showcase; branch=park-the-download-retry-idea; writer=ses_e4add21daffe0vW0OISMmtQS3i`

State: closed

A docs-fix unit with no change directory. The owner's authorization of 2026-11-08 ("Припаркуй идею") names the one
direct edit: park an idea in `docs/ideas.md` about the `build` gate's release-asset download window, which failed for
about half an hour (08:53–09:23Z) while `githubstatus.com` reported all systems operational for that window.

`Packs: none` — one idea entry, no code, build, or deployment surface.

## What the entry records

During PR #867 the `Install actionlint` and `Scan the source for secrets` steps each retried their GitHub release-asset
download three times with a five-second pause, and six attempts failed on the actionlint install and three on the
gitleaks scan between 08:53Z and 09:23Z (about half an hour): the run took ten attempts (nine re-runs,
`run_attempt: 10`) before both assets served `200` and the gate passed. The actionlint install's log shows a 92-byte
body that is not a gzip stream; the caller's own `curl` of both assets returned `504` with the same 92-byte body and
intermittently `200`; and `githubstatus.com` read `All Systems Operational` for that window, its 10:36–12:32Z incident
for unauthenticated-request timeouts postdating the failures and corroborating the premise. `gitleaks` is pinned to
8.30.1 and checksum-verified, while `actionlint` installs the installer script's `latest` default with no checksum, so
the idea scopes its "keep the pin and checksum" clause to `gitleaks` and requires pinning `actionlint` first.

## Boundary

The parked entry is a direction for a future change, not a task here: this unit writes the idea and nothing else. The
workflow stays as it is, so the gate's behaviour during the next such outage is unchanged.
