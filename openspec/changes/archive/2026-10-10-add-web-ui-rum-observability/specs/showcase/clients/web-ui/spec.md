# Spec Delta

## ADDED Requirements

### Requirement: Client-side experience measurement and reporting

The UI SHALL measure the Core Web Vitals (LCP, INP, CLS, FCP, TTFB) and capture uncaught JavaScript errors and unhandled
promise rejections, and SHALL report them to the gateway's telemetry endpoint tagged with the current route. Reporting
SHALL be fire-and-forget and SHALL NOT degrade or block the page. (The report's trace context is owned by the
"Trace-context propagation on API calls" requirement, which covers every fetch-based gateway request.)

#### Scenario: A measured vital is reported

- **WHEN** the browser reports a Core Web Vital for the page
- **THEN** the UI sends the vital to the gateway telemetry endpoint

#### Scenario: A client-side error is reported

- **WHEN** an uncaught JavaScript error or an unhandled promise rejection occurs
- **THEN** the UI reports it to the gateway telemetry endpoint

#### Scenario: A failed report leaves the page working

- **WHEN** the telemetry request fails or is unavailable
- **THEN** the page continues to work without a user-visible error
