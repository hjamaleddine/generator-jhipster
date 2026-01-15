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
package io.github.jhipster.generator.entity;

import io.github.jhipster.generator.entity.enums.ApplicationType;
import io.github.jhipster.generator.entity.enums.DatabaseType;
import io.github.jhipster.generator.entity.model.EntityConfig;
import io.github.jhipster.generator.entity.model.EntityData;
import io.github.jhipster.generator.entity.support.EntityValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entity generator class implementing the JHipster entity generation workflow.
 *
 * This is a Java port of the TypeScript generator.ts, following the same lifecycle phases:
 * - INITIALIZING: Parse options and load entity configuration
 * - PROMPTING: Ask user for microservice configuration
 * - LOADING: Load and validate entity configuration
 * - POST_PREPARING: Interactive prompts for entity definition
 * - END: Generation complete notification
 *
 * Usage:
 * <pre>
 * EntityGenerator generator = new EntityGenerator.Builder()
 *     .entityName("Product")
 *     .destinationPath("/path/to/project")
 *     .build();
 *
 * generator.run();
 * </pre>
 */
public class EntityGenerator {

    private static final Logger LOGGER = Logger.getLogger(EntityGenerator.class.getName());
    private static final String JHIPSTER_CONFIG_DIR = ".jhipster";

    // Generator options
    private final String entityName;
    private final Path destinationPath;
    private final GeneratorOptions options;

    // Application configuration
    private ApplicationConfig applicationConfig;

    // Entity state
    private EntityConfig entityConfig;
    private EntityData entityData;

    // Prompts handler
    private EntityPrompts prompts;

    /**
     * Creates a new EntityGenerator with the specified configuration.
     */
    private EntityGenerator(Builder builder) {
        this.entityName = capitalize(builder.entityName);
        this.destinationPath = builder.destinationPath;
        this.options = builder.options;
    }

    /**
     * Runs the complete entity generation workflow.
     */
    public void run() throws IOException {
        LOGGER.info("Starting entity generation for: " + entityName);

        try {
            // Phase: INITIALIZING
            initializing();

            // Phase: PROMPTING
            prompting();

            // Phase: LOADING
            loading();

            // Phase: POST_PREPARING (interactive prompts)
            postPreparing();

            // Phase: END
            end();

        } catch (EntityPrompts.EntityGenerationAbortedException e) {
            LOGGER.info(e.getMessage());
        }
    }

    // ==================== INITIALIZING Phase ====================

    /**
     * INITIALIZING phase: Parse options and set up entity configuration storage.
     */
    private void initializing() throws IOException {
        LOGGER.fine("Phase: INITIALIZING");

        parseOptions();
        loadOptions();
    }

    /**
     * Parses initial options and sets up entity data.
     */
    private void parseOptions() throws IOException {
        // Load or create entity configuration
        Path configPath = getEntityConfigPath();
        boolean configExisted = Files.exists(configPath);

        if (configExisted) {
            entityConfig = EntityConfig.load(configPath.toFile());
        } else {
            entityConfig = new EntityConfig(entityName);
        }
        entityConfig.setName(entityName);

        // Create entity data for runtime state
        entityData = new EntityData(entityName);
        entityData.setFilename(configPath.toString());
        entityData.setConfigExisted(configExisted);
        entityData.setEntityExisted(configExisted);
        entityData.setConfigurationFileExists(configExisted);
        entityData.setRegenerate(options.isRegenerate());

        // Load application configuration
        loadApplicationConfig();

        // Register entity in application config
        registerEntity();

        // Set up options from generator options
        setupEntityOptions();
    }

    /**
     * Loads additional options from command line or configuration.
     */
    private void loadOptions() {
        if (options.getDatabaseType() != null) {
            DatabaseType dbType = DatabaseType.fromDBValue(options.getDatabaseType());
            entityConfig.setDatabaseType(dbType.getValue());
            if (dbType == DatabaseType.SQL) {
                entityConfig.setProdDatabaseType(options.getDatabaseType());
                entityConfig.setDevDatabaseType(options.getDatabaseType());
            }
        }

        if (options.getSkipServer() != null) {
            entityConfig.setSkipServer(options.getSkipServer());
            entityData.setSkipServer(options.getSkipServer());
        }

        if (options.getSkipClient() != null) {
            entityConfig.setSkipClient(options.getSkipClient());
            entityData.setSkipClient(options.getSkipClient());
        }

        if (options.getSkipDbChangelog() != null) {
            entityConfig.setSkipDbChangelog(options.getSkipDbChangelog());
        }
    }

