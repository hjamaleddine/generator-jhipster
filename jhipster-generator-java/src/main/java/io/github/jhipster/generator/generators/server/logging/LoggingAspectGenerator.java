/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.logging;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Logging Aspect Generator.
 * Generates AOP-based logging infrastructure.
 */
public class LoggingAspectGenerator extends BaseApplicationGenerator {

    public LoggingAspectGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "logging-aspect";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingLoggingAspect", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing logging aspect infrastructure");

        writeLoggingAspect();
        writeLoggingAspectConfiguration();
    }

    private void writeLoggingAspect() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".aop.logging");

        builder.addImports(
            "org.aspectj.lang.JoinPoint",
            "org.aspectj.lang.ProceedingJoinPoint",
            "org.aspectj.lang.annotation.AfterThrowing",
            "org.aspectj.lang.annotation.Around",
            "org.aspectj.lang.annotation.Aspect",
            "org.aspectj.lang.annotation.Pointcut",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.core.env.Environment",
            "org.springframework.core.env.Profiles",
            "java.util.Arrays"
        );

        builder.javadoc("Aspect for logging execution of service and repository Spring components.\\n\\n" +
            "By default, it only runs with the \\\"dev\\\" profile.");
        builder.annotation("Aspect");

        builder.classDeclaration("public", "LoggingAspect", null);

        builder.field("private final", "Environment", "env");
        builder.line();

        builder.constructor("public", "LoggingAspect", "Environment env");
        builder.statement("this.env = env");
        builder.closeMethod();
        builder.line();

        // Pointcuts
        builder.javadoc("Pointcut that matches all repositories, services and Web REST endpoints.");
        builder.annotation("Pointcut", "\"within(@org.springframework.stereotype.Repository *) || \" +\n" +
            "            \"within(@org.springframework.stereotype.Service *) || \" +\n" +
            "            \"within(@org.springframework.web.bind.annotation.RestController *)\"");
        builder.methodSignature("public", "void", "springBeanPointcut");
        builder.line("// Method is empty as this is just a Pointcut, the implementations are in the advices.");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Pointcut that matches all Spring beans in the application's main packages.");
        builder.annotation("Pointcut", "\"within(" + config.getPackageName() + "..*) || \" +\n" +
            "            \"within(" + config.getPackageName() + ".service..*) || \" +\n" +
            "            \"within(" + config.getPackageName() + ".web.rest..*)\"");
        builder.methodSignature("public", "void", "applicationPackagePointcut");
        builder.line("// Method is empty as this is just a Pointcut, the implementations are in the advices.");
        builder.closeMethod();
        builder.line();

        // getLogger helper
        builder.javadoc("Retrieves the {@link Logger} associated to the given {@link JoinPoint}.");
        builder.methodSignature("private", "Logger", "logger", "JoinPoint joinPoint");
        builder.returnStatement("LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName())");
        builder.closeMethod();
        builder.line();

        // AfterThrowing advice
        builder.javadoc("Advice that logs methods throwing exceptions.");
        builder.annotation("AfterThrowing", "pointcut = \"applicationPackagePointcut() && springBeanPointcut()\", throwing = \"e\"");
        builder.methodSignature("public", "void", "logAfterThrowing", "JoinPoint joinPoint", "Throwable e");
        builder.ifStatement("env.acceptsProfiles(Profiles.of(\"dev\"))");
        builder.statement("logger(joinPoint).error(\"Exception in {}() with cause = '{}' and exception = '{}'\",\n" +
            "                joinPoint.getSignature().getName(),\n" +
            "                e.getCause() != null ? e.getCause() : \"NULL\",\n" +
            "                e.getMessage(),\n" +
            "                e)");
        builder.elseStatement();
        builder.statement("logger(joinPoint).error(\"Exception in {}() with cause = {}\",\n" +
            "                joinPoint.getSignature().getName(),\n" +
            "                e.getCause() != null ? e.getCause() : \"NULL\")");
        builder.closeIf();
        builder.closeMethod();
        builder.line();

        // Around advice
        builder.javadoc("Advice that logs when a method is entered and exited.");
        builder.annotation("Around", "\"applicationPackagePointcut() && springBeanPointcut()\"");
        builder.line("public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {");
        builder.indent();
        builder.statement("Logger log = logger(joinPoint)");
        builder.ifStatement("log.isDebugEnabled()");
        builder.statement("log.debug(\"Enter: {}() with argument[s] = {}\",\n" +
            "                joinPoint.getSignature().getName(),\n" +
            "                Arrays.toString(joinPoint.getArgs()))");
        builder.closeIf();
        builder.tryBlock();
        builder.statement("Object result = joinPoint.proceed()");
        builder.ifStatement("log.isDebugEnabled()");
        builder.statement("log.debug(\"Exit: {}() with result = {}\",\n" +
            "                    joinPoint.getSignature().getName(),\n" +
            "                    result)");
        builder.closeIf();
        builder.returnStatement("result");
        builder.catchBlock("IllegalArgumentException e");
        builder.statement("log.error(\"Illegal argument: {} in {}()\",\n" +
            "                Arrays.toString(joinPoint.getArgs()),\n" +
            "                joinPoint.getSignature().getName())");
        builder.statement("throw e");
        builder.closeTry();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "aop/logging/LoggingAspect.java", builder.build());
    }

    private void writeLoggingAspectConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            config.getPackageName() + ".aop.logging.LoggingAspect",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.context.annotation.EnableAspectJAutoProxy",
            "org.springframework.context.annotation.Profile",
            "org.springframework.core.env.Environment"
        );

        builder.javadoc("Configuration for logging aspects.");
        builder.annotation("Configuration");
        builder.annotation("EnableAspectJAutoProxy");

        builder.classDeclaration("public", "LoggingAspectConfiguration", null);

        builder.annotation("Bean");
        builder.annotation("Profile", "\"dev\"");
        builder.methodSignature("public", "LoggingAspect", "loggingAspect", "Environment env");
        builder.returnStatement("new LoggingAspect(env)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/LoggingAspectConfiguration.java", builder.build());
    }
}
