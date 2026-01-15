/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.user;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * User Management Generator.
 * Generates User entity, Authority entity, repositories, services, and REST resources.
 */
public class UserManagementGenerator extends BaseApplicationGenerator {

    public UserManagementGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "user-management";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingUserManagement", this::writing);
    }

    private void writing() throws Exception {
        JHipsterConfig config = getConfig();

        if (config.isSkipUserManagement()) {
            log.info("Skipping user management generation");
            return;
        }

        log.info("Writing user management");

        writeUserEntity();
        writeAuthorityEntity();
        writeUserRepository();
        writeAuthorityRepository();
        writeUserService();
        writeUserDTO();
        writeAdminUserDTO();
        writeUserMapper();
        writeUserResource();
        writeAccountResource();
        writePasswordChangeDTO();
        writeKeyAndPasswordVM();
        writeManagedUserVM();
        writeLoginVM();
    }

    private void writeUserEntity() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "com.fasterxml.jackson.annotation.JsonIgnore",
            "jakarta.persistence.*",
            "jakarta.validation.constraints.Email",
            "jakarta.validation.constraints.NotNull",
            "jakarta.validation.constraints.Pattern",
            "jakarta.validation.constraints.Size",
            "org.hibernate.annotations.BatchSize",
            "org.hibernate.annotations.Cache",
            "org.hibernate.annotations.CacheConcurrencyStrategy",
            "java.io.Serializable",
            "java.time.Instant",
            "java.util.HashSet",
            "java.util.Set"
        );

        builder.javadoc("A user entity.");
        builder.annotation("Entity");
        builder.annotation("Table", "name = \"jhi_user\"");
        builder.annotation("Cache", "usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE");

        builder.classDeclaration("public", "User", "AbstractAuditingEntity<Long>", "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.annotation("Id");
        builder.annotation("GeneratedValue", "strategy = GenerationType.SEQUENCE, generator = \"sequenceGenerator\"");
        builder.annotation("SequenceGenerator", "name = \"sequenceGenerator\"");
        builder.field("private", "Long", "id");
        builder.line();

        builder.annotation("NotNull");
        builder.annotation("Pattern", "regexp = \"^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$\"");
        builder.annotation("Size", "min = 1, max = 50");
        builder.annotation("Column", "length = 50, unique = true, nullable = false");
        builder.field("private", "String", "login");
        builder.line();

        builder.annotation("JsonIgnore");
        builder.annotation("NotNull");
        builder.annotation("Size", "min = 60, max = 60");
        builder.annotation("Column", "name = \"password_hash\", length = 60, nullable = false");
        builder.field("private", "String", "password");
        builder.line();

        builder.annotation("Size", "max = 50");
        builder.annotation("Column", "name = \"first_name\", length = 50");
        builder.field("private", "String", "firstName");
        builder.line();

        builder.annotation("Size", "max = 50");
        builder.annotation("Column", "name = \"last_name\", length = 50");
        builder.field("private", "String", "lastName");
        builder.line();

        builder.annotation("Email");
        builder.annotation("Size", "min = 5, max = 254");
        builder.annotation("Column", "length = 254, unique = true");
        builder.field("private", "String", "email");
        builder.line();

        builder.annotation("NotNull");
        builder.annotation("Column", "nullable = false");
        builder.field("private boolean", "activated", "false");
        builder.line();

        builder.annotation("Size", "min = 2, max = 10");
        builder.annotation("Column", "name = \"lang_key\", length = 10");
        builder.field("private", "String", "langKey");
        builder.line();

        builder.annotation("Size", "max = 256");
        builder.annotation("Column", "name = \"image_url\", length = 256");
        builder.field("private", "String", "imageUrl");
        builder.line();

        builder.annotation("Size", "max = 20");
        builder.annotation("Column", "name = \"activation_key\", length = 20");
        builder.annotation("JsonIgnore");
        builder.field("private", "String", "activationKey");
        builder.line();

        builder.annotation("Size", "max = 20");
        builder.annotation("Column", "name = \"reset_key\", length = 20");
        builder.annotation("JsonIgnore");
        builder.field("private", "String", "resetKey");
        builder.line();

        builder.annotation("Column", "name = \"reset_date\"");
        builder.field("private", "Instant", "resetDate", "null");
        builder.line();

        builder.annotation("JsonIgnore");
        builder.annotation("ManyToMany");
        builder.annotation("JoinTable", "name = \"jhi_user_authority\",\n" +
            "        joinColumns = {@JoinColumn(name = \"user_id\", referencedColumnName = \"id\")},\n" +
            "        inverseJoinColumns = {@JoinColumn(name = \"authority_name\", referencedColumnName = \"name\")}");
        builder.annotation("Cache", "usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE");
        builder.annotation("BatchSize", "size = 20");
        builder.field("private", "Set<Authority>", "authorities", "new HashSet<>()");
        builder.line();

        // Getters and Setters
        builder.annotation("Override");
        builder.methodSignature("public", "Long", "getId");
        builder.returnStatement("id");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setId", "Long id");
        builder.statement("this.id = id");
        builder.closeMethod();
        builder.line();

        // Other getters/setters
        String[] fields = {"login", "password", "firstName", "lastName", "email", "langKey", "imageUrl", "activationKey", "resetKey"};
        for (String field : fields) {
            String capitalizedField = capitalize(field);
            builder.methodSignature("public", "String", "get" + capitalizedField);
            builder.returnStatement(field);
            builder.closeMethod();
            builder.line();

            builder.methodSignature("public", "void", "set" + capitalizedField, "String " + field);
            builder.statement("this." + field + " = " + field);
            builder.closeMethod();
            builder.line();
        }

        // Boolean getter/setter
        builder.methodSignature("public", "boolean", "isActivated");
        builder.returnStatement("activated");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setActivated", "boolean activated");
        builder.statement("this.activated = activated");
        builder.closeMethod();
        builder.line();

        // Instant getter/setter
        builder.methodSignature("public", "Instant", "getResetDate");
        builder.returnStatement("resetDate");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setResetDate", "Instant resetDate");
        builder.statement("this.resetDate = resetDate");
        builder.closeMethod();
        builder.line();

        // Authorities getter/setter
        builder.methodSignature("public", "Set<Authority>", "getAuthorities");
        builder.returnStatement("authorities");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setAuthorities", "Set<Authority> authorities");
        builder.statement("this.authorities = authorities");
        builder.closeMethod();
        builder.line();

        // equals, hashCode, toString
        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.ifStatement("this == o");
        builder.returnStatement("true");
        builder.closeIf();
        builder.ifStatement("!(o instanceof User)");
        builder.returnStatement("false");
        builder.closeIf();
        builder.returnStatement("id != null && id.equals(((User) o).id)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("getClass().hashCode()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"User{\" +\n" +
            "                \"login='\" + login + '\\'' +\n" +
            "                \", firstName='\" + firstName + '\\'' +\n" +
            "                \", lastName='\" + lastName + '\\'' +\n" +
            "                \", email='\" + email + '\\'' +\n" +
            "                \", activated='\" + activated + '\\'' +\n" +
            "                \", langKey='\" + langKey + '\\'' +\n" +
            "                \", imageUrl='\" + imageUrl + '\\'' +\n" +
            "                \"}\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/User.java", builder.build());
    }

    private void writeAuthorityEntity() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "jakarta.persistence.Column",
            "jakarta.persistence.Entity",
            "jakarta.persistence.Id",
            "jakarta.persistence.Table",
            "jakarta.validation.constraints.NotNull",
            "jakarta.validation.constraints.Size",
            "org.hibernate.annotations.Cache",
            "org.hibernate.annotations.CacheConcurrencyStrategy",
            "java.io.Serializable",
            "java.util.Objects"
        );

        builder.javadoc("An authority (a security role) used by Spring Security.");
        builder.annotation("Entity");
        builder.annotation("Table", "name = \"jhi_authority\"");
        builder.annotation("Cache", "usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE");

        builder.classDeclaration("public", "Authority", null, "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.annotation("NotNull");
        builder.annotation("Size", "max = 50");
        builder.annotation("Id");
        builder.annotation("Column", "length = 50");
        builder.field("private", "String", "name");
        builder.line();

        builder.constructor("public", "Authority");
        builder.closeMethod();
        builder.line();

        builder.constructor("public", "Authority", "String name");
        builder.statement("this.name = name");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getName");
        builder.returnStatement("name");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setName", "String name");
        builder.statement("this.name = name");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.ifStatement("this == o");
        builder.returnStatement("true");
        builder.closeIf();
        builder.ifStatement("!(o instanceof Authority)");
        builder.returnStatement("false");
        builder.closeIf();
        builder.returnStatement("Objects.equals(name, ((Authority) o).name)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("Objects.hashCode(name)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"Authority{name='\" + name + \"'}\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/Authority.java", builder.build());
    }

    private void writeUserRepository() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain.User",
            "org.springframework.cache.annotation.Cacheable",
            "org.springframework.data.domain.Page",
            "org.springframework.data.domain.Pageable",
            "org.springframework.data.jpa.repository.EntityGraph",
            "org.springframework.data.jpa.repository.JpaRepository",
            "org.springframework.stereotype.Repository",
            "java.time.Instant",
            "java.util.List",
            "java.util.Optional"
        );

        builder.javadoc("Spring Data JPA repository for the {@link User} entity.");
        builder.annotation("Repository");

        builder.line("public interface UserRepository extends JpaRepository<User, Long> {");
        builder.indent();
        builder.line();
        builder.line("String USERS_BY_LOGIN_CACHE = \"usersByLogin\";");
        builder.line("String USERS_BY_EMAIL_CACHE = \"usersByEmail\";");
        builder.line();
        builder.line("Optional<User> findOneByActivationKey(String activationKey);");
        builder.line();
        builder.line("List<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);");
        builder.line();
        builder.line("Optional<User> findOneByResetKey(String resetKey);");
        builder.line();
        builder.line("Optional<User> findOneByEmailIgnoreCase(String email);");
        builder.line();
        builder.line("Optional<User> findOneByLogin(String login);");
        builder.line();
        builder.annotation("EntityGraph", "attributePaths = \"authorities\"");
        builder.annotation("Cacheable", "cacheNames = USERS_BY_LOGIN_CACHE");
        builder.line("Optional<User> findOneWithAuthoritiesByLogin(String login);");
        builder.line();
        builder.annotation("EntityGraph", "attributePaths = \"authorities\"");
        builder.annotation("Cacheable", "cacheNames = USERS_BY_EMAIL_CACHE");
        builder.line("Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);");
        builder.line();
        builder.line("Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);");
        builder.outdent();
        builder.line("}");

        writeFile(getMainJavaPath() + "repository/UserRepository.java", builder.build());
    }

    private void writeAuthorityRepository() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain.Authority",
            "org.springframework.data.jpa.repository.JpaRepository",
            "org.springframework.stereotype.Repository"
        );

        builder.javadoc("Spring Data JPA repository for the {@link Authority} entity.");
        builder.annotation("Repository");

        builder.line("public interface AuthorityRepository extends JpaRepository<Authority, String> {");
        builder.line("}");

        writeFile(getMainJavaPath() + "repository/AuthorityRepository.java", builder.build());
    }

    private void writeUserService() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service");

        builder.addImports(
            config.getPackageName() + ".domain.Authority",
            config.getPackageName() + ".domain.User",
            config.getPackageName() + ".repository.AuthorityRepository",
            config.getPackageName() + ".repository.UserRepository",
            config.getPackageName() + ".security.AuthoritiesConstants",
            config.getPackageName() + ".security.SecurityUtils",
            config.getPackageName() + ".service.dto.AdminUserDTO",
            config.getPackageName() + ".service.dto.UserDTO",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.cache.CacheManager",
            "org.springframework.data.domain.Page",
            "org.springframework.data.domain.Pageable",
            "org.springframework.scheduling.annotation.Scheduled",
            "org.springframework.security.crypto.password.PasswordEncoder",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional",
            "tech.jhipster.security.RandomUtil",
            "java.time.Instant",
            "java.time.temporal.ChronoUnit",
            "java.util.*",
            "java.util.stream.Collectors"
        );

        builder.javadoc("Service class for managing users.");
        builder.annotation("Service");
        builder.annotation("Transactional");

        builder.classDeclaration("public", "UserService", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(UserService.class)");
        builder.line();
        builder.field("private final", "UserRepository", "userRepository");
        builder.field("private final", "PasswordEncoder", "passwordEncoder");
        builder.field("private final", "AuthorityRepository", "authorityRepository");
        builder.field("private final", "CacheManager", "cacheManager");
        builder.line();

        builder.constructor("public", "UserService",
            "UserRepository userRepository",
            "PasswordEncoder passwordEncoder",
            "AuthorityRepository authorityRepository",
            "CacheManager cacheManager");
        builder.statement("this.userRepository = userRepository");
        builder.statement("this.passwordEncoder = passwordEncoder");
        builder.statement("this.authorityRepository = authorityRepository");
        builder.statement("this.cacheManager = cacheManager");
        builder.closeMethod();
        builder.line();

        // activateRegistration
        builder.methodSignature("public", "Optional<User>", "activateRegistration", "String key");
        builder.statement("log.debug(\"Activating user for activation key {}\", key)");
        builder.returnStatement("userRepository.findOneByActivationKey(key)\n" +
            "            .map(user -> {\n" +
            "                user.setActivated(true);\n" +
            "                user.setActivationKey(null);\n" +
            "                this.clearUserCaches(user);\n" +
            "                log.debug(\"Activated user: {}\", user);\n" +
            "                return user;\n" +
            "            })");
        builder.closeMethod();
        builder.line();

        // completePasswordReset
        builder.methodSignature("public", "Optional<User>", "completePasswordReset", "String newPassword", "String key");
        builder.statement("log.debug(\"Reset user password for reset key {}\", key)");
        builder.returnStatement("userRepository.findOneByResetKey(key)\n" +
            "            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))\n" +
            "            .map(user -> {\n" +
            "                user.setPassword(passwordEncoder.encode(newPassword));\n" +
            "                user.setResetKey(null);\n" +
            "                user.setResetDate(null);\n" +
            "                this.clearUserCaches(user);\n" +
            "                return user;\n" +
            "            })");
        builder.closeMethod();
        builder.line();

        // requestPasswordReset
        builder.methodSignature("public", "Optional<User>", "requestPasswordReset", "String mail");
        builder.returnStatement("userRepository.findOneByEmailIgnoreCase(mail)\n" +
            "            .filter(User::isActivated)\n" +
            "            .map(user -> {\n" +
            "                user.setResetKey(RandomUtil.generateResetKey());\n" +
            "                user.setResetDate(Instant.now());\n" +
            "                this.clearUserCaches(user);\n" +
            "                return user;\n" +
            "            })");
        builder.closeMethod();
        builder.line();

        // registerUser
        builder.methodSignature("public", "User", "registerUser", "AdminUserDTO userDTO", "String password");
        builder.statement("userRepository.findOneByLogin(userDTO.getLogin().toLowerCase())\n" +
            "            .ifPresent(existingUser -> {\n" +
            "                boolean removed = removeNonActivatedUser(existingUser);\n" +
            "                if (!removed) {\n" +
            "                    throw new UsernameAlreadyUsedException();\n" +
            "                }\n" +
            "            })");
        builder.statement("userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())\n" +
            "            .ifPresent(existingUser -> {\n" +
            "                boolean removed = removeNonActivatedUser(existingUser);\n" +
            "                if (!removed) {\n" +
            "                    throw new EmailAlreadyUsedException();\n" +
            "                }\n" +
            "            })");
        builder.statement("User newUser = new User()");
        builder.statement("String encryptedPassword = passwordEncoder.encode(password)");
        builder.statement("newUser.setLogin(userDTO.getLogin().toLowerCase())");
        builder.statement("newUser.setPassword(encryptedPassword)");
        builder.statement("newUser.setFirstName(userDTO.getFirstName())");
        builder.statement("newUser.setLastName(userDTO.getLastName())");
        builder.ifStatement("userDTO.getEmail() != null");
        builder.statement("newUser.setEmail(userDTO.getEmail().toLowerCase())");
        builder.closeIf();
        builder.statement("newUser.setImageUrl(userDTO.getImageUrl())");
        builder.statement("newUser.setLangKey(userDTO.getLangKey())");
        builder.statement("newUser.setActivated(false)");
        builder.statement("newUser.setActivationKey(RandomUtil.generateActivationKey())");
        builder.statement("Set<Authority> authorities = new HashSet<>()");
        builder.statement("authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add)");
        builder.statement("newUser.setAuthorities(authorities)");
        builder.statement("userRepository.save(newUser)");
        builder.statement("this.clearUserCaches(newUser)");
        builder.statement("log.debug(\"Created Information for User: {}\", newUser)");
        builder.returnStatement("newUser");
        builder.closeMethod();
        builder.line();

        // removeNonActivatedUser
        builder.methodSignature("private", "boolean", "removeNonActivatedUser", "User existingUser");
        builder.ifStatement("existingUser.isActivated()");
        builder.returnStatement("false");
        builder.closeIf();
        builder.statement("userRepository.delete(existingUser)");
        builder.statement("userRepository.flush()");
        builder.statement("this.clearUserCaches(existingUser)");
        builder.returnStatement("true");
        builder.closeMethod();
        builder.line();

        // createUser
        builder.methodSignature("public", "User", "createUser", "AdminUserDTO userDTO");
        builder.statement("User user = new User()");
        builder.statement("user.setLogin(userDTO.getLogin().toLowerCase())");
        builder.statement("user.setFirstName(userDTO.getFirstName())");
        builder.statement("user.setLastName(userDTO.getLastName())");
        builder.ifStatement("userDTO.getEmail() != null");
        builder.statement("user.setEmail(userDTO.getEmail().toLowerCase())");
        builder.closeIf();
        builder.statement("user.setImageUrl(userDTO.getImageUrl())");
        builder.ifStatement("userDTO.getLangKey() == null");
        builder.statement("user.setLangKey(\"en\")");
        builder.elseStatement();
        builder.statement("user.setLangKey(userDTO.getLangKey())");
        builder.closeIf();
        builder.statement("String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword())");
        builder.statement("user.setPassword(encryptedPassword)");
        builder.statement("user.setResetKey(RandomUtil.generateResetKey())");
        builder.statement("user.setResetDate(Instant.now())");
        builder.statement("user.setActivated(true)");
        builder.ifStatement("userDTO.getAuthorities() != null");
        builder.statement("Set<Authority> authorities = userDTO.getAuthorities().stream()\n" +
            "                .map(authorityRepository::findById)\n" +
            "                .filter(Optional::isPresent)\n" +
            "                .map(Optional::get)\n" +
            "                .collect(Collectors.toSet())");
        builder.statement("user.setAuthorities(authorities)");
        builder.closeIf();
        builder.statement("userRepository.save(user)");
        builder.statement("this.clearUserCaches(user)");
        builder.statement("log.debug(\"Created Information for User: {}\", user)");
        builder.returnStatement("user");
        builder.closeMethod();
        builder.line();

        // updateUser
        builder.methodSignature("public", "Optional<AdminUserDTO>", "updateUser", "AdminUserDTO userDTO");
        builder.returnStatement("Optional.of(userRepository.findById(userDTO.getId()))\n" +
            "            .filter(Optional::isPresent)\n" +
            "            .map(Optional::get)\n" +
            "            .map(user -> {\n" +
            "                this.clearUserCaches(user);\n" +
            "                user.setLogin(userDTO.getLogin().toLowerCase());\n" +
            "                user.setFirstName(userDTO.getFirstName());\n" +
            "                user.setLastName(userDTO.getLastName());\n" +
            "                if (userDTO.getEmail() != null) {\n" +
            "                    user.setEmail(userDTO.getEmail().toLowerCase());\n" +
            "                }\n" +
            "                user.setImageUrl(userDTO.getImageUrl());\n" +
            "                user.setActivated(userDTO.isActivated());\n" +
            "                user.setLangKey(userDTO.getLangKey());\n" +
            "                Set<Authority> managedAuthorities = user.getAuthorities();\n" +
            "                managedAuthorities.clear();\n" +
            "                userDTO.getAuthorities().stream()\n" +
            "                    .map(authorityRepository::findById)\n" +
            "                    .filter(Optional::isPresent)\n" +
            "                    .map(Optional::get)\n" +
            "                    .forEach(managedAuthorities::add);\n" +
            "                this.clearUserCaches(user);\n" +
            "                log.debug(\"Changed Information for User: {}\", user);\n" +
            "                return user;\n" +
            "            })\n" +
            "            .map(AdminUserDTO::new)");
        builder.closeMethod();
        builder.line();

        // deleteUser
        builder.methodSignature("public", "void", "deleteUser", "String login");
        builder.statement("userRepository.findOneByLogin(login)\n" +
            "            .ifPresent(user -> {\n" +
            "                userRepository.delete(user);\n" +
            "                this.clearUserCaches(user);\n" +
            "                log.debug(\"Deleted User: {}\", user);\n" +
            "            })");
        builder.closeMethod();
        builder.line();

        // changePassword
        builder.methodSignature("public", "void", "changePassword", "String currentClearTextPassword", "String newPassword");
        builder.statement("SecurityUtils.getCurrentUserLogin()\n" +
            "            .flatMap(userRepository::findOneByLogin)\n" +
            "            .ifPresent(user -> {\n" +
            "                String currentEncryptedPassword = user.getPassword();\n" +
            "                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {\n" +
            "                    throw new InvalidPasswordException();\n" +
            "                }\n" +
            "                String encryptedPassword = passwordEncoder.encode(newPassword);\n" +
            "                user.setPassword(encryptedPassword);\n" +
            "                this.clearUserCaches(user);\n" +
            "                log.debug(\"Changed password for User: {}\", user);\n" +
            "            })");
        builder.closeMethod();
        builder.line();

        // getAllManagedUsers
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "Page<AdminUserDTO>", "getAllManagedUsers", "Pageable pageable");
        builder.returnStatement("userRepository.findAll(pageable).map(AdminUserDTO::new)");
        builder.closeMethod();
        builder.line();

        // getPublicUsers
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "Page<UserDTO>", "getAllPublicUsers", "Pageable pageable");
        builder.returnStatement("userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new)");
        builder.closeMethod();
        builder.line();

        // getUserWithAuthoritiesByLogin
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "Optional<User>", "getUserWithAuthoritiesByLogin", "String login");
        builder.returnStatement("userRepository.findOneWithAuthoritiesByLogin(login)");
        builder.closeMethod();
        builder.line();

        // getUserWithAuthorities
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "Optional<User>", "getUserWithAuthorities");
        builder.returnStatement("SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin)");
        builder.closeMethod();
        builder.line();

        // removeNotActivatedUsers (scheduled)
        builder.javadoc("Not activated users should be automatically deleted after 3 days.\\n" +
            "This is scheduled to get fired everyday, at 01:00 (am).");
        builder.annotation("Scheduled", "cron = \"0 0 1 * * ?\"");
        builder.methodSignature("public", "void", "removeNotActivatedUsers");
        builder.statement("userRepository\n" +
            "            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))\n" +
            "            .forEach(user -> {\n" +
            "                log.debug(\"Deleting not activated user {}\", user.getLogin());\n" +
            "                userRepository.delete(user);\n" +
            "                this.clearUserCaches(user);\n" +
            "            })");
        builder.closeMethod();
        builder.line();

        // getAuthorities
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "List<String>", "getAuthorities");
        builder.returnStatement("authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList())");
        builder.closeMethod();
        builder.line();

        // clearUserCaches
        builder.methodSignature("private", "void", "clearUserCaches", "User user");
        builder.statement("Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin())");
        builder.ifStatement("user.getEmail() != null");
        builder.statement("Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail())");
        builder.closeIf();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/UserService.java", builder.build());
    }

    private void writeUserDTO() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.dto");

        builder.addImports(
            config.getPackageName() + ".domain.User",
            "java.io.Serializable"
        );

        builder.javadoc("A DTO representing a user, with only the public attributes.");

        builder.classDeclaration("public", "UserDTO", null, "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.field("private", "Long", "id");
        builder.field("private", "String", "login");
        builder.line();

        builder.constructor("public", "UserDTO");
        builder.closeMethod();
        builder.line();

        builder.constructor("public", "UserDTO", "User user");
        builder.statement("this.id = user.getId()");
        builder.statement("this.login = user.getLogin()");
        builder.closeMethod();
        builder.line();

        // Getters and setters
        builder.methodSignature("public", "Long", "getId");
        builder.returnStatement("id");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setId", "Long id");
        builder.statement("this.id = id");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getLogin");
        builder.returnStatement("login");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setLogin", "String login");
        builder.statement("this.login = login");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"UserDTO{\" + \"id=\" + id + \", login='\" + login + '\\'' + \"}\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/dto/UserDTO.java", builder.build());
    }

    private void writeAdminUserDTO() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.dto");

        builder.addImports(
            config.getPackageName() + ".domain.Authority",
            config.getPackageName() + ".domain.User",
            "jakarta.validation.constraints.Email",
            "jakarta.validation.constraints.NotBlank",
            "jakarta.validation.constraints.Pattern",
            "jakarta.validation.constraints.Size",
            "java.io.Serializable",
            "java.time.Instant",
            "java.util.Set",
            "java.util.stream.Collectors"
        );

        builder.javadoc("A DTO representing a user, with all attributes including authorities.");

        builder.classDeclaration("public", "AdminUserDTO", null, "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.field("private", "Long", "id");
        builder.line();

        builder.annotation("NotBlank");
        builder.annotation("Pattern", "regexp = \"^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$\"");
        builder.annotation("Size", "min = 1, max = 50");
        builder.field("private", "String", "login");
        builder.line();

        builder.annotation("Size", "max = 50");
        builder.field("private", "String", "firstName");
        builder.line();

        builder.annotation("Size", "max = 50");
        builder.field("private", "String", "lastName");
        builder.line();

        builder.annotation("Email");
        builder.annotation("Size", "min = 5, max = 254");
        builder.field("private", "String", "email");
        builder.line();

        builder.annotation("Size", "max = 256");
        builder.field("private", "String", "imageUrl");
        builder.line();

        builder.field("private boolean", "activated", "false");
        builder.line();

        builder.annotation("Size", "min = 2, max = 10");
        builder.field("private", "String", "langKey");
        builder.line();

        builder.field("private", "String", "createdBy");
        builder.field("private", "Instant", "createdDate");
        builder.field("private", "String", "lastModifiedBy");
        builder.field("private", "Instant", "lastModifiedDate");
        builder.field("private", "Set<String>", "authorities");
        builder.line();

        builder.constructor("public", "AdminUserDTO");
        builder.closeMethod();
        builder.line();

        builder.constructor("public", "AdminUserDTO", "User user");
        builder.statement("this.id = user.getId()");
        builder.statement("this.login = user.getLogin()");
        builder.statement("this.firstName = user.getFirstName()");
        builder.statement("this.lastName = user.getLastName()");
        builder.statement("this.email = user.getEmail()");
        builder.statement("this.activated = user.isActivated()");
        builder.statement("this.imageUrl = user.getImageUrl()");
        builder.statement("this.langKey = user.getLangKey()");
        builder.statement("this.createdBy = user.getCreatedBy()");
        builder.statement("this.createdDate = user.getCreatedDate()");
        builder.statement("this.lastModifiedBy = user.getLastModifiedBy()");
        builder.statement("this.lastModifiedDate = user.getLastModifiedDate()");
        builder.statement("this.authorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet())");
        builder.closeMethod();
        builder.line();

        // Getters and setters
        String[] fields = {"id:Long", "login:String", "firstName:String", "lastName:String", "email:String",
            "imageUrl:String", "langKey:String", "createdBy:String", "createdDate:Instant",
            "lastModifiedBy:String", "lastModifiedDate:Instant", "authorities:Set<String>"};

        for (String field : fields) {
            String[] parts = field.split(":");
            String fieldName = parts[0];
            String fieldType = parts[1];

            builder.methodSignature("public", fieldType, "get" + capitalize(fieldName));
            builder.returnStatement(fieldName);
            builder.closeMethod();
            builder.line();

            builder.methodSignature("public", "void", "set" + capitalize(fieldName), fieldType + " " + fieldName);
            builder.statement("this." + fieldName + " = " + fieldName);
            builder.closeMethod();
            builder.line();
        }

        // activated boolean getter/setter
        builder.methodSignature("public", "boolean", "isActivated");
        builder.returnStatement("activated");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setActivated", "boolean activated");
        builder.statement("this.activated = activated");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"AdminUserDTO{\" +\n" +
            "                \"login='\" + login + '\\'' +\n" +
            "                \", firstName='\" + firstName + '\\'' +\n" +
            "                \", lastName='\" + lastName + '\\'' +\n" +
            "                \", email='\" + email + '\\'' +\n" +
            "                \", imageUrl='\" + imageUrl + '\\'' +\n" +
            "                \", activated=\" + activated +\n" +
            "                \", langKey='\" + langKey + '\\'' +\n" +
            "                \", authorities=\" + authorities +\n" +
            "                \"}\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/dto/AdminUserDTO.java", builder.build());
    }

    private void writeUserMapper() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.mapper");

        builder.addImports(
            config.getPackageName() + ".domain.Authority",
            config.getPackageName() + ".domain.User",
            config.getPackageName() + ".service.dto.AdminUserDTO",
            config.getPackageName() + ".service.dto.UserDTO",
            "org.springframework.stereotype.Service",
            "java.util.*",
            "java.util.stream.Collectors"
        );

        builder.javadoc("Mapper for the entity {@link User} and its DTO called {@link UserDTO}.\\n\\n" +
            "Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct\\n" +
            "support is still in beta, and requires a manual step with an IDE.");
        builder.annotation("Service");

        builder.classDeclaration("public", "UserMapper", null);

        builder.methodSignature("public", "List<UserDTO>", "usersToUserDTOs", "List<User> users");
        builder.returnStatement("users.stream().filter(Objects::nonNull).map(this::userToUserDTO).collect(Collectors.toList())");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "UserDTO", "userToUserDTO", "User user");
        builder.returnStatement("new UserDTO(user)");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "List<AdminUserDTO>", "usersToAdminUserDTOs", "List<User> users");
        builder.returnStatement("users.stream().filter(Objects::nonNull).map(this::userToAdminUserDTO).collect(Collectors.toList())");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "AdminUserDTO", "userToAdminUserDTO", "User user");
        builder.returnStatement("new AdminUserDTO(user)");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "List<User>", "userDTOsToUsers", "List<AdminUserDTO> userDTOs");
        builder.returnStatement("userDTOs.stream().filter(Objects::nonNull).map(this::userDTOToUser).collect(Collectors.toList())");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "User", "userDTOToUser", "AdminUserDTO userDTO");
        builder.ifStatement("userDTO == null");
        builder.returnStatement("null");
        builder.closeIf();
        builder.statement("User user = new User()");
        builder.statement("user.setId(userDTO.getId())");
        builder.statement("user.setLogin(userDTO.getLogin())");
        builder.statement("user.setFirstName(userDTO.getFirstName())");
        builder.statement("user.setLastName(userDTO.getLastName())");
        builder.statement("user.setEmail(userDTO.getEmail())");
        builder.statement("user.setImageUrl(userDTO.getImageUrl())");
        builder.statement("user.setActivated(userDTO.isActivated())");
        builder.statement("user.setLangKey(userDTO.getLangKey())");
        builder.statement("Set<Authority> authorities = this.authoritiesFromStrings(userDTO.getAuthorities())");
        builder.statement("user.setAuthorities(authorities)");
        builder.returnStatement("user");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("private", "Set<Authority>", "authoritiesFromStrings", "Set<String> authoritiesAsString");
        builder.statement("Set<Authority> authorities = new HashSet<>()");
        builder.ifStatement("authoritiesAsString != null");
        builder.statement("authorities = authoritiesAsString.stream().map(string -> {\n" +
            "                Authority auth = new Authority();\n" +
            "                auth.setName(string);\n" +
            "                return auth;\n" +
            "            }).collect(Collectors.toSet())");
        builder.closeIf();
        builder.returnStatement("authorities");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/mapper/UserMapper.java", builder.build());
    }

    private void writeUserResource() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest");

        builder.addImports(
            config.getPackageName() + ".domain.User",
            config.getPackageName() + ".repository.UserRepository",
            config.getPackageName() + ".security.AuthoritiesConstants",
            config.getPackageName() + ".service.UserService",
            config.getPackageName() + ".service.dto.AdminUserDTO",
            config.getPackageName() + ".web.rest.errors.BadRequestAlertException",
            "jakarta.validation.Valid",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.beans.factory.annotation.Value",
            "org.springframework.data.domain.Page",
            "org.springframework.data.domain.Pageable",
            "org.springframework.http.HttpHeaders",
            "org.springframework.http.HttpStatus",
            "org.springframework.http.ResponseEntity",
            "org.springframework.security.access.prepost.PreAuthorize",
            "org.springframework.web.bind.annotation.*",
            "org.springframework.web.servlet.support.ServletUriComponentsBuilder",
            "tech.jhipster.web.util.HeaderUtil",
            "tech.jhipster.web.util.PaginationUtil",
            "tech.jhipster.web.util.ResponseUtil",
            "java.net.URI",
            "java.net.URISyntaxException",
            "java.util.*"
        );

        builder.javadoc("REST controller for managing users.\\n\\n" +
            "This class accesses the {@link User} entity, and needs to fetch its collection of authorities.");
        builder.annotation("RestController");
        builder.annotation("RequestMapping", "\"/api/admin\"");

        builder.classDeclaration("public", "UserResource", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(UserResource.class)");
        builder.line();

        builder.annotation("Value", "\"${jhipster.clientApp.name}\"");
        builder.field("private", "String", "applicationName");
        builder.line();

        builder.field("private final", "UserService", "userService");
        builder.field("private final", "UserRepository", "userRepository");
        builder.line();

        builder.constructor("public", "UserResource", "UserService userService", "UserRepository userRepository");
        builder.statement("this.userService = userService");
        builder.statement("this.userRepository = userRepository");
        builder.closeMethod();
        builder.line();

        // createUser
        builder.javadoc("Creates a new user.");
        builder.annotation("PostMapping", "\"/users\"");
        builder.annotation("PreAuthorize", "\"hasAuthority('\" + AuthoritiesConstants.ADMIN + \"')\"");
        builder.methodSignature("public", "ResponseEntity<User>", "createUser", "@Valid @RequestBody AdminUserDTO userDTO");
        builder.statement("throws URISyntaxException");
        builder.statement("log.debug(\"REST request to save User : {}\", userDTO)");
        builder.ifStatement("userDTO.getId() != null");
        builder.statement("throw new BadRequestAlertException(\"A new user cannot already have an ID\", \"userManagement\", \"idexists\")");
        builder.closeIf();
        builder.statement("User newUser = userService.createUser(userDTO)");
        builder.returnStatement("ResponseEntity.created(new URI(\"/api/admin/users/\" + newUser.getLogin()))\n" +
            "            .headers(HeaderUtil.createAlert(applicationName, \"userManagement.created\", newUser.getLogin()))\n" +
            "            .body(newUser)");
        builder.closeMethod();
        builder.line();

        // getAllUsers
        builder.javadoc("Gets all users with all the details.");
        builder.annotation("GetMapping", "\"/users\"");
        builder.annotation("PreAuthorize", "\"hasAuthority('\" + AuthoritiesConstants.ADMIN + \"')\"");
        builder.methodSignature("public", "ResponseEntity<List<AdminUserDTO>>", "getAllUsers", "Pageable pageable");
        builder.statement("log.debug(\"REST request to get all Users\")");
        builder.statement("final Page<AdminUserDTO> page = userService.getAllManagedUsers(pageable)");
        builder.statement("HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)");
        builder.returnStatement("new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK)");
        builder.closeMethod();
        builder.line();

        // getUser
        builder.javadoc("Gets a specific user by login.");
        builder.annotation("GetMapping", "\"/users/{login}\"");
        builder.annotation("PreAuthorize", "\"hasAuthority('\" + AuthoritiesConstants.ADMIN + \"')\"");
        builder.methodSignature("public", "ResponseEntity<AdminUserDTO>", "getUser", "@PathVariable String login");
        builder.statement("log.debug(\"REST request to get User : {}\", login)");
        builder.returnStatement("ResponseUtil.wrapOrNotFound(userService.getUserWithAuthoritiesByLogin(login).map(AdminUserDTO::new))");
        builder.closeMethod();
        builder.line();

        // updateUser
        builder.javadoc("Updates an existing user.");
        builder.annotation("PutMapping", "\"/users\"");
        builder.annotation("PreAuthorize", "\"hasAuthority('\" + AuthoritiesConstants.ADMIN + \"')\"");
        builder.methodSignature("public", "ResponseEntity<AdminUserDTO>", "updateUser", "@Valid @RequestBody AdminUserDTO userDTO");
        builder.statement("log.debug(\"REST request to update User : {}\", userDTO)");
        builder.statement("Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail())");
        builder.ifStatement("existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))");
        builder.statement("throw new BadRequestAlertException(\"Email is already in use\", \"userManagement\", \"emailexists\")");
        builder.closeIf();
        builder.statement("existingUser = userRepository.findOneByLogin(userDTO.getLogin().toLowerCase())");
        builder.ifStatement("existingUser.isPresent() && (!existingUser.get().getId().equals(userDTO.getId()))");
        builder.statement("throw new BadRequestAlertException(\"Login is already in use\", \"userManagement\", \"userexists\")");
        builder.closeIf();
        builder.statement("Optional<AdminUserDTO> updatedUser = userService.updateUser(userDTO)");
        builder.returnStatement("ResponseUtil.wrapOrNotFound(updatedUser,\n" +
            "            HeaderUtil.createAlert(applicationName, \"userManagement.updated\", userDTO.getLogin()))");
        builder.closeMethod();
        builder.line();

        // deleteUser
        builder.javadoc("Deletes a user.");
        builder.annotation("DeleteMapping", "\"/users/{login}\"");
        builder.annotation("PreAuthorize", "\"hasAuthority('\" + AuthoritiesConstants.ADMIN + \"')\"");
        builder.methodSignature("public", "ResponseEntity<Void>", "deleteUser", "@PathVariable String login");
        builder.statement("log.debug(\"REST request to delete User: {}\", login)");
        builder.statement("userService.deleteUser(login)");
        builder.returnStatement("ResponseEntity.noContent()\n" +
            "            .headers(HeaderUtil.createAlert(applicationName, \"userManagement.deleted\", login))\n" +
            "            .build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/UserResource.java", builder.build());
    }

    private void writeAccountResource() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest");

        builder.addImports(
            config.getPackageName() + ".service.UserService",
            config.getPackageName() + ".service.dto.AdminUserDTO",
            config.getPackageName() + ".web.rest.vm.KeyAndPasswordVM",
            config.getPackageName() + ".web.rest.vm.ManagedUserVM",
            "jakarta.validation.Valid",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.http.HttpStatus",
            "org.springframework.web.bind.annotation.*"
        );

        builder.javadoc("REST controller for managing the current user's account.");
        builder.annotation("RestController");
        builder.annotation("RequestMapping", "\"/api\"");

        builder.classDeclaration("public", "AccountResource", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(AccountResource.class)");
        builder.field("private final", "UserService", "userService");
        builder.line();

        builder.constructor("public", "AccountResource", "UserService userService");
        builder.statement("this.userService = userService");
        builder.closeMethod();
        builder.line();

        // registerAccount
        builder.javadoc("Registers a new user.");
        builder.annotation("PostMapping", "\"/register\"");
        builder.annotation("ResponseStatus", "HttpStatus.CREATED");
        builder.methodSignature("public", "void", "registerAccount", "@Valid @RequestBody ManagedUserVM managedUserVM");
        builder.statement("userService.registerUser(managedUserVM, managedUserVM.getPassword())");
        builder.closeMethod();
        builder.line();

        // activateAccount
        builder.javadoc("Activates a registered user.");
        builder.annotation("GetMapping", "\"/activate\"");
        builder.methodSignature("public", "void", "activateAccount", "@RequestParam(value = \"key\") String key");
        builder.statement("userService.activateRegistration(key)\n" +
            "            .orElseThrow(() -> new AccountResourceException(\"No user was found for this activation key\"))");
        builder.closeMethod();
        builder.line();

        // getAccount
        builder.javadoc("Gets the current user.");
        builder.annotation("GetMapping", "\"/account\"");
        builder.methodSignature("public", "AdminUserDTO", "getAccount");
        builder.returnStatement("userService.getUserWithAuthorities()\n" +
            "            .map(AdminUserDTO::new)\n" +
            "            .orElseThrow(() -> new AccountResourceException(\"User could not be found\"))");
        builder.closeMethod();
        builder.line();

        // saveAccount
        builder.javadoc("Updates the current user information.");
        builder.annotation("PostMapping", "\"/account\"");
        builder.methodSignature("public", "void", "saveAccount", "@Valid @RequestBody AdminUserDTO userDTO");
        builder.statement("// Implementation for updating current user");
        builder.closeMethod();
        builder.line();

        // changePassword
        builder.javadoc("Changes the current user's password.");
        builder.annotation("PostMapping", "\"/account/change-password\"");
        builder.methodSignature("public", "void", "changePassword", "@RequestBody PasswordChangeDTO passwordChangeDto");
        builder.statement("userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword())");
        builder.closeMethod();
        builder.line();

        // requestPasswordReset
        builder.javadoc("Send password reset email.");
        builder.annotation("PostMapping", "\"/account/reset-password/init\"");
        builder.methodSignature("public", "void", "requestPasswordReset", "@RequestBody String mail");
        builder.statement("userService.requestPasswordReset(mail)");
        builder.closeMethod();
        builder.line();

        // finishPasswordReset
        builder.javadoc("Finish password reset.");
        builder.annotation("PostMapping", "\"/account/reset-password/finish\"");
        builder.methodSignature("public", "void", "finishPasswordReset", "@RequestBody KeyAndPasswordVM keyAndPassword");
        builder.statement("userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())");
        builder.closeMethod();
        builder.line();

        // Inner exception class
        builder.line("private static class AccountResourceException extends RuntimeException {");
        builder.indent();
        builder.constructor("private", "AccountResourceException", "String message");
        builder.statement("super(message)");
        builder.closeMethod();
        builder.outdent();
        builder.line("}");

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/AccountResource.java", builder.build());
    }

    private void writePasswordChangeDTO() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest");

        builder.javadoc("A DTO representing a password change request.");

        builder.classDeclaration("public", "PasswordChangeDTO", null);

        builder.field("private", "String", "currentPassword");
        builder.field("private", "String", "newPassword");
        builder.line();

        builder.constructor("public", "PasswordChangeDTO");
        builder.closeMethod();
        builder.line();

        builder.constructor("public", "PasswordChangeDTO", "String currentPassword", "String newPassword");
        builder.statement("this.currentPassword = currentPassword");
        builder.statement("this.newPassword = newPassword");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getCurrentPassword");
        builder.returnStatement("currentPassword");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setCurrentPassword", "String currentPassword");
        builder.statement("this.currentPassword = currentPassword");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getNewPassword");
        builder.returnStatement("newPassword");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setNewPassword", "String newPassword");
        builder.statement("this.newPassword = newPassword");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/PasswordChangeDTO.java", builder.build());
    }

    private void writeKeyAndPasswordVM() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.vm");

        builder.javadoc("View Model object for storing the user's key and password.");

        builder.classDeclaration("public", "KeyAndPasswordVM", null);

        builder.field("private", "String", "key");
        builder.field("private", "String", "newPassword");
        builder.line();

        builder.methodSignature("public", "String", "getKey");
        builder.returnStatement("key");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setKey", "String key");
        builder.statement("this.key = key");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getNewPassword");
        builder.returnStatement("newPassword");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setNewPassword", "String newPassword");
        builder.statement("this.newPassword = newPassword");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/vm/KeyAndPasswordVM.java", builder.build());
    }

    private void writeManagedUserVM() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.vm");

        builder.addImports(
            config.getPackageName() + ".service.dto.AdminUserDTO",
            "jakarta.validation.constraints.Size"
        );

        builder.javadoc("View Model extending the AdminUserDTO, which is meant to be used in the user management UI.");

        builder.classDeclaration("public", "ManagedUserVM", "AdminUserDTO");

        builder.staticFinalField("int", "PASSWORD_MIN_LENGTH", "4");
        builder.staticFinalField("int", "PASSWORD_MAX_LENGTH", "100");
        builder.line();

        builder.annotation("Size", "min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH");
        builder.field("private", "String", "password");
        builder.line();

        builder.constructor("public", "ManagedUserVM");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getPassword");
        builder.returnStatement("password");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setPassword", "String password");
        builder.statement("this.password = password");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"ManagedUserVM{\" + super.toString() + \"}\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/vm/ManagedUserVM.java", builder.build());
    }

    private void writeLoginVM() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest.vm");

        builder.addImports(
            "jakarta.validation.constraints.NotNull",
            "jakarta.validation.constraints.Size"
        );

        builder.javadoc("View Model object for storing a user's credentials.");

        builder.classDeclaration("public", "LoginVM", null);

        builder.annotation("NotNull");
        builder.annotation("Size", "min = 1, max = 50");
        builder.field("private", "String", "username");
        builder.line();

        builder.annotation("NotNull");
        builder.annotation("Size", "min = 4, max = 100");
        builder.field("private", "String", "password");
        builder.line();

        builder.field("private boolean", "rememberMe");
        builder.line();

        builder.methodSignature("public", "String", "getUsername");
        builder.returnStatement("username");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setUsername", "String username");
        builder.statement("this.username = username");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getPassword");
        builder.returnStatement("password");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setPassword", "String password");
        builder.statement("this.password = password");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "boolean", "isRememberMe");
        builder.returnStatement("rememberMe");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setRememberMe", "boolean rememberMe");
        builder.statement("this.rememberMe = rememberMe");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"LoginVM{username='\" + username + '\\'' + \", rememberMe=\" + rememberMe + '}\"");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/vm/LoginVM.java", builder.build());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
