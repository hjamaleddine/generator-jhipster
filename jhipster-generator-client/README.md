# JHipster Client Generator (Java)

Port Java des générateurs frontend JHipster (Angular, React, Vue).

## Structure du Projet

```
jhipster-generator-client/
├── src/main/java/io/github/jhipster/client/
│   ├── config/
│   │   ├── ClientConfig.java           # Configuration client
│   │   └── ClientGeneratorContext.java # Contexte de génération
│   ├── core/
│   │   └── BaseClientGenerator.java    # Classe de base des générateurs
│   ├── generators/
│   │   ├── client/
│   │   │   └── ClientGenerator.java    # Orchestrateur principal
│   │   ├── angular/
│   │   │   └── AngularGenerator.java   # Générateur Angular
│   │   ├── react/
│   │   │   └── ReactGenerator.java     # Générateur React
│   │   ├── vue/
│   │   │   └── VueGenerator.java       # Générateur Vue
│   │   └── cypress/
│   │       └── CypressGenerator.java   # Générateur tests E2E
│   └── utils/
│       └── NeedleService.java          # Service d'injection needle
└── pom.xml
```

## Générateurs Disponibles

### ClientGenerator (Orchestrateur)
Point d'entrée principal qui compose avec les générateurs spécifiques.

```java
ClientGenerator generator = new ClientGenerator(context);
generator.generate();
```

### AngularGenerator
Génère une application Angular 17+ avec:
- Standalone components
- Routage avec lazy loading
- Services HTTP avec HttpClient
- NgBootstrap pour UI
- i18n avec ngx-translate

### ReactGenerator
Génère une application React 18+ avec:
- Redux Toolkit pour state management
- React Router 6
- Axios pour HTTP
- Reactstrap pour UI
- react-i18next pour i18n

### VueGenerator
Génère une application Vue 3+ avec:
- Pinia pour state management
- Vue Router 4
- Axios pour HTTP
- Bootstrap Vue 3 pour UI
- vue-i18n pour i18n

### CypressGenerator
Génère des tests E2E avec Cypress:
- Tests page d'accueil
- Tests login/logout
- Tests admin
- Tests CRUD par entité

## Configuration

### ClientConfig

| Option | Type | Description |
|--------|------|-------------|
| `clientFramework` | string | `angular`, `react`, `vue`, `no` |
| `clientBundler` | string | `webpack`, `vite`, `esbuild` |
| `clientTheme` | string | Thème Bootswatch |
| `clientTestFramework` | string | `jest`, `vitest` |
| `enableTranslation` | boolean | Support i18n |
| `devServerPort` | number | Port serveur dev |

## Utilisation

```java
// Charger la configuration depuis .yo-rc.json
ClientGeneratorContext context = ClientGeneratorContext.fromYoRc(projectPath);

// Créer et exécuter le générateur client
ClientGenerator generator = new ClientGenerator(context);
generator.generate();
```

## Cycle de Vie des Générateurs

```
INITIALIZING → PROMPTING → CONFIGURING → COMPOSING → LOADING →
PREPARING → PREPARING_EACH_ENTITY → DEFAULT → WRITING →
WRITING_ENTITIES → POST_WRITING → POST_WRITING_ENTITIES → END
```

## Système Needle

Le système needle permet l'injection non-destructive de code:

```java
NeedleService needleService = new NeedleService(basePath);

// Ajouter une route entité
needleService.addBeforeNeedle(
    "src/main/webapp/app/entities/entity.routes.ts",
    "jhipster-needle-add-entity-route",
    routeContent
);
```

### Needles Disponibles

| Needle | Description |
|--------|-------------|
| `jhipster-needle-add-entity-route` | Routes entités |
| `jhipster-needle-add-entity-to-menu` | Menu entités |
| `jhipster-needle-add-icon-import` | Import Font Awesome |
| `jhipster-needle-add-reducer-import` | Import reducer React |
| `jhipster-needle-add-reducer-combine` | Combine reducers |
| `jhipster-needle-add-webpack-config` | Config Webpack |
| `jhipster-needle-add-scss-style` | Styles SCSS |

## Fichiers Générés par Framework

### Angular
```
src/main/webapp/app/
├── app.component.ts
├── app.config.ts
├── app.routes.ts
├── main.ts
├── entities/
│   └── {entity}/
│       ├── {entity}.model.ts
│       ├── service/{entity}.service.ts
│       ├── {entity}.routes.ts
│       ├── list/{entity}.component.ts
│       ├── detail/{entity}-detail.component.ts
│       ├── update/{entity}-update.component.ts
│       └── delete/{entity}-delete-dialog.component.ts
├── layouts/
├── core/
├── shared/
├── admin/
└── account/
```

### React
```
src/main/webapp/app/
├── app.tsx
├── index.tsx
├── routes.tsx
├── config/store.ts
├── entities/
│   └── {entity}/
│       ├── {entity}.tsx
│       ├── {entity}-detail.tsx
│       ├── {entity}-update.tsx
│       ├── {entity}-delete-dialog.tsx
│       └── {entity}.reducer.ts
├── shared/
│   ├── model/{entity}.model.ts
│   ├── reducers/
│   └── layout/
├── modules/
│   ├── home/
│   ├── login/
│   ├── account/
│   └── administration/
```

### Vue
```
src/main/webapp/app/
├── app.vue
├── main.ts
├── router/
│   ├── index.ts
│   └── entities.ts
├── entities/
│   └── {entity}/
│       ├── {entity}.vue
│       ├── {entity}-details.vue
│       ├── {entity}-update.vue
│       └── {entity}.service.ts
├── shared/
│   ├── model/{entity}.model.ts
│   └── config/store/
├── core/
├── account/
└── admin/
```

## Dépendances

- Java 17+
- Mustache (templates)
- Jackson (JSON/YAML)
- Commons IO/Lang
- SLF4J + Logback

## Build

```bash
mvn clean package
```

## Tests

```bash
mvn test
```

---

**Date:** Janvier 2025
**Version:** 1.0.0-SNAPSHOT
