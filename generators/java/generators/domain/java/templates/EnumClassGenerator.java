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

import io.github.jhipster.generator.domain.support.EnumInfo;
import io.github.jhipster.generator.domain.support.EnumInfo.EnumValue;

import java.util.List;

/**
 * Generates enum Java classes.
 * This is the Java equivalent of _enumName_.java.ejs template.
 */
public class EnumClassGenerator {

    private final EnumInfo enumInfo;
    private final String entityAbsolutePackage;
    private final StringBuilder sb;
    private int indentLevel = 0;
    private static final String INDENT = "    ";

    public EnumClassGenerator(EnumInfo enumInfo, String entityAbsolutePackage) {
        this.enumInfo = enumInfo;
        this.entityAbsolutePackage = entityAbsolutePackage;
        this.sb = new StringBuilder();
    }

    /**
     * Generates the complete enum class source code.
     *
     * @return the generated Java source code
     */
    public String generate() {
        sb.setLength(0);

        generatePackageDeclaration();
        generateEnumJavadoc();
        generateEnumDeclaration();
        generateEnumValues();
        generateValueFieldAndMethods();
        generateEnumClose();

        return sb.toString();
    }

    private void generatePackageDeclaration() {
        appendLine("package " + entityAbsolutePackage + ".domain.enumeration;");
        appendLine();
    }

    private void generateEnumJavadoc() {
        if (enumInfo.getEnumJavadoc() != null) {
            appendLine(enumInfo.getEnumJavadoc());
        } else {
            appendLine("/**");
            appendLine(" * The " + enumInfo.getEnumName() + " enumeration.");
            appendLine(" */");
        }
    }

    private void generateEnumDeclaration() {
        appendLine("public enum " + enumInfo.getEnumName() + " {");
        indentLevel++;
    }

    private void generateEnumValues() {
        List<EnumValue> enumValues = enumInfo.getEnumValues();
        boolean withoutCustomValues = enumInfo.isWithoutCustomValues();
        boolean withCustomValues = enumInfo.isWithCustomValues();
        boolean withSomeCustomValues = enumInfo.isWithSomeCustomValues();

        if (withoutCustomValues) {
            // Simple enum without custom values
            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 0; i < enumValues.size(); i++) {
                EnumValue enumValue = enumValues.get(i);
                if (enumValue.getComment() != null) {
                    valueBuilder.append("\n").append(enumValue.getComment()).append("\n");
                }
                valueBuilder.append(getIndent()).append(enumValue.getName());
                if (i < enumValues.size() - 1) {
                    valueBuilder.append(",");
                }
            }
            sb.append(valueBuilder);
            appendLine();
        } else {
            // Enum with custom values
            for (int i = 0; i < enumValues.size(); i++) {
                EnumValue enumValue = enumValues.get(i);
                boolean isLast = i == enumValues.size() - 1;

                if (enumValue.getComment() != null) {
                    appendLine(enumValue.getComment());
                }

                if (enumValue.getName().equals(enumValue.getValue())) {
                    appendLine(enumValue.getName() + (isLast ? ";" : ","));
                } else {
                    appendLine(enumValue.getName() + "(\"" + enumValue.getValue() + "\")" + (isLast ? ";" : ","));
                }
            }
        }
    }

    private void generateValueFieldAndMethods() {
        if (enumInfo.isWithoutCustomValues()) {
            return;
        }

        appendLine();

        boolean withCustomValues = enumInfo.isWithCustomValues();
        boolean withSomeCustomValues = enumInfo.isWithSomeCustomValues();

        // Value field
        if (withCustomValues) {
            appendLine("private final String value;");
        } else {
            appendLine("private String value;");
        }
        appendLine();

        // Default constructor for enums that have some values without custom value
        if (withSomeCustomValues) {
            appendLine(enumInfo.getEnumName() + "() {}");
            appendLine();
        }

        // Constructor with value
        appendLine(enumInfo.getEnumName() + "(String value) {");
        indentLevel++;
        appendLine("this.value = value;");
        indentLevel--;
        appendLine("}");
        appendLine();

        // getValue method
        appendLine("public String getValue() {");
        indentLevel++;
        appendLine("return value;");
        indentLevel--;
        appendLine("}");
    }

    private void generateEnumClose() {
        indentLevel--;
        appendLine("}");
    }

    // Helper methods

    private void appendLine() {
        sb.append("\n");
    }

    private void appendLine(String line) {
        sb.append(getIndent()).append(line).append("\n");
    }

    private String getIndent() {
        return INDENT.repeat(indentLevel);
    }
}
