/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Quick Start Example - Demonstrates how to use JHipster Client Generators.
 *
 * Run with: mvn exec:java -Dexec.mainClass="io.github.jhipster.client.examples.QuickStartExample"
 */
package io.github.jhipster.client.examples;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientConfig.EntityConfig;
import io.github.jhipster.client.config.ClientConfig.FieldConfig;
import io.github.jhipster.client.config.ClientConfig.RelationshipConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.generators.client.ClientGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Quick Start Example.
 * Generates a complete JHipster frontend application with entities.
 */
public class QuickStartExample {

    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("  JHipster Client Generator - Quick Start  ");
        System.out.println("===========================================");
        System.out.println();

        // Select framework
        String framework = args.length > 0 ? args[0] : "angular";
        System.out.println("Framework: " + framework);

        // Create output directory
        Path outputDir = Paths.get("target/quick-start-" + framework);
        Files.createDirectories(outputDir);
        System.out.println("Output: " + outputDir.toAbsolutePath());
        System.out.println();

        // Create configuration
        ClientConfig config = new ClientConfig();
        config.setBaseName("quickstart");
        config.setPackageName("com.mycompany.quickstart");
        config.setClientFramework(framework);
        config.setClientBundler(framework.equals("vue") ? "vite" : "webpack");
        config.setAuthenticationType("jwt");
        config.setEnableTranslation(true);
        config.setNativeLanguage("en");
        config.setLanguages(List.of("en", "fr"));
        config.setDevServerPort(9000);
        config.setTestFrameworks(List.of("cypress"));

        // Add entities
        config.setEntities(List.of(
            createProductEntity(),
            createCategoryEntity(),
            createOrderEntity()
        ));

        System.out.println("Configuration:");
        System.out.println("  - Application: " + config.getBaseName());
        System.out.println("  - Framework: " + config.getClientFramework());
        System.out.println("  - Bundler: " + config.getClientBundler());
        System.out.println("  - Authentication: " + config.getAuthenticationType());
        System.out.println("  - i18n: " + config.getEnableTranslation());
        System.out.println("  - Entities: " + config.getEntities().size());
        System.out.println();

        // Generate
        System.out.println("Generating...");
        long startTime = System.currentTimeMillis();

        ClientGeneratorContext context = new ClientGeneratorContext(outputDir, config);
        ClientGenerator generator = new ClientGenerator(context);
        generator.generate();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("Done in " + duration + "ms!");
        System.out.println();

        // Print next steps
        System.out.println("===========================================");
        System.out.println("Next steps:");
        System.out.println("===========================================");
        System.out.println();
        System.out.println("  1. cd " + outputDir.toAbsolutePath());
        System.out.println("  2. npm install");
        System.out.println("  3. npm start");
        System.out.println();
        System.out.println("Open http://localhost:9000 in your browser");
        System.out.println();

        // Count generated files
        long fileCount = Files.walk(outputDir)
            .filter(Files::isRegularFile)
            .count();
        System.out.println("Generated " + fileCount + " files");
    }

    private static EntityConfig createProductEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Product");
        entity.setEntityAngularName("Product");
        entity.setEntityReactName("Product");
        entity.setEntityFileName("product");
        entity.setEntityFolderName("product");
        entity.setReadOnly(false);

        entity.setFields(List.of(
            createField("name", "String", true),
            createField("description", "String", false),
            createField("price", "BigDecimal", true),
            createField("quantity", "Integer", true),
            createField("available", "Boolean", false),
            createField("createdDate", "Instant", false)
        ));

        return entity;
    }

    private static EntityConfig createCategoryEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Category");
        entity.setEntityAngularName("Category");
        entity.setEntityReactName("Category");
        entity.setEntityFileName("category");
        entity.setEntityFolderName("category");
        entity.setReadOnly(false);

        entity.setFields(List.of(
            createField("name", "String", true),
            createField("description", "String", false),
            createField("sortOrder", "Integer", false)
        ));

        return entity;
    }

    private static EntityConfig createOrderEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Order");
        entity.setEntityAngularName("Order");
        entity.setEntityReactName("Order");
        entity.setEntityFileName("order");
        entity.setEntityFolderName("order");
        entity.setReadOnly(false);

        entity.setFields(List.of(
            createField("orderDate", "Instant", true),
            createField("status", "String", true),
            createField("totalAmount", "BigDecimal", true),
            createField("shippingAddress", "String", false),
            createField("notes", "String", false)
        ));

        return entity;
    }

    private static FieldConfig createField(String name, String type, boolean required) {
        FieldConfig field = new FieldConfig();
        field.setFieldName(name);
        field.setFieldType(type);
        field.setRequired(required);
        return field;
    }
}
