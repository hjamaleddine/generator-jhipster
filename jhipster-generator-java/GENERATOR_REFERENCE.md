# JHipster Java Generator - Documentation de Référence

## Table des Matières

1. [Vue d'Ensemble](#1-vue-densemble)
2. [Architecture du Framework](#2-architecture-du-framework)
3. [Cycle de Vie des Générateurs](#3-cycle-de-vie-des-générateurs)
4. [Configuration (JHipsterConfig)](#4-configuration-jhipsterconfig)
5. [Catalogue des Générateurs](#5-catalogue-des-générateurs)
6. [Flux de Composition](#6-flux-de-composition)
7. [Conditions d'Activation](#7-conditions-dactivation)
8. [Fichiers Générés](#8-fichiers-générés)
9. [Diagrammes](#9-diagrammes)

---

## 1. Vue d'Ensemble

Le **JHipster Java Generator** est un port Java du framework de génération JHipster TypeScript (basé sur Yeoman). Il génère des applications Spring Boot complètes avec toute l'infrastructure nécessaire.

### 1.1 Objectifs
- Génération d'applications Spring Boot microservices
- Support des architectures monolithiques, microservices et gateway
- Configuration automatique de la sécurité (JWT/OAuth2)
- Intégration avec bases de données SQL et NoSQL
- Génération de la CI/CD et containerisation

### 1.2 Technologies Supportées
| Catégorie | Options |
|-----------|---------|
| Framework | Spring Boot 3.2.x |
| Build | Maven |
| Base de données | PostgreSQL, MySQL, MariaDB, H2 |
| Authentification | JWT, OAuth2, Session |
| Service Discovery | Consul, Eureka |
| Cache | Ehcache, Caffeine, Redis, Hazelcast |
| Message Broker | Kafka, Pulsar |
| Search Engine | Elasticsearch |
| CI/CD | GitHub Actions, GitLab CI, Jenkins, Azure, CircleCI, Travis |

---

## 2. Architecture du Framework

### 2.1 Structure des Packages

```
io.github.jhipster.generator/
├── config/
│   ├── GeneratorContext.java      # Contexte partagé entre générateurs
│   └── JHipsterConfig.java        # Configuration de l'application
├── core/
│   ├── BaseGenerator.java         # Classe de base pour tous les générateurs
│   ├── BaseApplicationGenerator.java  # Base pour les générateurs d'application
│   ├── GeneratorPriority.java     # Priorités d'exécution
│   └── GeneratorTask.java         # Interface de tâche
├── model/
│   ├── EntityConfig.java          # Configuration d'entité
│   ├── FieldConfig.java           # Configuration de champ
│   └── RelationshipConfig.java    # Configuration de relation
├── template/
│   ├── JavaCodeBuilder.java       # Constructeur de code Java
│   └── TemplateEngine.java        # Moteur de templates
└── generators/
    ├── app/                       # Point d'entrée principal
    ├── bootstrap/                 # Initialisation
    ├── server/                    # Générateurs serveur
    ├── springboot/               # Configuration Spring Boot
    └── ...                       # Autres générateurs
```

### 2.2 Classes Principales

#### BaseGenerator
```java
public abstract class BaseGenerator {
    protected final GeneratorContext context;
    protected final Logger log;

    // Méthodes principales
    public abstract String getName();
    protected abstract void registerTasks();
    protected void beforeQueue() { }

    // Composition
    protected void composeWith(BaseGenerator generator);
    protected void dependsOn(BaseGenerator generator);

    // Opérations de fichiers
    protected void writeFile(String path, String content);
    protected void writeTemplate(String template, String path, Map<String, Object> data);

    // Helpers de configuration
    protected JHipsterConfig getConfig();
    protected String getMainJavaPath();
    protected String getTestJavaPath();
}
```

#### BaseApplicationGenerator
```java
public abstract class BaseApplicationGenerator extends BaseGenerator {
    // Méthodes pour le traitement des entités
    protected void configuringEachEntity(EntityConfig entity);
    protected void preparingEachEntity(EntityConfig entity);
    protected void preparingEachEntityField(EntityConfig entity, FieldConfig field);
    protected void preparingEachEntityRelationship(EntityConfig entity, RelationshipConfig rel);
    protected void writingEntity(EntityConfig entity);

    // Helpers de type d'application
    protected boolean isMonolith();
    protected boolean isMicroservice();
    protected boolean isGateway();

    // Helpers de base de données
    protected boolean isSql();
    protected boolean isMongodb();

    // Helpers d'authentification
    protected boolean isJwt();
    protected boolean isOauth2();

    // Helpers de fonctionnalités
    protected boolean hasServiceDiscovery();
    protected boolean hasMessageBroker();
    protected boolean hasSearchEngine();
}
```

---

## 3. Cycle de Vie des Générateurs

### 3.1 Priorités d'Exécution (GeneratorPriority)

Les générateurs s'exécutent selon un ordre de priorité défini :

| Priorité | Ordre | Description |
|----------|-------|-------------|
| `INITIALIZING` | 100 | Initialisation du générateur |
| `PROMPTING` | 200 | Questions à l'utilisateur |
| `CONFIGURING` | 300 | Configuration de l'application |
| `CONFIGURING_EACH_ENTITY` | 310 | Configuration par entité |
| `COMPOSING` | 400 | Composition avec sous-générateurs |
| `COMPOSING_COMPONENT` | 350 | Composition de composants |
| `LOADING` | 500 | Chargement des données |
| `LOADING_ENTITIES` | 510 | Chargement des entités |
| `PREPARING` | 600 | Préparation des données |
| `PREPARING_EACH_ENTITY` | 610 | Préparation par entité |
| `PREPARING_EACH_ENTITY_FIELD` | 620 | Préparation par champ |
| `PREPARING_EACH_ENTITY_RELATIONSHIP` | 630 | Préparation par relation |
| `POST_PREPARING_EACH_ENTITY` | 640 | Post-préparation par entité |
| `DEFAULT` | 700 | Phase par défaut |
| `WRITING` | 800 | Écriture des fichiers |
| `WRITING_ENTITIES` | 810 | Écriture des entités |
| `POST_WRITING_ENTITIES` | 820 | Post-écriture des entités |
| `POST_WRITING` | 850 | Post-écriture générale |
| `CONFLICTS` | 900 | Résolution des conflits |
| `INSTALL` | 1000 | Installation des dépendances |
| `END` | 1100 | Fin de la génération |

### 3.2 Flux d'Exécution

```
┌─────────────────────────────────────────────────────────────────┐
│                        INITIALIZING (100)                        │
│  - Initialisation des paramètres                                │
│  - Log de démarrage                                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CONFIGURING (300)                         │
│  - Configuration de l'application                               │
│  - Ajustement des paramètres (ex: skipUserManagement)           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         COMPOSING (400)                          │
│  - Composition avec sous-générateurs                            │
│  - Construction de l'arbre de génération                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         PREPARING (600)                          │
│  - Préparation des propriétés                                   │
│  - Calcul des URLs de base de données                           │
│  - Configuration des services                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          WRITING (800)                           │
│  - Génération des fichiers de code                              │
│  - Écriture des configurations                                  │
│  - Création de la structure du projet                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                            END (1100)                            │
│  - Affichage des instructions                                   │
│  - Fin de la génération                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. Configuration (JHipsterConfig)

### 4.1 Paramètres de Configuration

La classe `JHipsterConfig` contient tous les paramètres de configuration de l'application, lus depuis `.yo-rc.json`.

#### Application
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `baseName` | String | "jhipster" | Nom de l'application |
| `applicationType` | String | "microservice" | Type: monolith, microservice, gateway |
| `packageName` | String | "com.mycompany.myapp" | Package Java de base |
| `serverPort` | Integer | 8080 | Port du serveur |
| `reactive` | Boolean | false | Mode réactif (WebFlux) |

#### Authentification
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `authenticationType` | String | "jwt" | Type: jwt, oauth2, session |
| `skipUserManagement` | Boolean | false | Ignorer la gestion des utilisateurs |
| `jwtSecretKey` | String | null | Clé secrète JWT (Base64) |

#### Base de Données
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `databaseType` | String | "sql" | Type: sql, mongodb, cassandra, neo4j |
| `devDatabaseType` | String | "h2Disk" | Base dev: h2Disk, h2Memory, postgresql, mysql |
| `prodDatabaseType` | String | "postgresql" | Base prod: postgresql, mysql, mariadb |

#### Service Discovery
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `serviceDiscoveryType` | String | "consul" | Type: consul, eureka, no |

#### Cache
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `cacheProvider` | String | "ehcache" | Provider: ehcache, caffeine, redis, hazelcast, no |
| `enableHibernateCache` | Boolean | true | Activer le cache Hibernate L2 |

#### Fonctionnalités Optionnelles
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `searchEngine` | String | "no" | Moteur: elasticsearch, no |
| `messageBroker` | String | "no" | Broker: kafka, pulsar, no |
| `websocket` | String | "no" | WebSocket: spring-websocket, no |
| `feignClient` | Boolean | false | Activer Feign pour les appels inter-services |

#### Entités
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `dto` | String | "mapstruct" | Stratégie DTO: mapstruct, no |
| `service` | String | "serviceImpl" | Stratégie service: serviceImpl, serviceClass, no |
| `pagination` | String | "pagination" | Pagination: pagination, infinite-scroll, no |
| `jhiPrefix` | String | "jhi" | Préfixe pour les composants JHipster |
| `entitySuffix` | String | "" | Suffixe des entités |
| `dtoSuffix` | String | "DTO" | Suffixe des DTOs |

### 4.2 Propriétés Calculées

```java
// Nom de la classe principale
public String getMainClass() {
    return capitalize(baseName) + "App";  // Ex: "MyappApp"
}

// Nom en minuscules
public String getLowerBaseName() {
    return baseName.toLowerCase();  // Ex: "myapp"
}

// Préfixe d'endpoint pour microservices
public String getEndpointPrefix() {
    if (isMicroservice()) {
        return "services/" + getLowerBaseName();  // Ex: "services/myapp"
    }
    return "";
}

// Dossier du package
public String getPackageFolder() {
    return packageName.replace('.', '/');  // Ex: "com/mycompany/myapp"
}
```

### 4.3 Méthodes de Vérification

```java
// Type d'application
boolean isMicroservice();  // applicationType == "microservice"
boolean isGateway();       // applicationType == "gateway"
boolean isMonolith();      // applicationType == "monolith"

// Base de données
boolean isSql();           // databaseType == "sql"

// Authentification
boolean isJwt();           // authenticationType == "jwt"
boolean isOauth2();        // authenticationType == "oauth2"

// Service Discovery
boolean hasServiceDiscovery();  // serviceDiscoveryType != null && != "no"
boolean isConsul();            // serviceDiscoveryType == "consul"
boolean isEureka();            // serviceDiscoveryType == "eureka"
```

### 4.4 Exemple de Configuration (.yo-rc.json)

```json
{
  "generator-jhipster": {
    "baseName": "myMicroservice",
    "applicationType": "microservice",
    "packageName": "com.example.myservice",
    "packageFolder": "com/example/myservice",
    "serverPort": 8081,
    "authenticationType": "jwt",
    "databaseType": "sql",
    "devDatabaseType": "h2Disk",
    "prodDatabaseType": "postgresql",
    "buildTool": "maven",
    "serviceDiscoveryType": "consul",
    "cacheProvider": "ehcache",
    "enableHibernateCache": true,
    "searchEngine": "no",
    "messageBroker": "kafka",
    "websocket": "no",
    "feignClient": true,
    "skipClient": true,
    "skipUserManagement": false,
    "dto": "mapstruct",
    "service": "serviceImpl",
    "jhiPrefix": "jhi",
    "reactive": false,
    "jwtSecretKey": "YTM1YjIxMjQyNmMxZjRhZTc0MTc1..."
  }
}
```

---

## 5. Catalogue des Générateurs

### 5.1 Générateurs Principaux

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `AppGenerator` | `generators/app/` | Point d'entrée principal |
| `ServerGenerator` | `generators/server/` | Orchestrateur serveur |
| `SpringBootGenerator` | `generators/springboot/` | Configuration Spring Boot |
| `BootstrapApplicationBaseGenerator` | `generators/bootstrap/` | Initialisation de base |

### 5.2 Générateurs d'Infrastructure

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `GitGenerator` | `generators/git/` | .gitignore, .gitattributes |
| `MavenGenerator` | `generators/maven/` | mvnw, .mvn/, maven wrapper |
| `CommonGenerator` | `generators/common/` | editorconfig, prettier, README |
| `CiCdGenerator` | `generators/cicd/` | GitHub Actions, GitLab CI, Jenkins |

### 5.3 Générateurs Serveur

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `SecurityGenerator` | `generators/server/security/` | JWT/OAuth2 security |
| `ErrorHandlingGenerator` | `generators/server/error/` | Gestion des erreurs |
| `AuditGenerator` | `generators/server/audit/` | Audit des entités |
| `LoggingAspectGenerator` | `generators/server/logging/` | AOP logging |
| `CacheGenerator` | `generators/server/cache/` | Configuration cache |
| `UserManagementGenerator` | `generators/server/user/` | Gestion utilisateurs |
| `TestInfrastructureGenerator` | `generators/server/test/` | Infrastructure de tests |

### 5.4 Générateurs Base de Données

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `SpringDataRelationalGenerator` | `generators/springdata/` | JPA repositories |
| `LiquibaseGenerator` | `generators/liquibase/` | Migrations Liquibase |
| `SpringDataElasticsearchGenerator` | `generators/elasticsearch/` | Search repositories |

### 5.5 Générateurs Domaine

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `DomainGenerator` | `generators/domain/` | Entités, DTOs, Mappers |
| `EntityGenerator` | `generators/entity/` | Génération d'entité individuelle |

### 5.6 Générateurs Cloud/Microservices

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `GatewayGenerator` | `generators/gateway/` | Spring Cloud Gateway |
| `FeignClientGenerator` | `generators/feign/` | Clients Feign |
| `SpringCloudStreamGenerator` | `generators/kafka/` | Kafka/Pulsar messaging |
| `SpringWebSocketGenerator` | `generators/websocket/` | WebSocket STOMP |

### 5.7 Générateurs DevOps

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `DockerGenerator` | `generators/docker/` | Dockerfile |
| `DockerComposeGenerator` | `generators/docker/` | Docker Compose files |
| `JibGenerator` | `generators/jib/` | Container builds (Jib) |
| `CodeQualityGenerator` | `generators/codequality/` | SpotBugs, PMD, JaCoCo |

### 5.8 Générateurs Documentation

| Générateur | Fichier | Description |
|------------|---------|-------------|
| `SwaggerGenerator` | `generators/swagger/` | OpenAPI/Swagger config |
| `MetricsGenerator` | `generators/metrics/` | Micrometer, Prometheus |

---

## 6. Flux de Composition

### 6.1 Arbre de Composition Principal

```
AppGenerator
├── BootstrapApplicationBaseGenerator (dependsOn)
├── CommonGenerator (composeWith)
│   └── GitGenerator (composeWith)
├── MavenGenerator (composeWith)
└── ServerGenerator (composeWith)
    └── SpringBootGenerator (composeWith)
        ├── DomainGenerator (dependsOn)
        ├── SecurityGenerator (composeWith)
        ├── ErrorHandlingGenerator (composeWith)
        ├── AuditGenerator (composeWith)
        ├── LoggingAspectGenerator (composeWith)
        ├── CacheGenerator (composeWith)
        ├── SwaggerGenerator (composeWith)
        ├── MetricsGenerator (composeWith)
        ├── CodeQualityGenerator (composeWith)
        ├── DockerGenerator (composeWith)
        ├── DockerComposeGenerator (composeWith)
        ├── JibGenerator (composeWith)
        ├── [Conditionnel] SpringDataRelationalGenerator (si SQL)
        ├── [Conditionnel] LiquibaseGenerator (si SQL)
        ├── [Conditionnel] SpringDataElasticsearchGenerator (si elasticsearch)
        ├── [Conditionnel] SpringCloudStreamGenerator (si messageBroker)
        ├── [Conditionnel] SpringWebSocketGenerator (si websocket)
        ├── [Conditionnel] GatewayGenerator (si gateway)
        ├── [Conditionnel] UserManagementGenerator (si !skipUserManagement)
        ├── [Conditionnel] FeignClientGenerator (si feignClient + microservice)
        ├── TestInfrastructureGenerator (composeWith)
        └── CiCdGenerator (composeWith)
```

### 6.2 Mécanisme de Composition

```java
// Dans AppGenerator.java
private void composing() {
    // Générateurs toujours exécutés
    composeWith(new CommonGenerator(context));
    composeWith(new MavenGenerator(context));

    // Composition conditionnelle
    if (!Boolean.TRUE.equals(getConfig().getSkipClient()) || isMicroservice()) {
        composeWith(new ServerGenerator(context));
    }
}

// Dans SpringBootGenerator.java
private void composing() {
    // Toujours composés
    composeWith(new SecurityGenerator(context));
    composeWith(new ErrorHandlingGenerator(context));
    composeWith(new CacheGenerator(context));
    // ...

    // Conditionnels
    if (isSql()) {
        composeWith(new SpringDataRelationalGenerator(context));
        composeWith(new LiquibaseGenerator(context));
    }

    if ("elasticsearch".equals(config.getSearchEngine())) {
        composeWith(new SpringDataElasticsearchGenerator(context));
    }

    if (config.getMessageBroker() != null && !"no".equals(config.getMessageBroker())) {
        composeWith(new SpringCloudStreamGenerator(context));
    }
}
```

---

## 7. Conditions d'Activation

### 7.1 Table des Conditions

| Générateur | Condition d'Activation |
|------------|----------------------|
| `SecurityGenerator` | Toujours |
| `ErrorHandlingGenerator` | Toujours |
| `AuditGenerator` | Toujours |
| `LoggingAspectGenerator` | Toujours |
| `CacheGenerator` | `cacheProvider != null && cacheProvider != "no"` |
| `SwaggerGenerator` | Toujours |
| `MetricsGenerator` | Toujours |
| `CodeQualityGenerator` | Toujours |
| `DockerGenerator` | Toujours |
| `DockerComposeGenerator` | Toujours |
| `JibGenerator` | Toujours |
| `SpringDataRelationalGenerator` | `databaseType == "sql"` |
| `LiquibaseGenerator` | `databaseType == "sql"` |
| `SpringDataElasticsearchGenerator` | `searchEngine == "elasticsearch"` |
| `SpringCloudStreamGenerator` | `messageBroker != null && messageBroker != "no"` |
| `SpringWebSocketGenerator` | `websocket == "spring-websocket"` |
| `GatewayGenerator` | `applicationType == "gateway"` |
| `UserManagementGenerator` | `!skipUserManagement` |
| `FeignClientGenerator` | `feignClient == true && isMicroservice() && !reactive` |
| `TestInfrastructureGenerator` | Toujours |
| `CiCdGenerator` | Toujours |

### 7.2 Conditions Détaillées par Générateur

#### CacheGenerator
```java
private void writing() throws Exception {
    String cacheProvider = config.getCacheProvider();

    if (cacheProvider == null || "no".equals(cacheProvider)) {
        log.info("No cache provider configured, skipping");
        return;
    }

    switch (cacheProvider) {
        case "ehcache":   writeEhcacheConfiguration();    break;
        case "caffeine":  writeCaffeineConfiguration();   break;
        case "redis":     writeRedisConfiguration();      break;
        case "hazelcast": writeHazelcastConfiguration();  break;
    }
}
```

#### DockerComposeGenerator
```java
private void writing() throws Exception {
    writeDockerCompose();      // Toujours
    writeDockerComposeDev();   // Toujours
    writeDockerComposeProd();  // Toujours

    if (isSql()) {
        writeDatabaseDockerCompose();  // PostgreSQL ou MySQL
    }

    if (config.hasServiceDiscovery()) {
        writeServiceDiscoveryDockerCompose();  // Consul
    }

    if ("kafka".equals(config.getMessageBroker())) {
        writeKafkaDockerCompose();
    }

    if ("elasticsearch".equals(config.getSearchEngine())) {
        writeElasticsearchDockerCompose();
    }
}
```

#### CiCdGenerator
```java
private void writing() throws Exception {
    String ciCdType = config.getCiCd();

    if (ciCdType == null || "none".equals(ciCdType)) {
        writeGitHubActions();  // Défaut
    } else {
        switch (ciCdType) {
            case "github":  writeGitHubActions();    break;
            case "gitlab":  writeGitLabCi();         break;
            case "jenkins": writeJenkinsfile();      break;
            case "azure":   writeAzurePipelines();   break;
            case "circle":  writeCircleCi();         break;
            case "travis":  writeTravisCi();         break;
            default:        writeGitHubActions();
        }
    }
}
```

---

## 8. Fichiers Générés

### 8.1 Structure du Projet Généré

```
myapp/
├── .github/
│   ├── workflows/
│   │   └── main.yml                    # GitHub Actions CI/CD
│   └── dependabot.yml                  # Mises à jour automatiques
├── .jhipster/
│   └── *.json                          # Configuration des entités
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties    # Configuration Maven wrapper
├── src/
│   ├── main/
│   │   ├── docker/
│   │   │   ├── app.yml                 # Docker Compose app
│   │   │   ├── dev.yml                 # Docker Compose dev
│   │   │   ├── prod.yml                # Docker Compose prod
│   │   │   ├── postgresql.yml          # (si PostgreSQL)
│   │   │   ├── mysql.yml               # (si MySQL)
│   │   │   ├── consul.yml              # (si Consul)
│   │   │   ├── kafka.yml               # (si Kafka)
│   │   │   ├── elasticsearch.yml       # (si Elasticsearch)
│   │   │   ├── jib/
│   │   │   │   └── entrypoint.sh
│   │   │   └── Dockerfile
│   │   ├── java/
│   │   │   └── com/example/myapp/
│   │   │       ├── MyappApp.java               # Classe principale
│   │   │       ├── config/
│   │   │       │   ├── ApplicationProperties.java
│   │   │       │   ├── AsyncConfiguration.java
│   │   │       │   ├── CacheConfiguration.java
│   │   │       │   ├── DatabaseConfiguration.java
│   │   │       │   ├── JacksonConfiguration.java
│   │   │       │   ├── LoggingAspectConfiguration.java
│   │   │       │   ├── OpenApiConfiguration.java
│   │   │       │   ├── SecurityConfiguration.java
│   │   │       │   └── WebConfigurer.java
│   │   │       ├── domain/
│   │   │       │   ├── AbstractAuditingEntity.java
│   │   │       │   ├── Authority.java
│   │   │       │   ├── PersistentAuditEvent.java
│   │   │       │   └── User.java
│   │   │       ├── repository/
│   │   │       │   ├── AuthorityRepository.java
│   │   │       │   ├── PersistenceAuditEventRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       ├── security/
│   │   │       │   ├── jwt/
│   │   │       │   │   ├── JwtFilter.java
│   │   │       │   │   └── TokenProvider.java
│   │   │       │   ├── AuthoritiesConstants.java
│   │   │       │   ├── SecurityUtils.java
│   │   │       │   └── SpringSecurityAuditorAware.java
│   │   │       ├── service/
│   │   │       │   ├── dto/
│   │   │       │   │   ├── AdminUserDTO.java
│   │   │       │   │   └── UserDTO.java
│   │   │       │   ├── mapper/
│   │   │       │   │   └── UserMapper.java
│   │   │       │   └── UserService.java
│   │   │       ├── web/
│   │   │       │   ├── rest/
│   │   │       │   │   ├── errors/
│   │   │       │   │   │   ├── BadRequestAlertException.java
│   │   │       │   │   │   ├── ErrorConstants.java
│   │   │       │   │   │   ├── ExceptionTranslator.java
│   │   │       │   │   │   └── FieldErrorVM.java
│   │   │       │   │   ├── vm/
│   │   │       │   │   │   ├── KeyAndPasswordVM.java
│   │   │       │   │   │   ├── LoginVM.java
│   │   │       │   │   │   └── ManagedUserVM.java
│   │   │       │   │   ├── AccountResource.java
│   │   │       │   │   └── UserResource.java
│   │   │       │   └── filter/
│   │   │       │       └── SpaWebFilter.java
│   │   │       └── aop/
│   │   │           └── logging/
│   │   │               └── LoggingAspect.java
│   │   └── resources/
│   │       ├── config/
│   │       │   ├── liquibase/
│   │       │   │   ├── changelog/
│   │       │   │   │   └── 00000000000000_initial_schema.xml
│   │       │   │   └── master.xml
│   │       │   ├── application.yml
│   │       │   ├── application-dev.yml
│   │       │   └── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/
│       └── java/
│           └── com/example/myapp/
│               ├── IntegrationTest.java
│               ├── TestUtil.java
│               └── config/
│                   └── TestSecurityConfiguration.java
├── .editorconfig
├── .gitattributes
├── .gitignore
├── .prettierignore
├── .prettierrc
├── .yo-rc.json                         # Configuration JHipster
├── checkstyle.xml
├── jib.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── sonar-project.properties
```

### 8.2 Fichiers par Générateur

#### AppGenerator
- `.yo-rc.json` - Configuration JHipster

#### CommonGenerator
- `.editorconfig` - Configuration éditeur
- `.prettierrc` - Configuration Prettier
- `.prettierignore` - Fichiers ignorés par Prettier
- `README.md` - Documentation du projet
- `sonar-project.properties` - Configuration SonarQube
- `checkstyle.xml` - Règles Checkstyle

#### GitGenerator
- `.gitignore` - Fichiers ignorés par Git
- `.gitattributes` - Attributs Git

#### MavenGenerator
- `mvnw` - Maven wrapper (Unix)
- `mvnw.cmd` - Maven wrapper (Windows)
- `.mvn/wrapper/maven-wrapper.properties`

#### SpringBootGenerator
- `pom.xml` - Configuration Maven
- `src/main/java/.../MyappApp.java` - Classe principale
- `src/main/resources/config/application.yml`
- `src/main/resources/config/application-dev.yml`
- `src/main/resources/config/application-prod.yml`
- `src/main/resources/logback-spring.xml`
- Configuration classes (config/*.java)

#### SecurityGenerator
- `security/jwt/TokenProvider.java`
- `security/jwt/JwtFilter.java`
- `security/AuthoritiesConstants.java`
- `security/SecurityUtils.java`

#### ErrorHandlingGenerator
- `web/rest/errors/BadRequestAlertException.java`
- `web/rest/errors/ErrorConstants.java`
- `web/rest/errors/ExceptionTranslator.java`
- `web/rest/errors/FieldErrorVM.java`

#### AuditGenerator
- `domain/AbstractAuditingEntity.java`
- `domain/PersistentAuditEvent.java`
- `security/SpringSecurityAuditorAware.java`
- `repository/PersistenceAuditEventRepository.java`

#### UserManagementGenerator
- `domain/User.java`
- `domain/Authority.java`
- `repository/UserRepository.java`
- `repository/AuthorityRepository.java`
- `service/UserService.java`
- `service/dto/AdminUserDTO.java`
- `service/dto/UserDTO.java`
- `service/mapper/UserMapper.java`
- `web/rest/AccountResource.java`
- `web/rest/UserResource.java`
- `web/rest/vm/LoginVM.java`
- `web/rest/vm/ManagedUserVM.java`

#### LiquibaseGenerator
- `resources/config/liquibase/master.xml`
- `resources/config/liquibase/changelog/00000000000000_initial_schema.xml`

#### CacheGenerator
- `config/CacheConfiguration.java`
- `resources/config/ehcache/ehcache.xml` (si Ehcache)

#### DockerGenerator
- `src/main/docker/Dockerfile`

#### DockerComposeGenerator
- `src/main/docker/app.yml`
- `src/main/docker/dev.yml`
- `src/main/docker/prod.yml`
- `src/main/docker/postgresql.yml` (si PostgreSQL)
- `src/main/docker/mysql.yml` (si MySQL)
- `src/main/docker/consul.yml` (si Consul)
- `src/main/docker/kafka.yml` (si Kafka)
- `src/main/docker/elasticsearch.yml` (si Elasticsearch)

#### JibGenerator
- `jib.yml`
- `src/main/docker/jib/entrypoint.sh`

#### CiCdGenerator
- `.github/workflows/main.yml` (GitHub Actions)
- `.github/dependabot.yml`
- `.gitlab-ci.yml` (GitLab CI)
- `Jenkinsfile` (Jenkins)
- `azure-pipelines.yml` (Azure)
- `.circleci/config.yml` (CircleCI)
- `.travis.yml` (Travis CI)

#### SwaggerGenerator
- `config/OpenApiConfiguration.java`

#### MetricsGenerator
- `config/MetricsConfiguration.java`
- `service/metrics/CustomMetricsService.java`
- `web/rest/vm/metrics/ApplicationHealthIndicator.java`

#### CodeQualityGenerator
- `spotbugs-exclude.xml`
- `pmd-ruleset.xml`
- Configuration JaCoCo dans pom.xml

#### TestInfrastructureGenerator
- `test/java/.../IntegrationTest.java`
- `test/java/.../TestUtil.java`
- `test/java/.../config/TestSecurityConfiguration.java`

---

## 9. Diagrammes

### 9.1 Diagramme de Classes Simplifié

```
                    ┌─────────────────┐
                    │  BaseGenerator  │
                    │    (abstract)   │
                    ├─────────────────┤
                    │ - context       │
                    │ - tasks         │
                    ├─────────────────┤
                    │ + run()         │
                    │ + composeWith() │
                    │ + dependsOn()   │
                    │ + writeFile()   │
                    └────────┬────────┘
                             │
                             │ extends
                             ▼
              ┌──────────────────────────────┐
              │  BaseApplicationGenerator    │
              │         (abstract)           │
              ├──────────────────────────────┤
              │ + configuringEachEntity()    │
              │ + preparingEachEntity()      │
              │ + writingEntity()            │
              │ + isMicroservice()           │
              │ + isSql()                    │
              │ + isJwt()                    │
              └──────────────┬───────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ AppGenerator│      │SpringBoot  │      │ Domain      │
│             │      │Generator   │      │ Generator   │
└─────────────┘      └─────────────┘      └─────────────┘
```

### 9.2 Diagramme de Séquence (Génération)

```
User              AppGenerator         ServerGenerator      SpringBootGenerator
  │                    │                     │                      │
  │ run()              │                     │                      │
  │───────────────────>│                     │                      │
  │                    │ beforeQueue()       │                      │
  │                    │──────────┐          │                      │
  │                    │          │          │                      │
  │                    │<─────────┘          │                      │
  │                    │                     │                      │
  │                    │ registerTasks()     │                      │
  │                    │──────────┐          │                      │
  │                    │          │          │                      │
  │                    │<─────────┘          │                      │
  │                    │                     │                      │
  │                    │ composing()         │                      │
  │                    │──────────┐          │                      │
  │                    │          │          │                      │
  │                    │   composeWith()     │                      │
  │                    │────────────────────>│                      │
  │                    │                     │ composeWith()        │
  │                    │                     │─────────────────────>│
  │                    │                     │                      │
  │                    │                     │    composing()       │
  │                    │                     │                      │──┐
  │                    │                     │                      │  │
  │                    │                     │                      │<─┘
  │                    │                     │                      │
  │                    │ Execute all tasks (by priority order)      │
  │                    │════════════════════════════════════════════│
  │                    │                     │                      │
  │ Generation complete│                     │                      │
  │<───────────────────│                     │                      │
```

### 9.3 Diagramme de Décision (Composition)

```
                              ┌─────────────┐
                              │ AppGenerator│
                              │   start     │
                              └──────┬──────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │ composeWith(Common)   │
                         │ composeWith(Maven)    │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │  !skipClient OR       │
                         │  isMicroservice() ?   │
                         └───────────┬───────────┘
                                     │
                        ┌────────────┴────────────┐
                        │ Yes                     │ No
                        ▼                         ▼
           ┌─────────────────────┐         ┌──────────┐
           │composeWith(Server)  │         │   Skip   │
           └──────────┬──────────┘         └──────────┘
                      │
                      ▼
           ┌─────────────────────┐
           │ SpringBootGenerator │
           │      composing      │
           └──────────┬──────────┘
                      │
        ┌─────────────┼─────────────┬─────────────┐
        ▼             ▼             ▼             ▼
   ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
   │ isSql() │  │searchEng │  │msgBroker │  │websocket │
   │    ?    │  │  == ES?  │  │ != no?   │  │ == ws?   │
   └────┬────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘
        │            │             │             │
   ┌────┴────┐  ┌────┴────┐  ┌────┴────┐  ┌────┴────┐
   │Yes  No  │  │Yes  No  │  │Yes  No  │  │Yes  No  │
   ▼         ▼  ▼         ▼  ▼         ▼  ▼         ▼
┌─────┐   Skip ┌────┐  Skip ┌─────┐ Skip ┌────┐  Skip
│JPA  │        │ES  │       │Kafka│      │WS  │
│Liq  │        │Gen │       │Gen  │      │Gen │
└─────┘        └────┘       └─────┘      └────┘
```

---

## Annexes

### A. Commandes d'Exécution

```bash
# Génération d'une nouvelle application
java -jar jhipster-generator.jar generate

# Génération avec configuration existante
java -jar jhipster-generator.jar generate --config /path/to/.yo-rc.json

# Génération d'une entité
java -jar jhipster-generator.jar entity MyEntity

# Mise à jour de la configuration
java -jar jhipster-generator.jar upgrade
```

### B. Variables d'Environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `JHIPSTER_BASE_NAME` | Nom de l'application | - |
| `JHIPSTER_SERVER_PORT` | Port du serveur | 8080 |
| `SPRING_PROFILES_ACTIVE` | Profils Spring actifs | dev |
| `JAVA_OPTS` | Options JVM | - |

### C. Références

- [JHipster Documentation](https://www.jhipster.tech/documentation-archive/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Liquibase Documentation](https://docs.liquibase.com/)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)

---

**Version:** 1.0.0
**Date:** Janvier 2025
**Auteur:** JHipster Java Generator Team
