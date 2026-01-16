# Flux de Génération Frontend JHipster

## Vue d'Ensemble

Le frontend JHipster utilise un système de composition de générateurs où le `ClientGenerator` orchestre les générateurs spécifiques aux frameworks (Angular, React, Vue).

```
BootstrapApplication
└── CommonGenerator
    └── ClientGenerator (Orchestrateur)
        ├── AngularGenerator
        ├── ReactGenerator
        ├── VueGenerator
        └── CypressGenerator (tests E2E)
```

---

## 1. Client Generator - Orchestrateur Principal

**Fichier:** `generators/client/generator.ts`

### Structure
- Étend `BaseApplicationGenerator`
- Point d'entrée pour la génération client
- Compose avec le générateur de framework sélectionné

### Cycle de Vie

```
beforeQueue → prompting → configuring → composing → loading → preparing → writing → postWriting
```

### Options de Configuration

| Option | Type | Valeurs | Description |
|--------|------|---------|-------------|
| `clientFramework` | string | `angular`, `react`, `vue`, `no` | Framework frontend |
| `clientTheme` | string | `none`, `cerulean`, `cosmo`, etc. | Thème Bootswatch |
| `clientThemeVariant` | string | `primary`, `dark`, `light` | Variante navbar |
| `clientBundler` | string | `webpack`, `vite`, `esbuild` | Outil de build |
| `clientTestFramework` | string | `jest`, `vitest` | Framework de test |
| `devServerPort` | number | `9000 + index` | Port serveur dev |
| `enableI18nRTL` | boolean | true/false | Support RTL |

### Fichiers Générés (Communs)

```
src/main/webapp/
├── manifest.webapp
├── content/
│   ├── css/loading.css
│   └── images/ (favicon, SVG, PNG)
├── WEB-INF/web.xml
├── swagger-ui/index.html
└── .prettierignore.jhi.client
```

### Logique de Composition

```typescript
// Dans ClientGenerator.composing()
switch(clientFramework) {
  case 'angular':
    composeWith(AngularGenerator);
    break;
  case 'react':
    composeWith(ReactGenerator);
    break;
  case 'vue':
    composeWith(VueGenerator);
    break;
}

if (testFrameworks.includes('cypress')) {
  composeWith(CypressGenerator);
}
```

---

## 2. Angular Generator

**Fichier:** `generators/angular/generator.ts`

### Cycle de Vie

| Phase | Actions |
|-------|---------|
| `beforeQueue` | Dépend de GENERATOR_CLIENT, GENERATOR_LANGUAGES |
| `loading` | Charge package.json Angular |
| `preparing` | Configure needles pour entités/admin |
| `preparingEachEntity` | Prépare propriétés par entité |
| `preparingEachEntityField` | Convertit types (dayjs, enums) |
| `default` | Configure transformations traduction |
| `writing` | Écrit fichiers core, nettoie anciennes versions |
| `writingEntities` | Génère composants/services entités |
| `postWriting` | Merge package.json (bundler-specific) |
| `postWritingEntities` | Enregistre entités dans routes |

### Fichiers Entité Générés

```
src/main/webapp/app/entities/{entity}/
├── {entity}.model.ts                    # Interface TypeScript
├── {entity}.test-samples.ts             # Données de test
├── service/
│   ├── {entity}.service.ts              # Service HTTP
│   └── {entity}.service.spec.ts
├── {entity}.routes.ts                   # Routes
├── list/
│   ├── {entity}.component.html
│   ├── {entity}.component.ts
│   └── {entity}.component.spec.ts
├── detail/
│   ├── {entity}-detail.component.html
│   ├── {entity}-detail.component.ts
│   └── {entity}-detail.component.spec.ts
├── update/                              # Si !readOnly
│   ├── {entity}-update.component.html
│   ├── {entity}-update.component.ts
│   ├── {entity}-update.component.spec.ts
│   └── {entity}-form.service.ts
├── delete/                              # Si !readOnly
│   ├── {entity}-delete-dialog.component.html
│   ├── {entity}-delete-dialog.component.ts
│   └── {entity}-delete-dialog.component.spec.ts
└── route/
    ├── {entity}-routing-resolve.service.ts
    └── {entity}-routing-resolve.service.spec.ts
```

