/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.git;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * Git Generator.
 * Generates Git configuration files (.gitignore, .gitattributes).
 */
public class GitGenerator extends BaseApplicationGenerator {

    public GitGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "git";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingGit", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Git configuration files");

        writeGitIgnore();
        writeGitAttributes();
    }

    private void writeGitIgnore() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder gitignore = new StringBuilder();
        gitignore.append("######################\n");
        gitignore.append("# Project Specific\n");
        gitignore.append("######################\n");
        gitignore.append("/target/\n");
        gitignore.append("/build/\n");
        gitignore.append("!gradle/wrapper/gradle-wrapper.jar\n\n");

        gitignore.append("######################\n");
        gitignore.append("# IDE\n");
        gitignore.append("######################\n");
        gitignore.append(".idea/\n");
        gitignore.append("*.iml\n");
        gitignore.append("*.ipr\n");
        gitignore.append("*.iws\n");
        gitignore.append(".project\n");
        gitignore.append(".classpath\n");
        gitignore.append(".settings/\n");
        gitignore.append("*.code-workspace\n");
        gitignore.append(".factorypath\n");
        gitignore.append(".history\n");
        gitignore.append(".vscode/\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Maven\n");
        gitignore.append("######################\n");
        gitignore.append("/log/\n");
        gitignore.append("/target/\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Gradle\n");
        gitignore.append("######################\n");
        gitignore.append(".gradle/\n");
        gitignore.append("!gradle/wrapper/gradle-wrapper.jar\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Package Files\n");
        gitignore.append("######################\n");
        gitignore.append("*.jar\n");
        gitignore.append("*.war\n");
        gitignore.append("*.ear\n");
        gitignore.append("*.zip\n");
        gitignore.append("*.tar.gz\n");
        gitignore.append("*.rar\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Virtual Machine Crash Logs\n");
        gitignore.append("######################\n");
        gitignore.append("hs_err_pid*\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Application Server\n");
        gitignore.append("######################\n");
        gitignore.append("*.log\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Node Modules\n");
        gitignore.append("######################\n");
        gitignore.append("/node_modules/\n");
        gitignore.append("/node/\n");
        gitignore.append("package-lock.json\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Generated Files\n");
        gitignore.append("######################\n");
        gitignore.append("/.apt_generated/\n");
        gitignore.append("/.apt_generated_tests/\n\n");

        gitignore.append("######################\n");
        gitignore.append("# Secret Files\n");
        gitignore.append("######################\n");
        gitignore.append("*.env\n");
        gitignore.append("*.pem\n");
        gitignore.append("*.key\n");
        gitignore.append("application-*.yml\n");
        gitignore.append("!application-dev.yml\n");
        gitignore.append("!application-prod.yml\n");
        gitignore.append("!application-test.yml\n\n");

        // H2 database files
        if (isSql() && ("h2Disk".equals(config.getDevDatabaseType()) || "h2Memory".equals(config.getDevDatabaseType()))) {
            gitignore.append("######################\n");
            gitignore.append("# H2 Database\n");
            gitignore.append("######################\n");
            gitignore.append("/target/h2db/\n");
            gitignore.append("*.db\n\n");
        }

        gitignore.append("######################\n");
        gitignore.append("# OS Files\n");
        gitignore.append("######################\n");
        gitignore.append(".DS_Store\n");
        gitignore.append("Thumbs.db\n");

        writeFile(context.getBasePath().toString() + "/.gitignore", gitignore.toString());
    }

    private void writeGitAttributes() throws Exception {
        StringBuilder gitattributes = new StringBuilder();
        gitattributes.append("# All text files should have the \"lf\" (Unix) line endings\n");
        gitattributes.append("* text=auto eol=lf\n\n");

        gitattributes.append("# Explicitly declare text files\n");
        gitattributes.append("*.java text\n");
        gitattributes.append("*.kt text\n");
        gitattributes.append("*.xml text\n");
        gitattributes.append("*.yml text\n");
        gitattributes.append("*.yaml text\n");
        gitattributes.append("*.properties text\n");
        gitattributes.append("*.json text\n");
        gitattributes.append("*.md text\n");
        gitattributes.append("*.txt text\n");
        gitattributes.append("*.sh text\n");
        gitattributes.append("*.bat text\n");
        gitattributes.append("*.cmd text\n");
        gitattributes.append("*.gradle text\n");
        gitattributes.append("*.html text\n");
        gitattributes.append("*.css text\n");
        gitattributes.append("*.scss text\n");
        gitattributes.append("*.js text\n");
        gitattributes.append("*.ts text\n");
        gitattributes.append("*.tsx text\n");
        gitattributes.append("*.vue text\n\n");

        gitattributes.append("# Declare binary files\n");
        gitattributes.append("*.png binary\n");
        gitattributes.append("*.jpg binary\n");
        gitattributes.append("*.jpeg binary\n");
        gitattributes.append("*.gif binary\n");
        gitattributes.append("*.ico binary\n");
        gitattributes.append("*.svg binary\n");
        gitattributes.append("*.eot binary\n");
        gitattributes.append("*.ttf binary\n");
        gitattributes.append("*.woff binary\n");
        gitattributes.append("*.woff2 binary\n");
        gitattributes.append("*.jar binary\n\n");

        gitattributes.append("# Script files should retain Unix line endings\n");
        gitattributes.append("mvnw text eol=lf\n");
        gitattributes.append("gradlew text eol=lf\n");
        gitattributes.append("*.sh text eol=lf\n\n");

        gitattributes.append("# Batch files should retain Windows line endings\n");
        gitattributes.append("*.bat text eol=crlf\n");
        gitattributes.append("*.cmd text eol=crlf\n");

        writeFile(context.getBasePath().toString() + "/.gitattributes", gitattributes.toString());
    }
}
