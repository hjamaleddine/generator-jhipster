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

        // Add an entity
        EntityConfig product = new EntityConfig();
        product.setName("Product");
        product.setEntityTableName("product");

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");

        product.setFields(Arrays.asList(nameField));
        context.addEntity(product);

        // Debug
        System.out.println("Before run - Entities: " + context.getEntities().size());

        // When
        new ServerGenerator(context).run();

        // Debug
        System.out.println("After run - Entities: " + context.getEntities().size());

        // Then - verify both app files and entity were generated
        Path appClass = testDir.resolve("src/main/java/com/example/full/FullappApp.java");
        assertTrue(Files.exists(appClass), "Main app class should exist: " + appClass);

        Path entityFile = testDir.resolve("src/main/java/com/example/full/domain/Product.java");
        System.out.println("Expected entity file: " + entityFile);
        System.out.println("Entity file exists: " + Files.exists(entityFile));

        // List all files in domain directory if it exists
        Path domainDir = testDir.resolve("src/main/java/com/example/full/domain");
        if (Files.exists(domainDir)) {
            System.out.println("Files in domain directory:");
            Files.list(domainDir).forEach(p -> System.out.println("  " + p.getFileName()));
        } else {
            System.out.println("Domain directory does not exist: " + domainDir);
        }

        assertTrue(Files.exists(entityFile), "Entity class should exist: " + entityFile);
    }
}
