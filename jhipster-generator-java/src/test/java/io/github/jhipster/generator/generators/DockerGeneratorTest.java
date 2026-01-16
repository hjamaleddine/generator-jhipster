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
    @DisplayName("Should generate Dockerfile")
    void shouldGenerateDockerfile() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/Dockerfile");

        String dockerfile = Files.readString(testDir.resolve("src/main/docker/Dockerfile"));
        assertTrue(dockerfile.contains("FROM"));
        assertTrue(dockerfile.contains("ENTRYPOINT") || dockerfile.contains("CMD"));
    }

    @Test
    @DisplayName("Should generate Jib Dockerfile")
    void shouldGenerateJibDockerfile() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/jib/entrypoint.sh");
    }

    @Test
    @DisplayName("Should generate docker-compose file for development")
    void shouldGenerateDockerComposeForDevelopment() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/app.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/app.yml"));
        assertTrue(composeFile.contains("services:") || composeFile.contains("version:"));
    }

    @Test
    @DisplayName("Should generate PostgreSQL docker-compose")
    void shouldGeneratePostgresqlDockerCompose() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/postgresql.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/postgresql.yml"));
        assertTrue(composeFile.contains("postgres"));
    }

    @Test
    @DisplayName("Should generate MySQL docker-compose")
    void shouldGenerateMysqlDockerCompose() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("sql");
        config.setProdDatabaseType("mysql");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/mysql.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/mysql.yml"));
        assertTrue(composeFile.contains("mysql"));
    }

    @Test
    @DisplayName("Should generate MongoDB docker-compose")
    void shouldGenerateMongodbDockerCompose() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setDatabaseType("mongodb");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/mongodb.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/mongodb.yml"));
        assertTrue(composeFile.contains("mongo"));
    }

    @Test
    @DisplayName("Should generate Consul docker-compose")
    void shouldGenerateConsulDockerCompose() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setServiceDiscoveryType("consul");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/consul.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/consul.yml"));
        assertTrue(composeFile.contains("consul"));
    }

    @Test
    @DisplayName("Should generate Kafka docker-compose when message broker enabled")
    void shouldGenerateKafkaDockerCompose() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setMessageBroker("kafka");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/kafka.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/kafka.yml"));
        assertTrue(composeFile.contains("kafka"));
    }

    @Test
    @DisplayName("Should generate Elasticsearch docker-compose when search enabled")
    void shouldGenerateElasticsearchDockerCompose() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setSearchEngine("elasticsearch");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/elasticsearch.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/elasticsearch.yml"));
        assertTrue(composeFile.contains("elasticsearch"));
    }

    @Test
    @DisplayName("Should generate Keycloak docker-compose for OAuth2")
    void shouldGenerateKeycloakDockerComposeForOAuth2() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setAuthenticationType("oauth2");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        assertFileExists("src/main/docker/keycloak.yml");

        String composeFile = Files.readString(testDir.resolve("src/main/docker/keycloak.yml"));
        assertTrue(composeFile.contains("keycloak"));
    }

    @Test
    @DisplayName("Should include base name in container configuration")
    void shouldIncludeBaseNameInContainerConfiguration() throws Exception {
        // Given
        JHipsterConfig config = createConfig();
        config.setBaseName("myservice");
        GeneratorContext context = new GeneratorContext(testDir, config);

        // When
        new DockerComposeGenerator(context).run();

        // Then
        String composeFile = Files.readString(testDir.resolve("src/main/docker/app.yml"));
        assertTrue(composeFile.toLowerCase().contains("myservice"));
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

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