### Structure Application Core

```
src/main/webapp/app/
├── app.config.ts
├── app.routes.ts
├── app.component.ts
├── main.ts
├── app-page-title-strategy.ts
├── layouts/
│   ├── navbar/
│   ├── footer/
│   ├── error/
│   └── profiles/
├── admin/
│   ├── docs/
│   ├── configuration/
│   ├── health/
│   ├── logs/
│   ├── metrics/
│   ├── tracker/ (WebSocket)
│   └── gateway/ (si gateway)
├── account/
│   ├── settings/
│   ├── password/
│   ├── register/
│   ├── activate/
│   └── sessions/ (si session auth)
├── core/
│   └── services/ (auth, alert, HTTP interceptors)
└── shared/
    ├── pipes/
    ├── directives/
    ├── filters/
    └── pagination/
```

### Configuration Bundle

**Webpack:**
```
├── angular.json
├── webpack/
│   ├── webpack.custom.js
│   ├── proxy.conf.js
│   └── environment.js
```

**ESBuild (expérimental):**
```
├── angular.json.esbuild
├── proxy.config.mjs
├── build-plugins/
│   ├── define-esbuild.mjs
│   └── i18n-esbuild.mjs
└── i18n/index.ts
```

### Points d'Injection (Needles)

```typescript
source.addEntitiesToClient()      // Routes & menu entités
source.addAdminRoute()            // Routes admin
source.addItemToAdminMenu()       // Menu admin
source.addIconImport()            // Font Awesome
source.addWebpackConfig()         // Config webpack
source.addLanguagesInFrontend()   // Langues i18n
source.addExternalResourceToRoot()// Resources index.html
```

---

## 3. React Generator

**Fichier:** `generators/react/generator.ts`

### Cycle de Vie

| Phase | Actions |
|-------|---------|
| `beforeQueue` | Dépend de javascript:bootstrap, GENERATOR_CLIENT, GENERATOR_LANGUAGES |
| `composing` | Compose avec 'jhipster:client:common' |
| `loading` | Charge package.json React |
| `preparing` | Configure injection webpack/SCSS, callbacks entités |
| `preparingEachEntity` | Préparation entité custom |
| `preparingEachEntityField` | Règles validation champs |
| `default` | Queue transformations traduction |
| `writing` | Écrit fichiers core |
| `writingEntities` | Génère composants & reducers |
| `postWriting` | Gère dépendances (bundler-specific) |

### Fichiers Entité Générés

```
src/main/webapp/app/entities/{entity}/
├── {entity}.tsx                  # Composant Liste
├── {entity}-detail.tsx           # Composant Détail
├── {entity}.reducer.ts           # Redux reducer
├── index.tsx                     # Export
├── {entity}-delete-dialog.tsx    # Dialog suppression
└── {entity}-update.tsx           # Formulaire update

src/main/webapp/app/shared/model/
└── {entity}.model.ts             # Interface TypeScript
```

### Structure Application Core

```
src/main/webapp/app/
├── app.tsx                       # Root component
├── index.tsx                     # Entry point
├── routes.tsx                    # Configuration routes
├── setup-tests.ts
├── typings.d.ts
├── config/
│   ├── constants.ts
│   ├── dayjs.ts
│   ├── axios-interceptor.ts
│   ├── error-middleware.ts
│   ├── logger-middleware.ts
│   ├── notification-middleware.ts
│   ├── store.ts                  # Redux store
│   └── icon-loader.ts
├── modules/
│   ├── home/
│   └── login/
├── shared/
│   ├── reducers/
│   │   ├── index.ts
│   │   ├── reducer-utils.ts
│   │   ├── authentication.ts
│   │   ├── application-profile.ts
│   │   ├── locale.ts             # Si i18n
│   │   └── user-management.ts    # Si OAuth2
│   └── model/
├── account/
│   ├── activate/
│   ├── password/
│   ├── register/
│   ├── password-reset/
│   ├── settings/
│   └── sessions/                 # Si session auth
├── admin/
│   ├── user-management/
│   ├── health/
│   ├── logs/
│   ├── docs/
│   ├── metrics/
│   ├── configuration/
│   ├── tracker/
│   └── gateway/
└── entities/
    ├── reducers.ts
    ├── menu.tsx
    └── routes.tsx
```

