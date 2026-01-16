/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.entity;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.generators.domain.DomainGenerator;
import io.github.jhipster.generator.generators.springdata.SpringDataRelationalGenerator;
import io.github.jhipster.generator.generators.liquibase.LiquibaseGenerator;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity Generator.
 * Generates a single entity with all its components:
 * - Domain class
 * - Repository
 * - Service
 * - DTO and Mapper
 * - REST controller
 * - Liquibase changelog
 */
public class EntityGenerator extends BaseApplicationGenerator {

    private final EntityConfig entity;

    /**
     * Constructor for generating a single entity.
     */
    public EntityGenerator(GeneratorContext context, EntityConfig entity) {
        super(context);
        this.entity = entity;
    }

    /**
     * Constructor for generating all entities from context.
     * Uses the first entity or null if no entities exist.
     */
    public EntityGenerator(GeneratorContext context) {
        super(context);
        List<EntityConfig> entities = context.getEntities();
        this.entity = (entities != null && !entities.isEmpty()) ? entities.get(0) : null;
    }

    @Override
    public String getName() {
        return "entity";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringEntity", this::configuring);
        registerTask(GeneratorPriority.COMPOSING, "composingEntity", this::composing);
        registerTask(GeneratorPriority.WRITING, "writingEntity", this::writing);
    }

    private void configuring() {
        if (entity != null) {
            log.info("Configuring entity: {}", entity.getName());

            // Add entity to context if not already present
            List<EntityConfig> entities = context.getEntities();
            if (entities == null) {
                entities = new ArrayList<>();
            }

            boolean exists = entities.stream()
                .anyMatch(e -> e.getName().equals(entity.getName()));

            if (!exists) {
                entities.add(entity);
                context.setEntities(entities);
            }
        }
    }

    private void composing() {
        if (entity != null) {
            log.info("Composing entity sub-generators for: {}", entity.getName());
        }
        // The entity-specific generation is handled by the sub-generators
        // which are already composed by SpringBootGenerator
    }

    private void writing() throws Exception {
        // Write all entities from context
        for (EntityConfig e : context.getEntities()) {
            log.info("Writing entity: {}", e.getName());
            saveEntityConfig(e);
        }
    }

    private void saveEntityConfig(EntityConfig entityToSave) throws Exception {
        // Serialize entity configuration to JSON
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": \"").append(entityToSave.getName()).append("\",\n");
        json.append("  \"entityTableName\": \"").append(entityToSave.getEntityTableName()).append("\",\n");

        // Fields
        json.append("  \"fields\": [\n");
        List<FieldConfig> fields = entityToSave.getFields();
        for (int i = 0; i < fields.size(); i++) {
            FieldConfig field = fields.get(i);
            json.append("    {\n");
            json.append("      \"fieldName\": \"").append(field.getFieldName()).append("\",\n");
            json.append("      \"fieldType\": \"").append(field.getFieldType()).append("\"");

            if (field.isRequired()) {
                json.append(",\n      \"fieldValidateRules\": [\"required\"]");
            }
            if (field.getMinLength() != null) {
                json.append(",\n      \"fieldValidateRulesMinlength\": ").append(field.getMinLength());
            }
            if (field.getMaxLength() != null) {
                json.append(",\n      \"fieldValidateRulesMaxlength\": ").append(field.getMaxLength());
            }
            if (field.isUnique()) {
                json.append(",\n      \"fieldValidateRulesUnique\": true");
            }

            json.append("\n    }");
            if (i < fields.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");

        // Relationships
        json.append("  \"relationships\": [\n");
        List<RelationshipConfig> relationships = entityToSave.getRelationships();
        for (int i = 0; i < relationships.size(); i++) {
            RelationshipConfig rel = relationships.get(i);
            json.append("    {\n");
            json.append("      \"relationshipName\": \"").append(rel.getRelationshipName()).append("\",\n");
            json.append("      \"otherEntityName\": \"").append(rel.getOtherEntityName()).append("\",\n");
            json.append("      \"relationshipType\": \"").append(rel.getRelationshipType()).append("\",\n");
            json.append("      \"ownerSide\": ").append(rel.isOwnerSide());

            if (rel.getOtherEntityRelationshipName() != null) {
                json.append(",\n      \"otherEntityRelationshipName\": \"").append(rel.getOtherEntityRelationshipName()).append("\"");
            }

            json.append("\n    }");
            if (i < relationships.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ],\n");

        // Other properties
        json.append("  \"dto\": \"").append(entityToSave.isDto() ? "mapstruct" : "no").append("\",\n");
        json.append("  \"service\": \"").append(entityToSave.isServiceClass() ? "serviceClass" : "no").append("\",\n");
        json.append("  \"pagination\": \"").append(entityToSave.getPagination() != null ? entityToSave.getPagination() : "no").append("\",\n");
        json.append("  \"jpaMetamodelFiltering\": ").append(entityToSave.isFiltering()).append(",\n");
        json.append("  \"readOnly\": ").append(entityToSave.isReadOnly()).append("\n");
        json.append("}\n");

        String jhipsterDir = context.getBasePath().toString() + "/.jhipster/";
        writeFile(jhipsterDir + entityToSave.getName() + ".json", json.toString());
    }

    /**
     * Creates an EntityConfig from JSON configuration.
     */
    public static EntityConfig fromJson(String json, String entityName) {
        EntityConfig entity = new EntityConfig();
        entity.setName(entityName);

        // Simple JSON parsing (in production, use Jackson or Gson)
        // This is a simplified implementation
        if (json.contains("\"entityTableName\"")) {
            String tableName = extractJsonValue(json, "entityTableName");
            entity.setEntityTableName(tableName);
        }

        // Parse fields
        List<FieldConfig> fields = new ArrayList<>();
        // ... field parsing logic
        entity.setFields(fields);

        // Parse relationships
        List<RelationshipConfig> relationships = new ArrayList<>();
        // ... relationship parsing logic
        entity.setRelationships(relationships);

        return entity;
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();
        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }

        if (json.charAt(startIndex) == '"') {
            startIndex++;
            int endIndex = json.indexOf('"', startIndex);
            return json.substring(startIndex, endIndex);
        }

        // Non-string value
        int endIndex = startIndex;
        while (endIndex < json.length() &&
               json.charAt(endIndex) != ',' &&
               json.charAt(endIndex) != '}' &&
               json.charAt(endIndex) != '\n') {
            endIndex++;
        }
        return json.substring(startIndex, endIndex).trim();
    }

    public EntityConfig getEntity() {
        return entity;
    }
}
