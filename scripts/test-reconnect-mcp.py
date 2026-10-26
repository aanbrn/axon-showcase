#!/usr/bin/env python3
"""Tests for the MCP reconnect tool (`scripts/reconnect-mcp.py`). Stdlib only.

Run with `python3 scripts/test-reconnect-mcp.py`. The tool's kebab-case filename is not an importable identifier, so it
is loaded by path, the way `scripts/test-review-routing.py` loads its subject.

The tool's subject is its arguments, parsing, deadline, exit contract, and the request sequence it emits — the transport
is the `opencode api` client, which the tool does not own. Every case therefore drives `main()` against a fixture
`opencode` executable on `PATH` that records each invocation and mutates its own state file, so the assertions read the
argv that really reached the client and the exit status the really-running `subprocess` path produced.
"""

import contextlib
import importlib.util
import io
import json
import os
import tempfile
import time
import unittest
from pathlib import Path
from typing import Dict, List, Optional
from unittest import mock

REPO = Path(__file__).resolve().parent.parent
TOOL_PATH = Path(__file__).resolve().parent / "reconnect-mcp.py"
_spec = importlib.util.spec_from_file_location("reconnect_mcp", TOOL_PATH)
reconnect_mcp = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(reconnect_mcp)

FIXTURE = '''#!/usr/bin/env python3
"""Stand-in for the `opencode` CLI: records each invocation and evolves its state file."""

import json
import os
import sys
import time

state_path = os.environ["FIXTURE_STATE"]
log_path = os.environ["FIXTURE_LOG"]
with open(state_path, encoding="utf-8") as handle:
    state = json.load(handle)
argv = sys.argv[1:]
if argv and argv[0] == "api":
    argv = argv[1:]
with open(log_path, "a", encoding="utf-8") as handle:
    handle.write(json.dumps(argv) + "\\n")


def save():
    with open(state_path, "w", encoding="utf-8") as handle:
        json.dump(state, handle)


def parameter(name):
    for index, item in enumerate(argv):
        if item == "--param" and index + 1 < len(argv) and argv[index + 1].startswith(name + "="):
            return argv[index + 1].split("=", 1)[1]
    return ""


operation = argv[0] if argv else ""


def flip(names):
    for entry in state["data"]:
        if entry["name"] in names:
            entry["status"] = {"status": "connected"}


if operation == "mcp.list":
    time.sleep(state.get("list_delay", 0))
    failure = state.get("list_failure")
    if failure:
        print(failure)
        sys.exit(state.get("list_exit", 1))
    raw = state.get("list_raw")
    if raw:
        print(raw)
        sys.exit(0)
    pending = state.get("flip_after_lists")
    if isinstance(pending, int):
        pending -= 1
        state["flip_after_lists"] = pending
        if pending <= 0:
            flip([state.get("flip_server", "")])
        save()
    print(json.dumps({"location": {"directory": "fixture"}, "data": state["data"]}))
    sys.exit(0)

if operation == "experimental.mcp.connect":
    time.sleep(state.get("connect_delay", 0))
    target = parameter("server")
    location = parameter("location[directory]")
    if not target or not location:
        print("connect needs both server and location[directory]", file=sys.stderr)
        sys.exit(64)
    if state.get("effect") == "connected" and target not in state.get("effect_except", []):
        flip([target])
        save()
    sys.stdout.write(state.get("connect_stdout", ""))
    sys.stderr.write(state.get("connect_stderr", ""))
    sys.exit(state.get("connect_exit", 0))

print("unexpected operation: " + operation, file=sys.stderr)
sys.exit(64)
'''


