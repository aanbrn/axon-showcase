#!/usr/bin/env python3
"""Report and recover the MCP servers that failed to connect, through the running OpenCode server.

OpenCode records a failed MCP connection and does not retry it, so a server that failed once -- on a laptop whose
network was still coming up, for instance -- stays `failed` for that workspace until OpenCode restarts. The running
server exposes a per-server connect operation, and the `opencode api` client reaches it without this tool knowing the
service's address or password. This tool supplies the policy around that call: with no target it reports every
server's status and changes nothing, with a name it repairs one server, and with `--all` it repairs the failed set --
each attempt at most one connect, a verified `connected` result, its own bounded deadline, and an exit contract a caller
can branch on.

The fixture suite in `scripts/test-reconnect-mcp.py` is the reference for every branch; this docstring states only
what a caller needs.

Exit codes: 0 every targeted server is connected -- or, with no target, none of them is `failed`; 1 a targeted server is
unknown, its status is not eligible for reconnection, it is still not connected, or the outcome could not be confirmed
once an attempt was sent -- or, with no target, some server is `failed`; 2 the invocation or the transport failed before
an attempt was sent.

Stdlib only, compatible with the build's declared Python 3.12 minimum.
"""

import argparse
import json
import math
import subprocess
import sys
import time
from pathlib import Path
from typing import Dict, Optional, Sequence

DEFAULT_DEADLINE_SECONDS = 60.0
POLL_INTERVAL_SECONDS = 0.5
GIT_TIMEOUT_SECONDS = 10.0
CONNECTED = "connected"
FAILED = "failed"
SUCCESS = 0
UNCONFIRMED = 1
TRANSPORT_FAILURE = 2
API_OK = 0
MAX_DIAGNOSTIC_CHARS = 200


class TransportError(Exception):
    """The OpenCode client could not be run, could not answer, or answered with something this tool cannot read."""


class DeadlineExceeded(TransportError):
    """The invocation's budget ran out before a request could be sent or answered."""


class ServerMissing(Exception):
    """The API answered that the named server is not configured for the workspace."""


def run_api(arguments: Sequence[str], deadline: float) -> subprocess.CompletedProcess:
    """Run one `opencode api` invocation within what is left of the deadline."""
    remaining = deadline - time.monotonic()
    if remaining <= 0:
        raise DeadlineExceeded("the deadline expired before the request could be sent")
    try:
        return subprocess.run(
            ["opencode", "api", *arguments],
            capture_output=True,
            text=True,
            timeout=remaining,
            check=False,
        )
    except FileNotFoundError:
        raise TransportError("`opencode` is not on PATH")
    except subprocess.TimeoutExpired:
        raise DeadlineExceeded("`opencode api` did not answer within the deadline")


def describe(completed: subprocess.CompletedProcess) -> str:
    """Reduce a failed invocation to one short line, quoting its HTTP status rather than its body."""
    for stream in (completed.stdout, completed.stderr):
        for line in (stream or "").splitlines():
            stripped = line.strip()
            if stripped.startswith("HTTP "):
                return stripped[:MAX_DIAGNOSTIC_CHARS]
    return f"`opencode api` exited {completed.returncode}"


def list_servers(directory: str, deadline: float) -> Dict[str, Dict[str, str]]:
    """Return the workspace's MCP servers keyed by name, each mapped to its status object."""
    completed = run_api(["mcp.list", "--param", f"location[directory]={directory}"], deadline)
    if completed.returncode != 0:
        raise TransportError(describe(completed))
    try:
        payload = json.loads(completed.stdout)
    except json.JSONDecodeError:
        raise TransportError("`opencode api mcp.list` did not return JSON")
    data = payload.get("data") if isinstance(payload, dict) else None
    if not isinstance(data, list):
        raise TransportError("`opencode api mcp.list` returned no server list")
    servers: Dict[str, Dict[str, str]] = {}
    for entry in data:
        if isinstance(entry, dict) and isinstance(entry.get("name"), str):
            status = entry.get("status")
            servers[entry["name"]] = status if isinstance(status, dict) else {}
    return servers


def connect(server: str, directory: str, deadline: float) -> None:
    """Submit the one connect attempt, mapping a missing server onto its own exception."""
    completed = run_api(
        ["experimental.mcp.connect", "--param", f"server={server}", "--param", f"location[directory]={directory}"],
        deadline,
    )
    if completed.returncode == API_OK:
        return
    answer = f"{completed.stdout}\n{completed.stderr}"
    if "McpServerNotFoundError" in answer:
        raise ServerMissing(server)
    raise TransportError(describe(completed))


def wait_for_connected(server: str, directory: str, deadline: float) -> str:
    """Poll until the server reports `connected` or the deadline expires, returning the last status seen."""
    status = "unknown"
    while True:
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            return status
        servers = list_servers(directory, deadline)
        status = str(servers.get(server, {}).get("status", "unknown"))
        if status == CONNECTED:
            return status
        time.sleep(min(POLL_INTERVAL_SECONDS, max(deadline - time.monotonic(), 0.0)))