    /**
     * Sets up entity options from generator options.
     */
    private void setupEntityOptions() {
        if (options.getSkipCheckLengthOfIdentifier() != null) {
            entityConfig.setSkipCheckLengthOfIdentifier(options.getSkipCheckLengthOfIdentifier());
        }

        if (options.getAngularSuffix() != null) {
            entityConfig.setAngularJSSuffix(options.getAngularSuffix());
        }

        if (options.getSkipUiGrouping() != null) {
            entityConfig.setSkipUiGrouping(options.getSkipUiGrouping());
            entityData.setSkipUiGrouping(options.getSkipUiGrouping());
        }

        if (options.getClientRootFolder() != null) {
            if (Boolean.TRUE.equals(entityConfig.getSkipUiGrouping())) {
                LOGGER.warning("Ignoring client-root-folder due to skip-ui-grouping configuration");
            } else {
                entityConfig.setClientRootFolder(options.getClientRootFolder());
                entityData.setClientRootFolder(options.getClientRootFolder());
            }
        }

        if (options.getTableName() != null) {
            entityConfig.setEntityTableName(EntityValidator.hibernateSnakeCase(options.getTableName()));
        }
    }

    // ==================== PROMPTING Phase ====================

    /**
     * PROMPTING phase: Ask for microservice JSON configuration if applicable.
     */
    private void prompting() throws IOException {
        LOGGER.fine("Phase: PROMPTING");

        initializePrompts();
        prompts.askForMicroserviceJson(
            applicationConfig.getApplicationType(),
            applicationConfig.getDatabaseType()
        );
    }

    // ==================== LOADING Phase ====================

    /**
     * LOADING phase: Load, validate, and bootstrap entity configuration.
     */
    private void loading() throws IOException {
        LOGGER.fine("Phase: LOADING");

        checkBuiltInEntity();
        setupMicroServiceEntity();
        loadEntitySpecificOptions();
        validateEntityName();
        bootstrapConfig();
    }

    /**
     * Checks if entity is a built-in entity that cannot be overridden.
     */
    private void checkBuiltInEntity() {
        if (isBuiltInUser(entityName) || isBuiltInAuthority(entityName)) {
            throw new IllegalArgumentException("Cannot override built-in entity: " + entityName);
        }
    }

    /**
     * Sets up microservice-specific entity configuration.
     */
    private void setupMicroServiceEntity() throws IOException {
        ApplicationType appType = applicationConfig.getApplicationType();

        if (appType == ApplicationType.MICROSERVICE) {
            entityConfig.setMicroserviceName(applicationConfig.getBaseName());
            entityData.setMicroserviceName(applicationConfig.getBaseName());
            if (entityConfig.getClientRootFolder() == null) {
                entityConfig.setClientRootFolder(entityConfig.getMicroserviceName());
                entityData.setClientRootFolder(entityConfig.getMicroserviceName());
            }
        } else if (appType == ApplicationType.GATEWAY) {
            // If microservicePath is set, load entity from microservice
            boolean useMicroserviceJson = entityConfig.getMicroservicePath() != null;
            entityData.setUseMicroserviceJson(useMicroserviceJson);

            if (useMicroserviceJson) {
                Path microserviceConfigPath = Paths.get(
                    entityConfig.getMicroservicePath(),
                    JHIPSTER_CONFIG_DIR,
                    entityName + ".json"
                );
                entityData.setMicroserviceFileName(microserviceConfigPath.toString());
                entityData.setUseConfigurationFile(true);

                LOGGER.info("The entity " + entityName + " is being updated.");

                if (Files.exists(microserviceConfigPath)) {
                    EntityConfig microserviceConfig = EntityConfig.load(microserviceConfigPath.toFile());
                    mergeConfig(microserviceConfig);
                } else {
                    throw new IOException("Entity configuration file not found: " + microserviceConfigPath);
                }
            }

            if (entityConfig.getClientRootFolder() == null) {
                entityConfig.setClientRootFolder(
                    entityData.isSkipUiGrouping() ? "" : entityConfig.getMicroserviceName()
                );
            }
        }
    }

