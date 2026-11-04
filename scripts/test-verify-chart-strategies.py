#!/usr/bin/env python3
"""Tests for the chart strategy guard (`scripts/verify-chart-strategies.py`). Stdlib only.

Run with `python3 scripts/test-verify-chart-strategies.py`. The tool's kebab-case filename is not an importable
identifier, so it is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

The assertions run over fixture render trees rather than the chart's own, so `check` exercises them without Helm.
"""

import importlib.util
import io
import tempfile
import unittest
from contextlib import redirect_stderr
from pathlib import Path

TOOL_PATH = Path(__file__).resolve().parent / "verify-chart-strategies.py"
_spec = importlib.util.spec_from_file_location("verify_chart_strategies", TOOL_PATH)
guard = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(guard)

RECREATE = (
    "apiVersion: apps/v1\nkind: Deployment\nspec:\n  strategy:\n    type: Recreate\n    rollingUpdate: null\n"
)
ROLLING = (
    "apiVersion: apps/v1\nkind: Deployment\nspec:\n  strategy:\n    type: RollingUpdate\n"
    "    rollingUpdate:\n      maxUnavailable: 0\n"
)


def render(root: Path, strategy: str, body: dict | None = None) -> Path:
    """Write a fixture render tree for one strategy, one Deployment per service."""
    chart = root / strategy / "axon-showcase"
    for service, content in (body or {}).items():
        path = chart / "templates" / service / "deployment.yaml"
        path.parent.mkdir(parents=True)
        path.write_text(content, encoding="utf-8")
    return root / strategy


def complete(root: Path) -> tuple[Path, Path]:
    """A pair of renders where every Deployment carries its strategy's expectation."""
    recreate = render(root, "recreate", {service: RECREATE for service in guard.SERVICES})
    rolling = render(root, "rollingupdate", {service: ROLLING for service in guard.SERVICES})
    return recreate, rolling


class ViolationTests(unittest.TestCase):
    """The assertions discriminate both directions and name the Deployment."""

    def _tree(self) -> Path:
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        return Path(directory.name)

    def test_a_guarded_pair_passes(self) -> None:
        self.assertEqual(guard.violations(*complete(self._tree())), [])

    def test_a_missing_guard_fails(self) -> None:
        root = self._tree()
        recreate = render(root, "recreate", {service: RECREATE for service in guard.SERVICES})
        path = recreate / "axon-showcase" / "templates" / "command-service" / "deployment.yaml"
        path.write_text("spec:\n  strategy:\n    type: Recreate\n", encoding="utf-8")
        rolling = render(root, "rollingupdate", {service: ROLLING for service in guard.SERVICES})
        found = guard.violations(recreate, rolling)
        self.assertEqual(len(found), 1)
        self.assertIn("command-service", found[0])
        self.assertIn("does not set rollingUpdate to null", found[0])

    def test_a_stray_guard_under_rolling_update_fails(self) -> None:
        root = self._tree()
        recreate = render(root, "recreate", {service: RECREATE for service in guard.SERVICES})
        bodies = {service: ROLLING for service in guard.SERVICES}
        bodies["web-ui"] = RECREATE
        rolling = render(root, "rollingupdate", bodies)
        found = guard.violations(recreate, rolling)
        self.assertTrue(any("web-ui" in line and "rollingUpdate: null" in line for line in found), found)

    def test_an_omitted_deployment_fails(self) -> None:
        root = self._tree()
        recreate = render(root, "recreate", {service: RECREATE for service in guard.SERVICES})
        bodies = {service: ROLLING for service in guard.SERVICES}
        del bodies["query-service"]
        rolling = render(root, "rollingupdate", bodies)
        with self.assertRaises(OSError):
            guard.violations(recreate, rolling)

    def test_a_wrong_strategy_type_fails(self) -> None:
        root = self._tree()
        recreate = render(root, "recreate", {service: RECREATE for service in guard.SERVICES})
        rolling = render(root, "rollingupdate", {service: ROLLING for service in guard.SERVICES})
        path = rolling / "axon-showcase" / "templates" / "api-gateway" / "deployment.yaml"
        path.write_text("spec:\n  strategy:\n    type: Recreate\n", encoding="utf-8")
        found = guard.violations(recreate, rolling)
        self.assertTrue(any("api-gateway" in line and "sets type 'Recreate'" in line for line in found), found)

    def test_a_commented_guard_fails(self) -> None:
        root = self._tree()
        body = "spec:\n  strategy:\n    type: Recreate\n    # rollingUpdate: null\n"
        recreate = render(root, "recreate", {service: body for service in guard.SERVICES})
        rolling = render(root, "rollingupdate", {service: ROLLING for service in guard.SERVICES})
        found = guard.violations(recreate, rolling)
        self.assertTrue(
            any("command-service" in line and "does not set rollingUpdate" in line for line in found), found
        )

    def test_a_guard_outside_the_strategy_mapping_fails(self) -> None:
        root = self._tree()
        body = "spec:\n  strategy:\n    type: Recreate\n    rollingUpdate:\n      null: true\n"
        recreate = render(root, "recreate", {service: body for service in guard.SERVICES})
        rolling = render(root, "rollingupdate", {service: ROLLING for service in guard.SERVICES})
        found = guard.violations(recreate, rolling)
        self.assertTrue(any("query-service" in line and "does not set rollingUpdate" in line for line in found), found)

    def test_a_render_with_two_chart_directories_is_a_violation(self) -> None:
        root = self._tree()
        recreate, rolling = complete(root)
        (recreate / "second-chart").mkdir()
        with redirect_stderr(io.StringIO()):
            self.assertEqual(guard.main([str(recreate), str(rolling)]), 1)


if __name__ == "__main__":
    unittest.main()