### Configuration Bundle (Webpack)

```
├── webpack/
│   ├── webpack.common.js
│   ├── webpack.dev.js
│   ├── webpack.prod.js
│   ├── environment.js
│   └── utils.js
├── postcss.config.js
├── tsconfig.json
└── tsconfig.test.json
```

### Dépendances Principales

| Package | Rôle |
|---------|------|
| `react-redux` | State management |
| `redux` | Store |
| `axios` | HTTP client |
| `react-router-dom` | Routing |
| `react-i18next` | Internationalisation |
| `dayjs` | Dates |
| Font Awesome 6 | Icônes |

### Points d'Injection

```typescript
source.addWebpackConfig()         // Config webpack
source.addClientStyle()           // SCSS
source.addEntitiesToClient()      // Routes & reducers
source.mergeClientPackageJson()   // Dépendances
```

---

## 4. Vue Generator

**Fichier:** `generators/vue/generator.ts`

### Cycle de Vie

| Phase | Actions |
|-------|---------|
| `configuring` | Migrations version pour devServerPort |
| `loading` | Charge package.json Vue |
| `preparing` | Configure injection webpack/vite, callbacks routing |
| `default` | Queue transformations (Vue-to-i18n) |
| `writing` | Écrit fichiers core |
| `writingEntities` | Génère composants & services |
| `postWriting` | Scripts package.json, dépendances microfrontend |

### Fichiers Entité Générés

```
src/main/webapp/app/
├── shared/model/{entity}.model.ts
└── entities/{entity}/
    ├── {entity}.vue                    # Vue Liste
    ├── {entity}.component.ts
    ├── {entity}.component.spec.ts
    ├── {entity}-details.vue            # Vue Détail
    ├── {entity}-details.component.ts
    ├── {entity}-details.component.spec.ts
    ├── {entity}.service.ts             # Service HTTP
    ├── {entity}.service.spec.ts
    ├── {entity}-update.vue             # Si !readOnly
    ├── {entity}-update.component.ts
    └── {entity}-update.component.spec.ts
```

### Structure Application Core

```
src/main/webapp/app/
├── app.vue                       # Root component
├── app.component.ts
├── main.ts                       # Bootstrap
├── store.ts                      # Pinia/Vuex
├── constants.ts
├── router/
│   ├── index.ts
│   ├── admin.ts
│   ├── pages.ts
│   └── entities.ts               # Lazy loading
├── shared/
│   ├── alert/
│   ├── config/
│   │   ├── bootstrap-vue.ts
│   │   ├── dayjs.ts
│   │   └── languages.ts
│   ├── account-store.ts
│   └── translation-store.ts
├── core/
│   ├── home/
│   ├── error/
│   ├── navbar/
│   ├── footer/
│   └── ribbon/
├── account/
│   ├── login-form/
│   ├── register/
│   ├── activate/
│   ├── password-reset/
│   ├── settings/
│   └── sessions/
├── admin/
│   ├── user-management/
│   ├── health/
│   ├── logs/
│   ├── docs/
│   ├── metrics/
│   ├── configuration/
│   ├── tracker/
│   └── gateway/
├── composables/
│   ├── date-format.ts
│   └── validation.ts
└── computables/
    └── array.ts
```

### Configuration Bundle

