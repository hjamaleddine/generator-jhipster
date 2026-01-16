/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.docker.DockerGenerator;
import io.github.jhipster.generator.generators.docker.DockerComposeGenerator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Docker and Docker Compose Generators.
 * These generators configure Docker settings for the application.
 */
class DockerGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("docker-generator-test");
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
    @DisplayName("DockerGenerator should return correct name")
    void dockerGeneratorShouldReturnCorrectName() {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);
        DockerGenerator generator = new DockerGenerator(context);

        // When
        String name = generator.getName();

        // Then
        assertEquals("docker", name);
    }

    @Test
    @DisplayName("DockerComposeGenerator should return correct name")
    void dockerComposeGeneratorShouldReturnCorrectName() {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);
        DockerComposeGenerator generator = new DockerComposeGenerator(context);

        // When
        String name = generator.getName();

        // Then
        assertEquals("docker-compose", name);
    }

    @Test
    @DisplayName("DockerGenerator should run without errors")
    void dockerGeneratorShouldRunWithoutErrors() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then - should not throw
        assertDoesNotThrow(() -> new DockerGenerator(context).run());
    }

    @Test
    @DisplayName("DockerComposeGenerator should run without errors")
    void dockerComposeGeneratorShouldRunWithoutErrors() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then - should not throw
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
    }

    @Test
    @DisplayName("DockerGenerator should handle PostgreSQL configuration")
    void dockerGeneratorShouldHandlePostgresqlConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerGenerator(context).run());
        assertEquals("postgresql", config.getProdDatabaseType());
    }

    @Test
    @DisplayName("DockerGenerator should handle MySQL configuration")
    void dockerGeneratorShouldHandleMysqlConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("mysql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerGenerator(context).run());
        assertEquals("mysql", config.getProdDatabaseType());
    }

    @Test
    @DisplayName("DockerComposeGenerator should handle MongoDB configuration")
    void dockerComposeGeneratorShouldHandleMongodbConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("mongodb");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
        assertEquals("mongodb", config.getDatabaseType());
    }

    @Test
    @DisplayName("DockerComposeGenerator should handle Consul service discovery")
    void dockerComposeGeneratorShouldHandleConsulServiceDiscovery() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setServiceDiscoveryType("consul");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
        assertEquals("consul", config.getServiceDiscoveryType());
    }

    @Test
    @DisplayName("DockerComposeGenerator should handle Eureka service discovery")
    void dockerComposeGeneratorShouldHandleEurekaServiceDiscovery() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setServiceDiscoveryType("eureka");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
        assertEquals("eureka", config.getServiceDiscoveryType());
    }

    @Test
    @DisplayName("DockerComposeGenerator should handle Kafka message broker")
    void dockerComposeGeneratorShouldHandleKafkaMessageBroker() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setMessageBroker("kafka");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
        assertEquals("kafka", config.getMessageBroker());
    }

    @Test
    @DisplayName("DockerComposeGenerator should handle Elasticsearch search engine")
    void dockerComposeGeneratorShouldHandleElasticsearchSearchEngine() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setSearchEngine("elasticsearch");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
        assertEquals("elasticsearch", config.getSearchEngine());
    }

    @Test
    @DisplayName("DockerComposeGenerator should handle OAuth2 authentication")
    void dockerComposeGeneratorShouldHandleOAuth2Authentication() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setAuthenticationType("oauth2");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new DockerComposeGenerator(context).run());
        assertEquals("oauth2", config.getAuthenticationType());
    }

    @Test
    @DisplayName("Should preserve base name in configuration")
    void shouldPreserveBaseNameInConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setBaseName("myservice");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertEquals("myservice", config.getBaseName());
    }

    // Helper methods

    private JHipsterConfig createConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("dockertest");
        config.setPackageName("com.test.docker");
        config.setApplicationType("microservice");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        return config;
    }
}
