/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jhipster.generator.domain;

import io.github.jhipster.generator.domain.model.EntityContext;
import io.github.jhipster.generator.domain.model.FieldContext;
import io.github.jhipster.generator.domain.model.RelationshipContext;
import io.github.jhipster.generator.domain.support.EnumInfo;
import io.github.jhipster.generator.domain.support.JavaBeanUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Domain layer generator for JHipster entities.
 *
 * This is a Java port of the TypeScript domain/generator.ts, implementing:
 * - Entity preparation with reserved keyword validation
 * - Field and relationship preparation
 * - Cyclic relationship detection
 * - Domain class file generation
 * - Enum file generation
 *
 * Lifecycle phases:
 * - PREPARING_EACH_ENTITY: Validate entity names and prepare entity data
 * - PREPARING_EACH_ENTITY_FIELD: Validate and prepare field data
 * - PREPARING_EACH_ENTITY_RELATIONSHIP: Validate and prepare relationship data
 * - POST_PREPARING_EACH_ENTITY: Check for cyclic relationships
 * - WRITING: Generate base test utility files
 * - WRITING_ENTITIES: Generate entity domain classes and enums
 *
 * Usage:
 * <pre>
 * DomainGenerator generator = new DomainGenerator.Builder()
 *     .destinationPath("/path/to/project")
 *     .packageName("com.example.app")
 *     .generateEntities(true)
 *     .generateEnums(true)
 *     .useJakartaValidation(true)
 *     .build();
 *
 * generator.addEntity(entityContext);
 * generator.run();
 * </pre>
 */
public class DomainGenerator {

    private static final Logger LOGGER = Logger.getLogger(DomainGenerator.class.getName());

    // Generator configuration
    private final Path destinationPath;
    private final String packageName;
    private final boolean generateEntities;
    private final boolean generateEnums;
    private final boolean useJakartaValidation;
    private final boolean useJacksonIdentityInfo;

    // Application configuration
    private String javaPackageSrcDir = "src/main/java";
    private String javaPackageTestDir = "src/test/java";
    private String frontendAppName;

    // Entities to generate
    private final List<EntityContext> entities = new ArrayList<>();

    // Template engine (simplified - in practice, use a real template engine)
    private final TemplateEngine templateEngine;

    /**
     * Creates a new DomainGenerator.
     */
    private DomainGenerator(Builder builder) {
        this.destinationPath = builder.destinationPath;
        this.packageName = builder.packageName;
        this.generateEntities = builder.generateEntities;
        this.generateEnums = builder.generateEnums;
        this.useJakartaValidation = builder.useJakartaValidation;
        this.useJacksonIdentityInfo = builder.useJacksonIdentityInfo;
        this.javaPackageSrcDir = builder.javaPackageSrcDir;
        this.javaPackageTestDir = builder.javaPackageTestDir;
        this.frontendAppName = builder.frontendAppName;
        this.templateEngine = new TemplateEngine(destinationPath);
    }

    /**
     * Adds an entity to be generated.
     *
     * @param entity the entity context
     */
    public void addEntity(EntityContext entity) {
        entities.add(entity);
    }

    /**
     * Runs the complete domain generation workflow.
     */
    public void run() throws IOException {
        LOGGER.info("Starting domain generation for " + entities.size() + " entities");

        // Phase: PREPARING_EACH_ENTITY
        for (EntityContext entity : entities) {
            preparingEachEntity(entity);
        }

        // Phase: PREPARING_EACH_ENTITY_FIELD
        for (EntityContext entity : entities) {
            for (FieldContext field : entity.getFields()) {
                preparingEachEntityField(entity, field);
            }
        }

        // Phase: PREPARING_EACH_ENTITY_RELATIONSHIP
        for (EntityContext entity : entities) {
            for (RelationshipContext relationship : entity.getRelationships()) {
                preparingEachEntityRelationship(entity, relationship);
            }
        }

        // Phase: POST_PREPARING_EACH_ENTITY
        for (EntityContext entity : entities) {
            postPreparingEachEntity(entity);
        }

        // Phase: WRITING
        writing();

        // Phase: WRITING_ENTITIES
        writingEntities();

        LOGGER.info("Domain generation completed");
    }

    // ==================== PREPARING_EACH_ENTITY Phase ====================

