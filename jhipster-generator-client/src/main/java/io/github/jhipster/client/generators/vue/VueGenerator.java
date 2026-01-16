/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.generators.vue;

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
 * Vue Generator.
 * Generates Vue 3 frontend application with Pinia state management.
 * Equivalent to generators/vue/generator.ts.
 */
public class VueGenerator extends BaseClientGenerator {

    private NeedleService needleService;

    public VueGenerator(ClientGeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "vue";
    }

    @Override
    protected void registerTasks() {
        registerTask(GeneratorPhase.INITIALIZING, this::initializing);
        registerTask(GeneratorPhase.LOADING, this::loading);
        registerTask(GeneratorPhase.PREPARING, this::preparing);
        registerTask(GeneratorPhase.WRITING, this::writing);
        registerTask(GeneratorPhase.WRITING_ENTITIES, this::writingEntities);
        registerTask(GeneratorPhase.POST_WRITING, this::postWriting);
        registerTask(GeneratorPhase.END, this::end);
    }

    private void initializing() {
        log.info("Initializing Vue generator");
        this.needleService = new NeedleService(context.getDestinationRoot());
    }

    private void loading() {
        log.info("Loading Vue dependencies");

        // Add Vue-specific template data
        context.addTemplateData("vueVersion", "3.3.8");
        context.addTemplateData("piniaVersion", "2.1.7");
        context.addTemplateData("typescriptVersion", "5.2.2");
    }

    private void preparing() {
        log.info("Preparing Vue generation context");
    }

    private void writing() {
        log.info("Writing Vue application files");

        try {
            // Core application files
            writeAppVue();
            writeMainTs();
            writeRouterIndex();

            // Config files
            writePackageJson();
            writeTsConfig();

            // Pinia store
            writePiniaStore();

            // Core services
            writeAxiosConfig();
            writeAccountStore();

            // Home module
            writeHomeComponent();

            // Login module
            writeLoginComponent();

            // Layouts
            writeNavbar();
            writeFooter();

            // Build configuration
            if (getConfig().isVite()) {
                writeViteConfig();
            } else {
                writeWebpackConfig();
            }

            // Styles
            writeStyles();

        } catch (IOException e) {
            throw new RuntimeException("Error writing Vue files", e);
        }
    }

    private void writeAppVue() throws IOException {
        String content = """
            <template>
              <div id="app">
                <jhi-navbar />
                <div class="container-fluid">
                  <div class="card jh-card">
                    <router-view />
                  </div>
                  <jhi-footer />
                </div>
              </div>
            </template>

            <script lang="ts">
            import { defineComponent, onMounted } from 'vue';
            import { useAccountStore } from '@/shared/config/store/account-store';
            import JhiNavbar from '@/core/navbar/navbar.vue';
            import JhiFooter from '@/core/footer/footer.vue';

            export default defineComponent({
              name: 'App',
              components: {
                JhiNavbar,
                JhiFooter,
              },
              setup() {
                const accountStore = useAccountStore();

                onMounted(async () => {
                  await accountStore.fetchAccount();
                });

                return {};
              },
            });
            </script>

            <style lang="scss">
            @import '@/content/scss/global.scss';
            </style>
            """;
        writeFile(getConfig().getClientWebappDir() + "app.vue", content);
    }

    private void writeMainTs() throws IOException {
        String content = """
            import { createApp } from 'vue';
            import { createPinia } from 'pinia';
            %s
            import App from './app.vue';
            import router from './router';
            import { useAccountStore } from '@/shared/config/store/account-store';

            // Bootstrap
            import 'bootstrap/dist/css/bootstrap.css';
            import 'bootstrap';

            // FontAwesome
            import { library } from '@fortawesome/fontawesome-svg-core';
            import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
            import { fas } from '@fortawesome/free-solid-svg-icons';
            library.add(fas);

            const app = createApp(App);
            const pinia = createPinia();

            app.use(pinia);
            app.use(router);
            %s
            app.component('font-awesome-icon', FontAwesomeIcon);

            app.mount('#app');
            """.formatted(
                getConfig().getEnableTranslation() ?
                    "import { createI18n } from 'vue-i18n';\n" +
                    "import en from '@/i18n/en.json';\n" +
                    "import fr from '@/i18n/fr.json';" : "",
                getConfig().getEnableTranslation() ?
                    "const i18n = createI18n({\n" +
                    "  legacy: false,\n" +
                    "  locale: 'en',\n" +
                    "  fallbackLocale: 'en',\n" +
                    "  messages: { en, fr },\n" +
                    "});\napp.use(i18n);" : ""
        );
        writeFile(getConfig().getClientSrcDir() + "main.ts", content);
    }

