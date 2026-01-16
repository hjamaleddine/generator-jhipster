# Guide d'Utilisation - JHipster Generator Java (Backend)

## Table des Matières

1. [Prérequis](#prérequis)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Utilisation CLI](#utilisation-cli)
5. [Utilisation Programmatique](#utilisation-programmatique)
6. [Générateurs Disponibles](#générateurs-disponibles)
7. [Exemples Complets](#exemples-complets)
8. [Tests](#tests)
9. [Dépannage](#dépannage)

---

## Prérequis

### Logiciels Requis

| Logiciel | Version | Vérification |
|----------|---------|--------------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |

### Optionnel

| Logiciel | Version | Usage |
|----------|---------|-------|
| Docker | 20+ | Pour les conteneurs de base de données |
| PostgreSQL | 14+ | Base de données production |
| Node.js | 18+ | Si frontend inclus |

---

## Installation

### 1. Cloner et Builder le Projet

```bash
# Cloner le repository
git clone <repository-url>
cd generator-jhipster/jhipster-generator-java

# Builder le projet (crée un fat jar)
mvn clean package -DskipTests

# Vérifier le build
ls -la target/*.jar
# Devrait afficher: jhipster-generator-java-1.0.0-SNAPSHOT.jar
```

### 2. Vérifier l'Installation

```bash
# Afficher l'aide
java -jar target/jhipster-generator-java-1.0.0-SNAPSHOT.jar --help

# Afficher la version
java -jar target/jhipster-generator-java-1.0.0-SNAPSHOT.jar --version
```

---

## Configuration

### Structure du Fichier .yo-rc.json

Le générateur peut lire sa configuration depuis `.yo-rc.json`:

```json
{
  "generator-jhipster": {
    "baseName": "myApp",
    "packageName": "com.mycompany.myapp",
    "packageFolder": "com/mycompany/myapp",
    "applicationType": "microservice",
    "authenticationType": "jwt",
    "databaseType": "sql",
    "devDatabaseType": "h2Disk",
    "prodDatabaseType": "postgresql",
    "buildTool": "maven",
    "serviceDiscoveryType": "consul",
    "serverPort": 8081,
    "skipClient": true,
    "skipUserManagement": false,
    "enableSwaggerCodegen": true,
    "cacheProvider": "ehcache",
    "messageBroker": "kafka",
    "searchEngine": "elasticsearch",
    "websocket": "spring-websocket",
    "feignClient": true,
    "entities": []
  }
}
```

### Options de Configuration

#### applicationType

| Valeur | Description |
|--------|-------------|
| `monolith` | Application monolithique |
| `microservice` | Microservice Spring Boot |
| `gateway` | Gateway Spring Cloud |

#### databaseType

| Valeur | Description |
|--------|-------------|
| `sql` | Base SQL (PostgreSQL, MySQL, etc.) |
| `mongodb` | MongoDB |
| `cassandra` | Apache Cassandra |
| `couchbase` | Couchbase |
| `neo4j` | Neo4j (graphe) |
| `no` | Pas de base de données |

#### authenticationType

| Valeur | Description |
|--------|-------------|
| `jwt` | JSON Web Token |
| `oauth2` | OAuth2/OIDC |
| `session` | Session HTTP |

#### serviceDiscoveryType

| Valeur | Description |
|--------|-------------|
| `consul` | HashiCorp Consul |
| `eureka` | Netflix Eureka |
| `no` | Pas de service discovery |

#### cacheProvider

| Valeur | Description |
|--------|-------------|
| `ehcache` | Ehcache (défaut) |
| `caffeine` | Caffeine |
| `hazelcast` | Hazelcast |
| `redis` | Redis |
| `no` | Pas de cache |

---

## Utilisation CLI

### Syntaxe Générale

```bash
java -jar jhipster-generator-java-1.0.0-SNAPSHOT.jar [generator] [options]
```

### Options Disponibles

| Option | Description | Défaut |
|--------|-------------|--------|
| `-d, --directory` | Répertoire cible | `.` |
| `--base-name` | Nom de l'application | - |
| `--package-name` | Package Java | - |
| `--application-type` | Type d'application | `microservice` |
| `--database-type` | Type de BDD | `sql` |
| `--prod-database-type` | BDD de production | `postgresql` |
| `--authentication-type` | Type d'auth | `jwt` |
| `--service-discovery-type` | Service discovery | `consul` |
| `--server-port` | Port serveur | `8080` |
| `--feign-client` | Activer Feign | `false` |
| `--skip-client` | Ignorer frontend | `true` |
| `--skip-user-management` | Ignorer gestion users | `false` |

### Exemples CLI

```bash
# Générer un microservice basique
java -jar jhipster-generator-java-*.jar app \
  --base-name=orderService \
  --package-name=com.mycompany.order \
  --application-type=microservice \
  --database-type=sql \
  --prod-database-type=postgresql \
  --authentication-type=jwt

# Générer un gateway
java -jar jhipster-generator-java-*.jar app \
  --base-name=gateway \
  --package-name=com.mycompany.gateway \
  --application-type=gateway \
  --service-discovery-type=consul

# Générer avec MongoDB
java -jar jhipster-generator-java-*.jar app \
  --base-name=productService \
  --database-type=mongodb \
  --skip-user-management=true

# Générer dans un répertoire spécifique
java -jar jhipster-generator-java-*.jar app \
  -d ./my-microservice \
  --base-name=myService
```

---

## Utilisation Programmatique

### Exemple Basique

```java
import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.app.AppGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateApp {
    public static void main(String[] args) throws Exception {
        // 1. Créer la configuration
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("myService");
        config.setPackageName("com.mycompany.myservice");
        config.setApplicationType("microservice");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setServerPort(8081);
        config.setSkipClient(true);

        // 2. Créer le contexte
        Path projectPath = Paths.get("./my-service");
        GeneratorContext context = new GeneratorContext(projectPath, config);

        // 3. Exécuter le générateur
        AppGenerator generator = new AppGenerator(context);
        generator.run();

        System.out.println("Application generated at: " + projectPath);
    }
}
```

### Charger depuis .yo-rc.json

```java
import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.generators.app.AppGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateFromConfig {
    public static void main(String[] args) throws Exception {
        // Charger la configuration depuis .yo-rc.json
        Path projectPath = Paths.get("./existing-project");
        GeneratorContext context = GeneratorContext.fromYoRc(projectPath);

        // Générer
        new AppGenerator(context).run();
    }
}
```

### Utiliser des Générateurs Spécifiques

```java
import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.springboot.SpringBootGenerator;
import io.github.jhipster.generator.generators.docker.DockerGenerator;
import io.github.jhipster.generator.generators.liquibase.LiquibaseGenerator;
import io.github.jhipster.generator.generators.kubernetes.KubernetesGenerator;

// Générer uniquement Spring Boot
new SpringBootGenerator(context).run();

// Générer la configuration Docker
new DockerGenerator(context).run();

// Générer les migrations Liquibase
new LiquibaseGenerator(context).run();

// Générer les manifests Kubernetes
new KubernetesGenerator(context).run();
```

### Ajouter des Entités

```java
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;

// Créer une entité Product
EntityConfig product = new EntityConfig();
product.setName("Product");
product.setEntityTableName("product");

// Ajouter des champs
FieldConfig nameField = new FieldConfig();
nameField.setFieldName("name");
nameField.setFieldType("String");
nameField.setFieldValidateRules(List.of("required", "maxlength"));
nameField.setFieldValidateRulesMaxlength(100);

FieldConfig priceField = new FieldConfig();
priceField.setFieldName("price");
priceField.setFieldType("BigDecimal");
priceField.setFieldValidateRules(List.of("required", "min"));
priceField.setFieldValidateRulesMin("0");

product.setFields(List.of(nameField, priceField));

// Ajouter une relation
RelationshipConfig categoryRel = new RelationshipConfig();
categoryRel.setRelationshipName("category");
categoryRel.setRelationshipType("many-to-one");
categoryRel.setOtherEntityName("Category");

product.setRelationships(List.of(categoryRel));

// Ajouter à la configuration
config.getEntities().add(product);
```

---

## Générateurs Disponibles

### Générateurs Principaux

| Générateur | Description |
|------------|-------------|
| `AppGenerator` | Orchestrateur principal |
| `SpringBootGenerator` | Configuration Spring Boot |
| `ServerGenerator` | Configuration serveur |
| `DomainGenerator` | Entités JPA/domain |
| `EntityGenerator` | CRUD complet par entité |

### Générateurs de Base de Données

| Générateur | Description |
|------------|-------------|
| `SpringDataRelationalGenerator` | JPA/Hibernate |
| `LiquibaseGenerator` | Migrations Liquibase |
| `SpringDataMongoDBGenerator` | MongoDB |
| `SpringDataCassandraGenerator` | Cassandra |
| `SpringDataCouchbaseGenerator` | Couchbase |
| `SpringDataNeo4jGenerator` | Neo4j |
| `SpringDataElasticsearchGenerator` | Elasticsearch |

### Générateurs d'Infrastructure

| Générateur | Description |
|------------|-------------|
| `DockerGenerator` | Dockerfile |
| `DockerComposeGenerator` | docker-compose.yml |
| `KubernetesGenerator` | Manifests K8s |
| `KubernetesHelmGenerator` | Charts Helm |
| `JibGenerator` | Build images avec Jib |

### Générateurs de Fonctionnalités

| Générateur | Description |
|------------|-------------|
| `SecurityGenerator` | Spring Security |
| `CacheGenerator` | Configuration cache |
| `GatewayGenerator` | Spring Cloud Gateway |
| `FeignClientGenerator` | Clients Feign |
| `SpringCloudStreamGenerator` | Kafka/messaging |
| `SpringWebSocketGenerator` | WebSockets |
| `SwaggerGenerator` | OpenAPI/Swagger |

### Générateurs DevOps

| Générateur | Description |
|------------|-------------|
| `MavenGenerator` | pom.xml |
| `GradleGenerator` | build.gradle |
| `GitGenerator` | .gitignore, etc. |
| `CiCdGenerator` | GitHub Actions, GitLab CI |
| `CodeQualityGenerator` | Checkstyle, PMD |

---

## Exemples Complets

### Exemple 1: Microservice E-Commerce

```java
public class ECommerceServiceExample {
    public static void main(String[] args) throws Exception {
        Path projectPath = Paths.get("./order-service");

        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("orderService");
        config.setPackageName("com.mycompany.order");
        config.setApplicationType("microservice");
        config.setAuthenticationType("jwt");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setServiceDiscoveryType("consul");
        config.setMessageBroker("kafka");
        config.setCacheProvider("redis");
        config.setServerPort(8082);
        config.setSkipClient(true);
        config.setFeignClient(true);

        // Entité Order
        EntityConfig order = new EntityConfig();
        order.setName("Order");
        order.setFields(List.of(
            createField("orderNumber", "String", true),
            createField("orderDate", "Instant", true),
            createField("status", "OrderStatus", true), // Enum
            createField("totalAmount", "BigDecimal", true),
            createField("shippingAddress", "String", false)
        ));

        // Entité OrderItem
        EntityConfig orderItem = new EntityConfig();
        orderItem.setName("OrderItem");
        orderItem.setFields(List.of(
            createField("productId", "Long", true),
            createField("productName", "String", true),
            createField("quantity", "Integer", true),
            createField("unitPrice", "BigDecimal", true)
        ));

        config.setEntities(List.of(order, orderItem));

        GeneratorContext context = new GeneratorContext(projectPath, config);
        new AppGenerator(context).run();

        System.out.println("Order Service generated!");
    }

    static FieldConfig createField(String name, String type, boolean required) {
        FieldConfig field = new FieldConfig();
        field.setFieldName(name);
        field.setFieldType(type);
        if (required) {
            field.setFieldValidateRules(List.of("required"));
        }
        return field;
    }
}
```

### Exemple 2: Gateway avec Service Discovery

```java
public class GatewayExample {
    public static void main(String[] args) throws Exception {
        Path projectPath = Paths.get("./api-gateway");

        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("gateway");
        config.setPackageName("com.mycompany.gateway");
        config.setApplicationType("gateway");
        config.setAuthenticationType("oauth2");
        config.setServiceDiscoveryType("consul");
        config.setServerPort(8080);
        config.setSkipClient(false);
        config.setClientFramework("angular");

        GeneratorContext context = new GeneratorContext(projectPath, config);
        new AppGenerator(context).run();

        System.out.println("Gateway generated!");
        System.out.println("Start with: ./mvnw");
        System.out.println("Consul UI: http://localhost:8500");
    }
}
```

### Exemple 3: Service avec MongoDB

```java
public class MongoServiceExample {
    public static void main(String[] args) throws Exception {
        Path projectPath = Paths.get("./product-catalog");

        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("productCatalog");
        config.setPackageName("com.mycompany.catalog");
        config.setApplicationType("microservice");
        config.setDatabaseType("mongodb");
        config.setAuthenticationType("jwt");
        config.setSearchEngine("elasticsearch");
        config.setSkipUserManagement(true);

        // Entité avec document MongoDB
        EntityConfig product = new EntityConfig();
        product.setName("Product");
        product.setFields(List.of(
            createField("sku", "String", true),
            createField("name", "String", true),
            createField("description", "TextBlob", false),
            createField("price", "BigDecimal", true),
            createField("category", "String", false),
            createField("tags", "String", false), // Array simulé
            createField("createdAt", "Instant", false),
            createField("updatedAt", "Instant", false)
        ));

        config.setEntities(List.of(product));

        GeneratorContext context = new GeneratorContext(projectPath, config);
        new AppGenerator(context).run();
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
mvn test -Dtest=SpringBootGeneratorTest

# Tests avec rapport détaillé
mvn test -Dsurefire.reportFormat=plain

# Tests avec couverture
mvn test jacoco:report
```

### Tester la Génération Complète

```bash
# 1. Créer un dossier de test
mkdir -p /tmp/test-jhipster-backend
cd /tmp/test-jhipster-backend

# 2. Générer une application
java -jar /path/to/jhipster-generator-java-*.jar app \
  --base-name=testApp \
  --package-name=com.test \
  --application-type=microservice

# 3. Vérifier les fichiers générés
ls -la src/main/java/com/test/

# 4. Compiler l'application générée
./mvnw compile

# 5. Lancer les tests générés
./mvnw test
```

### Script de Test Automatisé

```bash
#!/bin/bash
# test-backend-generators.sh

set -e

JAR="target/jhipster-generator-java-1.0.0-SNAPSHOT.jar"
TEST_DIR="/tmp/jhipster-backend-test-$$"

echo "=== Testing JHipster Backend Generators ==="

# Build
echo "Building..."
mvn package -DskipTests -q

# Test microservice avec PostgreSQL
test_microservice() {
    echo ">>> Testing microservice with PostgreSQL..."
    mkdir -p "$TEST_DIR/postgres-service"
    java -jar "$JAR" app \
        -d "$TEST_DIR/postgres-service" \
        --base-name=postgresService \
        --package-name=com.test.postgres \
        --application-type=microservice \
        --database-type=sql \
        --prod-database-type=postgresql

    echo "Compiling..."
    cd "$TEST_DIR/postgres-service"
    ./mvnw compile -q
    echo ">>> PostgreSQL service: OK"
}

# Test avec MongoDB
test_mongodb() {
    echo ">>> Testing microservice with MongoDB..."
    mkdir -p "$TEST_DIR/mongo-service"
    java -jar "$JAR" app \
        -d "$TEST_DIR/mongo-service" \
        --base-name=mongoService \
        --database-type=mongodb

    cd "$TEST_DIR/mongo-service"
    ./mvnw compile -q
    echo ">>> MongoDB service: OK"
}

# Test gateway
test_gateway() {
    echo ">>> Testing gateway..."
    mkdir -p "$TEST_DIR/gateway"
    java -jar "$JAR" app \
        -d "$TEST_DIR/gateway" \
        --base-name=gateway \
        --application-type=gateway

    cd "$TEST_DIR/gateway"
    ./mvnw compile -q
    echo ">>> Gateway: OK"
}

# Exécuter les tests
test_microservice
test_mongodb
test_gateway

echo ""
echo "=== All tests passed! ==="
echo "Test files in: $TEST_DIR"
```

---

## Dépannage

### Erreurs Communes

#### 1. "Package name is not valid"

```
Error: Package name 'com.my-company.app' is not valid
```

**Solution:** Utilisez uniquement des lettres minuscules et des points:
```bash
--package-name=com.mycompany.app
```

#### 2. "Port already in use"

```
Error: Port 8080 is already in use
```

**Solution:** Spécifiez un port différent:
```bash
--server-port=8081
```

#### 3. "Database connection failed"

Pour les tests, utilisez H2:
```properties
# application-dev.yml généré
spring:
  datasource:
    url: jdbc:h2:file:./target/h2db/db/myapp
```

#### 4. Erreur Liquibase

```
Liquibase: Could not acquire change log lock
```

**Solution:**
```bash
# Supprimer le lock
rm -rf target/h2db
./mvnw clean
```

### Logs et Débogage

#### Activer les logs DEBUG

```bash
# Via variable d'environnement
export LOGGING_LEVEL_IO_GITHUB_JHIPSTER=DEBUG
java -jar jhipster-generator-java-*.jar app ...

# Ou dans logback.xml
<logger name="io.github.jhipster.generator" level="DEBUG"/>
```

#### Vérifier les fichiers générés

```bash
# Structure du projet
find . -type f -name "*.java" | head -20

# Fichiers de configuration
cat src/main/resources/application.yml

# Vérifier pom.xml
cat pom.xml | grep -A2 "<parent>"
```

---

## Annexe: Fichiers Générés

### Structure Projet Microservice

```
my-service/
├── src/
│   ├── main/
│   │   ├── java/com/mycompany/myservice/
│   │   │   ├── MyServiceApp.java              # Application principale
│   │   │   ├── config/
│   │   │   │   ├── ApplicationProperties.java
│   │   │   │   ├── DatabaseConfiguration.java
│   │   │   │   ├── SecurityConfiguration.java
│   │   │   │   ├── CacheConfiguration.java
│   │   │   │   ├── WebConfigurer.java
│   │   │   │   └── LoggingAspectConfiguration.java
│   │   │   ├── domain/
│   │   │   │   ├── AbstractAuditingEntity.java
│   │   │   │   └── {Entity}.java
│   │   │   ├── repository/
│   │   │   │   └── {Entity}Repository.java
│   │   │   ├── service/
│   │   │   │   ├── {Entity}Service.java
│   │   │   │   └── dto/{Entity}DTO.java
│   │   │   ├── web/rest/
│   │   │   │   ├── {Entity}Resource.java
│   │   │   │   └── errors/
│   │   │   ├── security/
│   │   │   │   ├── jwt/
│   │   │   │   └── SecurityUtils.java
│   │   │   └── aop/logging/
│   │   │       └── LoggingAspect.java
│   │   └── resources/
│   │       ├── config/
│   │       │   ├── application.yml
│   │       │   ├── application-dev.yml
│   │       │   └── application-prod.yml
│   │       ├── i18n/
│   │       └── templates/
│   └── test/
│       └── java/com/mycompany/myservice/
│           ├── IntegrationTest.java
│           └── web/rest/{Entity}ResourceIT.java
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

**Date:** Janvier 2025
**Version:** 1.0.0
