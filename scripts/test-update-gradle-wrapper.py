#!/usr/bin/env python3
"""Tests for the wrapper bump tool (`scripts/update-gradle-wrapper.py`). Stdlib only.

Run with `python3 scripts/test-update-gradle-wrapper.py`. The tool's kebab-case filename is not an importable
identifier, so it is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

Every case runs the tool's own `main` against a temporary repository whose `gradlew` is a fixture script: one that
stamps the requested version and checksum into the properties (the happy path), one that fails on the second pass, and
one that writes only the URL (the half-rewritten state). The checksum source is a local HTTP server, so the fetch path
runs without the distribution service; a malformed body, an empty body and a wrong path cover the refusals.
"""

import contextlib
import http.server
import importlib.util
import io
import stat
import sys
import tempfile
import threading
import unittest
import unittest.mock
from pathlib import Path

TOOL_PATH = Path(__file__).resolve().parent / "update-gradle-wrapper.py"
_spec = importlib.util.spec_from_file_location("update_gradle_wrapper", TOOL_PATH)
update_gradle_wrapper = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(update_gradle_wrapper)

CHECKSUM = "a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf"

PROPERTIES_BODY = (
    "distributionBase=GRADLE_USER_HOME\n"
    "distributionPath=wrapper/dists\n"
    "distributionSha256Sum={checksum}\n"
    "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.8.2-all.zip\n"
    "networkTimeout=10000\n"
    "zipStoreBase=GRADLE_USER_HOME\n"
    "zipStorePath=wrapper/dists\n"
)

HAPPY_GRADLEW = """#!/bin/sh
set -eu
version=""
checksum=""
for argument in "$@"; do
  case "$argument" in
    --gradle-version=*) version="${argument#--gradle-version=}" ;;
    --gradle-distribution-sha256-sum=*) checksum="${argument#--gradle-distribution-sha256-sum=}" ;;
  esac
done
cat > gradle/wrapper/gradle-wrapper.properties <<EOF
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionSha256Sum=$checksum
distributionUrl=https\\://services.gradle.org/distributions/gradle-$version-all.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF
"""

FAILING_GRADLEW = """#!/bin/sh
set -eu
marker=".first-pass-done"
if [ -f "$marker" ]; then
  echo "the second pass failed" >&2
  exit 1
fi
touch "$marker"
chmod 644 gradle/wrapper/gradle-wrapper.jar
printf 'distributionUrl=https\\://services.gradle.org/distributions/gradle-9.8.1-all.zip\\n' \\
  > gradle/wrapper/gradle-wrapper.properties
"""

INCOMPLETE_GRADLEW = """#!/bin/sh
set -eu
printf 'distributionUrl=https\\://services.gradle.org/distributions/gradle-9.8.1-all.zip\\n' \\
  > gradle/wrapper/gradle-wrapper.properties
"""


class ChecksumServer:
    """A local HTTP server answering the checksum path with a fixed body."""

    def __init__(self, body):
        """Serves `body` at `/gradle-9.8.1-all.zip.sha256`.

        :param body: the response body
        """
        payload = body.encode("utf-8")

        class Handler(http.server.BaseHTTPRequestHandler):
            """Answers the checksum path with the payload and everything else with 404."""

            def do_GET(self):
                """Writes the payload only for the `-all.zip.sha256` path."""
                if not self.path.endswith("/gradle-9.8.1-all.zip.sha256"):
                    self.send_response(404)
                    self.end_headers()
                    return
                self.send_response(200)
                self.send_header("Content-Type", "text/plain")
                self.send_header("Content-Length", str(len(payload)))
                self.end_headers()
                self.wfile.write(payload)

            def log_message(self, *_):
                """Silences the server's log."""

        self.server = http.server.HTTPServer(("127.0.0.1", 0), Handler)
        self.url = "http://127.0.0.1:{0}/gradle-{{version}}-all.zip.sha256".format(self.server.server_port)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)

    def __enter__(self):
        """Starts the server."""
        self.thread.start()
        return self

    def __exit__(self, *_):
        """Stops the server."""
        self.server.shutdown()
        self.server.server_close()


