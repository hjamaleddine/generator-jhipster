/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.client.generators;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientConfig.EntityConfig;
import io.github.jhipster.client.config.ClientConfig.FieldConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.generators.client.ClientGenerator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Vue Generator.
 */
class VueGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("vue-generator-test");
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
    @DisplayName("Should generate Vue application structure")
    void shouldGenerateVueApplicationStructure() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("webpack");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/app.vue");
        assertFileExists("src/main/webapp/main.ts");
        assertFileExists("src/main/webapp/app/router/index.ts");
        assertFileExists("package.json");
        assertFileExists("tsconfig.json");
    }

    @Test
    @DisplayName("Should generate Vite configuration when bundler is vite")
    void shouldGenerateViteConfigurationWhenBundlerIsVite() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("vite.config.ts");

        String viteConfig = Files.readString(testDir.resolve("vite.config.ts"));
        assertTrue(viteConfig.contains("defineConfig"));
        assertTrue(viteConfig.contains("vue()"));
    }

    @Test
    @DisplayName("Should generate webpack configuration when bundler is webpack")
    void shouldGenerateWebpackConfigurationWhenBundlerIsWebpack() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("webpack");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("webpack/webpack.common.js");
        assertFileExists("webpack/webpack.dev.js");
    }

    @Test
    @DisplayName("Should generate Pinia store")
    void shouldGeneratePiniaStore() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/shared/config/store/account-store.ts");

        String storeContent = Files.readString(testDir.resolve("src/main/webapp/app/shared/config/store/account-store.ts"));
        assertTrue(storeContent.contains("defineStore"));
        assertTrue(storeContent.contains("pinia"));
    }

    @Test
    @DisplayName("Should generate Vue entity components")
    void shouldGenerateVueEntityComponents() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        config.setEntities(List.of(createProductEntity()));
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/shared/model/product.model.ts");
        assertFileExists("src/main/webapp/app/entities/product/product.vue");
        assertFileExists("src/main/webapp/app/entities/product/product-details.vue");
        assertFileExists("src/main/webapp/app/entities/product/product-update.vue");
        assertFileExists("src/main/webapp/app/entities/product/product.service.ts");
    }

    @Test
    @DisplayName("Should generate Vue service with axios")
    void shouldGenerateVueServiceWithAxios() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        config.setEntities(List.of(createProductEntity()));
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String serviceContent = Files.readString(testDir.resolve("src/main/webapp/app/entities/product/product.service.ts"));
        assertTrue(serviceContent.contains("axios"));
        assertTrue(serviceContent.contains("find(id: number)"));
        assertTrue(serviceContent.contains("retrieve()"));
        assertTrue(serviceContent.contains("create(entity"));
        assertTrue(serviceContent.contains("update(entity"));
        assertTrue(serviceContent.contains("delete(id: number)"));
    }

    @Test
    @DisplayName("Should generate Vue layouts")
    void shouldGenerateVueLayouts() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/core/navbar/navbar.vue");
        assertFileExists("src/main/webapp/app/core/footer/footer.vue");
        assertFileExists("src/main/webapp/app/core/home/home.vue");
    }

    @Test
    @DisplayName("Should generate Vue router with entity routes")
    void shouldGenerateVueRouterWithEntityRoutes() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        config.setEntities(List.of(createProductEntity()));
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/router/entities.ts");

        String routerContent = Files.readString(testDir.resolve("src/main/webapp/app/router/entities.ts"));
        assertTrue(routerContent.contains("product"));
        assertTrue(routerContent.contains("RouteRecordRaw"));
    }

    @Test
    @DisplayName("Should generate login components for JWT auth")
    void shouldGenerateLoginComponentsForJwtAuth() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        config.setAuthenticationType("jwt");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/account/login.vue");

        String loginContent = Files.readString(testDir.resolve("src/main/webapp/app/account/login.vue"));
        assertTrue(loginContent.contains("username"));
        assertTrue(loginContent.contains("password"));
        assertTrue(loginContent.contains("doLogin"));
    }

    @Test
    @DisplayName("Should generate OAuth2 login redirect")
    void shouldGenerateOAuth2LoginRedirect() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("vite");
        config.setAuthenticationType("oauth2");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String loginContent = Files.readString(testDir.resolve("src/main/webapp/app/account/login.vue"));
        assertTrue(loginContent.contains("oauth2/authorization/oidc"));
    }

    // Helper methods

    private ClientConfig createBasicConfig(String bundler) {
        ClientConfig config = new ClientConfig();
        config.setBaseName("testVueApp");
        config.setPackageName("com.test.vue");
        config.setClientFramework("vue");
        config.setClientBundler(bundler);
        config.setAuthenticationType("jwt");
        config.setEnableTranslation(false);
        config.setDevServerPort(3000);
        return config;
    }

    private EntityConfig createProductEntity() {
        EntityConfig entity = new EntityConfig();
        entity.setName("Product");
        entity.setEntityAngularName("Product");
        entity.setEntityReactName("Product");
        entity.setEntityFileName("product");
        entity.setEntityFolderName("product");
        entity.setReadOnly(false);

        FieldConfig nameField = new FieldConfig();
        nameField.setFieldName("name");
        nameField.setFieldType("String");
        nameField.setRequired(true);

        FieldConfig priceField = new FieldConfig();
        priceField.setFieldName("price");
        priceField.setFieldType("BigDecimal");
        priceField.setRequired(true);

        entity.setFields(List.of(nameField, priceField));
        return entity;
    }

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
