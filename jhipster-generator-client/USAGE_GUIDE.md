# Guide d'Utilisation - JHipster Client Generator (Java)

## Table des Matières

1. [Prérequis](#prérequis)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Utilisation Programmatique](#utilisation-programmatique)
5. [Utilisation en Ligne de Commande](#utilisation-en-ligne-de-commande)
6. [Exemples Complets](#exemples-complets)
7. [Tests](#tests)
8. [Dépannage](#dépannage)

---

## Prérequis

### Logiciels Requis

| Logiciel | Version | Vérification |
|----------|---------|--------------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| npm | 9+ | `npm -v` |

### Installation Java (si nécessaire)

```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# macOS (Homebrew)
brew install openjdk@17

# Windows (Chocolatey)
choco install openjdk17
```

---

## Installation

### 1. Cloner et Builder le Projet

```bash
# Cloner le repository
git clone <repository-url>
cd generator-jhipster/jhipster-generator-client

# Builder le projet
mvn clean package -DskipTests

# Vérifier le build
ls target/*.jar
```

### 2. Installer les Dépendances Maven

```bash
mvn dependency:resolve
```

### 3. Vérifier l'Installation

```bash
# Exécuter les tests
mvn test

# Ou exécuter directement
java -jar target/jhipster-generator-client-1.0.0-SNAPSHOT.jar --help
```

---

## Configuration

### Structure du Fichier .yo-rc.json

Le générateur lit sa configuration depuis `.yo-rc.json`:

```json
{
  "generator-jhipster": {
    "baseName": "myApp",
    "packageName": "com.mycompany.myapp",
    "clientFramework": "angular",
    "clientBundler": "webpack",
    "clientTheme": "none",
    "clientThemeVariant": "primary",
    "clientTestFramework": "jest",
    "devServerPort": 9000,
    "enableTranslation": true,
    "nativeLanguage": "en",
    "languages": ["en", "fr"],
    "authenticationType": "jwt",
    "applicationType": "monolith",
    "testFrameworks": ["cypress"],
    "entities": []
  }
}
```

### Options de Configuration

#### clientFramework

| Valeur | Description |
|--------|-------------|
| `angular` | Angular 17+ avec standalone components |
| `react` | React 18+ avec Redux Toolkit |
| `vue` | Vue 3+ avec Pinia |
| `no` | Pas de frontend (API only) |

#### clientBundler

| Valeur | Framework | Description |
|--------|-----------|-------------|
| `webpack` | Tous | Bundler standard |
| `vite` | Vue | Build rapide avec HMR |
| `esbuild` | Angular | Expérimental, très rapide |

#### authenticationType

| Valeur | Description |
|--------|-------------|
| `jwt` | JSON Web Token |
| `oauth2` | OAuth2/OIDC (Keycloak, Okta) |
| `session` | Session HTTP classique |

---

## Utilisation Programmatique

### Exemple Basique

```java
import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.generators.client.ClientGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateClient {
    public static void main(String[] args) throws Exception {
        // 1. Définir le chemin du projet
        Path projectPath = Paths.get("/path/to/my-jhipster-app");

        // 2. Charger la configuration depuis .yo-rc.json
        ClientGeneratorContext context = ClientGeneratorContext.fromYoRc(projectPath);

        // 3. Créer et exécuter le générateur
        ClientGenerator generator = new ClientGenerator(context);
        generator.generate();

        System.out.println("Frontend generated successfully!");
    }
}
```

### Configuration Programmatique (sans .yo-rc.json)

```java
import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientGeneratorContext;
import io.github.jhipster.client.generators.client.ClientGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateClientProgrammatic {
    public static void main(String[] args) throws Exception {
        // 1. Créer la configuration manuellement
        ClientConfig config = new ClientConfig();
        config.setBaseName("myApp");
        config.setPackageName("com.mycompany.myapp");
        config.setClientFramework("react");
        config.setClientBundler("webpack");
        config.setEnableTranslation(true);
        config.setNativeLanguage("en");
        config.setAuthenticationType("jwt");
        config.setDevServerPort(9000);

        // 2. Créer le contexte
        Path projectPath = Paths.get("/path/to/my-app");
        ClientGeneratorContext context = new ClientGeneratorContext(projectPath, config);

        // 3. Générer
        ClientGenerator generator = new ClientGenerator(context);
        generator.generate();
    }
}
```

### Ajouter des Entités

```java
import io.github.jhipster.client.config.ClientConfig;
import io.github.jhipster.client.config.ClientConfig.EntityConfig;
import io.github.jhipster.client.config.ClientConfig.FieldConfig;

// Créer une entité
EntityConfig product = new EntityConfig();
product.setName("Product");
product.setEntityAngularName("Product");
product.setEntityFileName("product");
product.setEntityFolderName("product");
product.setReadOnly(false);

// Ajouter des champs
FieldConfig nameField = new FieldConfig();
nameField.setFieldName("name");
nameField.setFieldType("String");
nameField.setRequired(true);

FieldConfig priceField = new FieldConfig();
priceField.setFieldName("price");
priceField.setFieldType("BigDecimal");
priceField.setRequired(true);

FieldConfig descField = new FieldConfig();
descField.setFieldName("description");
descField.setFieldType("String");
descField.setRequired(false);

product.setFields(List.of(nameField, priceField, descField));

// Ajouter à la configuration
config.getEntities().add(product);
```

### Utiliser un Générateur Spécifique

```java
import io.github.jhipster.client.generators.angular.AngularGenerator;
import io.github.jhipster.client.generators.react.ReactGenerator;
import io.github.jhipster.client.generators.vue.VueGenerator;
import io.github.jhipster.client.generators.cypress.CypressGenerator;

// Générer uniquement Angular
AngularGenerator angularGen = new AngularGenerator(context);
angularGen.generate();

// Générer uniquement React
ReactGenerator reactGen = new ReactGenerator(context);
reactGen.generate();

// Générer uniquement Vue
VueGenerator vueGen = new VueGenerator(context);
vueGen.generate();

// Générer les tests Cypress
CypressGenerator cypressGen = new CypressGenerator(context);
cypressGen.generate();
```

---

## Utilisation en Ligne de Commande

### Classe Main

Créez ou utilisez la classe `ClientGeneratorMain`:

```bash
# Générer avec .yo-rc.json existant
java -jar jhipster-generator-client.jar /path/to/project

# Générer avec options
java -jar jhipster-generator-client.jar /path/to/project --framework=react

# Afficher l'aide
java -jar jhipster-generator-client.jar --help
```

### Options CLI

| Option | Description | Défaut |
|--------|-------------|--------|
| `--framework` | angular, react, vue | angular |
| `--bundler` | webpack, vite, esbuild | webpack |
| `--theme` | Thème Bootswatch | none |
| `--port` | Port serveur dev | 9000 |
| `--i18n` | Activer i18n | true |
| `--cypress` | Générer tests E2E | false |

---

## Exemples Complets

### Exemple 1: Application Angular avec Entités

```java
public class AngularAppExample {
    public static void main(String[] args) throws Exception {
        Path projectPath = Paths.get("./my-angular-app");
        Files.createDirectories(projectPath);

        // Configuration
        ClientConfig config = new ClientConfig();
        config.setBaseName("store");
        config.setPackageName("com.mycompany.store");
        config.setClientFramework("angular");
        config.setClientBundler("webpack");
        config.setEnableTranslation(true);
        config.setLanguages(List.of("en", "fr", "es"));
        config.setAuthenticationType("jwt");
        config.setTestFrameworks(List.of("cypress"));

        // Entité Product
        EntityConfig product = createEntity("Product",
            field("name", "String", true),
            field("price", "BigDecimal", true),
            field("description", "String", false),
            field("inStock", "Boolean", false)
        );

        // Entité Category
        EntityConfig category = createEntity("Category",
            field("name", "String", true),
            field("description", "String", false)
        );

        config.setEntities(List.of(product, category));

        // Générer
        ClientGeneratorContext context = new ClientGeneratorContext(projectPath, config);
        new ClientGenerator(context).generate();

        System.out.println("Angular app generated at: " + projectPath);
    }

    static EntityConfig createEntity(String name, FieldConfig... fields) {
        EntityConfig entity = new EntityConfig();
        entity.setName(name);
        entity.setEntityAngularName(name);
        entity.setEntityFileName(toKebabCase(name));
        entity.setEntityFolderName(toKebabCase(name));
        entity.setFields(List.of(fields));
        return entity;
    }

    static FieldConfig field(String name, String type, boolean required) {
        FieldConfig field = new FieldConfig();
        field.setFieldName(name);
        field.setFieldType(type);
        field.setRequired(required);
        return field;
    }

    static String toKebabCase(String s) {
        return s.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }
}
```

### Exemple 2: Application React E-Commerce

```java
public class ReactEcommerceExample {
    public static void main(String[] args) throws Exception {
        Path projectPath = Paths.get("./ecommerce-react");

        ClientConfig config = new ClientConfig();
        config.setBaseName("ecommerce");
        config.setClientFramework("react");
        config.setAuthenticationType("oauth2");
        config.setEnableTranslation(false);

        // Entités e-commerce
        config.setEntities(List.of(
            createEntity("Customer", false,
                field("firstName", "String", true),
                field("lastName", "String", true),
                field("email", "String", true),
                field("phone", "String", false)
            ),
            createEntity("Order", false,
                field("orderDate", "Instant", true),
                field("status", "String", true),
                field("totalAmount", "BigDecimal", true)
            ),
            createEntity("OrderItem", false,
                field("quantity", "Integer", true),
                field("unitPrice", "BigDecimal", true)
            )
        ));

        ClientGeneratorContext context = new ClientGeneratorContext(projectPath, config);
        new ClientGenerator(context).generate();
    }
}
```

### Exemple 3: Application Vue avec Vite

```java
public class VueViteExample {
    public static void main(String[] args) throws Exception {
        Path projectPath = Paths.get("./my-vue-app");

        ClientConfig config = new ClientConfig();
        config.setBaseName("myVueApp");
        config.setClientFramework("vue");
        config.setClientBundler("vite");  // Utiliser Vite
        config.setClientTestFramework("vitest");
        config.setAuthenticationType("jwt");
        config.setDevServerPort(3000);

        ClientGeneratorContext context = new ClientGeneratorContext(projectPath, config);
        new ClientGenerator(context).generate();

        System.out.println("Vue + Vite app generated!");
        System.out.println("Next steps:");
        System.out.println("  cd " + projectPath);
        System.out.println("  npm install");
        System.out.println("  npm run dev");
    }
}
```

---

## Tests

### Exécuter les Tests Unitaires

```bash
# Tous les tests
mvn test

# Tests d'un générateur spécifique
mvn test -Dtest=AngularGeneratorTest

# Tests avec rapport détaillé
mvn test -Dsurefire.reportFormat=plain
```

### Tester la Génération Complète

```bash
# 1. Créer un dossier de test
mkdir -p /tmp/test-jhipster
cd /tmp/test-jhipster

# 2. Créer un .yo-rc.json minimal
cat > .yo-rc.json << 'EOF'
{
  "generator-jhipster": {
    "baseName": "testApp",
    "clientFramework": "angular",
    "authenticationType": "jwt",
    "enableTranslation": true
  }
}
EOF

# 3. Exécuter le générateur
java -cp "/path/to/jhipster-generator-client/target/*:/path/to/jhipster-generator-client/target/dependency/*" \
  io.github.jhipster.client.ClientGeneratorMain .

# 4. Vérifier les fichiers générés
ls -la src/main/webapp/
```

### Tester l'Application Générée

```bash
# 1. Installer les dépendances Node
npm install

# 2. Lancer le serveur de développement
npm start
# ou
npm run dev

# 3. Ouvrir dans le navigateur
# http://localhost:9000

# 4. Lancer les tests
npm test

# 5. Lancer les tests E2E (si Cypress)
npm run e2e
```

### Script de Test Automatisé

```bash
#!/bin/bash
# test-generators.sh

set -e

GENERATOR_JAR="target/jhipster-generator-client-1.0.0-SNAPSHOT.jar"
TEST_DIR="/tmp/jhipster-test-$$"

echo "=== Testing JHipster Client Generators ==="

# Fonction de test pour un framework
test_framework() {
    local framework=$1
    local dir="$TEST_DIR/$framework"

    echo ""
    echo ">>> Testing $framework generator..."
    mkdir -p "$dir"

    # Créer config
    cat > "$dir/.yo-rc.json" << EOF
{
  "generator-jhipster": {
    "baseName": "test${framework^}",
    "clientFramework": "$framework",
    "authenticationType": "jwt",
    "enableTranslation": true,
    "devServerPort": 9000
  }
}
EOF

    # Générer
    java -jar "$GENERATOR_JAR" "$dir"

    # Vérifier les fichiers
    echo "Generated files in $dir:"
    find "$dir/src" -name "*.ts" -o -name "*.vue" -o -name "*.tsx" 2>/dev/null | head -20

    echo ">>> $framework: OK"
}

# Build
echo "Building generator..."
mvn package -DskipTests -q

# Tester chaque framework
for fw in angular react vue; do
    test_framework $fw
done

echo ""
echo "=== All tests passed! ==="
echo "Test files in: $TEST_DIR"
```

---

## Dépannage

### Erreurs Communes

#### 1. "File has not been read yet"

```
Error: File has not been read yet. Read it first before writing.
```

**Solution:** Le générateur essaie de modifier un fichier qui n'existe pas encore. Assurez-vous que le dossier cible existe.

```java
Files.createDirectories(projectPath);
```

#### 2. "Template not found"

```
Error: Template processing failed: component.ts.mustache
```

**Solution:** Les templates doivent être dans `src/main/resources/templates/`.

#### 3. "Cannot find module"

```
Error: Cannot find module '@/shared/config/store'
```

**Solution:** Vérifiez le `tsconfig.json` a les bons paths:

```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./src/main/webapp/app/*"]
    }
  }
}
```

#### 4. Port déjà utilisé

```
Error: Port 9000 is already in use
```

**Solution:** Changez le port dans la configuration:

```java
config.setDevServerPort(9001);
```

### Logs et Débogage

#### Activer les logs DEBUG

```java
// Dans logback.xml
<logger name="io.github.jhipster.client" level="DEBUG"/>
```

#### Afficher les fichiers générés

```java
// Après génération
Files.walk(projectPath)
    .filter(Files::isRegularFile)
    .forEach(System.out::println);
```

### Support

- **Issues:** https://github.com/jhipster/generator-jhipster/issues
- **Documentation:** https://www.jhipster.tech/
- **Stack Overflow:** Tag `jhipster`

---

## Annexe: Fichiers Générés par Framework

### Angular

```
src/main/webapp/
├── main.ts
├── index.html
├── app/
│   ├── app.component.ts
│   ├── app.config.ts
│   ├── app.routes.ts
│   ├── core/
│   │   ├── auth/
│   │   │   ├── account.service.ts
│   │   │   └── user-route-access.service.ts
│   │   ├── interceptor/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── error.interceptor.ts
│   │   └── util/
│   │       ├── alert.service.ts
│   │       └── event-manager.service.ts
│   ├── shared/
│   │   └── alert/
│   │       ├── alert.component.ts
│   │       └── alert-error.component.ts
│   ├── layouts/
│   │   ├── navbar/
│   │   └── footer/
│   ├── home/
│   ├── login/
│   ├── admin/
│   └── entities/
│       └── {entity}/
│           ├── {entity}.model.ts
│           ├── {entity}.routes.ts
│           ├── service/{entity}.service.ts
│           ├── list/{entity}.component.ts
│           ├── detail/{entity}-detail.component.ts
│           └── update/{entity}-update.component.ts
├── content/
│   ├── css/
│   └── scss/
├── angular.json
├── tsconfig.json
└── package.json
```

### React

```
src/main/webapp/
├── index.tsx
├── index.html
├── app/
│   ├── app.tsx
│   ├── routes.tsx
│   ├── app.scss
│   ├── config/
│   │   ├── store.ts
│   │   ├── constants.ts
│   │   ├── axios-interceptor.ts
│   │   ├── error-middleware.ts
│   │   └── notification-middleware.ts
│   ├── shared/
│   │   ├── reducers/
│   │   │   ├── index.ts
│   │   │   ├── authentication.ts
│   │   │   └── application-profile.ts
│   │   ├── model/
│   │   ├── auth/
│   │   ├── error/
│   │   └── layout/
│   │       ├── header/
│   │       └── footer/
│   ├── modules/
│   │   ├── home/
│   │   ├── login/
│   │   └── administration/
│   └── entities/
│       ├── index.tsx
│       ├── reducers.ts
│       └── {entity}/
│           ├── index.tsx
│           ├── {entity}.tsx
│           ├── {entity}-detail.tsx
│           ├── {entity}-update.tsx
│           ├── {entity}-delete-dialog.tsx
│           └── {entity}.reducer.ts
├── webpack/
├── tsconfig.json
└── package.json
```

### Vue

```
src/main/webapp/
├── main.ts
├── index.html
├── app/
│   ├── app.vue
│   ├── router/
│   │   ├── index.ts
│   │   └── entities.ts
│   ├── shared/
│   │   ├── config/
│   │   │   ├── axios-interceptor.ts
│   │   │   └── store/
│   │   │       └── account-store.ts
│   │   └── model/
│   ├── core/
│   │   ├── home/home.vue
│   │   ├── navbar/navbar.vue
│   │   └── footer/footer.vue
│   ├── account/
│   │   ├── login.vue
│   │   └── logout.vue
│   ├── admin/
│   └── entities/
│       └── {entity}/
│           ├── {entity}.vue
│           ├── {entity}-details.vue
│           ├── {entity}-update.vue
│           └── {entity}.service.ts
├── content/scss/
├── vite.config.ts (ou webpack/)
├── tsconfig.json
└── package.json
```

---

**Date:** Janvier 2025
**Version:** 1.0.0