    /**
     * Prepares each entity: validates name and sets entity domain layer flag.
     */
    private void preparingEachEntity(EntityContext entity) {
        LOGGER.fine("Preparing entity: " + entity.getEntityName());

        // Check for reserved Java keyword
        checkForReservedKeyword(entity.getEntityName(), "entity");

        // Mark as domain layer entity
        entity.setEntityDomainLayer(true);

        // Set package information
        if (entity.getPackageName() == null) {
            entity.setPackageName(packageName);
        }
        if (entity.getEntityAbsolutePackage() == null) {
            entity.setEntityAbsolutePackage(packageName);
        }
        entity.setJavaPackageSrcDir(javaPackageSrcDir);
        entity.setJavaPackageTestDir(javaPackageTestDir);

        // Set validation flags
        entity.setUseJakartaValidation(useJakartaValidation);
        entity.setUseJacksonIdentityInfo(useJacksonIdentityInfo);
    }

    // ==================== PREPARING_EACH_ENTITY_FIELD Phase ====================

    /**
     * Prepares each field: validates name and computes Java bean properties.
     */
    private void preparingEachEntityField(EntityContext entity, FieldContext field) {
        LOGGER.fine("Preparing field: " + entity.getEntityName() + "." + field.getFieldName());

        // Check for reserved Java keyword
        checkForReservedKeyword(field.getFieldName(), "field '" + field.getFieldName() +
                                                      "' in entity '" + entity.getEntityName() + "'");

        // Compute Java bean property name
        field.setPropertyJavaBeanName(JavaBeanUtils.javaBeanCase(field.getPropertyName() != null
            ? field.getPropertyName() : field.getFieldName()));

        // Set DTO Java type if using MapStruct
        if (entity.isDtoMapstruct() || entity.isBuiltIn()) {
            String dtoType = "text".equals(field.getFieldTypeBlobContent())
                ? "String"
                : field.getFieldType();
            field.setPropertyDtoJavaType(dtoType);
        }

        // Prepare computed properties
        field.prepare();
    }

    // ==================== PREPARING_EACH_ENTITY_RELATIONSHIP Phase ====================

    /**
     * Prepares each relationship: validates name and computes Java bean properties.
     */
    private void preparingEachEntityRelationship(EntityContext entity, RelationshipContext relationship) {
        LOGGER.fine("Preparing relationship: " + entity.getEntityName() + "." + relationship.getRelationshipName());

        // Check for reserved Java keyword
        checkForReservedKeyword(relationship.getRelationshipName(),
            "relationship '" + relationship.getRelationshipName() +
            "' in entity '" + entity.getEntityName() + "'");

        // Compute Java bean property name
        relationship.setPropertyJavaBeanName(JavaBeanUtils.javaBeanCase(
            relationship.getPropertyName() != null
                ? relationship.getPropertyName()
                : relationship.getRelationshipName()));

        // Set DTO Java type if using MapStruct
        if (entity.isDtoMapstruct()) {
            EntityContext otherEntity = relationship.getOtherEntity();
            if (otherEntity != null) {
                String dtoType = relationship.isCollection()
                    ? "Set<" + otherEntity.getDtoClass() + ">"
                    : otherEntity.getDtoClass();
                relationship.setPropertyDtoJavaType(dtoType);
            }
        }

        // Prepare computed properties
        relationship.prepare();
    }

    // ==================== POST_PREPARING_EACH_ENTITY Phase ====================

    /**
     * Post-preparation: checks for cyclic required relationships.
     */
    private void postPreparingEachEntity(EntityContext entity) {
        LOGGER.fine("Post-preparing entity: " + entity.getEntityName());

        // Compute field and relationship flags
        entity.computeFieldFlags();
        entity.computeRelationshipFlags();

        // Set skip JUnit tests if cyclic relationships detected
        if (entity.isHasCyclicRequiredRelationship()) {
            entity.setSkipJunitTests("Cyclic required relationships detected");
            LOGGER.warning("Entity " + entity.getEntityName() +
                          " has cyclic required relationships, skipping JUnit tests");
        }
    }

    // ==================== WRITING Phase ====================

    /**
     * Writes base domain utility files.
     */
    private void writing() throws IOException {
        LOGGER.fine("Phase: WRITING");

        // Generate base test files
        List<EntityServerFiles.FileConfig> baseFiles =
            EntityServerFiles.getBaseTestFiles(packageName, javaPackageTestDir);

        for (EntityServerFiles.FileConfig fileConfig : baseFiles) {
            Path outputPath = destinationPath.resolve(fileConfig.getOutputPath());
            templateEngine.generateFile(fileConfig.getTemplatePath(), outputPath, null);
        }
    }

    // ==================== WRITING_ENTITIES Phase ====================

