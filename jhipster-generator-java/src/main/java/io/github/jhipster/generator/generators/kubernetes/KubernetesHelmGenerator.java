/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.kubernetes;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * Kubernetes Helm Generator.
 * Generates Helm charts for Kubernetes deployment.
 * Equivalent to kubernetes-helm/generator.ts.
 */
public class KubernetesHelmGenerator extends BaseApplicationGenerator {

    private String kubernetesNamespace = "default";
    private String dockerRepositoryName;

    public KubernetesHelmGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "kubernetes-helm";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringHelm", this::configuring);
        registerTask(GeneratorPriority.WRITING, "writingHelm", this::writing);
    }

    private void configuring() {
        log.info("Configuring Helm charts");

        JHipsterConfig config = getConfig();
        this.dockerRepositoryName = config.getLowerBaseName();
    }

    private void writing() throws Exception {
        log.info("Writing Helm chart files");

        JHipsterConfig config = getConfig();
        String appName = config.getLowerBaseName();
        String helmPath = "helm/" + appName + "/";

        // Write Chart.yaml
        writeChartYaml(helmPath, appName);

        // Write values.yaml
        writeValuesYaml(helmPath, appName, config);

        // Write templates
        writeDeploymentTemplate(helmPath, appName);
        writeServiceTemplate(helmPath, appName);
        writeIngressTemplate(helmPath, appName);
        writeConfigMapTemplate(helmPath, appName);
        writeSecretTemplate(helmPath, appName);
        writeHpaTemplate(helmPath, appName);
        writeServiceAccountTemplate(helmPath, appName);
        writeHelpersTemplate(helmPath, appName);
        writeNotesTemplate(helmPath, appName);

        // Write database templates if SQL
        if (isSql()) {
            writeDatabaseTemplates(helmPath, config);
        }

        // Write .helmignore
        writeHelmIgnore(helmPath);

        // Write README
        writeReadme(helmPath, appName);
    }

    private void writeChartYaml(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v2\n");
        yaml.append("name: ").append(appName).append("\n");
        yaml.append("description: A Helm chart for ").append(appName).append(" microservice\n");
        yaml.append("type: application\n");
        yaml.append("version: 0.1.0\n");
        yaml.append("appVersion: \"1.0.0\"\n");
        yaml.append("keywords:\n");
        yaml.append("  - jhipster\n");
        yaml.append("  - spring-boot\n");
        yaml.append("  - microservice\n");
        yaml.append("maintainers:\n");
        yaml.append("  - name: JHipster Team\n");
        yaml.append("    email: team@jhipster.tech\n");
        yaml.append("home: https://www.jhipster.tech\n");
        yaml.append("sources:\n");
        yaml.append("  - https://github.com/jhipster/generator-jhipster\n");

        writeFile(helmPath + "Chart.yaml", yaml.toString());
    }

    private void writeValuesYaml(String helmPath, String appName, JHipsterConfig config) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("# Default values for ").append(appName).append(".\n\n");

        yaml.append("replicaCount: 1\n\n");

        yaml.append("image:\n");
        yaml.append("  repository: ").append(dockerRepositoryName).append("/").append(appName).append("\n");
        yaml.append("  pullPolicy: IfNotPresent\n");
        yaml.append("  tag: \"latest\"\n\n");

        yaml.append("imagePullSecrets: []\n");
        yaml.append("nameOverride: \"\"\n");
        yaml.append("fullnameOverride: \"\"\n\n");

        yaml.append("serviceAccount:\n");
        yaml.append("  create: true\n");
        yaml.append("  annotations: {}\n");
        yaml.append("  name: \"\"\n\n");

        yaml.append("podAnnotations: {}\n\n");

        yaml.append("podSecurityContext: {}\n\n");

        yaml.append("securityContext: {}\n\n");

        yaml.append("service:\n");
        yaml.append("  type: ClusterIP\n");
        yaml.append("  port: ").append(config.getServerPort()).append("\n\n");

        yaml.append("ingress:\n");
        yaml.append("  enabled: true\n");
        yaml.append("  className: nginx\n");
        yaml.append("  annotations:\n");
        yaml.append("    nginx.ingress.kubernetes.io/rewrite-target: /\n");
        yaml.append("  hosts:\n");
        yaml.append("    - host: ").append(appName).append(".local\n");
        yaml.append("      paths:\n");
        yaml.append("        - path: /\n");
        yaml.append("          pathType: Prefix\n");
        yaml.append("  tls: []\n\n");

        yaml.append("resources:\n");
        yaml.append("  limits:\n");
        yaml.append("    cpu: 1000m\n");
        yaml.append("    memory: 1Gi\n");
        yaml.append("  requests:\n");
        yaml.append("    cpu: 500m\n");
        yaml.append("    memory: 512Mi\n\n");

        yaml.append("autoscaling:\n");
        yaml.append("  enabled: false\n");
        yaml.append("  minReplicas: 1\n");
        yaml.append("  maxReplicas: 10\n");
        yaml.append("  targetCPUUtilizationPercentage: 80\n");
        yaml.append("  targetMemoryUtilizationPercentage: 80\n\n");

        yaml.append("nodeSelector: {}\n\n");

        yaml.append("tolerations: []\n\n");

        yaml.append("affinity: {}\n\n");

        yaml.append("# Application configuration\n");
        yaml.append("config:\n");
        yaml.append("  springProfilesActive: prod,api-docs\n");

        if (isSql()) {
            yaml.append("  datasource:\n");
            yaml.append("    url: jdbc:").append(getJdbcProtocol(config.getProdDatabaseType()));
            yaml.append("://").append(appName).append("-").append(config.getProdDatabaseType());
            yaml.append(":").append(getDbPort(config.getProdDatabaseType())).append("/").append(appName).append("\n");
            yaml.append("    username: ").append(appName).append("\n");
            yaml.append("    password: changeme\n");
        }

        if (config.isJwt()) {
            yaml.append("  jwt:\n");
            yaml.append("    base64Secret: YTM1YjIxMjQyNmMxZjRhZTc0MTc1ZDZkNjFkN2IxYjg=\n");
        }

        yaml.append("\n# Probes configuration\n");
        yaml.append("livenessProbe:\n");
        yaml.append("  httpGet:\n");
        yaml.append("    path: /management/health/liveness\n");
        yaml.append("    port: http\n");
        yaml.append("  initialDelaySeconds: 120\n");
        yaml.append("  periodSeconds: 15\n");
        yaml.append("  failureThreshold: 3\n\n");

        yaml.append("readinessProbe:\n");
        yaml.append("  httpGet:\n");
        yaml.append("    path: /management/health/readiness\n");
        yaml.append("    port: http\n");
        yaml.append("  initialDelaySeconds: 30\n");
        yaml.append("  periodSeconds: 15\n");
        yaml.append("  failureThreshold: 3\n");

        writeFile(helmPath + "values.yaml", yaml.toString());
    }

    private void writeDeploymentTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: apps/v1\n");
        yaml.append("kind: Deployment\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".fullname\" . }}\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("spec:\n");
        yaml.append("  {{- if not .Values.autoscaling.enabled }}\n");
        yaml.append("  replicas: {{ .Values.replicaCount }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("  selector:\n");
        yaml.append("    matchLabels:\n");
        yaml.append("      {{- include \"").append(appName).append(".selectorLabels\" . | nindent 6 }}\n");
        yaml.append("  template:\n");
        yaml.append("    metadata:\n");
        yaml.append("      {{- with .Values.podAnnotations }}\n");
        yaml.append("      annotations:\n");
        yaml.append("        {{- toYaml . | nindent 8 }}\n");
        yaml.append("      {{- end }}\n");
        yaml.append("      labels:\n");
        yaml.append("        {{- include \"").append(appName).append(".selectorLabels\" . | nindent 8 }}\n");
        yaml.append("    spec:\n");
        yaml.append("      {{- with .Values.imagePullSecrets }}\n");
        yaml.append("      imagePullSecrets:\n");
        yaml.append("        {{- toYaml . | nindent 8 }}\n");
        yaml.append("      {{- end }}\n");
        yaml.append("      serviceAccountName: {{ include \"").append(appName).append(".serviceAccountName\" . }}\n");
        yaml.append("      securityContext:\n");
        yaml.append("        {{- toYaml .Values.podSecurityContext | nindent 8 }}\n");
        yaml.append("      containers:\n");
        yaml.append("        - name: {{ .Chart.Name }}\n");
        yaml.append("          securityContext:\n");
        yaml.append("            {{- toYaml .Values.securityContext | nindent 12 }}\n");
        yaml.append("          image: \"{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}\"\n");
        yaml.append("          imagePullPolicy: {{ .Values.image.pullPolicy }}\n");
        yaml.append("          ports:\n");
        yaml.append("            - name: http\n");
        yaml.append("              containerPort: {{ .Values.service.port }}\n");
        yaml.append("              protocol: TCP\n");
        yaml.append("          envFrom:\n");
        yaml.append("            - configMapRef:\n");
        yaml.append("                name: {{ include \"").append(appName).append(".fullname\" . }}-config\n");
        yaml.append("            - secretRef:\n");
        yaml.append("                name: {{ include \"").append(appName).append(".fullname\" . }}-secret\n");
        yaml.append("          livenessProbe:\n");
        yaml.append("            {{- toYaml .Values.livenessProbe | nindent 12 }}\n");
        yaml.append("          readinessProbe:\n");
        yaml.append("            {{- toYaml .Values.readinessProbe | nindent 12 }}\n");
        yaml.append("          resources:\n");
        yaml.append("            {{- toYaml .Values.resources | nindent 12 }}\n");
        yaml.append("      {{- with .Values.nodeSelector }}\n");
        yaml.append("      nodeSelector:\n");
        yaml.append("        {{- toYaml . | nindent 8 }}\n");
        yaml.append("      {{- end }}\n");
        yaml.append("      {{- with .Values.affinity }}\n");
        yaml.append("      affinity:\n");
        yaml.append("        {{- toYaml . | nindent 8 }}\n");
        yaml.append("      {{- end }}\n");
        yaml.append("      {{- with .Values.tolerations }}\n");
        yaml.append("      tolerations:\n");
        yaml.append("        {{- toYaml . | nindent 8 }}\n");
        yaml.append("      {{- end }}\n");

        writeFile(helmPath + "templates/deployment.yaml", yaml.toString());
    }

    private void writeServiceTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: Service\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".fullname\" . }}\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("spec:\n");
        yaml.append("  type: {{ .Values.service.type }}\n");
        yaml.append("  ports:\n");
        yaml.append("    - port: {{ .Values.service.port }}\n");
        yaml.append("      targetPort: http\n");
        yaml.append("      protocol: TCP\n");
        yaml.append("      name: http\n");
        yaml.append("  selector:\n");
        yaml.append("    {{- include \"").append(appName).append(".selectorLabels\" . | nindent 4 }}\n");

        writeFile(helmPath + "templates/service.yaml", yaml.toString());
    }

    private void writeIngressTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("{{- if .Values.ingress.enabled -}}\n");
        yaml.append("apiVersion: networking.k8s.io/v1\n");
        yaml.append("kind: Ingress\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".fullname\" . }}\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("  {{- with .Values.ingress.annotations }}\n");
        yaml.append("  annotations:\n");
        yaml.append("    {{- toYaml . | nindent 4 }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("spec:\n");
        yaml.append("  {{- if .Values.ingress.className }}\n");
        yaml.append("  ingressClassName: {{ .Values.ingress.className }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("  {{- if .Values.ingress.tls }}\n");
        yaml.append("  tls:\n");
        yaml.append("    {{- range .Values.ingress.tls }}\n");
        yaml.append("    - hosts:\n");
        yaml.append("        {{- range .hosts }}\n");
        yaml.append("        - {{ . | quote }}\n");
        yaml.append("        {{- end }}\n");
        yaml.append("      secretName: {{ .secretName }}\n");
        yaml.append("    {{- end }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("  rules:\n");
        yaml.append("    {{- range .Values.ingress.hosts }}\n");
        yaml.append("    - host: {{ .host | quote }}\n");
        yaml.append("      http:\n");
        yaml.append("        paths:\n");
        yaml.append("          {{- range .paths }}\n");
        yaml.append("          - path: {{ .path }}\n");
        yaml.append("            pathType: {{ .pathType }}\n");
        yaml.append("            backend:\n");
        yaml.append("              service:\n");
        yaml.append("                name: {{ include \"").append(appName).append(".fullname\" $ }}\n");
        yaml.append("                port:\n");
        yaml.append("                  number: {{ $.Values.service.port }}\n");
        yaml.append("          {{- end }}\n");
        yaml.append("    {{- end }}\n");
        yaml.append("{{- end }}\n");

        writeFile(helmPath + "templates/ingress.yaml", yaml.toString());
    }

    private void writeConfigMapTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: ConfigMap\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".fullname\" . }}-config\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("data:\n");
        yaml.append("  SPRING_PROFILES_ACTIVE: {{ .Values.config.springProfilesActive | quote }}\n");
        yaml.append("  {{- if .Values.config.datasource }}\n");
        yaml.append("  SPRING_DATASOURCE_URL: {{ .Values.config.datasource.url | quote }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("  MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED: \"true\"\n");

        writeFile(helmPath + "templates/configmap.yaml", yaml.toString());
    }

    private void writeSecretTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: Secret\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".fullname\" . }}-secret\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("type: Opaque\n");
        yaml.append("stringData:\n");
        yaml.append("  {{- if .Values.config.datasource }}\n");
        yaml.append("  SPRING_DATASOURCE_USERNAME: {{ .Values.config.datasource.username | quote }}\n");
        yaml.append("  SPRING_DATASOURCE_PASSWORD: {{ .Values.config.datasource.password | quote }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("  {{- if .Values.config.jwt }}\n");
        yaml.append("  JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET: {{ .Values.config.jwt.base64Secret | quote }}\n");
        yaml.append("  {{- end }}\n");

        writeFile(helmPath + "templates/secret.yaml", yaml.toString());
    }

    private void writeHpaTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("{{- if .Values.autoscaling.enabled }}\n");
        yaml.append("apiVersion: autoscaling/v2\n");
        yaml.append("kind: HorizontalPodAutoscaler\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".fullname\" . }}\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("spec:\n");
        yaml.append("  scaleTargetRef:\n");
        yaml.append("    apiVersion: apps/v1\n");
        yaml.append("    kind: Deployment\n");
        yaml.append("    name: {{ include \"").append(appName).append(".fullname\" . }}\n");
        yaml.append("  minReplicas: {{ .Values.autoscaling.minReplicas }}\n");
        yaml.append("  maxReplicas: {{ .Values.autoscaling.maxReplicas }}\n");
        yaml.append("  metrics:\n");
        yaml.append("    {{- if .Values.autoscaling.targetCPUUtilizationPercentage }}\n");
        yaml.append("    - type: Resource\n");
        yaml.append("      resource:\n");
        yaml.append("        name: cpu\n");
        yaml.append("        target:\n");
        yaml.append("          type: Utilization\n");
        yaml.append("          averageUtilization: {{ .Values.autoscaling.targetCPUUtilizationPercentage }}\n");
        yaml.append("    {{- end }}\n");
        yaml.append("    {{- if .Values.autoscaling.targetMemoryUtilizationPercentage }}\n");
        yaml.append("    - type: Resource\n");
        yaml.append("      resource:\n");
        yaml.append("        name: memory\n");
        yaml.append("        target:\n");
        yaml.append("          type: Utilization\n");
        yaml.append("          averageUtilization: {{ .Values.autoscaling.targetMemoryUtilizationPercentage }}\n");
        yaml.append("    {{- end }}\n");
        yaml.append("{{- end }}\n");

        writeFile(helmPath + "templates/hpa.yaml", yaml.toString());
    }

    private void writeServiceAccountTemplate(String helmPath, String appName) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("{{- if .Values.serviceAccount.create -}}\n");
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: ServiceAccount\n");
        yaml.append("metadata:\n");
        yaml.append("  name: {{ include \"").append(appName).append(".serviceAccountName\" . }}\n");
        yaml.append("  labels:\n");
        yaml.append("    {{- include \"").append(appName).append(".labels\" . | nindent 4 }}\n");
        yaml.append("  {{- with .Values.serviceAccount.annotations }}\n");
        yaml.append("  annotations:\n");
        yaml.append("    {{- toYaml . | nindent 4 }}\n");
        yaml.append("  {{- end }}\n");
        yaml.append("{{- end }}\n");

        writeFile(helmPath + "templates/serviceaccount.yaml", yaml.toString());
    }

    private void writeHelpersTemplate(String helmPath, String appName) throws Exception {
        StringBuilder tpl = new StringBuilder();
        tpl.append("{{/*\n");
        tpl.append("Expand the name of the chart.\n");
        tpl.append("*/}}\n");
        tpl.append("{{- define \"").append(appName).append(".name\" -}}\n");
        tpl.append("{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix \"-\" }}\n");
        tpl.append("{{- end }}\n\n");

        tpl.append("{{/*\n");
        tpl.append("Create a default fully qualified app name.\n");
        tpl.append("*/}}\n");
        tpl.append("{{- define \"").append(appName).append(".fullname\" -}}\n");
        tpl.append("{{- if .Values.fullnameOverride }}\n");
        tpl.append("{{- .Values.fullnameOverride | trunc 63 | trimSuffix \"-\" }}\n");
        tpl.append("{{- else }}\n");
        tpl.append("{{- $name := default .Chart.Name .Values.nameOverride }}\n");
        tpl.append("{{- if contains $name .Release.Name }}\n");
        tpl.append("{{- .Release.Name | trunc 63 | trimSuffix \"-\" }}\n");
        tpl.append("{{- else }}\n");
        tpl.append("{{- printf \"%s-%s\" .Release.Name $name | trunc 63 | trimSuffix \"-\" }}\n");
        tpl.append("{{- end }}\n");
        tpl.append("{{- end }}\n");
        tpl.append("{{- end }}\n\n");

        tpl.append("{{/*\n");
        tpl.append("Create chart name and version as used by the chart label.\n");
        tpl.append("*/}}\n");
        tpl.append("{{- define \"").append(appName).append(".chart\" -}}\n");
        tpl.append("{{- printf \"%s-%s\" .Chart.Name .Chart.Version | replace \"+\" \"_\" | trunc 63 | trimSuffix \"-\" }}\n");
        tpl.append("{{- end }}\n\n");

        tpl.append("{{/*\n");
        tpl.append("Common labels\n");
        tpl.append("*/}}\n");
        tpl.append("{{- define \"").append(appName).append(".labels\" -}}\n");
        tpl.append("helm.sh/chart: {{ include \"").append(appName).append(".chart\" . }}\n");
        tpl.append("{{ include \"").append(appName).append(".selectorLabels\" . }}\n");
        tpl.append("{{- if .Chart.AppVersion }}\n");
        tpl.append("app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}\n");
        tpl.append("{{- end }}\n");
        tpl.append("app.kubernetes.io/managed-by: {{ .Release.Service }}\n");
        tpl.append("{{- end }}\n\n");

        tpl.append("{{/*\n");
        tpl.append("Selector labels\n");
        tpl.append("*/}}\n");
        tpl.append("{{- define \"").append(appName).append(".selectorLabels\" -}}\n");
        tpl.append("app.kubernetes.io/name: {{ include \"").append(appName).append(".name\" . }}\n");
        tpl.append("app.kubernetes.io/instance: {{ .Release.Name }}\n");
        tpl.append("{{- end }}\n\n");

        tpl.append("{{/*\n");
        tpl.append("Create the name of the service account to use\n");
        tpl.append("*/}}\n");
        tpl.append("{{- define \"").append(appName).append(".serviceAccountName\" -}}\n");
        tpl.append("{{- if .Values.serviceAccount.create }}\n");
        tpl.append("{{- default (include \"").append(appName).append(".fullname\" .) .Values.serviceAccount.name }}\n");
        tpl.append("{{- else }}\n");
        tpl.append("{{- default \"default\" .Values.serviceAccount.name }}\n");
        tpl.append("{{- end }}\n");
        tpl.append("{{- end }}\n");

        writeFile(helmPath + "templates/_helpers.tpl", tpl.toString());
    }

    private void writeNotesTemplate(String helmPath, String appName) throws Exception {
        StringBuilder notes = new StringBuilder();
        notes.append("1. Get the application URL by running these commands:\n");
        notes.append("{{- if .Values.ingress.enabled }}\n");
        notes.append("{{- range $host := .Values.ingress.hosts }}\n");
        notes.append("  {{- range .paths }}\n");
        notes.append("  http{{ if $.Values.ingress.tls }}s{{ end }}://{{ $host.host }}{{ .path }}\n");
        notes.append("  {{- end }}\n");
        notes.append("{{- end }}\n");
        notes.append("{{- else if contains \"NodePort\" .Values.service.type }}\n");
        notes.append("  export NODE_PORT=$(kubectl get --namespace {{ .Release.Namespace }} -o jsonpath=\"{.spec.ports[0].nodePort}\" services {{ include \"").append(appName).append(".fullname\" . }})\n");
        notes.append("  export NODE_IP=$(kubectl get nodes --namespace {{ .Release.Namespace }} -o jsonpath=\"{.items[0].status.addresses[0].address}\")\n");
        notes.append("  echo http://$NODE_IP:$NODE_PORT\n");
        notes.append("{{- else if contains \"LoadBalancer\" .Values.service.type }}\n");
        notes.append("     NOTE: It may take a few minutes for the LoadBalancer IP to be available.\n");
        notes.append("           You can watch the status of by running 'kubectl get --namespace {{ .Release.Namespace }} svc -w {{ include \"").append(appName).append(".fullname\" . }}'\n");
        notes.append("  export SERVICE_IP=$(kubectl get svc --namespace {{ .Release.Namespace }} {{ include \"").append(appName).append(".fullname\" . }} --template \"{{range (index .status.loadBalancer.ingress 0)}}{{.}}{{end}}\")\n");
        notes.append("  echo http://$SERVICE_IP:{{ .Values.service.port }}\n");
        notes.append("{{- else if contains \"ClusterIP\" .Values.service.type }}\n");
        notes.append("  export POD_NAME=$(kubectl get pods --namespace {{ .Release.Namespace }} -l \"app.kubernetes.io/name={{ include \"").append(appName).append(".name\" . }},app.kubernetes.io/instance={{ .Release.Name }}\" -o jsonpath=\"{.items[0].metadata.name}\")\n");
        notes.append("  export CONTAINER_PORT=$(kubectl get pod --namespace {{ .Release.Namespace }} $POD_NAME -o jsonpath=\"{.spec.containers[0].ports[0].containerPort}\")\n");
        notes.append("  echo \"Visit http://127.0.0.1:8080 to use your application\"\n");
        notes.append("  kubectl --namespace {{ .Release.Namespace }} port-forward $POD_NAME 8080:$CONTAINER_PORT\n");
        notes.append("{{- end }}\n");

        writeFile(helmPath + "templates/NOTES.txt", notes.toString());
    }

    private void writeDatabaseTemplates(String helmPath, JHipsterConfig config) throws Exception {
        // This would include database StatefulSet and Service templates
        // Similar to KubernetesGenerator but using Helm templating
    }

    private void writeHelmIgnore(String helmPath) throws Exception {
        StringBuilder ignore = new StringBuilder();
        ignore.append("# Patterns to ignore when building packages.\n");
        ignore.append(".DS_Store\n");
        ignore.append("*.swp\n");
        ignore.append("*.bak\n");
        ignore.append("*.tmp\n");
        ignore.append("*.orig\n");
        ignore.append("*~\n");
        ignore.append(".git/\n");
        ignore.append(".gitignore\n");
        ignore.append(".bzr/\n");
        ignore.append(".bzrignore\n");
        ignore.append(".hg/\n");
        ignore.append(".hgignore\n");
        ignore.append(".svn/\n");
        ignore.append("*.orig\n");
        ignore.append("*.bak\n");

        writeFile(helmPath + ".helmignore", ignore.toString());
    }

    private void writeReadme(String helmPath, String appName) throws Exception {
        StringBuilder readme = new StringBuilder();
        readme.append("# ").append(appName).append(" Helm Chart\n\n");

        readme.append("## Prerequisites\n\n");
        readme.append("- Kubernetes 1.19+\n");
        readme.append("- Helm 3.2.0+\n\n");

        readme.append("## Installing the Chart\n\n");
        readme.append("```bash\n");
        readme.append("helm install ").append(appName).append(" ./").append(appName).append("\n");
        readme.append("```\n\n");

        readme.append("## Uninstalling the Chart\n\n");
        readme.append("```bash\n");
        readme.append("helm uninstall ").append(appName).append("\n");
        readme.append("```\n\n");

        readme.append("## Configuration\n\n");
        readme.append("See `values.yaml` for configurable parameters.\n\n");

        readme.append("## Upgrading\n\n");
        readme.append("```bash\n");
        readme.append("helm upgrade ").append(appName).append(" ./").append(appName).append("\n");
        readme.append("```\n");

        writeFile(helmPath + "README.md", readme.toString());
    }

    private String getJdbcProtocol(String dbType) {
        switch (dbType) {
            case "postgresql": return "postgresql";
            case "mysql": return "mysql";
            case "mariadb": return "mariadb";
            default: return dbType;
        }
    }

    private int getDbPort(String dbType) {
        switch (dbType) {
            case "postgresql": return 5432;
            case "mysql":
            case "mariadb": return 3306;
            default: return 5432;
        }
    }
}
