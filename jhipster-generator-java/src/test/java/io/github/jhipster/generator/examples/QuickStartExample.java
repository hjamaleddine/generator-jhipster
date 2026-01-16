/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Quick Start Example - Demonstrates how to use JHipster Java Generators.
 *
 * Run with: mvn exec:java -Dexec.mainClass="io.github.jhipster.generator.examples.QuickStartExample"
 */
package io.github.jhipster.generator.examples;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.app.AppGenerator;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Quick Start Example.
 * Generates a complete JHipster microservice application with entities.
 */
public class QuickStartExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=============================================");
        System.out.println("  JHipster Java Generator - Quick Start");
        System.out.println("=============================================");
        System.out.println();

        // Select application type from args
        String appType = args.length > 0 ? args[0] : "microservice";
        System.out.println("Application type: " + appType);

        // Create output directory
        Path outputDir = Paths.get("target/quick-start-" + appType);
        Files.createDirectories(outputDir);
        System.out.println("Output directory: " + outputDir.toAbsolutePath());
        System.out.println();

        // Create configuration
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("productservice");
        config.setPackageName("com.mycompany.product");
        config.setApplicationType(appType);
        config.setDatabaseType("sql");
        config.setDevDatabaseType("h2Disk");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        config.setCacheProvider("ehcache");
        config.setEnableHibernateCache(true);
        config.setServerPort(8081);
        config.setDto("mapstruct");
        config.setService("serviceImpl");
        config.setSkipClient(true);

        System.out.println("Configuration:");
        System.out.println("  - Application: " + config.getBaseName());
        System.out.println("  - Package: " + config.getPackageName());
        System.out.println("  - Type: " + config.getApplicationType());
        System.out.println("  - Database: " + config.getProdDatabaseType());
        System.out.println("  - Authentication: " + config.getAuthenticationType());
        System.out.println("  - Service Discovery: " + config.getServiceDiscoveryType());
        System.out.println("  - Build Tool: " + config.getBuildTool());
        System.out.println("  - Port: " + config.getServerPort());
        System.out.println();

        // Create context
        GeneratorContext context = new GeneratorContext(outputDir, config);

        // Add entities
        System.out.println("Adding entities...");
        context.addEntity(createProductEntity());
        context.addEntity(createCategoryEntity());
        context.addEntity(createOrderEntity());
        System.out.println("  - Product");
        System.out.println("  - Category");
        System.out.println("  - Order");
        System.out.println();

        // Generate
        System.out.println("Generating application...");
        long startTime = System.currentTimeMillis();

        AppGenerator generator = new AppGenerator(context);
        generator.run();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("Generation completed in " + duration + "ms!");
        System.out.println();

        // Print next steps
        System.out.println("=============================================");
        System.out.println("Next steps:");
        System.out.println("=============================================");
        System.out.println();
        System.out.println("  1. cd " + outputDir.toAbsolutePath());
        System.out.println("  2. Start dependencies:");
        System.out.println("     docker compose -f src/main/docker/postgresql.yml up -d");
        System.out.println("     docker compose -f src/main/docker/consul.yml up -d");
        System.out.println("  3. Run the application:");
        System.out.println("     ./mvnw spring-boot:run");
        System.out.println();
        System.out.println("API will be available at http://localhost:" + config.getServerPort());
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
        entity.setEntityTableName("product");

        entity.setFields(Arrays.asList(
            createField("name", "String", true),
            createField("description", "String", false),
            createField("price", "BigDecimal", true),
            createField("quantity", "Integer", true),
            createField("available", "Boolean", false),
            createField("createdDate", "Instant", false)
        ));

        // Add relationship to Category
        RelationshipConfig categoryRel = new RelationshipConfig();
        categoryRel.setRelationshipType("many-to-one");
        categoryRel.setRelationshipName("category");
        categoryRel.setOtherEntityName("Category");
        categoryRel.setOtherEntityField("id");
        entity.setRelationships(List.of(categoryRel));

        return entity;
    }

    private static EntityConfig createCategoryEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Category");
        entity.setEntityTableName("category");

        entity.setFields(Arrays.asList(
            createField("name", "String", true),
            createField("description", "String", false),
            createField("sortOrder", "Integer", false)
        ));

        return entity;
    }

    private static EntityConfig createOrderEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Order");
        entity.setEntityTableName("jhi_order");

        entity.setFields(Arrays.asList(
            createField("orderDate", "Instant", true),
            createField("status", "String", true),
            createField("totalAmount", "BigDecimal", true),
            createField("shippingAddress", "String", false),
            createField("notes", "String", false)
        ));

        // Add relationship to Product
        RelationshipConfig productRel = new RelationshipConfig();
        productRel.setRelationshipType("many-to-many");
        productRel.setRelationshipName("products");
        productRel.setOtherEntityName("Product");
        productRel.setOtherEntityField("name");
        entity.setRelationships(List.of(productRel));

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
