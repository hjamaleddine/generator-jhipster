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

import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base generator class providing core functionality for all generators.
 * This is the Java equivalent of base-core/generator.ts and base/generator.ts.
 */
public abstract class BaseGenerator {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final GeneratorContext context;
    protected final TemplateEngine templateEngine;
    protected final Path destinationPath;

    private final Map<GeneratorPriority, List<GeneratorTask>> tasks = new TreeMap<>(
        Comparator.comparingInt(GeneratorPriority::getOrder)
    );

    private final Set<String> composedGenerators = ConcurrentHashMap.newKeySet();
    private final List<BaseGenerator> childGenerators = new ArrayList<>();

    protected BaseGenerator(GeneratorContext context) {
        this.context = context;
        this.templateEngine = new TemplateEngine();
        this.destinationPath = context.getDestinationPath();
    }

    /**
     * Returns the generator name.
     */
    public abstract String getName();

    /**
     * Registers tasks for each priority phase.
     * Subclasses override this to register their tasks.
     */
    protected abstract void registerTasks();

    /**
     * Called before the generator is queued.
     * Used for declaring dependencies on other generators.
     */
    protected void beforeQueue() {
        // Override in subclasses
    }

    /**
     * Registers a task at the specified priority.
     */
    protected void registerTask(GeneratorPriority priority, String name, GeneratorTask task) {
        tasks.computeIfAbsent(priority, k -> new ArrayList<>())
            .add(new GeneratorTask() {
                @Override
                public void execute() throws Exception {
                    task.execute();
                }

                @Override
                public String getName() {
                    return name;
                }
            });
    }

    /**
     * Composes with another generator.
     * The composed generator will be executed as part of this generator's lifecycle.
     */
    protected void composeWith(BaseGenerator generator) {
        String generatorName = generator.getName();
        if (composedGenerators.add(generatorName)) {
            log.debug("Composing {} with {}", getName(), generatorName);
            generator.beforeQueue();
            generator.registerTasks();
            childGenerators.add(generator);
        }
    }

    /**
     * Declares a dependency on another generator.
     * The dependent generator will be executed before this one.
     */
    protected void dependsOn(BaseGenerator generator) {
        composeWith(generator);
    }

    /**
     * Runs all registered tasks in priority order.
     * Handles dynamic composition by merging child generator tasks after each priority.
     */
    public void run() throws Exception {
        log.info("Running generator: {}", getName());

        beforeQueue();
        registerTasks();

        // Merge tasks from generators composed in beforeQueue/registerTasks
        mergeAllChildTasks();

        // Get priorities in execution order
        Set<GeneratorPriority> executedPriorities = new HashSet<>();

        while (true) {
            // Find next priority to execute
            GeneratorPriority nextPriority = null;
            for (GeneratorPriority priority : tasks.keySet()) {
                if (!executedPriorities.contains(priority)) {
                    nextPriority = priority;
                    break;
                }
            }

            if (nextPriority == null) {
                break; // All priorities executed
            }

            log.debug("Executing priority: {}", nextPriority);

            // Execute tasks at this priority (create copy to avoid concurrent modification)
            List<GeneratorTask> tasksAtPriority = new ArrayList<>(tasks.getOrDefault(nextPriority, Collections.emptyList()));
            for (GeneratorTask task : tasksAtPriority) {
                log.debug("  Running task: {}", task.getName());
                task.execute();
            }

            executedPriorities.add(nextPriority);

            // After executing tasks at this priority, merge any newly composed generators
            // This handles generators composed during COMPOSING phase
            mergeAllChildTasks();
        }

        log.info("Generator {} completed", getName());
    }

    private final Set<String> mergedGeneratorNames = new HashSet<>();

    /**
     * Merges tasks from all unmerged child generators, recursively.
     */
    private void mergeAllChildTasks() {
        mergeChildTasksRecursively(childGenerators);
    }

    /**
     * Recursively merges tasks from child generators and their children.
     */
    private void mergeChildTasksRecursively(List<BaseGenerator> generators) {
        for (BaseGenerator child : generators) {
            if (mergedGeneratorNames.add(child.getName())) {
                log.debug("Merging tasks from: {}", child.getName());
                for (Map.Entry<GeneratorPriority, List<GeneratorTask>> entry : child.tasks.entrySet()) {
                    tasks.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                        .addAll(entry.getValue());
                }
                // Recursively merge grandchildren
                if (!child.childGenerators.isEmpty()) {
                    mergeChildTasksRecursively(child.childGenerators);
                }
            }
        }
    }

    // ==================== File Operations ====================

    /**
     * Writes content to a file in the destination directory.
     */
    protected void writeFile(String relativePath, String content) throws IOException {
        Path filePath = destinationPath.resolve(relativePath);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, content);
        log.debug("Written: {}", relativePath);
    }

    /**
     * Writes a template to a file.
     */
    protected void writeTemplate(String templateName, String relativePath, Map<String, Object> data) throws IOException {
        String content = templateEngine.render(templateName, data);
        writeFile(relativePath, content);
    }

    /**
     * Copies a file from source to destination.
     */
    protected void copyFile(Path source, String relativePath) throws IOException {
        Path destPath = destinationPath.resolve(relativePath);
        Files.createDirectories(destPath.getParent());
        Files.copy(source, destPath);
        log.debug("Copied: {}", relativePath);
    }

    /**
     * Creates a directory in the destination path.
     */
    protected void createDirectory(String relativePath) throws IOException {
        Path dirPath = destinationPath.resolve(relativePath);
        Files.createDirectories(dirPath);
        log.debug("Created directory: {}", relativePath);
    }

    /**
     * Checks if a file exists in the destination path.
     */
    protected boolean fileExists(String relativePath) {
        return Files.exists(destinationPath.resolve(relativePath));
    }

    // ==================== Configuration Helpers ====================

    /**
     * Gets the JHipster configuration.
     */
    protected JHipsterConfig getConfig() {
        return context.getJHipsterConfig();
    }

    /**
     * Gets a configuration value with a default.
     */
    protected <T> T getConfigValue(String key, T defaultValue) {
        return context.getConfigValue(key, defaultValue);
    }

    /**
     * Sets a configuration value.
     */
    protected void setConfigValue(String key, Object value) {
        context.setConfigValue(key, value);
    }

    // ==================== Path Helpers ====================

    /**
     * Gets the main Java source path.
     */
    protected String getMainJavaPath() {
        return "src/main/java/" + getConfig().getPackageFolder() + "/";
    }

    /**
     * Gets the test Java source path.
     */
    protected String getTestJavaPath() {
        return "src/test/java/" + getConfig().getPackageFolder() + "/";
    }

    /**
     * Gets the main resources path.
     */
    protected String getMainResourcesPath() {
        return "src/main/resources/";
    }

    /**
     * Gets the test resources path.
     */
    protected String getTestResourcesPath() {
        return "src/test/resources/";
    }
}
