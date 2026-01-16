/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.server.ServerGenerator;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that generators actually produce output files.
 */
class FileGenerationTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("file-generation-test");
        System.out.println("Test output directory: " + testDir);
    }

    @AfterAll
    static void tearDownClass() throws IOException {
        System.out.println("\n=== Generated files ===");
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .filter(Files::isRegularFile)
                .forEach(path -> System.out.println("  " + testDir.relativize(path)));
        }
        // Don't delete files so user can inspect them
        System.out.println("\nFiles kept at: " + testDir);
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
        Path resourcesPath = testDir.resolve("src/main/resources");

        // Main application class
        Path appClass = mainJavaPath.resolve("MyappApp.java");
        assertTrue(Files.exists(appClass), "Main application class should exist: " + appClass);
        String appContent = Files.readString(appClass);
        assertTrue(appContent.contains("@SpringBootApplication"), "Should have @SpringBootApplication");
        assertTrue(appContent.contains("public class MyappApp"), "Should have correct class name");

        // pom.xml
        Path pomFile = testDir.resolve("pom.xml");
        assertTrue(Files.exists(pomFile), "pom.xml should exist");
        String pomContent = Files.readString(pomFile);
        assertTrue(pomContent.contains("<artifactId>myapp</artifactId>"), "pom.xml should have correct artifactId");
        assertTrue(pomContent.contains("spring-boot-starter-web"), "pom.xml should have web starter");

        // application.yml
        Path appYml = resourcesPath.resolve("config/application.yml");
        assertTrue(Files.exists(appYml), "application.yml should exist: " + appYml);
        String ymlContent = Files.readString(appYml);
        assertTrue(ymlContent.contains("myapp"), "application.yml should reference app name");

        // Configuration classes
        Path configPath = mainJavaPath.resolve("config");
        assertTrue(Files.exists(configPath.resolve("SecurityConfiguration.java")), "SecurityConfiguration should exist");
        assertTrue(Files.exists(configPath.resolve("ApplicationProperties.java")), "ApplicationProperties should exist");

        System.out.println("\n=== Test passed! Generated files verified ===");
    }

    @Test
    @DisplayName("Should generate entity domain class")
    void shouldGenerateEntityDomainClass() throws Exception {
        // Given
        Path entityTestDir = testDir.resolve("entity-test");
        Files.createDirectories(entityTestDir);

        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("entityapp");
        config.setPackageName("com.example.entity");
        config.setApplicationType("monolith");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");

        GeneratorContext context = new GeneratorContext(entityTestDir, config);

        // Add an entity
        EntityConfig product = new EntityConfig();
        product.setName("Product");
        product.setEntityTableName("product");

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");
        nameField.setFieldValidateRules(Arrays.asList("required"));

        FieldConfig priceField = new FieldConfig();
        priceField.setFieldName("price");
        priceField.setFieldType("BigDecimal");

        product.setFields(Arrays.asList(nameField, priceField));
        context.addEntity(product);

        // When
        new ServerGenerator(context).run();

        // Then - verify entity was generated
        Path domainPath = entityTestDir.resolve("src/main/java/com/example/entity/domain");
        Path entityFile = domainPath.resolve("Product.java");

        assertTrue(Files.exists(entityFile), "Entity class should exist: " + entityFile);
        String entityContent = Files.readString(entityFile);
        assertTrue(entityContent.contains("@Entity"), "Should have @Entity annotation");
        assertTrue(entityContent.contains("public class Product"), "Should have correct class name");
        assertTrue(entityContent.contains("private String name"), "Should have name field");
        assertTrue(entityContent.contains("private BigDecimal price"), "Should have price field");

        System.out.println("\n=== Entity generation test passed! ===");
    }
}
