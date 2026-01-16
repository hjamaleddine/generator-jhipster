/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.generators.cypress;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientConfig.EntityConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.core.BaseClientGenerator;

import java.io.IOException;

/**
 * Cypress Generator.
 * Generates Cypress E2E tests for JHipster applications.
 * Equivalent to generators/cypress/generator.ts.
 */
public class CypressGenerator extends BaseClientGenerator {

    public CypressGenerator(ClientGeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "cypress";
    }

    @Override
    protected void registerTasks() {
        registerTask(GeneratorPhase.INITIALIZING, this::initializing);
        registerTask(GeneratorPhase.WRITING, this::writing);
        registerTask(GeneratorPhase.WRITING_ENTITIES, this::writingEntities);
        registerTask(GeneratorPhase.END, this::end);
    }

    private void initializing() {
        log.info("Initializing Cypress generator");
    }

    private void writing() {
        log.info("Writing Cypress configuration and support files");

        try {
            // Cypress configuration
            writeCypressConfig();

            // Support files
            writeSupportFiles();

            // Common E2E tests
            writeCommonTests();

        } catch (IOException e) {
            throw new RuntimeException("Error writing Cypress files", e);
        }
    }

    private void writeCypressConfig() throws IOException {
        String config = """
            import { defineConfig } from 'cypress';

            export default defineConfig({
              e2e: {
                baseUrl: 'http://localhost:%d',
                specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
                supportFile: 'cypress/support/e2e.ts',
                videosFolder: 'cypress/videos',
                screenshotsFolder: 'cypress/screenshots',
                fixturesFolder: 'cypress/fixtures',
                video: false,
                screenshotOnRunFailure: true,
                defaultCommandTimeout: 10000,
                requestTimeout: 10000,
                responseTimeout: 30000,
                viewportWidth: 1280,
                viewportHeight: 720,
              },
            });
            """.formatted(getConfig().getDevServerPort());
        writeFile("cypress.config.ts", config);
    }

    private void writeSupportFiles() throws IOException {
        // Support e2e.ts
        String supportE2e = """
            /// <reference types="cypress" />

            import './commands';

            // Hide fetch/XHR requests from command log
            const app = window.top;
            if (app && !app.document.head.querySelector('[data-hide-command-log-request]')) {
              const style = app.document.createElement('style');
              style.innerHTML =
                '.command-name-request, .command-name-xhr { display: none }';
              style.setAttribute('data-hide-command-log-request', '');
              app.document.head.appendChild(style);
            }
            """;
        writeFile("cypress/support/e2e.ts", supportE2e);

        // Commands
        String commands = """
            /// <reference types="cypress" />

            declare global {
              namespace Cypress {
                interface Chainable {
                  login(username: string, password: string): Chainable<void>;
                  logout(): Chainable<void>;
                  authenticatedRequest(options: Partial<Cypress.RequestOptions>): Chainable<Cypress.Response<any>>;
                  getByTestId(testId: string): Chainable<JQuery<HTMLElement>>;
                  getByDataCy(dataCy: string): Chainable<JQuery<HTMLElement>>;
                }
              }
            }

            %s

            Cypress.Commands.add('logout', () => {
              cy.visit('/logout');
              cy.url().should('eq', Cypress.config().baseUrl + '/');
            });

            Cypress.Commands.add('authenticatedRequest', (options) => {
              const token = localStorage.getItem('authToken');
              return cy.request({
                ...options,
                headers: {
                  ...options.headers,
                  Authorization: token ? `Bearer ${token}` : undefined,
                },
              });
            });

            Cypress.Commands.add('getByTestId', (testId) => {
              return cy.get(`[data-testid="${testId}"]`);
            });

            Cypress.Commands.add('getByDataCy', (dataCy) => {
              return cy.get(`[data-cy="${dataCy}"]`);
            });

            export {};
            """.formatted(
                getConfig().isOAuth2() ?
                    "Cypress.Commands.add('login', (username, password) => {\n" +
                    "  cy.visit('/oauth2/authorization/oidc');\n" +
                    "  // OAuth2 login flow - depends on provider\n" +
                    "});" :
                    "Cypress.Commands.add('login', (username, password) => {\n" +
                    "  cy.request({\n" +
                    "    method: 'POST',\n" +
                    "    url: '/api/authenticate',\n" +
                    "    body: {\n" +
                    "      username,\n" +
                    "      password,\n" +
                    "      rememberMe: false,\n" +
                    "    },\n" +
                    "  }).then((response) => {\n" +
                    "    const token = response.headers['authorization']?.toString().replace('Bearer ', '');\n" +
                    "    if (token) {\n" +
                    "      localStorage.setItem('authToken', token);\n" +
                    "    }\n" +
                    "  });\n" +
                    "});"
        );
        writeFile("cypress/support/commands.ts", commands);

        // Fixtures
        String usersFixture = """
            {
              "admin": {
                "username": "admin",
                "password": "admin"
              },
              "user": {
                "username": "user",
                "password": "user"
              }
            }
            """;
        writeFile("cypress/fixtures/users.json", usersFixture);

        // Integration
        String integrationFixture = """
            {
              "baseUrl": "%s",
              "apiUrl": "/api"
            }
            """.formatted("http://localhost:" + getConfig().getDevServerPort());
        writeFile("cypress/fixtures/integration.json", integrationFixture);
    }