    private void writeRouterIndex() throws IOException {
        String content = """
            import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
            import { useAccountStore } from '@/shared/config/store/account-store';

            import Home from '@/core/home/home.vue';
            %s

            const routes: RouteRecordRaw[] = [
              {
                path: '/',
                name: 'Home',
                component: Home,
              },
              {
                path: '/login',
                name: 'Login',
                component: () => import('@/account/login.vue'),
              },
              {
                path: '/logout',
                name: 'Logout',
                component: () => import('@/account/logout.vue'),
              },
              %s
              {
                path: '/admin',
                name: 'Admin',
                component: () => import('@/admin/admin.vue'),
                meta: { authorities: ['ROLE_ADMIN'] },
                children: [
                  {
                    path: 'user-management',
                    name: 'JhiUserManagement',
                    component: () => import('@/admin/user-management/user-management.vue'),
                  },
                  {
                    path: 'logs',
                    name: 'JhiLogs',
                    component: () => import('@/admin/logs/logs.vue'),
                  },
                  {
                    path: 'health',
                    name: 'JhiHealth',
                    component: () => import('@/admin/health/health.vue'),
                  },
                  {
                    path: 'configuration',
                    name: 'JhiConfiguration',
                    component: () => import('@/admin/configuration/configuration.vue'),
                  },
                  {
                    path: 'metrics',
                    name: 'JhiMetrics',
                    component: () => import('@/admin/metrics/metrics.vue'),
                  },
                  {
                    path: 'docs',
                    name: 'JhiDocs',
                    component: () => import('@/admin/docs/docs.vue'),
                  },
                ],
              },
              // jhipster-needle-add-entity-to-router - JHipster will add entity routes here
            ];

            const router = createRouter({
              history: createWebHistory(),
              routes,
            });

            router.beforeEach(async (to, from, next) => {
              const accountStore = useAccountStore();
              const authorities = to.meta.authorities as string[] | undefined;

              if (authorities && authorities.length > 0) {
                if (!accountStore.authenticated) {
                  next('/login');
                  return;
                }
                const hasAuthority = authorities.some(auth =>
                  accountStore.account?.authorities?.includes(auth)
                );
                if (!hasAuthority) {
                  next('/accessdenied');
                  return;
                }
              }
              next();
            });

            export default router;
            """.formatted(
                getConfig().isOAuth2() ? "" : "import Account from '@/account/account.vue';",
                getConfig().isOAuth2() ? "" :
                    "{\n" +
                    "  path: '/account',\n" +
                    "  name: 'Account',\n" +
                    "  component: Account,\n" +
                    "  children: [\n" +
                    "    {\n" +
                    "      path: 'settings',\n" +
                    "      name: 'Settings',\n" +
                    "      component: () => import('@/account/settings/settings.vue'),\n" +
                    "    },\n" +
                    "    {\n" +
                    "      path: 'password',\n" +
                    "      name: 'Password',\n" +
                    "      component: () => import('@/account/password/password.vue'),\n" +
                    "    },\n" +
                    "  ],\n" +
                    "},"
        );
        writeFile(getConfig().getClientWebappDir() + "router/index.ts", content);
    }

    private void writePackageJson() throws IOException {
        boolean isVite = getConfig().isVite();

        String content = """
            {
              "name": "%s",
              "version": "0.0.1-SNAPSHOT",
              "private": true,
              "description": "JHipster application generated with Vue",
              "license": "UNLICENSED",
              "scripts": {
                %s
                "test": "%s",
                "test:watch": "%s",
                "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx --fix --ignore-path .gitignore",
                "e2e": "cypress run",
                "e2e:open": "cypress open"
              },
              "dependencies": {
                "@fortawesome/fontawesome-svg-core": "^6.4.2",
                "@fortawesome/free-solid-svg-icons": "^6.4.2",
                "@fortawesome/vue-fontawesome": "^3.0.5",
                "axios": "^1.6.2",
                "bootstrap": "^5.3.2",
                "dayjs": "^1.11.10",
                "pinia": "^2.1.7",
                %s
                "vue": "^3.3.8",
                "vue-router": "^4.2.5"
              },
              "devDependencies": {
                "@types/node": "^20.10.0",
                "@vitejs/plugin-vue": "^4.5.0",
                "@vue/test-utils": "^2.4.3",
                "eslint": "^8.55.0",
                "eslint-plugin-vue": "^9.19.2",
                "sass": "^1.69.5",
                "typescript": "^5.2.2",
                %s
                "vue-tsc": "^1.8.25",
                %s
              }
            }
            """.formatted(
                getConfig().getBaseName().toLowerCase(),
                isVite ?
                    "\"dev\": \"vite\",\n    \"build\": \"vue-tsc && vite build\",\n    \"preview\": \"vite preview\"," :
                    "\"dev\": \"webpack serve --config webpack/webpack.dev.js\",\n    \"build\": \"webpack --config webpack/webpack.prod.js\",",
                isVite ? "vitest" : "jest",
                isVite ? "vitest --watch" : "jest --watch",
                getConfig().getEnableTranslation() ? "\"vue-i18n\": \"^9.8.0\"," : "",
                isVite ?
                    "\"vite\": \"^5.0.0\",\n    \"vitest\": \"^1.0.0\"," :
                    "\"webpack\": \"^5.89.0\",\n    \"webpack-cli\": \"^5.1.4\",\n    \"webpack-dev-server\": \"^4.15.1\",\n    \"jest\": \"^29.7.0\",",
                getConfig().hasCypress() ? "\"cypress\": \"^13.6.0\"" : ""
        );
        writeFile("package.json", content);
    }

    private void writeTsConfig() throws IOException {
        String content = """
            {
              "compilerOptions": {
                "target": "ES2020",
                "useDefineForClassFields": true,
                "module": "ESNext",
                "lib": ["ES2020", "DOM", "DOM.Iterable"],
                "skipLibCheck": true,
                "moduleResolution": "bundler",
                "allowImportingTsExtensions": true,
                "resolveJsonModule": true,
                "isolatedModules": true,
                "noEmit": true,
                "jsx": "preserve",
                "strict": true,
                "noUnusedLocals": true,
                "noUnusedParameters": true,
                "noFallthroughCasesInSwitch": true,
                "paths": {
                  "@/*": ["./src/main/webapp/app/*"]
                }
              },
              "include": ["src/main/webapp/**/*.ts", "src/main/webapp/**/*.tsx", "src/main/webapp/**/*.vue"],
              "references": [{ "path": "./tsconfig.node.json" }]
            }
            """;
        writeFile("tsconfig.json", content);

        String nodeConfig = """
            {
              "compilerOptions": {
                "composite": true,
                "skipLibCheck": true,
                "module": "ESNext",
                "moduleResolution": "bundler",
                "allowSyntheticDefaultImports": true
              },
              "include": ["vite.config.ts"]
            }
            """;
        writeFile("tsconfig.node.json", nodeConfig);
    }

