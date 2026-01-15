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
import io.github.jhipster.generator.domain.model.RelationshipContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates the main JPA entity class.
 * This is the Java equivalent of _persistClass_.java.jhi.ejs template.
 */
public class EntityClassGenerator {

    private final EntityContext context;
    private final StringBuilder sb;
    private int indentLevel = 0;
    private static final String INDENT = "    ";

    public EntityClassGenerator(EntityContext context) {
        this.context = context;
        this.sb = new StringBuilder();
    }

    /**
     * Generates the complete entity class source code.
     *
     * @return the generated Java source code
     */
    public String generate() {
        sb.setLength(0);

        generatePackageDeclaration();
        generateImports();
        generateClassJavadoc();
        generateClassAnnotations();
        generateClassDeclaration();
        generateFields();
        generateRelationshipFields();
        generateFieldMethods();
        generateRelationshipMethods();
        generateEqualsHashCodeToString();
        generateClassClose();

        return sb.toString();
    }

    private void generatePackageDeclaration() {
        appendLine("package " + context.getEntityAbsolutePackage() + ".domain;");
        appendLine();
    }

    private void generateImports() {
        Set<String> imports = new LinkedHashSet<>();

        // Jackson imports
        if (context.isRelationshipsContainOtherSideIgnore()) {
            imports.add("com.fasterxml.jackson.annotation.JsonIgnoreProperties");
        }

        // Swagger imports
        if (!context.isDtoMapstruct() && (context.getEntityApiDescription() != null || context.isImportApiModelProperty())) {
            imports.add("io.swagger.v3.oas.annotations.media.Schema");
        }

        // Java imports
        imports.add("java.io.Serializable");

        if (context.isAnyFieldIsBigDecimal()) {
            imports.add("java.math.BigDecimal");
        }
        if (context.isAnyFieldIsInstant()) {
            imports.add("java.time.Instant");
        }
        if (context.isAnyFieldIsLocalDate()) {
            imports.add("java.time.LocalDate");
        }
        if (context.isAnyFieldIsZonedDateTime()) {
            imports.add("java.time.ZonedDateTime");
        }
        if (context.isAnyFieldIsLocalTime()) {
            imports.add("java.time.LocalTime");
        }
        if (context.isAnyFieldIsDuration()) {
            imports.add("java.time.Duration");
        }
        if (context.isEntityContainsCollectionField()) {
            imports.add("java.util.HashSet");
            imports.add("java.util.Set");
        }
        if (context.isAnyFieldIsUUID() || context.isOtherEntityPrimaryKeyTypesIncludesUUID()) {
            imports.add("java.util.UUID");
        }
        if (!context.isUpdatableEntity()) {
            imports.add("java.util.Objects");
        }

        // Write imports
        for (String imp : imports) {
            appendLine("import " + imp + ";");
        }

        // Enum imports
        for (String enumName : context.getUniqueEnums().keySet()) {
            appendLine();
            appendLine("import " + context.getEntityAbsolutePackage() + ".domain.enumeration." + enumName + ";");
        }

        // Other entity imports
        for (EntityContext otherEntity : context.getOtherEntities()) {
            if (!otherEntity.getEntityPackage().equals(context.getEntityPackage())) {
                appendLine("import " + otherEntity.getEntityAbsoluteClass() + ";");
            }
        }

        appendLine();
    }

    private void generateClassJavadoc() {
        if (context.getEntityJavadoc() != null) {
            appendLine(context.getEntityJavadoc());
        } else {
            appendLine("/**");
            appendLine(" * A " + context.getPersistClass() + ".");
            appendLine(" */");
        }
    }

    private void generateClassAnnotations() {
        if (!context.isDtoMapstruct() && context.getEntityApiDescription() != null) {
            appendLine("@Schema(description = \"" + escapeJava(context.getEntityApiDescription()) + "\")");
        }
        appendLine("@SuppressWarnings(\"common-java:DuplicatedBlocks\")");
    }

    private void generateClassDeclaration() {
        StringBuilder decl = new StringBuilder();
        decl.append("public class ").append(context.getPersistClass());
        decl.append(" implements Serializable");
        decl.append(" {");
        appendLine(decl.toString());
        appendLine();
        indentLevel++;

        appendLine("private static final long serialVersionUID = 1L;");
        appendLine();
    }

