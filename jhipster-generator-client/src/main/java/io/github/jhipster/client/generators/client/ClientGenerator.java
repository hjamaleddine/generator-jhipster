/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.generators.client;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.core.BaseClientGenerator;
import io.github.jhipster.client.generators.angular.AngularGenerator;
import io.github.jhipster.client.generators.react.ReactGenerator;
import io.github.jhipster.client.generators.vue.VueGenerator;
import io.github.jhipster.client.generators.cypress.CypressGenerator;

import java.io.IOException;

/**
 * Main Client Generator - Orchestrator.
 * Entry point for JHipster frontend generation.
 * Equivalent to generators/client/generator.ts.
 */
public class ClientGenerator extends BaseClientGenerator {

    public ClientGenerator(ClientGeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "client";
    }

    @Override
    protected String getTemplateDir() {
        return "common";
    }

    @Override
    protected void registerTasks() {
        registerTask(GeneratorPhase.INITIALIZING, this::initializing);
        registerTask(GeneratorPhase.CONFIGURING, this::configuring);
        registerTask(GeneratorPhase.COMPOSING, this::composing);
        registerTask(GeneratorPhase.LOADING, this::loading);
        registerTask(GeneratorPhase.PREPARING, this::preparing);
        registerTask(GeneratorPhase.WRITING, this::writing);
        registerTask(GeneratorPhase.WRITING_ENTITIES, this::writingEntities);
        registerTask(GeneratorPhase.POST_WRITING, this::postWriting);
        registerTask(GeneratorPhase.END, this::end);
    }

    private void initializing() {
        log.info("Initializing Client Generator");
        log.info("Client framework: {}", getConfig().getClientFramework());
        log.info("Client bundler: {}", getConfig().getClientBundler());
        log.info("Client theme: {}", getConfig().getClientTheme());
    }

    private void configuring() {
        log.info("Configuring client application");

        ClientConfig config = getConfig();

        // Set default test framework based on bundler
        if (config.getClientTestFramework() == null) {
            if (config.isVite()) {
                config.setClientTestFramework("vitest");
            } else {
                config.setClientTestFramework("jest");
            }
        }

        // Calculate dev server port if not set
        if (config.getDevServerPort() == null) {
            config.setDevServerPort(9000);
        }
    }

    private void composing() {
        log.info("Composing with framework-specific generators");

        ClientConfig config = getConfig();

        // Compose with the appropriate framework generator
        switch (config.getClientFramework()) {
            case "angular":
                log.info("Composing with Angular generator");
                composeWith(new AngularGenerator(context));
                break;
            case "react":
                log.info("Composing with React generator");
                composeWith(new ReactGenerator(context));
                break;
            case "vue":
                log.info("Composing with Vue generator");
                composeWith(new VueGenerator(context));
                break;
            case "no":
                log.info("No client framework selected, skipping client generation");
                return;
            default:
                log.warn("Unknown client framework: {}", config.getClientFramework());
        }

        // Compose with Cypress if enabled
        if (config.hasCypress()) {
            log.info("Composing with Cypress generator");
            composeWith(new CypressGenerator(context));
        }
    }

    private void loading() {
        log.info("Loading client configuration");

        // Add common template data
        context.addTemplateData("jhipsterVersion", "8.0.0");
        context.addTemplateData("nodeVersion", "20.10.0");
        context.addTemplateData("npmVersion", "10.2.3");
    }

    private void preparing() {
        log.info("Preparing client generation context");

        // Prepare entity data for client generation
        for (ClientConfig.EntityConfig entity : getConfig().getEntities()) {
            prepareEntity(entity);
        }
    }

    private void prepareEntity(ClientConfig.EntityConfig entity) {
        // Set default names if not provided
        if (entity.getEntityAngularName() == null) {
            entity.setEntityAngularName(capitalize(entity.getName()));
        }
        if (entity.getEntityReactName() == null) {
            entity.setEntityReactName(capitalize(entity.getName()));
        }
        if (entity.getEntityFileName() == null) {
            entity.setEntityFileName(toKebabCase(entity.getName()));
        }
        if (entity.getEntityFolderName() == null) {
            entity.setEntityFolderName(toKebabCase(entity.getName()));
        }
        if (entity.getEntityModelFileName() == null) {
            entity.setEntityModelFileName(uncapitalize(entity.getName()));
        }
    }

    private void writing() {
        log.info("Writing common client files");

        try {
            // Write manifest.webapp
            writeManifest();

            // Write common assets
            writeCommonAssets();

            // Write loading CSS
            writeLoadingCss();

            // Write web.xml for servlet containers
            writeWebXml();

            // Write swagger-ui index
            writeSwaggerUi();

        } catch (IOException e) {
            throw new RuntimeException("Error writing common client files", e);
        }
    }