    private void writePiniaStore() throws IOException {
        // Store is initialized in main.ts with createPinia()
        log.info("Pinia store configured in main.ts");
    }

    private void writeAxiosConfig() throws IOException {
        String content = """
            import axios, { AxiosInstance } from 'axios';

            const TIMEOUT = 60000;

            const axiosInstance: AxiosInstance = axios.create({
              timeout: TIMEOUT,
              baseURL: '/',
            });

            // Request interceptor
            axiosInstance.interceptors.request.use(
              config => {
                const token = localStorage.getItem('authToken');
                if (token) {
                  config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
              },
              error => Promise.reject(error)
            );

            // Response interceptor
            axiosInstance.interceptors.response.use(
              response => response,
              error => {
                if (error.response?.status === 401) {
                  localStorage.removeItem('authToken');
                }
                return Promise.reject(error);
              }
            );

            export default axiosInstance;
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/config/axios-interceptor.ts", content);
    }

    private void writeAccountStore() throws IOException {
        String content = """
            import { defineStore } from 'pinia';
            import axios from '@/shared/config/axios-interceptor';

            interface Account {
              login: string;
              firstName: string;
              lastName: string;
              email: string;
              langKey: string;
              authorities: string[];
            }

            interface AccountState {
              account: Account | null;
              authenticated: boolean;
              loading: boolean;
              error: string | null;
            }

            export const useAccountStore = defineStore('account', {
              state: (): AccountState => ({
                account: null,
                authenticated: false,
                loading: false,
                error: null,
              }),

              getters: {
                isAuthenticated: (state) => state.authenticated,
                hasAnyAuthority: (state) => (authorities: string[]) => {
                  if (!state.account?.authorities) return false;
                  return authorities.some(auth => state.account!.authorities.includes(auth));
                },
              },

              actions: {
                async fetchAccount() {
                  this.loading = true;
                  try {
                    const response = await axios.get<Account>('/api/account');
                    this.account = response.data;
                    this.authenticated = true;
                    this.error = null;
                  } catch (error) {
                    this.account = null;
                    this.authenticated = false;
                  } finally {
                    this.loading = false;
                  }
                },

                %s

                async logout() {
                  try {
                    %s
                    this.account = null;
                    this.authenticated = false;
                    localStorage.removeItem('authToken');
                  } catch (error) {
                    console.error('Logout error:', error);
                  }
                },

                clearAuth() {
                  this.account = null;
                  this.authenticated = false;
                },
              },
            });
            """.formatted(
                getConfig().isOAuth2() ? "" :
                    "async login(credentials: { username: string; password: string; rememberMe: boolean }) {\n" +
                    "          this.loading = true;\n" +
                    "          try {\n" +
                    "            const response = await axios.post('/api/authenticate', credentials);\n" +
                    "            const token = response.headers['authorization']?.replace('Bearer ', '');\n" +
                    "            if (token) {\n" +
                    "              localStorage.setItem('authToken', token);\n" +
                    "            }\n" +
                    "            await this.fetchAccount();\n" +
                    "            this.error = null;\n" +
                    "          } catch (error: any) {\n" +
                    "            this.error = error.response?.data?.message || 'Login failed';\n" +
                    "            throw error;\n" +
                    "          } finally {\n" +
                    "            this.loading = false;\n" +
                    "          }\n" +
                    "        },",
                getConfig().isOAuth2() ?
                    "window.location.href = '/api/logout';" :
                    "await axios.post('/api/logout');"
        );
        writeFile(getConfig().getClientWebappDir() + "shared/config/store/account-store.ts", content);
    }

    private void writeHomeComponent() throws IOException {
        String template = """
            <template>
              <div class="row">
                <div class="col-md-9">
                  <h2>%s</h2>
                  <p class="lead">%s</p>
                  <div v-if="authenticated" class="alert alert-success">
                    %s
                  </div>
                  <div v-else class="alert alert-warning">
                    %s
                    <router-link to="/login" class="alert-link">%s</router-link>
                  </div>
                </div>
              </div>
            </template>

            <script lang="ts">
            import { defineComponent, computed } from 'vue';
            import { useAccountStore } from '@/shared/config/store/account-store';

            export default defineComponent({
              name: 'Home',
              setup() {
                const accountStore = useAccountStore();

                const authenticated = computed(() => accountStore.authenticated);
                const account = computed(() => accountStore.account);

                return {
                  authenticated,
                  account,
                };
              },
            });
            </script>

            <style scoped lang="scss">
            .hipster {
              display: inline-block;
              width: 100%%;
              height: 497px;
              background-size: contain;
            }
            </style>
            """.formatted(
                getConfig().getEnableTranslation() ? "{{ $t('home.title') }}" : "Welcome, " + getConfig().getBaseName() + "!",
                getConfig().getEnableTranslation() ? "{{ $t('home.subtitle') }}" : "This is your homepage",
                getConfig().getEnableTranslation() ?
                    "{{ $t('home.logged.message', { username: account?.login }) }}" :
                    "You are logged in as {{ account?.login }}",
                getConfig().getEnableTranslation() ? "{{ $t('home.notLogged.message') }}" : "You are not signed in yet. ",
                getConfig().getEnableTranslation() ? "{{ $t('global.menu.account.login') }}" : "Sign in"
        );
        writeFile(getConfig().getClientWebappDir() + "core/home/home.vue", template);
    }

    private void writeLoginComponent() throws IOException {
        String template;
        if (getConfig().isOAuth2()) {
            template = """
                <template>
                  <div class="d-flex justify-content-center">
                    <div class="spinner-border" role="status">
                      <span class="visually-hidden">Redirecting...</span>
                    </div>
                  </div>
                </template>

                <script lang="ts">
                import { defineComponent, onMounted } from 'vue';

                export default defineComponent({
                  name: 'Login',
                  setup() {
                    onMounted(() => {
                      window.location.href = '/oauth2/authorization/oidc';
                    });
                    return {};
                  },
                });
                </script>
                """;
        } else {
            template = """
                <template>
                  <div class="row justify-content-center">
                    <div class="col-md-8">
                      <h1>%s</h1>
                      <div v-if="authenticationError" class="alert alert-danger">
                        %s
                      </div>
                      <form @submit.prevent="doLogin">
                        <div class="mb-3">
                          <label for="username" class="form-label">%s</label>
                          <input
                            type="text"
                            id="username"
                            class="form-control"
                            v-model="credentials.username"
                            required
                          />
                        </div>
                        <div class="mb-3">
                          <label for="password" class="form-label">%s</label>
                          <input
                            type="password"
                            id="password"
                            class="form-control"
                            v-model="credentials.password"
                            required
                          />
                        </div>
                        <div class="mb-3 form-check">
                          <input
                            type="checkbox"
                            id="rememberMe"
                            class="form-check-input"
                            v-model="credentials.rememberMe"
                          />
                          <label for="rememberMe" class="form-check-label">%s</label>
                        </div>
                        <button type="submit" class="btn btn-primary" :disabled="loading">
                          %s
                        </button>
                      </form>
                    </div>
                  </div>
                </template>

                <script lang="ts">
                import { defineComponent, ref, reactive, computed } from 'vue';
                import { useRouter } from 'vue-router';
                import { useAccountStore } from '@/shared/config/store/account-store';

                export default defineComponent({
                  name: 'Login',
                  setup() {
                    const router = useRouter();
                    const accountStore = useAccountStore();

                    const credentials = reactive({
                      username: '',
                      password: '',
                      rememberMe: false,
                    });

                    const authenticationError = ref(false);
                    const loading = computed(() => accountStore.loading);

                    const doLogin = async () => {
                      try {
                        authenticationError.value = false;
                        await accountStore.login(credentials);
                        router.push('/');
                      } catch (error) {
                        authenticationError.value = true;
                      }
                    };

                    return {
                      credentials,
                      authenticationError,
                      loading,
                      doLogin,
                    };
                  },
                });
                </script>
                """.formatted(
                    getConfig().getEnableTranslation() ? "{{ $t('login.title') }}" : "Sign in",
                    getConfig().getEnableTranslation() ?
                        "{{ $t('login.messages.error.authentication') }}" : "Failed to sign in!",
                    getConfig().getEnableTranslation() ? "{{ $t('global.form.username.label') }}" : "Username",
                    getConfig().getEnableTranslation() ? "{{ $t('login.form.password') }}" : "Password",
                    getConfig().getEnableTranslation() ? "{{ $t('login.form.rememberme') }}" : "Remember me",
                    getConfig().getEnableTranslation() ? "{{ $t('login.form.button') }}" : "Sign in"
            );
        }
        writeFile(getConfig().getClientWebappDir() + "account/login.vue", template);

        // Logout component
        String logoutTemplate = """
            <template>
              <div class="p-5">
                <h4>Logged out successfully!</h4>
              </div>
            </template>

            <script lang="ts">
            import { defineComponent, onMounted } from 'vue';
            import { useAccountStore } from '@/shared/config/store/account-store';

            export default defineComponent({
              name: 'Logout',
              setup() {
                const accountStore = useAccountStore();

                onMounted(async () => {
                  await accountStore.logout();
                });

                return {};
              },
            });
            </script>
            """;
        writeFile(getConfig().getClientWebappDir() + "account/logout.vue", logoutTemplate);
    }

    private void writeNavbar() throws IOException {
        String template = """
            <template>
              <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
                <div class="container-fluid">
                  <router-link class="navbar-brand" to="/">
                    <span class="navbar-title">%s</span>
                  </router-link>
                  <button
                    class="navbar-toggler"
                    type="button"
                    @click="toggleNavbar"
                  >
                    <span class="navbar-toggler-icon"></span>
                  </button>
                  <div class="collapse navbar-collapse" :class="{ show: isNavbarOpen }">
                    <ul class="navbar-nav me-auto">
                      <li class="nav-item">
                        <router-link class="nav-link" to="/">
                          <font-awesome-icon icon="home" />
                          <span>%s</span>
                        </router-link>
                      </li>
                      <li v-if="authenticated" class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" @click.prevent="toggleEntitiesMenu">
                          <font-awesome-icon icon="th-list" />
                          <span>%s</span>
                        </a>
                        <ul class="dropdown-menu" :class="{ show: isEntitiesMenuOpen }">
                          <!-- jhipster-needle-add-entity-to-menu - JHipster will add entities here -->
                        </ul>
                      </li>
                      <li v-if="hasAdminAuthority" class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" @click.prevent="toggleAdminMenu">
                          <font-awesome-icon icon="user-shield" />
                          <span>%s</span>
                        </a>
                        <ul class="dropdown-menu" :class="{ show: isAdminMenuOpen }">
                          <li><router-link class="dropdown-item" to="/admin/user-management">User management</router-link></li>
                          <li><router-link class="dropdown-item" to="/admin/metrics">Metrics</router-link></li>
                          <li><router-link class="dropdown-item" to="/admin/health">Health</router-link></li>
                          <li><router-link class="dropdown-item" to="/admin/configuration">Configuration</router-link></li>
                          <li><router-link class="dropdown-item" to="/admin/logs">Logs</router-link></li>
                          <li><router-link class="dropdown-item" to="/admin/docs">API</router-link></li>
                        </ul>
                      </li>
                    </ul>
                    <ul class="navbar-nav">
                      <li v-if="!authenticated" class="nav-item">
                        <router-link class="nav-link" to="/login">
                          <font-awesome-icon icon="sign-in-alt" />
                          <span>%s</span>
                        </router-link>
                      </li>
                      <li v-if="authenticated" class="nav-item">
                        <router-link class="nav-link" to="/logout">
                          <font-awesome-icon icon="sign-out-alt" />
                          <span>%s</span>
                        </router-link>
                      </li>
                    </ul>
                  </div>
                </div>
              </nav>
            </template>

            <script lang="ts">
            import { defineComponent, ref, computed } from 'vue';
            import { useAccountStore } from '@/shared/config/store/account-store';

            export default defineComponent({
              name: 'JhiNavbar',
              setup() {
                const accountStore = useAccountStore();

                const isNavbarOpen = ref(false);
                const isEntitiesMenuOpen = ref(false);
                const isAdminMenuOpen = ref(false);

                const authenticated = computed(() => accountStore.authenticated);
                const hasAdminAuthority = computed(() =>
                  accountStore.hasAnyAuthority(['ROLE_ADMIN'])
                );

                const toggleNavbar = () => {
                  isNavbarOpen.value = !isNavbarOpen.value;
                };

                const toggleEntitiesMenu = () => {
                  isEntitiesMenuOpen.value = !isEntitiesMenuOpen.value;
                  isAdminMenuOpen.value = false;
                };

                const toggleAdminMenu = () => {
                  isAdminMenuOpen.value = !isAdminMenuOpen.value;
                  isEntitiesMenuOpen.value = false;
                };

                return {
                  isNavbarOpen,
                  isEntitiesMenuOpen,
                  isAdminMenuOpen,
                  authenticated,
                  hasAdminAuthority,
                  toggleNavbar,
                  toggleEntitiesMenu,
                  toggleAdminMenu,
                };
              },
            });
            </script>

            <style scoped lang="scss">
            .navbar-brand .navbar-title {
              font-weight: bold;
            }
            </style>
            """.formatted(
                getConfig().getBaseName(),
                getConfig().getEnableTranslation() ? "{{ $t('global.menu.home') }}" : "Home",
                getConfig().getEnableTranslation() ? "{{ $t('global.menu.entities.main') }}" : "Entities",
                getConfig().getEnableTranslation() ? "{{ $t('global.menu.admin.main') }}" : "Administration",
                getConfig().getEnableTranslation() ? "{{ $t('global.menu.account.login') }}" : "Sign in",
                getConfig().getEnableTranslation() ? "{{ $t('global.menu.account.logout') }}" : "Sign out"
        );
        writeFile(getConfig().getClientWebappDir() + "core/navbar/navbar.vue", template);
    }

    private void writeFooter() throws IOException {
        String template = """
            <template>
              <footer class="footer">
                <div class="container">
                  <p class="text-muted">
                    &copy; %s - Powered by JHipster
                  </p>
                </div>
              </footer>
            </template>

            <script lang="ts">
            import { defineComponent } from 'vue';

            export default defineComponent({
              name: 'JhiFooter',
            });
            </script>

            <style scoped lang="scss">
            .footer {
              position: fixed;
              bottom: 0;
              width: 100%%;
              height: 60px;
              line-height: 60px;
              background-color: #f5f5f5;
            }
            </style>
            """.formatted(getConfig().getBaseName());
        writeFile(getConfig().getClientWebappDir() + "core/footer/footer.vue", template);
    }

    private void writeViteConfig() throws IOException {
        String content = """
            import { defineConfig } from 'vite';
            import vue from '@vitejs/plugin-vue';
            import { resolve } from 'path';

            export default defineConfig({
              plugins: [vue()],
              resolve: {
                alias: {
                  '@': resolve(__dirname, 'src/main/webapp/app'),
                },
              },
              server: {
                port: %d,
                proxy: {
                  '/api': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                  },
                  '/management': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                  },
                  '/v3/api-docs': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                  },
                },
              },
              build: {
                outDir: 'target/classes/static',
              },
            });
            """.formatted(getConfig().getDevServerPort());
        writeFile("vite.config.ts", content);
    }

    private void writeWebpackConfig() throws IOException {
        String webpackCommon = """
            const path = require('path');
            const { VueLoaderPlugin } = require('vue-loader');
            const HtmlWebpackPlugin = require('html-webpack-plugin');
            const MiniCssExtractPlugin = require('mini-css-extract-plugin');

            module.exports = {
              entry: './src/main/webapp/main.ts',
              output: {
                path: path.resolve('./target/classes/static/'),
                filename: '[name].[contenthash].js',
                publicPath: '/',
              },
              resolve: {
                extensions: ['.ts', '.js', '.vue', '.json'],
                alias: {
                  '@': path.resolve('./src/main/webapp/app/'),
                },
              },
              module: {
                rules: [
                  {
                    test: /\\.vue$/,
                    loader: 'vue-loader',
                  },
                  {
                    test: /\\.ts$/,
                    loader: 'ts-loader',
                    options: {
                      appendTsSuffixTo: [/\\.vue$/],
                    },
                    exclude: /node_modules/,
                  },
                  {
                    test: /\\.scss$/,
                    use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader'],
                  },
                  {
                    test: /\\.css$/,
                    use: [MiniCssExtractPlugin.loader, 'css-loader'],
                  },
                ],
              },
              plugins: [
                new VueLoaderPlugin(),
                new HtmlWebpackPlugin({
                  template: './src/main/webapp/index.html',
                }),
                new MiniCssExtractPlugin({
                  filename: '[name].[contenthash].css',
                }),
              ],
            };
            """;
        writeFile("webpack/webpack.common.js", webpackCommon);

        String webpackDev = """
            const { merge } = require('webpack-merge');
            const common = require('./webpack.common.js');

            module.exports = merge(common, {
              mode: 'development',
              devtool: 'inline-source-map',
              devServer: {
                port: %d,
                historyApiFallback: true,
                proxy: {
                  '/api': {
                    target: 'http://localhost:8080',
                    secure: false,
                  },
                  '/management': {
                    target: 'http://localhost:8080',
                    secure: false,
                  },
                },
              },
            });
            """.formatted(getConfig().getDevServerPort());
        writeFile("webpack/webpack.dev.js", webpackDev);
    }

    private void writeStyles() throws IOException {
        String globalScss = """
            @import 'bootstrap/scss/bootstrap';

            .jh-card {
              padding: 20px;
              margin-top: 20px;
            }

            .card {
              border: none;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            }

            // jhipster-needle-add-scss-style - JHipster will add SCSS styles here
            """;
        writeFile(getConfig().getClientWebappDir() + "content/scss/global.scss", globalScss);

        String indexHtml = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <link rel="icon" href="/favicon.ico" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>%s</title>
              </head>
              <body>
                <div id="app">
                  <div class="app-loading">
                    <div class="spinner-border" role="status"></div>
                    <p>Loading...</p>
                  </div>
                </div>
                <script type="module" src="/src/main/webapp/main.ts"></script>
              </body>
            </html>
            """.formatted(getConfig().getBaseName());
        writeFile("index.html", indexHtml);
    }

    private void writingEntities() {
        log.info("Writing Vue entity files");

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

        // Write entities router
        try {
            writeEntitiesRouter();
        } catch (IOException e) {
            log.error("Error writing entities router", e);
        }
    }

    private void writeEntityFiles(EntityConfig entity) throws IOException {
        String entityFolder = entity.getEntityFolderName();
        String entityFile = entity.getEntityFileName();
        String basePath = getConfig().getClientWebappDir() + "entities/" + entityFolder + "/";

        // Entity model
        writeEntityModel(entity);

        // Entity service
        writeEntityService(entity, basePath);

        // List component
        writeEntityListComponent(entity, basePath);

        // Detail component
        writeEntityDetailComponent(entity, basePath);

        // Update component (if not read-only)
        if (!entity.getReadOnly()) {
            writeEntityUpdateComponent(entity, basePath);
        }
    }

    private void writeEntityModel(EntityConfig entity) throws IOException {
        StringBuilder content = new StringBuilder();

        if (entity.hasDateField()) {
            content.append("import dayjs from 'dayjs';\n\n");
        }

        content.append("export interface I").append(entity.getEntityReactName()).append(" {\n");
        content.append("  id?: number;\n");

        for (FieldConfig field : entity.getFields()) {
            content.append("  ").append(field.getFieldName()).append("?: ")
                   .append(field.getTsType()).append(" | null;\n");
        }

        for (RelationshipConfig rel : entity.getRelationships()) {
            String type = "I" + rel.getOtherEntityAngularName();
            if (rel.isCollection()) {
                type += "[]";
            }
            content.append("  ").append(rel.getRelationshipName()).append("?: ").append(type).append(" | null;\n");
        }

        content.append("}\n\n");
        content.append("export class ").append(entity.getEntityReactName()).append(" implements I")
               .append(entity.getEntityReactName()).append(" {\n");
        content.append("  constructor(public id?: number) {}\n");
        content.append("}\n");

        writeFile(getConfig().getClientWebappDir() + "shared/model/" + entity.getEntityFileName() + ".model.ts", content.toString());
    }

    private void writeEntityService(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityUrl = toKebabCase(entity.getName());

        String content = """
            import axios from '@/shared/config/axios-interceptor';
            import { I%s } from '@/shared/model/%s.model';

            const apiUrl = '/api/%s';

            export default class %sService {
              public find(id: number): Promise<I%s> {
                return axios.get<I%s>(`${apiUrl}/${id}`).then(res => res.data);
              }

              public retrieve(): Promise<I%s[]> {
                return axios.get<I%s[]>(apiUrl).then(res => res.data);
              }

              public create(entity: I%s): Promise<I%s> {
                return axios.post<I%s>(apiUrl, entity).then(res => res.data);
              }

              public update(entity: I%s): Promise<I%s> {
                return axios.put<I%s>(`${apiUrl}/${entity.id}`, entity).then(res => res.data);
              }

              public delete(id: number): Promise<void> {
                return axios.delete(`${apiUrl}/${id}`);
              }
            }
            """.formatted(
                entityClass, entity.getEntityFileName(), entityUrl,
                entityClass,
                entityClass, entityClass,
                entityClass, entityClass,
                entityClass, entityClass, entityClass,
                entityClass, entityClass, entityClass
        );
        writeFile(basePath + entity.getEntityFileName() + ".service.ts", content);
    }

    private void writeEntityListComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        StringBuilder columns = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            columns.append("              <th>").append(capitalize(field.getFieldName())).append("</th>\n");
        }

        StringBuilder cells = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            cells.append("              <td>{{ ").append(entityInstance).append(".").append(field.getFieldName()).append(" }}</td>\n");
        }

        String template = """
            <template>
              <div>
                <h2>
                  %ss
                  %s
                </h2>
                <div v-if="loading" class="spinner-border" role="status"></div>
                <table v-else class="table table-striped">
                  <thead>
                    <tr>
                      <th>ID</th>
            %s          <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="%s in %ss" :key="%s.id">
                      <td>
                        <router-link :to="`/%s/${%s.id}`">{{ %s.id }}</router-link>
                      </td>
            %s          <td class="text-end">
                        <div class="btn-group">
                          <router-link :to="`/%s/${%s.id}`" class="btn btn-info btn-sm">
                            <font-awesome-icon icon="eye" />
                          </router-link>
                          %s
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </template>

            <script lang="ts">
            import { defineComponent, ref, onMounted } from 'vue';
            import { I%s } from '@/shared/model/%s.model';
            import %sService from './%s.service';

            export default defineComponent({
              name: '%s',
              setup() {
                const %sService = new %sService();
                const %ss = ref<I%s[]>([]);
                const loading = ref(true);

                const loadAll = async () => {
                  loading.value = true;
                  try {
                    %ss.value = await %sService.retrieve();
                  } finally {
                    loading.value = false;
                  }
                };

                %s

                onMounted(() => {
                  loadAll();
                });

                return {
                  %ss,
                  loading,
                  %s
                };
              },
            });
            </script>
            """.formatted(
                entityClass,
                entity.getReadOnly() ? "" :
                    "\n          <router-link to=\"/" + entityFile + "/new\" class=\"btn btn-primary float-end\">\n" +
                    "            <font-awesome-icon icon=\"plus\" /> New\n" +
                    "          </router-link>",
                columns.toString(),
                entityInstance, entityInstance, entityInstance,
                entityFile, entityInstance, entityInstance,
                cells.toString(),
                entityFile, entityInstance,
                entity.getReadOnly() ? "" :
                    "<router-link :to=\"`/" + entityFile + "/${" + entityInstance + ".id}/edit`\" class=\"btn btn-primary btn-sm\">\n" +
                    "                      <font-awesome-icon icon=\"pencil-alt\" />\n" +
                    "                    </router-link>\n" +
                    "                    <button class=\"btn btn-danger btn-sm\" @click=\"prepareRemove(" + entityInstance + ")\">\n" +
                    "                      <font-awesome-icon icon=\"trash\" />\n" +
                    "                    </button>",
                entityClass, entityFile,
                entityClass, entityFile,
                entityClass,
                entityInstance, entityClass,
                entityInstance, entityClass,
                entityInstance, entityInstance,
                entity.getReadOnly() ? "" :
                    "const prepareRemove = async (" + entityInstance + ": I" + entityClass + ") => {\n" +
                    "          if (confirm('Are you sure you want to delete this " + entityClass + "?')) {\n" +
                    "            await " + entityInstance + "Service.delete(" + entityInstance + ".id!);\n" +
                    "            await loadAll();\n" +
                    "          }\n" +
                    "        };",
                entityInstance,
                entity.getReadOnly() ? "" : "prepareRemove,"
        );
        writeFile(basePath + entityFile + ".vue", template);
    }

    private void writeEntityDetailComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        StringBuilder fields = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            fields.append("          <dt>").append(capitalize(field.getFieldName())).append("</dt>\n");
            fields.append("          <dd>{{ ").append(entityInstance).append("?.").append(field.getFieldName()).append(" }}</dd>\n");
        }

        String template = """
            <template>
              <div class="row justify-content-center">
                <div class="col-8">
                  <h2>%s {{ %s?.id }}</h2>
                  <dl class="row">
            %s      </dl>
                  <router-link to="/%s" class="btn btn-info">Back</router-link>
                  %s
                </div>
              </div>
            </template>

            <script lang="ts">
            import { defineComponent, ref, onMounted } from 'vue';
            import { useRoute } from 'vue-router';
            import { I%s } from '@/shared/model/%s.model';
            import %sService from './%s.service';

            export default defineComponent({
              name: '%sDetail',
              setup() {
                const route = useRoute();
                const %sService = new %sService();
                const %s = ref<I%s | null>(null);

                onMounted(async () => {
                  const id = Number(route.params.id);
                  %s.value = await %sService.find(id);
                });

                return {
                  %s,
                };
              },
            });
            </script>
            """.formatted(
                entityClass, entityInstance,
                fields.toString(),
                entityFile,
                entity.getReadOnly() ? "" :
                    "\n          <router-link :to=\"`/" + entityFile + "/${" + entityInstance + "?.id}/edit`\" class=\"btn btn-primary\">Edit</router-link>",
                entityClass, entityFile,
                entityClass, entityFile,
                entityClass,
                entityInstance, entityClass,
                entityInstance, entityClass,
                entityInstance, entityInstance,
                entityInstance
        );
        writeFile(basePath + entityFile + "-details.vue", template);
    }

    private void writeEntityUpdateComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        StringBuilder formFields = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            String inputType = getInputType(field);
            formFields.append("          <div class=\"mb-3\">\n");
            formFields.append("            <label for=\"").append(field.getFieldName()).append("\" class=\"form-label\">").append(capitalize(field.getFieldName())).append("</label>\n");
            formFields.append("            <input\n");
            formFields.append("              type=\"").append(inputType).append("\"\n");
            formFields.append("              id=\"").append(field.getFieldName()).append("\"\n");
            formFields.append("              class=\"form-control\"\n");
            formFields.append("              v-model=\"").append(entityInstance).append(".").append(field.getFieldName()).append("\"\n");
            if (field.getRequired()) {
                formFields.append("              required\n");
            }
            formFields.append("            />\n");
            formFields.append("          </div>\n");
        }

        String template = """
            <template>
              <div class="row justify-content-center">
                <div class="col-8">
                  <h2>{{ isNew ? 'Create' : 'Edit' }} %s</h2>
                  <form @submit.prevent="save">
            %s
                    <router-link to="/%s" class="btn btn-secondary">Cancel</router-link>
                    <button type="submit" class="btn btn-primary" :disabled="isSaving">Save</button>
                  </form>
                </div>
              </div>
            </template>

            <script lang="ts">
            import { defineComponent, ref, computed, onMounted } from 'vue';
            import { useRoute, useRouter } from 'vue-router';
            import { I%s, %s } from '@/shared/model/%s.model';
            import %sService from './%s.service';

            export default defineComponent({
              name: '%sUpdate',
              setup() {
                const route = useRoute();
                const router = useRouter();
                const %sService = new %sService();

                const %s = ref<I%s>(new %s());
                const isSaving = ref(false);
                const isNew = computed(() => !route.params.id);

                onMounted(async () => {
                  if (!isNew.value) {
                    const id = Number(route.params.id);
                    %s.value = await %sService.find(id);
                  }
                });

                const save = async () => {
                  isSaving.value = true;
                  try {
                    if (isNew.value) {
                      await %sService.create(%s.value);
                    } else {
                      await %sService.update(%s.value);
                    }
                    router.push('/%s');
                  } finally {
                    isSaving.value = false;
                  }
                };

                return {
                  %s,
                  isSaving,
                  isNew,
                  save,
                };
              },
            });
            </script>
            """.formatted(
                entityClass,
                formFields.toString(),
                entityFile,
                entityClass, entityClass, entityFile,
                entityClass, entityFile,
                entityClass,
                entityInstance, entityClass,
                entityInstance, entityClass, entityClass,
                entityInstance, entityInstance,
                entityInstance, entityInstance,
                entityInstance, entityInstance,
                entityFile,
                entityInstance
        );
        writeFile(basePath + entityFile + "-update.vue", template);
    }

    private void writeEntitiesRouter() throws IOException {
        StringBuilder imports = new StringBuilder();
        StringBuilder routes = new StringBuilder();

        for (EntityConfig entity : getConfig().getEntities()) {
            if (!entity.getBuiltIn() && !entity.getEmbedded()) {
                String entityClass = entity.getEntityReactName();
                String entityFile = entity.getEntityFileName();
                String entityFolder = entity.getEntityFolderName();

                routes.append("  {\n");
                routes.append("    path: '/").append(entityFile).append("',\n");
                routes.append("    name: '").append(entityClass).append("',\n");
                routes.append("    component: () => import('@/entities/").append(entityFolder).append("/").append(entityFile).append(".vue'),\n");
                routes.append("  },\n");
                routes.append("  {\n");
                routes.append("    path: '/").append(entityFile).append("/:id',\n");
                routes.append("    name: '").append(entityClass).append("Detail',\n");
                routes.append("    component: () => import('@/entities/").append(entityFolder).append("/").append(entityFile).append("-details.vue'),\n");
                routes.append("  },\n");

                if (!entity.getReadOnly()) {
                    routes.append("  {\n");
                    routes.append("    path: '/").append(entityFile).append("/new',\n");
                    routes.append("    name: '").append(entityClass).append("Create',\n");
                    routes.append("    component: () => import('@/entities/").append(entityFolder).append("/").append(entityFile).append("-update.vue'),\n");
                    routes.append("  },\n");
                    routes.append("  {\n");
                    routes.append("    path: '/").append(entityFile).append("/:id/edit',\n");
                    routes.append("    name: '").append(entityClass).append("Edit',\n");
                    routes.append("    component: () => import('@/entities/").append(entityFolder).append("/").append(entityFile).append("-update.vue'),\n");
                    routes.append("  },\n");
                }
            }
        }

        String content = """
            import { RouteRecordRaw } from 'vue-router';

            export const entityRoutes: RouteRecordRaw[] = [
            %s  // jhipster-needle-add-entity-to-router - JHipster will add entity routes here
            ];
            """.formatted(routes.toString());
        writeFile(getConfig().getClientWebappDir() + "router/entities.ts", content);
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
        log.info("Post-writing Vue configuration");
    }

    private void end() {
        log.info("Vue generation completed!");
        log.info("");
        log.info("==========================================================");
        log.info("Vue application generated successfully!");
        log.info("==========================================================");
    }
}