    /**
     * Loads entity-specific options from configuration.
     */
    private void loadEntitySpecificOptions() {
        if (entityConfig.getSkipClient() != null) {
            entityData.setSkipClient(entityConfig.getSkipClient());
        }

        DatabaseType dbType = entityConfig.getDatabaseTypeEnum();
        if (dbType == null) {
            dbType = applicationConfig.getDatabaseType();
        }
        entityData.setDatabaseType(dbType);
    }

    /**
     * Validates the entity name.
     */
    private void validateEntityName() {
        Optional<String> validationError = EntityValidator.validateEntityName(
            entityName, entityData.isSkipServer());

        if (validationError.isPresent()) {
            throw new IllegalArgumentException(validationError.get());
        }
    }

    /**
     * Bootstraps the entity configuration with defaults.
     */
    private void bootstrapConfig() {
        ApplicationType appType = applicationConfig.getApplicationType();

        if (appType == ApplicationType.MICROSERVICE || appType == ApplicationType.GATEWAY) {
            if (entityConfig.getDatabaseType() == null) {
                entityConfig.setDatabaseType(entityData.getDatabaseType().getValue());
            }
        }

        entityData.setUseConfigurationFile(
            entityData.isConfigurationFileExists() || entityData.isUseConfigurationFile()
        );

        if (entityData.isConfigurationFileExists()) {
            LOGGER.info("Found the " + entityData.getFilename() +
                       " configuration file, entity can be automatically generated!");
        }

        // Initialize fields and relationships if not present
        if (entityConfig.getFields() == null) {
            entityConfig.setFields(new ArrayList<>());
        }
        if (entityConfig.getRelationships() == null) {
            entityConfig.setRelationships(new ArrayList<>());
        }

        if (!entityData.isUseConfigurationFile()) {
            LOGGER.info("The entity " + entityName + " is being created.");
        }
    }

    // ==================== POST_PREPARING Phase ====================

    /**
     * POST_PREPARING phase: Interactive prompts for entity definition.
     */
    private void postPreparing() throws IOException {
        LOGGER.fine("Phase: POST_PREPARING");

        // Ask for update mode if entity exists
        prompts.askForUpdate(options.isForce());

        // Ask for fields
        prompts.askForFields(options.isDefaults());

        // Ask for fields to remove
        prompts.askForFieldsToRemove();

        // Ask for relationships
        prompts.askForRelationships(
            applicationConfig.getApplicationType(),
            applicationConfig.isGenerateBuiltInUserEntity()
        );

        // Ask for relationships to remove
        prompts.askForRelationsToRemove();

        // Ask for service layer
        prompts.askForService();

        // Ask for DTO
        prompts.askForDTO();

        // Ask for filtering
        prompts.askForFiltering();

        // Ask for read-only
        prompts.askForReadOnly();

        // Ask for pagination
        prompts.askForPagination();

        // Save entity configuration
        saveEntityConfig();
    }

    // ==================== END Phase ====================

    /**
     * END phase: Notify user of successful generation.
     */
    private void end() {
        LOGGER.fine("Phase: END");
        LOGGER.info("Entity " + entityData.getEntityNameCapitalized() + " generated successfully.");
    }

    // ==================== Helper Methods ====================

    /**
     * Gets the path to the entity configuration file.
     */
    private Path getEntityConfigPath() {
        return destinationPath.resolve(JHIPSTER_CONFIG_DIR).resolve(entityName + ".json");
    }

    /**
     * Loads the application configuration from .yo-rc.json or similar.
     */
    private void loadApplicationConfig() throws IOException {
        Path yoRcPath = destinationPath.resolve(".yo-rc.json");
        if (Files.exists(yoRcPath)) {
            applicationConfig = ApplicationConfig.load(yoRcPath);
        } else {
            applicationConfig = new ApplicationConfig();
        }
    }

    /**
     * Registers the entity in the application's entity list.
     */
    private void registerEntity() {
        if (applicationConfig.getEntities() == null) {
            applicationConfig.setEntities(new ArrayList<>());
        }
        if (!applicationConfig.getEntities().contains(entityName)) {
            applicationConfig.getEntities().add(entityName);
        }
    }

