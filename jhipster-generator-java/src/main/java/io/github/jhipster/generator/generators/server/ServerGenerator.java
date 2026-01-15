/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.generators.springboot.SpringBootGenerator;
import io.github.jhipster.generator.model.EntityConfig;

/**
 * Server Generator.
 * Handles server-side configuration and composes with Spring Boot generator.
 * Equivalent to server/generator.ts.
 */
public class ServerGenerator extends BaseApplicationGenerator {

    public ServerGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "server";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringServer", this::configuring);
        registerTask(GeneratorPriority.COMPOSING, "composingServer", this::composing);
        registerTask(GeneratorPriority.PREPARING, "preparingServer", this::preparing);
    }

    @Override
    protected void configuringEachEntity(EntityConfig entity) {
        log.debug("Configuring entity for server: {}", entity.getName());

        // Set microservice name for entity if not set
        if (isMicroservice()) {
            if (entity.getMicroserviceName() == null) {
                entity.setMicroserviceName(getConfig().getBaseName());
            }
            if (entity.getClientRootFolder() == null) {
                entity.setClientRootFolder(entity.getMicroserviceName());
            }
        }

        // Set entity package
        if (entity.getEntityAbsolutePackage() == null) {
            String entityPackage = entity.getEntityPackage();
            if (entityPackage != null && !entityPackage.isEmpty()) {
                entity.setEntityAbsolutePackage(getConfig().getPackageName() + "." + entityPackage);
            } else {
                entity.setEntityAbsolutePackage(getConfig().getPackageName());
            }
        }

        // Set entity absolute class
        if (entity.getEntityAbsoluteClass() == null) {
            entity.setEntityAbsoluteClass(entity.getEntityAbsolutePackage() + ".domain." + entity.getPersistClass());
        }
    }

    private void configuring() {
        log.info("Configuring server generator");

        // Server-specific configuration
        if (getConfig().getServerPort() == null) {
            if (isMicroservice()) {
                getConfig().setServerPort(8081);
            } else {
                getConfig().setServerPort(8080);
            }
        }
    }

    private void composing() {
        log.info("Composing with Spring Boot generator");
        composeWith(new SpringBootGenerator(context));
    }

    private void preparing() {
        log.info("Preparing server configuration");

        // Set server-specific context values
        context.setConfigValue("serverPort", getConfig().getServerPort());
        context.setConfigValue("authenticationType", getConfig().getAuthenticationType());
        context.setConfigValue("databaseType", getConfig().getDatabaseType());
        context.setConfigValue("devDatabaseType", getConfig().getDevDatabaseType());
        context.setConfigValue("prodDatabaseType", getConfig().getProdDatabaseType());
        context.setConfigValue("buildTool", getConfig().getBuildTool());
        context.setConfigValue("cacheProvider", getConfig().getCacheProvider());
    }
}
