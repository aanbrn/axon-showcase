import { tmpdir } from "node:os"

const SCRATCH_DIR_NAME = "opencode"

export default async () => ({
  config: (cfg: { permission?: Record<string, unknown> }) => {
    const base = tmpdir().replace(/\/+$/, "")
    const dir = `${base}/${SCRATCH_DIR_NAME}`

    cfg.permission ??= {}
    cfg.permission.external_directory = {
      ...((cfg.permission.external_directory as Record<string, string>) ?? {}),
      [`${dir}/**`]: "allow",
    }
  },
})
