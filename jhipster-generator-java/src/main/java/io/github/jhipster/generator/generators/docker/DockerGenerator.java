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
 * Docker Generator.
 * Generates Docker Compose files for the application and its dependencies.
 * Equivalent to docker/generator.ts.
 */
public class DockerGenerator extends BaseApplicationGenerator {

    public DockerGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingDocker", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Docker configuration files");

        JHipsterConfig config = getConfig();

        // Write Dockerfile
        writeDockerfile();

        // Write app.yml (application docker-compose)
        writeAppYml();

        // Write services based on configuration
        if (config.hasServiceDiscovery()) {
            if (config.isConsul()) {
                writeConsulYml();
            } else if (config.isEureka()) {
                writeJhipsterRegistryYml();
            }
        }

        // Write database compose file
        if (isSql()) {
            writeDatabaseYml();
        }

        // Write services.yml (combined services)
        writeServicesYml();
    }

    private void writeDockerfile() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder dockerfile = new StringBuilder();
        dockerfile.append("# Build stage\n");
        dockerfile.append("FROM eclipse-temurin:17-jdk-jammy AS build\n");
        dockerfile.append("WORKDIR /app\n");
        dockerfile.append("COPY .mvn/ .mvn\n");
        dockerfile.append("COPY mvnw pom.xml ./\n");
        dockerfile.append("RUN ./mvnw dependency:go-offline\n");
        dockerfile.append("COPY src ./src\n");
        dockerfile.append("RUN ./mvnw package -DskipTests\n\n");

        dockerfile.append("# Run stage\n");
        dockerfile.append("FROM eclipse-temurin:17-jre-jammy\n");
        dockerfile.append("WORKDIR /app\n");
        dockerfile.append("COPY --from=build /app/target/*.jar app.jar\n\n");

        dockerfile.append("ENV SPRING_PROFILES_ACTIVE=prod\n");
        dockerfile.append("ENV JAVA_OPTS=\"\"\n\n");

        dockerfile.append("EXPOSE ").append(config.getServerPort()).append("\n\n");

        dockerfile.append("ENTRYPOINT [\"sh\", \"-c\", \"java ${JAVA_OPTS} -jar app.jar\"]\n");

        writeFile(context.getDockerPath() + "Dockerfile", dockerfile.toString());
    }

    private void writeAppYml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# Docker Compose file for ").append(config.getBaseName()).append("\n");
        yml.append("version: '3.8'\n");
        yml.append("services:\n");
        yml.append("  ").append(config.getLowerBaseName()).append("-app:\n");
        yml.append("    image: ").append(config.getLowerBaseName()).append(":latest\n");
        yml.append("    build:\n");
        yml.append("      context: ../..\n");
        yml.append("      dockerfile: src/main/docker/Dockerfile\n");
        yml.append("    environment:\n");
        yml.append("      - SPRING_PROFILES_ACTIVE=prod\n");

        if (isSql()) {
            String prodDb = config.getProdDatabaseType();
            switch (prodDb) {
                case "postgresql":
                    yml.append("      - SPRING_DATASOURCE_URL=jdbc:postgresql://").append(config.getLowerBaseName()).append("-postgresql:5432/").append(config.getLowerBaseName()).append("\n");
                    break;
                case "mysql":
                    yml.append("      - SPRING_DATASOURCE_URL=jdbc:mysql://").append(config.getLowerBaseName()).append("-mysql:3306/").append(config.getLowerBaseName()).append("\n");
                    break;
            }
        }

        if (config.hasServiceDiscovery()) {
            if (config.isConsul()) {
                yml.append("      - SPRING_CLOUD_CONSUL_HOST=consul\n");
                yml.append("      - SPRING_CLOUD_CONSUL_PORT=8500\n");
            } else if (config.isEureka()) {
                yml.append("      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://admin:admin@jhipster-registry:8761/eureka\n");
            }
        }

        yml.append("    ports:\n");
        yml.append("      - \"").append(config.getServerPort()).append(":").append(config.getServerPort()).append("\"\n");

        // Dependencies
        yml.append("    depends_on:\n");
        if (isSql()) {
            String prodDb = config.getProdDatabaseType();
            yml.append("      - ").append(config.getLowerBaseName()).append("-").append(prodDb).append("\n");
        }
        if (config.hasServiceDiscovery()) {
            if (config.isConsul()) {
                yml.append("      - consul\n");
            } else if (config.isEureka()) {
                yml.append("      - jhipster-registry\n");
            }
        }

        writeFile(context.getDockerPath() + "app.yml", yml.toString());
    }

    private void writeConsulYml() throws Exception {
        StringBuilder yml = new StringBuilder();
        yml.append("# Consul Docker Compose configuration\n");
        yml.append("version: '3.8'\n");
        yml.append("services:\n");
        yml.append("  consul:\n");
        yml.append("    image: consul:1.15.4\n");
        yml.append("    command: consul agent -dev -ui -client 0.0.0.0\n");
        yml.append("    ports:\n");
        yml.append("      - \"8500:8500\"\n");
        yml.append("      - \"8600:8600/udp\"\n");
        yml.append("    volumes:\n");
        yml.append("      - consul-data:/consul/data\n");
        yml.append("\n");
        yml.append("volumes:\n");
        yml.append("  consul-data:\n");

        writeFile(context.getDockerPath() + "consul.yml", yml.toString());
    }

    private void writeJhipsterRegistryYml() throws Exception {
        StringBuilder yml = new StringBuilder();
        yml.append("# JHipster Registry (Eureka) Docker Compose configuration\n");
        yml.append("version: '3.8'\n");
        yml.append("services:\n");
        yml.append("  jhipster-registry:\n");
        yml.append("    image: jhipster/jhipster-registry:v7.4.0\n");
        yml.append("    volumes:\n");
        yml.append("      - ./central-server-config:/central-config\n");
        yml.append("    environment:\n");
        yml.append("      - SPRING_PROFILES_ACTIVE=dev\n");
        yml.append("      - SPRING_SECURITY_USER_PASSWORD=admin\n");
        yml.append("      - JHIPSTER_REGISTRY_PASSWORD=admin\n");
        yml.append("      - SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=native\n");
        yml.append("      - SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_SEARCH_LOCATIONS=file:./central-config\n");
        yml.append("    ports:\n");
        yml.append("      - \"8761:8761\"\n");

        writeFile(context.getDockerPath() + "jhipster-registry.yml", yml.toString());
    }

    private void writeDatabaseYml() throws Exception {
        JHipsterConfig config = getConfig();
        String prodDb = config.getProdDatabaseType();

        StringBuilder yml = new StringBuilder();
        yml.append("# Database Docker Compose configuration\n");
        yml.append("version: '3.8'\n");
        yml.append("services:\n");

        switch (prodDb) {
            case "postgresql":
                yml.append("  ").append(config.getLowerBaseName()).append("-postgresql:\n");
                yml.append("    image: postgres:15.4\n");
                yml.append("    environment:\n");
                yml.append("      - POSTGRES_DB=").append(config.getLowerBaseName()).append("\n");
                yml.append("      - POSTGRES_USER=").append(config.getLowerBaseName()).append("\n");
                yml.append("      - POSTGRES_PASSWORD=\n");
                yml.append("    ports:\n");
                yml.append("      - \"5432:5432\"\n");
                yml.append("    volumes:\n");
                yml.append("      - postgresql-data:/var/lib/postgresql/data\n");
                yml.append("\n");
                yml.append("volumes:\n");
                yml.append("  postgresql-data:\n");
                break;

            case "mysql":
                yml.append("  ").append(config.getLowerBaseName()).append("-mysql:\n");
                yml.append("    image: mysql:8.0.33\n");
                yml.append("    environment:\n");
                yml.append("      - MYSQL_DATABASE=").append(config.getLowerBaseName()).append("\n");
                yml.append("      - MYSQL_USER=").append(config.getLowerBaseName()).append("\n");
                yml.append("      - MYSQL_PASSWORD=\n");
                yml.append("      - MYSQL_ALLOW_EMPTY_PASSWORD=yes\n");
                yml.append("    ports:\n");
                yml.append("      - \"3306:3306\"\n");
                yml.append("    volumes:\n");
                yml.append("      - mysql-data:/var/lib/mysql\n");
                yml.append("\n");
                yml.append("volumes:\n");
                yml.append("  mysql-data:\n");
                break;

            case "mariadb":
                yml.append("  ").append(config.getLowerBaseName()).append("-mariadb:\n");
                yml.append("    image: mariadb:10.11\n");
                yml.append("    environment:\n");
                yml.append("      - MARIADB_DATABASE=").append(config.getLowerBaseName()).append("\n");
                yml.append("      - MARIADB_USER=").append(config.getLowerBaseName()).append("\n");
                yml.append("      - MARIADB_PASSWORD=\n");
                yml.append("      - MARIADB_ALLOW_EMPTY_ROOT_PASSWORD=yes\n");
                yml.append("    ports:\n");
                yml.append("      - \"3306:3306\"\n");
                yml.append("    volumes:\n");
                yml.append("      - mariadb-data:/var/lib/mysql\n");
                yml.append("\n");
                yml.append("volumes:\n");
                yml.append("  mariadb-data:\n");
                break;
        }

        writeFile(context.getDockerPath() + prodDb + ".yml", yml.toString());
    }

    private void writeServicesYml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# Combined services Docker Compose configuration\n");
        yml.append("version: '3.8'\n");
        yml.append("services:\n");

        // Database
        if (isSql()) {
            String prodDb = config.getProdDatabaseType();
            switch (prodDb) {
                case "postgresql":
                    yml.append("  ").append(config.getLowerBaseName()).append("-postgresql:\n");
                    yml.append("    extends:\n");
                    yml.append("      file: postgresql.yml\n");
                    yml.append("      service: ").append(config.getLowerBaseName()).append("-postgresql\n");
                    break;
                case "mysql":
                    yml.append("  ").append(config.getLowerBaseName()).append("-mysql:\n");
                    yml.append("    extends:\n");
                    yml.append("      file: mysql.yml\n");
                    yml.append("      service: ").append(config.getLowerBaseName()).append("-mysql\n");
                    break;
            }
        }

        // Service discovery
        if (config.hasServiceDiscovery()) {
            if (config.isConsul()) {
                yml.append("\n  consul:\n");
                yml.append("    extends:\n");
                yml.append("      file: consul.yml\n");
                yml.append("      service: consul\n");
            } else if (config.isEureka()) {
                yml.append("\n  jhipster-registry:\n");
                yml.append("    extends:\n");
                yml.append("      file: jhipster-registry.yml\n");
                yml.append("      service: jhipster-registry\n");
            }
        }

        writeFile(context.getDockerPath() + "services.yml", yml.toString());
    }
}
