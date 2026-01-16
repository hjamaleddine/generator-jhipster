/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.generators.react;

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
 * React Generator.
 * Generates React frontend application with Redux state management.
 * Equivalent to generators/react/generator.ts.
 */
public class ReactGenerator extends BaseClientGenerator {

    private NeedleService needleService;

    public ReactGenerator(ClientGeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "react";
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
        log.info("Initializing React generator");
        this.needleService = new NeedleService(context.getDestinationRoot());
    }

    private void loading() {
        log.info("Loading React dependencies");

        // Add React-specific template data
        context.addTemplateData("reactVersion", "18.2.0");
        context.addTemplateData("reduxVersion", "9.0.0");
        context.addTemplateData("typescriptVersion", "5.2.2");
    }

    private void preparing() {
        log.info("Preparing React generation context");

        // Prepare entities for React
        for (EntityConfig entity : getConfig().getEntities()) {
            prepareEntityForReact(entity);
        }
    }

    private void prepareEntityForReact(EntityConfig entity) {
        if (entity.getEntityReactName() == null) {
            entity.setEntityReactName(capitalize(entity.getName()));
        }
    }

    private void writing() {
        log.info("Writing React application files");

        try {
            // Core application files
            writeAppTsx();
            writeIndexTsx();
            writeRoutesTsx();

            // Config files
            writePackageJson();
            writeTsConfig();

            // Redux store
            writeReduxStore();

            // Shared reducers
            writeSharedReducers();

            // Core services
            writeAxiosConfig();
            writeAuthService();

            // Home module
            writeHomeModule();

            // Login module
            writeLoginModule();

            // Admin module
            writeAdminModule();

            // Layouts
            writeHeader();
            writeFooter();

            // Webpack configuration
            writeWebpackConfig();

            // SCSS styles
            writeStyles();

        } catch (IOException e) {
            throw new RuntimeException("Error writing React files", e);
        }
    }

    private void writeAppTsx() throws IOException {
        String content = """
            import React, { useEffect } from 'react';
            import { BrowserRouter } from 'react-router-dom';
            import { ToastContainer } from 'react-toastify';
            import { useAppDispatch, useAppSelector } from 'app/config/store';
            import { getSession } from 'app/shared/reducers/authentication';
            import Header from 'app/shared/layout/header/header';
            import Footer from 'app/shared/layout/footer/footer';
            import AppRoutes from 'app/routes';
            import ErrorBoundary from 'app/shared/error/error-boundary';
            %s
            import 'react-toastify/dist/ReactToastify.css';
            import './app.scss';

            const App = () => {
              const dispatch = useAppDispatch();
              const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
              const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
              const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);

              useEffect(() => {
                dispatch(getSession());
              }, [dispatch]);

              return (
                <BrowserRouter>
                  <div className="app-container">
                    <ToastContainer position="top-right" className="toastify-container" toastClassName="toastify-toast" />
                    <ErrorBoundary>
                      <Header isAuthenticated={isAuthenticated} ribbonEnv={ribbonEnv} isInProduction={isInProduction} />
                    </ErrorBoundary>
                    <div className="container-fluid view-container" id="app-view-container">
                      <ErrorBoundary>
                        <AppRoutes />
                      </ErrorBoundary>
                      <Footer />
                    </div>
                  </div>
                </BrowserRouter>
              );
            };

            export default App;
            """.formatted(
                getConfig().getEnableTranslation() ?
                    "import { TranslatorContext } from 'react-jhipster';" : ""
        );
        writeFile(getConfig().getClientWebappDir() + "app.tsx", content);
    }

    private void writeIndexTsx() throws IOException {
        String content = """
            import React from 'react';
            import { createRoot } from 'react-dom/client';
            import { Provider } from 'react-redux';
            import getStore from 'app/config/store';
            import App from 'app/app';
            %s

            const store = getStore();

            const container = document.getElementById('root');
            const root = createRoot(container!);

            root.render(
              <React.StrictMode>
                <Provider store={store}>
                  %s
                    <App />
                  %s
                </Provider>
              </React.StrictMode>
            );
            """.formatted(
                getConfig().getEnableTranslation() ?
                    "import { TranslatorContext } from 'react-jhipster';\n" +
                    "import { setLocale } from 'app/shared/reducers/locale';" : "",
                getConfig().getEnableTranslation() ?
                    "<TranslatorContext.Provider value={{ languages: ['en', 'fr'], currentLanguage: 'en' }}>" : "",
                getConfig().getEnableTranslation() ? "</TranslatorContext.Provider>" : ""
        );
        writeFile(getConfig().getClientSrcDir() + "index.tsx", content);
    }

    private void writeRoutesTsx() throws IOException {
        String content = """
            import React from 'react';
            import { Routes, Route, Navigate } from 'react-router-dom';
            import Loadable from 'react-loadable';

            import Home from 'app/modules/home/home';
            import Login from 'app/modules/login/login';
            import Logout from 'app/modules/login/logout';
            import PrivateRoute from 'app/shared/auth/private-route';
            import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
            import PageNotFound from 'app/shared/error/page-not-found';
            import { AUTHORITIES } from 'app/config/constants';

            const loading = () => <div className="spinner-border" role="status" />;

            const Admin = Loadable({
              loader: () => import('app/modules/administration'),
              loading: () => loading(),
            });

            const Entities = Loadable({
              loader: () => import('app/entities'),
              loading: () => loading(),
            });

            %s

            const AppRoutes = () => {
              return (
                <Routes>
                  <Route index element={<Home />} />
                  <Route path="login" element={<Login />} />
                  <Route path="logout" element={<Logout />} />
                  <Route
                    path="admin/*"
                    element={
                      <PrivateRoute hasAnyAuthorities={[AUTHORITIES.ADMIN]}>
                        <Admin />
                      </PrivateRoute>
                    }
                  />
                  %s
                  <Route
                    path="*"
                    element={
                      <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>
                        <Entities />
                      </PrivateRoute>
                    }
                  />
                  <Route path="*" element={<PageNotFound />} />
                </Routes>
              );
            };

            export default AppRoutes;
            """.formatted(
                getConfig().isOAuth2() ? "" :
                    "const Account = Loadable({\n" +
                    "  loader: () => import('app/modules/account'),\n" +
                    "  loading: () => loading(),\n" +
                    "});",
                getConfig().isOAuth2() ? "" :
                    "<Route path=\"account/*\" element={\n" +
                    "  <PrivateRoute hasAnyAuthorities={[AUTHORITIES.USER]}>\n" +
                    "    <Account />\n" +
                    "  </PrivateRoute>\n" +
                    "} />"
        );
        writeFile(getConfig().getClientWebappDir() + "routes.tsx", content);
    }