    private void writeCommonTests() throws IOException {
        // Home page test
        String homeTest = """
            describe('Home page', () => {
              beforeEach(() => {
                cy.visit('/');
              });

              it('should display the home page', () => {
                cy.get('h1, h2').should('be.visible');
              });

              it('should have a navbar', () => {
                cy.get('nav.navbar').should('be.visible');
              });

              it('should have a sign in link when not authenticated', () => {
                cy.get('a[href*="login"], [routerlink*="login"]').should('be.visible');
              });
            });
            """;
        writeFile("cypress/e2e/home.cy.ts", homeTest);

        // Login test
        String loginTest;
        if (getConfig().isOAuth2()) {
            loginTest = """
                describe('OAuth2 Login', () => {
                  it('should redirect to OAuth2 provider', () => {
                    cy.visit('/login');
                    cy.url().should('include', 'oauth2');
                  });
                });
                """;
        } else {
            loginTest = """
                describe('Login', () => {
                  beforeEach(() => {
                    cy.visit('/login');
                  });

                  it('should display login form', () => {
                    cy.get('input[name="username"], input#username').should('be.visible');
                    cy.get('input[name="password"], input#password').should('be.visible');
                    cy.get('button[type="submit"]').should('be.visible');
                  });

                  it('should fail to login with wrong credentials', () => {
                    cy.get('input[name="username"], input#username').type('wronguser');
                    cy.get('input[name="password"], input#password').type('wrongpassword');
                    cy.get('button[type="submit"]').click();
                    cy.get('.alert-danger').should('be.visible');
                  });

                  it('should login with admin credentials', () => {
                    cy.fixture('users').then((users) => {
                      cy.get('input[name="username"], input#username').type(users.admin.username);
                      cy.get('input[name="password"], input#password').type(users.admin.password);
                      cy.get('button[type="submit"]').click();
                      cy.url().should('eq', Cypress.config().baseUrl + '/');
                    });
                  });
                });
                """;
        }
        writeFile("cypress/e2e/login.cy.ts", loginTest);

        // Admin tests
        String adminTest = """
            describe('Admin module', () => {
              beforeEach(() => {
                cy.fixture('users').then((users) => {
                  cy.login(users.admin.username, users.admin.password);
                });
              });

              it('should access user management', () => {
                cy.visit('/admin/user-management');
                cy.get('h2, h1').should('contain', 'User');
              });

              it('should access metrics', () => {
                cy.visit('/admin/metrics');
                cy.get('h2, h1').should('contain', 'Metric');
              });

              it('should access health check', () => {
                cy.visit('/admin/health');
                cy.get('h2, h1').should('contain', 'Health');
              });

              it('should access logs', () => {
                cy.visit('/admin/logs');
                cy.get('h2, h1').should('contain', 'Log');
              });

              it('should access configuration', () => {
                cy.visit('/admin/configuration');
                cy.get('h2, h1').should('contain', 'Configuration');
              });
            });
            """;
        writeFile("cypress/e2e/admin.cy.ts", adminTest);

        // Logout test
        String logoutTest = """
            describe('Logout', () => {
              beforeEach(() => {
                cy.fixture('users').then((users) => {
                  cy.login(users.admin.username, users.admin.password);
                });
              });

              it('should logout successfully', () => {
                cy.visit('/');
                cy.get('a[href*="logout"], [routerlink*="logout"]').click();
                cy.url().should('not.include', 'admin');
              });
            });
            """;
        writeFile("cypress/e2e/logout.cy.ts", logoutTest);
    }

