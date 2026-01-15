/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Template engine for rendering templates.
 * Supports both Mustache templates and Java code generators.
 */
public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);

    private final MustacheFactory mustacheFactory;
    private Path templatesPath;

    public TemplateEngine() {
        this.mustacheFactory = new DefaultMustacheFactory();
    }

    public TemplateEngine(Path templatesPath) {
        this.templatesPath = templatesPath;
        this.mustacheFactory = new DefaultMustacheFactory(templatesPath.toFile());
    }

    /**
     * Renders a Mustache template with the given data.
     *
     * @param templateName the template name (relative to templates directory)
     * @param data the data to render
     * @return the rendered content
     */
    public String render(String templateName, Map<String, Object> data) {
        try {
            Mustache mustache = mustacheFactory.compile(templateName);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to render template {}: {}", templateName, e.getMessage());
            throw new TemplateException("Failed to render template: " + templateName, e);
        }
    }

    /**
     * Renders a template from a string.
     *
     * @param templateContent the template content
     * @param data the data to render
     * @return the rendered content
     */
    public String renderString(String templateContent, Map<String, Object> data) {
        try {
            Mustache mustache = mustacheFactory.compile(new StringReader(templateContent), "inline");
            StringWriter writer = new StringWriter();
            mustache.execute(writer, data);
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to render inline template: {}", e.getMessage());
            throw new TemplateException("Failed to render inline template", e);
        }
    }

    /**
     * Renders a template file to the destination.
     *
     * @param templateName the template name
     * @param destination the destination path
     * @param data the data to render
     */
    public void renderToFile(String templateName, Path destination, Map<String, Object> data) throws IOException {
        String content = render(templateName, data);
        Files.createDirectories(destination.getParent());
        Files.writeString(destination, content);
        log.debug("Rendered {} to {}", templateName, destination);
    }

    /**
     * Sets the templates path.
     */
    public void setTemplatesPath(Path templatesPath) {
        this.templatesPath = templatesPath;
    }

    /**
     * Template rendering exception.
     */
    public static class TemplateException extends RuntimeException {
        public TemplateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
