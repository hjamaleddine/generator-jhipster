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

import io.github.jhipster.generator.domain.model.EntityContext;
import io.github.jhipster.generator.domain.model.FieldContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates test sample factory class for an entity.
 * This is the Java equivalent of _persistClass_TestSamples.java.ejs template.
 */
public class EntityTestSamplesGenerator {

    private final EntityContext context;
    private final StringBuilder sb;
    private int indentLevel = 0;
    private static final String INDENT = "    ";

    public EntityTestSamplesGenerator(EntityContext context) {
        this.context = context;
        this.sb = new StringBuilder();
    }

    /**
     * Generates the complete test samples class source code.
     *
     * @return the generated Java source code
     */
    public String generate() {
        sb.setLength(0);

        generatePackageDeclaration();
        generateImports();
        generateClassDeclaration();
        generateStaticFields();
        generateSample1Method();
        generateSample2Method();
        generateRandomSampleMethod();
        generateClassClose();

        return sb.toString();
    }

    private void generatePackageDeclaration() {
        appendLine("package " + context.getEntityAbsolutePackage() + ".domain;");
        appendLine();
    }

    private void generateImports() {
        List<FieldContext> sampleFields = getSampleFields();

        appendLine("import org.junit.jupiter.api.Test;");
        appendLine("import static org.assertj.core.api.Assertions.assertThat;");
        appendLine("import " + context.getPackageName() + ".web.rest.TestUtil;");

        boolean hasStringOrUUID = sampleFields.stream()
            .anyMatch(f -> isStringType(f) || isUUIDType(f));
        if (hasStringOrUUID) {
            appendLine("import java.util.UUID;");
        }

        boolean hasLongOrInteger = sampleFields.stream()
            .anyMatch(f -> isLongType(f) || isIntegerType(f));
        if (hasLongOrInteger) {
            appendLine("import java.util.Random;");
        }

        if (sampleFields.stream().anyMatch(this::isLongType)) {
            appendLine("import java.util.concurrent.atomic.AtomicLong;");
        }

        if (sampleFields.stream().anyMatch(this::isIntegerType)) {
            appendLine("import java.util.concurrent.atomic.AtomicInteger;");
        }

        appendLine();
    }

    private void generateClassDeclaration() {
        appendLine("public class " + context.getPersistClass() + "TestSamples {");
        appendLine();
        indentLevel++;
    }

    private void generateStaticFields() {
        List<FieldContext> sampleFields = getSampleFields();

        boolean hasLongOrInteger = sampleFields.stream()
            .anyMatch(f -> isLongType(f) || isIntegerType(f));
        if (hasLongOrInteger) {
            appendLine("private static final Random random = new Random();");
        }

        if (sampleFields.stream().anyMatch(this::isLongType)) {
            appendLine("private static final AtomicLong longCount = new AtomicLong(random.nextInt() + ( 2L * Integer.MAX_VALUE ));");
        }

        if (sampleFields.stream().anyMatch(this::isIntegerType)) {
            appendLine("private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + ( 2 * Short.MAX_VALUE ));");
        }

        appendLine();
    }