class UpdateGradleWrapperTests(unittest.TestCase):
    """The tool's bump transaction, each case against its own fixture repository."""

    def setUp(self):
        """Builds a temporary repository carrying the four wrapper files."""
        self.outputs = []
        self.directory = tempfile.TemporaryDirectory()
        self.root = Path(self.directory.name)
        (self.root / "gradle" / "wrapper").mkdir(parents=True)
        self.properties = self.root / "gradle" / "wrapper" / "gradle-wrapper.properties"
        self.properties.write_text(PROPERTIES_BODY.format(checksum=CHECKSUM), encoding="utf-8")
        for name in ("gradle-wrapper.jar", "gradlew", "gradlew.bat"):
            path = self.root / "gradle" / "wrapper" / name if name.endswith(".jar") else self.root / name
            path.write_text("original\n", encoding="utf-8")
            path.chmod(0o755 if not name.endswith(".bat") else 0o644)

    def tearDown(self):
        """Removes the temporary repository."""
        self.directory.cleanup()

    def gradlew(self, body):
        """Writes the fixture `gradlew` and returns its path.

        :param body: the script's content
        :return: the script's path
        """
        script = self.root / "gradlew"
        script.write_text(body, encoding="utf-8")
        script.chmod(0o755)
        return script

    def run_tool(self, *arguments):
        """Runs the tool's `main` with the given arguments.

        :param arguments: the command line arguments after the program name
        :return: the exit status and the captured output
        """
        saved = sys.argv
        sys.argv = ["update-gradle-wrapper.py"] + list(arguments)
        output = io.StringIO()
        try:
            with contextlib.redirect_stdout(output), contextlib.redirect_stderr(io.StringIO()):
                status = update_gradle_wrapper.main()
        except BaseException:
            self.outputs.append(output.getvalue())
            raise
        finally:
            sys.argv = saved
        self.outputs.append(output.getvalue())
        return status, output.getvalue()

    def test_a_successful_bump_writes_the_version_and_checksum(self):
        """The happy path pins the new version and its checksum."""
        self.gradlew(HAPPY_GRADLEW)
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(0, status, output)
        body = self.properties.read_text(encoding="utf-8")
        self.assertIn("gradle-9.8.1-all.zip", body)
        self.assertIn("distributionSha256Sum=" + CHECKSUM, body)

    def test_a_version_is_required(self):
        """A missing --version is a usage error, never a resolved default."""
        with self.assertRaises(SystemExit):
            self.run_tool("--root", str(self.root))

    def test_a_failed_second_pass_restores_the_capture(self):
        """A reader left half-rewritten by the first pass comes back byte-for-byte."""
        self.gradlew(FAILING_GRADLEW)
        before = self.properties.read_bytes()
        jar = self.root / "gradle" / "wrapper" / "gradle-wrapper.jar"
        jar.chmod(0o750)
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(1, status)
        self.assertIn("were restored", output)
        self.assertEqual(before, self.properties.read_bytes())
        self.assertEqual(0o750, stat.S_IMODE(jar.stat().st_mode))

    def test_a_half_rewritten_result_restores_the_capture(self):
        """A bump that writes only the URL is incomplete and is rolled back."""
        self.gradlew(INCOMPLETE_GRADLEW)
        before = self.properties.read_bytes()
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(1, status)
        self.assertIn("did not complete", output)
        self.assertEqual(before, self.properties.read_bytes())

    def test_a_malformed_checksum_stops_before_touching_a_file(self):
        """A response that is not a checksum stops the run with the files untouched."""
        self.gradlew(HAPPY_GRADLEW)
        before = self.properties.read_bytes()
        with ChecksumServer("<html>not found</html>") as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(1, status)
        self.assertIn("not 64 hexadecimal characters", output)
        self.assertIn("No wrapper file was touched", output)
        self.assertEqual(before, self.properties.read_bytes())

    def test_an_empty_checksum_body_stops_the_run(self):
        """An empty response is refused rather than treated as a checksum."""
        self.gradlew(HAPPY_GRADLEW)
        before = self.properties.read_bytes()
        with ChecksumServer("") as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(1, status)
        self.assertIn("is empty", output)
        self.assertIn("No wrapper file was touched", output)
        self.assertEqual(before, self.properties.read_bytes())

    def test_a_wrong_checksum_path_is_refused(self):
        """A source that does not serve the release's own path is refused, not accepted."""
        self.gradlew(HAPPY_GRADLEW)
        before = self.properties.read_bytes()
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.2", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(1, status)
        self.assertIn("could not be fetched", output)
        self.assertIn("No wrapper file was touched", output)
        self.assertEqual(before, self.properties.read_bytes())

    def test_a_same_target_rerun_leaves_the_wrapper_unchanged(self):
        """Re-running against the version already pinned succeeds and changes nothing."""
        self.gradlew(HAPPY_GRADLEW)
        before = self.properties.read_bytes()
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )
        self.assertEqual(0, status, output)
        first = self.properties.read_bytes()
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(0, status, output)
        self.assertEqual(first, self.properties.read_bytes())
        self.assertNotEqual(before, first)

    def test_an_unreachable_checksum_source_stops_the_run(self):
        """A refused connection is reported, not treated as a checksum."""
        self.gradlew(HAPPY_GRADLEW)
        before = self.properties.read_bytes()

        status, output = self.run_tool(
            "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
            "--checksum-url", "http://127.0.0.1:9/gradle-{version}-all.zip.sha256",
        )

        self.assertEqual(1, status)
        self.assertIn("could not be fetched", output)
        self.assertIn("No wrapper file was touched", output)
        self.assertEqual(before, self.properties.read_bytes())

    def test_an_interruption_restores_the_files_and_removes_the_snapshot(self):
        """A cancelled run restores what it had begun to rewrite; a completed restore leaves no snapshot behind."""
        self.gradlew(HAPPY_GRADLEW)
        before = self.properties.read_bytes()
        with ChecksumServer(CHECKSUM) as server:
            with unittest.mock.patch(
                "__main__.update_gradle_wrapper.subprocess.run",
                side_effect=self.interrupt_after_rewriting,
            ):
                with self.assertRaises(KeyboardInterrupt):
                    self.run_tool(
                        "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                        "--checksum-url", server.url,
                    )

        self.assertEqual(before, self.properties.read_bytes())
        snapshot = self.last_snapshot()
        self.assertTrue(
            any("were restored" in line for line in self.outputs[-1].splitlines()),
            "the interruption reports the restore it performed",
        )
        self.assertFalse(
            Path(snapshot).exists(),
            "a completed restore removes the snapshot it no longer needs",
        )

    def test_an_incomplete_restore_keeps_the_snapshot_and_says_so(self):
        """A restore that cannot finish keeps the captured bytes and reports the recovery material."""
        self.gradlew(FAILING_GRADLEW)
        before = self.properties.read_bytes()
        original_copy = __import__("shutil").copy2

        def copy_or_fail(source, target, *args, **kwargs):
            if Path(target).resolve() == self.properties.resolve():
                raise OSError("the disk is read-only")
            return original_copy(source, target, *args, **kwargs)

        with ChecksumServer(CHECKSUM) as server:
            with unittest.mock.patch("__main__.update_gradle_wrapper.shutil.copy2", side_effect=copy_or_fail):
                status, output = self.run_tool(
                    "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                    "--checksum-url", server.url,
                )

        self.assertEqual(1, status)
        self.assertIn("the restore is incomplete", output)
        snapshot = self.last_snapshot()
        self.assertTrue(Path(snapshot).is_dir(), "the snapshot survives an incomplete restore")
        self.assertTrue((Path(snapshot) / "gradle" / "wrapper" / "gradle-wrapper.properties").is_file())
        self.assertNotEqual(before, self.properties.read_bytes())

    def test_the_production_checksum_url_is_the_all_archive_one(self):
        """Without --checksum-url the tool asks the distribution service for the release's `-all.zip` checksum."""
        self.gradlew(HAPPY_GRADLEW)
        response = unittest.mock.MagicMock()
        response.__enter__ = unittest.mock.MagicMock(return_value=response)
        response.__exit__ = unittest.mock.MagicMock(return_value=False)
        response.read.return_value = CHECKSUM.encode("utf-8")
        target = "__main__.update_gradle_wrapper.urllib.request.urlopen"
        with unittest.mock.patch(target, return_value=response) as opened:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew")
            )

        self.assertEqual(0, status, output)
        self.assertEqual(
            "https://services.gradle.org/distributions/gradle-9.8.1-all.zip.sha256",
            opened.call_args[0][0],
        )

    def interrupt_after_rewriting(self, *_args, **_kwargs):
        """Rewrites the properties and then cancels the run, as a Ctrl-C during a pass would.

        :raises KeyboardInterrupt: always, after the rewrite
        """
        self.properties.write_text("distributionUrl=https\\://example/gradle-9.8.1-all.zip\n", encoding="utf-8")
        raise KeyboardInterrupt()

    def last_snapshot(self):
        """Returns the snapshot directory the last run printed.

        :return: the path the tool reported
        """
        for line in reversed(self.outputs[-1].splitlines()):
            if line.startswith("Wrapper snapshot: "):
                path = line[len("Wrapper snapshot: "):].strip()
                self.addCleanup(__import__("shutil").rmtree, path, True)
                return path
        self.fail("the run printed no snapshot directory")

    def test_a_capture_failure_announces_no_snapshot_and_leaves_none(self):
        """A missing wrapper file stops the run before a snapshot is announced or a pass runs."""
        self.gradlew(HAPPY_GRADLEW)
        (self.root / "gradlew.bat").unlink()
        before = {path for path in Path(tempfile.gettempdir()).glob("wrapper-bump-*")}
        with ChecksumServer(CHECKSUM) as server:
            status, output = self.run_tool(
                "--version", "9.8.1", "--root", str(self.root), "--gradlew", str(self.root / "gradlew"),
                "--checksum-url", server.url,
            )

        self.assertEqual(1, status)
        self.assertIn("is missing", output)
        self.assertIn("No wrapper file was touched", output)
        self.assertNotIn("Wrapper snapshot:", output)
        after = {path for path in Path(tempfile.gettempdir()).glob("wrapper-bump-*")}
        self.assertEqual(set(), after - before, "the failed capture leaves no snapshot directory")

    def test_a_release_candidate_is_refused(self):
        """A non-stable label is refused before any fetch."""
        self.gradlew(HAPPY_GRADLEW)

        status, output = self.run_tool("--version", "9.9-rc-1", "--root", str(self.root))

        self.assertEqual(1, status)
        self.assertIn("not a stable Gradle release version", output)
        self.assertIn("No wrapper file was touched", output)


if __name__ == "__main__":
    unittest.main()