    private void generateFields() {
        // Primary key for composite keys
        if (!context.isEmbedded() && context.getPrimaryKey() != null && context.getPrimaryKey().isComposite()) {
            appendLine("@Id");
            appendLine("private " + context.getPrimaryKey().getType() + " " + context.getPrimaryKey().getName() + ";");
            appendLine();
        }

        // Regular fields
        for (FieldContext field : context.getFields()) {
            if (field.isJavaInherited() || field.isTransientField()) {
                continue;
            }
            if (!context.isEmbedded() && field.isId() && context.getPrimaryKey() != null && context.getPrimaryKey().isComposite()) {
                continue;
            }

            generateFieldDeclaration(field);
        }
    }

    private void generateFieldDeclaration(FieldContext field) {
        // Field Javadoc
        if (field.getFieldJavadoc() != null) {
            appendLine(field.getFieldJavadoc());
        }

        // Schema annotation
        if (!context.isDtoMapstruct() && field.getFieldApiDescription() != null) {
            String schemaAttr = field.isFieldValidationRequired() ? ", required = true" : "";
            appendLine("@Schema(description = \"" + escapeJava(field.getFieldApiDescription()) + "\"" + schemaAttr + ")");
        }

        // Field declaration
        appendLine("private " + field.getJavaFieldType() + " " + field.getFieldName() + ";");
        appendLine();

        // Content type field for blobs
        if (field.isFieldWithContentType()) {
            appendLine("private String " + field.getFieldName() + "ContentType;");
            appendLine();
        }
    }

    private void generateRelationshipFields() {
        for (RelationshipContext relationship : context.getRelationships()) {
            generateRelationshipFieldDeclaration(relationship);
        }

        appendLine("// jhipster-needle-entity-add-field - JHipster will add fields here");
    }

    private void generateRelationshipFieldDeclaration(RelationshipContext relationship) {
        // Javadoc
        if (relationship.getRelationshipJavadoc() != null) {
            appendLine(relationship.getRelationshipJavadoc());
        }

        // Schema annotation
        if (!context.isDtoMapstruct() && relationship.getRelationshipApiDescription() != null) {
            appendLine("@Schema(description = \"" + escapeJava(relationship.getRelationshipApiDescription()) + "\")");
        }

        // JsonIgnoreProperties
        if (relationship.isIgnoreOtherSideProperty() && relationship.getOtherEntity() != null) {
            appendLine("@JsonIgnoreProperties(value = {");
            List<RelationshipContext> otherRels = relationship.getOtherEntity().getRelationships();
            for (int i = 0; i < otherRels.size(); i++) {
                String comma = (i < otherRels.size() - 1) ? "," : "";
                appendLine("    \"" + otherRels.get(i).getRelationshipReferenceField() + "\"" + comma);
            }
            appendLine("}, allowSetters = true)");
        }

        // Field declaration
        String otherEntityClass = relationship.getOtherEntity() != null
            ? relationship.getOtherEntity().getPersistClass()
            : relationship.getOtherEntityName();

        if (relationship.isCollection()) {
            String fieldName = relationship.getRelationshipFieldNamePlural();
            appendLine("private Set<" + otherEntityClass + "> " + fieldName + " = new HashSet<>();");
        } else {
            appendLine("private " + otherEntityClass + " " + relationship.getRelationshipFieldName() + ";");
        }
        appendLine();
    }

    private void generateFieldMethods() {
        for (FieldContext field : context.getFields()) {
            if (field.isTransientField()) {
                continue;
            }
            if (!context.isEmbedded() && field.isId() && context.getPrimaryKey() != null && context.getPrimaryKey().isComposite()) {
                continue;
            }

            generateFieldGetter(field);
            if (context.isFluentMethods()) {
                generateFieldFluentSetter(field);
            }
            generateFieldSetter(field);

            if (field.isFieldWithContentType()) {
                generateContentTypeAccessors(field);
            }
        }
        appendLine();
    }