    private void writePackageJson() throws IOException {
        String content = """
            {
              "name": "%s",
              "version": "0.0.1-SNAPSHOT",
              "private": true,
              "description": "JHipster application generated with React",
              "license": "UNLICENSED",
              "scripts": {
                "start": "npm run webapp:dev",
                "build": "npm run webapp:prod",
                "webapp:dev": "webpack serve --config webpack/webpack.dev.js",
                "webapp:prod": "webpack --config webpack/webpack.prod.js",
                "test": "jest --coverage",
                "test:watch": "jest --watch",
                "lint": "eslint . --ext .ts,.tsx",
                "lint:fix": "npm run lint -- --fix",
                "e2e": "cypress run",
                "e2e:open": "cypress open"
              },
              "dependencies": {
                "@fortawesome/fontawesome-svg-core": "^6.4.2",
                "@fortawesome/free-solid-svg-icons": "^6.4.2",
                "@fortawesome/react-fontawesome": "^0.2.0",
                "@reduxjs/toolkit": "^2.0.0",
                "axios": "^1.6.2",
                "bootstrap": "^5.3.2",
                "dayjs": "^1.11.10",
                "lodash": "^4.17.21",
                "react": "^18.2.0",
                "react-dom": "^18.2.0",
                "react-hook-form": "^7.48.2",
                %s
                "react-loadable": "^5.5.0",
                "react-redux": "^9.0.0",
                "react-router-dom": "^6.20.0",
                "react-toastify": "^9.1.3",
                "reactstrap": "^9.2.1",
                "redux": "^5.0.0",
                "tslib": "^2.6.2",
                "uuid": "^9.0.1"
              },
              "devDependencies": {
                "@testing-library/jest-dom": "^6.1.5",
                "@testing-library/react": "^14.1.0",
                "@types/jest": "^29.5.10",
                "@types/lodash": "^4.14.202",
                "@types/react": "^18.2.42",
                "@types/react-dom": "^18.2.17",
                "@types/react-loadable": "^5.5.11",
                "autoprefixer": "^10.4.16",
                "copy-webpack-plugin": "^11.0.0",
                "css-loader": "^6.8.1",
                "css-minimizer-webpack-plugin": "^5.0.1",
                "eslint": "^8.55.0",
                "html-webpack-plugin": "^5.5.4",
                "jest": "^29.7.0",
                "jest-environment-jsdom": "^29.7.0",
                "mini-css-extract-plugin": "^2.7.6",
                "postcss-loader": "^7.3.3",
                "sass": "^1.69.5",
                "sass-loader": "^13.3.2",
                "style-loader": "^3.3.3",
                "terser-webpack-plugin": "^5.3.9",
                "ts-jest": "^29.1.1",
                "ts-loader": "^9.5.1",
                "typescript": "^5.2.2",
                "webpack": "^5.89.0",
                "webpack-cli": "^5.1.4",
                "webpack-dev-server": "^4.15.1",
                "webpack-merge": "^5.10.0",
                %s
                "workbox-webpack-plugin": "^7.0.0"
              }
            }
            """.formatted(
                getConfig().getBaseName().toLowerCase(),
                getConfig().getEnableTranslation() ?
                    "\"react-i18next\": \"^13.5.0\",\n    \"i18next\": \"^23.7.6\"," : "",
                getConfig().hasCypress() ? "\"cypress\": \"^13.6.0\"," : ""
        );
        writeFile("package.json", content);
    }

    private void writeTsConfig() throws IOException {
        String content = """
            {
              "compilerOptions": {
                "target": "ES2020",
                "lib": ["DOM", "DOM.Iterable", "ES2020"],
                "module": "ESNext",
                "moduleResolution": "bundler",
                "strict": true,
                "esModuleInterop": true,
                "skipLibCheck": true,
                "forceConsistentCasingInFileNames": true,
                "resolveJsonModule": true,
                "isolatedModules": true,
                "jsx": "react-jsx",
                "baseUrl": "./src/main/webapp",
                "paths": {
                  "app/*": ["app/*"]
                },
                "noEmit": true
              },
              "include": ["src/main/webapp/**/*"],
              "exclude": ["node_modules", "target"]
            }
            """;
        writeFile("tsconfig.json", content);
    }

    private void writeReduxStore() throws IOException {
        String content = """
            import { configureStore } from '@reduxjs/toolkit';
            import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';
            import reducer from 'app/shared/reducers';
            import errorMiddleware from 'app/config/error-middleware';
            import notificationMiddleware from 'app/config/notification-middleware';
            import loggerMiddleware from 'app/config/logger-middleware';

            const store = configureStore({
              reducer,
              middleware: getDefaultMiddleware =>
                getDefaultMiddleware({
                  serializableCheck: false,
                }).concat(errorMiddleware, notificationMiddleware, loggerMiddleware),
            });

            const getStore = () => store;

            export type IRootState = ReturnType<typeof store.getState>;
            export type AppDispatch = typeof store.dispatch;

            export const useAppSelector: TypedUseSelectorHook<IRootState> = useSelector;
            export const useAppDispatch = () => useDispatch<AppDispatch>();

            export default getStore;
            """;
        writeFile(getConfig().getClientWebappDir() + "config/store.ts", content);

        // Error middleware
        String errorMiddleware = """
            import { isRejectedWithValue, Middleware } from '@reduxjs/toolkit';
            import { toast } from 'react-toastify';

            const errorMiddleware: Middleware = () => next => action => {
              if (isRejectedWithValue(action)) {
                const payload = action.payload as any;
                if (payload?.status !== 401 && payload?.status !== 403) {
                  const message = payload?.data?.message || payload?.message || 'An error occurred';
                  toast.error(message);
                }
              }
              return next(action);
            };

            export default errorMiddleware;
            """;
        writeFile(getConfig().getClientWebappDir() + "config/error-middleware.ts", errorMiddleware);

        // Notification middleware
        String notificationMiddleware = """
            import { isFulfilled, Middleware } from '@reduxjs/toolkit';
            import { toast } from 'react-toastify';

            const notificationMiddleware: Middleware = () => next => action => {
              if (isFulfilled(action)) {
                const { meta } = action;
                if (meta?.successMessage) {
                  toast.success(meta.successMessage);
                }
              }
              return next(action);
            };

            export default notificationMiddleware;
            """;
        writeFile(getConfig().getClientWebappDir() + "config/notification-middleware.ts", notificationMiddleware);

        // Logger middleware
        String loggerMiddleware = """
            import { Middleware } from '@reduxjs/toolkit';

            const loggerMiddleware: Middleware = store => next => action => {
              if (process.env.NODE_ENV !== 'production') {
                console.group(action.type);
                console.info('dispatching', action);
                const result = next(action);
                console.log('next state', store.getState());
                console.groupEnd();
                return result;
              }
              return next(action);
            };

            export default loggerMiddleware;
            """;
        writeFile(getConfig().getClientWebappDir() + "config/logger-middleware.ts", loggerMiddleware);

        // Constants
        String constants = """
            export const AUTHORITIES = {
              ADMIN: 'ROLE_ADMIN',
              USER: 'ROLE_USER',
            };

            export const SERVER_API_URL = process.env.SERVER_API_URL || '/';
            """;
        writeFile(getConfig().getClientWebappDir() + "config/constants.ts", constants);
    }

