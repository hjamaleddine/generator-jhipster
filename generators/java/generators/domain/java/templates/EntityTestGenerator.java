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
import io.github.jhipster.generator.domain.model.RelationshipContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates JUnit test class for an entity.
 * This is the Java equivalent of _persistClass_Test.java.ejs template.
 */
public class EntityTestGenerator {

    private final EntityContext context;
    private final StringBuilder sb;
    private int indentLevel = 0;
    private static final String INDENT = "    ";

    public EntityTestGenerator(EntityContext context) {
        this.context = context;
        this.sb = new StringBuilder();
    }

    /**
     * Generates the complete test class source code.
     *
     * @return the generated Java source code
     */
    public String generate() {
        sb.setLength(0);

        generatePackageDeclaration();
        generateImports();
        generateClassDeclaration();
        generateEqualsVerifierTest();
        generateHashCodeVerifierTest();
        generateRelationshipTests();
        generateClassClose();

        return sb.toString();
    }

    private void generatePackageDeclaration() {
        appendLine("package " + context.getEntityAbsolutePackage() + ".domain;");
        appendLine();
    }

    private void generateImports() {
        String persistClass = context.getPersistClass();

        // Static imports for test samples
        appendLine("import static " + context.getEntityAbsolutePackage() + ".domain." + persistClass + "TestSamples.*;");

        // Other entity test samples imports
        for (EntityContext otherEntity : getOtherEntitiesForTest()) {
            appendLine("import static " + otherEntity.getEntityAbsolutePackage() + ".domain."
                + otherEntity.getPersistClass() + "TestSamples.*;");
        }

        // Other entity class imports
        for (EntityContext otherEntity : getOtherEntitiesWithDifferentPackage()) {
            appendLine("import " + otherEntity.getEntityAbsolutePackage() + ".domain."
                + otherEntity.getPersistClass() + ";");
        }

        // Collection imports
        if (hasCollectionRelationships()) {
            appendLine("import java.util.HashSet;");
            appendLine("import java.util.Set;");
        }

        appendLine("import org.junit.jupiter.api.Test;");
        appendLine("import static org.assertj.core.api.Assertions.assertThat;");
        appendLine("import " + context.getPackageName() + ".web.rest.TestUtil;");
        appendLine();
    }

    private void generateClassDeclaration() {
        appendLine("class " + context.getPersistClass() + "Test {");
        appendLine();
        indentLevel++;
    }

    private void generateEqualsVerifierTest() {
        String persistClass = context.getPersistClass();
        String persistInstance = context.getPersistInstance();

        appendLine("@Test");
        appendLine("void equalsVerifier() throws Exception {");
        indentLevel++;

        appendLine("TestUtil.equalsVerifier(" + persistClass + ".class);");

        if (!context.isEmbedded()) {
            appendLine(persistClass + " " + persistInstance + "1 = get" + persistClass + "Sample1();");
            appendLine(persistClass + " " + persistInstance + "2 = new " + persistClass + "();");
            appendLine("assertThat(" + persistInstance + "1).isNotEqualTo(" + persistInstance + "2);");
            appendLine();
            appendLine(persistInstance + "2.set" + context.getPrimaryKey().getNameCapitalized()
                + "(" + persistInstance + "1.get" + context.getPrimaryKey().getNameCapitalized() + "());");
            appendLine("assertThat(" + persistInstance + "1).isEqualTo(" + persistInstance + "2);");
            appendLine();
            appendLine(persistInstance + "2 = get" + persistClass + "Sample2();");
            appendLine("assertThat(" + persistInstance + "1).isNotEqualTo(" + persistInstance + "2);");
        }

        indentLevel--;
        appendLine("}");
    }

