import { execFileSync } from "node:child_process"
import { realpathSync, existsSync, readFileSync } from "node:fs"
import { dirname, join } from "node:path"
import { tmpdir } from "node:os"

const SCRATCH_DIR_NAME = "opencode"
const OPENSPEC_PACKAGE_NAME = "@fission-ai/openspec"

/**
 * Resolves the installed `@fission-ai/openspec` package root from the `openspec` binary on PATH.
 *
 * The CLI reads its schema templates from its own package directory, which lives outside the workspace (e.g.
 * `/usr/local/lib/node_modules/@fission-ai/openspec` on a CI runner, a Homebrew Cellar path locally). An unattended run
 * cannot answer the resulting `external_directory` prompt, so the path must be granted up front — and it differs per
 * machine, so it is resolved rather than hard-coded. Returns null when `openspec` is not on PATH or the resolution
 * fails, so the plugin still loads on a machine without it.
 */
function resolveOpenspecPackageRoot(): string | null {
  try {
    const binary = execFileSync("which", ["openspec"], { encoding: "utf8" }).trim()
    let dir = dirname(realpathSync(binary))
    while (dir !== "/" && !existsSync(join(dir, "package.json"))) {
      dir = dirname(dir)
    }
    const pkg = JSON.parse(readFileSync(join(dir, "package.json"), "utf8"))
    return pkg.name === OPENSPEC_PACKAGE_NAME ? dir : null
  } catch {
    return null
  }
}

export default async () => ({
  config: (cfg: { permission?: Record<string, unknown> }) => {
    const base = tmpdir().replace(/\/+$/, "")
    const scratchDir = `${base}/${SCRATCH_DIR_NAME}`
    const openspecRoot = resolveOpenspecPackageRoot()

    cfg.permission ??= {}
    cfg.permission.external_directory = {
      ...((cfg.permission.external_directory as Record<string, string>) ?? {}),
      [`${scratchDir}/**`]: "allow",
      ...(openspecRoot ? { [`${openspecRoot}/**`]: "allow" } : {}),
    }
  },
})