    private void writeSharedReducers() throws IOException {
        String indexReducer = """
            import { combineReducers } from '@reduxjs/toolkit';
            import authentication from './authentication';
            import applicationProfile from './application-profile';
            %s

            // jhipster-needle-add-reducer-import - JHipster will add reducer here

            const rootReducer = combineReducers({
              authentication,
              applicationProfile,
              %s
              // jhipster-needle-add-reducer-combine - JHipster will add reducer here
            });

            export default rootReducer;
            """.formatted(
                getConfig().getEnableTranslation() ? "import locale from './locale';" : "",
                getConfig().getEnableTranslation() ? "locale," : ""
        );
        writeFile(getConfig().getClientWebappDir() + "shared/reducers/index.ts", indexReducer);

        // Authentication reducer
        String authReducer = """
            import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
            import axios from 'axios';
            import { serializeAxiosError } from './reducer.utils';

            export interface IAuthenticationState {
              isAuthenticated: boolean;
              account: any;
              loading: boolean;
              loginError: boolean;
              sessionHasBeenFetched: boolean;
            }

            const initialState: IAuthenticationState = {
              isAuthenticated: false,
              account: {},
              loading: false,
              loginError: false,
              sessionHasBeenFetched: false,
            };

            export const getSession = createAsyncThunk(
              'authentication/getSession',
              async () => {
                const response = await axios.get('/api/account');
                return response.data;
              },
              { serializeError: serializeAxiosError }
            );

            %s

            export const logout = createAsyncThunk('authentication/logout', async () => {
              %s
              return {};
            });

            const AuthenticationSlice = createSlice({
              name: 'authentication',
              initialState,
              reducers: {
                clearAuth(state) {
                  state.isAuthenticated = false;
                  state.account = {};
                },
              },
              extraReducers(builder) {
                builder
                  .addCase(getSession.pending, state => {
                    state.loading = true;
                  })
                  .addCase(getSession.fulfilled, (state, action) => {
                    state.isAuthenticated = true;
                    state.loading = false;
                    state.sessionHasBeenFetched = true;
                    state.account = action.payload;
                  })
                  .addCase(getSession.rejected, (state, action) => {
                    state.loading = false;
                    state.isAuthenticated = false;
                    state.sessionHasBeenFetched = true;
                  })
                  %s
                  .addCase(logout.fulfilled, state => {
                    state.isAuthenticated = false;
                    state.account = {};
                  });
              },
            });

            export const { clearAuth } = AuthenticationSlice.actions;
            export default AuthenticationSlice.reducer;
            """.formatted(
                getConfig().isOAuth2() ? "" :
                    "export const login = createAsyncThunk(\n" +
                    "  'authentication/login',\n" +
                    "  async (auth: { username: string; password: string; rememberMe: boolean }) => {\n" +
                    "    const response = await axios.post('/api/authenticate', auth);\n" +
                    "    const token = response.headers['authorization']?.replace('Bearer ', '');\n" +
                    "    if (token) {\n" +
                    "      localStorage.setItem('authToken', token);\n" +
                    "    }\n" +
                    "    return response.data;\n" +
                    "  },\n" +
                    "  { serializeError: serializeAxiosError }\n" +
                    ");",
                getConfig().isOAuth2() ?
                    "window.location.href = '/api/logout';" :
                    "localStorage.removeItem('authToken');\n    await axios.post('/api/logout');",
                getConfig().isOAuth2() ? "" :
                    ".addCase(login.fulfilled, state => {\n" +
                    "    state.loginError = false;\n" +
                    "  })\n" +
                    "  .addCase(login.rejected, state => {\n" +
                    "    state.loginError = true;\n" +
                    "  })"
        );
        writeFile(getConfig().getClientWebappDir() + "shared/reducers/authentication.ts", authReducer);

        // Application profile reducer
        String profileReducer = """
            import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
            import axios from 'axios';

            export interface IApplicationProfileState {
              ribbonEnv: string;
              inProduction: boolean;
              isOpenAPIEnabled: boolean;
            }

            const initialState: IApplicationProfileState = {
              ribbonEnv: '',
              inProduction: true,
              isOpenAPIEnabled: false,
            };

            export const getProfile = createAsyncThunk('applicationProfile/getProfile', async () => {
              const response = await axios.get('/management/info');
              return response.data;
            });

            const ApplicationProfileSlice = createSlice({
              name: 'applicationProfile',
              initialState,
              reducers: {},
              extraReducers(builder) {
                builder.addCase(getProfile.fulfilled, (state, action) => {
                  const data = action.payload;
                  state.ribbonEnv = data?.activeProfiles?.includes('dev') ? 'dev' : '';
                  state.inProduction = data?.activeProfiles?.includes('prod');
                  state.isOpenAPIEnabled = data?.activeProfiles?.includes('api-docs');
                });
              },
            });

            export default ApplicationProfileSlice.reducer;
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/reducers/application-profile.ts", profileReducer);

        // Reducer utils
        String reducerUtils = """
            import { SerializedError } from '@reduxjs/toolkit';
            import { AxiosError } from 'axios';

            export const serializeAxiosError = (value: any): SerializedError => {
              if (value instanceof AxiosError) {
                return {
                  name: value.name,
                  message: value.message,
                  code: value.code,
                };
              }
              return value;
            };
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/reducers/reducer.utils.ts", reducerUtils);

        // Locale reducer (if i18n enabled)
        if (getConfig().getEnableTranslation()) {
            String localeReducer = """
                import { createSlice, PayloadAction } from '@reduxjs/toolkit';

                export interface ILocaleState {
                  currentLocale: string;
                }

                const initialState: ILocaleState = {
                  currentLocale: '%s',
                };

                const LocaleSlice = createSlice({
                  name: 'locale',
                  initialState,
                  reducers: {
                    setLocale(state, action: PayloadAction<string>) {
                      state.currentLocale = action.payload;
                    },
                  },
                });

                export const { setLocale } = LocaleSlice.actions;
                export default LocaleSlice.reducer;
                """.formatted(getConfig().getNativeLanguage());
            writeFile(getConfig().getClientWebappDir() + "shared/reducers/locale.ts", localeReducer);
        }
    }

    private void writeAxiosConfig() throws IOException {
        String content = """
            import axios from 'axios';
            import { SERVER_API_URL } from 'app/config/constants';

            const TIMEOUT = 60000;

            axios.defaults.timeout = TIMEOUT;
            axios.defaults.baseURL = SERVER_API_URL;

            // Add auth token to requests
            axios.interceptors.request.use(config => {
              const token = localStorage.getItem('authToken');
              if (token) {
                config.headers.Authorization = `Bearer ${token}`;
              }
              return config;
            });

            // Handle 401 responses
            axios.interceptors.response.use(
              response => response,
              error => {
                if (error.response?.status === 401) {
                  localStorage.removeItem('authToken');
                }
                return Promise.reject(error);
              }
            );

            export default axios;
            """;
        writeFile(getConfig().getClientWebappDir() + "config/axios-interceptor.ts", content);
    }

    private void writeAuthService() throws IOException {
        // Private route component
        String privateRoute = """
            import React from 'react';
            import { Navigate, useLocation } from 'react-router-dom';
            import { useAppSelector } from 'app/config/store';

            interface IPrivateRouteProps {
              children: React.ReactNode;
              hasAnyAuthorities?: string[];
            }

            const PrivateRoute = ({ children, hasAnyAuthorities = [] }: IPrivateRouteProps) => {
              const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
              const sessionFetched = useAppSelector(state => state.authentication.sessionHasBeenFetched);
              const account = useAppSelector(state => state.authentication.account);
              const location = useLocation();

              if (!sessionFetched) {
                return <div className="spinner-border" role="status" />;
              }

              if (!isAuthenticated) {
                return <Navigate to="/login" state={{ from: location }} replace />;
              }

              if (hasAnyAuthorities.length > 0) {
                const authorities = account?.authorities || [];
                const hasAuthority = hasAnyAuthorities.some(auth => authorities.includes(auth));
                if (!hasAuthority) {
                  return <Navigate to="/accessdenied" replace />;
                }
              }

              return <>{children}</>;
            };

            export default PrivateRoute;
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/auth/private-route.tsx", privateRoute);
    }

    private void writeHomeModule() throws IOException {
        String homeComponent = """
            import React from 'react';
            import { Link } from 'react-router-dom';
            import { Row, Col, Alert } from 'reactstrap';
            import { useAppSelector } from 'app/config/store';
            %s
            import './home.scss';

            const Home = () => {
              const account = useAppSelector(state => state.authentication.account);

              return (
                <Row>
                  <Col md="9">
                    <h2>%s</h2>
                    <p className="lead">%s</p>
                    {account?.login ? (
                      <Alert color="success">
                        %s
                      </Alert>
                    ) : (
                      <Alert color="warning">
                        %s
                        <Link to="/login" className="alert-link">
                          %s
                        </Link>
                      </Alert>
                    )}
                  </Col>
                </Row>
              );
            };

            export default Home;
            """.formatted(
                getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                getConfig().getEnableTranslation() ?
                    "<Translate contentKey=\"home.title\">Welcome, " + getConfig().getBaseName() + "!</Translate>" :
                    "Welcome, " + getConfig().getBaseName() + "!",
                getConfig().getEnableTranslation() ?
                    "<Translate contentKey=\"home.subtitle\">This is your homepage</Translate>" :
                    "This is your homepage",
                getConfig().getEnableTranslation() ?
                    "<Translate contentKey=\"home.logged.message\" interpolate={{ username: account.login }}>You are logged in as {account.login}</Translate>" :
                    "You are logged in as {account.login}",
                getConfig().getEnableTranslation() ?
                    "<Translate contentKey=\"home.notLogged.message\">You are not signed in yet.</Translate>" :
                    "You are not signed in yet. ",
                getConfig().getEnableTranslation() ?
                    "<Translate contentKey=\"global.menu.account.login\">Sign in</Translate>" :
                    "Sign in"
        );
        writeFile(getConfig().getClientWebappDir() + "modules/home/home.tsx", homeComponent);

        String homeScss = """
            .hipster {
              display: inline-block;
              width: 100%;
              height: 497px;
              background-size: contain;
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "modules/home/home.scss", homeScss);
    }

