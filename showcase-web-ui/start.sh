#!/bin/sh
# Renders the runtime API base URL into config.js from the SHOWCASE_API_BASE_URL env var, then starts nginx.
# Runs as the container's `web` process (overriding the Paketo nginx buildpack's default) via the Procfile.
# The base URL is mandatory: the browser needs the externally-visible gateway URL, so the container fails fast
# rather than serve a UI that silently calls the wrong origin.
set -e

BASE_URL="${SHOWCASE_API_BASE_URL:-}"
if [ -z "$BASE_URL" ]; then
    echo "SHOWCASE_API_BASE_URL is required: the browser needs the externally-visible gateway URL." >&2
    exit 1
fi
printf 'window.__API_BASE_URL__ = "%s";\n' "$BASE_URL" > /workspace/config.js

exec nginx -p /workspace -c /workspace/nginx.conf -g "pid /tmp/nginx.pid;"