**Vite (Moderne):**
```
├── vite.config.mts
├── vitest.config.mts
├── .postcssrc.js
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── tsconfig.vitest.json
└── module-federation.config.cjs    # Si microfrontend
```

**Webpack (Legacy):**
```
├── webpack/
│   ├── webpack.common.js
│   ├── webpack.dev.js
│   ├── webpack.prod.js
│   ├── config.js
│   └── vue.utils.js
```

### Dépendances Principales

| Package | Rôle |
|---------|------|
| `vue@3.x` | Framework |
| `vue-router@4.x` | Routing |
| `pinia` | State (Composition API) |
| `axios` | HTTP client |
| `vue-i18n` | Internationalisation |
| Bootstrap Vue 3 | Composants UI |
| `dayjs` | Dates |

### Points d'Injection

```typescript
source.addWebpackConfig()         // Config webpack (si webpack)
source.addEntitiesToClient()      // Routes (router/entities.ts)
source.mergeClientPackageJson()   // Dépendances
source.addExternalResourceToRoot()// Resources index.html
```

---

## 5. Flux de Génération des Entités

### Pipeline de Traitement

```
1. Filtrage
   ├── filterEntitiesForClient()           # Retire entités builtIn
   ├── filterEntitiesAndPropertiesForClient()
   └── filterEntityPropertiesForClient()   # Par entité

2. Préparation Entité
   ├── Angular: Autorités → format Angular, imports enum
   ├── React: Préparation custom, règles validation
   └── Vue: Pattern Vue-specific

3. Préparation Champs
   ├── Valeurs par défaut → TypeScript (dayjs pour dates)
   ├── Validation → regex framework-specific
   ├── Relations → eager-load avec Pick types
   └── Enums → imports selon conventions

4. Génération Templates
   Pour chaque entité (!builtInUser, !embedded):
   ├── Fichier model/interface
   ├── Service HTTP (Angular/Vue) ou Reducer (React)
   ├── Composant liste/table
   ├── Composant détail/view
   ├── Formulaire update/edit (si !readOnly)
   ├── Dialog suppression (si !readOnly)
   └── Fichiers test (spec)

5. Enregistrement Post-Entité
   ├── Angular: addEntitiesToClient() → routes, menu
   ├── React: Injection imports, reducers, routes
   └── Vue: addEntitiesToClient() → router, services, menu
```

### Template Modèle Entité (Exemple Angular)

```ejs
<%_ if (anyFieldIsDateDerived) { _%>
import dayjs from 'dayjs/esm';
<%_ } _%>

<%_ for (const relationship of differentRelationships) { _%>
import { I<%- otherEntity.entityAngularName %> } from '...';
<%_ } _%>

export interface I<%= entityAngularName %> {
  <%_ for (const field of fields) { _%>
  <%= fieldName %><%- !id && !required ? '?' : '' %>: <%= tsType %>;
  <%_ } _%>

  <%_ for (const relationship of relationships) { _%>
  <%= propertyName %>?: I<%= otherEntity.entityAngularName %><%= collection ? '[]' : '' %> | null;
  <%_ } _%>
}

<%_ if (!readOnly && primaryKey) { _%>
export type New<%= entityAngularName %> = Omit<I<%= entityAngularName %>, '<%= primaryKey.name %>'> & { <%= primaryKey.name %>: null };
<%_ } _%>
```

### Variables Template Clés

| Variable | Description |
|----------|-------------|
| `entityAngularName` | Nom entité CamelCase |
| `entityFolderName` | Nom dossier kebab-case |
| `entityFileName` | Nom fichier kebab-case |
| `fields` | Array définitions champs |
| `relationships` | Array définitions relations |
| `anyFieldIsDateDerived` | Flag pour import dayjs |
| `readOnly` | Entité lecture seule |
| `primaryKey` | Définition clé primaire |

---

## 6. Gestion CSS/SCSS

### Par Framework