    private void writeLoginModule() throws IOException {
        String loginComponent;
        if (getConfig().isOAuth2()) {
            loginComponent = """
                import React, { useEffect } from 'react';

                const Login = () => {
                  useEffect(() => {
                    window.location.href = '/oauth2/authorization/oidc';
                  }, []);

                  return <div>Redirecting to login...</div>;
                };

                export default Login;
                """;
        } else {
            loginComponent = """
                import React, { useState } from 'react';
                import { Navigate, useLocation } from 'react-router-dom';
                import { useForm } from 'react-hook-form';
                import { Button, Form, FormGroup, Label, Input, Alert, Row, Col } from 'reactstrap';
                import { useAppDispatch, useAppSelector } from 'app/config/store';
                import { login } from 'app/shared/reducers/authentication';
                %s

                interface ILoginForm {
                  username: string;
                  password: string;
                  rememberMe: boolean;
                }

                const Login = () => {
                  const dispatch = useAppDispatch();
                  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
                  const loginError = useAppSelector(state => state.authentication.loginError);
                  const location = useLocation();
                  const { register, handleSubmit, formState: { errors } } = useForm<ILoginForm>();

                  const from = (location.state as any)?.from?.pathname || '/';

                  if (isAuthenticated) {
                    return <Navigate to={from} replace />;
                  }

                  const onSubmit = (data: ILoginForm) => {
                    dispatch(login(data));
                  };

                  return (
                    <Row className="justify-content-center">
                      <Col md="8">
                        <h1>%s</h1>
                        {loginError && (
                          <Alert color="danger">
                            %s
                          </Alert>
                        )}
                        <Form onSubmit={handleSubmit(onSubmit)}>
                          <FormGroup>
                            <Label for="username">%s</Label>
                            <Input
                              type="text"
                              id="username"
                              {...register('username', { required: true })}
                              invalid={!!errors.username}
                            />
                          </FormGroup>
                          <FormGroup>
                            <Label for="password">%s</Label>
                            <Input
                              type="password"
                              id="password"
                              {...register('password', { required: true })}
                              invalid={!!errors.password}
                            />
                          </FormGroup>
                          <FormGroup check>
                            <Label check>
                              <Input type="checkbox" {...register('rememberMe')} /> %s
                            </Label>
                          </FormGroup>
                          <Button color="primary" type="submit">
                            %s
                          </Button>
                        </Form>
                      </Col>
                    </Row>
                  );
                };

                export default Login;
                """.formatted(
                    getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                    getConfig().getEnableTranslation() ? "<Translate contentKey=\"login.title\">Sign in</Translate>" : "Sign in",
                    getConfig().getEnableTranslation() ?
                        "<Translate contentKey=\"login.messages.error.authentication\">Failed to sign in!</Translate>" :
                        "Failed to sign in!",
                    getConfig().getEnableTranslation() ?
                        "<Translate contentKey=\"global.form.username.label\">Username</Translate>" : "Username",
                    getConfig().getEnableTranslation() ?
                        "<Translate contentKey=\"login.form.password\">Password</Translate>" : "Password",
                    getConfig().getEnableTranslation() ?
                        "<Translate contentKey=\"login.form.rememberme\">Remember me</Translate>" : "Remember me",
                    getConfig().getEnableTranslation() ?
                        "<Translate contentKey=\"login.form.button\">Sign in</Translate>" : "Sign in"
            );
        }
        writeFile(getConfig().getClientWebappDir() + "modules/login/login.tsx", loginComponent);

        // Logout component
        String logoutComponent = """
            import React, { useLayoutEffect } from 'react';
            import { useAppDispatch, useAppSelector } from 'app/config/store';
            import { logout } from 'app/shared/reducers/authentication';

            const Logout = () => {
              const dispatch = useAppDispatch();
              const logoutUrl = useAppSelector(state => state.authentication.logoutUrl);

              useLayoutEffect(() => {
                dispatch(logout());
                if (logoutUrl) {
                  window.location.href = logoutUrl;
                }
              }, [dispatch, logoutUrl]);

              return (
                <div className="p-5">
                  <h4>Logged out successfully!</h4>
                </div>
              );
            };

            export default Logout;
            """;
        writeFile(getConfig().getClientWebappDir() + "modules/login/logout.tsx", logoutComponent);
    }

    private void writeAdminModule() throws IOException {
        // Admin index
        String adminIndex = """
            import React from 'react';
            import { Route, Routes } from 'react-router-dom';
            import Loadable from 'react-loadable';

            const loading = () => <div className="spinner-border" role="status" />;

            const UserManagement = Loadable({
              loader: () => import('./user-management'),
              loading: () => loading(),
            });

            const Health = Loadable({
              loader: () => import('./health/health'),
              loading: () => loading(),
            });

            const Logs = Loadable({
              loader: () => import('./logs/logs'),
              loading: () => loading(),
            });

            const Configuration = Loadable({
              loader: () => import('./configuration/configuration'),
              loading: () => loading(),
            });

            const Metrics = Loadable({
              loader: () => import('./metrics/metrics'),
              loading: () => loading(),
            });

            const Docs = Loadable({
              loader: () => import('./docs/docs'),
              loading: () => loading(),
            });

            const Admin = () => (
              <Routes>
                <Route path="user-management/*" element={<UserManagement />} />
                <Route path="health" element={<Health />} />
                <Route path="logs" element={<Logs />} />
                <Route path="configuration" element={<Configuration />} />
                <Route path="metrics" element={<Metrics />} />
                <Route path="docs" element={<Docs />} />
              </Routes>
            );

            export default Admin;
            """;
        writeFile(getConfig().getClientWebappDir() + "modules/administration/index.tsx", adminIndex);
    }

