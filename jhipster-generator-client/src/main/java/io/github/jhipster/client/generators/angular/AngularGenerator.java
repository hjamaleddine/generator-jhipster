/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.generators.angular;

import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientConfig.EntityConfig;
import io.github.jhipster.client.config.ClientConfig.FieldConfig;
import io.github.jhipster.client.config.ClientConfig.RelationshipConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.core.BaseClientGenerator;
import io.github.jhipster.client.utils.NeedleService;

import java.io.IOException;
import java.util.*;

/**
 * Angular Generator.
 * Generates Angular frontend application with standalone components.
 * Equivalent to generators/angular/generator.ts.
 */
public class AngularGenerator extends BaseClientGenerator {

    private NeedleService needleService;

    public AngularGenerator(ClientGeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "angular";
    }

    @Override
    protected void registerTasks() {
        registerTask(GeneratorPhase.INITIALIZING, this::initializing);
        registerTask(GeneratorPhase.LOADING, this::loading);
        registerTask(GeneratorPhase.PREPARING, this::preparing);
        registerTask(GeneratorPhase.WRITING, this::writing);
        registerTask(GeneratorPhase.WRITING_ENTITIES, this::writingEntities);
        registerTask(GeneratorPhase.POST_WRITING, this::postWriting);
        registerTask(GeneratorPhase.POST_WRITING_ENTITIES, this::postWritingEntities);
        registerTask(GeneratorPhase.END, this::end);
    }

    private void initializing() {
        log.info("Initializing Angular generator");
        this.needleService = new NeedleService(context.getDestinationRoot());
    }

    private void loading() {
        log.info("Loading Angular dependencies");

        // Add Angular-specific template data
        context.addTemplateData("angularVersion", "17.0.0");
        context.addTemplateData("typescriptVersion", "5.2.2");
        context.addTemplateData("rxjsVersion", "7.8.1");
    }

    private void preparing() {
        log.info("Preparing Angular generation context");

        // Prepare entities for Angular
        for (EntityConfig entity : getConfig().getEntities()) {
            prepareEntityForAngular(entity);
        }
    }

    private void prepareEntityForAngular(EntityConfig entity) {
        // Ensure Angular-specific names are set
        if (entity.getEntityAngularName() == null) {
            entity.setEntityAngularName(capitalize(entity.getName()));
        }
    }

