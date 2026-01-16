/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.app.AppGenerator;
import io.github.jhipster.generator.generators.entity.EntityGenerator;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Entity Generator.
 * Note: EntityGenerator saves entity config to .jhipster/ directory.
 * Full code generation (domain, repository, service, etc.) requires AppGenerator.
 */
class EntityGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("entity-generator-test");
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                .filter(p -> !p.equals(testDir))
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }

    @Test
    @DisplayName("Should save entity configuration to .jhipster directory")
    void shouldSaveEntityConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        assertFileExists(".jhipster/Product.json");

        String jsonContent = Files.readString(testDir.resolve(".jhipster/Product.json"));
        assertTrue(jsonContent.contains("\"name\": \"Product\""));
        assertTrue(jsonContent.contains("\"entityTableName\": \"product\""));
        assertTrue(jsonContent.contains("\"fieldName\": \"name\""));
        assertTrue(jsonContent.contains("\"fieldName\": \"price\""));
    }

    @Test
    @DisplayName("Should save multiple entity configurations")
    void shouldSaveMultipleEntityConfigurations() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig productEntity = createProductEntity();
        EntityConfig categoryEntity = createCategoryEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(productEntity);
        context.addEntity(categoryEntity);

        // When
        new EntityGenerator(context).run();

        // Then
        assertFileExists(".jhipster/Product.json");
        assertFileExists(".jhipster/Category.json");
    }

    @Test
    @DisplayName("Should include field validation rules in entity config")
    void shouldIncludeFieldValidationRules() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        // Fields are already set with required=true
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String jsonContent = Files.readString(testDir.resolve(".jhipster/Product.json"));
        assertTrue(jsonContent.contains("\"fieldValidateRules\"") ||
                   jsonContent.contains("required"));
    }

    @Test
    @DisplayName("Should include relationships in entity config")
    void shouldIncludeRelationshipsInEntityConfig() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig productEntity = createProductEntity();

        // Add relationship Product -> Category
        RelationshipConfig relationship = new RelationshipConfig();
        relationship.setRelationshipType("many-to-one");
        relationship.setRelationshipName("category");
        relationship.setOtherEntityName("Category");
        relationship.setOtherEntityField("id");
        productEntity.setRelationships(List.of(relationship));

        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(productEntity);

        // When
        new EntityGenerator(context).run();

        // Then
        String jsonContent = Files.readString(testDir.resolve(".jhipster/Product.json"));
        assertTrue(jsonContent.contains("\"relationshipName\": \"category\""));
        assertTrue(jsonContent.contains("\"otherEntityName\": \"Category\""));
        assertTrue(jsonContent.contains("\"relationshipType\": \"many-to-one\""));
    }

    @Test
    @DisplayName("EntityGenerator with single entity constructor should work")
    void entityGeneratorWithSingleEntityConstructor() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When - using the two-parameter constructor
        new EntityGenerator(context, entityConfig).run();

        // Then
        assertFileExists(".jhipster/Product.json");
    }

    @Test
    @DisplayName("Should handle entity with no relationships")
    void shouldHandleEntityWithNoRelationships() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createCategoryEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        assertFileExists(".jhipster/Category.json");
        String jsonContent = Files.readString(testDir.resolve(".jhipster/Category.json"));
        assertTrue(jsonContent.contains("\"relationships\": ["));
    }

    @Test
    @DisplayName("Should include DTO and service settings")
    void shouldIncludeDtoAndServiceSettings() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        entityConfig.setDto("mapstruct");
        entityConfig.setService("serviceImpl");
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String jsonContent = Files.readString(testDir.resolve(".jhipster/Product.json"));
        assertTrue(jsonContent.contains("\"dto\":") || jsonContent.contains("mapstruct"));
        assertTrue(jsonContent.contains("\"service\":") || jsonContent.contains("serviceClass"));
    }

    // Helper methods

    private JHipsterConfig createConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("entitytest");
        config.setPackageName("com.test.entity");
        config.setApplicationType("microservice");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setBuildTool("maven");
        config.setDto("mapstruct");
        config.setService("serviceImpl");
        return config;
    }

    private EntityConfig createProductEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Product");
        entity.setEntityTableName("product");

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");
        nameField.setRequired(true);

        FieldConfig priceField = new FieldConfig();
        priceField.setFieldName("price");
        priceField.setFieldType("BigDecimal");
        priceField.setRequired(true);

        FieldConfig descriptionField = new FieldConfig();
        descriptionField.setFieldName("description");
        descriptionField.setFieldType("String");
        descriptionField.setRequired(false);

        entity.setFields(Arrays.asList(nameField, priceField, descriptionField));
        return entity;
    }

    private EntityConfig createCategoryEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Category");
        entity.setEntityTableName("category");

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");
        nameField.setRequired(true);

        FieldConfig descField = new FieldConfig();
        descField.setFieldName("description");
        descField.setFieldType("String");
        descField.setRequired(false);

        entity.setFields(Arrays.asList(nameField, descField));
        return entity;
    }

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