    private void writeHeader() throws IOException {
        String header = """
            import React, { useState } from 'react';
            import { Link, NavLink as RouterNavLink } from 'react-router-dom';
            import {
              Navbar,
              Nav,
              NavItem,
              NavLink,
              NavbarToggler,
              Collapse,
              NavbarBrand,
              DropdownToggle,
              DropdownMenu,
              DropdownItem,
              UncontrolledDropdown,
            } from 'reactstrap';
            import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
            %s
            import './header.scss';

            interface IHeaderProps {
              isAuthenticated: boolean;
              ribbonEnv: string;
              isInProduction: boolean;
            }

            const Header = ({ isAuthenticated, ribbonEnv, isInProduction }: IHeaderProps) => {
              const [menuOpen, setMenuOpen] = useState(false);

              const toggleMenu = () => setMenuOpen(!menuOpen);

              return (
                <Navbar dark expand="md" className="bg-dark">
                  <NavbarBrand tag={Link} to="/" className="brand-logo">
                    <span className="navbar-title">%s</span>
                  </NavbarBrand>
                  <NavbarToggler onClick={toggleMenu} />
                  <Collapse isOpen={menuOpen} navbar>
                    <Nav className="me-auto" navbar>
                      <NavItem>
                        <NavLink tag={RouterNavLink} to="/" className="nav-link">
                          <FontAwesomeIcon icon="home" />
                          <span>%s</span>
                        </NavLink>
                      </NavItem>
                      {isAuthenticated && (
                        <UncontrolledDropdown nav inNavbar>
                          <DropdownToggle nav caret>
                            <FontAwesomeIcon icon="th-list" />
                            <span>%s</span>
                          </DropdownToggle>
                          <DropdownMenu>
                            {/* jhipster-needle-add-entity-to-menu - JHipster will add entities here */}
                          </DropdownMenu>
                        </UncontrolledDropdown>
                      )}
                      {isAuthenticated && (
                        <UncontrolledDropdown nav inNavbar>
                          <DropdownToggle nav caret>
                            <FontAwesomeIcon icon="user-shield" />
                            <span>%s</span>
                          </DropdownToggle>
                          <DropdownMenu>
                            <DropdownItem tag={RouterNavLink} to="/admin/user-management">
                              User management
                            </DropdownItem>
                            <DropdownItem tag={RouterNavLink} to="/admin/metrics">
                              Metrics
                            </DropdownItem>
                            <DropdownItem tag={RouterNavLink} to="/admin/health">
                              Health
                            </DropdownItem>
                            <DropdownItem tag={RouterNavLink} to="/admin/configuration">
                              Configuration
                            </DropdownItem>
                            <DropdownItem tag={RouterNavLink} to="/admin/logs">
                              Logs
                            </DropdownItem>
                            <DropdownItem tag={RouterNavLink} to="/admin/docs">
                              API Docs
                            </DropdownItem>
                          </DropdownMenu>
                        </UncontrolledDropdown>
                      )}
                    </Nav>
                    <Nav className="ms-auto" navbar>
                      {isAuthenticated ? (
                        <NavItem>
                          <NavLink tag={RouterNavLink} to="/logout">
                            <FontAwesomeIcon icon="sign-out-alt" />
                            <span>%s</span>
                          </NavLink>
                        </NavItem>
                      ) : (
                        <NavItem>
                          <NavLink tag={RouterNavLink} to="/login">
                            <FontAwesomeIcon icon="sign-in-alt" />
                            <span>%s</span>
                          </NavLink>
                        </NavItem>
                      )}
                    </Nav>
                  </Collapse>
                </Navbar>
              );
            };

            export default Header;
            """.formatted(
                getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                getConfig().getBaseName(),
                getConfig().getEnableTranslation() ? "<Translate contentKey=\"global.menu.home\">Home</Translate>" : "Home",
                getConfig().getEnableTranslation() ? "<Translate contentKey=\"global.menu.entities.main\">Entities</Translate>" : "Entities",
                getConfig().getEnableTranslation() ? "<Translate contentKey=\"global.menu.admin.main\">Administration</Translate>" : "Administration",
                getConfig().getEnableTranslation() ? "<Translate contentKey=\"global.menu.account.logout\">Sign out</Translate>" : "Sign out",
                getConfig().getEnableTranslation() ? "<Translate contentKey=\"global.menu.account.login\">Sign in</Translate>" : "Sign in"
        );
        writeFile(getConfig().getClientWebappDir() + "shared/layout/header/header.tsx", header);

        String headerScss = """
            .navbar {
              .navbar-title {
                font-weight: bold;
              }
              .brand-logo {
                .navbar-brand {
                  color: white;
                }
              }
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/layout/header/header.scss", headerScss);
    }

    private void writeFooter() throws IOException {
        String footer = """
            import React from 'react';
            import './footer.scss';

            const Footer = () => (
              <footer className="footer">
                <div className="container">
                  <p className="text-muted">
                    &copy; %s - Powered by JHipster
                  </p>
                </div>
              </footer>
            );

            export default Footer;
            """.formatted(getConfig().getBaseName());
        writeFile(getConfig().getClientWebappDir() + "shared/layout/footer/footer.tsx", footer);

        String footerScss = """
            .footer {
              position: fixed;
              bottom: 0;
              width: 100%%;
              height: 60px;
              line-height: 60px;
              background-color: #f5f5f5;
            }
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/layout/footer/footer.scss", footerScss);
    }

