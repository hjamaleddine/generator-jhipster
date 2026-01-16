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
 * Kubernetes Generator.
 * Generates Kubernetes deployment manifests, services, and configurations.
 * Equivalent to kubernetes/generator.ts.
 */
public class KubernetesGenerator extends BaseApplicationGenerator {

    private String kubernetesNamespace = "default";
    private String dockerRepositoryName;
    private String dockerPushCommand = "docker push";
    private boolean istio = false;
    private String ingressType = "nginx";
    private String ingressDomain;
    private boolean enablePersistentStorage = true;
    private String storageClassName = "standard";

    public KubernetesGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "kubernetes";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringKubernetes", this::configuring);
        registerTask(GeneratorPriority.WRITING, "writingKubernetes", this::writing);
    }

    private void configuring() {
        log.info("Configuring Kubernetes deployment");

        JHipsterConfig config = getConfig();

        // Set default values
        this.dockerRepositoryName = config.getLowerBaseName();
        this.ingressDomain = config.getLowerBaseName() + ".local";
    }

    private void writing() throws Exception {
        log.info("Writing Kubernetes manifests");

        JHipsterConfig config = getConfig();
        String appName = config.getLowerBaseName();

        // Create kubernetes directory
        String k8sPath = "kubernetes/";

        // Write namespace
        writeNamespace(k8sPath);

        // Write deployment
        writeDeployment(k8sPath, appName);

        // Write service
        writeService(k8sPath, appName);

        // Write ingress
        writeIngress(k8sPath, appName);

        // Write ConfigMap
        writeConfigMap(k8sPath, appName);

        // Write Secret
        writeSecret(k8sPath, appName);

        // Write database deployment if needed
        if (isSql()) {
            writeDatabaseDeployment(k8sPath, config);
        }

        // Write service discovery if needed
        if (hasServiceDiscovery()) {
            writeServiceDiscovery(k8sPath, config);
        }

        // Write message broker if needed
        if ("kafka".equals(config.getMessageBroker())) {
            writeKafkaDeployment(k8sPath);
        }

        // Write Elasticsearch if needed
        if ("elasticsearch".equals(config.getSearchEngine())) {
            writeElasticsearchDeployment(k8sPath);
        }

        // Write kubectl apply script
        writeApplyScript(k8sPath);

        // Write README
        writeReadme(k8sPath);
    }

    private void writeNamespace(String k8sPath) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: Namespace\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(kubernetesNamespace).append("\n");
        yaml.append("  labels:\n");
        yaml.append("    app.kubernetes.io/managed-by: jhipster\n");

        if (istio) {
            yaml.append("    istio-injection: enabled\n");
        }

        writeFile(k8sPath + "namespace.yml", yaml.toString());
    }

    private void writeDeployment(String k8sPath, String appName) throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: apps/v1\n");
        yaml.append("kind: Deployment\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(appName).append("\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("  labels:\n");
        yaml.append("    app: ").append(appName).append("\n");
        yaml.append("    version: \"v1\"\n");
        yaml.append("spec:\n");
        yaml.append("  replicas: 1\n");
        yaml.append("  selector:\n");
        yaml.append("    matchLabels:\n");
        yaml.append("      app: ").append(appName).append("\n");
        yaml.append("      version: \"v1\"\n");
        yaml.append("  template:\n");
        yaml.append("    metadata:\n");
        yaml.append("      labels:\n");
        yaml.append("        app: ").append(appName).append("\n");
        yaml.append("        version: \"v1\"\n");

        if (istio) {
            yaml.append("      annotations:\n");
            yaml.append("        sidecar.istio.io/inject: \"true\"\n");
        }

        yaml.append("    spec:\n");
        yaml.append("      containers:\n");
        yaml.append("        - name: ").append(appName).append("\n");
        yaml.append("          image: ").append(dockerRepositoryName).append("/").append(appName).append(":latest\n");
        yaml.append("          imagePullPolicy: IfNotPresent\n");
        yaml.append("          ports:\n");
        yaml.append("            - name: http\n");
        yaml.append("              containerPort: ").append(config.getServerPort()).append("\n");
        yaml.append("          envFrom:\n");
        yaml.append("            - configMapRef:\n");
        yaml.append("                name: ").append(appName).append("-config\n");
        yaml.append("            - secretRef:\n");
        yaml.append("                name: ").append(appName).append("-secret\n");
        yaml.append("          resources:\n");
        yaml.append("            requests:\n");
        yaml.append("              memory: \"512Mi\"\n");
        yaml.append("              cpu: \"500m\"\n");
        yaml.append("            limits:\n");
        yaml.append("              memory: \"1Gi\"\n");
        yaml.append("              cpu: \"1\"\n");
        yaml.append("          livenessProbe:\n");
        yaml.append("            httpGet:\n");
        yaml.append("              path: /management/health/liveness\n");
        yaml.append("              port: http\n");
        yaml.append("            initialDelaySeconds: 120\n");
        yaml.append("            periodSeconds: 15\n");
        yaml.append("            failureThreshold: 3\n");
        yaml.append("          readinessProbe:\n");
        yaml.append("            httpGet:\n");
        yaml.append("              path: /management/health/readiness\n");
        yaml.append("              port: http\n");
        yaml.append("            initialDelaySeconds: 30\n");
        yaml.append("            periodSeconds: 15\n");
        yaml.append("            failureThreshold: 3\n");

        writeFile(k8sPath + appName + "-deployment.yml", yaml.toString());
    }

    private void writeService(String k8sPath, String appName) throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: Service\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(appName).append("\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("  labels:\n");
        yaml.append("    app: ").append(appName).append("\n");
        yaml.append("spec:\n");
        yaml.append("  type: ClusterIP\n");
        yaml.append("  selector:\n");
        yaml.append("    app: ").append(appName).append("\n");
        yaml.append("  ports:\n");
        yaml.append("    - name: http\n");
        yaml.append("      port: ").append(config.getServerPort()).append("\n");
        yaml.append("      targetPort: http\n");

        writeFile(k8sPath + appName + "-service.yml", yaml.toString());
    }

    private void writeIngress(String k8sPath, String appName) throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: networking.k8s.io/v1\n");
        yaml.append("kind: Ingress\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(appName).append("\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("  annotations:\n");

        if ("nginx".equals(ingressType)) {
            yaml.append("    kubernetes.io/ingress.class: nginx\n");
            yaml.append("    nginx.ingress.kubernetes.io/rewrite-target: /\n");
            yaml.append("    nginx.ingress.kubernetes.io/proxy-body-size: \"0\"\n");
        }

        yaml.append("spec:\n");
        yaml.append("  rules:\n");
        yaml.append("    - host: ").append(ingressDomain).append("\n");
        yaml.append("      http:\n");
        yaml.append("        paths:\n");
        yaml.append("          - path: /\n");
        yaml.append("            pathType: Prefix\n");
        yaml.append("            backend:\n");
        yaml.append("              service:\n");
        yaml.append("                name: ").append(appName).append("\n");
        yaml.append("                port:\n");
        yaml.append("                  number: ").append(config.getServerPort()).append("\n");

        writeFile(k8sPath + appName + "-ingress.yml", yaml.toString());
    }

    private void writeConfigMap(String k8sPath, String appName) throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: ConfigMap\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(appName).append("-config\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("data:\n");
        yaml.append("  SPRING_PROFILES_ACTIVE: prod,api-docs\n");
        yaml.append("  SPRING_APPLICATION_NAME: ").append(appName).append("\n");
        yaml.append("  MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED: \"true\"\n");

        if (isSql()) {
            String dbHost = appName + "-" + config.getProdDatabaseType();
            yaml.append("  SPRING_DATASOURCE_URL: jdbc:").append(getJdbcProtocol(config.getProdDatabaseType()));
            yaml.append("://").append(dbHost).append(":").append(getDbPort(config.getProdDatabaseType()));
            yaml.append("/").append(appName).append("\n");
        }

        if (hasServiceDiscovery()) {
            if (config.isConsul()) {
                yaml.append("  SPRING_CLOUD_CONSUL_HOST: consul\n");
                yaml.append("  SPRING_CLOUD_CONSUL_PORT: \"8500\"\n");
            } else if (config.isEureka()) {
                yaml.append("  EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://jhipster-registry:8761/eureka/\n");
            }
        }

        if ("kafka".equals(config.getMessageBroker())) {
            yaml.append("  SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS: kafka:9092\n");
        }

        if ("elasticsearch".equals(config.getSearchEngine())) {
            yaml.append("  SPRING_ELASTICSEARCH_URIS: http://elasticsearch:9200\n");
        }

        writeFile(k8sPath + appName + "-configmap.yml", yaml.toString());
    }

    private void writeSecret(String k8sPath, String appName) throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: Secret\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(appName).append("-secret\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("type: Opaque\n");
        yaml.append("stringData:\n");

        if (isSql()) {
            yaml.append("  SPRING_DATASOURCE_USERNAME: ").append(appName).append("\n");
            yaml.append("  SPRING_DATASOURCE_PASSWORD: changeme\n");
        }

        if (config.isJwt()) {
            yaml.append("  JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET: ");
            yaml.append(config.getJwtSecretKey() != null ? config.getJwtSecretKey() : "YTM1YjIxMjQyNmMxZjRhZTc0MTc1ZDZkNjFkN2IxYjg=");
            yaml.append("\n");
        }

        writeFile(k8sPath + appName + "-secret.yml", yaml.toString());
    }

    private void writeDatabaseDeployment(String k8sPath, JHipsterConfig config) throws Exception {
        String dbType = config.getProdDatabaseType();
        String appName = config.getLowerBaseName();
        String dbName = appName + "-" + dbType;

        // StatefulSet for database
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: apps/v1\n");
        yaml.append("kind: StatefulSet\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(dbName).append("\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("spec:\n");
        yaml.append("  serviceName: ").append(dbName).append("\n");
        yaml.append("  replicas: 1\n");
        yaml.append("  selector:\n");
        yaml.append("    matchLabels:\n");
        yaml.append("      app: ").append(dbName).append("\n");
        yaml.append("  template:\n");
        yaml.append("    metadata:\n");
        yaml.append("      labels:\n");
        yaml.append("        app: ").append(dbName).append("\n");
        yaml.append("    spec:\n");
        yaml.append("      containers:\n");
        yaml.append("        - name: ").append(dbType).append("\n");
        yaml.append("          image: ").append(getDbImage(dbType)).append("\n");
        yaml.append("          ports:\n");
        yaml.append("            - containerPort: ").append(getDbPort(dbType)).append("\n");
        yaml.append("          env:\n");

        switch (dbType) {
            case "postgresql":
                yaml.append("            - name: POSTGRES_USER\n");
                yaml.append("              value: ").append(appName).append("\n");
                yaml.append("            - name: POSTGRES_PASSWORD\n");
                yaml.append("              value: changeme\n");
                yaml.append("            - name: POSTGRES_DB\n");
                yaml.append("              value: ").append(appName).append("\n");
                break;
            case "mysql":
            case "mariadb":
                yaml.append("            - name: MYSQL_ROOT_PASSWORD\n");
                yaml.append("              value: changeme\n");
                yaml.append("            - name: MYSQL_USER\n");
                yaml.append("              value: ").append(appName).append("\n");
                yaml.append("            - name: MYSQL_PASSWORD\n");
                yaml.append("              value: changeme\n");
                yaml.append("            - name: MYSQL_DATABASE\n");
                yaml.append("              value: ").append(appName).append("\n");
                break;
        }

        if (enablePersistentStorage) {
            yaml.append("          volumeMounts:\n");
            yaml.append("            - name: data\n");
            yaml.append("              mountPath: ").append(getDbDataPath(dbType)).append("\n");
            yaml.append("  volumeClaimTemplates:\n");
            yaml.append("    - metadata:\n");
            yaml.append("        name: data\n");
            yaml.append("      spec:\n");
            yaml.append("        accessModes: [\"ReadWriteOnce\"]\n");
            yaml.append("        storageClassName: ").append(storageClassName).append("\n");
            yaml.append("        resources:\n");
            yaml.append("          requests:\n");
            yaml.append("            storage: 1Gi\n");
        }

        writeFile(k8sPath + dbName + "-statefulset.yml", yaml.toString());

        // Database Service
        StringBuilder serviceYaml = new StringBuilder();
        serviceYaml.append("apiVersion: v1\n");
        serviceYaml.append("kind: Service\n");
        serviceYaml.append("metadata:\n");
        serviceYaml.append("  name: ").append(dbName).append("\n");
        serviceYaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        serviceYaml.append("spec:\n");
        serviceYaml.append("  type: ClusterIP\n");
        serviceYaml.append("  selector:\n");
        serviceYaml.append("    app: ").append(dbName).append("\n");
        serviceYaml.append("  ports:\n");
        serviceYaml.append("    - port: ").append(getDbPort(dbType)).append("\n");
        serviceYaml.append("      targetPort: ").append(getDbPort(dbType)).append("\n");

        writeFile(k8sPath + dbName + "-service.yml", serviceYaml.toString());
    }

    private void writeServiceDiscovery(String k8sPath, JHipsterConfig config) throws Exception {
        if (config.isConsul()) {
            writeConsulDeployment(k8sPath);
        } else if (config.isEureka()) {
            writeJHipsterRegistryDeployment(k8sPath);
        }
    }

    private void writeConsulDeployment(String k8sPath) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: apps/v1\n");
        yaml.append("kind: Deployment\n");
        yaml.append("metadata:\n");
        yaml.append("  name: consul\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("spec:\n");
        yaml.append("  replicas: 1\n");
        yaml.append("  selector:\n");
        yaml.append("    matchLabels:\n");
        yaml.append("      app: consul\n");
        yaml.append("  template:\n");
        yaml.append("    metadata:\n");
        yaml.append("      labels:\n");
        yaml.append("        app: consul\n");
        yaml.append("    spec:\n");
        yaml.append("      containers:\n");
        yaml.append("        - name: consul\n");
        yaml.append("          image: hashicorp/consul:1.16\n");
        yaml.append("          args:\n");
        yaml.append("            - agent\n");
        yaml.append("            - -dev\n");
        yaml.append("            - -ui\n");
        yaml.append("            - -client=0.0.0.0\n");
        yaml.append("          ports:\n");
        yaml.append("            - containerPort: 8500\n");
        yaml.append("            - containerPort: 8600\n");

        writeFile(k8sPath + "consul-deployment.yml", yaml.toString());

        // Consul Service
        StringBuilder serviceYaml = new StringBuilder();
        serviceYaml.append("apiVersion: v1\n");
        serviceYaml.append("kind: Service\n");
        serviceYaml.append("metadata:\n");
        serviceYaml.append("  name: consul\n");
        serviceYaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        serviceYaml.append("spec:\n");
        serviceYaml.append("  type: ClusterIP\n");
        serviceYaml.append("  selector:\n");
        serviceYaml.append("    app: consul\n");
        serviceYaml.append("  ports:\n");
        serviceYaml.append("    - name: http\n");
        serviceYaml.append("      port: 8500\n");
        serviceYaml.append("      targetPort: 8500\n");
        serviceYaml.append("    - name: dns\n");
        serviceYaml.append("      port: 8600\n");
        serviceYaml.append("      targetPort: 8600\n");

        writeFile(k8sPath + "consul-service.yml", serviceYaml.toString());
    }

    private void writeJHipsterRegistryDeployment(String k8sPath) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: apps/v1\n");
        yaml.append("kind: Deployment\n");
        yaml.append("metadata:\n");
        yaml.append("  name: jhipster-registry\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("spec:\n");
        yaml.append("  replicas: 1\n");
        yaml.append("  selector:\n");
        yaml.append("    matchLabels:\n");
        yaml.append("      app: jhipster-registry\n");
        yaml.append("  template:\n");
        yaml.append("    metadata:\n");
        yaml.append("      labels:\n");
        yaml.append("        app: jhipster-registry\n");
        yaml.append("    spec:\n");
        yaml.append("      containers:\n");
        yaml.append("        - name: jhipster-registry\n");
        yaml.append("          image: jhipster/jhipster-registry:v7.4.0\n");
        yaml.append("          ports:\n");
        yaml.append("            - containerPort: 8761\n");
        yaml.append("          env:\n");
        yaml.append("            - name: SPRING_PROFILES_ACTIVE\n");
        yaml.append("              value: prod,api-docs,oauth2\n");
        yaml.append("            - name: SPRING_SECURITY_USER_PASSWORD\n");
        yaml.append("              value: admin\n");
        yaml.append("            - name: JHIPSTER_REGISTRY_PASSWORD\n");
        yaml.append("              value: admin\n");

        writeFile(k8sPath + "jhipster-registry-deployment.yml", yaml.toString());

        // Service
        StringBuilder serviceYaml = new StringBuilder();
        serviceYaml.append("apiVersion: v1\n");
        serviceYaml.append("kind: Service\n");
        serviceYaml.append("metadata:\n");
        serviceYaml.append("  name: jhipster-registry\n");
        serviceYaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        serviceYaml.append("spec:\n");
        serviceYaml.append("  type: ClusterIP\n");
        serviceYaml.append("  selector:\n");
        serviceYaml.append("    app: jhipster-registry\n");
        serviceYaml.append("  ports:\n");
        serviceYaml.append("    - port: 8761\n");
        serviceYaml.append("      targetPort: 8761\n");

        writeFile(k8sPath + "jhipster-registry-service.yml", serviceYaml.toString());
    }

    private void writeKafkaDeployment(String k8sPath) throws Exception {
        // Zookeeper
        StringBuilder zkYaml = new StringBuilder();
        zkYaml.append("apiVersion: apps/v1\n");
        zkYaml.append("kind: Deployment\n");
        zkYaml.append("metadata:\n");
        zkYaml.append("  name: zookeeper\n");
        zkYaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        zkYaml.append("spec:\n");
        zkYaml.append("  replicas: 1\n");
        zkYaml.append("  selector:\n");
        zkYaml.append("    matchLabels:\n");
        zkYaml.append("      app: zookeeper\n");
        zkYaml.append("  template:\n");
        zkYaml.append("    metadata:\n");
        zkYaml.append("      labels:\n");
        zkYaml.append("        app: zookeeper\n");
        zkYaml.append("    spec:\n");
        zkYaml.append("      containers:\n");
        zkYaml.append("        - name: zookeeper\n");
        zkYaml.append("          image: confluentinc/cp-zookeeper:7.5.0\n");
        zkYaml.append("          ports:\n");
        zkYaml.append("            - containerPort: 2181\n");
        zkYaml.append("          env:\n");
        zkYaml.append("            - name: ZOOKEEPER_CLIENT_PORT\n");
        zkYaml.append("              value: \"2181\"\n");

        writeFile(k8sPath + "zookeeper-deployment.yml", zkYaml.toString());

        // Kafka
        StringBuilder kafkaYaml = new StringBuilder();
        kafkaYaml.append("apiVersion: apps/v1\n");
        kafkaYaml.append("kind: Deployment\n");
        kafkaYaml.append("metadata:\n");
        kafkaYaml.append("  name: kafka\n");
        kafkaYaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        kafkaYaml.append("spec:\n");
        kafkaYaml.append("  replicas: 1\n");
        kafkaYaml.append("  selector:\n");
        kafkaYaml.append("    matchLabels:\n");
        kafkaYaml.append("      app: kafka\n");
        kafkaYaml.append("  template:\n");
        kafkaYaml.append("    metadata:\n");
        kafkaYaml.append("      labels:\n");
        kafkaYaml.append("        app: kafka\n");
        kafkaYaml.append("    spec:\n");
        kafkaYaml.append("      containers:\n");
        kafkaYaml.append("        - name: kafka\n");
        kafkaYaml.append("          image: confluentinc/cp-kafka:7.5.0\n");
        kafkaYaml.append("          ports:\n");
        kafkaYaml.append("            - containerPort: 9092\n");
        kafkaYaml.append("          env:\n");
        kafkaYaml.append("            - name: KAFKA_ZOOKEEPER_CONNECT\n");
        kafkaYaml.append("              value: zookeeper:2181\n");
        kafkaYaml.append("            - name: KAFKA_ADVERTISED_LISTENERS\n");
        kafkaYaml.append("              value: PLAINTEXT://kafka:9092\n");
        kafkaYaml.append("            - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR\n");
        kafkaYaml.append("              value: \"1\"\n");

        writeFile(k8sPath + "kafka-deployment.yml", kafkaYaml.toString());

        // Services
        writeServiceYaml(k8sPath, "zookeeper", 2181);
        writeServiceYaml(k8sPath, "kafka", 9092);
    }

    private void writeElasticsearchDeployment(String k8sPath) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: apps/v1\n");
        yaml.append("kind: Deployment\n");
        yaml.append("metadata:\n");
        yaml.append("  name: elasticsearch\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("spec:\n");
        yaml.append("  replicas: 1\n");
        yaml.append("  selector:\n");
        yaml.append("    matchLabels:\n");
        yaml.append("      app: elasticsearch\n");
        yaml.append("  template:\n");
        yaml.append("    metadata:\n");
        yaml.append("      labels:\n");
        yaml.append("        app: elasticsearch\n");
        yaml.append("    spec:\n");
        yaml.append("      containers:\n");
        yaml.append("        - name: elasticsearch\n");
        yaml.append("          image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0\n");
        yaml.append("          ports:\n");
        yaml.append("            - containerPort: 9200\n");
        yaml.append("          env:\n");
        yaml.append("            - name: discovery.type\n");
        yaml.append("              value: single-node\n");
        yaml.append("            - name: xpack.security.enabled\n");
        yaml.append("              value: \"false\"\n");
        yaml.append("            - name: ES_JAVA_OPTS\n");
        yaml.append("              value: \"-Xms512m -Xmx512m\"\n");

        writeFile(k8sPath + "elasticsearch-deployment.yml", yaml.toString());
        writeServiceYaml(k8sPath, "elasticsearch", 9200);
    }

    private void writeServiceYaml(String k8sPath, String name, int port) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: v1\n");
        yaml.append("kind: Service\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(name).append("\n");
        yaml.append("  namespace: ").append(kubernetesNamespace).append("\n");
        yaml.append("spec:\n");
        yaml.append("  type: ClusterIP\n");
        yaml.append("  selector:\n");
        yaml.append("    app: ").append(name).append("\n");
        yaml.append("  ports:\n");
        yaml.append("    - port: ").append(port).append("\n");
        yaml.append("      targetPort: ").append(port).append("\n");

        writeFile(k8sPath + name + "-service.yml", yaml.toString());
    }

    private void writeApplyScript(String k8sPath) throws Exception {
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n\n");
        script.append("# Kubernetes deployment script\n");
        script.append("# Generated by JHipster\n\n");

        script.append("set -e\n\n");

        script.append("echo \"Deploying to Kubernetes...\"\n\n");

        script.append("# Apply namespace\n");
        script.append("kubectl apply -f namespace.yml\n\n");

        script.append("# Apply all manifests\n");
        script.append("kubectl apply -f . --recursive\n\n");

        script.append("echo \"Deployment complete!\"\n");
        script.append("echo \"Run 'kubectl get pods -n ").append(kubernetesNamespace).append("' to check status\"\n");

        writeFile(k8sPath + "kubectl-apply.sh", script.toString());
    }

    private void writeReadme(String k8sPath) throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder readme = new StringBuilder();
        readme.append("# Kubernetes Deployment\n\n");

        readme.append("## Prerequisites\n\n");
        readme.append("- Kubernetes cluster (minikube, kind, GKE, EKS, AKS, etc.)\n");
        readme.append("- kubectl configured\n");
        readme.append("- Docker image built and pushed to registry\n\n");

        readme.append("## Build and Push Docker Image\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw -ntp jib:build -Djib.to.image=").append(dockerRepositoryName);
        readme.append("/").append(config.getLowerBaseName()).append(":latest\n");
        readme.append("```\n\n");

        readme.append("## Deploy to Kubernetes\n\n");
        readme.append("```bash\n");
        readme.append("cd kubernetes\n");
        readme.append("chmod +x kubectl-apply.sh\n");
        readme.append("./kubectl-apply.sh\n");
        readme.append("```\n\n");

        readme.append("## Check Deployment Status\n\n");
        readme.append("```bash\n");
        readme.append("kubectl get pods -n ").append(kubernetesNamespace).append("\n");
        readme.append("kubectl get services -n ").append(kubernetesNamespace).append("\n");
        readme.append("kubectl get ingress -n ").append(kubernetesNamespace).append("\n");
        readme.append("```\n\n");

        readme.append("## Access the Application\n\n");
        readme.append("Add the following to your `/etc/hosts`:\n");
        readme.append("```\n");
        readme.append("<INGRESS_IP> ").append(ingressDomain).append("\n");
        readme.append("```\n\n");

        readme.append("Then access: http://").append(ingressDomain).append("\n\n");

        readme.append("## Cleanup\n\n");
        readme.append("```bash\n");
        readme.append("kubectl delete namespace ").append(kubernetesNamespace).append("\n");
        readme.append("```\n");

        writeFile(k8sPath + "README.md", readme.toString());
    }

    // Helper methods
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

    private String getDbImage(String dbType) {
        switch (dbType) {
            case "postgresql": return "postgres:16";
            case "mysql": return "mysql:8.2";
            case "mariadb": return "mariadb:11.2";
            default: return "postgres:16";
        }
    }

    private String getDbDataPath(String dbType) {
        switch (dbType) {
            case "postgresql": return "/var/lib/postgresql/data";
            case "mysql": return "/var/lib/mysql";
            case "mariadb": return "/var/lib/mysql";
            default: return "/data";
        }
    }

    // Setters for configuration
    public void setKubernetesNamespace(String namespace) {
        this.kubernetesNamespace = namespace;
    }

    public void setDockerRepositoryName(String name) {
        this.dockerRepositoryName = name;
    }

    public void setIstio(boolean istio) {
        this.istio = istio;
    }

    public void setIngressDomain(String domain) {
        this.ingressDomain = domain;
    }
}