def resolve_directory(explicit: Optional[str], deadline: float) -> str:
    """Resolve the workspace location: the explicit override, else the repository root, else the working directory."""
    if explicit:
        return str(Path(explicit).expanduser().resolve())
    remaining = deadline - time.monotonic()
    if remaining <= 0:
        raise DeadlineExceeded("the deadline expired before the workspace could be resolved")
    try:
        completed = subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            capture_output=True,
            text=True,
            timeout=min(GIT_TIMEOUT_SECONDS, remaining),
            check=False,
        )
        top_level = completed.stdout.strip()
        if completed.returncode == 0 and top_level:
            return top_level
    except subprocess.TimeoutExpired:
        raise DeadlineExceeded("the workspace could not be resolved within the deadline")
    except OSError:
        pass
    return str(Path.cwd())


def render(entry: Dict[str, str]) -> str:
    """Render one server's status, with the client's own error text beside it when it has one."""
    status = str(entry.get("status", "unknown"))
    error = entry.get("error")
    return f"{status} ({error})" if isinstance(error, str) and error.strip() else status


def report(servers: Dict[str, Dict[str, str]]) -> int:
    """Print every server's status and return success only when none of them failed."""
    failed = False
    for name in sorted(servers):
        entry = servers[name]
        print(f"{name}: {render(entry)}")
        failed = failed or entry.get("status") == FAILED
    return UNCONFIRMED if failed else SUCCESS


def repair(server: str, directory: str, entry: Dict[str, str], budget: float) -> int:
    """Repair one already-listed server within its own budget, printing its single result line."""
    deadline = time.monotonic() + budget
    before = str(entry.get("status", "unknown"))
    if before == CONNECTED:
        print(f"{server}: connected (no-op)")
        return SUCCESS
    if before != FAILED:
        print(f"{server}: {before} (not eligible for reconnection; only a failed server is retried)")
        return UNCONFIRMED
    try:
        connect(server, directory, deadline)
        status = wait_for_connected(server, directory, deadline)
    except ServerMissing:
        print(f"{server}: not found at {directory}")
        return UNCONFIRMED
    except DeadlineExceeded:
        print(f"{server}: {before} (deadline exceeded; connection outcome unknown)")
        return UNCONFIRMED
    except TransportError as error:
        print(f"{server}: {before} (connection outcome unknown: {error})")
        return UNCONFIRMED
    if status == CONNECTED:
        print(f"{server}: {before} -> connected (reconnected)")
        return SUCCESS
    print(f"{server}: {status} (deadline exceeded; connection outcome unknown)")
    return UNCONFIRMED


def parse_arguments(argv: Optional[Sequence[str]]) -> argparse.Namespace:
    """Parse the target (one name, `--all`, or none for a report), the location, and the deadline."""
    parser = argparse.ArgumentParser(
        prog="reconnect-mcp.py",
        description=(
            "Reconnect the MCP servers that failed to connect, without restarting OpenCode. With no name it reports "
            "every server's status and changes nothing."
        ),
        epilog=(
            "Exit codes: 0 every targeted server is connected (with no name, none failed); 1 a targeted server is "
            "unknown, ineligible, still not connected, or unconfirmed (with no name, some server failed); 2 the "
            "invocation or the transport failed before an attempt was sent."
        ),
    )
    parser.add_argument(
        "server",
        nargs="?",
        help="one MCP server name, as the workspace configures it; omit to report every server's status",
    )
    parser.add_argument(
        "--all",
        action="store_true",
        help="repair every server whose status is failed; cannot be combined with a server name",
    )
    parser.add_argument(
        "--directory",
        help="the workspace location to address (default: this repository's root)",
    )
    parser.add_argument(
        "--deadline",
        type=float,
        default=DEFAULT_DEADLINE_SECONDS,
        metavar="SECONDS",
        help=f"the time budget for each recovery attempt (default: {DEFAULT_DEADLINE_SECONDS:g})",
    )
    arguments = parser.parse_args(argv)
    if not math.isfinite(arguments.deadline) or arguments.deadline <= 0:
        parser.error("--deadline must be a finite number of seconds above zero")
    if arguments.all and arguments.server:
        parser.error("--all cannot be combined with a server name")
    return arguments


def main(argv: Optional[Sequence[str]] = None) -> int:
    """Run one report or recovery invocation and return its exit code."""
    arguments = parse_arguments(argv)
    discovery_deadline = time.monotonic() + arguments.deadline
    label = arguments.server or arguments.directory or "workspace"
    try:
        directory = resolve_directory(arguments.directory, discovery_deadline)
        servers = list_servers(directory, discovery_deadline)
    except TransportError as error:
        print(f"{label}: {error}", file=sys.stderr)
        return TRANSPORT_FAILURE
    if arguments.all:
        targets = [name for name, entry in sorted(servers.items()) if entry.get("status") == FAILED]
        if not targets:
            print(f"no failed MCP servers at {directory}")
            return SUCCESS
        worst = SUCCESS
        for name in targets:
            worst = max(worst, repair(name, directory, servers[name], arguments.deadline))
        return worst
    if arguments.server is None:
        return report(servers)
    if arguments.server not in servers:
        known = ", ".join(sorted(servers)) or "none"
        print(f"{arguments.server}: not found at {directory} (servers: {known})")
        return UNCONFIRMED
    return repair(arguments.server, directory, servers[arguments.server], arguments.deadline)


if __name__ == "__main__":
    sys.exit(main())
