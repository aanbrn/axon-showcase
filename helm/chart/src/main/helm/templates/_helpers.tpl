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

{{- define "axon-showcase.images.imagePullPolicy" }}
    {{- $imageRoot := include "common.tplvalues.render" (dict "value" .imageRoot "context" .context ) | fromYaml }}
    {{- $imageRoot.pullPolicy | default (eq $imageRoot.tag "latest" | ternary "Always" "IfNotPresent") }}
{{- end }}

{{- define "axon-showcase.observability.isEnabled" }}
    {{- if or .Values.observability.logging.structured.enabled .Values.observability.metrics.prometheus.enabled .Values.observability.metrics.otlp.enabled .Values.observability.tracing.otlp.enabled }}
        {{- true }}
    {{- end }}
{{- end }}

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
    {{- if .Values.observability.metrics.prometheus.enabled }}
        {{- /* @formatter:off */}}
- name: "METRICS_PROMETHEUS_ENABLED"
  value: "true"
        {{- /* @formatter:on */}}
    {{- end }}
    {{- with .Values.observability.metrics.tags.application }}
        {{- if and .key .value }}
            {{- /* @formatter:off */}}
- name: "METRICS_TAG_APPLICATION_KEY"
  value: {{ include "common.tplvalues.render" (dict "value" .key "context" $) | quote }}
- name: "METRICS_TAG_APPLICATION_VALUE"
  value: {{ include "common.tplvalues.render" (dict "value" .value "context" $) | quote }}
            {{- /* @formatter:on */}}
        {{- end }}
    {{- end }}
    {{- if .Values.observability.metrics.otlp.enabled }}
        {{- if empty .Values.observability.metrics.otlp.endpoint }}
            {{- fail "\".Values.observability.metrics.otlp.endpoint\" is required to enable metrics export" }}
        {{- end }}
        {{- /* @formatter:off */}}
- name: "METRICS_OTLP_ENABLED"
  value: "true"
- name: "METRICS_OTLP_ENDPOINT"
  value: {{ .Values.observability.metrics.otlp.endpoint | quote }}
        {{- /* @formatter:on */}}
    {{- end }}
    {{- if .Values.observability.tracing.otlp.enabled }}
        {{- if empty .Values.observability.tracing.otlp.endpoint }}
            {{- fail "\".Values.observability.tracing.otlp.endpoint\" is required to enable tracing export" }}
        {{- end }}
        {{- /* @formatter:off */}}
- name: "TRACING_OTLP_ENABLED"
  value: "true"
- name: "TRACING_OTLP_ENDPOINT"
  value: {{ .Values.observability.tracing.otlp.endpoint | quote }}
        {{- /* @formatter:on */}}
    {{- end }}
    {{- /* @formatter:off */}}
- name: "TRACING_LOGGING"
  value: {{ .Values.observability.tracing.logging | quote }}
    {{- /* @formatter:on */}}
{{- end }}

{{- define "axon-showcase.serviceAccountName" }}
    {{- if .Values.serviceAccount.create }}
        {{- .Values.serviceAccount.name | default (include "common.names.fullname" $) }}
    {{- else }}
        {{- .Values.serviceAccount.name | default "default" }}
    {{- end }}
{{- end }}

{{- define "axon-showcase.jgroups-cluster.labels" }}
    {{- printf "app.kubernetes.io/instance=%s,app.kubernetes.io/name=%s,jgroups-cluster=axon-showcase" .Release.Name (include "common.names.name" $) }}
{{- end }}

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

{{- define "axon-showcase.query-service.url" }}
    {{- printf "http://%s:%d" (include "axon-showcase.query-service.fullname" $) (.Values.queryService.containerPorts.server | int) }}
{{- end }}

{{- define "axon-showcase.api-gateway.fullname" }}
    {{- printf "%s-%s" (include "common.names.fullname" $) "api-gateway" | trunc 63 | trimSuffix "-" }}
{{- end }}
