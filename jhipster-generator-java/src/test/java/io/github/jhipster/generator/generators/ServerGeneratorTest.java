/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.server.ServerGenerator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Server Generator.
 * ServerGenerator configures server settings and composes with SpringBootGenerator.
 */
class ServerGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("server-generator-test");
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
    @DisplayName("Should set default port for microservice")
    void shouldSetDefaultPortForMicroservice() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setServerPort(null); // Clear to test default
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then - microservices default to port 8081
        assertEquals(8081, config.getServerPort());
    }

    @Test
    @DisplayName("Should set default port for monolith")
    void shouldSetDefaultPortForMonolith() throws Exception {
        // Given
        JHipsterConfig config = createMonolithConfig();
        config.setServerPort(null); // Clear to test default
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then - monoliths default to port 8080
        assertEquals(8080, config.getServerPort());
    }

    @Test
    @DisplayName("Should preserve custom port")
    void shouldPreserveCustomPort() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setServerPort(9090);
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals(9090, config.getServerPort());
    }

    @Test
    @DisplayName("Should set context values during preparing phase")
    void shouldSetContextValuesDuringPreparing() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then - verify context has been populated with server values
        assertNotNull(context.getConfigValue("serverPort"));
        assertNotNull(context.getConfigValue("authenticationType"));
        assertNotNull(context.getConfigValue("databaseType"));
    }

    @Test
    @DisplayName("Should return correct generator name")
    void shouldReturnCorrectGeneratorName() {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);
        ServerGenerator generator = new ServerGenerator(context);

        // When
        String name = generator.getName();

        // Then
        assertEquals("server", name);
    }

    @Test
    @DisplayName("Should handle JWT authentication type")
    void shouldHandleJwtAuthenticationType() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setAuthenticationType("jwt");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals("jwt", context.getConfigValue("authenticationType"));
    }

    @Test
    @DisplayName("Should handle OAuth2 authentication type")
    void shouldHandleOAuth2AuthenticationType() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setAuthenticationType("oauth2");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals("oauth2", context.getConfigValue("authenticationType"));
    }

    @Test
    @DisplayName("Should handle SQL database type")
    void shouldHandleSqlDatabaseType() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals("sql", context.getConfigValue("databaseType"));
        assertEquals("postgresql", context.getConfigValue("prodDatabaseType"));
    }

    @Test
    @DisplayName("Should handle MongoDB database type")
    void shouldHandleMongodbDatabaseType() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setDatabaseType("mongodb");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals("mongodb", context.getConfigValue("databaseType"));
    }

    @Test
    @DisplayName("Should handle Maven build tool")
    void shouldHandleMavenBuildTool() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBuildTool("maven");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals("maven", context.getConfigValue("buildTool"));
    }

    @Test
    @DisplayName("Should handle Gradle build tool")
    void shouldHandleGradleBuildTool() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBuildTool("gradle");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertEquals("gradle", context.getConfigValue("buildTool"));
    }

    // Helper methods

    private JHipsterConfig createMicroserviceConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("servertest");
        config.setPackageName("com.test.server");
        config.setApplicationType("microservice");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        config.setCacheProvider("ehcache");
        config.setEnableHibernateCache(true);
        return config;
    }

    private JHipsterConfig createMonolithConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("monolithtest");
        config.setPackageName("com.test.monolith");
        config.setApplicationType("monolith");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setBuildTool("maven");
        return config;
    }
}
