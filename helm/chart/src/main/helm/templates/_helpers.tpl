{{- /*
Renders environment variables for a container from a map or slice of values.
Expected dot: .envVars (map of name -> value or name -> { value, valueFrom, ... }) or slice of env entries,
and .context (root context used to render templated values).
*/ -}}
{{- define "axon-showcase.renderEnvVars" }}
    {{- if not (empty .envVars) }}
        {{- if kindIs "map" .envVars }}
            {{- $context := .context }}
            {{- range $key, $value := .envVars }}
                {{- /* @formatter:off */}}
- name: {{ $key | quote }}
                {{- /* @formatter:on */}}
                {{- if kindIs "string" $value }}
                    {{- /* @formatter:off */}}
  value: {{ include "common.tplvalues.render" (dict "value" $value "context" $context) | quote }}
                    {{- /* @formatter:on */}}
                {{- else if kindIs "map" $value }}
                    {{- include "common.tplvalues.render" (dict "value" (omit $value "name") "context" $context) | nindent 2 }}
                {{- end }}
            {{- end }}
        {{- else if kindIs "slice" .envVars }}
            {{- include "common.tplvalues.render" (dict "value" .envVars "context" .context) }}
        {{- end }}
    {{- end }}
{{- end }}

{{- /*
Computes the image pull policy for a container.
Uses the explicitly configured pullPolicy, defaulting to Always for "latest" tags and IfNotPresent otherwise.
Expected dot: .imageRoot (image dict) and .context.
*/ -}}
{{- define "axon-showcase.images.imagePullPolicy" }}
    {{- $imageRoot := include "common.tplvalues.render" (dict "value" .imageRoot "context" .context ) | fromYaml }}
    {{- $imageRoot.pullPolicy | default (eq $imageRoot.tag "latest" | ternary "Always" "IfNotPresent") }}
{{- end }}

{{- /*
Returns true when at least one observability feature (structured logging, Prometheus or OTLP export) is enabled.
Used to add observability annotations (e.g. micrometer tags, OTLP) to pods.
*/ -}}
{{- define "axon-showcase.observability.enabled" }}
    {{- /* @formatter:off */}}
    {{- if or
           .Values.observability.logging.structured.enabled
           .Values.observability.prometheus.metrics.export.enabled
           .Values.observability.otlp.metrics.export.enabled
           .Values.observability.otlp.tracing.export.enabled
    }}
        {{- true }}
    {{- end }}
    {{- /* @formatter:on */}}
{{- end }}

{{- /*
Returns true when OTLP export (metrics or tracing) is enabled at all.
Used to allow OTLP ports in NetworkPolicies.
*/ -}}
{{- define "axon-showcase.observability.otlp.export.enabled" }}
    {{- if or .Values.observability.otlp.metrics.export.enabled .Values.observability.otlp.tracing.export.enabled }}
        {{- true }}
    {{- end }}
{{- end }}

{{- /*
Renders the environment variables shared by all services to configure observability
(structured logging, sampling probability, Prometheus and OTLP metrics/traces export).
Fails with a descriptive message when a feature is enabled without its required settings.
*/ -}}
{{- define "axon-showcase.observability.renderEnvVars" }}
    {{- if .Values.observability.logging.structured.enabled }}
        {{- if empty .Values.observability.logging.structured.format }}
            {{- fail "\".Values.observability.logging.structured.format\" is required to enable structured logging" }}
        {{- end }}
        {{- /* @formatter:off */}}
- name: "LOGGING_STRUCTURED_FORMAT_CONSOLE"
  value: {{ .Values.observability.logging.structured.format | quote }}
        {{- /* @formatter:on */}}
    {{- end }}
    {{- /* @formatter:off */}}
- name: "MANAGEMENT_TRACING_SAMPLING_PROBABILITY"
  value: {{ .Values.observability.tracing.sampling.probability | float64 | toString | quote }}
    {{- /* @formatter:on */}}
    {{- if .Values.observability.prometheus.metrics.export.enabled }}
        {{- /* @formatter:off */}}
