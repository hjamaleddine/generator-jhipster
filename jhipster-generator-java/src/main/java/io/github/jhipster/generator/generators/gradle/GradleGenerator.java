/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.gradle;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * Gradle Generator.
 * Generates Gradle build configuration files.
 * Equivalent to gradle/generator.ts.
 */
public class GradleGenerator extends BaseApplicationGenerator {

    private static final String SPRING_BOOT_VERSION = "3.2.1";
    private static final String SPRING_CLOUD_VERSION = "2023.0.0";
    private static final String GRADLE_VERSION = "8.5";

    public GradleGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "gradle";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingGradle", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Gradle build files");

        JHipsterConfig config = getConfig();

        // Write build.gradle
        writeBuildGradle(config);

        // Write settings.gradle
        writeSettingsGradle(config);

        // Write gradle.properties
        writeGradleProperties(config);

        // Write version catalog
        writeVersionCatalog(config);

        // Write Gradle wrapper
        writeGradleWrapper();

        // Write gradlew scripts
        writeGradlewScripts();
    }

    private void writeBuildGradle(JHipsterConfig config) throws Exception {
        StringBuilder build = new StringBuilder();

        build.append("plugins {\n");
        build.append("    id 'java'\n");
        build.append("    id 'org.springframework.boot' version '").append(SPRING_BOOT_VERSION).append("'\n");
        build.append("    id 'io.spring.dependency-management' version '1.1.4'\n");
        build.append("    id 'jacoco'\n");
        build.append("    id 'checkstyle'\n");

        if ("mapstruct".equals(config.getDto())) {
            build.append("    id 'net.ltgt.apt' version '0.21'\n");
        }

        build.append("}\n\n");

        build.append("group = '").append(config.getPackageName()).append("'\n");
        build.append("version = '0.0.1-SNAPSHOT'\n\n");

        build.append("java {\n");
        build.append("    sourceCompatibility = '17'\n");
        build.append("}\n\n");

        build.append("configurations {\n");
        build.append("    compileOnly {\n");
        build.append("        extendsFrom annotationProcessor\n");
        build.append("    }\n");
        build.append("}\n\n");

        build.append("repositories {\n");
        build.append("    mavenCentral()\n");
        build.append("}\n\n");

        // Dependency Management
        if (config.hasServiceDiscovery() || Boolean.TRUE.equals(config.getFeignClient())) {
            build.append("dependencyManagement {\n");
            build.append("    imports {\n");
            build.append("        mavenBom \"org.springframework.cloud:spring-cloud-dependencies:").append(SPRING_CLOUD_VERSION).append("\"\n");
            build.append("    }\n");
            build.append("}\n\n");
        }

        build.append("dependencies {\n");

        // Spring Boot Starters
        build.append("    // Spring Boot Starters\n");
        build.append("    implementation 'org.springframework.boot:spring-boot-starter-web'\n");
        build.append("    implementation 'org.springframework.boot:spring-boot-starter-validation'\n");
        build.append("    implementation 'org.springframework.boot:spring-boot-starter-actuator'\n");
        build.append("    implementation 'org.springframework.boot:spring-boot-starter-security'\n");

        // Database
        if (isSql()) {
            build.append("\n    // Database\n");
            build.append("    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'\n");
            build.append("    implementation 'org.liquibase:liquibase-core'\n");

            if ("h2Disk".equals(config.getDevDatabaseType()) || "h2Memory".equals(config.getDevDatabaseType())) {
                build.append("    runtimeOnly 'com.h2database:h2'\n");
            }

            switch (config.getProdDatabaseType()) {
                case "postgresql":
                    build.append("    runtimeOnly 'org.postgresql:postgresql'\n");
                    break;
                case "mysql":
                    build.append("    runtimeOnly 'com.mysql:mysql-connector-j'\n");
                    break;
                case "mariadb":
                    build.append("    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'\n");
                    break;
            }
        }

        // MongoDB
        if ("mongodb".equals(config.getDatabaseType())) {
            build.append("\n    // MongoDB\n");
            build.append("    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'\n");
            build.append("    implementation 'io.mongock:mongock-springboot-v3:5.4.0'\n");
            build.append("    implementation 'io.mongock:mongodb-springdata-v4-driver:5.4.0'\n");
        }

        // Security
        if (config.isJwt()) {
            build.append("\n    // JWT Security\n");
            build.append("    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'\n");
            build.append("    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'\n");
            build.append("    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'\n");
        }

        // Service Discovery
        if (config.hasServiceDiscovery()) {
            build.append("\n    // Service Discovery\n");
            if (config.isConsul()) {
                build.append("    implementation 'org.springframework.cloud:spring-cloud-starter-consul-discovery'\n");
                build.append("    implementation 'org.springframework.cloud:spring-cloud-starter-consul-config'\n");
            } else if (config.isEureka()) {
                build.append("    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'\n");
            }
        }

        // Feign Client
        if (Boolean.TRUE.equals(config.getFeignClient())) {
            build.append("\n    // Feign Client\n");
            build.append("    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'\n");
            build.append("    implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'\n");
        }

        // Kafka
        if ("kafka".equals(config.getMessageBroker())) {
            build.append("\n    // Kafka\n");
            build.append("    implementation 'org.springframework.cloud:spring-cloud-stream'\n");
            build.append("    implementation 'org.springframework.cloud:spring-cloud-stream-binder-kafka'\n");
        }

        // Elasticsearch
        if ("elasticsearch".equals(config.getSearchEngine())) {
            build.append("\n    // Elasticsearch\n");
            build.append("    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'\n");
        }

        // WebSocket
        if ("spring-websocket".equals(config.getWebsocket())) {
            build.append("\n    // WebSocket\n");
            build.append("    implementation 'org.springframework.boot:spring-boot-starter-websocket'\n");
            build.append("    implementation 'org.springframework.security:spring-security-messaging'\n");
        }

        // Cache
        if (config.getCacheProvider() != null && !"no".equals(config.getCacheProvider())) {
            build.append("\n    // Cache\n");
            switch (config.getCacheProvider()) {
                case "ehcache":
                    build.append("    implementation 'org.hibernate.orm:hibernate-jcache'\n");
                    build.append("    implementation 'org.ehcache:ehcache'\n");
                    break;
                case "caffeine":
                    build.append("    implementation 'com.github.ben-manes.caffeine:caffeine'\n");
                    break;
                case "redis":
                    build.append("    implementation 'org.springframework.boot:spring-boot-starter-data-redis'\n");
                    break;
                case "hazelcast":
                    build.append("    implementation 'com.hazelcast:hazelcast'\n");
                    build.append("    implementation 'com.hazelcast:hazelcast-spring'\n");
                    break;
            }
        }

        // MapStruct
        if ("mapstruct".equals(config.getDto())) {
            build.append("\n    // MapStruct\n");
            build.append("    implementation 'org.mapstruct:mapstruct:1.5.5.Final'\n");
            build.append("    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'\n");
        }

        // OpenAPI
        build.append("\n    // OpenAPI\n");
        build.append("    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'\n");

        // Metrics
        build.append("\n    // Metrics\n");
        build.append("    implementation 'io.micrometer:micrometer-registry-prometheus'\n");

        // Testing
        build.append("\n    // Testing\n");
        build.append("    testImplementation 'org.springframework.boot:spring-boot-starter-test'\n");
        build.append("    testImplementation 'org.springframework.security:spring-security-test'\n");
        build.append("    testImplementation 'org.testcontainers:junit-jupiter'\n");

        if (isSql()) {
            switch (config.getProdDatabaseType()) {
                case "postgresql":
                    build.append("    testImplementation 'org.testcontainers:postgresql'\n");
                    break;
                case "mysql":
                    build.append("    testImplementation 'org.testcontainers:mysql'\n");
                    break;
                case "mariadb":
                    build.append("    testImplementation 'org.testcontainers:mariadb'\n");
                    break;
            }
        }

        if ("mongodb".equals(config.getDatabaseType())) {
            build.append("    testImplementation 'org.testcontainers:mongodb'\n");
        }

        if ("kafka".equals(config.getMessageBroker())) {
            build.append("    testImplementation 'org.testcontainers:kafka'\n");
        }

        if ("elasticsearch".equals(config.getSearchEngine())) {
            build.append("    testImplementation 'org.testcontainers:elasticsearch'\n");
        }

        build.append("}\n\n");

        // Tasks
        build.append("tasks.named('test') {\n");
        build.append("    useJUnitPlatform()\n");
        build.append("}\n\n");

        build.append("jacocoTestReport {\n");
        build.append("    dependsOn test\n");
        build.append("    reports {\n");
        build.append("        xml.required = true\n");
        build.append("        html.required = true\n");
        build.append("    }\n");
        build.append("}\n\n");

        build.append("checkstyle {\n");
        build.append("    toolVersion = '10.12.5'\n");
        build.append("    configFile = file(\"${rootDir}/checkstyle.xml\")\n");
        build.append("}\n\n");

        // Boot Jar configuration
        build.append("bootJar {\n");
        build.append("    archiveFileName = '").append(config.getLowerBaseName()).append(".jar'\n");
        build.append("    launchScript()\n");
        build.append("}\n");

        writeFile("build.gradle", build.toString());
    }

    private void writeSettingsGradle(JHipsterConfig config) throws Exception {
        StringBuilder settings = new StringBuilder();
        settings.append("rootProject.name = '").append(config.getLowerBaseName()).append("'\n");

        writeFile("settings.gradle", settings.toString());
    }

    private void writeGradleProperties(JHipsterConfig config) throws Exception {
        StringBuilder props = new StringBuilder();
        props.append("# Gradle properties\n");
        props.append("org.gradle.caching=true\n");
        props.append("org.gradle.parallel=true\n");
        props.append("org.gradle.jvmargs=-Xmx2g -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8\n\n");

        props.append("# Project properties\n");
        props.append("springBootVersion=").append(SPRING_BOOT_VERSION).append("\n");

        if (config.hasServiceDiscovery() || Boolean.TRUE.equals(config.getFeignClient())) {
            props.append("springCloudVersion=").append(SPRING_CLOUD_VERSION).append("\n");
        }

        writeFile("gradle.properties", props.toString());
    }

    private void writeVersionCatalog(JHipsterConfig config) throws Exception {
        StringBuilder catalog = new StringBuilder();
        catalog.append("[versions]\n");
        catalog.append("spring-boot = \"").append(SPRING_BOOT_VERSION).append("\"\n");
        catalog.append("spring-cloud = \"").append(SPRING_CLOUD_VERSION).append("\"\n");
        catalog.append("mapstruct = \"1.5.5.Final\"\n");
        catalog.append("jjwt = \"0.12.3\"\n");
        catalog.append("springdoc = \"2.3.0\"\n\n");

        catalog.append("[libraries]\n");
        catalog.append("spring-boot-starter-web = { module = \"org.springframework.boot:spring-boot-starter-web\" }\n");
        catalog.append("spring-boot-starter-validation = { module = \"org.springframework.boot:spring-boot-starter-validation\" }\n");
        catalog.append("spring-boot-starter-security = { module = \"org.springframework.boot:spring-boot-starter-security\" }\n");
        catalog.append("spring-boot-starter-actuator = { module = \"org.springframework.boot:spring-boot-starter-actuator\" }\n");
        catalog.append("spring-boot-starter-data-jpa = { module = \"org.springframework.boot:spring-boot-starter-data-jpa\" }\n");
        catalog.append("spring-boot-starter-test = { module = \"org.springframework.boot:spring-boot-starter-test\" }\n\n");

        catalog.append("[plugins]\n");
        catalog.append("spring-boot = { id = \"org.springframework.boot\", version.ref = \"spring-boot\" }\n");
        catalog.append("spring-dependency-management = { id = \"io.spring.dependency-management\", version = \"1.1.4\" }\n");

        writeFile("gradle/libs.versions.toml", catalog.toString());
    }

    private void writeGradleWrapper() throws Exception {
        StringBuilder props = new StringBuilder();
        props.append("distributionBase=GRADLE_USER_HOME\n");
        props.append("distributionPath=wrapper/dists\n");
        props.append("distributionUrl=https\\://services.gradle.org/distributions/gradle-").append(GRADLE_VERSION).append("-bin.zip\n");
        props.append("networkTimeout=10000\n");
        props.append("validateDistributionUrl=true\n");
        props.append("zipStoreBase=GRADLE_USER_HOME\n");
        props.append("zipStorePath=wrapper/dists\n");

        writeFile("gradle/wrapper/gradle-wrapper.properties", props.toString());
    }

    private void writeGradlewScripts() throws Exception {
        // gradlew (Unix)
        StringBuilder gradlew = new StringBuilder();
        gradlew.append("#!/bin/sh\n\n");
        gradlew.append("# Gradle wrapper script for Unix\n\n");
        gradlew.append("APP_NAME=\"Gradle\"\n");
        gradlew.append("APP_BASE_NAME=$(basename \"$0\")\n\n");

        gradlew.append("# Resolve links and canonicalize paths\n");
        gradlew.append("PRG=\"$0\"\n");
        gradlew.append("while [ -h \"$PRG\" ]; do\n");
        gradlew.append("    ls=$(ls -ld \"$PRG\")\n");
        gradlew.append("    link=$(expr \"$ls\" : '.*-> \\(.*\\)$')\n");
        gradlew.append("    if expr \"$link\" : '/.*' > /dev/null; then\n");
        gradlew.append("        PRG=\"$link\"\n");
        gradlew.append("    else\n");
        gradlew.append("        PRG=$(dirname \"$PRG\")/\"$link\"\n");
        gradlew.append("    fi\n");
        gradlew.append("done\n\n");

        gradlew.append("APP_HOME=$(cd \"$(dirname \"$PRG\")\" && pwd -P)\n\n");

        gradlew.append("# Set Java options\n");
        gradlew.append("DEFAULT_JVM_OPTS='-Dfile.encoding=UTF-8 -Xmx512m'\n\n");

        gradlew.append("# Find Java\n");
        gradlew.append("if [ -n \"$JAVA_HOME\" ]; then\n");
        gradlew.append("    JAVACMD=\"$JAVA_HOME/bin/java\"\n");
        gradlew.append("else\n");
        gradlew.append("    JAVACMD=\"java\"\n");
        gradlew.append("fi\n\n");

        gradlew.append("# Check Java\n");
        gradlew.append("if ! command -v \"$JAVACMD\" > /dev/null 2>&1; then\n");
        gradlew.append("    echo \"ERROR: JAVA_HOME is not set and no 'java' command could be found.\"\n");
        gradlew.append("    exit 1\n");
        gradlew.append("fi\n\n");

        gradlew.append("CLASSPATH=\"$APP_HOME/gradle/wrapper/gradle-wrapper.jar\"\n\n");

        gradlew.append("exec \"$JAVACMD\" $DEFAULT_JVM_OPTS $JAVA_OPTS -classpath \"$CLASSPATH\" org.gradle.wrapper.GradleWrapperMain \"$@\"\n");

        writeFile("gradlew", gradlew.toString());

        // gradlew.bat (Windows)
        StringBuilder gradlewBat = new StringBuilder();
        gradlewBat.append("@rem Gradle wrapper script for Windows\n");
        gradlewBat.append("@if \"%DEBUG%\"==\"\" @echo off\n\n");

        gradlewBat.append("@rem Set local scope for variables\n");
        gradlewBat.append("setlocal\n\n");

        gradlewBat.append("set DIRNAME=%~dp0\n");
        gradlewBat.append("if \"%DIRNAME%\"==\"\" set DIRNAME=.\n\n");

        gradlewBat.append("@rem Find java.exe\n");
        gradlewBat.append("if defined JAVA_HOME goto findJavaFromJavaHome\n\n");

        gradlewBat.append("set JAVA_EXE=java.exe\n");
        gradlewBat.append("%JAVA_EXE% -version >NUL 2>&1\n");
        gradlewBat.append("if %ERRORLEVEL% equ 0 goto execute\n\n");

        gradlewBat.append("echo ERROR: JAVA_HOME is not set and no 'java' command could be found.\n");
        gradlewBat.append("goto fail\n\n");

        gradlewBat.append(":findJavaFromJavaHome\n");
        gradlewBat.append("set JAVA_HOME=%JAVA_HOME:\"=%\n");
        gradlewBat.append("set JAVA_EXE=%JAVA_HOME%/bin/java.exe\n\n");

        gradlewBat.append("if exist \"%JAVA_EXE%\" goto execute\n\n");

        gradlewBat.append("echo ERROR: JAVA_HOME is set but java.exe is not found.\n");
        gradlewBat.append("goto fail\n\n");

        gradlewBat.append(":execute\n");
        gradlewBat.append("@rem Setup the command line\n");
        gradlewBat.append("set CLASSPATH=%DIRNAME%\\gradle\\wrapper\\gradle-wrapper.jar\n\n");

        gradlewBat.append("@rem Execute Gradle\n");
        gradlewBat.append("\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -classpath \"%CLASSPATH%\" org.gradle.wrapper.GradleWrapperMain %*\n\n");

        gradlewBat.append(":end\n");
        gradlewBat.append("@rem End local scope for variables\n");
        gradlewBat.append("endlocal\n\n");

        gradlewBat.append(":fail\n");
        gradlewBat.append("exit /b 1\n");

        writeFile("gradlew.bat", gradlewBat.toString());
    }
}
