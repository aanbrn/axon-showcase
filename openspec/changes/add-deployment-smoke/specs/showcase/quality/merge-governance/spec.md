## ADDED Requirements

### Requirement: A deployment smoke runs on a schedule and on demand

The repository SHALL run a deployment smoke automatically on a nightly schedule and be manually triggerable, as its own
job in a dedicated workflow separate from the merge gate. The smoke SHALL create a Kubernetes cluster in the runner,
build the five images — the four service images and the web UI image — and load them into that cluster, install all six
Helm releases through the documented local install path, wait for them to become ready, drive the load profiles against
the deployed gateway over a port-forward, assert that no request fails, record no performance numbers, delete the
cluster even when a step fails, and SHALL NOT be part of the merge-gate `build` check or a required check for merging
into `main`.

#### Scenario: Nightly schedule triggers the deployment smoke

- **WHEN** the scheduled nightly trigger fires
- **THEN** the smoke job creates a Kubernetes cluster in the runner, installs the six releases through the local install
  path, and drives the load profiles against the deployed gateway

#### Scenario: Manual trigger runs the deployment smoke

- **WHEN** a maintainer dispatches the deployment-smoke workflow manually
- **THEN** the smoke job runs the same cluster install and load smoke against the current `main`

#### Scenario: The deployment smoke is not a merge gate

- **WHEN** a pull request or a push to `main` is evaluated for merging
- **THEN** the smoke run is not required, because it is not part of the merge-gate `build` check and no ruleset requires
  it

#### Scenario: A failed smoke leaves no cluster behind

- **WHEN** any step of the smoke fails
- **THEN** the cluster is deleted before the job ends