    /**
     * Writes entity domain classes and enum files.
     */
    private void writingEntities() throws IOException {
        LOGGER.fine("Phase: WRITING_ENTITIES");

        if (!generateEntities) {
            LOGGER.info("Entity generation is disabled");
            return;
        }

        for (EntityContext entity : entities) {
            if (entity.isSkipServer()) {
                LOGGER.info("Skipping server generation for entity: " + entity.getEntityName());
                continue;
            }

            writeEntityFiles(entity);

            if (generateEnums) {
                writeEnumFiles(entity);
            }
        }
    }

    /**
     * Writes all files for a single entity.
     */
    private void writeEntityFiles(EntityContext entity) throws IOException {
        LOGGER.info("Generating domain files for entity: " + entity.getEntityName());

        // Model files (main entity class)
        List<EntityServerFiles.FileConfig> modelFiles = EntityServerFiles.getModelFiles(entity);
        for (EntityServerFiles.FileConfig fileConfig : modelFiles) {
            if (fileConfig.shouldGenerate(entity)) {
                Path outputPath = destinationPath.resolve(fileConfig.getOutputPath());
                templateEngine.generateFile(fileConfig.getTemplatePath(), outputPath, entity);
            }
        }

        // Test files
        List<EntityServerFiles.FileConfig> testFiles = EntityServerFiles.getModelTestFiles(entity);
        for (EntityServerFiles.FileConfig fileConfig : testFiles) {
            if (fileConfig.shouldGenerate(entity)) {
                Path outputPath = destinationPath.resolve(fileConfig.getOutputPath());
                templateEngine.generateFile(fileConfig.getTemplatePath(), outputPath, entity);
            }
        }

        // Server files (validation, Jackson)
        List<EntityServerFiles.FileConfig> serverFiles = EntityServerFiles.getServerFiles(entity);
        for (EntityServerFiles.FileConfig fileConfig : serverFiles) {
            if (fileConfig.shouldGenerate(entity)) {
                Path outputPath = destinationPath.resolve(fileConfig.getOutputPath());
                templateEngine.generateFile(fileConfig.getTemplatePath(), outputPath, entity);
            }
        }
    }

    /**
     * Writes enum files for an entity.
     */
    private void writeEnumFiles(EntityContext entity) throws IOException {
        for (FieldContext field : entity.getFields()) {
            if (!field.isFieldIsEnum()) {
                continue;
            }

            EnumInfo enumInfo = EnumInfo.fromField(
                field.getFieldType(),
                field.getFieldValues(),
                entity.getClientRootFolder()
            );
            enumInfo.setPackageName(packageName);
            enumInfo.setEntityAbsolutePackage(entity.getEntityAbsolutePackage());
            enumInfo.setEntityJavaPackageFolder(entity.getEntityJavaPackageFolder());
            enumInfo.setFrontendAppName(frontendAppName);

            List<EntityServerFiles.FileConfig> enumFiles =
                EntityServerFiles.getEnumFiles(enumInfo, entity);

            for (EntityServerFiles.FileConfig fileConfig : enumFiles) {
                Path outputPath = destinationPath.resolve(fileConfig.getOutputPath());
                templateEngine.generateEnumFile(fileConfig.getTemplatePath(), outputPath, enumInfo);
                LOGGER.info("Generated enum: " + enumInfo.getEnumName());
            }
        }
    }

    /**
     * Validates that a name is not a reserved Java keyword.
     */
    private void checkForReservedKeyword(String name, String context) {
        if (JavaBeanUtils.isReservedJavaKeyword(name)) {
            throw new IllegalArgumentException(
                "The " + context + " name '" + name + "' is a reserved Java keyword.");
        }
    }

    // ==================== Getters ====================

    public List<EntityContext> getEntities() {
        return entities;
    }

    // ==================== Builder ====================

    /**
     * Builder for DomainGenerator.
     */
    public static class Builder {
        private Path destinationPath = Paths.get(".");
        private String packageName = "com.example.app";
        private boolean generateEntities = true;
        private boolean generateEnums = true;
        private boolean useJakartaValidation = true;
        private boolean useJacksonIdentityInfo = false;
        private String javaPackageSrcDir = "src/main/java";
        private String javaPackageTestDir = "src/test/java";
        private String frontendAppName;

        public Builder destinationPath(String path) {
            this.destinationPath = Paths.get(path);
            return this;
        }

