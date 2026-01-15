/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.app;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.generators.bootstrap.BootstrapApplicationBaseGenerator;
import io.github.jhipster.generator.generators.server.ServerGenerator;

/**
 * Main Application Generator.
 * Entry point for JHipster application generation.
 * Equivalent to app/generator.ts.
 */
public class AppGenerator extends BaseApplicationGenerator {

    public AppGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "app";
    }

    @Override
    protected void beforeQueue() {
        // Bootstrap dependencies
        dependsOn(new BootstrapApplicationBaseGenerator(context));
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.INITIALIZING, "initializingApp", this::initializing);
        registerTask(GeneratorPriority.CONFIGURING, "configuringApp", this::configuring);
        registerTask(GeneratorPriority.COMPOSING_COMPONENT, "composingApp", this::composing);
        registerTask(GeneratorPriority.WRITING, "writingApp", this::writing);
        registerTask(GeneratorPriority.END, "endApp", this::end);
    }

    private void initializing() {
        log.info("Initializing JHipster Application Generator");
        log.info("Application type: {}", getConfig().getApplicationType());
        log.info("Base name: {}", getConfig().getBaseName());
        log.info("Package name: {}", getConfig().getPackageName());
    }

    private void configuring() {
        log.info("Configuring application");

        // Microservice specific configuration
        if (isMicroservice()) {
            getConfig().setSkipUserManagement(true);
            log.info("Microservice mode: skipUserManagement enabled");
        }
    }

    private void composing() {
        log.info("Composing with sub-generators");

        // Always compose with server generator for microservices
        if (!Boolean.TRUE.equals(getConfig().getSkipClient()) || isMicroservice()) {
            composeWith(new ServerGenerator(context));
        }

        // Note: Client generator would be composed here if not skipped
        // For microservices, client is typically skipped
    }

    private void writing() throws Exception {
        log.info("Writing application configuration files");

        // Write .yo-rc.json
        context.saveJHipsterConfig();

        // Write README
        writeReadme();

        // Write .gitignore
        writeGitignore();

        // Write .editorconfig
        writeEditorConfig();
    }

    private void end() {
        log.info("Application generation completed successfully!");
        log.info("");
        log.info("==========================================================");
        log.info("Application '{}' generated successfully!", getConfig().getBaseName());
        log.info("==========================================================");
        log.info("");
        log.info("To start the application:");
        log.info("  ./mvnw spring-boot:run");
        log.info("");
    }

    private void writeReadme() throws Exception {
        StringBuilder readme = new StringBuilder();
        readme.append("# ").append(getConfig().getBaseName()).append("\n\n");
        readme.append("This application was generated using JHipster Generator Java.\n\n");
        readme.append("## Development\n\n");
        readme.append("To start your application in the dev profile, run:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw spring-boot:run\n");
        readme.append("```\n\n");

        if (isMicroservice()) {
            readme.append("## Microservice\n\n");
            readme.append("This is a microservice application. It requires a service discovery server ");
            readme.append("(").append(getConfig().getServiceDiscoveryType()).append(") to be running.\n\n");
        }

        readme.append("## Building for production\n\n");
        readme.append("To build the final jar for production, run:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw -Pprod clean verify\n");
        readme.append("```\n\n");
        readme.append("## Testing\n\n");
        readme.append("To run tests, run:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw verify\n");
        readme.append("```\n");

        writeFile("README.md", readme.toString());
    }

    private void writeGitignore() throws Exception {
        StringBuilder gitignore = new StringBuilder();
        gitignore.append("######################\n");
        gitignore.append("# Project Specific\n");
        gitignore.append("######################\n");
        gitignore.append("/target/\n");
        gitignore.append("/build/\n");
        gitignore.append("\n");
        gitignore.append("######################\n");
        gitignore.append("# Node\n");
        gitignore.append("######################\n");
        gitignore.append("node_modules/\n");
        gitignore.append("\n");
        gitignore.append("######################\n");
        gitignore.append("# IDE\n");
        gitignore.append("######################\n");
        gitignore.append(".idea/\n");
        gitignore.append("*.iml\n");
        gitignore.append(".vscode/\n");
        gitignore.append("*.swp\n");
        gitignore.append("*.swo\n");
        gitignore.append("\n");
        gitignore.append("######################\n");
        gitignore.append("# OS\n");
        gitignore.append("######################\n");
        gitignore.append(".DS_Store\n");
        gitignore.append("Thumbs.db\n");
        gitignore.append("\n");
        gitignore.append("######################\n");
        gitignore.append("# Gradle\n");
        gitignore.append("######################\n");
        gitignore.append(".gradle/\n");
        gitignore.append("gradle/wrapper/gradle-wrapper.jar\n");
        gitignore.append("\n");
        gitignore.append("######################\n");
        gitignore.append("# Maven\n");
        gitignore.append("######################\n");
        gitignore.append(".mvn/wrapper/maven-wrapper.jar\n");
        gitignore.append("\n");
        gitignore.append("######################\n");
        gitignore.append("# Logs\n");
        gitignore.append("######################\n");
        gitignore.append("*.log\n");

        writeFile(".gitignore", gitignore.toString());
    }

    private void writeEditorConfig() throws Exception {
        StringBuilder editorConfig = new StringBuilder();
        editorConfig.append("# EditorConfig helps maintain consistent coding styles\n");
        editorConfig.append("# https://editorconfig.org\n\n");
        editorConfig.append("root = true\n\n");
        editorConfig.append("[*]\n");
        editorConfig.append("indent_style = space\n");
        editorConfig.append("indent_size = 4\n");
        editorConfig.append("end_of_line = lf\n");
        editorConfig.append("charset = utf-8\n");
        editorConfig.append("trim_trailing_whitespace = true\n");
        editorConfig.append("insert_final_newline = true\n\n");
        editorConfig.append("[*.md]\n");
        editorConfig.append("trim_trailing_whitespace = false\n\n");
        editorConfig.append("[*.{yml,yaml}]\n");
        editorConfig.append("indent_size = 2\n");

        writeFile(".editorconfig", editorConfig.toString());
    }
}
