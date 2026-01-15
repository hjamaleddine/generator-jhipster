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
import io.github.jhipster.generator.generators.common.CommonGenerator;
import io.github.jhipster.generator.generators.maven.MavenGenerator;

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

        // Common generator (git, prettier, editorconfig, etc.)
        composeWith(new CommonGenerator(context));

        // Maven generator (wrapper, pom.xml structure)
        composeWith(new MavenGenerator(context));

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

        // Note: README, .gitignore, .editorconfig are now handled by CommonGenerator and GitGenerator
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

}
