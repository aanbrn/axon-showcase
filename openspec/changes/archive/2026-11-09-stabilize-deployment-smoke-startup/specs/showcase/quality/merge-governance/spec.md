# Spec Delta

## MODIFIED Requirements

### Requirement: A deployment smoke runs on a schedule and on demand

The repository SHALL run a deployment smoke automatically on a nightly schedule and be manually triggerable, as its own
job in a dedicated workflow separate from the merge gate. The smoke SHALL create a Kubernetes cluster in the runner,
build the five images — the four service images and the web UI image — and load them into that cluster, install the
application and its infrastructure releases — not the observability — with values whose requests and startup-probe
budgets fit the runner, since larger runners are not available to this repository, install an ingress controller and
enable the deployment's ingress, wait for them to become ready and for the gateway's live-event stream to deliver an
event, then drive the smoke profile against the deployed gateway through that ingress, assert that no request fails,
record no performance numbers, delete the cluster even when a step fails, and SHALL NOT be part of the merge-gate
`build` check or a required check for merging into `main`.

#### Scenario: Nightly schedule triggers the deployment smoke

- **WHEN** the scheduled nightly trigger fires
- **THEN** the smoke job creates a Kubernetes cluster in the runner, installs the application, infrastructure, and
  ingress releases with values that fit it, and drives the smoke profile against the deployed gateway

#### Scenario: The smoke reaches the gateway through the deployment's ingress

- **WHEN** the smoke drives its load
- **THEN** it targets the deployment's ingress hostname with an ingress controller serving in the cluster, rather than a
  port-forward

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

#### Scenario: The smoke waits for the live-event stream before driving the load

- **WHEN** the smoke has installed the releases and the gateway answers its requests
- **THEN** it confirms the gateway's live-event stream delivers an event before it drives the load, so a live-event
  check does not fail because the stream's subscription had not attached when the load published its events

#### Scenario: A slow cold start does not restart a service

- **WHEN** a service on the runner takes longer to start than the chart's default startup-probe budget allows
- **THEN** the `ci` values give it a budget that covers the cold start, so the install waits for the service instead of
  the startup probe restarting the container
