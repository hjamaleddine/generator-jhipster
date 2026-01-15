/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.springboot;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.generators.domain.DomainGenerator;
import io.github.jhipster.generator.generators.springdata.SpringDataRelationalGenerator;
import io.github.jhipster.generator.generators.feign.FeignClientGenerator;
import io.github.jhipster.generator.generators.docker.DockerGenerator;
import io.github.jhipster.generator.template.JavaCodeBuilder;

import java.util.*;

/**
 * Spring Boot Generator.
 * Main orchestrator for all backend-related generators.
 * Equivalent to spring-boot/generator.ts.
 */
public class SpringBootGenerator extends BaseApplicationGenerator {

    public SpringBootGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-boot";
    }

    @Override
    protected void beforeQueue() {
        // Depend on domain generator for entity generation
        dependsOn(new DomainGenerator(context));
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.COMPOSING, "composingSpringBoot", this::composing);
        registerTask(GeneratorPriority.PREPARING, "preparingSpringBoot", this::preparing);
        registerTask(GeneratorPriority.WRITING, "writingSpringBoot", this::writing);
        registerTask(GeneratorPriority.POST_WRITING, "postWritingSpringBoot", this::postWriting);
    }

    private void composing() {
        log.info("Composing Spring Boot sub-generators");

        JHipsterConfig config = getConfig();

        // Docker generator
        composeWith(new DockerGenerator(context));

        // Database generator based on type
        if (isSql()) {
            composeWith(new SpringDataRelationalGenerator(context));
        }
        // Add other database generators: MongoDB, Cassandra, etc.

        // Feign client for microservices (non-reactive)
        if (isMicroservice() && !isReactive() && Boolean.TRUE.equals(config.getFeignClient())) {
            composeWith(new FeignClientGenerator(context));
        }

        // Add other generators based on configuration:
        // - Spring Cloud Stream (message broker)
        // - Spring Data Elasticsearch (search engine)
        // - Spring Cache (cache provider)
        // - Spring WebSocket
    }

    private void preparing() {
        log.info("Preparing Spring Boot configuration");

        // Prepare database connection properties
        prepareDatabaseProperties();

        // Prepare authentication properties
        prepareAuthenticationProperties();

        // Prepare service discovery properties
        if (hasServiceDiscovery()) {
            prepareServiceDiscoveryProperties();
        }
    }

    private void writing() throws Exception {
        log.info("Writing Spring Boot files");

        // Write pom.xml
        writePomXml();

        // Write main application class
        writeMainApplicationClass();

        // Write application.yml
        writeApplicationYml();

        // Write configuration classes
        writeConfigurationClasses();

        // Write security configuration
        writeSecurityConfiguration();

        // Write logging configuration
        writeLogbackConfiguration();
    }

    private void postWriting() {
        log.info("Post-writing Spring Boot tasks");
        // Add any post-writing tasks here
    }

    private void prepareDatabaseProperties() {
        JHipsterConfig config = getConfig();

        if (isSql()) {
            String devDb = config.getDevDatabaseType();
            String prodDb = config.getProdDatabaseType();

            // Dev database URL
            switch (devDb) {
                case "h2Disk":
                    context.setConfigValue("devDatabaseUrl", "jdbc:h2:file:./target/h2db/db/" + config.getLowerBaseName());
                    context.setConfigValue("devDatabaseDriver", "org.h2.Driver");
                    break;
                case "h2Memory":
                    context.setConfigValue("devDatabaseUrl", "jdbc:h2:mem:" + config.getLowerBaseName());
                    context.setConfigValue("devDatabaseDriver", "org.h2.Driver");
                    break;
                case "postgresql":
                    context.setConfigValue("devDatabaseUrl", "jdbc:postgresql://localhost:5432/" + config.getLowerBaseName());
                    context.setConfigValue("devDatabaseDriver", "org.postgresql.Driver");
                    break;
                case "mysql":
                    context.setConfigValue("devDatabaseUrl", "jdbc:mysql://localhost:3306/" + config.getLowerBaseName());
                    context.setConfigValue("devDatabaseDriver", "com.mysql.cj.jdbc.Driver");
                    break;
            }

            // Prod database URL
            switch (prodDb) {
                case "postgresql":
                    context.setConfigValue("prodDatabaseUrl", "jdbc:postgresql://localhost:5432/" + config.getLowerBaseName());
                    context.setConfigValue("prodDatabaseDriver", "org.postgresql.Driver");
                    break;
                case "mysql":
                    context.setConfigValue("prodDatabaseUrl", "jdbc:mysql://localhost:3306/" + config.getLowerBaseName());
                    context.setConfigValue("prodDatabaseDriver", "com.mysql.cj.jdbc.Driver");
                    break;
                case "mariadb":
                    context.setConfigValue("prodDatabaseUrl", "jdbc:mariadb://localhost:3306/" + config.getLowerBaseName());
                    context.setConfigValue("prodDatabaseDriver", "org.mariadb.jdbc.Driver");
                    break;
            }
        }
    }

    private void prepareAuthenticationProperties() {
        JHipsterConfig config = getConfig();

        if (config.isJwt()) {
            context.setConfigValue("jwtSecretKey", config.getJwtSecretKey());
            context.setConfigValue("jwtTokenValidityInSeconds", 86400); // 24 hours
            context.setConfigValue("jwtTokenValidityInSecondsForRememberMe", 2592000); // 30 days
        }
    }

    private void prepareServiceDiscoveryProperties() {
        JHipsterConfig config = getConfig();

        if (config.isConsul()) {
            context.setConfigValue("consulHost", "localhost");
            context.setConfigValue("consulPort", 8500);
        } else if (config.isEureka()) {
            context.setConfigValue("eurekaHost", "localhost");
            context.setConfigValue("eurekaPort", 8761);
        }
    }

    private void writePomXml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder pom = new StringBuilder();
        pom.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        pom.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        pom.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        pom.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        pom.append("    <modelVersion>4.0.0</modelVersion>\n\n");

        pom.append("    <parent>\n");
        pom.append("        <groupId>org.springframework.boot</groupId>\n");
        pom.append("        <artifactId>spring-boot-starter-parent</artifactId>\n");
        pom.append("        <version>3.2.1</version>\n");
        pom.append("        <relativePath/>\n");
        pom.append("    </parent>\n\n");

        pom.append("    <groupId>").append(config.getPackageName()).append("</groupId>\n");
        pom.append("    <artifactId>").append(config.getLowerBaseName()).append("</artifactId>\n");
        pom.append("    <version>0.0.1-SNAPSHOT</version>\n");
        pom.append("    <packaging>jar</packaging>\n");
        pom.append("    <name>").append(config.getBaseName()).append("</name>\n\n");

        pom.append("    <properties>\n");
        pom.append("        <java.version>17</java.version>\n");
        pom.append("        <spring-cloud.version>2023.0.0</spring-cloud.version>\n");
        pom.append("        <mapstruct.version>1.5.5.Final</mapstruct.version>\n");
        pom.append("    </properties>\n\n");

        // Dependencies
        pom.append("    <dependencies>\n");

        // Spring Boot Starters
        pom.append("        <!-- Spring Boot Starters -->\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.springframework.boot</groupId>\n");
        pom.append("            <artifactId>spring-boot-starter-web</artifactId>\n");
        pom.append("        </dependency>\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.springframework.boot</groupId>\n");
        pom.append("            <artifactId>spring-boot-starter-validation</artifactId>\n");
        pom.append("        </dependency>\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.springframework.boot</groupId>\n");
        pom.append("            <artifactId>spring-boot-starter-actuator</artifactId>\n");
        pom.append("        </dependency>\n");

        // Security
        pom.append("\n        <!-- Security -->\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.springframework.boot</groupId>\n");
        pom.append("            <artifactId>spring-boot-starter-security</artifactId>\n");
        pom.append("        </dependency>\n");

        if (config.isJwt()) {
            pom.append("        <dependency>\n");
            pom.append("            <groupId>io.jsonwebtoken</groupId>\n");
            pom.append("            <artifactId>jjwt-api</artifactId>\n");
            pom.append("            <version>0.12.3</version>\n");
            pom.append("        </dependency>\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>io.jsonwebtoken</groupId>\n");
            pom.append("            <artifactId>jjwt-impl</artifactId>\n");
            pom.append("            <version>0.12.3</version>\n");
            pom.append("            <scope>runtime</scope>\n");
            pom.append("        </dependency>\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>io.jsonwebtoken</groupId>\n");
            pom.append("            <artifactId>jjwt-jackson</artifactId>\n");
            pom.append("            <version>0.12.3</version>\n");
            pom.append("            <scope>runtime</scope>\n");
            pom.append("        </dependency>\n");
        }

        // Database
        if (isSql()) {
            pom.append("\n        <!-- Database -->\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>org.springframework.boot</groupId>\n");
            pom.append("            <artifactId>spring-boot-starter-data-jpa</artifactId>\n");
            pom.append("        </dependency>\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>org.liquibase</groupId>\n");
            pom.append("            <artifactId>liquibase-core</artifactId>\n");
            pom.append("        </dependency>\n");

            // H2 for dev
            if ("h2Disk".equals(config.getDevDatabaseType()) || "h2Memory".equals(config.getDevDatabaseType())) {
                pom.append("        <dependency>\n");
                pom.append("            <groupId>com.h2database</groupId>\n");
                pom.append("            <artifactId>h2</artifactId>\n");
                pom.append("            <scope>runtime</scope>\n");
                pom.append("        </dependency>\n");
            }

            // Production database driver
            switch (config.getProdDatabaseType()) {
                case "postgresql":
                    pom.append("        <dependency>\n");
                    pom.append("            <groupId>org.postgresql</groupId>\n");
                    pom.append("            <artifactId>postgresql</artifactId>\n");
                    pom.append("            <scope>runtime</scope>\n");
                    pom.append("        </dependency>\n");
                    break;
                case "mysql":
                    pom.append("        <dependency>\n");
                    pom.append("            <groupId>com.mysql</groupId>\n");
                    pom.append("            <artifactId>mysql-connector-j</artifactId>\n");
                    pom.append("            <scope>runtime</scope>\n");
                    pom.append("        </dependency>\n");
                    break;
            }
        }

        // Service Discovery
        if (config.hasServiceDiscovery()) {
            pom.append("\n        <!-- Service Discovery -->\n");
            if (config.isConsul()) {
                pom.append("        <dependency>\n");
                pom.append("            <groupId>org.springframework.cloud</groupId>\n");
                pom.append("            <artifactId>spring-cloud-starter-consul-discovery</artifactId>\n");
                pom.append("        </dependency>\n");
                pom.append("        <dependency>\n");
                pom.append("            <groupId>org.springframework.cloud</groupId>\n");
                pom.append("            <artifactId>spring-cloud-starter-consul-config</artifactId>\n");
                pom.append("        </dependency>\n");
            } else if (config.isEureka()) {
                pom.append("        <dependency>\n");
                pom.append("            <groupId>org.springframework.cloud</groupId>\n");
                pom.append("            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>\n");
                pom.append("        </dependency>\n");
            }
        }

        // Feign Client
        if (Boolean.TRUE.equals(config.getFeignClient())) {
            pom.append("\n        <!-- Feign Client -->\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>org.springframework.cloud</groupId>\n");
            pom.append("            <artifactId>spring-cloud-starter-openfeign</artifactId>\n");
            pom.append("        </dependency>\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>org.springframework.cloud</groupId>\n");
            pom.append("            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>\n");
            pom.append("        </dependency>\n");
        }

        // MapStruct
        pom.append("\n        <!-- MapStruct -->\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.mapstruct</groupId>\n");
        pom.append("            <artifactId>mapstruct</artifactId>\n");
        pom.append("            <version>${mapstruct.version}</version>\n");
        pom.append("        </dependency>\n");

        // Testing
        pom.append("\n        <!-- Testing -->\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.springframework.boot</groupId>\n");
        pom.append("            <artifactId>spring-boot-starter-test</artifactId>\n");
        pom.append("            <scope>test</scope>\n");
        pom.append("        </dependency>\n");
        pom.append("        <dependency>\n");
        pom.append("            <groupId>org.springframework.security</groupId>\n");
        pom.append("            <artifactId>spring-security-test</artifactId>\n");
        pom.append("            <scope>test</scope>\n");
        pom.append("        </dependency>\n");

        pom.append("    </dependencies>\n\n");

        // Dependency Management for Spring Cloud
        if (config.hasServiceDiscovery() || Boolean.TRUE.equals(config.getFeignClient())) {
            pom.append("    <dependencyManagement>\n");
            pom.append("        <dependencies>\n");
            pom.append("            <dependency>\n");
            pom.append("                <groupId>org.springframework.cloud</groupId>\n");
            pom.append("                <artifactId>spring-cloud-dependencies</artifactId>\n");
            pom.append("                <version>${spring-cloud.version}</version>\n");
            pom.append("                <type>pom</type>\n");
            pom.append("                <scope>import</scope>\n");
            pom.append("            </dependency>\n");
            pom.append("        </dependencies>\n");
            pom.append("    </dependencyManagement>\n\n");
        }

        // Build
        pom.append("    <build>\n");
        pom.append("        <plugins>\n");
        pom.append("            <plugin>\n");
        pom.append("                <groupId>org.springframework.boot</groupId>\n");
        pom.append("                <artifactId>spring-boot-maven-plugin</artifactId>\n");
        pom.append("            </plugin>\n");
        pom.append("            <plugin>\n");
        pom.append("                <groupId>org.apache.maven.plugins</groupId>\n");
        pom.append("                <artifactId>maven-compiler-plugin</artifactId>\n");
        pom.append("                <configuration>\n");
        pom.append("                    <annotationProcessorPaths>\n");
        pom.append("                        <path>\n");
        pom.append("                            <groupId>org.mapstruct</groupId>\n");
        pom.append("                            <artifactId>mapstruct-processor</artifactId>\n");
        pom.append("                            <version>${mapstruct.version}</version>\n");
        pom.append("                        </path>\n");
        pom.append("                    </annotationProcessorPaths>\n");
        pom.append("                </configuration>\n");
        pom.append("            </plugin>\n");
        pom.append("        </plugins>\n");
        pom.append("    </build>\n\n");

        // Profiles
        pom.append("    <profiles>\n");
        pom.append("        <profile>\n");
        pom.append("            <id>dev</id>\n");
        pom.append("            <activation>\n");
        pom.append("                <activeByDefault>true</activeByDefault>\n");
        pom.append("            </activation>\n");
        pom.append("            <properties>\n");
        pom.append("                <spring.profiles.active>dev</spring.profiles.active>\n");
        pom.append("            </properties>\n");
        pom.append("        </profile>\n");
        pom.append("        <profile>\n");
        pom.append("            <id>prod</id>\n");
        pom.append("            <properties>\n");
        pom.append("                <spring.profiles.active>prod</spring.profiles.active>\n");
        pom.append("            </properties>\n");
        pom.append("        </profile>\n");
        pom.append("    </profiles>\n");

        pom.append("</project>\n");

        writeFile("pom.xml", pom.toString());
    }

    private void writeMainApplicationClass() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName());

        builder.addImports(
            "org.springframework.boot.SpringApplication",
            "org.springframework.boot.autoconfigure.SpringBootApplication",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory"
        );

        if (Boolean.TRUE.equals(config.getFeignClient())) {
            builder.addImport("org.springframework.cloud.openfeign.EnableFeignClients");
        }

        builder.javadoc("Main application class for " + config.getBaseName() + ".");
        builder.annotation("SpringBootApplication");
        if (Boolean.TRUE.equals(config.getFeignClient())) {
            builder.annotation("EnableFeignClients");
        }

        builder.classDeclaration("public", config.getMainClass(), null);

        builder.staticFinalField("Logger", "log", "LoggerFactory.getLogger(" + config.getMainClass() + ".class)");
        builder.line();

        builder.methodSignature("public static", "void", "main", "String[] args");
        builder.statement("SpringApplication.run(" + config.getMainClass() + ".class, args)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + config.getMainClass() + ".java", builder.build());
    }

    private void writeApplicationYml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# ===================================================================\n");
        yml.append("# Spring Boot configuration.\n");
        yml.append("# ===================================================================\n\n");

        yml.append("spring:\n");
        yml.append("  application:\n");
        yml.append("    name: ").append(config.getLowerBaseName()).append("\n");
        yml.append("  profiles:\n");
        yml.append("    active: dev\n");

        if (isSql()) {
            yml.append("  jpa:\n");
            yml.append("    open-in-view: false\n");
            yml.append("    hibernate:\n");
            yml.append("      ddl-auto: none\n");
            yml.append("      naming:\n");
            yml.append("        physical-strategy: org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy\n");
            yml.append("        implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy\n");
            yml.append("  liquibase:\n");
            yml.append("    change-log: classpath:config/liquibase/master.xml\n");
        }

        yml.append("\nserver:\n");
        yml.append("  port: ").append(config.getServerPort()).append("\n");

        // Service Discovery
        if (config.isConsul()) {
            yml.append("\n  cloud:\n");
            yml.append("    consul:\n");
            yml.append("      discovery:\n");
            yml.append("        healthCheckPath: /management/health\n");
            yml.append("        instanceId: ").append(config.getLowerBaseName()).append(":${spring.application.instance-id:${random.value}}\n");
            yml.append("        service-name: ").append(config.getLowerBaseName()).append("\n");
            yml.append("      config:\n");
            yml.append("        watch:\n");
            yml.append("          enabled: false\n");
        } else if (config.isEureka()) {
            yml.append("\neureka:\n");
            yml.append("  client:\n");
            yml.append("    enabled: true\n");
            yml.append("    healthcheck:\n");
            yml.append("      enabled: true\n");
            yml.append("    fetch-registry: true\n");
            yml.append("    register-with-eureka: true\n");
            yml.append("  instance:\n");
            yml.append("    appname: ").append(config.getLowerBaseName()).append("\n");
            yml.append("    instanceId: ").append(config.getLowerBaseName()).append(":${spring.application.instance-id:${random.value}}\n");
        }

        // Feign
        if (Boolean.TRUE.equals(config.getFeignClient())) {
            yml.append("\nfeign:\n");
            yml.append("  circuitbreaker:\n");
            yml.append("    enabled: true\n");
        }

        // Management
        yml.append("\nmanagement:\n");
        yml.append("  endpoints:\n");
        yml.append("    web:\n");
        yml.append("      base-path: /management\n");
        yml.append("      exposure:\n");
        yml.append("        include: health,info,metrics,prometheus\n");
        yml.append("  endpoint:\n");
        yml.append("    health:\n");
        yml.append("      show-details: when_authorized\n");
        yml.append("      probes:\n");
        yml.append("        enabled: true\n");

        // Application specific
        yml.append("\n# ===================================================================\n");
        yml.append("# Application specific properties\n");
        yml.append("# ===================================================================\n\n");

        yml.append("jhipster:\n");
        if (config.isJwt()) {
            yml.append("  security:\n");
            yml.append("    authentication:\n");
            yml.append("      jwt:\n");
            yml.append("        base64-secret: ").append(config.getJwtSecretKey()).append("\n");
            yml.append("        token-validity-in-seconds: 86400\n");
            yml.append("        token-validity-in-seconds-for-remember-me: 2592000\n");
        }

        writeFile(getMainResourcesPath() + "config/application.yml", yml.toString());

        // Write dev profile
        writeApplicationDevYml();

        // Write prod profile
        writeApplicationProdYml();
    }

    private void writeApplicationDevYml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# ===================================================================\n");
        yml.append("# Spring Boot configuration for the \"dev\" profile.\n");
        yml.append("# ===================================================================\n\n");

        yml.append("logging:\n");
        yml.append("  level:\n");
        yml.append("    ROOT: DEBUG\n");
        yml.append("    ").append(config.getPackageName()).append(": DEBUG\n");

        if (isSql()) {
            yml.append("\nspring:\n");
            yml.append("  datasource:\n");

            String devDb = config.getDevDatabaseType();
            if ("h2Disk".equals(devDb) || "h2Memory".equals(devDb)) {
                yml.append("    url: ").append(context.getConfigValue("devDatabaseUrl", "")).append("\n");
                yml.append("    username: ").append(config.getLowerBaseName()).append("\n");
                yml.append("    password:\n");
                yml.append("  h2:\n");
                yml.append("    console:\n");
                yml.append("      enabled: true\n");
                yml.append("      path: /h2-console\n");
            }
        }

        writeFile(getMainResourcesPath() + "config/application-dev.yml", yml.toString());
    }

    private void writeApplicationProdYml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# ===================================================================\n");
        yml.append("# Spring Boot configuration for the \"prod\" profile.\n");
        yml.append("# ===================================================================\n\n");

        yml.append("logging:\n");
        yml.append("  level:\n");
        yml.append("    ROOT: INFO\n");
        yml.append("    ").append(config.getPackageName()).append(": INFO\n");

        if (isSql()) {
            yml.append("\nspring:\n");
            yml.append("  datasource:\n");
            yml.append("    url: ").append(context.getConfigValue("prodDatabaseUrl", "")).append("\n");
            yml.append("    username: ").append(config.getLowerBaseName()).append("\n");
            yml.append("    password:\n");
        }

        writeFile(getMainResourcesPath() + "config/application-prod.yml", yml.toString());
    }

    private void writeConfigurationClasses() throws Exception {
        writeApplicationPropertiesClass();
        writeAsyncConfigurationClass();
        writeJacksonConfigurationClass();
        writeWebConfigurerClass();
    }

    private void writeApplicationPropertiesClass() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.boot.context.properties.ConfigurationProperties",
            "org.springframework.stereotype.Component"
        );

        builder.javadoc("Application-specific properties.");
        builder.annotation("Component");
        builder.annotation("ConfigurationProperties", "prefix = \"jhipster\"");

        builder.classDeclaration("public", "ApplicationProperties", null);

        // Security inner class for JWT
        if (config.isJwt()) {
            builder.line("private final Security security = new Security();");
            builder.line();
            builder.methodSignature("public", "Security", "getSecurity");
            builder.returnStatement("security");
            builder.closeMethod();
            builder.line();

            builder.line("public static class Security {");
            builder.indent();
            builder.line("private final Authentication authentication = new Authentication();");
            builder.line();
            builder.methodSignature("public", "Authentication", "getAuthentication");
            builder.returnStatement("authentication");
            builder.closeMethod();
            builder.line();

            builder.line("public static class Authentication {");
            builder.indent();
            builder.line("private final Jwt jwt = new Jwt();");
            builder.line();
            builder.methodSignature("public", "Jwt", "getJwt");
            builder.returnStatement("jwt");
            builder.closeMethod();
            builder.line();

            builder.line("public static class Jwt {");
            builder.indent();
            builder.field("private", "String", "base64Secret");
            builder.field("private", "long", "tokenValidityInSeconds", "86400");
            builder.field("private", "long", "tokenValidityInSecondsForRememberMe", "2592000");
            builder.line();

            // Getters and setters for Jwt
            builder.methodSignature("public", "String", "getBase64Secret");
            builder.returnStatement("base64Secret");
            builder.closeMethod();
            builder.methodSignature("public", "void", "setBase64Secret", "String base64Secret");
            builder.statement("this.base64Secret = base64Secret");
            builder.closeMethod();
            builder.methodSignature("public", "long", "getTokenValidityInSeconds");
            builder.returnStatement("tokenValidityInSeconds");
            builder.closeMethod();
            builder.methodSignature("public", "void", "setTokenValidityInSeconds", "long tokenValidityInSeconds");
            builder.statement("this.tokenValidityInSeconds = tokenValidityInSeconds");
            builder.closeMethod();
            builder.methodSignature("public", "long", "getTokenValidityInSecondsForRememberMe");
            builder.returnStatement("tokenValidityInSecondsForRememberMe");
            builder.closeMethod();
            builder.methodSignature("public", "void", "setTokenValidityInSecondsForRememberMe", "long tokenValidityInSecondsForRememberMe");
            builder.statement("this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe");
            builder.closeMethod();

            builder.outdent();
            builder.line("}"); // Jwt
            builder.outdent();
            builder.line("}"); // Authentication
            builder.outdent();
            builder.line("}"); // Security
        }

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/ApplicationProperties.java", builder.build());
    }

    private void writeAsyncConfigurationClass() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.scheduling.annotation.EnableAsync",
            "org.springframework.scheduling.annotation.EnableScheduling"
        );

        builder.javadoc("Async configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableAsync");
        builder.annotation("EnableScheduling");

        builder.classDeclaration("public", "AsyncConfiguration", null);
        builder.closeClass();

        writeFile(getMainJavaPath() + "config/AsyncConfiguration.java", builder.build());
    }

    private void writeJacksonConfigurationClass() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration"
        );

        builder.javadoc("Jackson configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "JacksonConfiguration", null);

        builder.javadoc("Support for Java date and time API.", "@return the corresponding Jackson module.");
        builder.annotation("Bean");
        builder.methodSignature("public", "JavaTimeModule", "javaTimeModule");
        builder.returnStatement("new JavaTimeModule()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/JacksonConfiguration.java", builder.build());
    }

    private void writeWebConfigurerClass() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.web.servlet.config.annotation.WebMvcConfigurer"
        );

        builder.javadoc("Web MVC configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "WebConfigurer", null, "WebMvcConfigurer");
        builder.lineComment("Add web configuration here");
        builder.closeClass();

        writeFile(getMainJavaPath() + "config/WebConfigurer.java", builder.build());
    }

    private void writeSecurityConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity",
            "org.springframework.security.config.annotation.web.builders.HttpSecurity",
            "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity",
            "org.springframework.security.config.http.SessionCreationPolicy",
            "org.springframework.security.web.SecurityFilterChain"
        );

        builder.javadoc("Security configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableWebSecurity");
        builder.annotation("EnableMethodSecurity", "securedEnabled = true");

        builder.classDeclaration("public", "SecurityConfiguration", null);

        builder.annotation("Bean");
        builder.methodSignature("public", "SecurityFilterChain", "securityFilterChain", "HttpSecurity http");
        builder.line("http");
        builder.indent();
        builder.line(".csrf(csrf -> csrf.disable())");
        builder.line(".sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))");
        builder.line(".authorizeHttpRequests(auth -> auth");
        builder.indent();
        builder.line(".requestMatchers(\"/management/**\").permitAll()");
        builder.line(".requestMatchers(\"/api/authenticate\").permitAll()");
        builder.line(".requestMatchers(\"/api/**\").authenticated()");
        builder.line(".anyRequest().permitAll()");
        builder.outdent();
        builder.line(");");
        builder.outdent();
        builder.returnStatement("http.build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/SecurityConfiguration.java", builder.build());
    }

    private void writeLogbackConfiguration() throws Exception {
        StringBuilder logback = new StringBuilder();
        logback.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        logback.append("<configuration scan=\"true\">\n");
        logback.append("    <include resource=\"org/springframework/boot/logging/logback/defaults.xml\"/>\n");
        logback.append("    <include resource=\"org/springframework/boot/logging/logback/console-appender.xml\"/>\n\n");
        logback.append("    <root level=\"INFO\">\n");
        logback.append("        <appender-ref ref=\"CONSOLE\"/>\n");
        logback.append("    </root>\n\n");
        logback.append("    <logger name=\"").append(getConfig().getPackageName()).append("\" level=\"DEBUG\"/>\n");
        logback.append("</configuration>\n");

        writeFile(getMainResourcesPath() + "logback-spring.xml", logback.toString());
    }
}
