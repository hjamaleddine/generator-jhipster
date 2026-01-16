/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.template;

import java.util.*;

/**
 * Fluent builder for generating Java source code.
 * Provides a programmatic way to build Java classes.
 */
public class JavaCodeBuilder {

    private final StringBuilder code = new StringBuilder();
    private final Set<String> imports = new LinkedHashSet<>();
    private String packageName;
    private int indentLevel = 0;
    private static final String INDENT = "    ";

    public JavaCodeBuilder() {
    }

    // ==================== Package & Imports ====================

    public JavaCodeBuilder packageDeclaration(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public JavaCodeBuilder addImport(String importClass) {
        if (importClass != null && !importClass.startsWith("java.lang.")) {
            imports.add(importClass);
        }
        return this;
    }

    public JavaCodeBuilder addImports(String... importClasses) {
        for (String imp : importClasses) {
            addImport(imp);
        }
        return this;
    }

    public JavaCodeBuilder addImports(Collection<String> importClasses) {
        for (String imp : importClasses) {
            addImport(imp);
        }
        return this;
    }

    public JavaCodeBuilder addStaticImport(String importClass) {
        imports.add("static " + importClass);
        return this;
    }

    // ==================== Class/Interface Declaration ====================

    public JavaCodeBuilder classDeclaration(String modifiers, String className, String extendsClass, String... interfaces) {
        StringBuilder decl = new StringBuilder();
        decl.append(modifiers).append(" class ").append(className);

        if (extendsClass != null && !extendsClass.isEmpty()) {
            decl.append(" extends ").append(extendsClass);
        }

        if (interfaces != null && interfaces.length > 0) {
            decl.append(" implements ").append(String.join(", ", interfaces));
        }

        decl.append(" {");
        line(decl.toString());
        indent();
        line();
        return this;
    }

    public JavaCodeBuilder interfaceDeclaration(String modifiers, String interfaceName, String... extendsInterfaces) {
        StringBuilder decl = new StringBuilder();
        decl.append(modifiers).append(" interface ").append(interfaceName);

        if (extendsInterfaces != null && extendsInterfaces.length > 0) {
            decl.append(" extends ").append(String.join(", ", extendsInterfaces));
        }

        decl.append(" {");
        line(decl.toString());
        indent();
        line();
        return this;
    }

    public JavaCodeBuilder enumDeclaration(String modifiers, String enumName) {
        line(modifiers + " enum " + enumName + " {");
        indent();
        return this;
    }

    public JavaCodeBuilder closeClass() {
        outdent();
        line("}");
        return this;
    }

    // ==================== Annotations ====================

    public JavaCodeBuilder annotation(String annotation) {
        line("@" + annotation);
        return this;
    }

    public JavaCodeBuilder annotation(String annotation, String value) {
        line("@" + annotation + "(" + value + ")");
        return this;
    }

    public JavaCodeBuilder annotation(String annotation, Map<String, String> attributes) {
        if (attributes.isEmpty()) {
            return annotation(annotation);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("@").append(annotation).append("(");

        List<String> attrs = new ArrayList<>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attrs.add(entry.getKey() + " = " + entry.getValue());
        }
        sb.append(String.join(", ", attrs));
        sb.append(")");
        line(sb.toString());
        return this;
    }

    // ==================== Fields ====================

    /**
     * Field declaration with combined modifiers and type.
     * Example: field("private String", "name")
     */
    public JavaCodeBuilder field(String modifiersAndType, String name) {
        line(modifiersAndType + " " + name + ";");
        return this;
    }

    public JavaCodeBuilder field(String modifiers, String type, String name) {
        line(modifiers + " " + type + " " + name + ";");
        return this;
    }

    public JavaCodeBuilder field(String modifiers, String type, String name, String initialValue) {
        line(modifiers + " " + type + " " + name + " = " + initialValue + ";");
        return this;
    }

    public JavaCodeBuilder staticFinalField(String type, String name, String value) {
        return field("private static final", type, name, value);
    }

    public JavaCodeBuilder publicStaticFinalField(String type, String name, String value) {
        return field("public static final", type, name, value);
    }

    // ==================== Methods ====================

    public JavaCodeBuilder methodSignature(String modifiers, String returnType, String name, String... parameters) {
        StringBuilder sig = new StringBuilder();
        sig.append(modifiers);
        if (!modifiers.isEmpty()) sig.append(" ");
        sig.append(returnType).append(" ").append(name).append("(");
        sig.append(String.join(", ", parameters));
        sig.append(") {");
        line(sig.toString());
        indent();
        return this;
    }

    public JavaCodeBuilder abstractMethod(String modifiers, String returnType, String name, String... parameters) {
        StringBuilder sig = new StringBuilder();
        sig.append(modifiers).append(" ").append(returnType).append(" ").append(name).append("(");
        sig.append(String.join(", ", parameters));
        sig.append(");");
        line(sig.toString());
        return this;
    }

    public JavaCodeBuilder constructor(String modifiers, String className, String... parameters) {
        StringBuilder sig = new StringBuilder();
        sig.append(modifiers);
        if (!modifiers.isEmpty()) sig.append(" ");
        sig.append(className).append("(");
        sig.append(String.join(", ", parameters));
        sig.append(") {");
        line(sig.toString());
        indent();
        return this;
    }

    public JavaCodeBuilder closeMethod() {
        outdent();
        line("}");
        return this;
    }

    // ==================== Statements ====================

    public JavaCodeBuilder statement(String statement) {
        line(statement + ";");
        return this;
    }

    public JavaCodeBuilder returnStatement(String expression) {
        if (expression == null || expression.isEmpty()) {
            line("return;");
        } else {
            line("return " + expression + ";");
        }
        return this;
    }

    public JavaCodeBuilder assignStatement(String variable, String expression) {
        line(variable + " = " + expression + ";");
        return this;
    }

    public JavaCodeBuilder ifStatement(String condition) {
        line("if (" + condition + ") {");
        indent();
        return this;
    }

    public JavaCodeBuilder elseIfStatement(String condition) {
        outdent();
        line("} else if (" + condition + ") {");
        indent();
        return this;
    }

    public JavaCodeBuilder elseStatement() {
        outdent();
        line("} else {");
        indent();
        return this;
    }

    public JavaCodeBuilder closeIf() {
        outdent();
        line("}");
        return this;
    }

    public JavaCodeBuilder forLoop(String init, String condition, String increment) {
        line("for (" + init + "; " + condition + "; " + increment + ") {");
        indent();
        return this;
    }

    public JavaCodeBuilder forEachLoop(String type, String variable, String iterable) {
        line("for (" + type + " " + variable + " : " + iterable + ") {");
        indent();
        return this;
    }

    public JavaCodeBuilder closeLoop() {
        outdent();
        line("}");
        return this;
    }

    /**
     * Simplified for statement that takes a complete for clause.
     * Example: forStatement("int i = 0; i < 10; i++") or forStatement("Map.Entry entry : map.entrySet()")
     */
    public JavaCodeBuilder forStatement(String forClause) {
        line("for (" + forClause + ") {");
        indent();
        return this;
    }

    /**
     * Closes a for block (alias for closeLoop).
     */
    public JavaCodeBuilder closeFor() {
        return closeLoop();
    }

    public JavaCodeBuilder tryBlock() {
        line("try {");
        indent();
        return this;
    }

    /**
     * Catch block with combined exception type and variable.
     * Example: catchBlock("Exception e") or catchBlock("IOException | SQLException ex")
     */
    public JavaCodeBuilder catchBlock(String exceptionAndVariable) {
        outdent();
        line("} catch (" + exceptionAndVariable + ") {");
        indent();
        return this;
    }

    public JavaCodeBuilder catchBlock(String exceptionType, String variable) {
        outdent();
        line("} catch (" + exceptionType + " " + variable + ") {");
        indent();
        return this;
    }

    public JavaCodeBuilder finallyBlock() {
        outdent();
        line("} finally {");
        indent();
        return this;
    }

    public JavaCodeBuilder closeTryCatch() {
        outdent();
        line("}");
        return this;
    }

    /**
     * Closes a try block (alias for closeTryCatch).
     */
    public JavaCodeBuilder closeTry() {
        return closeTryCatch();
    }

    // ==================== Comments & Javadoc ====================

    public JavaCodeBuilder javadoc(String... lines) {
        line("/**");
        for (String docLine : lines) {
            line(" * " + docLine);
        }
        line(" */");
        return this;
    }

    public JavaCodeBuilder javadocWithParams(String description, Map<String, String> params, String returnDesc) {
        line("/**");
        line(" * " + description);
        if (!params.isEmpty()) {
            line(" *");
            for (Map.Entry<String, String> param : params.entrySet()) {
                line(" * @param " + param.getKey() + " " + param.getValue());
            }
        }
        if (returnDesc != null) {
            line(" * @return " + returnDesc);
        }
        line(" */");
        return this;
    }

    public JavaCodeBuilder lineComment(String comment) {
        line("// " + comment);
        return this;
    }

    public JavaCodeBuilder blockComment(String... lines) {
        line("/*");
        for (String commentLine : lines) {
            line(" * " + commentLine);
        }
        line(" */");
        return this;
    }

    // ==================== Raw content ====================

    public JavaCodeBuilder line() {
        code.append("\n");
        return this;
    }

    public JavaCodeBuilder line(String content) {
        code.append(getIndent()).append(content).append("\n");
        return this;
    }

    public JavaCodeBuilder lines(String... contentLines) {
        for (String contentLine : contentLines) {
            line(contentLine);
        }
        return this;
    }

    public JavaCodeBuilder raw(String content) {
        code.append(content);
        return this;
    }

    public JavaCodeBuilder indent() {
        indentLevel++;
        return this;
    }

    public JavaCodeBuilder outdent() {
        if (indentLevel > 0) {
            indentLevel--;
        }
        return this;
    }

    // ==================== Build ====================

    public String build() {
        StringBuilder result = new StringBuilder();

        // License header
        result.append("/*\n");
        result.append(" * Copyright 2013-2025 the original author or authors from the JHipster project.\n");
        result.append(" *\n");
        result.append(" * Licensed under the Apache License, Version 2.0 (the \"License\");\n");
        result.append(" * you may not use this file except in compliance with the License.\n");
        result.append(" * You may obtain a copy of the License at\n");
        result.append(" *\n");
        result.append(" *      https://www.apache.org/licenses/LICENSE-2.0\n");
        result.append(" */\n");

        // Package
        if (packageName != null) {
            result.append("package ").append(packageName).append(";\n\n");
        }

        // Imports
        if (!imports.isEmpty()) {
            List<String> staticImports = new ArrayList<>();
            List<String> regularImports = new ArrayList<>();

            for (String imp : imports) {
                if (imp.startsWith("static ")) {
                    staticImports.add(imp);
                } else {
                    regularImports.add(imp);
                }
            }

            // Static imports first
            for (String imp : staticImports) {
                result.append("import ").append(imp).append(";\n");
            }
            if (!staticImports.isEmpty() && !regularImports.isEmpty()) {
                result.append("\n");
            }

            // Regular imports grouped by package
            Collections.sort(regularImports);
            String lastPrefix = "";
            for (String imp : regularImports) {
                String prefix = imp.contains(".") ? imp.substring(0, imp.indexOf('.')) : imp;
                if (!lastPrefix.isEmpty() && !prefix.equals(lastPrefix)) {
                    result.append("\n");
                }
                result.append("import ").append(imp).append(";\n");
                lastPrefix = prefix;
            }
            result.append("\n");
        }

        // Code
        result.append(code);

        return result.toString();
    }

    private String getIndent() {
        return INDENT.repeat(indentLevel);
    }

    // ==================== Utility Methods ====================

    public static String quote(String value) {
        return "\"" + escapeString(value) + "\"";
    }

    public static String escapeString(String value) {
        if (value == null) return null;
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String decapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