    private void writeWebpackConfig() throws IOException {
        // Webpack common
        String webpackCommon = """
            const path = require('path');
            const HtmlWebpackPlugin = require('html-webpack-plugin');
            const CopyWebpackPlugin = require('copy-webpack-plugin');
            const MiniCssExtractPlugin = require('mini-css-extract-plugin');

            module.exports = {
              entry: './src/main/webapp/index.tsx',
              output: {
                path: path.resolve('./target/classes/static/'),
                filename: '[name].[contenthash].js',
                publicPath: '/',
              },
              resolve: {
                extensions: ['.ts', '.tsx', '.js', '.jsx'],
                alias: {
                  app: path.resolve('./src/main/webapp/app/'),
                },
              },
              module: {
                rules: [
                  {
                    test: /\\.tsx?$/,
                    use: 'ts-loader',
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
                  {
                    test: /\\.(png|svg|jpg|jpeg|gif)$/i,
                    type: 'asset/resource',
                  },
                  {
                    test: /\\.(woff|woff2|eot|ttf|otf)$/i,
                    type: 'asset/resource',
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
                    { from: './src/main/webapp/favicon.ico', to: 'favicon.ico' },
                    { from: './src/main/webapp/manifest.webapp', to: 'manifest.webapp' },
                  ],
                }),
                new MiniCssExtractPlugin({
                  filename: '[name].[contenthash].css',
                }),
                // jhipster-needle-add-webpack-config
              ],
            };
            """;
        writeFile("webpack/webpack.common.js", webpackCommon);

        // Webpack dev
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
                  '/v3/api-docs': {
                    target: 'http://localhost:8080',
                    secure: false,
                  },
                },
              },
            });
            """.formatted(getConfig().getDevServerPort());
        writeFile("webpack/webpack.dev.js", webpackDev);

        // Webpack prod
        String webpackProd = """
            const { merge } = require('webpack-merge');
            const common = require('./webpack.common.js');
            const TerserPlugin = require('terser-webpack-plugin');
            const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');

            module.exports = merge(common, {
              mode: 'production',
              optimization: {
                minimizer: [new TerserPlugin(), new CssMinimizerPlugin()],
                splitChunks: {
                  chunks: 'all',
                },
              },
            });
            """;
        writeFile("webpack/webpack.prod.js", webpackProd);
    }

    private void writeStyles() throws IOException {
        // Main app SCSS
        String appScss = """
            @import '~bootstrap/scss/bootstrap';

            .app-container {
              min-height: 100vh;
              display: flex;
              flex-direction: column;
            }

            .view-container {
              flex: 1;
              padding-bottom: 70px;
            }

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
        writeFile(getConfig().getClientWebappDir() + "app.scss", appScss);

        // Index HTML
        String indexHtml = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <meta name="theme-color" content="#000000" />
                <meta name="description" content="JHipster application - %s" />
                <link rel="icon" href="favicon.ico" />
                <title>%s</title>
              </head>
              <body>
                <noscript>You need to enable JavaScript to run this app.</noscript>
                <div id="root">
                  <div class="app-loading">
                    <div class="spinner"></div>
                    <p class="loading-text">Loading...</p>
                  </div>
                </div>
              </body>
            </html>
            """.formatted(getConfig().getBaseName(), getConfig().getBaseName());
        writeFile(getConfig().getClientSrcDir() + "index.html", indexHtml);
    }

    private void writingEntities() {
        log.info("Writing React entity files");

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

        // Write entities index
        try {
            writeEntitiesIndex();
        } catch (IOException e) {
            log.error("Error writing entities index", e);
        }
    }

    private void writeEntityFiles(EntityConfig entity) throws IOException {
        String entityFolder = entity.getEntityFolderName();
        String entityFile = entity.getEntityFileName();
        String entityClass = entity.getEntityReactName();
        String basePath = getConfig().getClientWebappDir() + "entities/" + entityFolder + "/";

        // Entity model
        writeEntityModel(entity);

        // Entity reducer
        writeEntityReducer(entity, basePath);

        // Entity index (routing)
        writeEntityIndex(entity, basePath);

        // List component
        writeEntityListComponent(entity, basePath);

        // Detail component
        writeEntityDetailComponent(entity, basePath);

        // Update component (if not read-only)
        if (!entity.getReadOnly()) {
            writeEntityUpdateComponent(entity, basePath);
            writeEntityDeleteDialog(entity, basePath);
        }
    }

    private void writeEntityModel(EntityConfig entity) throws IOException {
        StringBuilder content = new StringBuilder();

        // Imports
        if (entity.hasDateField()) {
            content.append("import dayjs from 'dayjs';\n");
        }

        // Import related entities
        Set<String> imports = new HashSet<>();
        for (RelationshipConfig rel : entity.getRelationships()) {
            imports.add(rel.getOtherEntityAngularName());
        }
        for (String imp : imports) {
            content.append("import { I").append(imp).append(" } from 'app/shared/model/")
                   .append(toKebabCase(imp)).append(".model';\n");
        }

        if (!imports.isEmpty() || entity.hasDateField()) {
            content.append("\n");
        }

        // Interface
        content.append("export interface I").append(entity.getEntityReactName()).append(" {\n");
        content.append("  id?: number;\n");

        for (FieldConfig field : entity.getFields()) {
            String optional = field.getRequired() ? "" : "?";
            content.append("  ").append(field.getFieldName()).append(optional).append("?: ")
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

        // Default value
        content.append("export const defaultValue: Readonly<I").append(entity.getEntityReactName()).append("> = {};\n");

        writeFile(getConfig().getClientWebappDir() + "shared/model/" + entity.getEntityFileName() + ".model.ts", content.toString());
    }

    private void writeEntityReducer(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityUrl = toKebabCase(entity.getName());

        String content = """
            import { createAsyncThunk, createSlice, isFulfilled, isPending } from '@reduxjs/toolkit';
            import axios from 'axios';
            import { I%s, defaultValue } from 'app/shared/model/%s.model';
            import { serializeAxiosError } from 'app/shared/reducers/reducer.utils';

            const apiUrl = '/api/%s';

            interface I%sState {
              loading: boolean;
              errorMessage: string | null;
              entities: I%s[];
              entity: I%s;
              updating: boolean;
              updateSuccess: boolean;
            }

            const initialState: I%sState = {
              loading: false,
              errorMessage: null,
              entities: [],
              entity: defaultValue,
              updating: false,
              updateSuccess: false,
            };

            // Async actions
            export const getEntities = createAsyncThunk('%s/fetch_entity_list', async () => {
              const response = await axios.get<I%s[]>(apiUrl);
              return response.data;
            });

            export const getEntity = createAsyncThunk(
              '%s/fetch_entity',
              async (id: string | number) => {
                const response = await axios.get<I%s>(`${apiUrl}/${id}`);
                return response.data;
              },
              { serializeError: serializeAxiosError }
            );

            export const createEntity = createAsyncThunk(
              '%s/create_entity',
              async (entity: I%s, thunkAPI) => {
                const response = await axios.post<I%s>(apiUrl, entity);
                thunkAPI.dispatch(getEntities());
                return response.data;
              },
              { serializeError: serializeAxiosError }
            );

            export const updateEntity = createAsyncThunk(
              '%s/update_entity',
              async (entity: I%s, thunkAPI) => {
                const response = await axios.put<I%s>(`${apiUrl}/${entity.id}`, entity);
                thunkAPI.dispatch(getEntities());
                return response.data;
              },
              { serializeError: serializeAxiosError }
            );

            export const deleteEntity = createAsyncThunk(
              '%s/delete_entity',
              async (id: string | number, thunkAPI) => {
                await axios.delete(`${apiUrl}/${id}`);
                thunkAPI.dispatch(getEntities());
                return id;
              },
              { serializeError: serializeAxiosError }
            );

            // Slice
            const %sSlice = createSlice({
              name: '%s',
              initialState,
              reducers: {
                reset() {
                  return initialState;
                },
              },
              extraReducers(builder) {
                builder
                  .addMatcher(isFulfilled(getEntities), (state, action) => {
                    state.loading = false;
                    state.entities = action.payload;
                  })
                  .addMatcher(isFulfilled(getEntity), (state, action) => {
                    state.loading = false;
                    state.entity = action.payload;
                  })
                  .addMatcher(isFulfilled(createEntity, updateEntity), (state, action) => {
                    state.updating = false;
                    state.updateSuccess = true;
                    state.entity = action.payload;
                  })
                  .addMatcher(isFulfilled(deleteEntity), state => {
                    state.updating = false;
                    state.updateSuccess = true;
                    state.entity = defaultValue;
                  })
                  .addMatcher(isPending(getEntities, getEntity), state => {
                    state.loading = true;
                    state.errorMessage = null;
                  })
                  .addMatcher(isPending(createEntity, updateEntity, deleteEntity), state => {
                    state.updating = true;
                    state.updateSuccess = false;
                    state.errorMessage = null;
                  });
              },
            });

            export const { reset } = %sSlice.actions;
            export default %sSlice.reducer;
            """.formatted(
                entityClass, entityFile, entityUrl,
                entityClass, entityClass, entityClass,
                entityClass, uncapitalize(entityClass), entityClass,
                uncapitalize(entityClass), entityClass,
                uncapitalize(entityClass), entityClass, entityClass,
                uncapitalize(entityClass), entityClass, entityClass,
                uncapitalize(entityClass),
                entityClass, uncapitalize(entityClass),
                entityClass, entityClass
        );

        writeFile(basePath + entityFile + ".reducer.ts", content);
    }

    private void writeEntityIndex(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();

        String content = """
            import React from 'react';
            import { Route, Routes } from 'react-router-dom';

            import %s from './%s';
            import %sDetail from './%s-detail';
            %s

            const %sRoutes = () => (
              <Routes>
                <Route index element={<%s />} />
                %s
                <Route path=":id" element={<%sDetail />} />
              </Routes>
            );

            export default %sRoutes;
            """.formatted(
                entityClass, entityFile,
                entityClass, entityFile,
                entity.getReadOnly() ? "" :
                    "import " + entityClass + "Update from './" + entityFile + "-update';\n" +
                    "import " + entityClass + "DeleteDialog from './" + entityFile + "-delete-dialog';",
                entityClass,
                entityClass,
                entity.getReadOnly() ? "" :
                    "<Route path=\"new\" element={<" + entityClass + "Update />} />\n" +
                    "    <Route path=\":id/edit\" element={<" + entityClass + "Update />} />\n" +
                    "    <Route path=\":id/delete\" element={<" + entityClass + "DeleteDialog />} />",
                entityClass,
                entityClass
        );

        writeFile(basePath + "index.tsx", content);
    }

    private void writeEntityListComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        StringBuilder columns = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            columns.append("                <th>").append(capitalize(field.getFieldName())).append("</th>\n");
        }

        StringBuilder cells = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            cells.append("                  <td>{").append(entityInstance).append(".").append(field.getFieldName()).append("}</td>\n");
        }

        String content = """
            import React, { useEffect } from 'react';
            import { Link } from 'react-router-dom';
            import { Button, Table } from 'reactstrap';
            import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
            import { useAppDispatch, useAppSelector } from 'app/config/store';
            import { getEntities } from './%s.reducer';
            %s

            const %s = () => {
              const dispatch = useAppDispatch();
              const %sList = useAppSelector(state => state.%s.entities);
              const loading = useAppSelector(state => state.%s.loading);

              useEffect(() => {
                dispatch(getEntities());
              }, [dispatch]);

              return (
                <div>
                  <h2>
                    %ss
                    %s
                  </h2>
                  {loading ? (
                    <div className="spinner-border" role="status" />
                  ) : (
                    <Table responsive>
                      <thead>
                        <tr>
                          <th>ID</th>
            %s              <th />
                        </tr>
                      </thead>
                      <tbody>
                        {%sList.map((%s, i) => (
                          <tr key={`entity-${i}`}>
                            <td>
                              <Button tag={Link} to={`/%s/${%s.id}`} color="link">
                                {%s.id}
                              </Button>
                            </td>
            %s              <td className="text-end">
                              <div className="btn-group flex-btn-group-container">
                                <Button tag={Link} to={`/%s/${%s.id}`} color="info" size="sm">
                                  <FontAwesomeIcon icon="eye" />
                                </Button>
                                %s
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  )}
                </div>
              );
            };

            export default %s;
            """.formatted(
                entityFile,
                getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                entityClass,
                entityInstance, entityInstance, entityInstance,
                entityClass,
                entity.getReadOnly() ? "" :
                    "\n            <Link to={`/" + entityFile + "/new`} className=\"btn btn-primary float-end\">\n" +
                    "              <FontAwesomeIcon icon=\"plus\" /> New\n" +
                    "            </Link>",
                columns.toString(),
                entityInstance, entityInstance,
                entityFile, entityInstance,
                entityInstance,
                cells.toString(),
                entityFile, entityInstance,
                entity.getReadOnly() ? "" :
                    "<Button tag={Link} to={`/" + entityFile + "/${" + entityInstance + ".id}/edit`} color=\"primary\" size=\"sm\">\n" +
                    "                          <FontAwesomeIcon icon=\"pencil-alt\" />\n" +
                    "                        </Button>\n" +
                    "                        <Button tag={Link} to={`/" + entityFile + "/${" + entityInstance + ".id}/delete`} color=\"danger\" size=\"sm\">\n" +
                    "                          <FontAwesomeIcon icon=\"trash\" />\n" +
                    "                        </Button>",
                entityClass
        );

        writeFile(basePath + entityFile + ".tsx", content);
    }

    private void writeEntityDetailComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        StringBuilder fields = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            fields.append("            <dt>").append(capitalize(field.getFieldName())).append("</dt>\n");
            fields.append("            <dd>{").append(entityInstance).append("Entity.").append(field.getFieldName()).append("}</dd>\n");
        }

        String content = """
            import React, { useEffect } from 'react';
            import { Link, useParams } from 'react-router-dom';
            import { Button, Row, Col } from 'reactstrap';
            import { useAppDispatch, useAppSelector } from 'app/config/store';
            import { getEntity } from './%s.reducer';
            %s

            const %sDetail = () => {
              const dispatch = useAppDispatch();
              const { id } = useParams<'id'>();
              const %sEntity = useAppSelector(state => state.%s.entity);

              useEffect(() => {
                dispatch(getEntity(id!));
              }, [dispatch, id]);

              return (
                <Row>
                  <Col md="8">
                    <h2>
                      %s [<b>{%sEntity.id}</b>]
                    </h2>
                    <dl className="jh-entity-details">
            %s        </dl>
                    <Button tag={Link} to="/%s" replace color="info">
                      <span>Back</span>
                    </Button>
                    %s
                  </Col>
                </Row>
              );
            };

            export default %sDetail;
            """.formatted(
                entityFile,
                getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                entityClass,
                entityInstance, entityInstance,
                entityClass, entityInstance,
                fields.toString(),
                entityFile,
                entity.getReadOnly() ? "" :
                    "\n            <Button tag={Link} to={`/" + entityFile + "/${" + entityInstance + "Entity.id}/edit`} replace color=\"primary\">\n" +
                    "              <span>Edit</span>\n" +
                    "            </Button>",
                entityClass
        );

        writeFile(basePath + entityFile + "-detail.tsx", content);
    }

    private void writeEntityUpdateComponent(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        StringBuilder formFields = new StringBuilder();
        for (FieldConfig field : entity.getFields()) {
            String inputType = getInputType(field);
            formFields.append("              <FormGroup>\n");
            formFields.append("                <Label for=\"").append(entityInstance).append("-").append(field.getFieldName()).append("\">").append(capitalize(field.getFieldName())).append("</Label>\n");
            formFields.append("                <Input\n");
            formFields.append("                  id=\"").append(entityInstance).append("-").append(field.getFieldName()).append("\"\n");
            formFields.append("                  type=\"").append(inputType).append("\"\n");
            formFields.append("                  {...register('").append(field.getFieldName()).append("'");
            if (field.getRequired()) {
                formFields.append(", { required: true }");
            }
            formFields.append(")}\n");
            formFields.append("                />\n");
            formFields.append("              </FormGroup>\n");
        }

        String content = """
            import React, { useEffect } from 'react';
            import { Link, useNavigate, useParams } from 'react-router-dom';
            import { Button, Row, Col, Form, FormGroup, Label, Input } from 'reactstrap';
            import { useForm } from 'react-hook-form';
            import { useAppDispatch, useAppSelector } from 'app/config/store';
            import { getEntity, createEntity, updateEntity, reset } from './%s.reducer';
            import { I%s } from 'app/shared/model/%s.model';
            %s

            const %sUpdate = () => {
              const dispatch = useAppDispatch();
              const navigate = useNavigate();
              const { id } = useParams<'id'>();
              const isNew = id === undefined;

              const %sEntity = useAppSelector(state => state.%s.entity);
              const loading = useAppSelector(state => state.%s.loading);
              const updating = useAppSelector(state => state.%s.updating);
              const updateSuccess = useAppSelector(state => state.%s.updateSuccess);

              const { register, handleSubmit, reset: resetForm, formState: { errors } } = useForm<I%s>();

              useEffect(() => {
                if (isNew) {
                  dispatch(reset());
                } else {
                  dispatch(getEntity(id));
                }
              }, [dispatch, id, isNew]);

              useEffect(() => {
                if (!isNew && %sEntity.id) {
                  resetForm(%sEntity);
                }
              }, [%sEntity, isNew, resetForm]);

              useEffect(() => {
                if (updateSuccess) {
                  navigate('/%s');
                }
              }, [navigate, updateSuccess]);

              const saveEntity = (data: I%s) => {
                if (isNew) {
                  dispatch(createEntity(data));
                } else {
                  dispatch(updateEntity({ ...data, id: Number(id) }));
                }
              };

              return (
                <Row className="justify-content-center">
                  <Col md="8">
                    <h2>
                      {isNew ? 'Create' : 'Edit'} %s
                    </h2>
                    {loading ? (
                      <div className="spinner-border" role="status" />
                    ) : (
                      <Form onSubmit={handleSubmit(saveEntity)}>
            %s
                        <Button tag={Link} to="/%s" replace color="info">
                          <span>Back</span>
                        </Button>
                        &nbsp;
                        <Button color="primary" type="submit" disabled={updating}>
                          Save
                        </Button>
                      </Form>
                    )}
                  </Col>
                </Row>
              );
            };

            export default %sUpdate;
            """.formatted(
                entityFile,
                entityClass, entityFile,
                getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                entityClass,
                entityInstance, entityInstance, entityInstance, entityInstance, entityInstance,
                entityClass,
                entityInstance, entityInstance,
                entityInstance,
                entityFile,
                entityClass,
                entityClass,
                formFields.toString(),
                entityFile,
                entityClass
        );

        writeFile(basePath + entityFile + "-update.tsx", content);
    }

    private void writeEntityDeleteDialog(EntityConfig entity, String basePath) throws IOException {
        String entityClass = entity.getEntityReactName();
        String entityFile = entity.getEntityFileName();
        String entityInstance = uncapitalize(entityClass);

        String content = """
            import React, { useEffect } from 'react';
            import { useNavigate, useParams } from 'react-router-dom';
            import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
            import { useAppDispatch, useAppSelector } from 'app/config/store';
            import { getEntity, deleteEntity } from './%s.reducer';
            %s

            const %sDeleteDialog = () => {
              const dispatch = useAppDispatch();
              const navigate = useNavigate();
              const { id } = useParams<'id'>();

              const %sEntity = useAppSelector(state => state.%s.entity);
              const updateSuccess = useAppSelector(state => state.%s.updateSuccess);

              useEffect(() => {
                dispatch(getEntity(id!));
              }, [dispatch, id]);

              useEffect(() => {
                if (updateSuccess) {
                  navigate('/%s');
                }
              }, [navigate, updateSuccess]);

              const confirmDelete = () => {
                dispatch(deleteEntity(%sEntity.id!));
              };

              return (
                <Modal isOpen toggle={() => navigate('/%s')}>
                  <ModalHeader toggle={() => navigate('/%s')}>
                    Confirm delete operation
                  </ModalHeader>
                  <ModalBody>
                    Are you sure you want to delete %s {%sEntity.id}?
                  </ModalBody>
                  <ModalFooter>
                    <Button color="secondary" onClick={() => navigate('/%s')}>
                      Cancel
                    </Button>
                    <Button color="danger" onClick={confirmDelete}>
                      Delete
                    </Button>
                  </ModalFooter>
                </Modal>
              );
            };

            export default %sDeleteDialog;
            """.formatted(
                entityFile,
                getConfig().getEnableTranslation() ? "import { Translate } from 'react-jhipster';" : "",
                entityClass,
                entityInstance, entityInstance, entityInstance,
                entityFile,
                entityInstance,
                entityFile,
                entityFile,
                entityClass, entityInstance,
                entityFile,
                entityClass
        );

        writeFile(basePath + entityFile + "-delete-dialog.tsx", content);
    }

    private void writeEntitiesIndex() throws IOException {
        StringBuilder imports = new StringBuilder();
        StringBuilder reducerImports = new StringBuilder();
        StringBuilder routes = new StringBuilder();
        StringBuilder reducers = new StringBuilder();

        for (EntityConfig entity : getConfig().getEntities()) {
            if (!entity.getBuiltIn() && !entity.getEmbedded()) {
                String entityClass = entity.getEntityReactName();
                String entityFile = entity.getEntityFileName();
                String entityInstance = uncapitalize(entityClass);

                imports.append("import ").append(entityClass).append(" from './").append(entity.getEntityFolderName()).append("';\n");
                reducerImports.append("import ").append(entityInstance).append(" from './").append(entity.getEntityFolderName()).append("/").append(entityFile).append(".reducer';\n");
                routes.append("    <Route path=\"").append(entityFile).append("/*\" element={<").append(entityClass).append(" />} />\n");
                reducers.append("  ").append(entityInstance).append(",\n");
            }
        }

        // Entity routes index
        String entitiesIndex = """
            import React from 'react';
            import { Route, Routes } from 'react-router-dom';
            import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

            %s
            const Entities = () => (
              <ErrorBoundaryRoutes>
            %s    {/* jhipster-needle-add-route-path - JHipster will add entity routes here */}
              </ErrorBoundaryRoutes>
            );

            export default Entities;
            """.formatted(imports.toString(), routes.toString());
        writeFile(getConfig().getClientWebappDir() + "entities/index.tsx", entitiesIndex);

        // Entity reducers
        String reducersFile = """
            // jhipster-needle-add-reducer-import - JHipster will add reducer here
            %s
            const entitiesReducers = {
            %s  // jhipster-needle-add-reducer-combine - JHipster will add reducer here
            };

            export default entitiesReducers;
            """.formatted(reducerImports.toString(), reducers.toString());
        writeFile(getConfig().getClientWebappDir() + "entities/reducers.ts", reducersFile);
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
        log.info("Post-writing React configuration");

        // Write error boundary and shared components
        try {
            writeErrorBoundary();
        } catch (IOException e) {
            log.error("Error writing shared components", e);
        }
    }

    private void writeErrorBoundary() throws IOException {
        String errorBoundary = """
            import React from 'react';

            interface IErrorBoundaryProps {
              children: React.ReactNode;
            }

            interface IErrorBoundaryState {
              hasError: boolean;
              error?: Error;
            }

            class ErrorBoundary extends React.Component<IErrorBoundaryProps, IErrorBoundaryState> {
              constructor(props: IErrorBoundaryProps) {
                super(props);
                this.state = { hasError: false };
              }

              static getDerivedStateFromError(error: Error) {
                return { hasError: true, error };
              }

              componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
                console.error('Error caught by boundary:', error, errorInfo);
              }

              render() {
                if (this.state.hasError) {
                  return (
                    <div className="alert alert-danger">
                      <h2>An unexpected error has occurred.</h2>
                      <p>{this.state.error?.message}</p>
                    </div>
                  );
                }
                return this.props.children;
              }
            }

            export default ErrorBoundary;
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/error/error-boundary.tsx", errorBoundary);

        String errorBoundaryRoutes = """
            import React from 'react';
            import { Routes, Route } from 'react-router-dom';
            import ErrorBoundary from './error-boundary';

            interface IErrorBoundaryRoutesProps {
              children: React.ReactNode;
            }

            const ErrorBoundaryRoutes = ({ children }: IErrorBoundaryRoutesProps) => (
              <ErrorBoundary>
                <Routes>{children}</Routes>
              </ErrorBoundary>
            );

            export default ErrorBoundaryRoutes;
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/error/error-boundary-routes.tsx", errorBoundaryRoutes);

        String pageNotFound = """
            import React from 'react';
            import { Alert } from 'reactstrap';

            const PageNotFound = () => (
              <Alert color="danger">
                The page does not exist.
              </Alert>
            );

            export default PageNotFound;
            """;
        writeFile(getConfig().getClientWebappDir() + "shared/error/page-not-found.tsx", pageNotFound);
    }

    private void end() {
        log.info("React generation completed!");
        log.info("");
        log.info("==========================================================");
        log.info("React application generated successfully!");
        log.info("==========================================================");
    }
}