    private void generateFieldGetter(FieldContext field) {
        appendLine("public " + field.getJavaFieldType() + " get" + field.getFieldInJavaBeanMethod() + "() {");
        indentLevel++;
        appendLine("return this." + field.getFieldName() + ";");
        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateFieldFluentSetter(FieldContext field) {
        appendLine("public " + context.getPersistClass() + " " + field.getFieldName() + "(" + field.getJavaFieldType() + " " + field.getFieldName() + ") {");
        indentLevel++;
        appendLine("this.set" + field.getFieldInJavaBeanMethod() + "(" + field.getFieldName() + ");");
        appendLine("return this;");
        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateFieldSetter(FieldContext field) {
        appendLine("public void set" + field.getFieldInJavaBeanMethod() + "(" + field.getJavaFieldType() + " " + field.getFieldName() + ") {");
        indentLevel++;
        appendLine("this." + field.getFieldName() + " = " + field.getFieldName() + ";");
        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateContentTypeAccessors(FieldContext field) {
        // Getter
        appendLine("public String get" + field.getFieldInJavaBeanMethod() + "ContentType() {");
        indentLevel++;
        appendLine("return this." + field.getFieldName() + "ContentType;");
        indentLevel--;
        appendLine("}");
        appendLine();

        // Fluent setter
        if (context.isFluentMethods()) {
            appendLine("public " + context.getPersistClass() + " " + field.getFieldName() + "ContentType(String " + field.getFieldName() + "ContentType) {");
            indentLevel++;
            appendLine("this." + field.getFieldName() + "ContentType = " + field.getFieldName() + "ContentType;");
            appendLine("return this;");
            indentLevel--;
            appendLine("}");
            appendLine();
        }

        // Setter
        appendLine("public void set" + field.getFieldInJavaBeanMethod() + "ContentType(String " + field.getFieldName() + "ContentType) {");
        indentLevel++;
        appendLine("this." + field.getFieldName() + "ContentType = " + field.getFieldName() + "ContentType;");
        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateRelationshipMethods() {
        for (RelationshipContext relationship : context.getRelationships()) {
            generateRelationshipGetter(relationship);
            generateRelationshipSetter(relationship);

            if (context.isFluentMethods()) {
                generateRelationshipFluentMethods(relationship);
            }
        }

        appendLine("// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here");
        appendLine();
    }

    private void generateRelationshipGetter(RelationshipContext relationship) {
        String otherEntityClass = relationship.getOtherEntity() != null
            ? relationship.getOtherEntity().getPersistClass()
            : relationship.getOtherEntityName();

        if (relationship.isCollection()) {
            appendLine("public Set<" + otherEntityClass + "> get" + relationship.getRelationshipNameCapitalizedPlural() + "() {");
            indentLevel++;
            appendLine("return this." + relationship.getRelationshipFieldNamePlural() + ";");
            indentLevel--;
            appendLine("}");
        } else {
            appendLine("public " + otherEntityClass + " get" + relationship.getRelationshipNameCapitalized() + "() {");
            indentLevel++;
            appendLine("return this." + relationship.getRelationshipFieldName() + ";");
            indentLevel--;
            appendLine("}");
        }
        appendLine();
    }

    private void generateRelationshipSetter(RelationshipContext relationship) {
        String otherEntityClass = relationship.getOtherEntity() != null
            ? relationship.getOtherEntity().getPersistClass()
            : relationship.getOtherEntityName();

        if (relationship.isCollection()) {
            appendLine("public void set" + relationship.getRelationshipNameCapitalizedPlural() + "(Set<" + otherEntityClass + "> " + relationship.getOtherEntityNamePlural() + ") {");
            indentLevel++;

            // Back reference handling
            if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()) {
                generateBackReferenceUpdateForCollection(relationship);
            }

            appendLine("this." + relationship.getRelationshipFieldNamePlural() + " = " + relationship.getOtherEntityNamePlural() + ";");
            indentLevel--;
            appendLine("}");
        } else {
            appendLine("public void set" + relationship.getRelationshipNameCapitalized() + "(" + otherEntityClass + " " + relationship.getOtherEntityName() + ") {");
            indentLevel++;

            // Back reference handling
            if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()) {
                generateBackReferenceUpdateForSingle(relationship);
            }

            appendLine("this." + relationship.getRelationshipFieldName() + " = " + relationship.getOtherEntityName() + ";");
            indentLevel--;
            appendLine("}");
        }
        appendLine();
    }

    private void generateBackReferenceUpdateForCollection(RelationshipContext relationship) {
        RelationshipContext otherRel = relationship.getOtherRelationship();
        String fieldNamePlural = relationship.getRelationshipFieldNamePlural();
        String otherEntityNamePlural = relationship.getOtherEntityNamePlural();

        appendLine("if (this." + fieldNamePlural + " != null) {");
        indentLevel++;
        if (otherRel.isCollection()) {
            appendLine("this." + fieldNamePlural + ".forEach(i -> i.remove" + otherRel.getRelationshipNameCapitalized() + "(this));");
        } else {
            appendLine("this." + fieldNamePlural + ".forEach(i -> i.set" + otherRel.getRelationshipNameCapitalized() + "(null));");
        }
        indentLevel--;
        appendLine("}");

        appendLine("if (" + otherEntityNamePlural + " != null) {");
        indentLevel++;
        if (otherRel.isCollection()) {
            appendLine(otherEntityNamePlural + ".forEach(i -> i.add" + otherRel.getRelationshipNameCapitalized() + "(this));");
        } else {
            appendLine(otherEntityNamePlural + ".forEach(i -> i.set" + otherRel.getRelationshipNameCapitalized() + "(this));");
        }
        indentLevel--;
        appendLine("}");
    }

    private void generateBackReferenceUpdateForSingle(RelationshipContext relationship) {
        RelationshipContext otherRel = relationship.getOtherRelationship();
        String fieldName = relationship.getRelationshipFieldName();
        String otherEntityName = relationship.getOtherEntityName();

        appendLine("if (this." + fieldName + " != null) {");
        indentLevel++;
        if (otherRel.isCollection()) {
            appendLine("this." + fieldName + ".remove" + otherRel.getRelationshipNameCapitalized() + "(this);");
        } else {
            appendLine("this." + fieldName + ".set" + otherRel.getRelationshipNameCapitalized() + "(null);");
        }
        indentLevel--;
        appendLine("}");

        appendLine("if (" + otherEntityName + " != null) {");
        indentLevel++;
        if (otherRel.isCollection()) {
            appendLine(otherEntityName + ".add" + otherRel.getRelationshipNameCapitalized() + "(this);");
        } else {
            appendLine(otherEntityName + ".set" + otherRel.getRelationshipNameCapitalized() + "(this);");
        }
        indentLevel--;
        appendLine("}");
    }

    private void generateRelationshipFluentMethods(RelationshipContext relationship) {
        String otherEntityClass = relationship.getOtherEntity() != null
            ? relationship.getOtherEntity().getPersistClass()
            : relationship.getOtherEntityName();

        if (relationship.isCollection()) {
            // Fluent collection setter
            appendLine("public " + context.getPersistClass() + " " + relationship.getRelationshipFieldNamePlural() + "(Set<" + otherEntityClass + "> " + relationship.getOtherEntityNamePlural() + ") {");
            indentLevel++;
            appendLine("this.set" + relationship.getRelationshipNameCapitalizedPlural() + "(" + relationship.getOtherEntityNamePlural() + ");");
            appendLine("return this;");
            indentLevel--;
            appendLine("}");
            appendLine();

            // Add method
            generateAddMethod(relationship, otherEntityClass);

            // Remove method
            generateRemoveMethod(relationship, otherEntityClass);
        } else {
            appendLine("public " + context.getPersistClass() + " " + relationship.getRelationshipFieldName() + "(" + otherEntityClass + " " + relationship.getOtherEntityName() + ") {");
            indentLevel++;
            appendLine("this.set" + relationship.getRelationshipNameCapitalized() + "(" + relationship.getOtherEntityName() + ");");
            appendLine("return this;");
            indentLevel--;
            appendLine("}");
            appendLine();
        }
    }

    private void generateAddMethod(RelationshipContext relationship, String otherEntityClass) {
        String otherEntityName = relationship.getOtherEntityName();

        appendLine("public " + context.getPersistClass() + " add" + relationship.getRelationshipNameCapitalized() + "(" + otherEntityClass + " " + otherEntityName + ") {");
        indentLevel++;
        appendLine("this." + relationship.getRelationshipFieldNamePlural() + ".add(" + otherEntityName + ");");

        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            if (otherRel.isCollection()) {
                appendLine(otherEntityName + ".get" + otherRel.getRelationshipNameCapitalizedPlural() + "().add(this);");
            } else {
                appendLine(otherEntityName + ".set" + otherRel.getRelationshipNameCapitalized() + "(this);");
            }
        }

        appendLine("return this;");
        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateRemoveMethod(RelationshipContext relationship, String otherEntityClass) {
        String otherEntityName = relationship.getOtherEntityName();

        appendLine("public " + context.getPersistClass() + " remove" + relationship.getRelationshipNameCapitalized() + "(" + otherEntityClass + " " + otherEntityName + ") {");
        indentLevel++;
        appendLine("this." + relationship.getRelationshipFieldNamePlural() + ".remove(" + otherEntityName + ");");

        if (relationship.getOtherRelationship() != null && relationship.isRelationshipUpdateBackReference()) {
            RelationshipContext otherRel = relationship.getOtherRelationship();
            if (otherRel.isCollection()) {
                appendLine(otherEntityName + ".get" + otherRel.getRelationshipNameCapitalizedPlural() + "().remove(this);");
            } else {
                appendLine(otherEntityName + ".set" + otherRel.getRelationshipNameCapitalized() + "(null);");
            }
        }

        appendLine("return this;");
        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateEqualsHashCodeToString() {
        generateEquals();
        generateHashCode();
        generateToString();
    }

    private void generateEquals() {
        appendLine("@Override");
        appendLine("public boolean equals(Object o) {");
        indentLevel++;
        appendLine("if (this == o) {");
        indentLevel++;
        appendLine("return true;");
        indentLevel--;
        appendLine("}");
        appendLine("if (!(o instanceof " + context.getPersistClass() + ")) {");
        indentLevel++;
        appendLine("return false;");
        indentLevel--;
        appendLine("}");

        if (!context.isEmbedded() && context.getPrimaryKey() != null) {
            String pkGetter = "get" + context.getPrimaryKey().getNameCapitalized() + "()";
            appendLine("return " + pkGetter + " != null && " + pkGetter + ".equals(((" + context.getPersistClass() + ") o)." + pkGetter + ");");
        } else {
            appendLine("return false;");
        }

        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateHashCode() {
        appendLine("@Override");
        appendLine("public int hashCode() {");
        indentLevel++;

        if (context.isUpdatableEntity()) {
            appendLine("// see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/");
            appendLine("return getClass().hashCode();");
        } else if (context.getPrimaryKey() != null) {
            appendLine("return Objects.hashCode(get" + context.getPrimaryKey().getNameCapitalized() + "());");
        } else {
            appendLine("return getClass().hashCode();");
        }

        indentLevel--;
        appendLine("}");
        appendLine();
    }

    private void generateToString() {
        appendLine("// prettier-ignore");
        appendLine("@Override");
        appendLine("public String toString() {");
        indentLevel++;

        StringBuilder toStringBuilder = new StringBuilder();
        toStringBuilder.append("return \"").append(context.getPersistClass()).append("{\" +");

        // Primary key
        if (!context.isEmbedded() && context.getPrimaryKey() != null) {
            toStringBuilder.append("\n").append(getIndent()).append(INDENT)
                .append("\"").append(context.getPrimaryKey().getName()).append("=\" + get")
                .append(context.getPrimaryKey().getNameCapitalized()).append("() +");
        }

        // Fields
        for (FieldContext field : context.getFields()) {
            if (field.isId() || field.isTransientField()) {
                continue;
            }

            String quote = field.isFieldTypeNumeric() ? "" : "'";
            toStringBuilder.append("\n").append(getIndent()).append(INDENT)
                .append("\", ").append(field.getFieldName()).append("=").append(quote)
                .append("\" + get").append(field.getFieldInJavaBeanMethod()).append("()");
            if (!quote.isEmpty()) {
                toStringBuilder.append(" + \"'\"");
            }
            toStringBuilder.append(" +");

            if (field.isFieldWithContentType()) {
                toStringBuilder.append("\n").append(getIndent()).append(INDENT)
                    .append("\", ").append(field.getFieldName()).append("ContentType='\" + get")
                    .append(field.getFieldInJavaBeanMethod()).append("ContentType() + \"'\" +");
            }
        }

        toStringBuilder.append("\n").append(getIndent()).append(INDENT).append("\"}\";");

        appendLine(toStringBuilder.toString());
        indentLevel--;
        appendLine("}");
    }

    private void generateClassClose() {
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

    private String escapeJava(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