    private void writing() {
        log.info("Writing Angular application files");

        try {
            // Core application files
            writeAppComponent();
            writeAppConfig();
            writeAppRoutes();
            writeMainTs();

            // Config files
            writeAngularJson();
            writeTsConfig();
            writePackageJson();

            // Layouts
            writeNavbarComponent();
            writeFooterComponent();

            // Core services
            writeAuthService();
            writeAlertService();
            writeHttpInterceptors();

            // Shared components
            writeSharedModule();

            // Account module (if not OAuth2)
            if (!getConfig().isOAuth2()) {
                writeAccountModule();
            }

            // Admin module
            writeAdminModule();

            // Home module
            writeHomeModule();

            // Webpack/ESBuild configuration
            if (getConfig().isWebpack()) {
                writeWebpackConfig();
            } else if (getConfig().isEsbuild()) {
                writeEsbuildConfig();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error writing Angular files", e);
        }
    }

    private void writeAppComponent() throws IOException {
        String content = """
            import { Component, OnInit, inject } from '@angular/core';
            import { RouterOutlet } from '@angular/router';
            import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
            %s
            import { NavbarComponent } from './layouts/navbar/navbar.component';
            import { FooterComponent } from './layouts/footer/footer.component';
            import { AlertComponent } from './shared/alert/alert.component';
            import { AlertErrorComponent } from './shared/alert/alert-error.component';

            @Component({
              selector: 'jhi-root',
              standalone: true,
              imports: [
                RouterOutlet,
                NgbModule,
                NavbarComponent,
                FooterComponent,
                AlertComponent,
                AlertErrorComponent,
                %s
              ],
              template: `
                <jhi-navbar></jhi-navbar>
                <div class="container-fluid">
                  <div class="card jh-card">
                    <jhi-alert></jhi-alert>
                    <jhi-alert-error></jhi-alert-error>
                    <router-outlet></router-outlet>
                  </div>
                  <jhi-footer></jhi-footer>
                </div>
              `
            })
            export class AppComponent implements OnInit {
              %s

              ngOnInit(): void {
                %s
              }
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                getConfig().getEnableTranslation() ? "TranslateModule," : "",
                "",
                ""
        );
        writeFile(getConfig().getClientWebappDir() + "app.component.ts", content);
    }

    private void writeAppConfig() throws IOException {
        String content = """
            import { ApplicationConfig, importProvidersFrom } from '@angular/core';
            import { provideRouter, withComponentInputBinding } from '@angular/router';
            import { provideHttpClient, withInterceptors } from '@angular/common/http';
            import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
            %s

            import { routes } from './app.routes';
            import { authInterceptor } from './core/interceptor/auth.interceptor';
            import { errorInterceptor } from './core/interceptor/error.interceptor';
            %s

            export const appConfig: ApplicationConfig = {
              providers: [
                provideRouter(routes, withComponentInputBinding()),
                provideHttpClient(
                  withInterceptors([authInterceptor, errorInterceptor%s])
                ),
                importProvidersFrom(NgbModule),
                %s
              ],
            };
            """.formatted(
                getConfig().getEnableTranslation() ?
                    "import { TranslateModule, TranslateLoader, MissingTranslationHandler } from '@ngx-translate/core';\n" +
                    "import { HttpClient } from '@angular/common/http';\n" +
                    "import { TranslateHttpLoader } from '@ngx-translate/http-loader';\n" +
                    "import { missingTranslationHandler } from './config/translation.config';" : "",
                getConfig().getEnableTranslation() ?
                    "export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {\n" +
                    "  return new TranslateHttpLoader(http, 'i18n/', '.json');\n}" : "",
                "",
                getConfig().getEnableTranslation() ?
                    "importProvidersFrom(\n" +
                    "  TranslateModule.forRoot({\n" +
                    "    loader: { provide: TranslateLoader, useFactory: HttpLoaderFactory, deps: [HttpClient] },\n" +
                    "    missingTranslationHandler: { provide: MissingTranslationHandler, useFactory: missingTranslationHandler },\n" +
                    "  })\n)," : ""
        );
        writeFile(getConfig().getClientWebappDir() + "app.config.ts", content);
    }

    private void writeAppRoutes() throws IOException {
        String content = """
            import { Routes } from '@angular/router';

            import { HomeComponent } from './home/home.component';
            import { Authority } from './config/authority.constants';
            import { UserRouteAccessService } from './core/auth/user-route-access.service';
            import { errorRoute } from './layouts/error/error.route';

            export const routes: Routes = [
              {
                path: '',
                component: HomeComponent,
                title: 'home.title',
              },
              {
                path: 'admin',
                loadChildren: () => import('./admin/admin.routes').then(m => m.adminRoutes),
                data: {
                  authorities: [Authority.ADMIN],
                },
                canActivate: [UserRouteAccessService],
              },
              %s
              {
                path: 'entity',
                loadChildren: () => import('./entities/entity.routes').then(m => m.entityRoutes),
              },
              ...errorRoute,
            ];
            """.formatted(
                getConfig().isOAuth2() ? "" :
                    "{\n" +
                    "  path: 'account',\n" +
                    "  loadChildren: () => import('./account/account.routes').then(m => m.accountRoutes),\n" +
                    "},"
        );
        writeFile(getConfig().getClientWebappDir() + "app.routes.ts", content);
    }

    private void writeMainTs() throws IOException {
        String content = """
            import { bootstrapApplication } from '@angular/platform-browser';
            import { AppComponent } from './app/app.component';
            import { appConfig } from './app/app.config';

            bootstrapApplication(AppComponent, appConfig)
              .catch(err => console.error(err));
            """;
        writeFile(getConfig().getClientSrcDir() + "main.ts", content);
    }

    private void writeAngularJson() throws IOException {
        String bundler = getConfig().isEsbuild() ? "esbuild" : "webpack";
        String content = """
            {
              "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
              "version": 1,
              "newProjectRoot": "projects",
              "projects": {
                "%s": {
                  "projectType": "application",
                  "root": "",
                  "sourceRoot": "src/main/webapp",
                  "prefix": "jhi",
                  "architect": {
                    "build": {
                      "builder": "@angular-devkit/build-angular:application",
                      "options": {
                        "outputPath": "target/classes/static",
                        "index": "src/main/webapp/index.html",
                        "browser": "src/main/webapp/main.ts",
                        "polyfills": ["zone.js"],
                        "tsConfig": "tsconfig.app.json",
                        "inlineStyleLanguage": "scss",
                        "assets": [
                          "src/main/webapp/content",
                          "src/main/webapp/favicon.ico",
                          "src/main/webapp/manifest.webapp",
                          "src/main/webapp/robots.txt"
                        ],
                        "styles": [
                          "src/main/webapp/content/scss/vendor.scss",
                          "src/main/webapp/content/scss/global.scss"
                        ],
                        "scripts": []
                      },
                      "configurations": {
                        "production": {
                          "budgets": [
                            { "type": "initial", "maximumWarning": "500kb", "maximumError": "1mb" },
                            { "type": "anyComponentStyle", "maximumWarning": "2kb", "maximumError": "4kb" }
                          ],
                          "outputHashing": "all"
                        },
                        "development": {
                          "optimization": false,
                          "extractLicenses": false,
                          "sourceMap": true
                        }
                      },
                      "defaultConfiguration": "production"
                    },
                    "serve": {
                      "builder": "@angular-devkit/build-angular:dev-server",
                      "configurations": {
                        "production": { "buildTarget": "%s:build:production" },
                        "development": { "buildTarget": "%s:build:development" }
                      },
                      "defaultConfiguration": "development",
                      "options": {
                        "port": %d,
                        "proxyConfig": "proxy.conf.json"
                      }
                    },
                    "test": {
                      "builder": "@angular-devkit/build-angular:karma",
                      "options": {
                        "main": "src/test/javascript/spec/test.component.ts",
                        "polyfills": ["zone.js", "zone.js/testing"],
                        "tsConfig": "tsconfig.spec.json",
                        "karmaConfig": "karma.conf.js",
                        "inlineStyleLanguage": "scss",
                        "styles": [
                          "src/main/webapp/content/scss/vendor.scss",
                          "src/main/webapp/content/scss/global.scss"
                        ]
                      }
                    }
                  }
                }
              },
              "cli": {
                "analytics": false
              }
            }
            """.formatted(
                getConfig().getBaseName(),
                getConfig().getBaseName(),
                getConfig().getBaseName(),
                getConfig().getDevServerPort()
        );
        writeFile("angular.json", content);
    }

    private void writeTsConfig() throws IOException {
        String content = """
            {
              "compileOnSave": false,
              "compilerOptions": {
                "baseUrl": "./",
                "outDir": "./dist/out-tsc",
                "strict": true,
                "noImplicitOverride": true,
                "noPropertyAccessFromIndexSignature": true,
                "noImplicitReturns": true,
                "noFallthroughCasesInSwitch": true,
                "sourceMap": true,
                "declaration": false,
                "downlevelIteration": true,
                "experimentalDecorators": true,
                "moduleResolution": "node",
                "importHelpers": true,
                "target": "ES2022",
                "module": "ES2022",
                "useDefineForClassFields": false,
                "lib": ["ES2022", "dom"],
                "paths": {
                  "app/*": ["src/main/webapp/app/*"]
                }
              },
              "angularCompilerOptions": {
                "enableI18nLegacyMessageIdFormat": false,
                "strictInjectionParameters": true,
                "strictInputAccessModifiers": true,
                "strictTemplates": true
              }
            }
            """;
        writeFile("tsconfig.json", content);

        String appTsConfig = """
            {
              "extends": "./tsconfig.json",
              "compilerOptions": {
                "outDir": "./out-tsc/app",
                "types": []
              },
              "files": ["src/main/webapp/main.ts"],
              "include": ["src/main/webapp/**/*.d.ts"]
            }
            """;
        writeFile("tsconfig.app.json", appTsConfig);
    }

    private void writePackageJson() throws IOException {
        String content = """
            {
              "name": "%s",
              "version": "0.0.1-SNAPSHOT",
              "private": true,
              "description": "JHipster application generated with Angular",
              "license": "UNLICENSED",
              "scripts": {
                "ng": "ng",
                "start": "ng serve",
                "build": "ng build",
                "watch": "ng build --watch --configuration development",
                "test": "ng test",
                "lint": "eslint . --ext .ts",
                "e2e": "cypress run",
                "e2e:open": "cypress open"
              },
              "dependencies": {
                "@angular/animations": "^17.0.0",
                "@angular/common": "^17.0.0",
                "@angular/compiler": "^17.0.0",
                "@angular/core": "^17.0.0",
                "@angular/forms": "^17.0.0",
                "@angular/platform-browser": "^17.0.0",
                "@angular/platform-browser-dynamic": "^17.0.0",
                "@angular/router": "^17.0.0",
                "@fortawesome/angular-fontawesome": "^0.14.0",
                "@fortawesome/fontawesome-svg-core": "^6.4.2",
                "@fortawesome/free-solid-svg-icons": "^6.4.2",
                "@ng-bootstrap/ng-bootstrap": "^16.0.0",
                "@popperjs/core": "^2.11.8",
                "bootstrap": "^5.3.2",
                "dayjs": "^1.11.10",
                %s
                "rxjs": "^7.8.1",
                "tslib": "^2.6.2",
                "zone.js": "^0.14.2"
              },
              "devDependencies": {
                "@angular-devkit/build-angular": "^17.0.0",
                "@angular/cli": "^17.0.0",
                "@angular/compiler-cli": "^17.0.0",
                "@types/jasmine": "~5.1.0",
                "@types/node": "^20.10.0",
                "jasmine-core": "~5.1.0",
                "karma": "~6.4.0",
                "karma-chrome-launcher": "~3.2.0",
                "karma-coverage": "~2.2.0",
                "karma-jasmine": "~5.1.0",
                "karma-jasmine-html-reporter": "~2.1.0",
                "typescript": "~5.2.2",
                %s
                "eslint": "^8.54.0"
              }
            }
            """.formatted(
                getConfig().getBaseName().toLowerCase(),
                getConfig().getEnableTranslation() ?
                    "\"@ngx-translate/core\": \"^15.0.0\",\n    \"@ngx-translate/http-loader\": \"^8.0.0\"," : "",
                getConfig().hasCypress() ? "\"cypress\": \"^13.6.0\"," : ""
        );
        writeFile("package.json", content);
    }

    private void writeNavbarComponent() throws IOException {
        String content = """
            import { Component, OnInit, inject } from '@angular/core';
            import { Router, RouterModule } from '@angular/router';
            import { NgbCollapseModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
            %s
            import { AccountService } from 'app/core/auth/account.service';
            import { LoginService } from 'app/login/login.service';

            @Component({
              selector: 'jhi-navbar',
              standalone: true,
              imports: [RouterModule, NgbCollapseModule, NgbDropdownModule%s],
              templateUrl: './navbar.component.html',
              styleUrl: './navbar.component.scss'
            })
            export class NavbarComponent implements OnInit {
              isNavbarCollapsed = true;
              isAuthenticated = false;

              private accountService = inject(AccountService);
              private loginService = inject(LoginService);
              private router = inject(Router);

              ngOnInit(): void {
                this.accountService.getAuthenticationState().subscribe(account => {
                  this.isAuthenticated = account !== null;
                });
              }

              toggleNavbar(): void {
                this.isNavbarCollapsed = !this.isNavbarCollapsed;
              }

              login(): void {
                this.loginService.login();
              }

              logout(): void {
                this.loginService.logout();
                this.router.navigate(['']);
              }
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                getConfig().getEnableTranslation() ? ", TranslateModule" : ""
        );
        writeFile(getConfig().getClientWebappDir() + "layouts/navbar/navbar.component.ts", content);

        // Write navbar template
        String template = """
            <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
              <div class="container-fluid">
                <a class="navbar-brand" routerLink="/">
                  <span class="navbar-title">%s</span>
                </a>
                <button class="navbar-toggler" type="button" (click)="toggleNavbar()">
                  <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" [ngbCollapse]="isNavbarCollapsed">
                  <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                      <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
                        <span>%s</span>
                      </a>
                    </li>
                    @if (isAuthenticated) {
                      <li ngbDropdown class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" ngbDropdownToggle>
                          <span>%s</span>
                        </a>
                        <ul ngbDropdownMenu class="dropdown-menu">
                          <!-- jhipster-needle-add-entity-to-menu - JHipster will add entities here -->
                        </ul>
                      </li>
                    }
                  </ul>
                  <ul class="navbar-nav">
                    @if (!isAuthenticated) {
                      <li class="nav-item">
                        <a class="nav-link" (click)="login()">
                          <span>%s</span>
                        </a>
                      </li>
                    }
                    @if (isAuthenticated) {
                      <li class="nav-item">
                        <a class="nav-link" (click)="logout()">
                          <span>%s</span>
                        </a>
                      </li>
                    }
                  </ul>
                </div>
              </div>
            </nav>
            """.formatted(
                getConfig().getBaseName(),
                getConfig().getEnableTranslation() ? "{{ 'global.menu.home' | translate }}" : "Home",
                getConfig().getEnableTranslation() ? "{{ 'global.menu.entities.main' | translate }}" : "Entities",
                getConfig().getEnableTranslation() ? "{{ 'global.menu.account.login' | translate }}" : "Sign in",
                getConfig().getEnableTranslation() ? "{{ 'global.menu.account.logout' | translate }}" : "Sign out"
        );
        writeFile(getConfig().getClientWebappDir() + "layouts/navbar/navbar.component.html", template);

        // Write navbar SCSS
        String scss = """
            .navbar-brand {
              .navbar-title {
                font-weight: bold;
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "layouts/navbar/navbar.component.scss", scss);
    }

    private void writeFooterComponent() throws IOException {
        String content = """
            import { Component } from '@angular/core';

            @Component({
              selector: 'jhi-footer',
              standalone: true,
              template: `
                <footer class="footer">
                  <div class="container">
                    <p class="text-muted">
                      &copy; %s - Powered by JHipster
                    </p>
                  </div>
                </footer>
              `,
              styles: [`
                .footer {
                  position: fixed;
                  bottom: 0;
                  width: 100%%;
                  height: 60px;
                  line-height: 60px;
                  background-color: #f5f5f5;
                }
              `]
            })
            export class FooterComponent {}
            """.formatted(getConfig().getBaseName());
        writeFile(getConfig().getClientWebappDir() + "layouts/footer/footer.component.ts", content);
    }

    private void writeAuthService() throws IOException {
        String content = """
            import { Injectable, inject } from '@angular/core';
            import { HttpClient } from '@angular/common/http';
            import { BehaviorSubject, Observable, ReplaySubject, of } from 'rxjs';
            import { shareReplay, tap, catchError, map } from 'rxjs/operators';

            export interface Account {
              activated: boolean;
              authorities: string[];
              email: string;
              firstName: string;
              langKey: string;
              lastName: string;
              login: string;
            }

            @Injectable({ providedIn: 'root' })
            export class AccountService {
              private userIdentity: Account | null = null;
              private authenticationState = new ReplaySubject<Account | null>(1);
              private accountCache$?: Observable<Account | null>;

              private http = inject(HttpClient);

              authenticate(identity: Account | null): void {
                this.userIdentity = identity;
                this.authenticationState.next(this.userIdentity);
                if (!identity) {
                  this.accountCache$ = undefined;
                }
              }

              hasAnyAuthority(authorities: string[] | string): boolean {
                if (!this.userIdentity || !this.userIdentity.authorities) {
                  return false;
                }
                if (!Array.isArray(authorities)) {
                  authorities = [authorities];
                }
                return this.userIdentity.authorities.some(auth => authorities.includes(auth));
              }

              identity(force?: boolean): Observable<Account | null> {
                if (force) {
                  this.accountCache$ = undefined;
                }
                if (!this.accountCache$) {
                  this.accountCache$ = this.fetch().pipe(
                    tap(account => {
                      this.authenticate(account);
                    }),
                    shareReplay()
                  );
                }
                return this.accountCache$;
              }

              isAuthenticated(): boolean {
                return this.userIdentity !== null;
              }

              getAuthenticationState(): Observable<Account | null> {
                return this.authenticationState.asObservable();
              }

              private fetch(): Observable<Account | null> {
                return this.http.get<Account>('/api/account').pipe(
                  catchError(() => of(null))
                );
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "core/auth/account.service.ts", content);

        // Write login service
        String loginServiceContent = getConfig().isOAuth2() ?
            """
            import { Injectable, inject } from '@angular/core';
            import { Location } from '@angular/common';

            @Injectable({ providedIn: 'root' })
            export class LoginService {
              private location = inject(Location);

              login(): void {
                window.location.href = '/oauth2/authorization/oidc';
              }

              logout(): void {
                window.location.href = '/api/logout';
              }
            }
            """ :
            """
            import { Injectable, inject } from '@angular/core';
            import { HttpClient } from '@angular/common/http';
            import { Observable } from 'rxjs';
            import { map } from 'rxjs/operators';
            import { AccountService } from 'app/core/auth/account.service';

            export interface Login {
              username: string;
              password: string;
              rememberMe: boolean;
            }

            @Injectable({ providedIn: 'root' })
            export class LoginService {
              private http = inject(HttpClient);
              private accountService = inject(AccountService);

              login(credentials: Login): Observable<void> {
                return this.http.post('/api/authenticate', credentials).pipe(
                  map(() => this.accountService.identity(true).subscribe())
                );
              }

              logout(): Observable<void> {
                return this.http.post<void>('/api/logout', {}).pipe(
                  map(() => this.accountService.authenticate(null))
                );
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "login/login.service.ts", loginServiceContent);
    }

    private void writeAlertService() throws IOException {
        String content = """
            import { Injectable } from '@angular/core';
            import { Observable, Subject } from 'rxjs';

            export interface Alert {
              type: 'success' | 'danger' | 'warning' | 'info';
              message: string;
              timeout?: number;
            }

            @Injectable({ providedIn: 'root' })
            export class AlertService {
              private alertSubject = new Subject<Alert>();

              get(): Observable<Alert> {
                return this.alertSubject.asObservable();
              }

              addAlert(alert: Alert): void {
                this.alertSubject.next(alert);
              }

              success(message: string): void {
                this.addAlert({ type: 'success', message });
              }

              error(message: string): void {
                this.addAlert({ type: 'danger', message });
              }

              warning(message: string): void {
                this.addAlert({ type: 'warning', message });
              }

              info(message: string): void {
                this.addAlert({ type: 'info', message });
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "core/util/alert.service.ts", content);

        // Write Alert component
        String alertComponent = """
            import { Component, OnInit, OnDestroy, inject } from '@angular/core';
            import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';
            import { Subscription } from 'rxjs';
            import { Alert, AlertService } from 'app/core/util/alert.service';

            @Component({
              selector: 'jhi-alert',
              standalone: true,
              imports: [NgbAlertModule],
              template: `
                @for (alert of alerts; track alert) {
                  <ngb-alert [type]="alert.type" (closed)="close(alert)">
                    {{ alert.message }}
                  </ngb-alert>
                }
              `
            })
            export class AlertComponent implements OnInit, OnDestroy {
              alerts: Alert[] = [];
              private subscription?: Subscription;
              private alertService = inject(AlertService);

              ngOnInit(): void {
                this.subscription = this.alertService.get().subscribe(alert => {
                  this.alerts.push(alert);
                  if (alert.timeout) {
                    setTimeout(() => this.close(alert), alert.timeout);
                  }
                });
              }

              ngOnDestroy(): void {
                this.subscription?.unsubscribe();
              }

              close(alert: Alert): void {
                const index = this.alerts.indexOf(alert);
                if (index > -1) {
                  this.alerts.splice(index, 1);
                }
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/alert/alert.component.ts", alertComponent);

        // Write Alert Error component
        String alertErrorComponent = """
            import { Component, OnDestroy, inject } from '@angular/core';
            import { HttpErrorResponse } from '@angular/common/http';
            import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';
            import { Subscription } from 'rxjs';
            import { EventManagerService } from 'app/core/util/event-manager.service';

            @Component({
              selector: 'jhi-alert-error',
              standalone: true,
              imports: [NgbAlertModule],
              template: `
                @for (alert of alerts; track alert) {
                  <ngb-alert type="danger" (closed)="close(alert)">
                    {{ alert.message }}
                  </ngb-alert>
                }
              `
            })
            export class AlertErrorComponent implements OnDestroy {
              alerts: { message: string }[] = [];
              private subscription: Subscription;
              private eventManager = inject(EventManagerService);

              constructor() {
                this.subscription = this.eventManager.subscribe('httpError', (response: HttpErrorResponse) => {
                  const message = response.error?.message || response.message || 'An error occurred';
                  this.alerts.push({ message });
                });
              }

              ngOnDestroy(): void {
                this.subscription.unsubscribe();
              }

              close(alert: { message: string }): void {
                const index = this.alerts.indexOf(alert);
                if (index > -1) {
                  this.alerts.splice(index, 1);
                }
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/alert/alert-error.component.ts", alertErrorComponent);
    }

    private void writeHttpInterceptors() throws IOException {
        // Auth interceptor
        String authInterceptor = """
            import { HttpInterceptorFn } from '@angular/common/http';

            export const authInterceptor: HttpInterceptorFn = (req, next) => {
              const token = localStorage.getItem('authToken');
              if (token) {
                req = req.clone({
                  setHeaders: {
                    Authorization: `Bearer ${token}`
                  }
                });
              }
              return next(req);
            };
            """;
        writeFile(getConfig().getClientWebappDir() + "core/interceptor/auth.interceptor.ts", authInterceptor);

        // Error interceptor
        String errorInterceptor = """
            import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
            import { inject } from '@angular/core';
            import { catchError, throwError } from 'rxjs';
            import { EventManagerService } from 'app/core/util/event-manager.service';

            export const errorInterceptor: HttpInterceptorFn = (req, next) => {
              const eventManager = inject(EventManagerService);

              return next(req).pipe(
                catchError((error: HttpErrorResponse) => {
                  if (error.status !== 401 && error.status !== 403) {
                    eventManager.broadcast({ name: 'httpError', content: error });
                  }
                  return throwError(() => error);
                })
              );
            };
            """;
        writeFile(getConfig().getClientWebappDir() + "core/interceptor/error.interceptor.ts", errorInterceptor);

        // Event manager service
        String eventManager = """
            import { Injectable } from '@angular/core';
            import { Subject, Observable, Subscription, filter, map } from 'rxjs';

            export interface EventWithContent<T> {
              name: string;
              content: T;
            }

            @Injectable({ providedIn: 'root' })
            export class EventManagerService {
              private eventSubject = new Subject<EventWithContent<any>>();

              broadcast<T>(event: EventWithContent<T>): void {
                this.eventSubject.next(event);
              }

              subscribe<T>(eventName: string, callback: (payload: T) => void): Subscription {
                return this.eventSubject.pipe(
                  filter(event => event.name === eventName),
                  map(event => event.content)
                ).subscribe(callback);
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "core/util/event-manager.service.ts", eventManager);
    }

    private void writeSharedModule() throws IOException {
        // User route access service
        String userRouteAccess = """
            import { Injectable, inject } from '@angular/core';
            import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
            import { Observable, map, tap } from 'rxjs';
            import { AccountService } from './account.service';

            @Injectable({ providedIn: 'root' })
            export class UserRouteAccessService {
              private accountService = inject(AccountService);
              private router = inject(Router);

              canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
                return this.accountService.identity().pipe(
                  map(account => {
                    if (account) {
                      const authorities = route.data['authorities'];
                      if (!authorities || authorities.length === 0) {
                        return true;
                      }
                      return this.accountService.hasAnyAuthority(authorities);
                    }
                    return false;
                  }),
                  tap(hasAccess => {
                    if (!hasAccess) {
                      this.router.navigate(['/accessdenied']);
                    }
                  })
                );
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "core/auth/user-route-access.service.ts", userRouteAccess);

        // Authority constants
        String authorityConstants = """
            export const Authority = {
              ADMIN: 'ROLE_ADMIN',
              USER: 'ROLE_USER',
            };
            """;
        writeFile(getConfig().getClientWebappDir() + "config/authority.constants.ts", authorityConstants);
    }

    private void writeAccountModule() throws IOException {
        // Account routes
        String accountRoutes = """
            import { Routes } from '@angular/router';

            export const accountRoutes: Routes = [
              {
                path: 'login',
                loadComponent: () => import('./login/login.component').then(m => m.LoginComponent),
                title: 'login.title',
              },
              {
                path: 'register',
                loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent),
                title: 'register.title',
              },
              {
                path: 'activate',
                loadComponent: () => import('./activate/activate.component').then(m => m.ActivateComponent),
                title: 'activate.title',
              },
              {
                path: 'password',
                loadComponent: () => import('./password/password.component').then(m => m.PasswordComponent),
                title: 'password.title',
              },
              {
                path: 'settings',
                loadComponent: () => import('./settings/settings.component').then(m => m.SettingsComponent),
                title: 'settings.title',
              },
            ];
            """;
        writeFile(getConfig().getClientWebappDir() + "account/account.routes.ts", accountRoutes);
    }

    private void writeAdminModule() throws IOException {
        // Admin routes
        String adminRoutes = """
            import { Routes } from '@angular/router';
            import { Authority } from 'app/config/authority.constants';
            import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

            export const adminRoutes: Routes = [
              {
                path: 'user-management',
                loadChildren: () => import('./user-management/user-management.routes').then(m => m.userManagementRoutes),
                data: { authorities: [Authority.ADMIN] },
                canActivate: [UserRouteAccessService],
              },
              {
                path: 'health',
                loadComponent: () => import('./health/health.component').then(m => m.HealthComponent),
                data: { authorities: [Authority.ADMIN] },
                canActivate: [UserRouteAccessService],
              },
              {
                path: 'logs',
                loadComponent: () => import('./logs/logs.component').then(m => m.LogsComponent),
                data: { authorities: [Authority.ADMIN] },
                canActivate: [UserRouteAccessService],
              },
              {
                path: 'configuration',
                loadComponent: () => import('./configuration/configuration.component').then(m => m.ConfigurationComponent),
                data: { authorities: [Authority.ADMIN] },
                canActivate: [UserRouteAccessService],
              },
              {
                path: 'metrics',
                loadComponent: () => import('./metrics/metrics.component').then(m => m.MetricsComponent),
                data: { authorities: [Authority.ADMIN] },
                canActivate: [UserRouteAccessService],
              },
              {
                path: 'docs',
                loadComponent: () => import('./docs/docs.component').then(m => m.DocsComponent),
                data: { authorities: [Authority.ADMIN] },
                canActivate: [UserRouteAccessService],
              },
            ];
            """;
        writeFile(getConfig().getClientWebappDir() + "admin/admin.routes.ts", adminRoutes);
    }

    private void writeHomeModule() throws IOException {
        String homeComponent = """
            import { Component, OnInit, inject } from '@angular/core';
            import { RouterModule } from '@angular/router';
            %s
            import { AccountService } from 'app/core/auth/account.service';

            @Component({
              selector: 'jhi-home',
              standalone: true,
              imports: [RouterModule%s],
              templateUrl: './home.component.html',
              styleUrl: './home.component.scss'
            })
            export class HomeComponent implements OnInit {
              account$ = inject(AccountService).getAuthenticationState();

              ngOnInit(): void {}
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                getConfig().getEnableTranslation() ? ", TranslateModule" : ""
        );
        writeFile(getConfig().getClientWebappDir() + "home/home.component.ts", homeComponent);

        String homeTemplate = """
            <div class="row">
              <div class="col-md-9">
                <h1 class="display-4">%s</h1>
                <p class="lead">%s</p>
                @if ((account$ | async); as account) {
                  <div class="alert alert-success">
                    %s
                  </div>
                } @else {
                  <div class="alert alert-warning">
                    %s
                  </div>
                }
              </div>
            </div>
            """.formatted(
                getConfig().getEnableTranslation() ? "{{ 'home.title' | translate }}" : "Welcome, " + getConfig().getBaseName() + "!",
                getConfig().getEnableTranslation() ? "{{ 'home.subtitle' | translate }}" : "This is your homepage",
                getConfig().getEnableTranslation() ? "{{ 'home.logged.message' | translate: { username: account.login } }}" : "You are logged in as {{ account.login }}",
                getConfig().getEnableTranslation() ? "{{ 'home.notLogged.message' | translate }}" : "You are not signed in yet."
        );
        writeFile(getConfig().getClientWebappDir() + "home/home.component.html", homeTemplate);

        String homeScss = """
            .hipster {
              display: inline-block;
              width: 100%;
              height: 497px;
              background-size: contain;
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "home/home.component.scss", homeScss);
    }

    private void writeWebpackConfig() throws IOException {
        // Webpack common
        String webpackCommon = """
            const webpack = require('webpack');
            const { merge } = require('webpack-merge');
            const path = require('path');
            const HtmlWebpackPlugin = require('html-webpack-plugin');
            const CopyWebpackPlugin = require('copy-webpack-plugin');

            module.exports = {
              entry: {
                main: './src/main/webapp/main.ts',
              },
              output: {
                path: path.resolve('./target/classes/static/'),
                filename: '[name].[contenthash].bundle.js',
              },
              resolve: {
                extensions: ['.ts', '.js'],
                alias: {
                  app: path.resolve('./src/main/webapp/app/'),
                },
              },
              module: {
                rules: [
                  {
                    test: /\\.ts$/,
                    use: ['ts-loader', '@angular-devkit/build-optimizer/webpack-loader'],
                    exclude: /node_modules/,
                  },
                  {
                    test: /\\.scss$/,
                    use: ['style-loader', 'css-loader', 'sass-loader'],
                  },
                  {
                    test: /\\.css$/,
                    use: ['style-loader', 'css-loader'],
                  },
                ],
              },
              plugins: [
                new HtmlWebpackPlugin({
                  template: './src/main/webapp/index.html',
                }),
                new CopyWebpackPlugin({
                  patterns: [
                    { from: './src/main/webapp/content/', to: 'content/' },
                    { from: './src/main/webapp/manifest.webapp', to: 'manifest.webapp' },
                    { from: './src/main/webapp/favicon.ico', to: 'favicon.ico' },
                  ],
                }),
                // jhipster-needle-add-webpack-config
              ],
            };
            """;
        writeFile("webpack/webpack.common.js", webpackCommon);
    }

    private void writeEsbuildConfig() throws IOException {
        // ESBuild proxy config
        String proxyConfig = """
            export default [
              {
                context: ['/api', '/services', '/management', '/v3/api-docs', '/h2-console', '/health'],
                target: 'http://localhost:8080',
                secure: false,
              },
            ];
            """;
        writeFile("proxy.config.mjs", proxyConfig);
    }

    private void writingEntities() {
        log.info("Writing Angular entity files");

        for (EntityConfig entity : getConfig().getEntities()) {
            if (entity.getBuiltIn() || entity.getEmbedded()) {
                continue;
            }

            try {
                writeEntityFiles(entity);
            } catch (IOException e) {
                log.error("Error writing entity files for: {}", entity.getName(), e);
            }
        }
    }

    private void writeEntityFiles(EntityConfig entity) throws IOException {
        String entityFolder = entity.getEntityFolderName();
        String entityFile = entity.getEntityFileName();
        String entityClass = entity.getEntityAngularName();
        String basePath = getConfig().getClientWebappDir() + "entities/" + entityFolder + "/";

        // Entity model
        writeEntityModel(entity, basePath);

        // Entity service
        writeEntityService(entity, basePath);

        // Entity routes
        writeEntityRoutes(entity, basePath);

        // List component
        writeEntityListComponent(entity, basePath);

        // Detail component
        writeEntityDetailComponent(entity, basePath);

        // If not read-only, generate update and delete components
        if (!entity.getReadOnly()) {
            writeEntityUpdateComponent(entity, basePath);
            writeEntityDeleteComponent(entity, basePath);
        }
    }

    private void writeEntityModel(EntityConfig entity, String basePath) throws IOException {
        StringBuilder content = new StringBuilder();

        // Imports
        if (entity.hasDateField()) {
            content.append("import dayjs from 'dayjs/esm';\n");
        }

        // Import related entities
        Set<String> imports = new HashSet<>();
        for (RelationshipConfig rel : entity.getRelationships()) {
            imports.add(rel.getOtherEntityAngularName());
        }
        for (String imp : imports) {
            content.append("import { I").append(imp).append(" } from '../")
                   .append(toKebabCase(imp)).append("/").append(toKebabCase(imp)).append(".model';\n");
        }

        if (!imports.isEmpty() || entity.hasDateField()) {
            content.append("\n");
        }

        // Interface
        content.append("export interface I").append(entity.getEntityAngularName()).append(" {\n");

        // ID field
        content.append("  id: number | null;\n");

        // Fields
        for (FieldConfig field : entity.getFields()) {
            String optional = field.getRequired() ? "" : "?";
            content.append("  ").append(field.getFieldName()).append(optional).append(": ")
                   .append(field.getTsType()).append(";\n");
        }

        // Relationships
        for (RelationshipConfig rel : entity.getRelationships()) {
            String type = "I" + rel.getOtherEntityAngularName();
            if (rel.isCollection()) {
                type += "[]";
            }
            content.append("  ").append(rel.getRelationshipName()).append("?: ").append(type).append(" | null;\n");
        }

        content.append("}\n\n");

        // New entity type (without ID)
        content.append("export type New").append(entity.getEntityAngularName())
               .append(" = Omit<I").append(entity.getEntityAngularName()).append(", 'id'> & { id: null };\n");

        writeFile(basePath + entity.getEntityFileName() + ".model.ts", content.toString());
    }

    private void writeEntityService(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityUrl = toKebabCase(entity.getName());

        String content = """
            import { Injectable, inject } from '@angular/core';
            import { HttpClient, HttpResponse } from '@angular/common/http';
            import { Observable%s } from 'rxjs';
            import { I%s, New%s } from './%s.model';

            export type EntityResponseType = HttpResponse<I%s>;
            export type EntityArrayResponseType = HttpResponse<I%s[]>;

            @Injectable({ providedIn: 'root' })
            export class %sService {
              private readonly resourceUrl = '/api/%s';
              private http = inject(HttpClient);

              create(%s: New%s): Observable<EntityResponseType> {
                return this.http.post<I%s>(this.resourceUrl, %s, { observe: 'response' });
              }

              update(%s: I%s): Observable<EntityResponseType> {
                return this.http.put<I%s>(`${this.resourceUrl}/${%s.id}`, %s, { observe: 'response' });
              }

              find(id: number): Observable<EntityResponseType> {
                return this.http.get<I%s>(`${this.resourceUrl}/${id}`, { observe: 'response' });
              }

              query(): Observable<EntityArrayResponseType> {
                return this.http.get<I%s[]>(this.resourceUrl, { observe: 'response' });
              }

              delete(id: number): Observable<HttpResponse<{}>> {
                return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
              }
            }
            """.formatted(
                entity.hasDateField() ? ", map" : "",
                entityClass, entityClass, entity.getEntityFileName(),
                entityClass, entityClass,
                entityClass, entityUrl,
                uncapitalize(entityClass), entityClass, entityClass, uncapitalize(entityClass),
                uncapitalize(entityClass), entityClass, entityClass, uncapitalize(entityClass), uncapitalize(entityClass),
                entityClass,
                entityClass
        );

        writeFile(basePath + "service/" + entity.getEntityFileName() + ".service.ts", content);
    }

    private void writeEntityRoutes(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityFile = entity.getEntityFileName();

        String content = """
            import { Routes } from '@angular/router';
            import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
            import { %sComponent } from './%s.component';
            import { %sDetailComponent } from './detail/%s-detail.component';
            %s

            export const %sRoute: Routes = [
              {
                path: '',
                component: %sComponent,
                canActivate: [UserRouteAccessService],
              },
              {
                path: ':id/view',
                component: %sDetailComponent,
                canActivate: [UserRouteAccessService],
              },
              %s
            ];
            """.formatted(
                entityClass, entityFile,
                entityClass, entityFile,
                entity.getReadOnly() ? "" :
                    "import { " + entityClass + "UpdateComponent } from './update/" + entityFile + "-update.component';",
                uncapitalize(entityClass),
                entityClass,
                entityClass,
                entity.getReadOnly() ? "" : """
                    {
                      path: 'new',
                      component: %sUpdateComponent,
                      canActivate: [UserRouteAccessService],
                    },
                    {
                      path: ':id/edit',
                      component: %sUpdateComponent,
                      canActivate: [UserRouteAccessService],
                    },
                    """.formatted(entityClass, entityClass)
        );

        writeFile(basePath + entity.getEntityFileName() + ".routes.ts", content);
    }

    private void writeEntityListComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        String componentContent = """
            import { Component, OnInit, inject } from '@angular/core';
            import { RouterModule } from '@angular/router';
            import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
            %s
            import { I%s } from './%s.model';
            import { %sService } from './service/%s.service';

            @Component({
              selector: 'jhi-%s',
              standalone: true,
              imports: [RouterModule, NgbModule%s],
              templateUrl: './%s.component.html',
            })
            export class %sComponent implements OnInit {
              %ss: I%s[] = [];
              isLoading = false;

              private %sService = inject(%sService);

              ngOnInit(): void {
                this.load();
              }

              load(): void {
                this.isLoading = true;
                this.%sService.query().subscribe({
                  next: res => {
                    this.%ss = res.body ?? [];
                    this.isLoading = false;
                  },
                  error: () => {
                    this.isLoading = false;
                  },
                });
              }
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                entityClass, entityFile, entityClass, entityFile,
                entityFile,
                getConfig().getEnableTranslation() ? ", TranslateModule" : "",
                entityFile,
                entityClass,
                entityInstance, entityClass,
                entityInstance, entityClass,
                entityInstance,
                entityInstance
        );
        writeFile(basePath + entityFile + ".component.ts", componentContent);

        // Template
        StringBuilder template = new StringBuilder();
        template.append("<div>\n");
        template.append("  <h2>").append(entityClass).append("s</h2>\n\n");
        template.append("  @if (isLoading) {\n    <div class=\"spinner-border\" role=\"status\"></div>\n  }\n\n");
        template.append("  <table class=\"table table-striped\">\n");
        template.append("    <thead>\n      <tr>\n        <th>ID</th>\n");

        for (FieldConfig field : entity.getFields()) {
            template.append("        <th>").append(capitalize(field.getFieldName())).append("</th>\n");
        }
        template.append("        <th></th>\n      </tr>\n    </thead>\n");

        template.append("    <tbody>\n");
        template.append("      @for (").append(entityInstance).append(" of ").append(entityInstance).append("s; track ").append(entityInstance).append(".id) {\n");
        template.append("        <tr>\n");
        template.append("          <td><a [routerLink]=\"['/").append(entityFile).append("', ").append(entityInstance).append(".id, 'view']\">{{ ").append(entityInstance).append(".id }}</a></td>\n");

        for (FieldConfig field : entity.getFields()) {
            template.append("          <td>{{ ").append(entityInstance).append(".").append(field.getFieldName()).append(" }}</td>\n");
        }

        template.append("          <td class=\"text-end\">\n");
        template.append("            <div class=\"btn-group\">\n");
        template.append("              <a [routerLink]=\"['/").append(entityFile).append("', ").append(entityInstance).append(".id, 'view']\" class=\"btn btn-info btn-sm\">View</a>\n");
        if (!entity.getReadOnly()) {
            template.append("              <a [routerLink]=\"['/").append(entityFile).append("', ").append(entityInstance).append(".id, 'edit']\" class=\"btn btn-primary btn-sm\">Edit</a>\n");
            template.append("              <button type=\"button\" class=\"btn btn-danger btn-sm\" (click)=\"delete(").append(entityInstance).append(")\">Delete</button>\n");
        }
        template.append("            </div>\n          </td>\n        </tr>\n      }\n    </tbody>\n  </table>\n</div>\n");

        writeFile(basePath + entityFile + ".component.html", template.toString());
    }

    private void writeEntityDetailComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        String componentContent = """
            import { Component, OnInit, inject, input } from '@angular/core';
            import { RouterModule } from '@angular/router';
            %s
            import { I%s } from '../%s.model';

            @Component({
              selector: 'jhi-%s-detail',
              standalone: true,
              imports: [RouterModule%s],
              templateUrl: './%s-detail.component.html',
            })
            export class %sDetailComponent {
              %s = input<I%s | null>(null);
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                entityClass, entityFile,
                entityFile,
                getConfig().getEnableTranslation() ? ", TranslateModule" : "",
                entityFile,
                entityClass,
                entityInstance, entityClass
        );
        writeFile(basePath + "detail/" + entityFile + "-detail.component.ts", componentContent);

        // Template
        StringBuilder template = new StringBuilder();
        template.append("<div class=\"d-flex justify-content-center\">\n");
        template.append("  @if (").append(entityInstance).append("()) {\n");
        template.append("    <div class=\"col-8\">\n");
        template.append("      <h2>").append(entityClass).append(" {{ ").append(entityInstance).append("()?.id }}</h2>\n\n");
        template.append("      <dl class=\"row\">\n");

        for (FieldConfig field : entity.getFields()) {
            template.append("        <dt class=\"col-4\">").append(capitalize(field.getFieldName())).append("</dt>\n");
            template.append("        <dd class=\"col-8\">{{ ").append(entityInstance).append("()?.").append(field.getFieldName()).append(" }}</dd>\n");
        }

        template.append("      </dl>\n\n");
        template.append("      <button type=\"button\" class=\"btn btn-secondary\" routerLink=\"/").append(entityFile).append("\">Back</button>\n");
        if (!entity.getReadOnly()) {
            template.append("      <button type=\"button\" class=\"btn btn-primary\" [routerLink]=\"['/").append(entityFile).append("', ").append(entityInstance).append("()?.id, 'edit']\">Edit</button>\n");
        }
        template.append("    </div>\n  }\n</div>\n");

        writeFile(basePath + "detail/" + entityFile + "-detail.component.html", template.toString());
    }

    private void writeEntityUpdateComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        String componentContent = """
            import { Component, OnInit, inject } from '@angular/core';
            import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
            import { ActivatedRoute, Router, RouterModule } from '@angular/router';
            %s
            import { I%s, New%s } from '../%s.model';
            import { %sService } from '../service/%s.service';

            @Component({
              selector: 'jhi-%s-update',
              standalone: true,
              imports: [FormsModule, ReactiveFormsModule, RouterModule%s],
              templateUrl: './%s-update.component.html',
            })
            export class %sUpdateComponent implements OnInit {
              isSaving = false;
              %s: I%s | null = null;

              private fb = inject(FormBuilder);
              private %sService = inject(%sService);
              private route = inject(ActivatedRoute);
              private router = inject(Router);

              editForm = this.fb.group({
                id: [null as number | null],
                %s
              });

              ngOnInit(): void {
                this.route.data.subscribe(({ %s }) => {
                  this.%s = %s;
                  if (%s) {
                    this.editForm.patchValue(%s);
                  }
                });
              }

              save(): void {
                this.isSaving = true;
                const %s = this.createFrom();
                if (%s.id !== null) {
                  this.%sService.update(%s).subscribe({
                    next: () => this.onSaveSuccess(),
                    error: () => this.onSaveError(),
                  });
                } else {
                  this.%sService.create(%s as New%s).subscribe({
                    next: () => this.onSaveSuccess(),
                    error: () => this.onSaveError(),
                  });
                }
              }

              private createFrom(): I%s {
                return {
                  id: this.editForm.get('id')?.value,
                  %s
                };
              }

              private onSaveSuccess(): void {
                this.isSaving = false;
                this.router.navigate(['/%s']);
              }

              private onSaveError(): void {
                this.isSaving = false;
              }
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                entityClass, entityClass, entityFile, entityClass, entityFile,
                entityFile,
                getConfig().getEnableTranslation() ? ", TranslateModule" : "",
                entityFile,
                entityClass,
                entityInstance, entityClass,
                entityInstance, entityClass,
                buildFormFields(entity),
                entityInstance,
                entityInstance, entityInstance,
                entityInstance, entityInstance,
                entityInstance, entityInstance, entityInstance, entityInstance,
                entityInstance, entityInstance, entityClass,
                entityClass,
                buildCreateFromFields(entity),
                entityFile
        );
        writeFile(basePath + "update/" + entityFile + "-update.component.ts", componentContent);

        // Template
        StringBuilder template = new StringBuilder();
        template.append("<div class=\"d-flex justify-content-center\">\n");
        template.append("  <div class=\"col-8\">\n");
        template.append("    <h2>Create or edit a ").append(entityClass).append("</h2>\n\n");
        template.append("    <form [formGroup]=\"editForm\" (ngSubmit)=\"save()\">\n");

        for (FieldConfig field : entity.getFields()) {
            template.append("      <div class=\"mb-3\">\n");
            template.append("        <label class=\"form-label\" for=\"field_").append(field.getFieldName()).append("\">").append(capitalize(field.getFieldName())).append("</label>\n");

            String inputType = getInputType(field);
            template.append("        <input type=\"").append(inputType).append("\" class=\"form-control\" id=\"field_").append(field.getFieldName()).append("\" formControlName=\"").append(field.getFieldName()).append("\" />\n");
            template.append("      </div>\n");
        }

        template.append("\n      <div>\n");
        template.append("        <button type=\"button\" class=\"btn btn-secondary\" routerLink=\"/").append(entityFile).append("\">Cancel</button>\n");
        template.append("        <button type=\"submit\" class=\"btn btn-primary\" [disabled]=\"isSaving || editForm.invalid\">Save</button>\n");
        template.append("      </div>\n");
        template.append("    </form>\n  </div>\n</div>\n");

        writeFile(basePath + "update/" + entityFile + "-update.component.html", template.toString());
    }

    private void writeEntityDeleteComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityAngularName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        String componentContent = """
            import { Component, inject } from '@angular/core';
            import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
            %s
            import { I%s } from '../%s.model';
            import { %sService } from '../service/%s.service';

            @Component({
              selector: 'jhi-%s-delete-dialog',
              standalone: true,
              imports: [%s],
              templateUrl: './%s-delete-dialog.component.html',
            })
            export class %sDeleteDialogComponent {
              %s?: I%s;

              private activeModal = inject(NgbActiveModal);
              private %sService = inject(%sService);

              cancel(): void {
                this.activeModal.dismiss();
              }

              confirmDelete(id: number): void {
                this.%sService.delete(id).subscribe(() => {
                  this.activeModal.close('deleted');
                });
              }
            }
            """.formatted(
                getConfig().getEnableTranslation() ? "import { TranslateModule } from '@ngx-translate/core';" : "",
                entityClass, entityFile, entityClass, entityFile,
                entityFile,
                getConfig().getEnableTranslation() ? "TranslateModule" : "",
                entityFile,
                entityClass,
                entityInstance, entityClass,
                entityInstance, entityClass,
                entityInstance
        );
        writeFile(basePath + "delete/" + entityFile + "-delete-dialog.component.ts", componentContent);

        String template = """
            <form name="deleteForm" (ngSubmit)="confirmDelete(%s!.id!)">
              <div class="modal-header">
                <h4 class="modal-title">Confirm delete operation</h4>
                <button type="button" class="btn-close" aria-label="Close" (click)="cancel()"></button>
              </div>
              <div class="modal-body">
                <p>Are you sure you want to delete %s {{ %s?.id }}?</p>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" (click)="cancel()">Cancel</button>
                <button type="submit" class="btn btn-danger">Delete</button>
              </div>
            </form>
            """.formatted(entityInstance, entityClass, entityInstance);
        writeFile(basePath + "delete/" + entityFile + "-delete-dialog.component.html", template);
    }

    private String buildFormFields(EntityConfig entity) {
        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < entity.getFields().size(); i++) {
            FieldConfig field = entity.getFields().get(i);
            fields.append(field.getFieldName()).append(": [null");
            if (field.getRequired()) {
                fields.append(", Validators.required");
            }
            fields.append("]");
            if (i < entity.getFields().size() - 1) {
                fields.append(",\n    ");
            }
        }
        return fields.toString();
    }

    private String buildCreateFromFields(EntityConfig entity) {
        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < entity.getFields().size(); i++) {
            FieldConfig field = entity.getFields().get(i);
            fields.append(field.getFieldName()).append(": this.editForm.get('").append(field.getFieldName()).append("')?.value");
            if (i < entity.getFields().size() - 1) {
                fields.append(",\n      ");
            }
        }
        return fields.toString();
    }

    private String getInputType(FieldConfig field) {
        return switch (field.getFieldType()) {
            case "Integer", "Long", "Float", "Double", "BigDecimal" -> "number";
            case "Boolean" -> "checkbox";
            case "LocalDate" -> "date";
            case "Instant", "ZonedDateTime" -> "datetime-local";
            default -> "text";
        };
    }

    private void postWriting() {
        log.info("Post-writing Angular configuration");
        // Entity routes file
        try {
            writeEntityRoutesIndex();
        } catch (IOException e) {
            log.error("Error writing entity routes index", e);
        }
    }

    private void writeEntityRoutesIndex() throws IOException {
        StringBuilder routes = new StringBuilder();
        routes.append("import { Routes } from '@angular/router';\n\n");

        for (EntityConfig entity : getConfig().getEntities()) {
            if (!entity.getBuiltIn() && !entity.getEmbedded()) {
                routes.append("import { ").append(uncapitalize(entity.getEntityAngularName())).append("Route } from './")
                      .append(entity.getEntityFolderName()).append("/").append(entity.getEntityFileName()).append(".routes';\n");
            }
        }

        routes.append("\nexport const entityRoutes: Routes = [\n");

        for (EntityConfig entity : getConfig().getEntities()) {
            if (!entity.getBuiltIn() && !entity.getEmbedded()) {
                routes.append("  {\n");
                routes.append("    path: '").append(entity.getEntityFileName()).append("',\n");
                routes.append("    children: ").append(uncapitalize(entity.getEntityAngularName())).append("Route,\n");
                routes.append("  },\n");
            }
        }

        routes.append("  // jhipster-needle-add-entity-route - JHipster will add entity routes here\n");
        routes.append("];\n");

        writeFile(getConfig().getClientWebappDir() + "entities/entity.routes.ts", routes.toString());
    }

    private void postWritingEntities() {
        log.info("Post-writing entities - adding to menu and routes");

        for (EntityConfig entity : getConfig().getEntities()) {
            if (!entity.getBuiltIn() && !entity.getEmbedded()) {
                try {
                    // Add entity to navbar menu
                    String menuItem = String.format(
                        "<li class=\"nav-item\"><a class=\"dropdown-item\" routerLink=\"/%s\">%s</a></li>",
                        entity.getEntityFileName(),
                        entity.getEntityAngularName()
                    );
                    needleService.addAngularEntityToMenu(menuItem);
                } catch (IOException e) {
                    log.warn("Could not add entity {} to menu: {}", entity.getName(), e.getMessage());
                }
            }
        }
    }

    private void end() {
        log.info("Angular generation completed!");
        log.info("");
        log.info("==========================================================");
        log.info("Angular application generated successfully!");
        log.info("==========================================================");
    }
}
