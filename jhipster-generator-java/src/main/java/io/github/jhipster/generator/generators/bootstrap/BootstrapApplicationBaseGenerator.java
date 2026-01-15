/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.bootstrap;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.PrimaryKeyConfig;

import java.util.Base64;
import java.security.SecureRandom;

/**
 * Bootstrap Application Base Generator.
 * Initializes the base application configuration and built-in entities.
 * Equivalent to bootstrap-application-base/generator.ts.
 */
public class BootstrapApplicationBaseGenerator extends BaseApplicationGenerator {

    public BootstrapApplicationBaseGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "bootstrap-application-base";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.INITIALIZING, "initializingBootstrap", this::initializing);
        registerTask(GeneratorPriority.LOADING, "loadingBootstrap", this::loading);
        registerTask(GeneratorPriority.PREPARING, "preparingBootstrap", this::preparing);
    }

    private void initializing() {
        log.info("Initializing bootstrap application base");

        JHipsterConfig config = getConfig();

        // Set defaults for microservice
        if (config.isMicroservice()) {
            config.setSkipUserManagement(true);
            config.setSkipClient(true);
        }

        // Generate JWT secret if not present
        if (config.isJwt() && config.getJwtSecretKey() == null) {
            config.setJwtSecretKey(generateSecretKey());
        }
    }

    private void loading() {
        log.info("Loading entities from .jhipster directory");
        context.loadEntities();

        // Create built-in entities if not skipping user management
        if (!Boolean.TRUE.equals(getConfig().getSkipUserManagement())) {
            createBuiltInEntities();
        }
    }

    private void preparing() {
        log.info("Preparing application configuration");

        JHipsterConfig config = getConfig();

        // Set computed properties in context
        context.setConfigValue("mainClass", config.getMainClass());
        context.setConfigValue("packageFolder", config.getPackageFolder());
        context.setConfigValue("mainJavaDir", "src/main/java/" + config.getPackageFolder() + "/");
        context.setConfigValue("testJavaDir", "src/test/java/" + config.getPackageFolder() + "/");
        context.setConfigValue("mainResourceDir", "src/main/resources/");
        context.setConfigValue("testResourceDir", "src/test/resources/");
        context.setConfigValue("dockerDir", "src/main/docker/");

        // Microservice specific
        if (config.isMicroservice()) {
            context.setConfigValue("endpointPrefix", "services/" + config.getLowerBaseName());
        } else {
            context.setConfigValue("endpointPrefix", "");
        }
    }

    private void createBuiltInEntities() {
        // Create User entity
        EntityConfig user = new EntityConfig();
        user.setName("User");
        user.setEntityTableName("jhi_user");
        user.setDto("no");
        user.setService("no");
        user.setPagination("no");
        user.setReadOnly(true);
        user.setSkipClient(true);

        // User fields
        FieldConfig userId = new FieldConfig();
        userId.setFieldName("id");
        userId.setFieldType("Long");
        userId.setId(true);
        user.getFields().add(userId);

        FieldConfig login = new FieldConfig();
        login.setFieldName("login");
        login.setFieldType("String");
        login.setFieldValidateRules(new String[]{"required", "unique"});
        user.getFields().add(login);

        FieldConfig firstName = new FieldConfig();
        firstName.setFieldName("firstName");
        firstName.setFieldType("String");
        user.getFields().add(firstName);

        FieldConfig lastName = new FieldConfig();
        lastName.setFieldName("lastName");
        lastName.setFieldType("String");
        user.getFields().add(lastName);

        FieldConfig email = new FieldConfig();
        email.setFieldName("email");
        email.setFieldType("String");
        email.setFieldValidateRules(new String[]{"required", "unique"});
        user.getFields().add(email);

        user.setPrimaryKey(PrimaryKeyConfig.defaultLong());
        user.setEntityAbsolutePackage(getConfig().getPackageName());

        addEntity(user);

        // Create Authority entity
        EntityConfig authority = new EntityConfig();
        authority.setName("Authority");
        authority.setEntityTableName("jhi_authority");
        authority.setDto("no");
        authority.setService("no");
        authority.setPagination("no");
        authority.setReadOnly(true);
        authority.setSkipClient(true);

        FieldConfig authorityName = new FieldConfig();
        authorityName.setFieldName("name");
        authorityName.setFieldType("String");
        authorityName.setId(true);
        authority.getFields().add(authorityName);

        authority.setPrimaryKey(new PrimaryKeyConfig("name", "String"));
        authority.setEntityAbsolutePackage(getConfig().getPackageName());

        addEntity(authority);

        log.info("Created built-in entities: User, Authority");
    }

    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
