#!/bin/bash
set -euo pipefail

# Resolves the local cluster's ingress-controller LoadBalancer address (IP or hostname, detected generically
# against the current kube context: colima + Traefik, kind/minikube + ingress-nginx, ...) and manages the
# /etc/hosts entries for the deployed ingress hostnames (axon-showcase-api, axon-showcase-ui), so the API gateway
# and web UI are reachable by hostname instead of a Host-header curl workaround.
# Usage: ./setup-hosts.sh [setup|remove]  (requires a running local cluster and sudo for /etc/hosts).

HOSTNAMES="axon-showcase-api axon-showcase-ui"
MARKER="axon-showcase ingress (managed by setup-hosts.sh)"

usage() {
  cat <<'EOF'
Manage the local /etc/hosts entries for the deployed ingress hostnames.

Usage:
  ./setup-hosts.sh setup    Resolve the ingress-controller LoadBalancer address and add/replace the hosts entries
  ./setup-hosts.sh remove   Remove the managed hosts entries

The ingress-controller Service is found generically across namespaces in the current kube context
(colima + Traefik, kind/minikube + ingress-nginx, ...). Requires kubectl (local cluster running) and sudo.
EOF
}

# The ingress controller's LoadBalancer address (IP or hostname), or empty if it cannot be determined.
ingress_address() {
  kubectl get svc -A -o json | python3 -c '
import json, sys
data = json.load(sys.stdin)
lbs = [s for s in data.get("items", []) if s.get("spec", {}).get("type") == "LoadBalancer"]
def is_ingress(s):
    labels = s.get("metadata", {}).get("labels", {})
    return "ingress" in (labels.get("app.kubernetes.io/component", "")
                         or labels.get("app.kubernetes.io/name", "")
                         or s.get("metadata", {}).get("name", "")).lower()
lbs = [s for s in lbs if is_ingress(s)] or lbs
if len(lbs) != 1:
    sys.exit(1)
for addr in lbs[0].get("status", {}).get("loadBalancer", {}).get("ingress", []):
    if addr.get("ip") or addr.get("hostname"):
        print(addr.get("ip") or addr.get("hostname"))
        sys.exit(0)
sys.exit(1)
' 2>/dev/null || true
}

# Drop the managed block (appended at the end of /etc/hosts), portably (awk, not BSD-only sed -i).
drop_managed() {
  sudo awk -v marker="# $MARKER" 'index($0, marker) == 1 { exit } { print }' /etc/hosts
}

setup() {
  local addr
  addr="$(ingress_address)"
  if [ -z "$addr" ]; then
    echo "Could not determine the ingress-controller LoadBalancer address (is the local cluster running and is an" >&2
    echo "ingress controller deployed? See setup-hosts.sh usage)." >&2
    exit 1
  fi
  echo "Ingress LoadBalancer address: $addr"
  echo "Adding /etc/hosts entries for: $HOSTNAMES"
  sudo -v
  drop_managed > /tmp/axon-showcase-hosts.new
  sudo mv /tmp/axon-showcase-hosts.new /etc/hosts
  sudo sh -c "cat >> /etc/hosts <<EOF

# ${MARKER}
${addr} ${HOSTNAMES}
EOF"
  echo "Done. The address may change on cluster restart; re-run './setup-hosts.sh setup' to refresh."
}

remove() {
  echo "Removing managed /etc/hosts entries"
  sudo -v
  drop_managed > /tmp/axon-showcase-hosts.new
  sudo mv /tmp/axon-showcase-hosts.new /etc/hosts
  echo "Done."
}

case "${1:-}" in
  setup) setup ;;
  remove) remove ;;
  *) usage; exit 1 ;;
esac