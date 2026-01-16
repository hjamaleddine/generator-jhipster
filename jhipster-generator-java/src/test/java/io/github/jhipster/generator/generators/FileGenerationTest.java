/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.server.ServerGenerator;
import io.github.jhipster.generator.generators.domain.DomainGenerator;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that generators actually produce output files.
 */
class FileGenerationTest {

    private static final Path TARGET_DIR = Paths.get("target/generated-test-sources");
    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        // Use target directory so files persist after test
        testDir = TARGET_DIR.resolve("test-" + System.currentTimeMillis());
        Files.createDirectories(testDir);
        System.out.println("===========================================");
        System.out.println("Test output directory: " + testDir.toAbsolutePath());
        System.out.println("===========================================");
    }

    @AfterEach
    void tearDown() throws IOException {
        System.out.println("\n=== Generated files ===");
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .filter(Files::isRegularFile)
                .forEach(path -> System.out.println("  " + testDir.relativize(path)));
        }
        System.out.println("\nFiles kept at: " + testDir.toAbsolutePath());
        System.out.println("===========================================\n");
        // Don't delete - keep files for inspection
    }

    @Test
    @DisplayName("Should generate Spring Boot application files")
    void shouldGenerateSpringBootApplicationFiles() throws Exception {
        // Given
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("myapp");
        config.setPackageName("com.example.myapp");
        config.setApplicationType("monolith");
        config.setDatabaseType("sql");
        config.setDevDatabaseType("h2Memory");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setBuildTool("maven");
        config.setServerPort(8080);

        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then - verify key files were generated
        Path mainJavaPath = testDir.resolve("src/main/java/com/example/myapp");

        // Main application class
        Path appClass = mainJavaPath.resolve("MyappApp.java");
        assertTrue(Files.exists(appClass), "Main application class should exist: " + appClass);

        String appContent = Files.readString(appClass);
        assertTrue(appContent.contains("@SpringBootApplication"), "Should have @SpringBootApplication");
        assertTrue(appContent.contains("public class MyappApp"), "Should have correct class name");

        // pom.xml
        Path pomFile = testDir.resolve("pom.xml");
        assertTrue(Files.exists(pomFile), "pom.xml should exist");
    }

    @Test
    @DisplayName("Should generate entity domain class with DomainGenerator directly")
    void shouldGenerateEntityDomainClassDirectly() throws Exception {
        // Given
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("entityapp");
        config.setPackageName("com.example.entity");
        config.setApplicationType("monolith");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");

        GeneratorContext context = new GeneratorContext(testDir, config);

        // Add an entity
        EntityConfig product = new EntityConfig();
        product.setName("Product");
        product.setEntityTableName("product");

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");
        nameField.setFieldValidateRules(new String[]{"required"});

        FieldConfig priceField = new FieldConfig();
        priceField.setFieldName("price");
        priceField.setFieldType("BigDecimal");

        product.setFields(Arrays.asList(nameField, priceField));
        context.addEntity(product);

        // Debug: Print entities in context
        System.out.println("Entities in context: " + context.getEntities().size());
        for (EntityConfig e : context.getEntities()) {
            System.out.println("  - " + e.getName());
        }

        // When - Run DomainGenerator directly
        DomainGenerator domainGenerator = new DomainGenerator(context);
        domainGenerator.run();

        // Then - verify entity was generated
        Path domainPath = testDir.resolve("src/main/java/com/example/entity/domain");
        Path entityFile = domainPath.resolve("Product.java");

        System.out.println("Expected entity file: " + entityFile);
        System.out.println("Entity file exists: " + Files.exists(entityFile));

        assertTrue(Files.exists(entityFile), "Entity class should exist: " + entityFile);

        String entityContent = Files.readString(entityFile);
        assertTrue(entityContent.contains("@Entity"), "Should have @Entity annotation");
        assertTrue(entityContent.contains("public class Product"), "Should have correct class name");
    }

    @Test
    @DisplayName("Should generate entity via ServerGenerator composition")
    void shouldGenerateEntityViaServerGenerator() throws Exception {
        // Given
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("fullapp");
        config.setPackageName("com.example.full");
        config.setApplicationType("monolith");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");

        GeneratorContext context = new GeneratorContext(testDir, config);

        // Add an entity with full configuration for prod-ready generation
        EntityConfig product = new EntityConfig();
        product.setName("Product");
        product.setEntityTableName("product");
        product.setDto("mapstruct");           // Enable DTO generation
        product.setService("serviceImpl");     // Enable Service + ServiceImpl generation
        product.setPagination("pagination");   // Enable pagination
        product.setJpaMetamodelFiltering(true); // Enable filtering/criteria

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");
        nameField.setFieldValidateRules(new String[]{"required"});

        FieldConfig priceField = new FieldConfig();
        priceField.setFieldName("price");
        priceField.setFieldType("BigDecimal");

        FieldConfig descField = new FieldConfig();
        descField.setFieldName("description");
        descField.setFieldType("String");

        product.setFields(Arrays.asList(nameField, priceField, descField));
        context.addEntity(product);

        // Debug
        System.out.println("Before run - Entities: " + context.getEntities().size());
        System.out.println("Entity config: dto=" + product.getDto() + ", service=" + product.getService());

        // When
        new ServerGenerator(context).run();

        // Debug
        System.out.println("After run - Entities: " + context.getEntities().size());

        // Then - verify all generated files
        String basePath = "src/main/java/com/example/full/";

        // Main app class
        Path appClass = testDir.resolve(basePath + "FullappApp.java");
        assertTrue(Files.exists(appClass), "Main app class should exist: " + appClass);

        // Domain class
        Path entityFile = testDir.resolve(basePath + "domain/Product.java");
        assertTrue(Files.exists(entityFile), "Entity class should exist: " + entityFile);

        // Repository
        Path repoFile = testDir.resolve(basePath + "repository/ProductRepository.java");
        assertTrue(Files.exists(repoFile), "Repository should exist: " + repoFile);

        // Service
        Path serviceFile = testDir.resolve(basePath + "service/ProductService.java");
        assertTrue(Files.exists(serviceFile), "Service interface should exist: " + serviceFile);

        // Service Implementation
        Path serviceImplFile = testDir.resolve(basePath + "service/impl/ProductServiceImpl.java");
        assertTrue(Files.exists(serviceImplFile), "Service implementation should exist: " + serviceImplFile);

        // DTO
        Path dtoFile = testDir.resolve(basePath + "service/dto/ProductDTO.java");
        assertTrue(Files.exists(dtoFile), "DTO should exist: " + dtoFile);

        // Mapper
        Path mapperFile = testDir.resolve(basePath + "service/mapper/ProductMapper.java");
        assertTrue(Files.exists(mapperFile), "Mapper should exist: " + mapperFile);

        // REST Controller
        Path resourceFile = testDir.resolve(basePath + "web/rest/ProductResource.java");
        assertTrue(Files.exists(resourceFile), "REST controller should exist: " + resourceFile);

        // Criteria (for filtering)
        Path criteriaFile = testDir.resolve(basePath + "service/criteria/ProductCriteria.java");
        assertTrue(Files.exists(criteriaFile), "Criteria class should exist: " + criteriaFile);

        // Query Service
        Path queryServiceFile = testDir.resolve(basePath + "service/ProductQueryService.java");
        assertTrue(Files.exists(queryServiceFile), "Query service should exist: " + queryServiceFile);

        System.out.println("\n=== Full microservice structure generated successfully! ===");
    }
}