class FixtureCase(unittest.TestCase):
    """A case with a fixture `opencode`, an isolated workspace directory, and a per-case call log."""

    def setUp(self) -> None:
        self._temp = tempfile.TemporaryDirectory()
        self.addCleanup(self._temp.cleanup)
        self.root = Path(self._temp.name)
        self.state = self.root / "state.json"
        self.log = self.root / "calls.log"
        self.bin = self.root / "bin"
        self.bin.mkdir()
        self.directory = self.root / "workspace"
        self.directory.mkdir()
        fixture = self.bin / "opencode"
        fixture.write_text(FIXTURE, encoding="utf-8")
        fixture.chmod(0o755)
        self.log.write_text("", encoding="utf-8")
        self._interval = reconnect_mcp.POLL_INTERVAL_SECONDS
        reconnect_mcp.POLL_INTERVAL_SECONDS = 0.02
        self.addCleanup(self.restore_interval)

    def restore_interval(self) -> None:
        reconnect_mcp.POLL_INTERVAL_SECONDS = self._interval

    def write_state(self, servers: List[Dict[str, object]], **extra: object) -> None:
        payload = {"data": servers}
        payload.update(extra)
        self.state.write_text(json.dumps(payload), encoding="utf-8")

    def servers(self, *statuses: tuple) -> List[Dict[str, object]]:
        return [{"name": name, "status": {"status": status}} for name, status in statuses]

    def fixture_environment(self) -> Dict[str, str]:
        """The environment that puts the fixture `opencode` first on PATH and points it at this case's state."""
        environment = dict(os.environ)
        environment["PATH"] = f"{self.bin}{os.pathsep}{environment.get('PATH', '')}"
        environment["FIXTURE_STATE"] = str(self.state)
        environment["FIXTURE_LOG"] = str(self.log)
        return environment

    def run_tool(
        self,
        *arguments: str,
        deadline: str = "5",
        directory: Optional[Path] = None,
        omit_directory: bool = False,
    ) -> tuple:
        environment = self.fixture_environment()
        target = directory or self.directory
        argv = list(arguments) + ["--deadline", deadline]
        if not omit_directory:
            argv += ["--directory", str(target)]
        out, err = io.StringIO(), io.StringIO()
        with mock.patch.dict(os.environ, environment, clear=False):
            with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
                code = reconnect_mcp.main(argv)
        return code, out.getvalue().strip(), err.getvalue().strip()

    def calls(self) -> List[List[str]]:
        return [json.loads(line) for line in self.log.read_text(encoding="utf-8").splitlines() if line]

    def connects(self) -> List[List[str]]:
        return [call for call in self.calls() if call[0] == "experimental.mcp.connect"]

    def statuses(self) -> Dict[str, str]:
        state = json.loads(self.state.read_text(encoding="utf-8"))
        return {entry["name"]: entry["status"]["status"] for entry in state["data"]}