**Angular:**
```
src/main/webapp/content/scss/
├── global.scss
├── _bootstrap-variables.scss
├── vendor.scss
└── layouts/ (composant-specific)
```

**React:**
```
src/main/webapp/app/
├── app.scss
├── _bootstrap-variables.scss
└── modules/home/home.scss
```

**Vue:**
```
src/main/webapp/content/scss/
├── global.scss
├── _bootstrap-variables.scss
└── vendor.scss

+ Styles scoped dans <style scoped>
```

### Intégration Bootstrap

- Bootstrap 5 pour Angular/React
- Bootstrap Vue 3 pour Vue
- Thèmes Bootswatch via CDN ou npm

---

## 7. Configuration Tests

### Angular (Jest)

```typescript
// jest.conf.js
// Fichiers: *.spec.ts colocated avec sources
Couverture: >80% target
```

### React (Jest)

```typescript
// jest.conf.js
// setup-tests.ts
// Fichiers: *.spec.ts, *-reducer.spec.ts
```

### Vue (Vitest/Jest)

```typescript
// Vite: vitest.config.mts
// Webpack: jest.conf.js
// Fichiers: *.spec.ts colocated
```

### Cypress (E2E)

```
cypress/
├── cypress.config.ts
├── e2e/
│   └── *.cy.ts
├── support/
└── fixtures/
```

---

## 8. Pattern Needle

Le système Needle permet des modifications non-destructives via des marqueurs:

```typescript
// jhipster-needle-add-entity-route
// jhipster-needle-add-entity-to-menu
// jhipster-needle-add-icon-import
// jhipster-needle-add-webpack-config
```

### Exemple d'utilisation

```typescript
// Avant injection
export const entityRoutes = [
  // jhipster-needle-add-entity-route
];

// Après injection de "Product"
export const entityRoutes = [
  productRoute,
  // jhipster-needle-add-entity-route
];
```

---

## 9. Microfrontend Support

### Module Federation

**Angular:** `@module-federation/enhanced`

**React:**
- Gateway: `@module-federation/enhanced`
- Apps: `@module-federation/utilities`

**Vue:**
- Vite: `@originjs/vite-plugin-federation@1.3.6`
- Webpack: `@module-federation/enhanced`

### Configuration

```javascript
// module-federation.config.cjs
module.exports = {
  name: 'appName',
  filename: 'remoteEntry.js',
  exposes: {
    './Module': './src/main/webapp/app/entities/entities.module.ts'
  },
  shared: ['@angular/core', '@angular/router', ...]
};
```

---

## 10. Résumé Comparatif

| Aspect | Angular | React | Vue |
|--------|---------|-------|-----|
| **Bundler** | Webpack/ESBuild | Webpack | Webpack/Vite |
| **State** | Services/RxJS | Redux | Pinia/Vuex |
| **HTTP** | HttpClient | Axios | Axios |
| **Routing** | @angular/router | react-router-dom | vue-router |
| **i18n** | @ngx-translate | react-i18next | vue-i18n |
| **Tests Unit** | Jest | Jest | Jest/Vitest |
| **Tests E2E** | Cypress | Cypress | Cypress |
| **UI** | Bootstrap 5 | Bootstrap 5 | Bootstrap Vue 3 |

---

## 11. Prochaines Étapes pour Migration Java

Pour migrer les générateurs frontend en Java, il faudra:

1. **ClientGenerator Java** - Orchestrateur principal
2. **AngularGenerator Java** - Templates Angular
3. **ReactGenerator Java** - Templates React avec Redux
4. **VueGenerator Java** - Templates Vue avec Pinia
5. **Système de Templates** - Port du système EJS vers Java (Mustache/Freemarker)
6. **Système Needle** - Implémentation des points d'injection

### Priorité

Pour les microservices backend-only (`skipClient=true`), ces générateurs ne sont **pas requis**. Ils sont uniquement nécessaires pour les applications monolithiques avec frontend intégré.

---

**Date:** Janvier 2025
**Version:** 1.0.0
