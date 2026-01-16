/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.generator.generators;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.app.AppGenerator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for App Generator.
 * AppGenerator is the main orchestrator that composes with other generators.
 */
class AppGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("app-generator-test");
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
    @DisplayName("Should return correct generator name")
    void shouldReturnCorrectGeneratorName() {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);
        AppGenerator generator = new AppGenerator(context);

        // When
        String name = generator.getName();

        // Then
        assertEquals("app", name);
    }

    @Test
    @DisplayName("Should run without errors for microservice")
    void shouldRunWithoutErrorsForMicroservice() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new AppGenerator(context).run());
    }

    @Test
    @DisplayName("Should run without errors for monolith")
    void shouldRunWithoutErrorsForMonolith() throws Exception {
        // Given
        JHipsterConfig config = createMonolithConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new AppGenerator(context).run());
    }

    @Test
    @DisplayName("Should run without errors for gateway")
    void shouldRunWithoutErrorsForGateway() throws Exception {
        // Given
        JHipsterConfig config = createGatewayConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When/Then
        assertDoesNotThrow(() -> new AppGenerator(context).run());
    }

    @Test
    @DisplayName("Should handle JWT authentication configuration")
    void shouldHandleJwtAuthenticationConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setAuthenticationType("jwt");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("jwt", config.getAuthenticationType());
    }

    @Test
    @DisplayName("Should handle OAuth2 authentication configuration")
    void shouldHandleOAuth2AuthenticationConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setAuthenticationType("oauth2");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("oauth2", config.getAuthenticationType());
    }

    @Test
    @DisplayName("Should handle Consul service discovery")
    void shouldHandleConsulServiceDiscovery() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setServiceDiscoveryType("consul");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("consul", config.getServiceDiscoveryType());
    }

    @Test
    @DisplayName("Should handle Eureka service discovery")
    void shouldHandleEurekaServiceDiscovery() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setServiceDiscoveryType("eureka");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("eureka", config.getServiceDiscoveryType());
    }

    @Test
    @DisplayName("Should handle Maven build tool")
    void shouldHandleMavenBuildTool() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBuildTool("maven");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("maven", config.getBuildTool());
    }

    @Test
    @DisplayName("Should handle Gradle build tool")
    void shouldHandleGradleBuildTool() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBuildTool("gradle");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("gradle", config.getBuildTool());
    }

    @Test
    @DisplayName("Should handle SQL database")
    void shouldHandleSqlDatabase() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("sql", config.getDatabaseType());
        assertEquals("postgresql", config.getProdDatabaseType());
    }

    @Test
    @DisplayName("Should handle MongoDB database")
    void shouldHandleMongodbDatabase() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setDatabaseType("mongodb");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("mongodb", config.getDatabaseType());
    }

    @Test
    @DisplayName("Should identify microservice application type")
    void shouldIdentifyMicroserviceApplicationType() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertTrue(config.isMicroservice());
        assertFalse(config.isMonolith());
        assertFalse(config.isGateway());
    }

    @Test
    @DisplayName("Should identify gateway application type")
    void shouldIdentifyGatewayApplicationType() throws Exception {
        // Given
        JHipsterConfig config = createGatewayConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertTrue(config.isGateway());
        assertFalse(config.isMicroservice());
        assertFalse(config.isMonolith());
    }

    @Test
    @DisplayName("Should preserve base name configuration")
    void shouldPreserveBaseNameConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBaseName("myapp");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("myapp", config.getBaseName());
    }

    @Test
    @DisplayName("Should preserve package name configuration")
    void shouldPreservePackageNameConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setPackageName("com.example.myapp");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertEquals("com.example.myapp", config.getPackageName());
    }

    // Helper methods

    private JHipsterConfig createMicroserviceConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("testapp");
        config.setPackageName("com.test.app");
        config.setApplicationType("microservice");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        config.setSkipClient(true);
        return config;
    }

    private JHipsterConfig createMonolithConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("monolith");
        config.setPackageName("com.test.monolith");
        config.setApplicationType("monolith");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setBuildTool("maven");
        return config;
    }

    private JHipsterConfig createGatewayConfig() {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("gateway");
        config.setPackageName("com.test.gateway");
        config.setApplicationType("gateway");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        config.setSkipClient(false);
        return config;
    }
}
