/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.core;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Base class for all client generators.
 * Provides common functionality for template processing, file generation, and lifecycle management.
 */
public abstract class BaseClientGenerator {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ClientGeneratorContext context;
    protected final MustacheFactory mustacheFactory;

    // Generator lifecycle phases
    private final Map<GeneratorPhase, List<Runnable>> tasks = new EnumMap<>(GeneratorPhase.class);
    private final List<BaseClientGenerator> composedGenerators = new ArrayList<>();
    private final List<BaseClientGenerator> dependencies = new ArrayList<>();

    public BaseClientGenerator(ClientGeneratorContext context) {
        this.context = context;
        this.mustacheFactory = new DefaultMustacheFactory("templates/" + getTemplateDir());
        registerTasks();
    }

    /**
     * Get the generator name.
     */
    public abstract String getName();

    /**
     * Get the template directory name.
     */
    protected String getTemplateDir() {
        return getName();
    }

    /**
     * Register tasks for each lifecycle phase.
     */
    protected abstract void registerTasks();

    /**
     * Register a task for a specific phase.
     */
    protected void registerTask(GeneratorPhase phase, Runnable task) {
        tasks.computeIfAbsent(phase, k -> new ArrayList<>()).add(task);
    }

    /**
     * Compose with another generator.
     */
    protected void composeWith(BaseClientGenerator generator) {
        composedGenerators.add(generator);
    }

    /**
     * Declare a dependency on another generator.
     */
    protected void dependsOn(BaseClientGenerator generator) {
        dependencies.add(generator);
    }

    /**
     * Execute all phases of this generator.
     */
    public void generate() throws Exception {
        log.info("Starting {} generator", getName());

        // Execute dependencies first
        for (BaseClientGenerator dependency : dependencies) {
            dependency.generate();
        }

        // Execute lifecycle phases
        for (GeneratorPhase phase : GeneratorPhase.values()) {
            executePhase(phase);
        }

        // Execute composed generators
        for (BaseClientGenerator composed : composedGenerators) {
            composed.generate();
        }

        log.info("Completed {} generator", getName());
    }

    /**
     * Execute a specific phase.
     */
    private void executePhase(GeneratorPhase phase) {
        List<Runnable> phaseTasks = tasks.get(phase);
        if (phaseTasks != null && !phaseTasks.isEmpty()) {
            log.debug("Executing phase: {}", phase);
            for (Runnable task : phaseTasks) {
                try {
                    task.run();
                } catch (Exception e) {
                    throw new RuntimeException("Error in phase " + phase + ": " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Get the client configuration.
     */
    protected ClientConfig getConfig() {
        return context.getConfig();
    }

    /**
     * Get template data for Mustache rendering.
     */
    protected Map<String, Object> getTemplateData() {
        return context.getTemplateData();
    }

    /**
     * Process a template and return the result.
     */
    protected String processTemplate(String templateName) {
        return processTemplate(templateName, getTemplateData());
    }

    /**
     * Process a template with custom data.
     */
    protected String processTemplate(String templateName, Map<String, Object> data) {
        try {
            Mustache mustache = mustacheFactory.compile(templateName);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error processing template {}: {}", templateName, e.getMessage());
            throw new RuntimeException("Template processing failed: " + templateName, e);
        }
    }

    /**
     * Write a template to a destination path.
     */
    protected void writeTemplate(String templateName, String destinationPath) throws IOException {
        String content = processTemplate(templateName);
        Path path = context.destinationPath(destinationPath);
        context.writeFile(path, content);
    }

    /**
     * Write a template to the webapp directory.
     */
    protected void writeWebappTemplate(String templateName, String relativePath) throws IOException {
        String content = processTemplate(templateName);
        Path path = context.webappPath(relativePath);
        context.writeFile(path, content);
    }

    /**
     * Write a template to the app directory.
     */
    protected void writeAppTemplate(String templateName, String relativePath) throws IOException {
        String content = processTemplate(templateName);
        Path path = context.appPath(relativePath);
        context.writeFile(path, content);
    }

    /**
     * Write content directly to a file.
     */
    protected void writeFile(String destinationPath, String content) throws IOException {
        Path path = context.destinationPath(destinationPath);
        context.writeFile(path, content);
    }

    /**
     * Copy a file from resources to destination.
     */
    protected void copyFile(String resourcePath, String destinationPath) throws IOException {
        var inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        Path path = context.destinationPath(destinationPath);
        context.ensureDirectory(path.getParent());
        Files.copy(inputStream, path);
        log.info("Copied: {}", path);
    }

    /**
     * Check if a file exists in the destination.
     */
    protected boolean fileExists(String relativePath) {
        return Files.exists(context.destinationPath(relativePath));
    }

    /**
     * Delete a file from the destination.
     */
    protected void deleteFile(String relativePath) throws IOException {
        Path path = context.destinationPath(relativePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.debug("Deleted: {}", path);
        }
    }

    /**
     * Convert entity name to various formats.
     */
    protected Map<String, String> getEntityNames(String entityName) {
        Map<String, String> names = new HashMap<>();
        names.put("entityName", entityName);
        names.put("entityClass", capitalize(entityName));
        names.put("entityInstance", uncapitalize(entityName));
        names.put("entityFileName", toKebabCase(entityName));
        names.put("entityFolderName", toKebabCase(entityName));
        names.put("entityModelFileName", uncapitalize(entityName));
        names.put("entityAngularName", capitalize(entityName));
        names.put("entityReactName", capitalize(entityName));
        return names;
    }

    /**
     * Capitalize a string.
     */
    protected String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Uncapitalize a string.
     */
    protected String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Convert to kebab-case.
     */
    protected String toKebabCase(String str) {
        if (str == null) return null;
        return str.replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }

    /**
     * Convert to camelCase.
     */
    protected String toCamelCase(String str) {
        if (str == null) return null;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : str.toCharArray()) {
            if (c == '-' || c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * Generator lifecycle phases.
     */
    public enum GeneratorPhase {
        INITIALIZING,
        PROMPTING,
        CONFIGURING,
        COMPOSING,
        LOADING,
        PREPARING,
        PREPARING_EACH_ENTITY,
        DEFAULT,
        WRITING,
        WRITING_ENTITIES,
        POST_WRITING,
        POST_WRITING_ENTITIES,
        END
    }
}
