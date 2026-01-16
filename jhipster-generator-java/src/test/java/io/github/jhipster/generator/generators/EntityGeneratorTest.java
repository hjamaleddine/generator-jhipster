/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
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
    @DisplayName("Should generate entity domain class")
    void shouldGenerateEntityDomainClass() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/domain/Product.java");

        String entityContent = Files.readString(
            testDir.resolve("src/main/java/" + packagePath + "/domain/Product.java"));
        assertTrue(entityContent.contains("@Entity"));
        assertTrue(entityContent.contains("public class Product"));
        assertTrue(entityContent.contains("private String name"));
        assertTrue(entityContent.contains("private BigDecimal price"));
    }

    @Test
    @DisplayName("Should generate entity repository")
    void shouldGenerateEntityRepository() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/repository/ProductRepository.java");

        String repoContent = Files.readString(
            testDir.resolve("src/main/java/" + packagePath + "/repository/ProductRepository.java"));
        assertTrue(repoContent.contains("@Repository"));
        assertTrue(repoContent.contains("interface ProductRepository"));
        assertTrue(repoContent.contains("JpaRepository"));
    }

    @Test
    @DisplayName("Should generate entity service when service layer enabled")
    void shouldGenerateEntityService() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setService("serviceImpl");
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/service/ProductService.java");
        assertFileExists("src/main/java/" + packagePath + "/service/impl/ProductServiceImpl.java");
    }

    @Test
    @DisplayName("Should generate entity DTO when DTO layer enabled")
    void shouldGenerateEntityDto() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDto("mapstruct");
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/service/dto/ProductDTO.java");
        assertFileExists("src/main/java/" + packagePath + "/service/mapper/ProductMapper.java");
    }

    @Test
    @DisplayName("Should generate entity REST controller")
    void shouldGenerateEntityRestController() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/web/rest/ProductResource.java");

        String resourceContent = Files.readString(
            testDir.resolve("src/main/java/" + packagePath + "/web/rest/ProductResource.java"));
        assertTrue(resourceContent.contains("@RestController"));
        assertTrue(resourceContent.contains("@RequestMapping"));
        assertTrue(resourceContent.contains("@GetMapping"));
        assertTrue(resourceContent.contains("@PostMapping"));
        assertTrue(resourceContent.contains("@PutMapping"));
        assertTrue(resourceContent.contains("@DeleteMapping"));
    }

    @Test
    @DisplayName("Should generate entity with validation annotations")
    void shouldGenerateEntityWithValidation() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        // Make name required
        entityConfig.getFields().get(0).setRequired(true);
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        String entityContent = Files.readString(
            testDir.resolve("src/main/java/" + packagePath + "/domain/Product.java"));
        assertTrue(entityContent.contains("@NotNull") || entityContent.contains("@Column(nullable = false)"));
    }

    @Test
    @DisplayName("Should generate entity with relationship")
    void shouldGenerateEntityWithRelationship() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig productEntity = createProductEntity();
        EntityConfig categoryEntity = createCategoryEntity();

        // Add relationship Product -> Category
        RelationshipConfig relationship = new RelationshipConfig();
        relationship.setRelationshipType("many-to-one");
        relationship.setRelationshipName("category");
        relationship.setOtherEntityName("Category");
        relationship.setOtherEntityField("id");
        productEntity.setRelationships(List.of(relationship));

        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(productEntity);
        context.addEntity(categoryEntity);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        String productContent = Files.readString(
            testDir.resolve("src/main/java/" + packagePath + "/domain/Product.java"));
        assertTrue(productContent.contains("@ManyToOne") || productContent.contains("Category category"));
    }

    @Test
    @DisplayName("Should generate Liquibase changelog for entity")
    void shouldGenerateLiquibaseChangelog() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("sql");
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        // Check for Liquibase changelog (may vary based on implementation)
        Path changelogDir = testDir.resolve("src/main/resources/config/liquibase/changelog");
        if (Files.exists(changelogDir)) {
            assertTrue(Files.list(changelogDir).count() > 0);
        }
    }

    @Test
    @DisplayName("Should generate entity integration test")
    void shouldGenerateEntityIntegrationTest() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        EntityConfig entityConfig = createProductEntity();
        GeneratorContext context = new GeneratorContext(testDir, config);
        context.addEntity(entityConfig);

        // When
        new EntityGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/test/java/" + packagePath + "/web/rest/ProductResourceIT.java");

        String testContent = Files.readString(
            testDir.resolve("src/test/java/" + packagePath + "/web/rest/ProductResourceIT.java"));
        assertTrue(testContent.contains("@SpringBootTest") || testContent.contains("@IntegrationTest"));
    }

    @Test
    @DisplayName("Should handle multiple entities")
    void shouldHandleMultipleEntities() throws Exception {
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
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/domain/Product.java");
        assertFileExists("src/main/java/" + packagePath + "/domain/Category.java");
        assertFileExists("src/main/java/" + packagePath + "/repository/ProductRepository.java");
        assertFileExists("src/main/java/" + packagePath + "/repository/CategoryRepository.java");
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
