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
package io.github.jhipster.generator.domain.templates;

/**
 * Generates the AssertUtils test utility class.
 * This is the Java equivalent of AssertUtils.java.ejs template.
 */
public class AssertUtilsGenerator {

    private final String packageName;
    private final StringBuilder sb;
    private static final String INDENT = "    ";

    public AssertUtilsGenerator(String packageName) {
        this.packageName = packageName;
        this.sb = new StringBuilder();
    }

    /**
     * Generates the complete AssertUtils class source code.
     *
     * @return the generated Java source code
     */
    public String generate() {
        sb.setLength(0);

        generatePackageDeclaration();
        generateImports();
        generateClassDeclaration();
        generateComparators();
        generateClassClose();

        return sb.toString();
    }

    private void generatePackageDeclaration() {
        appendLine("package " + packageName + ".domain;");
        appendLine();
    }

    private void generateImports() {
        appendLine("import java.math.BigDecimal;");
        appendLine("import java.time.ZoneOffset;");
        appendLine("import java.time.ZonedDateTime;");
        appendLine("import java.util.Comparator;");
        appendLine();
    }

    private void generateClassDeclaration() {
        appendLine("public class AssertUtils {");
        appendLine();
    }

    private void generateComparators() {
        // ZonedDateTime comparator
        appendLine(INDENT + "public static Comparator<ZonedDateTime> zonedDataTimeSameInstant = Comparator.nullsFirst((e1, a2) ->");
        appendLine(INDENT + INDENT + "e1.withZoneSameInstant(ZoneOffset.UTC).compareTo(a2.withZoneSameInstant(ZoneOffset.UTC))");
        appendLine(INDENT + ");");
        appendLine();

        // BigDecimal comparator
        appendLine(INDENT + "public static Comparator<BigDecimal> bigDecimalCompareTo = Comparator.nullsFirst(BigDecimal::compareTo);");
    }

    private void generateClassClose() {
        appendLine("}");
    }

    // Helper methods

    private void appendLine() {
        sb.append("\n");
    }

    private void appendLine(String line) {
        sb.append(line).append("\n");
    }
}
