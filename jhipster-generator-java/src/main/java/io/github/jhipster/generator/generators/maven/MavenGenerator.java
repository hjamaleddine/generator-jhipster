/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.maven;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

/**
 * Maven Generator.
 * Generates Maven build configuration (pom.xml, wrapper).
 */
public class MavenGenerator extends BaseApplicationGenerator {

    private static final String MAVEN_WRAPPER_VERSION = "3.2.0";
    private static final String MAVEN_VERSION = "3.9.6";

    public MavenGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "maven";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingMaven", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Maven configuration files");

        writeMavenWrapper();
        writeMavenWrapperProperties();
        writeMvnwScript();
        writeMvnwCmdScript();
    }

    private void writeMavenWrapper() throws Exception {
        // Create .mvn directory
        Path mvnDir = Path.of(context.getBasePath().toString(), ".mvn", "wrapper");
        Files.createDirectories(mvnDir);

        // Write maven-wrapper.properties
        StringBuilder props = new StringBuilder();
        props.append("# Licensed to the Apache Software Foundation (ASF)\n");
        props.append("distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/")
             .append(MAVEN_VERSION).append("/apache-maven-").append(MAVEN_VERSION).append("-bin.zip\n");
        props.append("wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/")
             .append(MAVEN_WRAPPER_VERSION).append("/maven-wrapper-").append(MAVEN_WRAPPER_VERSION).append(".jar\n");

        writeFile(mvnDir.resolve("maven-wrapper.properties").toString(), props.toString());
    }

    private void writeMavenWrapperProperties() throws Exception {
        // .mvn/jvm.config
        StringBuilder jvmConfig = new StringBuilder();
        jvmConfig.append("-Xmx512m\n");

        writeFile(context.getBasePath().toString() + "/.mvn/jvm.config", jvmConfig.toString());
    }

    private void writeMvnwScript() throws Exception {
        StringBuilder mvnw = new StringBuilder();
        mvnw.append("#!/bin/sh\n");
        mvnw.append("# ----------------------------------------------------------------------------\n");
        mvnw.append("# Licensed to the Apache Software Foundation (ASF) under one\n");
        mvnw.append("# or more contributor license agreements.\n");
        mvnw.append("# ----------------------------------------------------------------------------\n\n");

        mvnw.append("# OS specific support.\n");
        mvnw.append("cygwin=false;\n");
        mvnw.append("darwin=false;\n");
        mvnw.append("mingw=false\n");
        mvnw.append("case \"`uname`\" in\n");
        mvnw.append("  CYGWIN*) cygwin=true ;;\n");
        mvnw.append("  MINGW*) mingw=true;;\n");
        mvnw.append("  Darwin*) darwin=true\n");
        mvnw.append("    if [ -z \"$JAVA_VERSION\" ] ; then\n");
        mvnw.append("      JAVA_VERSION=\"CurrentJDK\"\n");
        mvnw.append("    else\n");
        mvnw.append("      echo \"Using Java version: $JAVA_VERSION\"\n");
        mvnw.append("    fi\n");
        mvnw.append("    if [ -z \"$JAVA_HOME\" ]; then\n");
        mvnw.append("      if [ -x \"/usr/libexec/java_home\" ]; then\n");
        mvnw.append("        JAVA_HOME=`/usr/libexec/java_home`\n");
        mvnw.append("      else\n");
        mvnw.append("        JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home\n");
        mvnw.append("      fi\n");
        mvnw.append("    fi\n");
        mvnw.append("    ;;\n");
        mvnw.append("esac\n\n");

        mvnw.append("BASEDIR=`dirname \"$0\"`\n");
        mvnw.append("BASEDIR=`cd \"$BASEDIR\"; pwd`\n\n");

        mvnw.append("if [ -z \"$JAVA_HOME\" ] ; then\n");
        mvnw.append("  echo \"Warning: JAVA_HOME environment variable is not set.\"\n");
        mvnw.append("fi\n\n");

        mvnw.append("WRAPPER_JAR=\"$BASEDIR/.mvn/wrapper/maven-wrapper.jar\"\n");
        mvnw.append("WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain\n\n");

        mvnw.append("# Download maven-wrapper.jar if it doesn't exist\n");
        mvnw.append("if [ ! -f \"$WRAPPER_JAR\" ]; then\n");
        mvnw.append("  if command -v curl > /dev/null; then\n");
        mvnw.append("    curl -sL https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/")
            .append(MAVEN_WRAPPER_VERSION).append("/maven-wrapper-").append(MAVEN_WRAPPER_VERSION)
            .append(".jar -o \"$WRAPPER_JAR\"\n");
        mvnw.append("  elif command -v wget > /dev/null; then\n");
        mvnw.append("    wget -q https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/")
            .append(MAVEN_WRAPPER_VERSION).append("/maven-wrapper-").append(MAVEN_WRAPPER_VERSION)
            .append(".jar -O \"$WRAPPER_JAR\"\n");
        mvnw.append("  fi\n");
        mvnw.append("fi\n\n");

        mvnw.append("MAVEN_PROJECTBASEDIR=\"${MAVEN_BASEDIR:-$BASEDIR}\"\n");
        mvnw.append("export MAVEN_PROJECTBASEDIR\n");
        mvnw.append("MAVEN_OPTS=\"$(concat_lines \"$MAVEN_PROJECTBASEDIR/.mvn/jvm.config\") $MAVEN_OPTS\"\n\n");

        mvnw.append("exec \"$JAVA_HOME/bin/java\" \\\n");
        mvnw.append("  $MAVEN_OPTS \\\n");
        mvnw.append("  $MAVEN_DEBUG_OPTS \\\n");
        mvnw.append("  -classpath \"$WRAPPER_JAR\" \\\n");
        mvnw.append("  \"-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR\" \\\n");
        mvnw.append("  $WRAPPER_LAUNCHER \"$@\"\n");

        writeFile(context.getBasePath().toString() + "/mvnw", mvnw.toString());
    }

    private void writeMvnwCmdScript() throws Exception {
        StringBuilder mvnwCmd = new StringBuilder();
        mvnwCmd.append("@REM ----------------------------------------------------------------------------\n");
        mvnwCmd.append("@REM Licensed to the Apache Software Foundation (ASF)\n");
        mvnwCmd.append("@REM ----------------------------------------------------------------------------\n\n");

        mvnwCmd.append("@echo off\n");
        mvnwCmd.append("setlocal\n\n");

        mvnwCmd.append("set WRAPPER_JAR=\"%~dp0\\.mvn\\wrapper\\maven-wrapper.jar\"\n");
        mvnwCmd.append("set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain\n\n");

        mvnwCmd.append("@REM Download maven-wrapper.jar if it doesn't exist\n");
        mvnwCmd.append("if not exist %WRAPPER_JAR% (\n");
        mvnwCmd.append("    powershell -Command \"(New-Object System.Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/")
            .append(MAVEN_WRAPPER_VERSION).append("/maven-wrapper-").append(MAVEN_WRAPPER_VERSION)
            .append(".jar', '%WRAPPER_JAR%')\"\n");
        mvnwCmd.append(")\n\n");

        mvnwCmd.append("set MAVEN_PROJECTBASEDIR=%~dp0\n");
        mvnwCmd.append("set MAVEN_OPTS=-Xmx512m %MAVEN_OPTS%\n\n");

        mvnwCmd.append("\"%JAVA_HOME%\\bin\\java\" ^\n");
        mvnwCmd.append("  %MAVEN_OPTS% ^\n");
        mvnwCmd.append("  %MAVEN_DEBUG_OPTS% ^\n");
        mvnwCmd.append("  -classpath %WRAPPER_JAR% ^\n");
        mvnwCmd.append("  \"-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%\" ^\n");
        mvnwCmd.append("  %WRAPPER_LAUNCHER% %*\n\n");

        mvnwCmd.append("endlocal\n");

        writeFile(context.getBasePath().toString() + "/mvnw.cmd", mvnwCmd.toString());
    }
}
