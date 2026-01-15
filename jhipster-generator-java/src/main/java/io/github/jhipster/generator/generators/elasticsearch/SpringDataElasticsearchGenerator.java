/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.elasticsearch;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

import java.util.List;

/**
 * Spring Data Elasticsearch Generator.
 * Generates Elasticsearch search configuration and search repositories.
 */
public class SpringDataElasticsearchGenerator extends BaseApplicationGenerator {

    public SpringDataElasticsearchGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-data-elasticsearch";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingElasticsearch", this::writing);
    }

    private void writing() throws Exception {
        JHipsterConfig config = getConfig();

        if (!"elasticsearch".equals(config.getSearchEngine())) {
            log.info("Elasticsearch not configured, skipping");
            return;
        }

        log.info("Writing Elasticsearch configuration");

        writeElasticsearchConfiguration();
        writeElasticsearchIndexService();

        // Generate search repositories for entities
        List<EntityConfig> entities = context.getEntities();
        for (EntityConfig entity : entities) {
            if (!entity.isBuiltIn()) {
                writeSearchRepository(entity);
            }
        }

        writeElasticsearchYaml();
    }

    private void writeElasticsearchConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories"
        );

        builder.javadoc("Elasticsearch configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableElasticsearchRepositories", "\"" + config.getPackageName() + ".repository.search\"");

        builder.classDeclaration("public", "ElasticsearchConfiguration", null);
        builder.closeClass();

        writeFile(getMainJavaPath() + "config/ElasticsearchConfiguration.java", builder.build());
    }

    private void writeElasticsearchIndexService() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service");

        builder.addImports(
            "org.elasticsearch.client.RestHighLevelClient",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.scheduling.annotation.Async",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional"
        );

        builder.javadoc("Service for managing Elasticsearch indexes.");
        builder.annotation("Service");
        builder.annotation("Transactional", "readOnly = true");

        builder.classDeclaration("public", "ElasticsearchIndexService", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(ElasticsearchIndexService.class)");
        builder.line();

        builder.javadoc("Reindex all entities.");
        builder.annotation("Async");
        builder.methodSignature("public", "void", "reindexAll");
        builder.statement("log.info(\"Starting reindex of all entities\")");
        builder.lineComment("Add reindex logic for each entity type");
        builder.statement("log.info(\"Completed reindex of all entities\")");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/ElasticsearchIndexService.java", builder.build());
    }

    private void writeSearchRepository(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository.search");

        builder.addImports(
            config.getPackageName() + ".domain." + entity.getName(),
            "org.springframework.data.elasticsearch.repository.ElasticsearchRepository"
        );

        builder.javadoc("Spring Data Elasticsearch repository for the {@link " + entity.getName() + "} entity.");

        builder.line("public interface " + entity.getName() + "SearchRepository extends ElasticsearchRepository<" + entity.getName() + ", Long> {");
        builder.line("}");

        writeFile(getMainJavaPath() + "repository/search/" + entity.getName() + "SearchRepository.java", builder.build());
    }

    private void writeElasticsearchYaml() throws Exception {
        StringBuilder yml = new StringBuilder();
        yml.append("# ===================================================================\n");
        yml.append("# Elasticsearch configuration\n");
        yml.append("# ===================================================================\n\n");

        yml.append("spring:\n");
        yml.append("  elasticsearch:\n");
        yml.append("    uris: http://localhost:9200\n");

        writeFile(getMainResourcesPath() + "config/elasticsearch.yml", yml.toString());
    }

    private String getMainResourcesPath() {
        return context.getBasePath().toString() + "/src/main/resources/";
    }
}
