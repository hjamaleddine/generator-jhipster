/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.core;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;

import java.util.List;

/**
 * Base generator for application-level generators that handle entities.
 * This is the Java equivalent of base-application/generator.ts.
 */
public abstract class BaseApplicationGenerator extends BaseGenerator {

    protected BaseApplicationGenerator(GeneratorContext context) {
        super(context);
    }

    /**
     * Called during CONFIGURING_EACH_ENTITY priority for each entity.
     */
    protected void configuringEachEntity(EntityConfig entity) {
        // Override in subclasses
    }

    /**
     * Called during PREPARING_EACH_ENTITY priority for each entity.
     */
    protected void preparingEachEntity(EntityConfig entity) {
        // Override in subclasses
    }

    /**
     * Called during PREPARING_EACH_ENTITY_FIELD priority for each field.
     */
    protected void preparingEachEntityField(EntityConfig entity, FieldConfig field) {
        // Override in subclasses
    }

    /**
     * Called during PREPARING_EACH_ENTITY_RELATIONSHIP priority for each relationship.
     */
    protected void preparingEachEntityRelationship(EntityConfig entity, RelationshipConfig relationship) {
        // Override in subclasses
    }

    /**
     * Called during POST_PREPARING_EACH_ENTITY priority for each entity.
     */
    protected void postPreparingEachEntity(EntityConfig entity) {
        // Override in subclasses
    }

    /**
     * Called during WRITING_ENTITIES priority for each entity.
     */
    protected void writingEntity(EntityConfig entity) throws Exception {
        // Override in subclasses
    }

    /**
     * Called during POST_WRITING_ENTITIES priority for each entity.
     */
    protected void postWritingEntity(EntityConfig entity) throws Exception {
        // Override in subclasses
    }

    @Override
    protected void registerTasks() {
        // Register entity processing tasks
        registerTask(GeneratorPriority.CONFIGURING_EACH_ENTITY, "configuringEachEntity", () -> {
            for (EntityConfig entity : getEntities()) {
                configuringEachEntity(entity);
            }
        });

        registerTask(GeneratorPriority.PREPARING_EACH_ENTITY, "preparingEachEntity", () -> {
            for (EntityConfig entity : getEntities()) {
                preparingEachEntity(entity);
            }
        });

        registerTask(GeneratorPriority.PREPARING_EACH_ENTITY_FIELD, "preparingEachEntityField", () -> {
            for (EntityConfig entity : getEntities()) {
                for (FieldConfig field : entity.getFields()) {
                    preparingEachEntityField(entity, field);
                }
            }
        });

        registerTask(GeneratorPriority.PREPARING_EACH_ENTITY_RELATIONSHIP, "preparingEachEntityRelationship", () -> {
            for (EntityConfig entity : getEntities()) {
                for (RelationshipConfig relationship : entity.getRelationships()) {
                    preparingEachEntityRelationship(entity, relationship);
                }
            }
        });

        registerTask(GeneratorPriority.POST_PREPARING_EACH_ENTITY, "postPreparingEachEntity", () -> {
            for (EntityConfig entity : getEntities()) {
                postPreparingEachEntity(entity);
            }
        });

        registerTask(GeneratorPriority.WRITING_ENTITIES, "writingEntities", () -> {
            for (EntityConfig entity : getEntities()) {
                writingEntity(entity);
            }
        });

        registerTask(GeneratorPriority.POST_WRITING_ENTITIES, "postWritingEntities", () -> {
            for (EntityConfig entity : getEntities()) {
                postWritingEntity(entity);
            }
        });
    }

    /**
     * Gets the list of entities to process.
     */
    protected List<EntityConfig> getEntities() {
        return context.getEntities();
    }

    /**
     * Adds an entity to the context.
     */
    protected void addEntity(EntityConfig entity) {
        context.addEntity(entity);
    }

    /**
     * Gets an entity by name.
     */
    protected EntityConfig getEntity(String name) {
        return context.getEntity(name);
    }

    /**
     * Checks if the application has any entities.
     */
    protected boolean hasEntities() {
        return !getEntities().isEmpty();
    }

    // ==================== Application Type Helpers ====================

    protected boolean isMonolith() {
        return "monolith".equals(getConfig().getApplicationType());
    }

    protected boolean isMicroservice() {
        return "microservice".equals(getConfig().getApplicationType());
    }

    protected boolean isGateway() {
        return "gateway".equals(getConfig().getApplicationType());
    }

    // ==================== Database Helpers ====================

    protected boolean isSql() {
        return "sql".equals(getConfig().getDatabaseType());
    }

    protected boolean isMongodb() {
        return "mongodb".equals(getConfig().getDatabaseType());
    }

    protected boolean isCassandra() {
        return "cassandra".equals(getConfig().getDatabaseType());
    }

    protected boolean isNeo4j() {
        return "neo4j".equals(getConfig().getDatabaseType());
    }

    // ==================== Authentication Helpers ====================

    protected boolean isJwt() {
        return "jwt".equals(getConfig().getAuthenticationType());
    }

    protected boolean isOauth2() {
        return "oauth2".equals(getConfig().getAuthenticationType());
    }

    protected boolean isSession() {
        return "session".equals(getConfig().getAuthenticationType());
    }

    // ==================== Service Discovery Helpers ====================

    protected boolean hasServiceDiscovery() {
        String serviceDiscoveryType = getConfig().getServiceDiscoveryType();
        return serviceDiscoveryType != null && !"no".equals(serviceDiscoveryType);
    }

    protected boolean isEureka() {
        return "eureka".equals(getConfig().getServiceDiscoveryType());
    }

    protected boolean isConsul() {
        return "consul".equals(getConfig().getServiceDiscoveryType());
    }

    // ==================== Feature Helpers ====================

    protected boolean isReactive() {
        return Boolean.TRUE.equals(getConfig().getReactive());
    }

    protected boolean hasFeignClient() {
        return Boolean.TRUE.equals(getConfig().getFeignClient());
    }

    protected boolean hasMessageBroker() {
        String messageBroker = getConfig().getMessageBroker();
        return messageBroker != null && !"no".equals(messageBroker);
    }

    protected boolean hasSearchEngine() {
        String searchEngine = getConfig().getSearchEngine();
        return searchEngine != null && !"no".equals(searchEngine);
    }

    protected boolean hasCacheProvider() {
        String cacheProvider = getConfig().getCacheProvider();
        return cacheProvider != null && !"no".equals(cacheProvider);
    }
}
