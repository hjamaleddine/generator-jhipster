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
    @DisplayName("Should generate server application class")
    void shouldGenerateServerApplicationClass() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/" + config.getMainClass() + ".java");
    }

    @Test
    @DisplayName("Should generate Spring Boot configuration")
    void shouldGenerateSpringBootConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertFileExists("src/main/resources/config/application.yml");
        assertFileExists("src/main/resources/config/application-dev.yml");
        assertFileExists("src/main/resources/config/application-prod.yml");

        String appConfig = Files.readString(testDir.resolve("src/main/resources/config/application.yml"));
        assertTrue(appConfig.contains("spring:") || appConfig.contains("server:"));
    }

    @Test
    @DisplayName("Should generate security configuration for JWT")
    void shouldGenerateSecurityConfigurationForJwt() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setAuthenticationType("jwt");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/security/SecurityConfiguration.java");
        assertFileExists("src/main/java/" + packagePath + "/security/jwt/JWTFilter.java");
        assertFileExists("src/main/java/" + packagePath + "/security/jwt/TokenProvider.java");
    }

    @Test
    @DisplayName("Should generate security configuration for OAuth2")
    void shouldGenerateSecurityConfigurationForOAuth2() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setAuthenticationType("oauth2");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/security/SecurityConfiguration.java");

        String securityConfig = Files.readString(
            testDir.resolve("src/main/java/" + packagePath + "/security/SecurityConfiguration.java"));
        assertTrue(securityConfig.contains("oauth2") || securityConfig.contains("OAuth2"));
    }

    @Test
    @DisplayName("Should generate service discovery configuration for Consul")
    void shouldGenerateServiceDiscoveryConfigurationForConsul() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setServiceDiscoveryType("consul");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String appConfig = Files.readString(testDir.resolve("src/main/resources/config/application.yml"));
        assertTrue(appConfig.contains("consul") || appConfig.contains("cloud"));
    }

    @Test
    @DisplayName("Should generate service discovery configuration for Eureka")
    void shouldGenerateServiceDiscoveryConfigurationForEureka() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setServiceDiscoveryType("eureka");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String appConfig = Files.readString(testDir.resolve("src/main/resources/config/application.yml"));
        assertTrue(appConfig.contains("eureka") || appConfig.contains("cloud"));
    }

    @Test
    @DisplayName("Should generate cache configuration")
    void shouldGenerateCacheConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setCacheProvider("ehcache");
        config.setEnableHibernateCache(true);
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/config/CacheConfiguration.java");
    }

    @Test
    @DisplayName("Should generate database configuration for SQL")
    void shouldGenerateDatabaseConfigurationForSql() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/config/DatabaseConfiguration.java");
    }

    @Test
    @DisplayName("Should generate logging configuration")
    void shouldGenerateLoggingConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        assertFileExists("src/main/resources/logback-spring.xml");
    }

    @Test
    @DisplayName("Should generate exception handling classes")
    void shouldGenerateExceptionHandlingClasses() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/main/java/" + packagePath + "/web/rest/errors/ExceptionTranslator.java");
        assertFileExists("src/main/java/" + packagePath + "/web/rest/errors/BadRequestAlertException.java");
    }

    @Test
    @DisplayName("Should generate health check endpoints")
    void shouldGenerateHealthCheckEndpoints() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String appConfig = Files.readString(testDir.resolve("src/main/resources/config/application.yml"));
        assertTrue(appConfig.contains("management:") || appConfig.contains("actuator"));
    }

    @Test
    @DisplayName("Should generate test infrastructure")
    void shouldGenerateTestInfrastructure() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new ServerGenerator(context).run();

        // Then
        String packagePath = config.getPackageFolder();
        assertFileExists("src/test/java/" + packagePath + "/IntegrationTest.java");
    }

    // Helper methods

    private JHipsterConfig createConfig() {
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

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
