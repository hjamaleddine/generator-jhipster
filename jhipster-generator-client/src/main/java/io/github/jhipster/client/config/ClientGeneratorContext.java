/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Context for client-side generation.
 * Holds configuration and provides utility methods.
 */
public class ClientGeneratorContext {

    private static final Logger log = LoggerFactory.getLogger(ClientGeneratorContext.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path destinationRoot;
    private final ClientConfig config;
    private final Map<String, Object> templateData;

    public ClientGeneratorContext(Path destinationRoot, ClientConfig config) {
        this.destinationRoot = destinationRoot;
        this.config = config;
        this.templateData = new HashMap<>();
        initializeTemplateData();
    }

    /**
     * Load context from .yo-rc.json file.
     */
    public static ClientGeneratorContext fromYoRc(Path projectPath) throws IOException {
        Path yoRcPath = projectPath.resolve(".yo-rc.json");
        if (!Files.exists(yoRcPath)) {
            throw new IOException(".yo-rc.json not found at: " + yoRcPath);
        }

        Map<String, Object> yoRc = objectMapper.readValue(yoRcPath.toFile(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> generatorConfig = (Map<String, Object>) yoRc.get("generator-jhipster");

        if (generatorConfig == null) {
            throw new IOException("No 'generator-jhipster' configuration found in .yo-rc.json");
        }

        ClientConfig config = objectMapper.convertValue(generatorConfig, ClientConfig.class);
        return new ClientGeneratorContext(projectPath, config);
    }

    /**
     * Initialize template data from configuration.
     */
    private void initializeTemplateData() {
        // Application info
        templateData.put("baseName", config.getBaseName());
        templateData.put("packageName", config.getPackageName());
        templateData.put("baseNameLowerCase", config.getBaseName() != null ? config.getBaseName().toLowerCase() : "");
        templateData.put("baseNameUpperCase", config.getBaseName() != null ? config.getBaseName().toUpperCase() : "");

        // Client framework
        templateData.put("clientFramework", config.getClientFramework());
        templateData.put("isAngular", config.isAngular());
        templateData.put("isReact", config.isReact());
        templateData.put("isVue", config.isVue());

        // Bundler
        templateData.put("clientBundler", config.getClientBundler());
        templateData.put("isWebpack", config.isWebpack());
        templateData.put("isVite", config.isVite());
        templateData.put("isEsbuild", config.isEsbuild());

        // Theme
        templateData.put("clientTheme", config.getClientTheme());
        templateData.put("clientThemeVariant", config.getClientThemeVariant());
        templateData.put("hasTheme", config.getClientTheme() != null && !"none".equals(config.getClientTheme()));

        // i18n
        templateData.put("enableTranslation", config.getEnableTranslation());
        templateData.put("nativeLanguage", config.getNativeLanguage());
        templateData.put("languages", config.getLanguages());
        templateData.put("enableI18nRTL", config.getEnableI18nRTL());

        // Authentication
        templateData.put("authenticationType", config.getAuthenticationType());
        templateData.put("isJwt", config.isJwt());
        templateData.put("isOAuth2", config.isOAuth2());
        templateData.put("isSession", config.isSession());

        // Application type
        templateData.put("applicationType", config.getApplicationType());
        templateData.put("isMonolith", config.isMonolith());
        templateData.put("isGateway", config.isGateway());
        templateData.put("isMicroservice", config.isMicroservice());

        // Features
        templateData.put("hasWebsocket", config.hasWebsocket());
        templateData.put("websocket", config.getWebsocket());
        templateData.put("hasCypress", config.hasCypress());
        templateData.put("microfrontend", config.getMicrofrontend());

        // Paths
        templateData.put("clientSrcDir", config.getClientSrcDir());
        templateData.put("clientTestDir", config.getClientTestDir());
        templateData.put("clientWebappDir", config.getClientWebappDir());
        templateData.put("devServerPort", config.getDevServerPort());

        // Test
        templateData.put("clientTestFramework", config.getClientTestFramework());
        templateData.put("isJest", "jest".equals(config.getClientTestFramework()));
        templateData.put("isVitest", "vitest".equals(config.getClientTestFramework()));
    }

    public Path getDestinationRoot() {
        return destinationRoot;
    }

    public ClientConfig getConfig() {
        return config;
    }

    public Map<String, Object> getTemplateData() {
        return templateData;
    }

    /**
     * Resolve a path relative to destination root.
     */
    public Path destinationPath(String relativePath) {
        return destinationRoot.resolve(relativePath);
    }

    /**
     * Get the webapp directory path.
     */
    public Path webappPath(String relativePath) {
        return destinationRoot.resolve(config.getClientSrcDir()).resolve(relativePath);
    }

    /**
     * Get the app directory path.
     */
    public Path appPath(String relativePath) {
        return destinationRoot.resolve(config.getClientWebappDir()).resolve(relativePath);
    }

    /**
     * Create directory if it doesn't exist.
     */
    public void ensureDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.debug("Created directory: {}", path);
        }
    }

    /**
     * Write content to file.
     */
    public void writeFile(Path path, String content) throws IOException {
        ensureDirectory(path.getParent());
        Files.writeString(path, content);
        log.info("Generated: {}", destinationRoot.relativize(path));
    }

    /**
     * Save configuration to .yo-rc.json
     */
    public void saveConfig() throws IOException {
        Path yoRcPath = destinationRoot.resolve(".yo-rc.json");
        Map<String, Object> yoRc = new HashMap<>();

        if (Files.exists(yoRcPath)) {
            yoRc = objectMapper.readValue(yoRcPath.toFile(), Map.class);
        }

        yoRc.put("generator-jhipster", objectMapper.convertValue(config, Map.class));
        objectMapper.writeValue(yoRcPath.toFile(), yoRc);
        log.info("Saved configuration to .yo-rc.json");
    }

    /**
     * Add template data.
     */
    public void addTemplateData(String key, Object value) {
        templateData.put(key, value);
    }

    /**
     * Add multiple template data entries.
     */
    public void addTemplateData(Map<String, Object> data) {
        templateData.putAll(data);
    }
}
