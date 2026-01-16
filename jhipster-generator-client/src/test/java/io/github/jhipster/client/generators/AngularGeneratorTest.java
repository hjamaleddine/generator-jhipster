/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 */
package io.github.jhipster.client.generators;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientConfig.EntityConfig;
import io.github.jhipster.client.config.ClientConfig.FieldConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.generators.angular.AngularGenerator;
import io.github.jhipster.client.generators.client.ClientGenerator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Angular Generator.
 */
class AngularGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("angular-generator-test");
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
        // Clean test directory before each test
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
    @DisplayName("Should generate Angular application structure")
    void shouldGenerateAngularApplicationStructure() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        ClientGenerator generator = new ClientGenerator(context);
        generator.generate();

        // Then
        assertFileExists("src/main/webapp/app/app.component.ts");
        assertFileExists("src/main/webapp/app/app.config.ts");
        assertFileExists("src/main/webapp/app/app.routes.ts");
        assertFileExists("src/main/webapp/main.ts");
        assertFileExists("angular.json");
        assertFileExists("package.json");
        assertFileExists("tsconfig.json");
    }

    @Test
    @DisplayName("Should generate Angular layouts")
    void shouldGenerateAngularLayouts() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/layouts/navbar/navbar.component.ts");
        assertFileExists("src/main/webapp/app/layouts/navbar/navbar.component.html");
        assertFileExists("src/main/webapp/app/layouts/footer/footer.component.ts");
    }

    @Test
    @DisplayName("Should generate Angular core services")
    void shouldGenerateAngularCoreServices() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/core/auth/account.service.ts");
        assertFileExists("src/main/webapp/app/core/auth/user-route-access.service.ts");
        assertFileExists("src/main/webapp/app/core/interceptor/auth.interceptor.ts");
        assertFileExists("src/main/webapp/app/core/interceptor/error.interceptor.ts");
    }

    @Test
    @DisplayName("Should generate Angular entity files")
    void shouldGenerateAngularEntityFiles() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        config.setEntities(List.of(createProductEntity()));
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then - Entity files
        assertFileExists("src/main/webapp/app/entities/product/product.model.ts");
        assertFileExists("src/main/webapp/app/entities/product/service/product.service.ts");
        assertFileExists("src/main/webapp/app/entities/product/product.routes.ts");
        assertFileExists("src/main/webapp/app/entities/product/list/product.component.ts");
        assertFileExists("src/main/webapp/app/entities/product/detail/product-detail.component.ts");
        assertFileExists("src/main/webapp/app/entities/product/update/product-update.component.ts");
        assertFileExists("src/main/webapp/app/entities/product/delete/product-delete-dialog.component.ts");

        // Then - Entity routes index
        assertFileExists("src/main/webapp/app/entities/entity.routes.ts");
    }

    @Test
    @DisplayName("Should generate correct model interface")
    void shouldGenerateCorrectModelInterface() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        config.setEntities(List.of(createProductEntity()));
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String modelContent = Files.readString(testDir.resolve("src/main/webapp/app/entities/product/product.model.ts"));
        assertTrue(modelContent.contains("export interface IProduct"));
        assertTrue(modelContent.contains("name"));
        assertTrue(modelContent.contains("price"));
        assertTrue(modelContent.contains("description"));
    }

    @Test
    @DisplayName("Should handle JWT authentication")
    void shouldHandleJwtAuthentication() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        config.setAuthenticationType("jwt");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String loginService = Files.readString(testDir.resolve("src/main/webapp/app/login/login.service.ts"));
        assertTrue(loginService.contains("/api/authenticate"));
    }

    @Test
    @DisplayName("Should handle OAuth2 authentication")
    void shouldHandleOAuth2Authentication() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        config.setAuthenticationType("oauth2");
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String loginService = Files.readString(testDir.resolve("src/main/webapp/app/login/login.service.ts"));
        assertTrue(loginService.contains("oauth2/authorization/oidc"));
    }

    @Test
    @DisplayName("Should enable translation when i18n is true")
    void shouldEnableTranslationWhenI18nIsTrue() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        config.setEnableTranslation(true);
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String packageJson = Files.readString(testDir.resolve("package.json"));
        assertTrue(packageJson.contains("@ngx-translate/core"));

        String appConfig = Files.readString(testDir.resolve("src/main/webapp/app/app.config.ts"));
        assertTrue(appConfig.contains("TranslateModule"));
    }

    @Test
    @DisplayName("Should not include translation when i18n is false")
    void shouldNotIncludeTranslationWhenI18nIsFalse() throws Exception {
        // Given
        ClientConfig config = createBasicConfig("angular");
        config.setEnableTranslation(false);
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        String appConfig = Files.readString(testDir.resolve("src/main/webapp/app/app.config.ts"));
        assertFalse(appConfig.contains("TranslateModule"));
    }

    // Helper methods

    private ClientConfig createBasicConfig(String framework) {
        ClientConfig config = new ClientConfig();
        config.setBaseName("testApp");
        config.setPackageName("com.test.app");
        config.setClientFramework(framework);
        config.setClientBundler("webpack");
        config.setAuthenticationType("jwt");
        config.setEnableTranslation(true);
        config.setNativeLanguage("en");
        config.setDevServerPort(9000);
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

        FieldConfig descField = new FieldConfig();
        descField.setFieldName("description");
        descField.setFieldType("String");
        descField.setRequired(false);

        entity.setFields(List.of(nameField, priceField, descField));
        return entity;
    }

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
