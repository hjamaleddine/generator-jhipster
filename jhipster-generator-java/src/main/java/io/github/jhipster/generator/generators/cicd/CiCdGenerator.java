/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.cicd;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;

/**
 * CI/CD Generator.
 * Generates CI/CD configuration for GitHub Actions, GitLab CI, Jenkins, etc.
 */
public class CiCdGenerator extends BaseApplicationGenerator {

    public CiCdGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "ci-cd";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingCiCd", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing CI/CD configuration");

        JHipsterConfig config = getConfig();
        String ciCdType = config.getCiCd();

        if (ciCdType == null || "none".equals(ciCdType)) {
            // Default to GitHub Actions
            writeGitHubActions();
        } else {
            switch (ciCdType) {
                case "github":
                    writeGitHubActions();
                    break;
                case "gitlab":
                    writeGitLabCi();
                    break;
                case "jenkins":
                    writeJenkinsfile();
                    break;
                case "azure":
                    writeAzurePipelines();
                    break;
                case "circle":
                    writeCircleCi();
                    break;
                case "travis":
                    writeTravisCi();
                    break;
                default:
                    writeGitHubActions();
            }
        }
    }

    private void writeGitHubActions() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("name: Application CI\n\n");
        yml.append("on: [push, pull_request]\n\n");

        yml.append("jobs:\n");
        yml.append("  pipeline:\n");
        yml.append("    name: ").append(config.getBaseName()).append(" pipeline\n");
        yml.append("    runs-on: ubuntu-latest\n");
        yml.append("    if: \"!contains(github.event.head_commit.message, '[ci skip]') && !contains(github.event.head_commit.message, '[skip ci]') && !contains(github.event.pull_request.title, '[skip ci]') && !contains(github.event.pull_request.title, '[ci skip]')\"\n");
        yml.append("    timeout-minutes: 40\n");
        yml.append("    env:\n");
        yml.append("      SPRING_OUTPUT_ANSI_ENABLED: DETECT\n");
        yml.append("      SPRING_JPA_SHOW_SQL: false\n");
        yml.append("      JHI_DISABLE_WEBPACK_LOGS: true\n\n");

        yml.append("    steps:\n");
        yml.append("      - uses: actions/checkout@v4\n\n");

        yml.append("      - uses: actions/setup-java@v4\n");
        yml.append("        with:\n");
        yml.append("          distribution: 'temurin'\n");
        yml.append("          java-version: 17\n\n");

        yml.append("      - name: Cache Maven dependencies\n");
        yml.append("        uses: actions/cache@v4\n");
        yml.append("        with:\n");
        yml.append("          path: ~/.m2/repository\n");
        yml.append("          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}\n");
        yml.append("          restore-keys: |\n");
        yml.append("            ${{ runner.os }}-maven-\n\n");

        yml.append("      - name: Run backend tests\n");
        yml.append("        run: |\n");
        yml.append("          chmod +x mvnw\n");
        yml.append("          ./mvnw -ntp clean verify -P-webapp\n\n");

        yml.append("      - name: Package application\n");
        yml.append("        run: ./mvnw -ntp package -DskipTests\n\n");

        if (isSql()) {
            yml.append("      - name: Build and push Docker image\n");
            yml.append("        if: github.event_name == 'push' && github.ref == 'refs/heads/main'\n");
            yml.append("        run: |\n");
            yml.append("          ./mvnw -ntp jib:build -Djib.to.image=").append(config.getLowerBaseName()).append(":latest\n\n");
        }

        yml.append("      - name: Analyze code with SonarQube\n");
        yml.append("        if: github.event_name == 'push' && github.ref == 'refs/heads/main'\n");
        yml.append("        env:\n");
        yml.append("          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}\n");
        yml.append("          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}\n");
        yml.append("        run: |\n");
        yml.append("          if [ ! -z \"$SONAR_TOKEN\" ]; then\n");
        yml.append("            ./mvnw -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:sonar\n");
        yml.append("          fi\n");

        writeFile(context.getBasePath().toString() + "/.github/workflows/main.yml", yml.toString());

        // Also write dependabot configuration
        writeDependabotConfig();
    }

    private void writeDependabotConfig() throws Exception {
        StringBuilder yml = new StringBuilder();
        yml.append("version: 2\n");
        yml.append("updates:\n");
        yml.append("  - package-ecosystem: maven\n");
        yml.append("    directory: /\n");
        yml.append("    schedule:\n");
        yml.append("      interval: weekly\n");
        yml.append("    open-pull-requests-limit: 5\n");
        yml.append("    labels:\n");
        yml.append("      - dependencies\n\n");

        yml.append("  - package-ecosystem: github-actions\n");
        yml.append("    directory: /\n");
        yml.append("    schedule:\n");
        yml.append("      interval: weekly\n");
        yml.append("    labels:\n");
        yml.append("      - dependencies\n");

        writeFile(context.getBasePath().toString() + "/.github/dependabot.yml", yml.toString());
    }

    private void writeGitLabCi() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("image: jhipster/jhipster:v8.0.0\n\n");

        yml.append("cache:\n");
        yml.append("  key: \"$CI_COMMIT_REF_NAME\"\n");
        yml.append("  paths:\n");
        yml.append("    - .maven/\n\n");

        yml.append("stages:\n");
        yml.append("  - check\n");
        yml.append("  - build\n");
        yml.append("  - test\n");
        yml.append("  - analyze\n");
        yml.append("  - package\n");
        yml.append("  - release\n");
        yml.append("  - deploy\n\n");

        yml.append("before_script:\n");
        yml.append("  - export MAVEN_USER_HOME=`pwd`/.maven\n");
        yml.append("  - chmod +x mvnw\n\n");

        // Validation stage
        yml.append("nohttp:\n");
        yml.append("  stage: check\n");
        yml.append("  script:\n");
        yml.append("    - ./mvnw -ntp checkstyle:check -Dmaven.repo.local=$MAVEN_USER_HOME\n\n");

        // Build stage
        yml.append("maven-compile:\n");
        yml.append("  stage: build\n");
        yml.append("  script:\n");
        yml.append("    - ./mvnw -ntp compile -P-webapp -Dmaven.repo.local=$MAVEN_USER_HOME\n");
        yml.append("  artifacts:\n");
        yml.append("    paths:\n");
        yml.append("      - target/classes/\n");
        yml.append("      - target/generated-sources/\n");
        yml.append("    expire_in: 1 day\n\n");

        // Test stage
        yml.append("maven-test:\n");
        yml.append("  stage: test\n");
        yml.append("  script:\n");
        yml.append("    - ./mvnw -ntp verify -P-webapp -Dmaven.repo.local=$MAVEN_USER_HOME\n");
        yml.append("  artifacts:\n");
        yml.append("    reports:\n");
        yml.append("      junit:\n");
        yml.append("        - target/surefire-reports/TEST-*.xml\n");
        yml.append("        - target/failsafe-reports/TEST-*.xml\n");
        yml.append("    paths:\n");
        yml.append("      - target/surefire-reports\n");
        yml.append("      - target/failsafe-reports\n");
        yml.append("      - target/site\n");
        yml.append("    expire_in: 1 day\n\n");

        // Analyze stage
        yml.append("sonar-analyze:\n");
        yml.append("  stage: analyze\n");
        yml.append("  dependencies:\n");
        yml.append("    - maven-test\n");
        yml.append("  script:\n");
        yml.append("    - ./mvnw -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_TOKEN -Dmaven.repo.local=$MAVEN_USER_HOME\n");
        yml.append("  only:\n");
        yml.append("    - main\n\n");

        // Package stage
        yml.append("maven-package:\n");
        yml.append("  stage: package\n");
        yml.append("  script:\n");
        yml.append("    - ./mvnw -ntp verify -Pprod -DskipTests -Dmaven.repo.local=$MAVEN_USER_HOME\n");
        yml.append("  artifacts:\n");
        yml.append("    paths:\n");
        yml.append("      - target/*.jar\n");
        yml.append("      - target/classes\n");
        yml.append("    expire_in: 1 day\n");
        yml.append("  only:\n");
        yml.append("    - main\n\n");

        // Release/Deploy stage
        yml.append("docker-push:\n");
        yml.append("  stage: release\n");
        yml.append("  variables:\n");
        yml.append("    DOCKER_HOST: tcp://docker:2375\n");
        yml.append("  dependencies:\n");
        yml.append("    - maven-package\n");
        yml.append("  script:\n");
        yml.append("    - ./mvnw -ntp jib:build -Djib.to.image=$CI_REGISTRY_IMAGE:$CI_COMMIT_REF_SLUG -Djib.to.auth.username=gitlab-ci-token -Djib.to.auth.password=$CI_JOB_TOKEN -Dmaven.repo.local=$MAVEN_USER_HOME\n");
        yml.append("  services:\n");
        yml.append("    - docker:dind\n");
        yml.append("  only:\n");
        yml.append("    - main\n");

        writeFile(context.getBasePath().toString() + "/.gitlab-ci.yml", yml.toString());
    }

    private void writeJenkinsfile() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder jenkinsfile = new StringBuilder();
        jenkinsfile.append("#!/usr/bin/env groovy\n\n");

        jenkinsfile.append("node {\n");
        jenkinsfile.append("    stage('checkout') {\n");
        jenkinsfile.append("        checkout scm\n");
        jenkinsfile.append("    }\n\n");

        jenkinsfile.append("    stage('check java') {\n");
        jenkinsfile.append("        sh \"java -version\"\n");
        jenkinsfile.append("    }\n\n");

        jenkinsfile.append("    stage('clean') {\n");
        jenkinsfile.append("        sh \"chmod +x mvnw\"\n");
        jenkinsfile.append("        sh \"./mvnw -ntp clean -P-webapp\"\n");
        jenkinsfile.append("    }\n\n");

        jenkinsfile.append("    stage('nohttp') {\n");
        jenkinsfile.append("        sh \"./mvnw -ntp checkstyle:check\"\n");
        jenkinsfile.append("    }\n\n");

        jenkinsfile.append("    stage('backend tests') {\n");
        jenkinsfile.append("        try {\n");
        jenkinsfile.append("            sh \"./mvnw -ntp verify -P-webapp\"\n");
        jenkinsfile.append("        } catch(err) {\n");
        jenkinsfile.append("            throw err\n");
        jenkinsfile.append("        } finally {\n");
        jenkinsfile.append("            junit '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'\n");
        jenkinsfile.append("        }\n");
        jenkinsfile.append("    }\n\n");

        jenkinsfile.append("    stage('packaging') {\n");
        jenkinsfile.append("        sh \"./mvnw -ntp verify -Pprod -DskipTests\"\n");
        jenkinsfile.append("        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true\n");
        jenkinsfile.append("    }\n\n");

        jenkinsfile.append("    stage('quality analysis') {\n");
        jenkinsfile.append("        withSonarQubeEnv('Sonar') {\n");
        jenkinsfile.append("            sh \"./mvnw -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:sonar\"\n");
        jenkinsfile.append("        }\n");
        jenkinsfile.append("    }\n");

        jenkinsfile.append("}\n");

        writeFile(context.getBasePath().toString() + "/Jenkinsfile", jenkinsfile.toString());
    }

    private void writeAzurePipelines() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("trigger:\n");
        yml.append("  - main\n\n");

        yml.append("pool:\n");
        yml.append("  vmImage: 'ubuntu-latest'\n\n");

        yml.append("variables:\n");
        yml.append("  MAVEN_CACHE_FOLDER: $(Pipeline.Workspace)/.m2/repository\n");
        yml.append("  MAVEN_OPTS: '-Dmaven.repo.local=$(MAVEN_CACHE_FOLDER)'\n\n");

        yml.append("stages:\n");
        yml.append("  - stage: Build\n");
        yml.append("    displayName: Build stage\n");
        yml.append("    jobs:\n");
        yml.append("      - job: Build\n");
        yml.append("        displayName: Build\n");
        yml.append("        steps:\n");
        yml.append("          - task: Cache@2\n");
        yml.append("            inputs:\n");
        yml.append("              key: 'maven | \"$(Agent.OS)\" | **/pom.xml'\n");
        yml.append("              restoreKeys: |\n");
        yml.append("                maven | \"$(Agent.OS)\"\n");
        yml.append("                maven\n");
        yml.append("              path: $(MAVEN_CACHE_FOLDER)\n");
        yml.append("            displayName: Cache Maven local repo\n\n");

        yml.append("          - task: JavaToolInstaller@0\n");
        yml.append("            inputs:\n");
        yml.append("              versionSpec: '17'\n");
        yml.append("              jdkArchitectureOption: 'x64'\n");
        yml.append("              jdkSourceOption: 'PreInstalled'\n\n");

        yml.append("          - script: |\n");
        yml.append("              chmod +x mvnw\n");
        yml.append("              ./mvnw -ntp verify -P-webapp $(MAVEN_OPTS)\n");
        yml.append("            displayName: 'Run tests'\n\n");

        yml.append("          - task: PublishTestResults@2\n");
        yml.append("            inputs:\n");
        yml.append("              testResultsFormat: 'JUnit'\n");
        yml.append("              testResultsFiles: '**/TEST-*.xml'\n");
        yml.append("            condition: succeededOrFailed()\n\n");

        yml.append("          - script: |\n");
        yml.append("              ./mvnw -ntp package -Pprod -DskipTests $(MAVEN_OPTS)\n");
        yml.append("            displayName: 'Package application'\n\n");

        yml.append("          - publish: target/*.jar\n");
        yml.append("            artifact: app\n");
        yml.append("            displayName: 'Publish artifact'\n");

        writeFile(context.getBasePath().toString() + "/azure-pipelines.yml", yml.toString());
    }

    private void writeCircleCi() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("version: 2.1\n\n");

        yml.append("executors:\n");
        yml.append("  java-executor:\n");
        yml.append("    docker:\n");
        yml.append("      - image: cimg/openjdk:17.0\n");
        yml.append("    working_directory: ~/repo\n");
        yml.append("    environment:\n");
        yml.append("      MAVEN_OPTS: -Xmx3200m\n\n");

        yml.append("jobs:\n");
        yml.append("  build:\n");
        yml.append("    executor: java-executor\n");
        yml.append("    steps:\n");
        yml.append("      - checkout\n\n");

        yml.append("      - restore_cache:\n");
        yml.append("          keys:\n");
        yml.append("            - v1-dependencies-{{ checksum \"pom.xml\" }}\n");
        yml.append("            - v1-dependencies-\n\n");

        yml.append("      - run:\n");
        yml.append("          name: Run tests\n");
        yml.append("          command: |\n");
        yml.append("            chmod +x mvnw\n");
        yml.append("            ./mvnw -ntp verify -P-webapp\n\n");

        yml.append("      - save_cache:\n");
        yml.append("          paths:\n");
        yml.append("            - ~/.m2\n");
        yml.append("          key: v1-dependencies-{{ checksum \"pom.xml\" }}\n\n");

        yml.append("      - store_test_results:\n");
        yml.append("          path: target/surefire-reports\n\n");

        yml.append("      - store_artifacts:\n");
        yml.append("          path: target/surefire-reports\n\n");

        yml.append("  package:\n");
        yml.append("    executor: java-executor\n");
        yml.append("    steps:\n");
        yml.append("      - checkout\n\n");

        yml.append("      - restore_cache:\n");
        yml.append("          keys:\n");
        yml.append("            - v1-dependencies-{{ checksum \"pom.xml\" }}\n\n");

        yml.append("      - run:\n");
        yml.append("          name: Package application\n");
        yml.append("          command: |\n");
        yml.append("            ./mvnw -ntp package -Pprod -DskipTests\n\n");

        yml.append("      - store_artifacts:\n");
        yml.append("          path: target/*.jar\n\n");

        yml.append("workflows:\n");
        yml.append("  version: 2\n");
        yml.append("  build-and-package:\n");
        yml.append("    jobs:\n");
        yml.append("      - build\n");
        yml.append("      - package:\n");
        yml.append("          requires:\n");
        yml.append("            - build\n");
        yml.append("          filters:\n");
        yml.append("            branches:\n");
        yml.append("              only: main\n");

        writeFile(context.getBasePath().toString() + "/.circleci/config.yml", yml.toString());
    }

    private void writeTravisCi() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("os:\n");
        yml.append("  - linux\n\n");

        yml.append("language: java\n\n");

        yml.append("jdk:\n");
        yml.append("  - openjdk17\n\n");

        yml.append("cache:\n");
        yml.append("  directories:\n");
        yml.append("    - $HOME/.m2\n\n");

        yml.append("before_install:\n");
        yml.append("  - chmod +x mvnw\n\n");

        yml.append("script:\n");
        yml.append("  - ./mvnw -ntp checkstyle:check\n");
        yml.append("  - ./mvnw -ntp clean verify -P-webapp\n\n");

        yml.append("after_success:\n");
        yml.append("  - ./mvnw -ntp package -Pprod -DskipTests\n\n");

        yml.append("notifications:\n");
        yml.append("  email: false\n");

        writeFile(context.getBasePath().toString() + "/.travis.yml", yml.toString());
    }
}
