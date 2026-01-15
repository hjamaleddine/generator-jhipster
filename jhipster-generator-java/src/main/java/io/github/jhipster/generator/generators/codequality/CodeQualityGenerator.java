/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.codequality;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * Code Quality Generator.
 * Generates code quality configuration (Checkstyle, SpotBugs, PMD, JaCoCo).
 */
public class CodeQualityGenerator extends BaseApplicationGenerator {

    public CodeQualityGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "code-quality";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingCodeQuality", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing code quality configuration");

        writeSpotBugsExclusions();
        writePmdRuleset();
        writeJacocoConfiguration();
    }

    private void writeSpotBugsExclusions() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<FindBugsFilter\n");
        xml.append("    xmlns=\"https://github.com/spotbugs/filter/3.0.0\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"https://github.com/spotbugs/filter/3.0.0 https://raw.githubusercontent.com/spotbugs/spotbugs/3.1.0/spotbugs/etc/findbugsfilter.xsd\">\n\n");

        xml.append("    <!-- Exclude generated code -->\n");
        xml.append("    <Match>\n");
        xml.append("        <Package name=\"~").append(config.getPackageName().replace(".", "\\.")).append("\\..*Generated.*\"/>\n");
        xml.append("    </Match>\n\n");

        xml.append("    <!-- Exclude configuration classes -->\n");
        xml.append("    <Match>\n");
        xml.append("        <Package name=\"~").append(config.getPackageName().replace(".", "\\.")).append("\\.config\\..*\"/>\n");
        xml.append("        <Bug pattern=\"EI_EXPOSE_REP,EI_EXPOSE_REP2\"/>\n");
        xml.append("    </Match>\n\n");

        xml.append("    <!-- Exclude DTOs -->\n");
        xml.append("    <Match>\n");
        xml.append("        <Package name=\"~").append(config.getPackageName().replace(".", "\\.")).append("\\.service\\.dto\\..*\"/>\n");
        xml.append("        <Bug pattern=\"EI_EXPOSE_REP,EI_EXPOSE_REP2\"/>\n");
        xml.append("    </Match>\n\n");

        xml.append("    <!-- Exclude domain entities -->\n");
        xml.append("    <Match>\n");
        xml.append("        <Package name=\"~").append(config.getPackageName().replace(".", "\\.")).append("\\.domain\\..*\"/>\n");
        xml.append("        <Bug pattern=\"EI_EXPOSE_REP,EI_EXPOSE_REP2\"/>\n");
        xml.append("    </Match>\n\n");

        xml.append("    <!-- Exclude test code -->\n");
        xml.append("    <Match>\n");
        xml.append("        <Source name=\"~.*Test\\.java\"/>\n");
        xml.append("    </Match>\n\n");

        xml.append("</FindBugsFilter>\n");

        writeFile(context.getBasePath().toString() + "/spotbugs-exclude.xml", xml.toString());
    }

    private void writePmdRuleset() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\"?>\n");
        xml.append("<ruleset name=\"Custom PMD Ruleset\"\n");
        xml.append("    xmlns=\"http://pmd.sourceforge.net/ruleset/2.0.0\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd\">\n\n");

        xml.append("    <description>Custom PMD ruleset for ").append(config.getBaseName()).append("</description>\n\n");

        xml.append("    <!-- Best Practices -->\n");
        xml.append("    <rule ref=\"category/java/bestpractices.xml\">\n");
        xml.append("        <exclude name=\"JUnitTestsShouldIncludeAssert\"/>\n");
        xml.append("        <exclude name=\"JUnitAssertionsShouldIncludeMessage\"/>\n");
        xml.append("    </rule>\n\n");

        xml.append("    <!-- Code Style -->\n");
        xml.append("    <rule ref=\"category/java/codestyle.xml\">\n");
        xml.append("        <exclude name=\"AtLeastOneConstructor\"/>\n");
        xml.append("        <exclude name=\"OnlyOneReturn\"/>\n");
        xml.append("        <exclude name=\"LongVariable\"/>\n");
        xml.append("        <exclude name=\"ShortVariable\"/>\n");
        xml.append("        <exclude name=\"CommentDefaultAccessModifier\"/>\n");
        xml.append("        <exclude name=\"DefaultPackage\"/>\n");
        xml.append("    </rule>\n\n");

        xml.append("    <!-- Design -->\n");
        xml.append("    <rule ref=\"category/java/design.xml\">\n");
        xml.append("        <exclude name=\"LawOfDemeter\"/>\n");
        xml.append("        <exclude name=\"LoosePackageCoupling\"/>\n");
        xml.append("        <exclude name=\"DataClass\"/>\n");
        xml.append("    </rule>\n\n");

        xml.append("    <!-- Documentation -->\n");
        xml.append("    <rule ref=\"category/java/documentation.xml\">\n");
        xml.append("        <exclude name=\"CommentRequired\"/>\n");
        xml.append("        <exclude name=\"CommentSize\"/>\n");
        xml.append("    </rule>\n\n");

        xml.append("    <!-- Error Prone -->\n");
        xml.append("    <rule ref=\"category/java/errorprone.xml\">\n");
        xml.append("        <exclude name=\"BeanMembersShouldSerialize\"/>\n");
        xml.append("        <exclude name=\"DataflowAnomalyAnalysis\"/>\n");
        xml.append("    </rule>\n\n");

        xml.append("    <!-- Multithreading -->\n");
        xml.append("    <rule ref=\"category/java/multithreading.xml\"/>\n\n");

        xml.append("    <!-- Performance -->\n");
        xml.append("    <rule ref=\"category/java/performance.xml\"/>\n\n");

        xml.append("    <!-- Security -->\n");
        xml.append("    <rule ref=\"category/java/security.xml\"/>\n\n");

        xml.append("</ruleset>\n");

        writeFile(context.getBasePath().toString() + "/pmd-ruleset.xml", xml.toString());
    }

    private void writeJacocoConfiguration() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!-- JaCoCo coverage exclusions -->\n");
        xml.append("<configuration>\n");
        xml.append("    <excludes>\n");
        xml.append("        <!-- Exclude configuration classes -->\n");
        xml.append("        <exclude>**/config/*</exclude>\n");
        xml.append("        <!-- Exclude constants -->\n");
        xml.append("        <exclude>**/*Constants*</exclude>\n");
        xml.append("        <!-- Exclude main application class -->\n");
        xml.append("        <exclude>**/*Application*</exclude>\n");
        xml.append("        <!-- Exclude DTOs -->\n");
        xml.append("        <exclude>**/dto/*</exclude>\n");
        xml.append("        <!-- Exclude VM classes -->\n");
        xml.append("        <exclude>**/vm/*</exclude>\n");
        xml.append("    </excludes>\n");
        xml.append("</configuration>\n");

        writeFile(context.getBasePath().toString() + "/jacoco-exclusions.xml", xml.toString());
    }

    /**
     * Returns the code quality Maven plugins configuration as XML snippet.
     */
    public String getCodeQualityMavenPlugins() {
        StringBuilder plugins = new StringBuilder();

        // Checkstyle plugin
        plugins.append("            <plugin>\n");
        plugins.append("                <groupId>org.apache.maven.plugins</groupId>\n");
        plugins.append("                <artifactId>maven-checkstyle-plugin</artifactId>\n");
        plugins.append("                <version>3.3.1</version>\n");
        plugins.append("                <configuration>\n");
        plugins.append("                    <configLocation>checkstyle.xml</configLocation>\n");
        plugins.append("                    <includeTestSourceDirectory>true</includeTestSourceDirectory>\n");
        plugins.append("                    <consoleOutput>true</consoleOutput>\n");
        plugins.append("                    <failsOnError>true</failsOnError>\n");
        plugins.append("                </configuration>\n");
        plugins.append("                <executions>\n");
        plugins.append("                    <execution>\n");
        plugins.append("                        <id>validate</id>\n");
        plugins.append("                        <phase>validate</phase>\n");
        plugins.append("                        <goals>\n");
        plugins.append("                            <goal>check</goal>\n");
        plugins.append("                        </goals>\n");
        plugins.append("                    </execution>\n");
        plugins.append("                </executions>\n");
        plugins.append("            </plugin>\n");

        // SpotBugs plugin
        plugins.append("            <plugin>\n");
        plugins.append("                <groupId>com.github.spotbugs</groupId>\n");
        plugins.append("                <artifactId>spotbugs-maven-plugin</artifactId>\n");
        plugins.append("                <version>4.8.2.0</version>\n");
        plugins.append("                <configuration>\n");
        plugins.append("                    <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>\n");
        plugins.append("                    <effort>Max</effort>\n");
        plugins.append("                    <threshold>Low</threshold>\n");
        plugins.append("                </configuration>\n");
        plugins.append("            </plugin>\n");

        // JaCoCo plugin
        plugins.append("            <plugin>\n");
        plugins.append("                <groupId>org.jacoco</groupId>\n");
        plugins.append("                <artifactId>jacoco-maven-plugin</artifactId>\n");
        plugins.append("                <version>0.8.11</version>\n");
        plugins.append("                <executions>\n");
        plugins.append("                    <execution>\n");
        plugins.append("                        <id>pre-unit-tests</id>\n");
        plugins.append("                        <goals>\n");
        plugins.append("                            <goal>prepare-agent</goal>\n");
        plugins.append("                        </goals>\n");
        plugins.append("                    </execution>\n");
        plugins.append("                    <execution>\n");
        plugins.append("                        <id>post-unit-test</id>\n");
        plugins.append("                        <phase>test</phase>\n");
        plugins.append("                        <goals>\n");
        plugins.append("                            <goal>report</goal>\n");
        plugins.append("                        </goals>\n");
        plugins.append("                    </execution>\n");
        plugins.append("                </executions>\n");
        plugins.append("            </plugin>\n");

        return plugins.toString();
    }
}