    private void generateSample1Method() {
        String persistClass = context.getPersistClass();
        String persistInstance = context.getPersistInstance();
        List<FieldContext> sampleFields = getSampleFields();

        appendLine("public static " + persistClass + " get" + persistClass + "Sample1() {");
        indentLevel++;

        if (context.isFluentMethods()) {
            StringBuilder builder = new StringBuilder();
            builder.append("return new ").append(persistClass).append("()");
            for (FieldContext field : sampleFields) {
                builder.append(".").append(field.getFieldName()).append("(")
                    .append(getJavaValueSample1(field)).append(")");
            }
            builder.append(";");
            appendLine(builder.toString());
        } else {
            appendLine(persistClass + " " + persistInstance + " = new " + persistClass + "();");
            for (FieldContext field : sampleFields) {
                appendLine(persistInstance + ".set" + field.getFieldNameCapitalized() + "("
                    + getJavaValueSample1(field) + ");");
            }
            appendLine("return " + persistInstance + ";");
        }

        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateSample2Method() {
        String persistClass = context.getPersistClass();
        String persistInstance = context.getPersistInstance();
        List<FieldContext> sampleFields = getSampleFields();

        appendLine("public static " + persistClass + " get" + persistClass + "Sample2() {");
        indentLevel++;

        if (context.isFluentMethods()) {
            StringBuilder builder = new StringBuilder();
            builder.append("return new ").append(persistClass).append("()");
            for (FieldContext field : sampleFields) {
                builder.append(".").append(field.getFieldName()).append("(")
                    .append(getJavaValueSample2(field)).append(")");
            }
            builder.append(";");
            appendLine(builder.toString());
        } else {
            appendLine(persistClass + " " + persistInstance + " = new " + persistClass + "();");
            for (FieldContext field : sampleFields) {
                appendLine(persistInstance + ".set" + field.getFieldNameCapitalized() + "("
                    + getJavaValueSample2(field) + ");");
            }
            appendLine("return " + persistInstance + ";");
        }

        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateRandomSampleMethod() {
        String persistClass = context.getPersistClass();
        String persistInstance = context.getPersistInstance();
        List<FieldContext> sampleFields = getSampleFields();

        appendLine("public static " + persistClass + " get" + persistClass + "RandomSampleGenerator() {");
        indentLevel++;

        if (context.isFluentMethods()) {
            StringBuilder builder = new StringBuilder();
            builder.append("return new ").append(persistClass).append("()");
            for (FieldContext field : sampleFields) {
                builder.append(".").append(field.getFieldName()).append("(")
                    .append(getJavaValueGenerator(field)).append(")");
            }
            builder.append(";");
            appendLine(builder.toString());
        } else {
            appendLine(persistClass + " " + persistInstance + " = new " + persistClass + "();");
            for (FieldContext field : sampleFields) {
                appendLine(persistInstance + ".set" + field.getFieldNameCapitalized() + "("
                    + getJavaValueGenerator(field) + ");");
            }
            appendLine("return " + persistInstance + ";");
        }

        indentLevel--;
        appendLine("}");
    }

    private void generateClassClose() {
        indentLevel--;
        appendLine("}");
    }

    // Helper methods

    private List<FieldContext> getSampleFields() {
        return context.getFields().stream()
            .filter(field -> !field.isTransientField())
            .filter(field -> isIntegerType(field) || isLongType(field) || isStringType(field) || isUUIDType(field))
            .collect(Collectors.toList());
    }

    private boolean isIntegerType(FieldContext field) {
        return "Integer".equals(field.getJavaFieldType());
    }

    private boolean isLongType(FieldContext field) {
        return "Long".equals(field.getJavaFieldType());
    }

    private boolean isStringType(FieldContext field) {
        return "String".equals(field.getJavaFieldType());
    }

    private boolean isUUIDType(FieldContext field) {
        return "UUID".equals(field.getJavaFieldType());
    }

    private String getJavaValueSample1(FieldContext field) {
        if (isLongType(field)) {
            return "1L";
        } else if (isIntegerType(field)) {
            return "1";
        } else if (isStringType(field)) {
            return "\"" + field.getFieldName() + "1\"";
        } else if (isUUIDType(field)) {
            return "UUID.randomUUID()";
        }
        return "null";
    }

    private String getJavaValueSample2(FieldContext field) {
        if (isLongType(field)) {
            return "2L";
        } else if (isIntegerType(field)) {
            return "2";
        } else if (isStringType(field)) {
            return "\"" + field.getFieldName() + "2\"";
        } else if (isUUIDType(field)) {
            return "UUID.randomUUID()";
        }
        return "null";
    }

    private String getJavaValueGenerator(FieldContext field) {
        if (isLongType(field)) {
            return "longCount.incrementAndGet()";
        } else if (isIntegerType(field)) {
            return "intCount.incrementAndGet()";
        } else if (isStringType(field)) {
            return "UUID.randomUUID().toString()";
        } else if (isUUIDType(field)) {
            return "UUID.randomUUID()";
        }
        return "null";
    }

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
