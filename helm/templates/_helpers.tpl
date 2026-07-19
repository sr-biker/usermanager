{{- define "usermanager.name" -}}
usermanager
{{- end -}}

{{- define "usermanager.selectorLabels" -}}
app.kubernetes.io/name: {{ include "usermanager.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "usermanager.labels" -}}
{{ include "usermanager.selectorLabels" . }}
environment: {{ .Values.environment }}
{{- end -}}

{{- define "usermanager.dbSecretName" -}}
{{- if .Values.db.existingSecret -}}
{{ .Values.db.existingSecret }}
{{- else -}}
{{ include "usermanager.name" . }}-db
{{- end -}}
{{- end -}}

{{- define "usermanager.awsSecretName" -}}
{{- if .Values.aws.existingSecret -}}
{{ .Values.aws.existingSecret }}
{{- else -}}
{{ include "usermanager.name" . }}-aws
{{- end -}}
{{- end -}}