    private void generateHashCodeVerifierTest() {
        if (context.isUpdatableEntity()) {
            return;
        }

        appendLine();
        String persistClass = context.getPersistClass();
        String persistInstance = context.getPersistInstance();

        appendLine("@Test");
        appendLine("void hashCodeVerifier() {");
        indentLevel++;

        appendLine(persistClass + " " + persistInstance + " = new " + persistClass + "();");
        appendLine("assertThat(" + persistInstance + ".hashCode()).isZero();");
        appendLine();
        appendLine(persistClass + " " + persistInstance + "1 = get" + persistClass + "Sample1();");
        appendLine(persistInstance + ".set" + context.getPrimaryKey().getNameCapitalized()
            + "(" + persistInstance + "1.get" + context.getPrimaryKey().getNameCapitalized() + "());");
        appendLine("assertThat(" + persistInstance + ").hasSameHashCodeAs(" + persistInstance + "1);");

        indentLevel--;
        appendLine("}");
    }

    private void generateRelationshipTests() {
        List<RelationshipContext> testableRelationships = getTestableRelationships();

        for (RelationshipContext relationship : testableRelationships) {
            generateRelationshipTest(relationship);
        }
    }

    private void generateRelationshipTest(RelationshipContext relationship) {
        appendLine();
        String persistClass = context.getPersistClass();
        String persistInstance = context.getPersistInstance();
        EntityContext otherEntity = relationship.getOtherEntity();
        String otherPersistClass = otherEntity != null ? otherEntity.getPersistClass() : relationship.getOtherEntityName();
        String otherPersistInstance = otherEntity != null ? otherEntity.getPersistInstance() : decapitalize(relationship.getOtherEntityName());

        appendLine("@Test");
        appendLine("void " + relationship.getRelationshipName() + "Test() {");
        indentLevel++;

        appendLine(persistClass + " " + persistInstance + " = get" + persistClass + "RandomSampleGenerator();");
        appendLine(otherPersistClass + " " + otherPersistInstance + "Back = get" + otherPersistClass + "RandomSampleGenerator();");

        if (relationship.isCollection()) {
            generateCollectionRelationshipTest(relationship, persistInstance, otherPersistInstance, otherPersistClass);
        } else {
            generateSingleRelationshipTest(relationship, persistInstance, otherPersistInstance);
        }

        indentLevel--;
        appendLine("}");
    }

