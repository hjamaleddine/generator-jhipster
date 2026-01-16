/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.jhipster.generator.model.EntityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generator context holding the current state of generation.
 * Shared across all generators in the composition chain.
 */
public class GeneratorContext {

    private static final Logger log = LoggerFactory.getLogger(GeneratorContext.class);

    private final Path destinationPath;
    private final JHipsterConfig jhipsterConfig;
    private final Map<String, EntityConfig> entities = new LinkedHashMap<>();
    private final Map<String, Object> sharedData = new ConcurrentHashMap<>();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Creates a new generator context.
     *
     * @param destinationPath the destination directory for generated files
     */
    public GeneratorContext(Path destinationPath) {
        this.destinationPath = destinationPath;
        this.jhipsterConfig = loadJHipsterConfig();
    }

    /**
     * Creates a new generator context with a specific configuration.
     *
     * @param destinationPath the destination directory
     * @param config the JHipster configuration
     */
    public GeneratorContext(Path destinationPath, JHipsterConfig config) {
        this.destinationPath = destinationPath;
        this.jhipsterConfig = config;
    }

    /**
     * Loads JHipster configuration from .yo-rc.json file.
     */
    private JHipsterConfig loadJHipsterConfig() {
        Path yoRcPath = destinationPath.resolve(".yo-rc.json");
        if (Files.exists(yoRcPath)) {
            try {
                JsonNode root = jsonMapper.readTree(yoRcPath.toFile());
                JsonNode jhipsterNode = root.get("generator-jhipster");
                if (jhipsterNode != null) {
                    return jsonMapper.treeToValue(jhipsterNode, JHipsterConfig.class);
                }
            } catch (IOException e) {
                log.warn("Failed to load .yo-rc.json: {}", e.getMessage());
            }
        }
        log.info("No .yo-rc.json found, using default configuration");
        return new JHipsterConfig();
    }

    /**
     * Saves the JHipster configuration to .yo-rc.json.
     */
    public void saveJHipsterConfig() throws IOException {
        Path yoRcPath = destinationPath.resolve(".yo-rc.json");
        Map<String, Object> root = new LinkedHashMap<>();

        // Load existing if present
        if (Files.exists(yoRcPath)) {
            root = jsonMapper.readValue(yoRcPath.toFile(), Map.class);
        }

        root.put("generator-jhipster", jhipsterConfig);

        Files.writeString(yoRcPath, jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        log.info("Saved configuration to .yo-rc.json");
    }

    /**
     * Loads entity configurations from .jhipster directory.
     */
    public void loadEntities() {
        Path jhipsterDir = destinationPath.resolve(".jhipster");
        if (Files.exists(jhipsterDir) && Files.isDirectory(jhipsterDir)) {
            try {
                Files.list(jhipsterDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(this::loadEntity);
            } catch (IOException e) {
                log.warn("Failed to load entities: {}", e.getMessage());
            }
        }
    }

    /**
     * Loads a single entity configuration.
     */
    private void loadEntity(Path entityPath) {
        try {
            EntityConfig entity = jsonMapper.readValue(entityPath.toFile(), EntityConfig.class);
            String name = entityPath.getFileName().toString().replace(".json", "");
            entity.setName(name);
            entities.put(name, entity);
            log.debug("Loaded entity: {}", name);
        } catch (IOException e) {
            log.warn("Failed to load entity {}: {}", entityPath, e.getMessage());
        }
    }

    /**
     * Saves entity configurations to .jhipster directory.
     */
    public void saveEntities() throws IOException {
        Path jhipsterDir = destinationPath.resolve(".jhipster");
        Files.createDirectories(jhipsterDir);

        for (Map.Entry<String, EntityConfig> entry : entities.entrySet()) {
            Path entityPath = jhipsterDir.resolve(entry.getKey() + ".json");
            Files.writeString(entityPath, jsonMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(entry.getValue()));
        }
    }

    // Getters

    public Path getDestinationPath() {
        return destinationPath;
    }

    public JHipsterConfig getJHipsterConfig() {
        return jhipsterConfig;
    }

    public List<EntityConfig> getEntities() {
        return new ArrayList<>(entities.values());
    }

    public EntityConfig getEntity(String name) {
        return entities.get(name);
    }

    public void addEntity(EntityConfig entity) {
        entities.put(entity.getName(), entity);
    }

    public void removeEntity(String name) {
        entities.remove(name);
    }

    public void setEntities(List<EntityConfig> entityList) {
        entities.clear();
        for (EntityConfig entity : entityList) {
            entities.put(entity.getName(), entity);
        }
    }

    // Shared data methods

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, T defaultValue) {
        Object value = sharedData.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    public void setConfigValue(String key, Object value) {
        sharedData.put(key, value);
    }

    public boolean hasConfigValue(String key) {
        return sharedData.containsKey(key);
    }

    // Path helpers

    public String getMainJavaPath() {
        return "src/main/java/" + jhipsterConfig.getPackageFolder() + "/";
    }

    public String getTestJavaPath() {
        return "src/test/java/" + jhipsterConfig.getPackageFolder() + "/";
    }

    public String getMainResourcesPath() {
        return "src/main/resources/";
    }

    public String getTestResourcesPath() {
        return "src/test/resources/";
    }

    public String getDockerPath() {
        return "src/main/docker/";
    }

    /**
     * Returns the base path (alias for getDestinationPath).
     */
    public Path getBasePath() {
        return destinationPath;
    }

    /**
     * Returns a simple bean factory for dependency management.
     * This is a simplified version - in a full implementation,
     * this would be a proper Spring BeanFactory.
     */
    public Object getBeanFactory() {
        return sharedData;
    }

    /**
     * Gets a config value with just the key (returns null if not found).
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        return (T) sharedData.get(key);
    }

    // JSON/YAML utilities

    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public ObjectMapper getYamlMapper() {
        return yamlMapper;
    }
}