    private void writingEntities() {
        log.info("Writing Cypress entity tests");

        for (EntityConfig entity : getConfig().getEntities()) {
            if (entity.getBuiltIn() || entity.getEmbedded()) {
                continue;
            }

            try {
                writeEntityTest(entity);
            } catch (IOException e) {
                log.error("Error writing Cypress test for entity: {}", entity.getName(), e);
            }
        }
    }

    private void writeEntityTest(EntityConfig entity) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityFile = entity.getEntityFileName();
        String entityUrl = toKebabCase(entity.getName());

        String testContent = """
            describe('%s CRUD', () => {
              beforeEach(() => {
                cy.fixture('users').then((users) => {
                  cy.login(users.admin.username, users.admin.password);
                });
              });

              it('should display %s list', () => {
                cy.visit('/%s');
                cy.get('h2, h1').should('contain', '%s');
                cy.get('table').should('be.visible');
              });

              %s

              it('should view %s details', () => {
                cy.visit('/%s');
                cy.get('table tbody tr').first().within(() => {
                  cy.get('a').first().click();
                });
                cy.url().should('include', '/%s/');
              });

              %s
            });
            """.formatted(
                entityClass,
                entityClass,
                entityFile, entityClass,
                entity.getReadOnly() ? "" :
                    "it('should create a new " + entityClass + "', () => {\n" +
                    "    cy.visit('/" + entityFile + "/new');\n" +
                    "    cy.get('h2, h1').should('contain', '" + entityClass + "');\n" +
                    "    // Fill form fields here\n" +
                    "    cy.get('button[type=\"submit\"]').click();\n" +
                    "    cy.url().should('include', '/" + entityFile + "');\n" +
                    "  });",
                entityClass,
                entityFile,
                entityFile,
                entity.getReadOnly() ? "" :
                    "it('should edit a " + entityClass + "', () => {\n" +
                    "    cy.visit('/" + entityFile + "');\n" +
                    "    cy.get('a[href*=\"edit\"], button').contains('Edit').first().click({ force: true });\n" +
                    "    cy.url().should('include', '/edit');\n" +
                    "  });\n\n" +
                    "  it('should delete a " + entityClass + "', () => {\n" +
                    "    cy.visit('/" + entityFile + "');\n" +
                    "    cy.get('button').contains('Delete').first().click({ force: true });\n" +
                    "    // Confirm deletion if modal appears\n" +
                    "    cy.get('button').contains('Delete').click({ force: true });\n" +
                    "  });"
        );

        writeFile("cypress/e2e/entity/" + entityFile + ".cy.ts", testContent);
    }

    private void end() {
        log.info("Cypress generation completed!");
        log.info("");
        log.info("==========================================================");
        log.info("Cypress E2E tests generated successfully!");
        log.info("==========================================================");
        log.info("");
        log.info("To run E2E tests:");
        log.info("  npm run e2e");
        log.info("  npm run e2e:open (interactive)");
        log.info("");
    }
}
