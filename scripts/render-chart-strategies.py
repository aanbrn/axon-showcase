#!/usr/bin/env python3
"""Render the chart with every service on one update strategy, so the strategy guard's output can be asserted.

A Kubernetes Deployment that sets `strategy.type: Recreate` while the live release still carries
`strategy.rollingUpdate` fails the next upgrade: a server-side apply that changes only the type keeps that block, and
the API rejects the pair. The chart's guard renders `rollingUpdate: null` beside the type for a Helm-owned transition.

The CLI is resolved in this order — `$HELM_EXECUTABLE`, `helm` on `PATH`, then the Gradle Helm plugin's downloaded
client under `.gradle/helm/client/<version>/<platform>/helm` — so the check needs no host Helm: the plugin the chart
module already uses extracts its client during packaging.

Usage: `render-chart-strategies.py <chart-dir> <output-root> <strategy> [--client-version <version>]`, which writes
`<output-root>/<strategy>/`. With `--client-version`, the plugin's client at exactly that version is preferred, so the
render uses the CLI the catalog pins rather than whichever client is first on `PATH`.
Exit status: 0 rendered, 1 the render failed, 2 a usage error, 3 no Helm client resolved.
"""

import os
import shutil
import subprocess
import sys
from pathlib import Path

SERVICES = ("projectionService", "commandService", "queryService", "apiGateway", "webUi")
PLUGIN_CLIENT_GLOB = ".gradle/helm/client/*/*/helm"


def version_key(path: Path) -> tuple[int, ...]:
    """The plugin cache's version segment as numbers, so `4.10.0` sorts after `4.9.0`."""
    return tuple(int(part) for part in path.parent.parent.name.split(".") if part.isdigit())


def resolve_helm(repo: Path, pinned_version: str | None) -> str | None:
    """The Helm CLI: the plugin's client at the pinned version, the environment's, PATH, else the newest cached one."""
    if pinned_version:
        pinned = sorted(repo.glob(".gradle/helm/client/{0}/*/helm".format(pinned_version)))
        if pinned:
            return str(pinned[-1])
    configured = os.environ.get("HELM_EXECUTABLE")
    if configured and os.access(configured, os.X_OK):
        return configured
    found = shutil.which("helm")
    if found:
        return found
    cached = sorted(repo.glob(PLUGIN_CLIENT_GLOB), key=version_key)
    return str(cached[-1]) if cached else None


def render(helm: str, chart: Path, output: Path, strategy: str) -> subprocess.CompletedProcess[str]:
    """Render `chart` with every service on `strategy`, writing one file per template under `output`."""
    arguments = [helm, "template", str(chart), "--output-dir", str(output)]
    for service in SERVICES:
        arguments += ["--set", "{0}.updateStrategy.type={1}".format(service, strategy)]
    return subprocess.run(arguments, capture_output=True, text=True, check=False)


def main(argv: list[str] | None = None) -> int:
    raw = list(sys.argv[1:] if argv is None else argv)
    pinned_version = None
    if "--client-version" in raw:
        index = raw.index("--client-version")
        try:
            pinned_version = raw[index + 1]
        except IndexError:
            print("render-chart-strategies: --client-version needs a value", file=sys.stderr)
            return 2
        del raw[index : index + 2]
    arguments = raw
    if len(arguments) != 3:
        print(__doc__.splitlines()[0], file=sys.stderr)
        return 2
    chart, output_root, strategy = Path(arguments[0]), Path(arguments[1]), arguments[2]
    if strategy not in ("Recreate", "RollingUpdate"):
        print("render-chart-strategies: unknown strategy '{0}'".format(strategy), file=sys.stderr)
        return 2
    repository = Path(__file__).resolve().parent.parent
    helm = resolve_helm(repository, pinned_version)
    if helm is None:
        print(
            "render-chart-strategies: no Helm client resolved (env, PATH, or the plugin's cache)",
            file=sys.stderr,
        )
        return 3
    output = output_root / strategy.lower()
    if output.exists():
        shutil.rmtree(output)
    result = render(helm, chart, output, strategy)
    if result.returncode != 0:
        print(result.stdout, file=sys.stderr)
        print(result.stderr, file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