        public Builder destinationPath(Path path) {
            this.destinationPath = path;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder generateEntities(boolean generateEntities) {
            this.generateEntities = generateEntities;
            return this;
        }

        public Builder generateEnums(boolean generateEnums) {
            this.generateEnums = generateEnums;
            return this;
        }

        public Builder useJakartaValidation(boolean useJakartaValidation) {
            this.useJakartaValidation = useJakartaValidation;
            return this;
        }

        public Builder useJacksonIdentityInfo(boolean useJacksonIdentityInfo) {
            this.useJacksonIdentityInfo = useJacksonIdentityInfo;
            return this;
        }

        public Builder javaPackageSrcDir(String javaPackageSrcDir) {
            this.javaPackageSrcDir = javaPackageSrcDir;
            return this;
        }

        public Builder javaPackageTestDir(String javaPackageTestDir) {
            this.javaPackageTestDir = javaPackageTestDir;
            return this;
        }

        public Builder frontendAppName(String frontendAppName) {
            this.frontendAppName = frontendAppName;
            return this;
        }

        public DomainGenerator build() {
            return new DomainGenerator(this);
        }
    }

    // ==================== Template Engine ====================

    /**
     * Simple template engine for generating Java files.
     * In practice, this would use a real template engine like FreeMarker or Velocity.
     */
    public static class TemplateEngine {
        private final Path basePath;

        public TemplateEngine(Path basePath) {
            this.basePath = basePath;
        }

        public void generateFile(String templatePath, Path outputPath, EntityContext context) throws IOException {
            // Create parent directories
            Files.createDirectories(outputPath.getParent());

            // In a real implementation, this would:
            // 1. Load the template from templatePath
            // 2. Process it with the context
            // 3. Write the result to outputPath

            LOGGER.fine("Would generate: " + outputPath + " from template: " + templatePath);

            // Placeholder: generate a comment indicating the file would be generated
            String content = generatePlaceholderContent(templatePath, context);
            Files.writeString(outputPath, content);
        }

        public void generateEnumFile(String templatePath, Path outputPath, EnumInfo enumInfo) throws IOException {
            Files.createDirectories(outputPath.getParent());

            String content = generateEnumContent(enumInfo);
            Files.writeString(outputPath, content);
        }

        private String generatePlaceholderContent(String templatePath, EntityContext context) {
            StringBuilder sb = new StringBuilder();
            sb.append("// Generated from template: ").append(templatePath).append("\n");
            if (context != null) {
                sb.append("// Entity: ").append(context.getEntityName()).append("\n");
                sb.append("package ").append(context.getEntityAbsolutePackage()).append(".domain;\n\n");
                sb.append("// TODO: Implement template processing\n");
                sb.append("public class ").append(context.getPersistClass()).append(" {\n");
                sb.append("    // Fields and methods would be generated here\n");
                sb.append("}\n");
            }
            return sb.toString();
        }

        private String generateEnumContent(EnumInfo enumInfo) {
            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(enumInfo.getEnumPackage()).append(";\n\n");
            sb.append("/**\n");
            sb.append(" * The ").append(enumInfo.getEnumName()).append(" enumeration.\n");
            sb.append(" */\n");
            sb.append("public enum ").append(enumInfo.getEnumName()).append(" {\n");

            List<EnumInfo.EnumValue> values = enumInfo.getEnumValues();
            for (int i = 0; i < values.size(); i++) {
                EnumInfo.EnumValue value = values.get(i);
                sb.append("    ").append(value.getName());
                if (value.hasCustomValue()) {
                    sb.append("(\"").append(value.getCustomValue()).append("\")");
                }
                if (i < values.size() - 1) {
                    sb.append(",");
                } else {
                    sb.append(";");
                }
                sb.append("\n");
            }

            if (enumInfo.isEnumWithCustomValues()) {
                sb.append("\n");
                sb.append("    private final String value;\n\n");
                sb.append("    ").append(enumInfo.getEnumName()).append("(String value) {\n");
                sb.append("        this.value = value;\n");
                sb.append("    }\n\n");
                sb.append("    public String getValue() {\n");
                sb.append("        return value;\n");
                sb.append("    }\n");
            }

            sb.append("}\n");
            return sb.toString();
        }
    }

    // ==================== Main Method ====================

    /**
     * Main entry point for command-line usage.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: DomainGenerator <destinationPath> <packageName>");
            System.exit(1);
        }

        try {
            DomainGenerator generator = new DomainGenerator.Builder()
                .destinationPath(args[0])
                .packageName(args[1])
                .build();

            // In practice, entities would be loaded from configuration
            System.out.println("DomainGenerator initialized. Add entities and call run().");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
