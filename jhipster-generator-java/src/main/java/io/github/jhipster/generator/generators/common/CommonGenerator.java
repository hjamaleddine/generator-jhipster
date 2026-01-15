/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.common;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.generators.git.GitGenerator;

/**
 * Common Generator.
 * Generates common configuration files (editorconfig, prettier, README, etc.).
 */
public class CommonGenerator extends BaseApplicationGenerator {

    public CommonGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "common";
    }

    @Override
    protected void beforeQueue() {
        // Compose with git generator
        composeWith(new GitGenerator(context));
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingCommon", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing common configuration files");

        writeEditorConfig();
        writePrettierConfig();
        writePrettierIgnore();
        writeReadme();
        writeSonarProperties();
        writeCheckstyleConfig();
    }

    private void writeEditorConfig() throws Exception {
        StringBuilder editorconfig = new StringBuilder();
        editorconfig.append("# EditorConfig helps maintain consistent coding styles\n");
        editorconfig.append("# https://editorconfig.org\n\n");

        editorconfig.append("root = true\n\n");

        editorconfig.append("[*]\n");
        editorconfig.append("indent_style = space\n");
        editorconfig.append("indent_size = 4\n");
        editorconfig.append("end_of_line = lf\n");
        editorconfig.append("charset = utf-8\n");
        editorconfig.append("trim_trailing_whitespace = true\n");
        editorconfig.append("insert_final_newline = true\n\n");

        editorconfig.append("[*.md]\n");
        editorconfig.append("trim_trailing_whitespace = false\n\n");

        editorconfig.append("[*.{yml,yaml}]\n");
        editorconfig.append("indent_size = 2\n\n");

        editorconfig.append("[*.{json,js,ts,tsx,vue}]\n");
        editorconfig.append("indent_size = 2\n\n");

        editorconfig.append("[*.xml]\n");
        editorconfig.append("indent_size = 4\n\n");

        editorconfig.append("[Makefile]\n");
        editorconfig.append("indent_style = tab\n");

        writeFile(context.getBasePath().toString() + "/.editorconfig", editorconfig.toString());
    }

    private void writePrettierConfig() throws Exception {
        StringBuilder prettier = new StringBuilder();
        prettier.append("# Prettier configuration\n");
        prettier.append("# https://prettier.io/docs/en/configuration.html\n\n");

        prettier.append("printWidth: 140\n");
        prettier.append("singleQuote: true\n");
        prettier.append("tabWidth: 2\n");
        prettier.append("useTabs: false\n");
        prettier.append("endOfLine: lf\n");

        // Java-specific
        prettier.append("\n# Java files\n");
        prettier.append("overrides:\n");
        prettier.append("  - files: '*.java'\n");
        prettier.append("    options:\n");
        prettier.append("      tabWidth: 4\n");

        writeFile(context.getBasePath().toString() + "/.prettierrc.yml", prettier.toString());
    }

    private void writePrettierIgnore() throws Exception {
        StringBuilder prettierIgnore = new StringBuilder();
        prettierIgnore.append("# Prettier ignore file\n\n");

        prettierIgnore.append("# Build directories\n");
        prettierIgnore.append("target/\n");
        prettierIgnore.append("build/\n");
        prettierIgnore.append("node_modules/\n\n");

        prettierIgnore.append("# Generated files\n");
        prettierIgnore.append("*.min.js\n");
        prettierIgnore.append("*.min.css\n\n");

        prettierIgnore.append("# Package manager files\n");
        prettierIgnore.append("package-lock.json\n");
        prettierIgnore.append("yarn.lock\n\n");

        prettierIgnore.append("# Maven wrapper\n");
        prettierIgnore.append(".mvn/\n\n");

        prettierIgnore.append("# IDE\n");
        prettierIgnore.append(".idea/\n");
        prettierIgnore.append(".vscode/\n");

        writeFile(context.getBasePath().toString() + "/.prettierignore", prettierIgnore.toString());
    }

    private void writeReadme() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder readme = new StringBuilder();
        readme.append("# ").append(config.getBaseName()).append("\n\n");

        readme.append("This application was generated using JHipster Java Generator.\n\n");

        readme.append("## Development\n\n");

        readme.append("To start your application in the dev profile, run:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw\n");
        readme.append("```\n\n");

        readme.append("## Building for production\n\n");
        readme.append("### Packaging as jar\n\n");
        readme.append("To build the final jar and optimize the application for production, run:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw -Pprod clean verify\n");
        readme.append("```\n\n");

        readme.append("To ensure everything worked, run:\n\n");
        readme.append("```bash\n");
        readme.append("java -jar target/*.jar\n");
        readme.append("```\n\n");

        readme.append("### Packaging as Docker container\n\n");
        readme.append("To build and run the Docker image:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw -Pprod jib:dockerBuild\n");
        readme.append("docker run -p ").append(config.getServerPort()).append(":").append(config.getServerPort()).append(" ").append(config.getLowerBaseName()).append("\n");
        readme.append("```\n\n");

        readme.append("## Testing\n\n");
        readme.append("To launch your application's tests, run:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw verify\n");
        readme.append("```\n\n");

        if (isMicroservice()) {
            readme.append("## Microservice\n\n");
            readme.append("This application is a microservice. ");
            if (config.hasServiceDiscovery()) {
                if (config.isConsul()) {
                    readme.append("It uses Consul for service discovery.\n\n");
                } else if (config.isEureka()) {
                    readme.append("It uses Eureka for service discovery.\n\n");
                }
            }
        }

        readme.append("## API Documentation\n\n");
        readme.append("API documentation is available at `/swagger-ui.html` when the application is running.\n\n");

        readme.append("## Code Quality\n\n");
        readme.append("To analyze code quality with SonarQube:\n\n");
        readme.append("```bash\n");
        readme.append("./mvnw -Pprod clean verify sonar:sonar\n");
        readme.append("```\n");

        writeFile(context.getBasePath().toString() + "/README.md", readme.toString());
    }

    private void writeSonarProperties() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder sonar = new StringBuilder();
        sonar.append("# SonarQube Properties\n");
        sonar.append("sonar.projectKey=").append(config.getPackageName()).append(":").append(config.getLowerBaseName()).append("\n");
        sonar.append("sonar.projectName=").append(config.getBaseName()).append("\n");
        sonar.append("sonar.projectVersion=0.0.1-SNAPSHOT\n\n");

        sonar.append("sonar.sources=src/main/java\n");
        sonar.append("sonar.tests=src/test/java\n");
        sonar.append("sonar.java.binaries=target/classes\n");
        sonar.append("sonar.java.test.binaries=target/test-classes\n\n");

        sonar.append("sonar.java.coveragePlugin=jacoco\n");
        sonar.append("sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml\n\n");

        sonar.append("sonar.exclusions=src/main/java/**/config/**/*,src/main/java/**/*Constants.java\n");
        sonar.append("sonar.issue.ignore.multicriteria=S3437,S4502,S4684,UndocumentedApi\n\n");

        sonar.append("# Rule https://rules.sonarsource.com/java/RSPEC-3437 is ignored\n");
        sonar.append("sonar.issue.ignore.multicriteria.S3437.resourceKey=src/main/java/**/*\n");
        sonar.append("sonar.issue.ignore.multicriteria.S3437.ruleKey=java:S3437\n\n");

        sonar.append("# Rule https://rules.sonarsource.com/java/RSPEC-4502 is ignored\n");
        sonar.append("sonar.issue.ignore.multicriteria.S4502.resourceKey=src/main/java/**/*\n");
        sonar.append("sonar.issue.ignore.multicriteria.S4502.ruleKey=java:S4502\n\n");

        sonar.append("# Rule https://rules.sonarsource.com/java/RSPEC-4684 is ignored\n");
        sonar.append("sonar.issue.ignore.multicriteria.S4684.resourceKey=src/main/java/**/*\n");
        sonar.append("sonar.issue.ignore.multicriteria.S4684.ruleKey=java:S4684\n\n");

        sonar.append("# Rule https://rules.sonarsource.com/java/RSPEC-1176 is ignored\n");
        sonar.append("sonar.issue.ignore.multicriteria.UndocumentedApi.resourceKey=src/main/java/**/*\n");
        sonar.append("sonar.issue.ignore.multicriteria.UndocumentedApi.ruleKey=java:S1176\n");

        writeFile(context.getBasePath().toString() + "/sonar-project.properties", sonar.toString());
    }

    private void writeCheckstyleConfig() throws Exception {
        StringBuilder checkstyle = new StringBuilder();
        checkstyle.append("<?xml version=\"1.0\"?>\n");
        checkstyle.append("<!DOCTYPE module PUBLIC\n");
        checkstyle.append("    \"-//Checkstyle//DTD Checkstyle Configuration 1.3//EN\"\n");
        checkstyle.append("    \"https://checkstyle.org/dtds/configuration_1_3.dtd\">\n\n");

        checkstyle.append("<module name=\"Checker\">\n");
        checkstyle.append("    <property name=\"charset\" value=\"UTF-8\"/>\n");
        checkstyle.append("    <property name=\"severity\" value=\"warning\"/>\n");
        checkstyle.append("    <property name=\"fileExtensions\" value=\"java, properties, xml\"/>\n\n");

        checkstyle.append("    <!-- Checks for whitespace -->\n");
        checkstyle.append("    <module name=\"FileTabCharacter\">\n");
        checkstyle.append("        <property name=\"eachLine\" value=\"true\"/>\n");
        checkstyle.append("    </module>\n\n");

        checkstyle.append("    <module name=\"TreeWalker\">\n");
        checkstyle.append("        <!-- Checks for Naming Conventions -->\n");
        checkstyle.append("        <module name=\"ConstantName\"/>\n");
        checkstyle.append("        <module name=\"LocalFinalVariableName\"/>\n");
        checkstyle.append("        <module name=\"LocalVariableName\"/>\n");
        checkstyle.append("        <module name=\"MemberName\"/>\n");
        checkstyle.append("        <module name=\"MethodName\"/>\n");
        checkstyle.append("        <module name=\"PackageName\"/>\n");
        checkstyle.append("        <module name=\"ParameterName\"/>\n");
        checkstyle.append("        <module name=\"StaticVariableName\"/>\n");
        checkstyle.append("        <module name=\"TypeName\"/>\n\n");

        checkstyle.append("        <!-- Checks for imports -->\n");
        checkstyle.append("        <module name=\"IllegalImport\"/>\n");
        checkstyle.append("        <module name=\"RedundantImport\"/>\n");
        checkstyle.append("        <module name=\"UnusedImports\"/>\n\n");

        checkstyle.append("        <!-- Checks for whitespace -->\n");
        checkstyle.append("        <module name=\"EmptyForIteratorPad\"/>\n");
        checkstyle.append("        <module name=\"GenericWhitespace\"/>\n");
        checkstyle.append("        <module name=\"MethodParamPad\"/>\n");
        checkstyle.append("        <module name=\"NoWhitespaceAfter\"/>\n");
        checkstyle.append("        <module name=\"NoWhitespaceBefore\"/>\n");
        checkstyle.append("        <module name=\"ParenPad\"/>\n");
        checkstyle.append("        <module name=\"TypecastParenPad\"/>\n");
        checkstyle.append("        <module name=\"WhitespaceAfter\"/>\n");
        checkstyle.append("        <module name=\"WhitespaceAround\"/>\n\n");

        checkstyle.append("        <!-- Checks for blocks -->\n");
        checkstyle.append("        <module name=\"AvoidNestedBlocks\"/>\n");
        checkstyle.append("        <module name=\"EmptyBlock\"/>\n");
        checkstyle.append("        <module name=\"LeftCurly\"/>\n");
        checkstyle.append("        <module name=\"NeedBraces\"/>\n");
        checkstyle.append("        <module name=\"RightCurly\"/>\n\n");

        checkstyle.append("        <!-- Checks for common coding problems -->\n");
        checkstyle.append("        <module name=\"EmptyStatement\"/>\n");
        checkstyle.append("        <module name=\"EqualsHashCode\"/>\n");
        checkstyle.append("        <module name=\"IllegalInstantiation\"/>\n");
        checkstyle.append("        <module name=\"InnerAssignment\"/>\n");
        checkstyle.append("        <module name=\"MissingSwitchDefault\"/>\n");
        checkstyle.append("        <module name=\"MultipleVariableDeclarations\"/>\n");
        checkstyle.append("        <module name=\"SimplifyBooleanExpression\"/>\n");
        checkstyle.append("        <module name=\"SimplifyBooleanReturn\"/>\n\n");

        checkstyle.append("        <!-- Checks for class design -->\n");
        checkstyle.append("        <module name=\"FinalClass\"/>\n");
        checkstyle.append("        <module name=\"HideUtilityClassConstructor\"/>\n");
        checkstyle.append("        <module name=\"InterfaceIsType\"/>\n\n");

        checkstyle.append("        <!-- Miscellaneous -->\n");
        checkstyle.append("        <module name=\"ArrayTypeStyle\"/>\n");
        checkstyle.append("        <module name=\"UpperEll\"/>\n");
        checkstyle.append("    </module>\n");
        checkstyle.append("</module>\n");

        writeFile(context.getBasePath().toString() + "/checkstyle.xml", checkstyle.toString());
    }
}
