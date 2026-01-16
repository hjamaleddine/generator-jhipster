/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.error;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Error Handling Generator.
 * Generates exception classes and error handling infrastructure.
 */
public class ErrorHandlingGenerator extends BaseApplicationGenerator {

    public ErrorHandlingGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "error-handling";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingErrorHandling", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing error handling infrastructure");

        writeErrorConstants();
        writeBadRequestAlertException();
        writeFieldErrorVM();
        writeExceptionTranslator();
        writeInvalidPasswordException();
        writeEmailAlreadyUsedException();
        writeUsernameAlreadyUsedException();
    }

    private void writeErrorConstants() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.errors");

        builder.addImport("java.net.URI");

        builder.javadoc("Constants for error handling.");

        builder.classDeclaration("public final", "ErrorConstants", null);

        builder.publicStaticFinalField("String", "ERR_CONCURRENCY_FAILURE", "\"error.concurrencyFailure\"");
        builder.publicStaticFinalField("String", "ERR_VALIDATION", "\"error.validation\"");
        builder.publicStaticFinalField("String", "PROBLEM_BASE_URL", "\"https://www.jhipster.tech/problem\"");
        builder.publicStaticFinalField("URI", "DEFAULT_TYPE", "URI.create(PROBLEM_BASE_URL + \"/problem-with-message\")");
        builder.publicStaticFinalField("URI", "CONSTRAINT_VIOLATION_TYPE", "URI.create(PROBLEM_BASE_URL + \"/constraint-violation\")");
        builder.publicStaticFinalField("URI", "ENTITY_NOT_FOUND_TYPE", "URI.create(PROBLEM_BASE_URL + \"/entity-not-found\")");
        builder.line();

        builder.constructor("private", "ErrorConstants");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/errors/ErrorConstants.java", builder.build());
    }

    private void writeBadRequestAlertException() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.errors");

        builder.addImports(
            "org.springframework.http.HttpStatus",
            "org.springframework.web.ErrorResponseException",
            "java.io.Serial",
            "java.net.URI"
        );

        builder.javadoc("Exception for bad request errors with alert information.");

        builder.classDeclaration("public", "BadRequestAlertException", "ErrorResponseException");

        builder.annotation("Serial");
        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.field("private final", "String", "entityName");
        builder.field("private final", "String", "errorKey");
        builder.line();

        // Constructor with all parameters
        builder.constructor("public", "BadRequestAlertException",
            "String defaultMessage", "String entityName", "String errorKey");
        builder.statement("this(ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey)");
        builder.closeMethod();
        builder.line();

        builder.constructor("public", "BadRequestAlertException",
            "URI type", "String defaultMessage", "String entityName", "String errorKey");
        builder.statement("super(HttpStatus.BAD_REQUEST)");
        builder.statement("this.entityName = entityName");
        builder.statement("this.errorKey = errorKey");
        builder.closeMethod();
        builder.line();

        // Getters
        builder.methodSignature("public", "String", "getEntityName");
        builder.returnStatement("entityName");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getErrorKey");
        builder.returnStatement("errorKey");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/errors/BadRequestAlertException.java", builder.build());
    }

    private void writeFieldErrorVM() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.errors");

        builder.addImport("java.io.Serializable");

        builder.javadoc("View Model for transferring field error information.");

        builder.classDeclaration("public", "FieldErrorVM", null, "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.field("private final", "String", "objectName");
        builder.field("private final", "String", "field");
        builder.field("private final", "String", "message");
        builder.line();

        builder.constructor("public", "FieldErrorVM", "String objectName", "String field", "String message");
        builder.statement("this.objectName = objectName");
        builder.statement("this.field = field");
        builder.statement("this.message = message");
        builder.closeMethod();
        builder.line();

        // Getters
        builder.methodSignature("public", "String", "getObjectName");
        builder.returnStatement("objectName");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getField");
        builder.returnStatement("field");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getMessage");
        builder.returnStatement("message");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/errors/FieldErrorVM.java", builder.build());
    }

    private void writeExceptionTranslator() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.errors");

        builder.addImports(
            "jakarta.servlet.http.HttpServletRequest",
            "org.springframework.dao.ConcurrencyFailureException",
            "org.springframework.dao.DataAccessException",
            "org.springframework.http.HttpHeaders",
            "org.springframework.http.HttpStatus",
            "org.springframework.http.HttpStatusCode",
            "org.springframework.http.ProblemDetail",
            "org.springframework.http.ResponseEntity",
            "org.springframework.lang.Nullable",
            "org.springframework.security.access.AccessDeniedException",
            "org.springframework.security.authentication.BadCredentialsException",
            "org.springframework.validation.BindingResult",
            "org.springframework.validation.FieldError",
            "org.springframework.web.ErrorResponse",
            "org.springframework.web.bind.MethodArgumentNotValidException",
            "org.springframework.web.bind.annotation.ControllerAdvice",
            "org.springframework.web.bind.annotation.ExceptionHandler",
            "org.springframework.web.context.request.NativeWebRequest",
            "org.springframework.web.context.request.WebRequest",
            "org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler",
            "java.net.URI",
            "java.util.List",
            "java.util.stream.Collectors"
        );

        builder.javadoc("Controller advice to translate server-side exceptions to client-friendly error responses.");
        builder.annotation("ControllerAdvice");

        builder.classDeclaration("public", "ExceptionTranslator", "ResponseEntityExceptionHandler");

        builder.staticFinalField("String", "FIELD_ERRORS_KEY", "\"fieldErrors\"");
        builder.staticFinalField("String", "MESSAGE_KEY", "\"message\"");
        builder.staticFinalField("String", "PATH_KEY", "\"path\"");
        builder.line();

        // handleMethodArgumentNotValid override
        builder.annotation("Override");
        builder.annotation("Nullable");
        builder.methodSignature("protected", "ResponseEntity<Object>", "handleMethodArgumentNotValid",
            "MethodArgumentNotValidException ex",
            "HttpHeaders headers",
            "HttpStatusCode status",
            "WebRequest request");
        builder.statement("BindingResult result = ex.getBindingResult()");
        builder.statement("List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()\n" +
            "            .map(f -> new FieldErrorVM(f.getObjectName().replaceFirst(\"DTO$\", \"\"), f.getField(), f.getDefaultMessage()))\n" +
            "            .collect(Collectors.toList())");
        builder.line();
        builder.statement("ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, \"Validation failed\")");
        builder.statement("problem.setType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)");
        builder.statement("problem.setTitle(\"Method argument not valid\")");
        builder.statement("problem.setProperty(FIELD_ERRORS_KEY, fieldErrors)");
        builder.statement("problem.setProperty(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)");
        builder.statement("problem.setProperty(PATH_KEY, getRequestPath(request))");
        builder.line();
        builder.returnStatement("handleExceptionInternal(ex, problem, headers, status, request)");
        builder.closeMethod();
        builder.line();

        // handleBadRequestAlertException
        builder.annotation("ExceptionHandler");
        builder.methodSignature("public", "ResponseEntity<Object>", "handleBadRequestAlertException",
            "BadRequestAlertException ex", "NativeWebRequest request");
        builder.statement("ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage())");
        builder.statement("problem.setProperty(MESSAGE_KEY, \"error.\" + ex.getErrorKey())");
        builder.statement("problem.setProperty(\"params\", ex.getEntityName())");
        builder.statement("problem.setProperty(PATH_KEY, getRequestPath(request))");
        builder.returnStatement("handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request)");
        builder.closeMethod();
        builder.line();

        // handleConcurrencyFailure
        builder.annotation("ExceptionHandler");
        builder.methodSignature("public", "ResponseEntity<Object>", "handleConcurrencyFailure",
            "ConcurrencyFailureException ex", "NativeWebRequest request");
        builder.statement("ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, \"Conflict\")");
        builder.statement("problem.setProperty(MESSAGE_KEY, ErrorConstants.ERR_CONCURRENCY_FAILURE)");
        builder.statement("problem.setProperty(PATH_KEY, getRequestPath(request))");
        builder.returnStatement("handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.CONFLICT, request)");
        builder.closeMethod();
        builder.line();

        // handleAccessDeniedException
        builder.annotation("ExceptionHandler");
        builder.methodSignature("public", "ResponseEntity<Object>", "handleAccessDeniedException",
            "AccessDeniedException ex", "NativeWebRequest request");
        builder.statement("ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, \"Access denied\")");
        builder.statement("problem.setProperty(MESSAGE_KEY, \"error.http.403\")");
        builder.statement("problem.setProperty(PATH_KEY, getRequestPath(request))");
        builder.returnStatement("handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.FORBIDDEN, request)");
        builder.closeMethod();
        builder.line();

        // handleBadCredentialsException
        builder.annotation("ExceptionHandler");
        builder.methodSignature("public", "ResponseEntity<Object>", "handleBadCredentialsException",
            "BadCredentialsException ex", "NativeWebRequest request");
        builder.statement("ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, \"Bad credentials\")");
        builder.statement("problem.setProperty(MESSAGE_KEY, \"error.http.401\")");
        builder.statement("problem.setProperty(PATH_KEY, getRequestPath(request))");
        builder.returnStatement("handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.UNAUTHORIZED, request)");
        builder.closeMethod();
        builder.line();

        // handleGenericException
        builder.annotation("ExceptionHandler");
        builder.methodSignature("public", "ResponseEntity<Object>", "handleGenericException",
            "Exception ex", "NativeWebRequest request");
        builder.statement("ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, \"Internal server error\")");
        builder.statement("problem.setProperty(MESSAGE_KEY, \"error.http.500\")");
        builder.statement("problem.setProperty(PATH_KEY, getRequestPath(request))");
        builder.returnStatement("handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)");
        builder.closeMethod();
        builder.line();

        // getRequestPath helper
        builder.methodSignature("private", "String", "getRequestPath", "WebRequest request");
        builder.ifStatement("request instanceof NativeWebRequest nativeWebRequest");
        builder.statement("HttpServletRequest servletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class)");
        builder.ifStatement("servletRequest != null");
        builder.returnStatement("servletRequest.getRequestURI()");
        builder.closeIf();
        builder.closeIf();
        builder.returnStatement("\"\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/errors/ExceptionTranslator.java", builder.build());
    }

    private void writeInvalidPasswordException() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service");

        builder.addImport("java.io.Serial");

        builder.javadoc("Exception thrown when an invalid password is used.");

        builder.classDeclaration("public", "InvalidPasswordException", "RuntimeException");

        builder.annotation("Serial");
        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.constructor("public", "InvalidPasswordException");
        builder.statement("super(\"Incorrect password\")");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/InvalidPasswordException.java", builder.build());
    }

    private void writeEmailAlreadyUsedException() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service");

        builder.addImport("java.io.Serial");

        builder.javadoc("Exception thrown when an email is already used.");

        builder.classDeclaration("public", "EmailAlreadyUsedException", "RuntimeException");

        builder.annotation("Serial");
        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.constructor("public", "EmailAlreadyUsedException");
        builder.statement("super(\"Email is already in use!\")");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/EmailAlreadyUsedException.java", builder.build());
    }

    private void writeUsernameAlreadyUsedException() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service");

        builder.addImport("java.io.Serial");

        builder.javadoc("Exception thrown when a login is already used.");

        builder.classDeclaration("public", "UsernameAlreadyUsedException", "RuntimeException");

        builder.annotation("Serial");
        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.constructor("public", "UsernameAlreadyUsedException");
        builder.statement("super(\"Login name already used!\")");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/UsernameAlreadyUsedException.java", builder.build());
    }
}