    private void generateCollectionRelationshipTest(RelationshipContext relationship, String persistInstance,
            String otherPersistInstance, String otherPersistClass) {
        EntityContext otherEntity = relationship.getOtherEntity();
        String propertyNameCapitalized = capitalize(relationship.getPropertyName());

        appendLine();
        appendLine(persistInstance + ".add" + relationship.getRelationshipNameCapitalized() + "(" + otherPersistInstance + "Back);");
        appendLine("assertThat(" + persistInstance + ".get" + propertyNameCapitalized + "()).containsOnly(" + otherPersistInstance + "Back);");

        // Back reference check
        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()
                && otherEntity != null && !otherEntity.isEmbedded()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            String otherPropertyNameCapitalized = capitalize(otherRel.getPropertyName());
            if (otherRel.isCollection()) {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).containsOnly(" + persistInstance + ");");
            } else {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).isEqualTo(" + persistInstance + ");");
            }
        }

        appendLine();
        appendLine(persistInstance + ".remove" + relationship.getRelationshipNameCapitalized() + "(" + otherPersistInstance + "Back);");
        appendLine("assertThat(" + persistInstance + ".get" + propertyNameCapitalized + "()).doesNotContain(" + otherPersistInstance + "Back);");

        // Back reference check after removal
        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()
                && otherEntity != null && !otherEntity.isEmbedded()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            String otherPropertyNameCapitalized = capitalize(otherRel.getPropertyName());
            if (otherRel.isCollection()) {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).doesNotContain(" + persistInstance + ");");
            } else {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).isNull();");
            }
        }

        appendLine();
        if (context.isFluentMethods()) {
            appendLine(persistInstance + "." + relationship.getPropertyName() + "(new HashSet<>(Set.of(" + otherPersistInstance + "Back)));");
        } else {
            appendLine(persistInstance + ".set" + propertyNameCapitalized + "(new HashSet<>(Set.of(" + otherPersistInstance + "Back)));");
        }
        appendLine("assertThat(" + persistInstance + ".get" + propertyNameCapitalized + "()).containsOnly(" + otherPersistInstance + "Back);");

        // Back reference check after set
        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()
                && otherEntity != null && !otherEntity.isEmbedded()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            String otherPropertyNameCapitalized = capitalize(otherRel.getPropertyName());
            if (otherRel.isCollection()) {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).containsOnly(" + persistInstance + ");");
            } else {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).isEqualTo(" + persistInstance + ");");
            }
        }

        appendLine();
        appendLine(persistInstance + ".set" + propertyNameCapitalized + "(new HashSet<>());");
        appendLine("assertThat(" + persistInstance + ".get" + propertyNameCapitalized + "()).doesNotContain(" + otherPersistInstance + "Back);");

        // Back reference check after clear
        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()
                && otherEntity != null && !otherEntity.isEmbedded()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            String otherPropertyNameCapitalized = capitalize(otherRel.getPropertyName());
            if (otherRel.isCollection()) {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).doesNotContain(" + persistInstance + ");");
            } else {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).isNull();");
            }
        }
    }

    private void generateSingleRelationshipTest(RelationshipContext relationship, String persistInstance,
            String otherPersistInstance) {
        EntityContext otherEntity = relationship.getOtherEntity();
        String propertyNameCapitalized = capitalize(relationship.getPropertyName());

        appendLine();
        appendLine(persistInstance + ".set" + propertyNameCapitalized + "(" + otherPersistInstance + "Back);");
        appendLine("assertThat(" + persistInstance + ".get" + propertyNameCapitalized + "()).isEqualTo(" + otherPersistInstance + "Back);");

        // Back reference check
        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()
                && otherEntity != null && !otherEntity.isEmbedded()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            String otherPropertyNameCapitalized = capitalize(otherRel.getPropertyName());
            if (otherRel.isCollection()) {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).containsOnly(" + persistInstance + ");");
            } else {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).isEqualTo(" + persistInstance + ");");
            }
        }

        appendLine();
        if (context.isFluentMethods()) {
            appendLine(persistInstance + "." + relationship.getPropertyName() + "(null);");
        } else {
            appendLine(persistInstance + ".set" + propertyNameCapitalized + "(null);");
        }
        appendLine("assertThat(" + persistInstance + ".get" + propertyNameCapitalized + "()).isNull();");

        // Back reference check after clear
        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()
                && otherEntity != null && !otherEntity.isEmbedded()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            String otherPropertyNameCapitalized = capitalize(otherRel.getPropertyName());
            if (otherRel.isCollection()) {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).doesNotContain(" + persistInstance + ");");
            } else {
                appendLine("assertThat(" + otherPersistInstance + "Back.get" + otherPropertyNameCapitalized + "()).isNull();");
            }
        }
    }

    private void generateClassClose() {
        indentLevel--;
        appendLine("}");
    }

    // Helper methods

    private List<EntityContext> getOtherEntitiesForTest() {
        return context.getOtherEntities().stream()
            .filter(e -> !e.isBuiltIn())
            .collect(Collectors.toList());
    }

    private List<EntityContext> getOtherEntitiesWithDifferentPackage() {
        return context.getOtherEntities().stream()
            .filter(e -> !context.getEntityPackage().equals(e.getEntityPackage()))
            .collect(Collectors.toList());
    }

    private boolean hasCollectionRelationships() {
        return context.getRelationships().stream().anyMatch(RelationshipContext::isCollection);
    }

    private List<RelationshipContext> getTestableRelationships() {
        return context.getRelationships().stream()
            .filter(rel -> {
                EntityContext otherEntity = rel.getOtherEntity();
                if (otherEntity == null || otherEntity.isBuiltIn()) {
                    return false;
                }
                if (context.isEmbedded()) {
                    return otherEntity.isEmbedded() && rel.isOwnerSide();
                }
                return true;
            })
            .collect(Collectors.toList());
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

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
