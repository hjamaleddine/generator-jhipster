/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.jib;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * Jib Generator.
 * Generates Jib configuration for building container images without Docker.
 */
public class JibGenerator extends BaseApplicationGenerator {

    private static final String JIB_VERSION = "3.4.0";

    public JibGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "jib";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingJib", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Jib configuration");

        writeJibConfiguration();
        writeEntrypoint();
    }

    private void writeJibConfiguration() throws Exception {
        // Jib configuration is added to pom.xml
        // This method generates supplementary configuration files

        JHipsterConfig config = getConfig();

        StringBuilder jibConfig = new StringBuilder();
        jibConfig.append("# Jib Configuration\n");
        jibConfig.append("# Build container image without Docker daemon:\n");
        jibConfig.append("#   ./mvnw package -Pprod jib:build\n");
        jibConfig.append("#\n");
        jibConfig.append("# Build to local Docker daemon:\n");
        jibConfig.append("#   ./mvnw package -Pprod jib:dockerBuild\n");
        jibConfig.append("#\n");
        jibConfig.append("# Build to tar file:\n");
        jibConfig.append("#   ./mvnw package -Pprod jib:buildTar\n\n");

        jibConfig.append("# Image settings:\n");
        jibConfig.append("jib:\n");
        jibConfig.append("  from:\n");
        jibConfig.append("    image: eclipse-temurin:17-jre-focal\n");
        jibConfig.append("  to:\n");
        jibConfig.append("    image: ").append(config.getLowerBaseName()).append("\n");
        jibConfig.append("    tags:\n");
        jibConfig.append("      - latest\n");
        jibConfig.append("  container:\n");
        jibConfig.append("    ports:\n");
        jibConfig.append("      - ").append(config.getServerPort()).append("\n");
        jibConfig.append("    environment:\n");
        jibConfig.append("      SPRING_OUTPUT_ANSI_ENABLED: ALWAYS\n");
        jibConfig.append("      JHIPSTER_SLEEP: 0\n");
        jibConfig.append("    creationTime: USE_CURRENT_TIMESTAMP\n");
        jibConfig.append("    user: 1000\n");
        jibConfig.append("  extraDirectories:\n");
        jibConfig.append("    paths: src/main/docker/jib\n");
        jibConfig.append("    permissions:\n");
        jibConfig.append("      /entrypoint.sh: 755\n");

        writeFile(context.getBasePath().toString() + "/jib.yml", jibConfig.toString());
    }

    private void writeEntrypoint() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder entrypoint = new StringBuilder();
        entrypoint.append("#!/bin/sh\n\n");

        entrypoint.append("echo \"The application will start in ${JHIPSTER_SLEEP}s...\" && sleep ${JHIPSTER_SLEEP}\n\n");

        entrypoint.append("exec java ${JAVA_OPTS} \\\n");
        entrypoint.append("  -Djava.security.egd=file:/dev/./urandom \\\n");
        entrypoint.append("  -cp /app/resources/:/app/classes/:/app/libs/* \\\n");
        entrypoint.append("  \"").append(config.getPackageName()).append(".").append(config.getMainClass()).append("\" \"$@\"\n");

        // Create the directory structure
        String jibDir = context.getBasePath().toString() + "/src/main/docker/jib/";
        writeFile(jibDir + "entrypoint.sh", entrypoint.toString());
    }

    /**
     * Returns the Jib Maven plugin configuration as XML snippet.
     * This can be added to pom.xml by the Maven generator.
     */
    public String getJibMavenPlugin() {
        JHipsterConfig config = getConfig();

        StringBuilder plugin = new StringBuilder();
        plugin.append("            <plugin>\n");
        plugin.append("                <groupId>com.google.cloud.tools</groupId>\n");
        plugin.append("                <artifactId>jib-maven-plugin</artifactId>\n");
        plugin.append("                <version>").append(JIB_VERSION).append("</version>\n");
        plugin.append("                <configuration>\n");
        plugin.append("                    <from>\n");
        plugin.append("                        <image>eclipse-temurin:17-jre-focal</image>\n");
        plugin.append("                    </from>\n");
        plugin.append("                    <to>\n");
        plugin.append("                        <image>").append(config.getLowerBaseName()).append(":latest</image>\n");
        plugin.append("                    </to>\n");
        plugin.append("                    <container>\n");
        plugin.append("                        <entrypoint>\n");
        plugin.append("                            <shell>bash</shell>\n");
        plugin.append("                            <option>-c</option>\n");
        plugin.append("                            <arg>/entrypoint.sh</arg>\n");
        plugin.append("                        </entrypoint>\n");
        plugin.append("                        <ports>\n");
        plugin.append("                            <port>").append(config.getServerPort()).append("</port>\n");
        plugin.append("                        </ports>\n");
        plugin.append("                        <environment>\n");
        plugin.append("                            <SPRING_OUTPUT_ANSI_ENABLED>ALWAYS</SPRING_OUTPUT_ANSI_ENABLED>\n");
        plugin.append("                            <JHIPSTER_SLEEP>0</JHIPSTER_SLEEP>\n");
        plugin.append("                        </environment>\n");
        plugin.append("                        <creationTime>USE_CURRENT_TIMESTAMP</creationTime>\n");
        plugin.append("                        <user>1000</user>\n");
        plugin.append("                    </container>\n");
        plugin.append("                    <extraDirectories>\n");
        plugin.append("                        <paths>src/main/docker/jib</paths>\n");
        plugin.append("                        <permissions>\n");
        plugin.append("                            <permission>\n");
        plugin.append("                                <file>/entrypoint.sh</file>\n");
        plugin.append("                                <mode>755</mode>\n");
        plugin.append("                            </permission>\n");
        plugin.append("                        </permissions>\n");
        plugin.append("                    </extraDirectories>\n");
        plugin.append("                </configuration>\n");
        plugin.append("            </plugin>\n");

        return plugin.toString();
    }
}
