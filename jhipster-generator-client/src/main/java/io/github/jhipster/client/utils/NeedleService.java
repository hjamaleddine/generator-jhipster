/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for handling needle-based file modifications.
 * Needles are comment markers that allow non-destructive code injection.
 */
public class NeedleService {

    private static final Logger log = LoggerFactory.getLogger(NeedleService.class);

    // Common needle patterns
    public static final String NEEDLE_ADD_ENTITY_ROUTE = "jhipster-needle-add-entity-route";
    public static final String NEEDLE_ADD_ENTITY_TO_MENU = "jhipster-needle-add-entity-to-menu";
    public static final String NEEDLE_ADD_ICON_IMPORT = "jhipster-needle-add-icon-import";
    public static final String NEEDLE_ADD_ELEMENT_TO_MENU = "jhipster-needle-add-element-to-menu";
    public static final String NEEDLE_ADD_ADMIN_ROUTE = "jhipster-needle-add-admin-route";
    public static final String NEEDLE_ADD_ADMIN_MODULE_IMPORT = "jhipster-needle-add-admin-module-import";
    public static final String NEEDLE_ADD_REDUCER_IMPORT = "jhipster-needle-add-reducer-import";
    public static final String NEEDLE_ADD_REDUCER_COMBINE = "jhipster-needle-add-reducer-combine";
    public static final String NEEDLE_ADD_ROUTE_IMPORT = "jhipster-needle-add-route-import";
    public static final String NEEDLE_ADD_ROUTE_PATH = "jhipster-needle-add-route-path";
    public static final String NEEDLE_ADD_WEBPACK_CONFIG = "jhipster-needle-add-webpack-config";
    public static final String NEEDLE_ADD_SCSS_STYLE = "jhipster-needle-add-scss-style";
    public static final String NEEDLE_ENTITY_ADD_TO_ROUTER = "jhipster-needle-add-entity-to-router";
    public static final String NEEDLE_ENTITY_ADD_SERVICE = "jhipster-needle-add-entity-service";

    private final Path basePath;

    public NeedleService(Path basePath) {
        this.basePath = basePath;
    }

    /**
     * Add content before a needle marker.
     *
     * @param filePath Relative path to the file
     * @param needle The needle marker to find
     * @param content The content to add
     * @return true if the content was added successfully
     */
    public boolean addBeforeNeedle(String filePath, String needle, String content) throws IOException {
        Path path = basePath.resolve(filePath);
        if (!Files.exists(path)) {
            log.warn("File not found for needle injection: {}", path);
            return false;
        }

        String fileContent = Files.readString(path);

        // Check if content already exists (avoid duplicates)
        if (fileContent.contains(content.trim())) {
            log.debug("Content already exists in {}, skipping", filePath);
            return false;
        }

        // Find needle and add content before it
        String needlePattern = ".*" + Pattern.quote(needle) + ".*";
        Pattern pattern = Pattern.compile(needlePattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileContent);

        if (matcher.find()) {
            String needleLine = matcher.group();
            String indentation = getIndentation(needleLine);
            String indentedContent = indentContent(content, indentation);

            String newContent = fileContent.replace(needleLine, indentedContent + "\n" + needleLine);
            Files.writeString(path, newContent);
            log.info("Added content before needle '{}' in {}", needle, filePath);
            return true;
        } else {
            log.warn("Needle '{}' not found in {}", needle, filePath);
            return false;
        }
    }

    /**
     * Add content after a needle marker.
     */
    public boolean addAfterNeedle(String filePath, String needle, String content) throws IOException {
        Path path = basePath.resolve(filePath);
        if (!Files.exists(path)) {
            log.warn("File not found for needle injection: {}", path);
            return false;
        }

        String fileContent = Files.readString(path);

        if (fileContent.contains(content.trim())) {
            log.debug("Content already exists in {}, skipping", filePath);
            return false;
        }

        String needlePattern = ".*" + Pattern.quote(needle) + ".*";
        Pattern pattern = Pattern.compile(needlePattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileContent);

        if (matcher.find()) {
            String needleLine = matcher.group();
            String indentation = getIndentation(needleLine);
            String indentedContent = indentContent(content, indentation);

            String newContent = fileContent.replace(needleLine, needleLine + "\n" + indentedContent);
            Files.writeString(path, newContent);
            log.info("Added content after needle '{}' in {}", needle, filePath);
            return true;
        } else {
            log.warn("Needle '{}' not found in {}", needle, filePath);
            return false;
        }
    }