- name: "MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED"
  value: "true"
        {{- /* @formatter:on */}}
    {{- end }}
    {{- if .Values.observability.otlp.metrics.export.enabled }}
        {{- if empty .Values.observability.otlp.metrics.export.endpoint }}
            {{- fail "\".Values.observability.otlp.metrics.export.endpoint\" is required to enable export of metrics" }}
        {{- end }}
        {{- /* @formatter:off */}}
        {{- if not (or (hasPrefix "http://" .Values.observability.otlp.metrics.export.endpoint)
                       (hasPrefix "https://" .Values.observability.otlp.metrics.export.endpoint)) }}
            {{- fail "\".Values.observability.otlp.metrics.export.endpoint\" must use protocol HTTP or HTTPS only" }}
        {{- end }}
        {{- /* @formatter:on */}}
        {{- if not (hasSuffix ":4318" .Values.observability.otlp.metrics.export.endpoint) }}
            {{- fail "\".Values.observability.otlp.metrics.export.endpoint\" must use port 4318 only" }}
        {{- end }}
        {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED"
  value: "true"
        {{- /* @formatter:on */}}
        {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_EXPORT_METRICS_EXPORT_URL"
  value: {{ print .Values.observability.otlp.metrics.export.endpoint "/v1/metrics" | quote }}
        {{- /* @formatter:on */}}
        {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_EXPORT_METRICS_EXPORT_STEP"
  value: {{ .Values.observability.otlp.metrics.export.step | quote }}
        {{- /* @formatter:on */}}
    {{- end }}
    {{- if .Values.observability.otlp.tracing.export.enabled }}
        {{- if empty .Values.observability.otlp.tracing.export.endpoint }}
            {{- fail "\".Values.observability.tracing.export.endpoint\" is required to enable export of tracing" }}
        {{- end }}
        {{- /* @formatter:off */}}
        {{- if not (or (hasPrefix "http://" .Values.observability.otlp.tracing.export.endpoint)
                       (hasPrefix "https://" .Values.observability.otlp.tracing.export.endpoint)) }}
            {{- fail "\".Values.observability.otlp.tracing.export.endpoint\" must use protocol HTTP or HTTPS only" }}
        {{- end }}
        {{- /* @formatter:on */}}
        {{- /* @formatter:off */}}
        {{- if not (or (hasSuffix ":4317" .Values.observability.otlp.tracing.export.endpoint)
                       (hasSuffix ":4318" .Values.observability.otlp.tracing.export.endpoint)) }}
            {{- fail "\".Values.observability.otlp.tracing.export.endpoint\" must use port 4317 or 4318 only" }}
        {{- end }}
        {{- /* @formatter:on */}}
        {{- if empty .Values.observability.otlp.tracing.export.compression }}
            {{- fail "\".Values.observability.tracing.export.compression\" is required to enable export of tracing" }}
        {{- end }}
        {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_TRACING_EXPORT_ENABLED"
  value: "true"
        {{- /* @formatter:on */}}
        {{- if hasSuffix ":4317" .Values.observability.otlp.tracing.export.endpoint }}
            {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_TRACING_ENDPOINT"
  value: {{ .Values.observability.otlp.tracing.export.endpoint | quote }}
- name: "MANAGEMENT_OTLP_TRACING_TRANSPORT"
  value: "grpc"
            {{- /* @formatter:on */}}
        {{- else }}
            {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_TRACING_ENDPOINT"
  value: {{ print .Values.observability.otlp.tracing.export.endpoint "/v1/traces" | quote }}
- name: "MANAGEMENT_OTLP_TRACING_TRANSPORT"
  value: "http"
            {{- /* @formatter:on */}}
        {{- end }}
        {{- /* @formatter:off */}}
- name: "MANAGEMENT_OTLP_TRACING_COMPRESSION"
  value: {{ .Values.observability.otlp.tracing.export.compression | quote }}
        {{- /* @formatter:on */}}
    {{- end }}
{{- end }}

{{- /*
Returns the ServiceAccount name to reference from pods:
the configured name when a dedicated ServiceAccount is created, otherwise the configured name or "default".
*/ -}}
{{- define "axon-showcase.serviceAccountName" }}
    {{- if .Values.serviceAccount.create }}
        {{- .Values.serviceAccount.name | default (include "common.names.fullname" $) }}
    {{- else }}
        {{- .Values.serviceAccount.name | default "default" }}
    {{- end }}
{{- end }}

{{- /*
Returns the comma-separated label selector used to discover JGroups cluster peers
(e.g. for the jgroups-ping environment or network policies).
*/ -}}
{{- define "axon-showcase.jgroups-cluster.labels" }}
    {{- printf "app.kubernetes.io/instance=%s,app.kubernetes.io/name=%s,jgroups-cluster=axon-showcase" .Release.Name (include "common.names.name" $) }}
{{- end }}

{{- /*
Per-component names, truncated to the Kubernetes resource name limit (63 chars)
and trimmed of a trailing dash after truncation.
*/ -}}
{{- define "axon-showcase.command-service.fullname" }}
    {{- printf "%s-%s" (include "common.names.fullname" $) "command-service" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "axon-showcase.saga-service.fullname" }}
    {{- printf "%s-%s" (include "common.names.fullname" $) "saga-service" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "axon-showcase.projection-service.fullname" }}
    {{- printf "%s-%s" (include "common.names.fullname" $) "projection-service" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "axon-showcase.query-service.fullname" }}
    {{- printf "%s-%s" (include "common.names.fullname" $) "query-service" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- /*
Internal HTTP URL of the query service (used by the API gateway to route requests).
*/ -}}
{{- define "axon-showcase.query-service.url" }}
    {{- printf "http://%s:%d" (include "axon-showcase.query-service.fullname" $) (.Values.queryService.containerPorts.server | int) }}
{{- end }}

{{- define "axon-showcase.api-gateway.fullname" }}
    {{- printf "%s-%s" (include "common.names.fullname" $) "api-gateway" | trunc 63 | trimSuffix "-" }}
{{- end }}
