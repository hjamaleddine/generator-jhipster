/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.docker;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * Docker Compose Generator.
 * Generates Docker Compose files for multi-container orchestration.
 */
public class DockerComposeGenerator extends BaseApplicationGenerator {

    public DockerComposeGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "docker-compose";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingDockerCompose", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Docker Compose configuration");

        writeDockerCompose();
        writeDockerComposeDev();
        writeDockerComposeProd();

        if (isSql()) {
            writeDatabaseDockerCompose();
        }

        JHipsterConfig config = getConfig();
        if (config.hasServiceDiscovery()) {
            writeServiceDiscoveryDockerCompose();
        }

        if ("kafka".equals(config.getMessageBroker())) {
            writeKafkaDockerCompose();
        }

        if ("elasticsearch".equals(config.getSearchEngine())) {
            writeElasticsearchDockerCompose();
        }
    }

    private void writeDockerCompose() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder compose = new StringBuilder();
        compose.append("# Docker Compose configuration\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");
        compose.append("  ").append(config.getLowerBaseName()).append("-app:\n");
        compose.append("    image: ").append(config.getLowerBaseName()).append("\n");
        compose.append("    environment:\n");
        compose.append("      - _JAVA_OPTIONS=-Xmx512m -Xms256m\n");
        compose.append("      - SPRING_PROFILES_ACTIVE=prod,api-docs\n");

        if (isSql()) {
            compose.append("      - SPRING_DATASOURCE_URL=jdbc:");
            if ("postgresql".equals(config.getProdDatabaseType())) {
                compose.append("postgresql://").append(config.getLowerBaseName()).append("-postgresql:5432/").append(config.getLowerBaseName());
            } else if ("mysql".equals(config.getProdDatabaseType())) {
                compose.append("mysql://").append(config.getLowerBaseName()).append("-mysql:3306/").append(config.getLowerBaseName());
            }
            compose.append("\n");
        }

        if (config.isConsul()) {
            compose.append("      - SPRING_CLOUD_CONSUL_HOST=consul\n");
            compose.append("      - SPRING_CLOUD_CONSUL_PORT=8500\n");
        } else if (config.isEureka()) {
            compose.append("      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://admin:admin@jhipster-registry:8761/eureka\n");
        }

        compose.append("    ports:\n");
        compose.append("      - ").append(config.getServerPort()).append(":").append(config.getServerPort()).append("\n");

        compose.append("    healthcheck:\n");
        compose.append("      test: ['CMD', 'curl', '-f', 'http://localhost:").append(config.getServerPort()).append("/management/health']\n");
        compose.append("      interval: 5s\n");
        compose.append("      timeout: 5s\n");
        compose.append("      retries: 40\n");

        compose.append("    depends_on:\n");
        if (isSql()) {
            if ("postgresql".equals(config.getProdDatabaseType())) {
                compose.append("      ").append(config.getLowerBaseName()).append("-postgresql:\n");
                compose.append("        condition: service_healthy\n");
            } else if ("mysql".equals(config.getProdDatabaseType())) {
                compose.append("      ").append(config.getLowerBaseName()).append("-mysql:\n");
                compose.append("        condition: service_healthy\n");
            }
        }

        writeFile(getDockerPath() + "app.yml", compose.toString());
    }

    private void writeDockerComposeDev() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder compose = new StringBuilder();
        compose.append("# Development Docker Compose configuration\n");
        compose.append("# Run with: docker-compose -f src/main/docker/dev.yml up -d\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");

        // Database service
        if (isSql()) {
            if ("postgresql".equals(config.getProdDatabaseType())) {
                compose.append("  ").append(config.getLowerBaseName()).append("-postgresql:\n");
                compose.append("    extends:\n");
                compose.append("      file: postgresql.yml\n");
                compose.append("      service: ").append(config.getLowerBaseName()).append("-postgresql\n");
            } else if ("mysql".equals(config.getProdDatabaseType())) {
                compose.append("  ").append(config.getLowerBaseName()).append("-mysql:\n");
                compose.append("    extends:\n");
                compose.append("      file: mysql.yml\n");
                compose.append("      service: ").append(config.getLowerBaseName()).append("-mysql\n");
            }
        }

        // Service discovery
        if (config.isConsul()) {
            compose.append("  consul:\n");
            compose.append("    extends:\n");
            compose.append("      file: consul.yml\n");
            compose.append("      service: consul\n");
        }

        // Kafka
        if ("kafka".equals(config.getMessageBroker())) {
            compose.append("  zookeeper:\n");
            compose.append("    extends:\n");
            compose.append("      file: kafka.yml\n");
            compose.append("      service: zookeeper\n");
            compose.append("  kafka:\n");
            compose.append("    extends:\n");
            compose.append("      file: kafka.yml\n");
            compose.append("      service: kafka\n");
        }

        writeFile(getDockerPath() + "dev.yml", compose.toString());
    }

    private void writeDockerComposeProd() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder compose = new StringBuilder();
        compose.append("# Production Docker Compose configuration\n");
        compose.append("# Run with: docker-compose -f src/main/docker/prod.yml up -d\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");
        compose.append("  ").append(config.getLowerBaseName()).append("-app:\n");
        compose.append("    extends:\n");
        compose.append("      file: app.yml\n");
        compose.append("      service: ").append(config.getLowerBaseName()).append("-app\n");

        if (isSql()) {
            if ("postgresql".equals(config.getProdDatabaseType())) {
                compose.append("  ").append(config.getLowerBaseName()).append("-postgresql:\n");
                compose.append("    extends:\n");
                compose.append("      file: postgresql.yml\n");
                compose.append("      service: ").append(config.getLowerBaseName()).append("-postgresql\n");
            } else if ("mysql".equals(config.getProdDatabaseType())) {
                compose.append("  ").append(config.getLowerBaseName()).append("-mysql:\n");
                compose.append("    extends:\n");
                compose.append("      file: mysql.yml\n");
                compose.append("      service: ").append(config.getLowerBaseName()).append("-mysql\n");
            }
        }

        writeFile(getDockerPath() + "prod.yml", compose.toString());
    }

    private void writeDatabaseDockerCompose() throws Exception {
        JHipsterConfig config = getConfig();

        if ("postgresql".equals(config.getProdDatabaseType())) {
            writePostgresqlCompose();
        } else if ("mysql".equals(config.getProdDatabaseType())) {
            writeMysqlCompose();
        }
    }

    private void writePostgresqlCompose() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder compose = new StringBuilder();
        compose.append("# PostgreSQL Docker Compose configuration\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");
        compose.append("  ").append(config.getLowerBaseName()).append("-postgresql:\n");
        compose.append("    image: postgres:15.4\n");
        compose.append("    volumes:\n");
        compose.append("      - postgresql-data:/var/lib/postgresql/data/\n");
        compose.append("    environment:\n");
        compose.append("      - POSTGRES_USER=").append(config.getLowerBaseName()).append("\n");
        compose.append("      - POSTGRES_PASSWORD=\n");
        compose.append("      - POSTGRES_HOST_AUTH_METHOD=trust\n");
        compose.append("    ports:\n");
        compose.append("      - 5432:5432\n");
        compose.append("    healthcheck:\n");
        compose.append("      test: ['CMD-SHELL', 'pg_isready -U ").append(config.getLowerBaseName()).append("']\n");
        compose.append("      interval: 5s\n");
        compose.append("      timeout: 5s\n");
        compose.append("      retries: 10\n\n");

        compose.append("volumes:\n");
        compose.append("  postgresql-data:\n");

        writeFile(getDockerPath() + "postgresql.yml", compose.toString());
    }

    private void writeMysqlCompose() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder compose = new StringBuilder();
        compose.append("# MySQL Docker Compose configuration\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");
        compose.append("  ").append(config.getLowerBaseName()).append("-mysql:\n");
        compose.append("    image: mysql:8.0.33\n");
        compose.append("    volumes:\n");
        compose.append("      - mysql-data:/var/lib/mysql/\n");
        compose.append("    environment:\n");
        compose.append("      - MYSQL_ALLOW_EMPTY_PASSWORD=yes\n");
        compose.append("      - MYSQL_DATABASE=").append(config.getLowerBaseName()).append("\n");
        compose.append("    ports:\n");
        compose.append("      - 3306:3306\n");
        compose.append("    command: mysqld --lower_case_table_names=1 --skip-ssl --character_set_server=utf8mb4 --explicit_defaults_for_timestamp\n");
        compose.append("    healthcheck:\n");
        compose.append("      test: ['CMD', 'mysql', '-e', 'SELECT 1']\n");
        compose.append("      interval: 5s\n");
        compose.append("      timeout: 5s\n");
        compose.append("      retries: 10\n\n");

        compose.append("volumes:\n");
        compose.append("  mysql-data:\n");

        writeFile(getDockerPath() + "mysql.yml", compose.toString());
    }

    private void writeServiceDiscoveryDockerCompose() throws Exception {
        JHipsterConfig config = getConfig();

        if (config.isConsul()) {
            StringBuilder compose = new StringBuilder();
            compose.append("# Consul Docker Compose configuration\n");
            compose.append("version: '3.8'\n\n");

            compose.append("services:\n");
            compose.append("  consul:\n");
            compose.append("    image: hashicorp/consul:1.16.2\n");
            compose.append("    ports:\n");
            compose.append("      - 8300:8300\n");
            compose.append("      - 8500:8500\n");
            compose.append("      - 8600:8600\n");
            compose.append("    command: consul agent -dev -ui -client 0.0.0.0 -log-level=INFO\n");

            writeFile(getDockerPath() + "consul.yml", compose.toString());
        }
    }

    private void writeKafkaDockerCompose() throws Exception {
        StringBuilder compose = new StringBuilder();
        compose.append("# Kafka Docker Compose configuration\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");
        compose.append("  zookeeper:\n");
        compose.append("    image: confluentinc/cp-zookeeper:7.5.0\n");
        compose.append("    environment:\n");
        compose.append("      ZOOKEEPER_CLIENT_PORT: 2181\n");
        compose.append("      ZOOKEEPER_TICK_TIME: 2000\n\n");

        compose.append("  kafka:\n");
        compose.append("    image: confluentinc/cp-kafka:7.5.0\n");
        compose.append("    ports:\n");
        compose.append("      - 9092:9092\n");
        compose.append("    environment:\n");
        compose.append("      KAFKA_BROKER_ID: 1\n");
        compose.append("      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181\n");
        compose.append("      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT\n");
        compose.append("      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092\n");
        compose.append("      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1\n");
        compose.append("      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1\n");
        compose.append("      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1\n");
        compose.append("    depends_on:\n");
        compose.append("      - zookeeper\n");

        writeFile(getDockerPath() + "kafka.yml", compose.toString());
    }

    private void writeElasticsearchDockerCompose() throws Exception {
        StringBuilder compose = new StringBuilder();
        compose.append("# Elasticsearch Docker Compose configuration\n");
        compose.append("version: '3.8'\n\n");

        compose.append("services:\n");
        compose.append("  elasticsearch:\n");
        compose.append("    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.2\n");
        compose.append("    volumes:\n");
        compose.append("      - elasticsearch-data:/usr/share/elasticsearch/data\n");
        compose.append("    ports:\n");
        compose.append("      - 9200:9200\n");
        compose.append("      - 9300:9300\n");
        compose.append("    environment:\n");
        compose.append("      - discovery.type=single-node\n");
        compose.append("      - xpack.security.enabled=false\n");
        compose.append("      - ES_JAVA_OPTS=-Xms512m -Xmx512m\n\n");

        compose.append("volumes:\n");
        compose.append("  elasticsearch-data:\n");

        writeFile(getDockerPath() + "elasticsearch.yml", compose.toString());
    }

    private String getDockerPath() {
        return context.getBasePath().toString() + "/src/main/docker/";
    }
}