    /**
     * Replace the needle line with content (keeping the needle as a comment).
     */
    public boolean replaceNeedleLine(String filePath, String needle, String content) throws IOException {
        Path path = basePath.resolve(filePath);
        if (!Files.exists(path)) {
            log.warn("File not found: {}", path);
            return false;
        }

        String fileContent = Files.readString(path);

        if (fileContent.contains(content.trim())) {
            log.debug("Content already exists in {}, skipping", filePath);
            return false;
        }

        String needlePattern = "(.*)(" + Pattern.quote(needle) + ".*)";
        Pattern pattern = Pattern.compile(needlePattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileContent);

        if (matcher.find()) {
            String prefix = matcher.group(1);
            String needleComment = matcher.group(2);
            String indentation = prefix.replaceAll("\\S.*", "");
            String indentedContent = indentContent(content, indentation);

            String replacement = indentedContent + "\n" + prefix + needleComment;
            String newContent = fileContent.replaceFirst(Pattern.quote(matcher.group()), replacement);
            Files.writeString(path, newContent);
            log.info("Replaced needle '{}' in {}", needle, filePath);
            return true;
        }

        log.warn("Needle '{}' not found in {}", needle, filePath);
        return false;
    }

    /**
     * Check if a needle exists in a file.
     */
    public boolean hasNeedle(String filePath, String needle) throws IOException {
        Path path = basePath.resolve(filePath);
        if (!Files.exists(path)) {
            return false;
        }
        String content = Files.readString(path);
        return content.contains(needle);
    }

    /**
     * Get the indentation of a line.
     */
    private String getIndentation(String line) {
        StringBuilder indent = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indent.append(c);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    /**
     * Indent content with a given indentation.
     */
    private String indentContent(String content, String indentation) {
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                result.append("\n");
            }
            result.append(indentation).append(lines[i]);
        }
        return result.toString();
    }

    // Convenience methods for common operations

    /**
     * Add an entity route to Angular routes file.
     */
    public boolean addAngularEntityRoute(String entityRoute) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/entities/entity-routing.module.ts",
            NEEDLE_ADD_ENTITY_ROUTE,
            entityRoute
        );
    }

    /**
     * Add an entity to the Angular menu.
     */
    public boolean addAngularEntityToMenu(String menuItem) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/layouts/navbar/navbar.component.html",
            NEEDLE_ADD_ENTITY_TO_MENU,
            menuItem
        );
    }

    /**
     * Add a React reducer import.
     */
    public boolean addReactReducerImport(String importStatement) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/shared/reducers/index.ts",
            NEEDLE_ADD_REDUCER_IMPORT,
            importStatement
        );
    }

    /**
     * Add a React reducer to combine.
     */
    public boolean addReactReducerCombine(String reducer) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/shared/reducers/index.ts",
            NEEDLE_ADD_REDUCER_COMBINE,
            reducer
        );
    }

    /**
     * Add a React route import.
     */
    public boolean addReactRouteImport(String importStatement) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/entities/routes.tsx",
            NEEDLE_ADD_ROUTE_IMPORT,
            importStatement
        );
    }

    /**
     * Add a React route path.
     */
    public boolean addReactRoutePath(String route) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/entities/routes.tsx",
            NEEDLE_ADD_ROUTE_PATH,
            route
        );
    }

    /**
     * Add a Vue entity to router.
     */
    public boolean addVueEntityToRouter(String route) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/router/entities.ts",
            NEEDLE_ENTITY_ADD_TO_ROUTER,
            route
        );
    }

    /**
     * Add SCSS style.
     */
    public boolean addScssStyle(String filePath, String style) throws IOException {
        return addBeforeNeedle(filePath, NEEDLE_ADD_SCSS_STYLE, style);
    }

    /**
     * Add webpack configuration.
     */
    public boolean addWebpackConfig(String config) throws IOException {
        return addBeforeNeedle(
            "webpack/webpack.common.js",
            NEEDLE_ADD_WEBPACK_CONFIG,
            config
        );
    }

    /**
     * Add Font Awesome icon import.
     */
    public boolean addIconImport(String iconImport) throws IOException {
        return addBeforeNeedle(
            "src/main/webapp/app/config/icon-loader.ts",
            NEEDLE_ADD_ICON_IMPORT,
            iconImport
        );
    }
}
