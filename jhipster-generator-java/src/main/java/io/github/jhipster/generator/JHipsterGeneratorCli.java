/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.app.AppGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * JHipster Generator CLI.
 * Main entry point for the Java-based JHipster generator.
 */
@Command(
    name = "jhipster-java",
    mixinStandardHelpOptions = true,
    version = "JHipster Generator Java 1.0.0",
    description = "Java port of JHipster code generator for microservices"
)
public class JHipsterGeneratorCli implements Callable<Integer> {

    @Parameters(index = "0", defaultValue = "app", description = "Generator to run (app, entity, etc.)")
    private String generator;

    @Option(names = {"-d", "--directory"}, description = "Target directory for generation", defaultValue = ".")
    private String directory;

    @Option(names = {"--base-name"}, description = "Application base name")
    private String baseName;

    @Option(names = {"--package-name"}, description = "Java package name")
    private String packageName;

    @Option(names = {"--application-type"}, description = "Application type (monolith, microservice, gateway)")
    private String applicationType;

    @Option(names = {"--database-type"}, description = "Database type (sql, mongodb, cassandra, etc.)")
    private String databaseType;

    @Option(names = {"--prod-database-type"}, description = "Production database type (postgresql, mysql, etc.)")
    private String prodDatabaseType;

    @Option(names = {"--authentication-type"}, description = "Authentication type (jwt, oauth2, session)")
    private String authenticationType;

    @Option(names = {"--service-discovery-type"}, description = "Service discovery type (consul, eureka, no)")
    private String serviceDiscoveryType;

    @Option(names = {"--server-port"}, description = "Server port")
    private Integer serverPort;

    @Option(names = {"--feign-client"}, description = "Enable Feign client for inter-service communication")
    private Boolean feignClient;

    @Option(names = {"--skip-client"}, description = "Skip client generation")
    private Boolean skipClient;

    @Option(names = {"--skip-user-management"}, description = "Skip user management generation")
    private Boolean skipUserManagement;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JHipsterGeneratorCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("╭───────────────────────────────────────────────────────────────╮");
        System.out.println("│     JHipster Generator Java v1.0.0                            │");
        System.out.println("│     Java port of JHipster code generator                      │");
        System.out.println("╰───────────────────────────────────────────────────────────────╯");
        System.out.println();

        Path targetPath = Paths.get(directory).toAbsolutePath();
        System.out.println("Target directory: " + targetPath);

        // Create configuration
        JHipsterConfig config = new JHipsterConfig();

        // Apply command line options
        if (baseName != null) config.setBaseName(baseName);
        if (packageName != null) config.setPackageName(packageName);
        if (applicationType != null) config.setApplicationType(applicationType);
        if (databaseType != null) config.setDatabaseType(databaseType);
        if (prodDatabaseType != null) config.setProdDatabaseType(prodDatabaseType);
        if (authenticationType != null) config.setAuthenticationType(authenticationType);
        if (serviceDiscoveryType != null) config.setServiceDiscoveryType(serviceDiscoveryType);
        if (serverPort != null) config.setServerPort(serverPort);
        if (feignClient != null) config.setFeignClient(feignClient);
        if (skipClient != null) config.setSkipClient(skipClient);
        if (skipUserManagement != null) config.setSkipUserManagement(skipUserManagement);

        // Create context
        GeneratorContext context = new GeneratorContext(targetPath, config);

        // Run the appropriate generator
        switch (generator.toLowerCase()) {
            case "app":
                System.out.println("Running application generator...");
                System.out.println();
                new AppGenerator(context).run();
                break;
            default:
                System.err.println("Unknown generator: " + generator);
                System.err.println("Available generators: app");
                return 1;
        }

        return 0;
    }
}