    private void writeManifest() throws IOException {
        String manifest = """
            {
              "name": "%s",
              "short_name": "%s",
              "icons": [
                {
                  "src": "./content/images/jhipster_family_member_%d.svg",
                  "sizes": "any",
                  "type": "image/svg+xml"
                }
              ],
              "theme_color": "#000000",
              "background_color": "#e0e0e0",
              "start_url": "./index.html",
              "display": "standalone"
            }
            """.formatted(
                getConfig().getBaseName(),
                getConfig().getBaseName(),
                (int)(Math.random() * 4)
        );
        writeFile(getConfig().getClientSrcDir() + "manifest.webapp", manifest);
    }

    private void writeCommonAssets() throws IOException {
        // Create content directories
        context.ensureDirectory(context.webappPath("content/images"));
        context.ensureDirectory(context.webappPath("content/css"));

        // Write favicon (placeholder)
        String favicon = "<!-- JHipster favicon placeholder -->";
        writeFile(getConfig().getClientSrcDir() + "favicon.ico", favicon);
    }

    private void writeLoadingCss() throws IOException {
        String loadingCss = """
            /* Loading animation for JHipster application */
            .app-loading {
              position: relative;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              height: 100vh;
            }

            .app-loading .logo {
              width: 100px;
              height: 100px;
              animation: spin 2s linear infinite;
            }

            .app-loading .spinner {
              width: 60px;
              height: 60px;
              animation: spin 1.5s linear infinite;
              border: 5px solid #3498db;
              border-top: 5px solid transparent;
              border-radius: 50%;
            }

            @keyframes spin {
              0% { transform: rotate(0deg); }
              100% { transform: rotate(360deg); }
            }

            .app-loading .loading-text {
              margin-top: 20px;
              font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
              font-size: 16px;
              color: #555;
            }
            """;
        writeFile(getConfig().getClientSrcDir() + "content/css/loading.css", loadingCss);
    }

    private void writeWebXml() throws IOException {
        String webXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                                         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
                     version="4.0"
                     metadata-complete="true">
                <display-name>%s</display-name>
                <distributable/>
            </web-app>
            """.formatted(getConfig().getBaseName());
        writeFile(getConfig().getClientSrcDir() + "WEB-INF/web.xml", webXml);
    }

    private void writeSwaggerUi() throws IOException {
        String swaggerIndex = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>%s - Swagger UI</title>
                <link rel="stylesheet" type="text/css" href="./swagger-ui.css">
                <style>
                    html { box-sizing: border-box; overflow-y: scroll; }
                    *, *:before, *:after { box-sizing: inherit; }
                    body { margin: 0; background: #fafafa; }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="./swagger-ui-bundle.js" charset="UTF-8"></script>
                <script src="./swagger-ui-standalone-preset.js" charset="UTF-8"></script>
                <script>
                    window.onload = function() {
                        const ui = SwaggerUIBundle({
                            url: "/v3/api-docs",
                            dom_id: '#swagger-ui',
                            deepLinking: true,
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIStandalonePreset
                            ],
                            plugins: [
                                SwaggerUIBundle.plugins.DownloadUrl
                            ],
                            layout: "StandaloneLayout"
                        });
                        window.ui = ui;
                    };
                </script>
            </body>
            </html>
            """.formatted(getConfig().getBaseName());
        writeFile(getConfig().getClientSrcDir() + "swagger-ui/index.html", swaggerIndex);
    }

    private void writingEntities() {
        log.info("Writing entity enumerations");

        // Generate enum files for entity fields
        for (ClientConfig.EntityConfig entity : getConfig().getEntities()) {
            if (entity.getBuiltIn() || entity.getEmbedded()) {
                continue;
            }

            for (ClientConfig.FieldConfig field : entity.getFields()) {
                if (field.getFieldIsEnum()) {
                    try {
                        writeEnumFile(field);
                    } catch (IOException e) {
                        log.error("Error writing enum file for field: {}", field.getFieldName(), e);
                    }
                }
            }
        }
    }

    private void writeEnumFile(ClientConfig.FieldConfig field) throws IOException {
        String enumName = field.getFieldType();
        String enumValues = field.getFieldValues();

        if (enumValues == null || enumValues.isEmpty()) {
            return;
        }

        StringBuilder content = new StringBuilder();
        content.append("export enum ").append(enumName).append(" {\n");

        String[] values = enumValues.split(",");
        for (int i = 0; i < values.length; i++) {
            String value = values[i].trim();
            content.append("  ").append(value).append(" = '").append(value).append("'");
            if (i < values.length - 1) {
                content.append(",");
            }
            content.append("\n");
        }
        content.append("}\n");

        String enumPath = getConfig().getEnumerationsDir() + toKebabCase(enumName) + ".model.ts";
        writeFile(enumPath, content.toString());
    }

    private void postWriting() {
        log.info("Post-writing client configuration");

        // Generate package.json scripts and dependencies are handled by framework-specific generators
    }

    private void end() {
        log.info("Client generation completed!");
        log.info("");
        log.info("==========================================================");
        log.info("Client '{}' generated successfully!", getConfig().getBaseName());
        log.info("Framework: {}", getConfig().getClientFramework());
        log.info("Bundler: {}", getConfig().getClientBundler());
        log.info("==========================================================");
        log.info("");
        log.info("To start the application:");
        log.info("  npm start");
        log.info("");
    }
}