class ReconnectTests(FixtureCase):
    def test_a_failed_server_is_reconnected(self) -> None:
        self.write_state(self.servers(("playwright", "failed"), ("github", "connected")), effect="connected")
        code, out, err = self.run_tool("playwright")
        self.assertEqual(0, code, err)
        self.assertEqual("playwright: failed -> connected (reconnected)", out)
        self.assertEqual(1, len(self.connects()))

    def test_an_already_connected_server_is_a_no_op(self) -> None:
        self.write_state(self.servers(("playwright", "connected")))
        code, out, err = self.run_tool("playwright")
        self.assertEqual(0, code, err)
        self.assertEqual("playwright: connected (no-op)", out)
        self.assertEqual([], self.connects())

    def test_only_the_named_server_is_mutated(self) -> None:
        self.write_state(self.servers(("playwright", "failed"), ("github", "failed")), effect="connected")
        code, _, err = self.run_tool("playwright")
        self.assertEqual(0, code, err)
        connects = self.connects()
        self.assertEqual(1, len(connects))
        self.assertIn("server=playwright", connects[0])
        self.assertIn(f"location[directory]={self.directory.resolve()}", connects[0])
        self.assertEqual({"playwright": "connected", "github": "failed"}, self.statuses())

    def test_an_unknown_server_is_not_mutated(self) -> None:
        self.write_state(self.servers(("github", "connected")))
        code, out, _ = self.run_tool("steroid")
        self.assertEqual(1, code)
        self.assertEqual(f"steroid: not found at {self.directory.resolve()} (servers: github)", out)
        self.assertEqual([], self.connects())

    def test_an_ineligible_status_is_reported_without_connecting(self) -> None:
        self.write_state(self.servers(("playwright", "needs-auth")))
        code, out, _ = self.run_tool("playwright")
        self.assertEqual(1, code)
        self.assertIn("needs-auth", out)
        self.assertIn("not eligible", out)
        self.assertEqual([], self.connects())

    def test_a_server_still_failed_after_the_acknowledgement_exits_nonzero(self) -> None:
        self.write_state(self.servers(("playwright", "failed")))
        code, out, _ = self.run_tool("playwright", deadline="1.5")
        self.assertEqual(1, code)
        self.assertIn("playwright: failed", out)
        self.assertIn("outcome unknown", out)
        self.assertEqual(1, len(self.connects()))

    def test_a_transition_after_a_failed_poll_is_reported(self) -> None:
        self.write_state(
            self.servers(("playwright", "failed")),
            flip_after_lists=3,
            flip_server="playwright",
        )
        code, out, err = self.run_tool("playwright")
        self.assertEqual(0, code, err)
        self.assertEqual("playwright: failed -> connected (reconnected)", out)
        polls = [call for call in self.calls() if call[0] == "mcp.list"]
        self.assertGreaterEqual(len(polls), 3)

    def test_a_missing_server_from_the_connect_call_exits_one(self) -> None:
        self.write_state(
            self.servers(("playwright", "failed")),
            connect_exit=1,
            connect_stdout='{"_tag":"McpServerNotFoundError","server":"playwright"}\n',
        )
        code, out, _ = self.run_tool("playwright")
        self.assertEqual(1, code)
        self.assertIn("not found", out)

    def test_a_failing_transport_exits_two(self) -> None:
        self.write_state(self.servers(("playwright", "failed")), list_failure="HTTP 500 Internal Server Error")
        code, out, err = self.run_tool("playwright")
        self.assertEqual(2, code)
        self.assertEqual("", out)
        self.assertIn("HTTP 500 Internal Server Error", err)

    def test_a_transport_failure_during_the_attempt_reports_an_unknown_outcome(self) -> None:
        self.write_state(
            self.servers(("playwright", "failed")),
            connect_exit=1,
            connect_stderr="HTTP 500 Internal Server Error\n",
        )
        code, out, _ = self.run_tool("playwright")
        self.assertEqual(1, code)
        self.assertIn("outcome unknown", out)
        self.assertIn("HTTP 500 Internal Server Error", out)

    def test_a_malformed_list_response_exits_two(self) -> None:
        self.write_state(self.servers(("playwright", "failed")), list_raw="not json")
        code, _, err = self.run_tool("playwright")
        self.assertEqual(2, code)
        self.assertIn("did not return JSON", err)

    def test_the_output_carries_no_response_body(self) -> None:
        secret = "token=super-secret-value"
        self.write_state(
            self.servers(("playwright", "failed")),
            list_failure=f'{{"_tag":"SomeError","message":"{secret}"}}',
            list_exit=1,
        )
        code, out, err = self.run_tool("playwright")
        self.assertEqual(2, code)
        self.assertNotIn(secret, out + err)

    def test_a_directory_with_spaces_is_passed_verbatim(self) -> None:
        self.write_state(self.servers(("playwright", "connected")))
        spaced = self.root / "a workspace"
        spaced.mkdir()
        code, _, err = self.run_tool("playwright", directory=spaced)
        self.assertEqual(0, code, err)
        params = [call for call in self.calls() if call[0] == "mcp.list"]
        self.assertIn(f"location[directory]={spaced.resolve()}", params[0])

    def test_a_stalled_listing_fails_within_the_deadline(self) -> None:
        self.write_state(self.servers(("playwright", "failed")), list_delay=3)
        started = time.monotonic()
        code, _, err = self.run_tool("playwright", deadline="0.6")
        self.assertEqual(2, code)
        self.assertIn("did not answer within the deadline", err)
        self.assertLess(time.monotonic() - started, 2.0)

    def test_a_stalled_connect_reports_an_unknown_outcome(self) -> None:
        self.write_state(self.servers(("playwright", "failed")), connect_delay=5)
        started = time.monotonic()
        code, out, _ = self.run_tool("playwright", deadline="2")
        self.assertEqual(1, code)
        self.assertIn("outcome unknown", out)
        self.assertEqual(1, len(self.connects()))
        self.assertLess(time.monotonic() - started, 4.0)

    def test_no_name_reports_every_server_and_succeeds_when_none_failed(self) -> None:
        self.write_state(self.servers(("playwright", "connected"), ("github", "connected")))
        code, out, err = self.run_tool()
        self.assertEqual(0, code, err)
        self.assertEqual("github: connected\nplaywright: connected", out)
        self.assertEqual([], self.connects())

    def test_no_name_reports_a_failed_server_and_exits_nonzero(self) -> None:
        state = self.servers(("playwright", "failed"), ("github", "connected"))
        state[0]["status"]["error"] = "Request timed out"
        self.write_state(state)
        code, out, err = self.run_tool()
        self.assertEqual(1, code, err)
        self.assertIn("playwright: failed (Request timed out)", out)
        self.assertEqual([], self.connects())

    def test_all_repairs_every_failed_server(self) -> None:
        self.write_state(
            self.servers(("playwright", "failed"), ("github", "failed"), ("steroid", "connected")),
            effect="connected",
        )
        code, out, err = self.run_tool("--all")
        self.assertEqual(0, code, err)
        self.assertEqual(2, len(self.connects()))
        self.assertIn("github: failed -> connected (reconnected)", out)
        self.assertIn("playwright: failed -> connected (reconnected)", out)
        self.assertEqual(
            {"playwright": "connected", "github": "connected", "steroid": "connected"},
            self.statuses(),
        )

    def test_all_reports_a_server_it_could_not_repair(self) -> None:
        self.write_state(
            self.servers(("playwright", "failed"), ("github", "failed")),
            effect="connected",
            effect_except=["github"],
        )
        code, out, err = self.run_tool("--all", deadline="2")
        self.assertEqual(1, code, err)
        self.assertIn("playwright: failed -> connected (reconnected)", out)
        self.assertIn("github: failed", out)
        self.assertIn("outcome unknown", out)

    def test_all_with_no_failed_server_changes_nothing(self) -> None:
        self.write_state(self.servers(("playwright", "connected")))
        code, out, err = self.run_tool("--all")
        self.assertEqual(0, code, err)
        self.assertIn("no failed MCP servers", out)
        self.assertEqual([], self.connects())

    def test_all_with_a_name_is_a_usage_error(self) -> None:
        self.write_state(self.servers(("playwright", "connected")))
        with mock.patch.dict(os.environ, self.fixture_environment(), clear=False):
            with contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises(SystemExit) as raised:
                    reconnect_mcp.main(["--all", "playwright", "--directory", str(self.directory)])
        self.assertEqual(2, raised.exception.code)
        self.assertEqual([], self.calls())

    def test_the_default_directory_is_the_repository_root(self) -> None:
        working = Path.cwd().resolve()
        if working != REPO and REPO not in working.parents:
            self.skipTest("run from inside the repository")
        self.assertEqual(str(REPO), reconnect_mcp.resolve_directory(None, time.monotonic() + 5))

    def stall_discovery(self) -> None:
        self.write_state(self.servers(("playwright", "failed")), effect="connected")
        git = self.bin / "git"
        git.write_text("#!/usr/bin/env python3\nimport time\ntime.sleep(3)\nprint('/nowhere')\n", encoding="utf-8")
        git.chmod(0o755)

    def assert_discovery_is_bounded(self, *arguments: str) -> None:
        started = time.monotonic()
        code, _, err = self.run_tool(*arguments, deadline="0.6", omit_directory=True)
        self.assertEqual(2, code, err)
        self.assertIn("workspace could not be resolved within the deadline", err)
        self.assertLess(time.monotonic() - started, 2.0)
        self.assertEqual([], self.calls())

    def test_a_stalled_directory_discovery_fails_within_the_deadline(self) -> None:
        self.stall_discovery()
        self.assert_discovery_is_bounded("playwright")

    def test_stalled_discovery_fails_a_report_without_a_traceback(self) -> None:
        self.stall_discovery()
        self.assert_discovery_is_bounded()

    def test_stalled_discovery_fails_an_all_run_without_a_traceback(self) -> None:
        self.stall_discovery()
        self.assert_discovery_is_bounded("--all")

    def test_a_non_finite_deadline_is_a_usage_error(self) -> None:
        for value in ("inf", "nan"):
            with contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises(SystemExit) as raised:
                    reconnect_mcp.main(["playwright", "--deadline", value])
            self.assertEqual(2, raised.exception.code)


if __name__ == "__main__":
    unittest.main(verbosity=2)
