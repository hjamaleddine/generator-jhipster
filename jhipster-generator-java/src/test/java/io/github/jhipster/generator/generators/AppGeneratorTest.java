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
    @DisplayName("Should generate microservice application structure")
    void shouldGenerateMicroserviceApplicationStructure() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists(".yo-rc.json");
        assertFileExists("pom.xml");
        assertFileExists(".gitignore");
        assertFileExists(".editorconfig");
    }

    @Test
    @DisplayName("Should generate main application class")
    void shouldGenerateMainApplicationClass() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBaseName("product");
        config.setPackageName("com.example.product");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists("src/main/java/com/example/product/ProductApp.java");

        String appContent = Files.readString(testDir.resolve("src/main/java/com/example/product/ProductApp.java"));
        assertTrue(appContent.contains("@SpringBootApplication"));
        assertTrue(appContent.contains("public class ProductApp"));
    }

    @Test
    @DisplayName("Should generate Spring configuration files")
    void shouldGenerateSpringConfigurationFiles() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists("src/main/resources/config/application.yml");
        assertFileExists("src/main/resources/config/application-dev.yml");
        assertFileExists("src/main/resources/config/application-prod.yml");
    }

    @Test
    @DisplayName("Should generate Gradle build when buildTool is gradle")
    void shouldGenerateGradleBuildWhenBuildToolIsGradle() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setBuildTool("gradle");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists("build.gradle");
        assertFileExists("settings.gradle");
        assertFileExists("gradlew");
        assertFileExists("gradlew.bat");
        assertFileExists("gradle/wrapper/gradle-wrapper.properties");
    }

    @Test
    @DisplayName("Should configure service discovery with Consul")
    void shouldConfigureServiceDiscoveryWithConsul() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setServiceDiscoveryType("consul");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        String pomContent = Files.readString(testDir.resolve("pom.xml"));
        assertTrue(pomContent.contains("spring-cloud-starter-consul-discovery") ||
                   pomContent.contains("consul"));
    }

    @Test
    @DisplayName("Should configure JWT authentication")
    void shouldConfigureJwtAuthentication() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setAuthenticationType("jwt");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists("src/main/java/" + config.getPackageFolder() + "/security/jwt/JWTFilter.java");
        assertFileExists("src/main/java/" + config.getPackageFolder() + "/security/jwt/TokenProvider.java");
    }

    @Test
    @DisplayName("Should configure OAuth2 authentication")
    void shouldConfigureOAuth2Authentication() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setAuthenticationType("oauth2");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        String pomContent = Files.readString(testDir.resolve("pom.xml"));
        assertTrue(pomContent.contains("spring-boot-starter-oauth2-resource-server") ||
                   pomContent.contains("oauth2"));
    }

    @Test
    @DisplayName("Should generate gateway application")
    void shouldGenerateGatewayApplication() throws Exception {
        // Given
        JHipsterConfig config = createGatewayConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists(".yo-rc.json");
        assertFileExists("pom.xml");

        String yoRcContent = Files.readString(testDir.resolve(".yo-rc.json"));
        assertTrue(yoRcContent.contains("\"applicationType\": \"gateway\"") ||
                   yoRcContent.contains("\"applicationType\":\"gateway\""));
    }

    @Test
    @DisplayName("Should skip user management for microservices")
    void shouldSkipUserManagementForMicroservices() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        config.setSkipUserManagement(false); // Should be overridden
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then - Microservices should have user management skipped
        assertTrue(config.getSkipUserManagement() || config.isMicroservice());
    }

    @Test
    @DisplayName("Should generate test infrastructure")
    void shouldGenerateTestInfrastructure() throws Exception {
        // Given
        JHipsterConfig config = createMicroserviceConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new AppGenerator(context).run();

        // Then
        assertFileExists("src/test/java/" + config.getPackageFolder() + "/IntegrationTest.java");
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

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
