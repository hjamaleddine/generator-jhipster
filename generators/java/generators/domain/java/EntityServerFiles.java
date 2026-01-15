/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jhipster.generator.domain;

import io.github.jhipster.generator.domain.model.EntityContext;
import io.github.jhipster.generator.domain.support.EnumInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Configuration for entity server files to be generated.
 * Defines the templates and their output paths for domain layer generation.
 */
public class EntityServerFiles {

    private EntityServerFiles() {
        // Utility class
    }

    /**
     * Gets the list of domain model files to generate for an entity.
     *
     * @param context the entity context
     * @return list of file configurations
     */
    public static List<FileConfig> getModelFiles(EntityContext context) {
        List<FileConfig> files = new ArrayList<>();

        if (!context.isEntityDomainLayer()) {
            return files;
        }

        // Main entity class
        files.add(new FileConfig(
            "_entityPackage_/domain/_persistClass_.java.jhi",
            getMainJavaPath(context, "domain", context.getPersistClass() + ".java"),
            ctx -> ctx.isEntityDomainLayer()
        ));

        return files;
    }

    /**
     * Gets the list of test files to generate for an entity.
     *
     * @param context the entity context
     * @return list of file configurations
     */
    public static List<FileConfig> getModelTestFiles(EntityContext context) {
        List<FileConfig> files = new ArrayList<>();

        if (!context.isEntityDomainLayer()) {
            return files;
        }

        String entityName = context.getPersistClass();

        // Entity asserts
        files.add(new FileConfig(
            "_entityPackage_/domain/_persistClass_Asserts.java",
            getTestJavaPath(context, "domain", entityName + "Asserts.java"),
            ctx -> ctx.isEntityDomainLayer()
        ));

        // Entity test
        files.add(new FileConfig(
            "_entityPackage_/domain/_persistClass_Test.java",
            getTestJavaPath(context, "domain", entityName + "Test.java"),
            ctx -> ctx.isEntityDomainLayer()
        ));

        // Entity test samples
        files.add(new FileConfig(
            "_entityPackage_/domain/_persistClass_TestSamples.java",
            getTestJavaPath(context, "domain", entityName + "TestSamples.java"),
            ctx -> ctx.isEntityDomainLayer()
        ));

        return files;
    }

    /**
     * Gets the list of server files (validation, Jackson) to generate for an entity.
     *
     * @param context the entity context
     * @return list of file configurations
     */
    public static List<FileConfig> getServerFiles(EntityContext context) {
        List<FileConfig> files = new ArrayList<>();

        if (!context.isEntityDomainLayer()) {
            return files;
        }

        String entityName = context.getPersistClass();

        // Jakarta validation fragment
        if (context.isUseJakartaValidation()) {
            files.add(new FileConfig(
                "_entityPackage_/domain/_persistClass_.java.jhi.jakarta_validation",
                getMainJavaPath(context, "domain", entityName + ".java.jhi.jakarta_validation"),
                ctx -> ctx.isUseJakartaValidation() && ctx.isEntityDomainLayer()
            ));
        }

        // Jackson identity info fragment
        if (context.isUseJacksonIdentityInfo()) {
            files.add(new FileConfig(
                "_entityPackage_/domain/_persistClass_.java.jhi.jackson_identity_info",
                getMainJavaPath(context, "domain", entityName + ".java.jhi.jackson_identity_info"),
                ctx -> ctx.isUseJacksonIdentityInfo() && ctx.isEntityDomainLayer()
            ));
        }

        return files;
    }

    /**
     * Gets the list of enum files to generate.
     *
     * @param enumInfo the enum information
     * @param context  the entity context
     * @return list of file configurations
     */
    public static List<FileConfig> getEnumFiles(EnumInfo enumInfo, EntityContext context) {
        List<FileConfig> files = new ArrayList<>();

        files.add(new FileConfig(
            "_entityPackage_/domain/enumeration/_enumName_.java",
            getMainJavaPath(context, "domain/enumeration", enumInfo.getEnumName() + ".java"),
            ctx -> true
        ));

        return files;
    }

    /**
     * Gets base test utility files.
     *
     * @param packageName the base package name
     * @param testSrcDir  the test source directory
     * @return list of file configurations
     */
    public static List<FileConfig> getBaseTestFiles(String packageName, String testSrcDir) {
        List<FileConfig> files = new ArrayList<>();

        String packagePath = packageName.replace('.', '/');

        files.add(new FileConfig(
            "domain/AssertUtils.java",
            Paths.get(testSrcDir, packagePath, "domain", "AssertUtils.java").toString(),
            ctx -> true
        ));

        return files;
    }

    // Path construction helpers

    private static String getMainJavaPath(EntityContext context, String subPackage, String fileName) {
        String basePath = context.getJavaPackageSrcDir();
        if (basePath == null) {
            basePath = "src/main/java";
        }

        String packagePath = context.getEntityAbsolutePackage().replace('.', '/');

        return Paths.get(basePath, packagePath, subPackage, fileName).toString();
    }

    private static String getTestJavaPath(EntityContext context, String subPackage, String fileName) {
        String basePath = context.getJavaPackageTestDir();
        if (basePath == null) {
            basePath = "src/test/java";
        }

        String packagePath = context.getEntityAbsolutePackage().replace('.', '/');

        return Paths.get(basePath, packagePath, subPackage, fileName).toString();
    }

    /**
     * Configuration for a single file to generate.
     */
    public static class FileConfig {
        private final String templatePath;
        private final String outputPath;
        private final Predicate<EntityContext> condition;

        public FileConfig(String templatePath, String outputPath, Predicate<EntityContext> condition) {
            this.templatePath = templatePath;
            this.outputPath = outputPath;
            this.condition = condition;
        }

        public String getTemplatePath() {
            return templatePath;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public boolean shouldGenerate(EntityContext context) {
            return condition.test(context);
        }

        @Override
        public String toString() {
            return "FileConfig{" +
                   "templatePath='" + templatePath + '\'' +
                   ", outputPath='" + outputPath + '\'' +
                   '}';
        }
    }
}
