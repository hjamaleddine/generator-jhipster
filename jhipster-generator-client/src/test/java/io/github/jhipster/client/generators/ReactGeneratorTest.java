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
 * Tests for React Generator.
 */
class ReactGeneratorTest {

    private static Path testDir;

    @BeforeAll
    static void setUpClass() throws IOException {
        testDir = Files.createTempDirectory("react-generator-test");
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
    @DisplayName("Should generate React application structure")
    void shouldGenerateReactApplicationStructure() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/app.tsx");
        assertFileExists("src/main/webapp/index.tsx");
        assertFileExists("src/main/webapp/app/routes.tsx");
        assertFileExists("package.json");
        assertFileExists("tsconfig.json");
    }

    @Test
    @DisplayName("Should generate Redux store configuration")
    void shouldGenerateReduxStoreConfiguration() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/config/store.ts");
        assertFileExists("src/main/webapp/app/shared/reducers/index.ts");
        assertFileExists("src/main/webapp/app/shared/reducers/authentication.ts");
        assertFileExists("src/main/webapp/app/shared/reducers/application-profile.ts");

        // Verify Redux setup
        String storeContent = Files.readString(testDir.resolve("src/main/webapp/app/config/store.ts"));
        assertTrue(storeContent.contains("configureStore"));
        assertTrue(storeContent.contains("reducer"));
    }

    @Test
    @DisplayName("Should generate React entity files with reducer")
    void shouldGenerateReactEntityFilesWithReducer() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        config.setEntities(List.of(createProductEntity()));
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then - Entity files
        assertFileExists("src/main/webapp/app/shared/model/product.model.ts");
        assertFileExists("src/main/webapp/app/entities/product/product.tsx");
        assertFileExists("src/main/webapp/app/entities/product/product-detail.tsx");
        assertFileExists("src/main/webapp/app/entities/product/product-update.tsx");
        assertFileExists("src/main/webapp/app/entities/product/product-delete-dialog.tsx");
        assertFileExists("src/main/webapp/app/entities/product/product.reducer.ts");
        assertFileExists("src/main/webapp/app/entities/product/index.tsx");

        // Verify reducer structure
        String reducerContent = Files.readString(testDir.resolve("src/main/webapp/app/entities/product/product.reducer.ts"));
        assertTrue(reducerContent.contains("createAsyncThunk"));
        assertTrue(reducerContent.contains("createSlice"));
        assertTrue(reducerContent.contains("getEntities"));
        assertTrue(reducerContent.contains("createEntity"));
        assertTrue(reducerContent.contains("updateEntity"));
        assertTrue(reducerContent.contains("deleteEntity"));
    }

    @Test
    @DisplayName("Should generate webpack configuration")
    void shouldGenerateWebpackConfiguration() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("webpack/webpack.common.js");
        assertFileExists("webpack/webpack.dev.js");
        assertFileExists("webpack/webpack.prod.js");
    }

    @Test
    @DisplayName("Should generate React layouts")
    void shouldGenerateReactLayouts() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/shared/layout/header/header.tsx");
        assertFileExists("src/main/webapp/app/shared/layout/footer/footer.tsx");
    }

    @Test
    @DisplayName("Should generate home and login modules")
    void shouldGenerateHomeAndLoginModules() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/modules/home/home.tsx");
        assertFileExists("src/main/webapp/app/modules/login/login.tsx");
        assertFileExists("src/main/webapp/app/modules/login/logout.tsx");
    }

    @Test
    @DisplayName("Should include axios configuration")
    void shouldIncludeAxiosConfiguration() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/config/axios-interceptor.ts");

        String axiosConfig = Files.readString(testDir.resolve("src/main/webapp/app/config/axios-interceptor.ts"));
        assertTrue(axiosConfig.contains("axios"));
        assertTrue(axiosConfig.contains("interceptors"));
    }

    @Test
    @DisplayName("Should generate error boundary components")
    void shouldGenerateErrorBoundaryComponents() throws Exception {
        // Given
        ClientConfig config = createBasicConfig();
        ClientGeneratorContext context = new ClientGeneratorContext(testDir, config);

        // When
        new ClientGenerator(context).generate();

        // Then
        assertFileExists("src/main/webapp/app/shared/error/error-boundary.tsx");
        assertFileExists("src/main/webapp/app/shared/error/error-boundary-routes.tsx");
        assertFileExists("src/main/webapp/app/shared/error/page-not-found.tsx");
    }

    // Helper methods

    private ClientConfig createBasicConfig() {
        ClientConfig config = new ClientConfig();
        config.setBaseName("testReactApp");
        config.setPackageName("com.test.react");
        config.setClientFramework("react");
        config.setClientBundler("webpack");
        config.setAuthenticationType("jwt");
        config.setEnableTranslation(false);
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

        entity.setFields(List.of(nameField, priceField));
        return entity;
    }

    private void assertFileExists(String relativePath) {
        Path path = testDir.resolve(relativePath);
        assertTrue(Files.exists(path), "File should exist: " + relativePath);
    }
}