    /**
     * Initializes the prompts handler.
     */
    private void initializePrompts() {
        List<String> existingEntities = getExistingEntityNames();
        prompts = new EntityPrompts(entityConfig, entityData, existingEntities);
    }

    /**
     * Gets list of existing entity names from .jhipster directory.
     */
    private List<String> getExistingEntityNames() {
        List<String> entities = new ArrayList<>();
        Path jhipsterDir = destinationPath.resolve(JHIPSTER_CONFIG_DIR);

        if (Files.exists(jhipsterDir) && Files.isDirectory(jhipsterDir)) {
            try {
                Files.list(jhipsterDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .filter(name -> !name.equals(entityName))
                    .forEach(entities::add);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not list existing entities", e);
            }
        }

        return entities;
    }

    /**
     * Merges configuration from microservice entity.
     */
    private void mergeConfig(EntityConfig source) {
        if (source.getFields() != null) {
            entityConfig.setFields(source.getFields());
        }
        if (source.getRelationships() != null) {
            entityConfig.setRelationships(source.getRelationships());
        }
        if (source.getService() != null) {
            entityConfig.setService(source.getService());
        }
        if (source.getDto() != null) {
            entityConfig.setDto(source.getDto());
        }
        if (source.getPagination() != null) {
            entityConfig.setPagination(source.getPagination());
        }
        if (source.getJpaMetamodelFiltering() != null) {
            entityConfig.setJpaMetamodelFiltering(source.getJpaMetamodelFiltering());
        }
    }

    /**
     * Saves the entity configuration to file.
     */
    private void saveEntityConfig() throws IOException {
        Path configPath = getEntityConfigPath();
        Files.createDirectories(configPath.getParent());
        entityConfig.save(configPath.toFile());
        LOGGER.info("Entity configuration saved to: " + configPath);
    }

    /**
     * Checks if entity is a built-in User entity.
     */
    private boolean isBuiltInUser(String name) {
        return applicationConfig.isGenerateBuiltInUserEntity() && "User".equalsIgnoreCase(name);
    }

    /**
     * Checks if entity is a built-in Authority entity.
     */
    private boolean isBuiltInAuthority(String name) {
        return applicationConfig.isGenerateBuiltInAuthorityEntity() && "Authority".equalsIgnoreCase(name);
    }

    /**
     * Capitalizes the first letter of a string.
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    // ==================== Getters ====================

    public EntityConfig getEntityConfig() {
        return entityConfig;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    // ==================== Builder ====================

    /**
     * Builder for EntityGenerator.
     */
    public static class Builder {
        private String entityName;
        private Path destinationPath = Paths.get(".");
        private GeneratorOptions options = new GeneratorOptions();

        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder destinationPath(String path) {
            this.destinationPath = Paths.get(path);
            return this;
        }

        public Builder destinationPath(Path path) {
            this.destinationPath = path;
            return this;
        }

        public Builder options(GeneratorOptions options) {
            this.options = options;
            return this;
        }

        public Builder force(boolean force) {
            this.options.setForce(force);
            return this;
        }

        public Builder regenerate(boolean regenerate) {
            this.options.setRegenerate(regenerate);
            return this;
        }

        public Builder skipServer(boolean skipServer) {
            this.options.setSkipServer(skipServer);
            return this;
        }

        public Builder skipClient(boolean skipClient) {
            this.options.setSkipClient(skipClient);
            return this;
        }

        public Builder databaseType(String databaseType) {
            this.options.setDatabaseType(databaseType);
            return this;
        }

        public EntityGenerator build() {
            Objects.requireNonNull(entityName, "Entity name is required");
            return new EntityGenerator(this);
        }
    }

    // ==================== Main Method ====================

    /**
     * Main entry point for command-line usage.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: EntityGenerator <entityName> [destinationPath]");
            System.exit(1);
        }

        String entityName = args[0];
        String destPath = args.length > 1 ? args[1] : ".";

        try {
            EntityGenerator generator = new EntityGenerator.Builder()
                .entityName(entityName)
                .destinationPath(destPath)
                .build();

            generator.run();

        } catch (Exception e) {
            System.err.println("Error generating entity: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
