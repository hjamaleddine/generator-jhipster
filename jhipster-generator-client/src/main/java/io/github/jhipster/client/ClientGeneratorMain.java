/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.generators.client.ClientGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point for JHipster Client Generator CLI.
 *
 * Usage:
 *   java -jar jhipster-generator-client.jar [project-path] [options]
 *
 * Options:
 *   --framework=angular|react|vue   Client framework (default: angular)
 *   --bundler=webpack|vite|esbuild  Bundler (default: webpack)
 *   --theme=<theme-name>            Bootswatch theme (default: none)
 *   --port=<number>                 Dev server port (default: 9000)
 *   --i18n=true|false              Enable i18n (default: true)
 *   --cypress=true|false           Generate Cypress tests (default: false)
 *   --auth=jwt|oauth2|session      Authentication type (default: jwt)
 *   --help                         Show this help message
 */
public class ClientGeneratorMain {

    private static final Logger log = LoggerFactory.getLogger(ClientGeneratorMain.class);

    private static final String VERSION = "1.0.0-SNAPSHOT";

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            log.error("Generation failed: {}", e.getMessage());
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void run(String[] args) throws Exception {
        // Parse arguments
        Map<String, String> options = parseArguments(args);

        // Show help
        if (options.containsKey("help")) {
            printHelp();
            return;
        }

        // Show version
        if (options.containsKey("version")) {
            System.out.println("JHipster Client Generator v" + VERSION);
            return;
        }

        // Get project path
        String projectPathStr = options.getOrDefault("path", ".");
        Path projectPath = Paths.get(projectPathStr).toAbsolutePath();

        log.info("JHipster Client Generator v{}", VERSION);
        log.info("Project path: {}", projectPath);

        // Create project directory if needed
        if (!Files.exists(projectPath)) {
            Files.createDirectories(projectPath);
            log.info("Created project directory: {}", projectPath);
        }

        // Load or create configuration
        ClientGeneratorContext context;
        Path yoRcPath = projectPath.resolve(".yo-rc.json");

        if (Files.exists(yoRcPath)) {
            log.info("Loading configuration from .yo-rc.json");
            context = ClientGeneratorContext.fromYoRc(projectPath);

            // Apply CLI overrides
            applyOverrides(context.getConfig(), options);
        } else {
            log.info("No .yo-rc.json found, using CLI options");
            ClientConfig config = createConfigFromOptions(options);
            context = new ClientGeneratorContext(projectPath, config);

            // Save the configuration
            context.saveConfig();
        }

        // Display configuration
        printConfiguration(context.getConfig());

        // Confirm generation
        if (!options.containsKey("force")) {
            System.out.println("\nPress Enter to start generation or Ctrl+C to cancel...");
            System.in.read();
        }

        // Run generation
        log.info("Starting generation...");
        long startTime = System.currentTimeMillis();

        ClientGenerator generator = new ClientGenerator(context);
        generator.generate();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Generation completed in {} ms", duration);

        // Print next steps
        printNextSteps(projectPath, context.getConfig());
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> options = new HashMap<>();

        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                String key = parts[0];
                String value = parts.length > 1 ? parts[1] : "true";
                options.put(key, value);
            } else if (!arg.startsWith("-")) {
                options.put("path", arg);
            }
        }

        return options;
    }

    private static void applyOverrides(ClientConfig config, Map<String, String> options) {
        if (options.containsKey("framework")) {
            config.setClientFramework(options.get("framework"));
        }
        if (options.containsKey("bundler")) {
            config.setClientBundler(options.get("bundler"));
        }
        if (options.containsKey("theme")) {
            config.setClientTheme(options.get("theme"));
        }
        if (options.containsKey("port")) {
            config.setDevServerPort(Integer.parseInt(options.get("port")));
        }
        if (options.containsKey("i18n")) {
            config.setEnableTranslation(Boolean.parseBoolean(options.get("i18n")));
        }
        if (options.containsKey("cypress")) {
            if (Boolean.parseBoolean(options.get("cypress"))) {
                config.getTestFrameworks().add("cypress");
            }
        }
        if (options.containsKey("auth")) {
            config.setAuthenticationType(options.get("auth"));
        }
    }

    private static ClientConfig createConfigFromOptions(Map<String, String> options) {
        ClientConfig config = new ClientConfig();

        // Required
        config.setBaseName(options.getOrDefault("name", "jhipsterApp"));
        config.setPackageName(options.getOrDefault("package", "com.mycompany.myapp"));

        // Framework
        config.setClientFramework(options.getOrDefault("framework", "angular"));
        config.setClientBundler(options.getOrDefault("bundler", "webpack"));
        config.setClientTheme(options.getOrDefault("theme", "none"));
        config.setClientThemeVariant(options.getOrDefault("theme-variant", "primary"));

        // Dev server
        config.setDevServerPort(Integer.parseInt(options.getOrDefault("port", "9000")));

        // i18n
        config.setEnableTranslation(Boolean.parseBoolean(options.getOrDefault("i18n", "true")));
        config.setNativeLanguage(options.getOrDefault("lang", "en"));

        // Auth
        config.setAuthenticationType(options.getOrDefault("auth", "jwt"));

        // Application type
        config.setApplicationType(options.getOrDefault("type", "monolith"));

        // Test frameworks
        if (Boolean.parseBoolean(options.getOrDefault("cypress", "false"))) {
            config.getTestFrameworks().add("cypress");
        }

        return config;
    }

    private static void printConfiguration(ClientConfig config) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              JHipster Client Generator                    ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║  Application:     %-38s ║%n", config.getBaseName());
        System.out.printf("║  Framework:       %-38s ║%n", config.getClientFramework());
        System.out.printf("║  Bundler:         %-38s ║%n", config.getClientBundler());
        System.out.printf("║  Theme:           %-38s ║%n", config.getClientTheme());
        System.out.printf("║  Authentication:  %-38s ║%n", config.getAuthenticationType());
        System.out.printf("║  i18n:            %-38s ║%n", config.getEnableTranslation());
        System.out.printf("║  Dev Server Port: %-38d ║%n", config.getDevServerPort());
        System.out.printf("║  Entities:        %-38d ║%n", config.getEntities().size());
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    private static void printNextSteps(Path projectPath, ClientConfig config) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                    Generation Complete!                   ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  Next steps:                                              ║");
        System.out.println("║                                                           ║");
        System.out.printf("║  1. cd %s%n", padRight(projectPath.toString(), 46) + "║");
        System.out.println("║  2. npm install                                           ║");

        String startCmd = switch (config.getClientFramework()) {
            case "vue" -> config.isVite() ? "npm run dev" : "npm run dev";
            case "react" -> "npm start";
            default -> "npm start";
        };
        System.out.printf("║  3. %s%n", padRight(startCmd, 50) + "║");
        System.out.println("║                                                           ║");
        System.out.printf("║  Open: http://localhost:%d%n", config.getDevServerPort());
        System.out.println(padRight("", 25) + "║");

        if (config.hasCypress()) {
            System.out.println("║                                                           ║");
            System.out.println("║  E2E Tests:                                               ║");
            System.out.println("║    npm run e2e       (headless)                           ║");
            System.out.println("║    npm run e2e:open  (interactive)                        ║");
        }

        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    private static void printHelp() {
        System.out.println("JHipster Client Generator v" + VERSION);
        System.out.println();
        System.out.println("Usage: java -jar jhipster-generator-client.jar [project-path] [options]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  project-path          Path to the project directory (default: current dir)");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --name=<name>         Application name (default: jhipsterApp)");
        System.out.println("  --package=<pkg>       Java package name (default: com.mycompany.myapp)");
        System.out.println("  --framework=<fw>      Client framework: angular, react, vue (default: angular)");
        System.out.println("  --bundler=<bundler>   Bundler: webpack, vite, esbuild (default: webpack)");
        System.out.println("  --theme=<theme>       Bootswatch theme name (default: none)");
        System.out.println("  --theme-variant=<v>   Theme variant: primary, dark, light (default: primary)");
        System.out.println("  --port=<port>         Dev server port (default: 9000)");
        System.out.println("  --i18n=<bool>         Enable internationalization (default: true)");
        System.out.println("  --lang=<lang>         Native language (default: en)");
        System.out.println("  --auth=<type>         Auth type: jwt, oauth2, session (default: jwt)");
        System.out.println("  --type=<type>         App type: monolith, gateway (default: monolith)");
        System.out.println("  --cypress=<bool>      Generate Cypress E2E tests (default: false)");
        System.out.println("  --force               Skip confirmation prompt");
        System.out.println("  --help                Show this help message");
        System.out.println("  --version             Show version number");
        System.out.println();
        System.out.println("Examples:");
        System.out.println();
        System.out.println("  # Generate Angular app in current directory");
        System.out.println("  java -jar jhipster-generator-client.jar");
        System.out.println();
        System.out.println("  # Generate React app in specific directory");
        System.out.println("  java -jar jhipster-generator-client.jar ./my-app --framework=react");
        System.out.println();
        System.out.println("  # Generate Vue app with Vite and Cypress");
        System.out.println("  java -jar jhipster-generator-client.jar ./vue-app --framework=vue --bundler=vite --cypress=true");
        System.out.println();
        System.out.println("  # Use existing .yo-rc.json config");
        System.out.println("  java -jar jhipster-generator-client.jar ./existing-project");
        System.out.println();
        System.out.println("Documentation: https://www.jhipster.tech/");
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
