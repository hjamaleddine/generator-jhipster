/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.security;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Security Generator.
 * Generates security infrastructure including JWT/OAuth2 support.
 */
public class SecurityGenerator extends BaseApplicationGenerator {

    public SecurityGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "security";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingSecurity", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing security infrastructure");

        // Write common security classes
        writeAuthoritiesConstants();
        writeSecurityUtils();

        // Write authentication-specific classes
        if (getConfig().isJwt()) {
            writeJwtSecurity();
        } else if (getConfig().isOauth2()) {
            writeOAuth2Security();
        }

        // Write security configuration
        writeSecurityConfiguration();
    }

    private void writeAuthoritiesConstants() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".security");

        builder.javadoc("Constants for Spring Security authorities.");

        builder.classDeclaration("public final", "AuthoritiesConstants", null);

        builder.staticFinalField("String", "ADMIN", "\"ROLE_ADMIN\"");
        builder.staticFinalField("String", "USER", "\"ROLE_USER\"");
        builder.staticFinalField("String", "ANONYMOUS", "\"ROLE_ANONYMOUS\"");
        builder.line();

        builder.constructor("private", "AuthoritiesConstants");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "security/AuthoritiesConstants.java", builder.build());
    }

    private void writeSecurityUtils() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".security");

        builder.addImports(
            "org.springframework.security.core.Authentication",
            "org.springframework.security.core.GrantedAuthority",
            "org.springframework.security.core.context.SecurityContext",
            "org.springframework.security.core.context.SecurityContextHolder",
            "org.springframework.security.core.userdetails.UserDetails",
            "java.util.Arrays",
            "java.util.Optional",
            "java.util.stream.Stream"
        );

        if (config.isJwt()) {
            builder.addImport("org.springframework.security.oauth2.jwt.Jwt");
        }

        builder.javadoc("Utility class for Spring Security.");

        builder.classDeclaration("public final", "SecurityUtils", null);

        builder.constructor("private", "SecurityUtils");
        builder.closeMethod();
        builder.line();

        // getCurrentUserLogin
        builder.javadoc("Get the login of the current user.",
            "@return the login of the current user.");
        builder.methodSignature("public static", "Optional<String>", "getCurrentUserLogin");
        builder.statement("SecurityContext securityContext = SecurityContextHolder.getContext()");
        builder.returnStatement("Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()))");
        builder.closeMethod();
        builder.line();

        // extractPrincipal
        builder.methodSignature("private static", "String", "extractPrincipal", "Authentication authentication");
        builder.ifStatement("authentication == null");
        builder.returnStatement("null");
        builder.closeIf();

        if (config.isJwt()) {
            builder.ifStatement("authentication.getPrincipal() instanceof Jwt jwt");
            builder.returnStatement("jwt.getSubject()");
            builder.closeIf();
        }

        builder.ifStatement("authentication.getPrincipal() instanceof UserDetails springSecurityUser");
        builder.returnStatement("springSecurityUser.getUsername()");
        builder.closeIf();
        builder.ifStatement("authentication.getPrincipal() instanceof String s");
        builder.returnStatement("s");
        builder.closeIf();
        builder.returnStatement("null");
        builder.closeMethod();
        builder.line();

        // getCurrentUserJwt (for JWT only)
        if (config.isJwt()) {
            builder.javadoc("Get the JWT of the current user.",
                "@return the JWT of the current user.");
            builder.methodSignature("public static", "Optional<String>", "getCurrentUserJWT");
            builder.statement("SecurityContext securityContext = SecurityContextHolder.getContext()");
            builder.returnStatement("Optional.ofNullable(securityContext.getAuthentication())\n" +
                "            .filter(authentication -> authentication.getCredentials() instanceof String)\n" +
                "            .map(authentication -> (String) authentication.getCredentials())");
            builder.closeMethod();
            builder.line();
        }

        // isAuthenticated
        builder.javadoc("Check if a user is authenticated.",
            "@return true if the user is authenticated, false otherwise.");
        builder.methodSignature("public static", "boolean", "isAuthenticated");
        builder.statement("Authentication authentication = SecurityContextHolder.getContext().getAuthentication()");
        builder.returnStatement("authentication != null && getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANONYMOUS::equals)");
        builder.closeMethod();
        builder.line();

        // hasCurrentUserThisAuthority
        builder.javadoc("Checks if the current user has any of the authorities.",
            "@param authorities the authorities to check.",
            "@return true if the current user has any of the authorities, false otherwise.");
        builder.methodSignature("public static", "boolean", "hasCurrentUserAnyOfAuthorities", "String... authorities");
        builder.statement("Authentication authentication = SecurityContextHolder.getContext().getAuthentication()");
        builder.returnStatement("(authentication != null && getAuthorities(authentication)\n" +
            "            .anyMatch(authority -> Arrays.asList(authorities).contains(authority)))");
        builder.closeMethod();
        builder.line();

        // hasCurrentUserNoneOfAuthorities
        builder.javadoc("Checks if the current user has none of the authorities.",
            "@param authorities the authorities to check.",
            "@return true if the current user has none of the authorities, false otherwise.");
        builder.methodSignature("public static", "boolean", "hasCurrentUserNoneOfAuthorities", "String... authorities");
        builder.returnStatement("!hasCurrentUserAnyOfAuthorities(authorities)");
        builder.closeMethod();
        builder.line();

        // hasCurrentUserThisAuthority
        builder.javadoc("Checks if the current user has a specific authority.",
            "@param authority the authority to check.",
            "@return true if the current user has the authority, false otherwise.");
        builder.methodSignature("public static", "boolean", "hasCurrentUserThisAuthority", "String authority");
        builder.returnStatement("hasCurrentUserAnyOfAuthorities(authority)");
        builder.closeMethod();
        builder.line();

        // getAuthorities
        builder.methodSignature("private static", "Stream<String>", "getAuthorities", "Authentication authentication");
        builder.returnStatement("authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "security/SecurityUtils.java", builder.build());
    }

    private void writeJwtSecurity() throws Exception {
        writeTokenProvider();
        writeJwtAuthenticationConverter();
        writeSecurityJwtConfiguration();
    }

    private void writeTokenProvider() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".security.jwt");

        builder.addImports(
            "io.jsonwebtoken.*",
            "io.jsonwebtoken.io.Decoders",
            "io.jsonwebtoken.security.Keys",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
            "org.springframework.security.core.Authentication",
            "org.springframework.security.core.GrantedAuthority",
            "org.springframework.security.core.authority.SimpleGrantedAuthority",
            "org.springframework.security.core.userdetails.User",
            "org.springframework.stereotype.Component",
            config.getPackageName() + ".config.ApplicationProperties",
            "javax.crypto.SecretKey",
            "java.util.*",
            "java.util.stream.Collectors"
        );

        builder.javadoc("JWT Token provider for creating and validating tokens.");
        builder.annotation("Component");

        builder.classDeclaration("public", "TokenProvider", null);

        builder.staticFinalField("Logger", "log", "LoggerFactory.getLogger(TokenProvider.class)");
        builder.staticFinalField("String", "AUTHORITIES_KEY", "\"auth\"");
        builder.staticFinalField("String", "INVALID_JWT_TOKEN", "\"Invalid JWT token.\"");
        builder.line();

        builder.field("private", "SecretKey", "key");
        builder.field("private", "JwtParser", "jwtParser");
        builder.field("private", "long", "tokenValidityInMilliseconds");
        builder.field("private", "long", "tokenValidityInMillisecondsForRememberMe");
        builder.line();

        // Constructor
        builder.constructor("public", "TokenProvider", "ApplicationProperties applicationProperties");
        builder.statement("byte[] keyBytes = Decoders.BASE64.decode(applicationProperties.getSecurity().getAuthentication().getJwt().getBase64Secret())");
        builder.statement("this.key = Keys.hmacShaKeyFor(keyBytes)");
        builder.statement("this.jwtParser = Jwts.parser().verifyWith(key).build()");
        builder.statement("this.tokenValidityInMilliseconds = 1000 * applicationProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds()");
        builder.statement("this.tokenValidityInMillisecondsForRememberMe = 1000 * applicationProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe()");
        builder.closeMethod();
        builder.line();

        // createToken
        builder.javadoc("Create a JWT token for an authentication.",
            "@param authentication the authentication to create a token for.",
            "@param rememberMe whether to remember the user.",
            "@return the JWT token.");
        builder.methodSignature("public", "String", "createToken", "Authentication authentication", "boolean rememberMe");
        builder.statement("String authorities = authentication.getAuthorities().stream()\n" +
            "            .map(GrantedAuthority::getAuthority)\n" +
            "            .collect(Collectors.joining(\",\"))");
        builder.line();
        builder.statement("long now = (new Date()).getTime()");
        builder.statement("Date validity");
        builder.ifStatement("rememberMe");
        builder.statement("validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe)");
        builder.elseStatement();
        builder.statement("validity = new Date(now + this.tokenValidityInMilliseconds)");
        builder.closeIf();
        builder.line();
        builder.returnStatement("Jwts.builder()\n" +
            "            .subject(authentication.getName())\n" +
            "            .claim(AUTHORITIES_KEY, authorities)\n" +
            "            .signWith(key)\n" +
            "            .expiration(validity)\n" +
            "            .compact()");
        builder.closeMethod();
        builder.line();

        // getAuthentication
        builder.javadoc("Get an authentication from a JWT token.",
            "@param token the JWT token.",
            "@return the authentication.");
        builder.methodSignature("public", "Authentication", "getAuthentication", "String token");
        builder.statement("Claims claims = jwtParser.parseSignedClaims(token).getPayload()");
        builder.line();
        builder.statement("Collection<? extends GrantedAuthority> authorities = Arrays\n" +
            "            .stream(claims.get(AUTHORITIES_KEY).toString().split(\",\"))\n" +
            "            .filter(auth -> !auth.trim().isEmpty())\n" +
            "            .map(SimpleGrantedAuthority::new)\n" +
            "            .collect(Collectors.toList())");
        builder.line();
        builder.statement("User principal = new User(claims.getSubject(), \"\", authorities)");
        builder.line();
        builder.returnStatement("new UsernamePasswordAuthenticationToken(principal, token, authorities)");
        builder.closeMethod();
        builder.line();

        // validateToken
        builder.javadoc("Validate a JWT token.",
            "@param authToken the token to validate.",
            "@return true if the token is valid, false otherwise.");
        builder.methodSignature("public", "boolean", "validateToken", "String authToken");
        builder.tryBlock();
        builder.statement("jwtParser.parseSignedClaims(authToken)");
        builder.returnStatement("true");
        builder.catchBlock("JwtException | IllegalArgumentException", "e");
        builder.statement("log.info(INVALID_JWT_TOKEN)");
        builder.statement("log.trace(INVALID_JWT_TOKEN, e)");
        builder.closeTryCatch();
        builder.returnStatement("false");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "security/jwt/TokenProvider.java", builder.build());
    }

    private void writeJwtAuthenticationConverter() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".security.jwt");

        builder.addImports(
            "org.springframework.core.convert.converter.Converter",
            "org.springframework.security.authentication.AbstractAuthenticationToken",
            "org.springframework.security.core.GrantedAuthority",
            "org.springframework.security.core.authority.SimpleGrantedAuthority",
            "org.springframework.security.oauth2.jwt.Jwt",
            "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken",
            "org.springframework.stereotype.Component",
            "java.util.*",
            "java.util.stream.Collectors"
        );

        builder.javadoc("Converter to extract authorities from JWT claims.");
        builder.annotation("Component");

        builder.classDeclaration("public", "JwtAuthenticationConverter", null,
            "Converter<Jwt, AbstractAuthenticationToken>");

        builder.staticFinalField("String", "AUTHORITIES_KEY", "\"auth\"");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "AbstractAuthenticationToken", "convert", "Jwt jwt");
        builder.statement("Collection<GrantedAuthority> authorities = extractAuthorities(jwt)");
        builder.returnStatement("new JwtAuthenticationToken(jwt, authorities)");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("private", "Collection<GrantedAuthority>", "extractAuthorities", "Jwt jwt");
        builder.statement("Object authoritiesClaim = jwt.getClaim(AUTHORITIES_KEY)");
        builder.ifStatement("authoritiesClaim == null");
        builder.returnStatement("Collections.emptyList()");
        builder.closeIf();
        builder.line();
        builder.ifStatement("authoritiesClaim instanceof String authorities");
        builder.returnStatement("Arrays.stream(authorities.split(\",\"))\n" +
            "                .filter(auth -> !auth.trim().isEmpty())\n" +
            "                .map(SimpleGrantedAuthority::new)\n" +
            "                .collect(Collectors.toList())");
        builder.closeIf();
        builder.line();
        builder.ifStatement("authoritiesClaim instanceof Collection<?> collection");
        builder.returnStatement("collection.stream()\n" +
            "                .filter(String.class::isInstance)\n" +
            "                .map(String.class::cast)\n" +
            "                .map(SimpleGrantedAuthority::new)\n" +
            "                .collect(Collectors.toList())");
        builder.closeIf();
        builder.line();
        builder.returnStatement("Collections.emptyList()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "security/jwt/JwtAuthenticationConverter.java", builder.build());
    }

    private void writeSecurityJwtConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.security.oauth2.jwt.JwtDecoder",
            "org.springframework.security.oauth2.jwt.NimbusJwtDecoder",
            "io.jsonwebtoken.io.Decoders",
            "io.jsonwebtoken.security.Keys",
            "javax.crypto.SecretKey"
        );

        builder.javadoc("JWT Security configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "SecurityJwtConfiguration", null);

        builder.field("private final", "ApplicationProperties", "applicationProperties");
        builder.line();

        builder.constructor("public", "SecurityJwtConfiguration", "ApplicationProperties applicationProperties");
        builder.statement("this.applicationProperties = applicationProperties");
        builder.closeMethod();
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "JwtDecoder", "jwtDecoder");
        builder.statement("byte[] keyBytes = Decoders.BASE64.decode(applicationProperties.getSecurity().getAuthentication().getJwt().getBase64Secret())");
        builder.statement("SecretKey key = Keys.hmacShaKeyFor(keyBytes)");
        builder.returnStatement("NimbusJwtDecoder.withSecretKey(key).build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/SecurityJwtConfiguration.java", builder.build());
    }

    private void writeOAuth2Security() throws Exception {
        writeAudienceValidator();
        writeOAuth2Configuration();
    }

    private void writeAudienceValidator() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".security.oauth2");

        builder.addImports(
            "org.springframework.security.oauth2.core.OAuth2Error",
            "org.springframework.security.oauth2.core.OAuth2TokenValidator",
            "org.springframework.security.oauth2.core.OAuth2TokenValidatorResult",
            "org.springframework.security.oauth2.jwt.Jwt",
            "org.springframework.util.Assert",
            "java.util.List"
        );

        builder.javadoc("Validates that the JWT token contains the expected audience.");

        builder.classDeclaration("public", "AudienceValidator", null, "OAuth2TokenValidator<Jwt>");

        builder.field("private final", "List<String>", "allowedAudience");
        builder.field("private final", "OAuth2Error", "error");
        builder.line();

        builder.constructor("public", "AudienceValidator", "List<String> allowedAudience");
        builder.statement("Assert.notEmpty(allowedAudience, \"Allowed audience should not be empty\")");
        builder.statement("this.allowedAudience = allowedAudience");
        builder.statement("this.error = new OAuth2Error(\"invalid_token\", \"The required audience is missing\", null)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "OAuth2TokenValidatorResult", "validate", "Jwt jwt");
        builder.statement("List<String> audience = jwt.getAudience()");
        builder.ifStatement("audience.stream().anyMatch(allowedAudience::contains)");
        builder.returnStatement("OAuth2TokenValidatorResult.success()");
        builder.closeIf();
        builder.returnStatement("OAuth2TokenValidatorResult.failure(error)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "security/oauth2/AudienceValidator.java", builder.build());
    }

    private void writeOAuth2Configuration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.beans.factory.annotation.Value",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator",
            "org.springframework.security.oauth2.core.OAuth2TokenValidator",
            "org.springframework.security.oauth2.jwt.*",
            config.getPackageName() + ".security.oauth2.AudienceValidator",
            "java.util.List"
        );

        builder.javadoc("OAuth2 configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "OAuth2Configuration", null);

        builder.annotation("Value", "\"${spring.security.oauth2.client.provider.oidc.issuer-uri}\"");
        builder.field("private", "String", "issuerUri");
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "JwtDecoder", "jwtDecoder");
        builder.statement("NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri)");
        builder.line();
        builder.statement("OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(List.of(\"account\", \"api://default\"))");
        builder.statement("OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri)");
        builder.statement("OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator)");
        builder.line();
        builder.statement("jwtDecoder.setJwtValidator(withAudience)");
        builder.line();
        builder.returnStatement("jwtDecoder");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/OAuth2Configuration.java", builder.build());
    }

    private void writeSecurityConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.http.HttpMethod",
            "org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity",
            "org.springframework.security.config.annotation.web.builders.HttpSecurity",
            "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity",
            "org.springframework.security.config.http.SessionCreationPolicy",
            "org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder",
            "org.springframework.security.crypto.password.PasswordEncoder",
            "org.springframework.security.web.SecurityFilterChain",
            "org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter",
            config.getPackageName() + ".security.AuthoritiesConstants"
        );

        if (config.isJwt()) {
            builder.addImport(config.getPackageName() + ".security.jwt.JwtAuthenticationConverter");
        }

        builder.javadoc("Security configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableWebSecurity");
        builder.annotation("EnableMethodSecurity", "securedEnabled = true");

        builder.classDeclaration("public", "SecurityConfiguration", null);

        if (config.isJwt()) {
            builder.field("private final", "JwtAuthenticationConverter", "jwtAuthenticationConverter");
            builder.line();
            builder.constructor("public", "SecurityConfiguration", "JwtAuthenticationConverter jwtAuthenticationConverter");
            builder.statement("this.jwtAuthenticationConverter = jwtAuthenticationConverter");
            builder.closeMethod();
            builder.line();
        }

        // PasswordEncoder bean
        builder.annotation("Bean");
        builder.methodSignature("public", "PasswordEncoder", "passwordEncoder");
        builder.returnStatement("new BCryptPasswordEncoder()");
        builder.closeMethod();
        builder.line();

        // SecurityFilterChain
        builder.annotation("Bean");
        builder.methodSignature("public", "SecurityFilterChain", "filterChain", "HttpSecurity http");
        builder.line("http");
        builder.indent();
        builder.line(".csrf(csrf -> csrf.disable())");
        builder.line(".headers(headers -> headers");
        builder.indent();
        builder.line(".contentSecurityPolicy(csp -> csp.policyDirectives(\"default-src 'self'; frame-ancestors 'self'; form-action 'self'\"))");
        builder.line(".referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))");
        builder.line(".permissionsPolicy(permissions -> permissions.policy(\"camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()\"))");
        builder.outdent();
        builder.line(")");
        builder.line(".sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))");
        builder.line(".authorizeHttpRequests(auth -> auth");
        builder.indent();
        builder.line(".requestMatchers(HttpMethod.OPTIONS, \"/**\").permitAll()");
        builder.line(".requestMatchers(\"/swagger-ui/**\").permitAll()");
        builder.line(".requestMatchers(\"/v3/api-docs/**\").permitAll()");
        builder.line(".requestMatchers(\"/management/health\").permitAll()");
        builder.line(".requestMatchers(\"/management/health/**\").permitAll()");
        builder.line(".requestMatchers(\"/management/info\").permitAll()");
        builder.line(".requestMatchers(\"/management/prometheus\").permitAll()");
        builder.line(".requestMatchers(\"/management/**\").hasAuthority(AuthoritiesConstants.ADMIN)");
        builder.line(".requestMatchers(\"/api/authenticate\").permitAll()");
        builder.line(".requestMatchers(\"/api/register\").permitAll()");
        builder.line(".requestMatchers(\"/api/activate\").permitAll()");
        builder.line(".requestMatchers(\"/api/account/reset-password/init\").permitAll()");
        builder.line(".requestMatchers(\"/api/account/reset-password/finish\").permitAll()");
        builder.line(".requestMatchers(\"/api/admin/**\").hasAuthority(AuthoritiesConstants.ADMIN)");
        builder.line(".requestMatchers(\"/api/**\").authenticated()");
        builder.line(".anyRequest().authenticated()");
        builder.outdent();
        builder.line(")");

        if (config.isJwt()) {
            builder.line(".oauth2ResourceServer(oauth2 -> oauth2");
            builder.indent();
            builder.line(".jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))");
            builder.outdent();
            builder.line(");");
        } else if (config.isOauth2()) {
            builder.line(".oauth2Login(oauth2 -> oauth2)");
            builder.line(".oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt));");
        } else {
            builder.line(";");
        }

        builder.outdent();
        builder.line();
        builder.returnStatement("http.build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/SecurityConfiguration.java", builder.build());
    }
}
