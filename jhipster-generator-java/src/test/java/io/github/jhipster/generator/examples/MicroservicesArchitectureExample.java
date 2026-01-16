/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Microservices Architecture Example - Generate a complete microservices setup.
 *
 * Run with: mvn exec:java -Dexec.mainClass="io.github.jhipster.generator.examples.MicroservicesArchitectureExample"
 */
package io.github.jhipster.generator.examples;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.generators.app.AppGenerator;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Microservices Architecture Example.
 * Generates a complete microservices architecture with:
 * - API Gateway
 * - Product Service
 * - Order Service
 * - Invoice Service
 */
public class MicroservicesArchitectureExample {

    private static final String BASE_OUTPUT = "target/microservices-demo";

    public static void main(String[] args) throws Exception {
        System.out.println("=======================================================");
        System.out.println("  JHipster Java - Microservices Architecture Generator");
        System.out.println("=======================================================");
        System.out.println();

        Path baseDir = Paths.get(BASE_OUTPUT);
        Files.createDirectories(baseDir);
        System.out.println("Output directory: " + baseDir.toAbsolutePath());
        System.out.println();

        long totalStart = System.currentTimeMillis();

        // Generate Gateway
        System.out.println("Generating API Gateway...");
        generateGateway(baseDir);

        // Generate Product Service
        System.out.println("Generating Product Service...");
        generateProductService(baseDir);

        // Generate Order Service
        System.out.println("Generating Order Service...");
        generateOrderService(baseDir);

        // Generate Invoice Service
        System.out.println("Generating Invoice Service...");
        generateInvoiceService(baseDir);

        long totalDuration = System.currentTimeMillis() - totalStart;
        System.out.println();
        System.out.println("=======================================================");
        System.out.println("  Generation Complete!");
        System.out.println("=======================================================");
        System.out.println();
        System.out.println("Total time: " + totalDuration + "ms");
        System.out.println();
        System.out.println("Generated services:");
        System.out.println("  - gateway (port 8080)");
        System.out.println("  - productservice (port 8081)");
        System.out.println("  - orderservice (port 8082)");
        System.out.println("  - invoiceservice (port 8083)");
        System.out.println();
        System.out.println("To start the infrastructure:");
        System.out.println("  cd " + baseDir.toAbsolutePath());
        System.out.println("  docker compose up -d");
        System.out.println();
        System.out.println("To start each service:");
        System.out.println("  cd gateway && ./mvnw spring-boot:run");
        System.out.println("  cd productservice && ./mvnw spring-boot:run");
        System.out.println("  cd orderservice && ./mvnw spring-boot:run");
        System.out.println("  cd invoiceservice && ./mvnw spring-boot:run");
        System.out.println();
    }

    private static void generateGateway(Path baseDir) throws Exception {
        Path gatewayDir = baseDir.resolve("gateway");
        Files.createDirectories(gatewayDir);

        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName("gateway");
        config.setPackageName("com.mycompany.gateway");
        config.setApplicationType("gateway");
        config.setDatabaseType("sql");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        config.setServerPort(8080);
        config.setSkipClient(false);
        config.setClientFramework("angular");
        config.setReactive(true);

        GeneratorContext context = new GeneratorContext(gatewayDir, config);
        new AppGenerator(context).run();

        System.out.println("  -> Gateway generated at " + gatewayDir);
    }

    private static void generateProductService(Path baseDir) throws Exception {
        Path serviceDir = baseDir.resolve("productservice");
        Files.createDirectories(serviceDir);

        JHipsterConfig config = createMicroserviceConfig("productservice", "com.mycompany.product", 8081);

        GeneratorContext context = new GeneratorContext(serviceDir, config);

        // Add Product entity
        EntityConfig product = new EntityConfig();
        product.setName("Product");
        product.setEntityTableName("product");
        product.setFields(Arrays.asList(
            createField("name", "String", true),
            createField("description", "String", false),
            createField("price", "BigDecimal", true),
            createField("stockQuantity", "Integer", true),
            createField("sku", "String", true),
            createField("createdDate", "Instant", false)
        ));
        context.addEntity(product);

        // Add Category entity
        EntityConfig category = new EntityConfig();
        category.setName("Category");
        category.setEntityTableName("category");
        category.setFields(Arrays.asList(
            createField("name", "String", true),
            createField("description", "String", false)
        ));
        context.addEntity(category);

        new AppGenerator(context).run();
        System.out.println("  -> Product Service generated at " + serviceDir);
    }

    private static void generateOrderService(Path baseDir) throws Exception {
        Path serviceDir = baseDir.resolve("orderservice");
        Files.createDirectories(serviceDir);

        JHipsterConfig config = createMicroserviceConfig("orderservice", "com.mycompany.order", 8082);
        config.setFeignClient(true); // Enable Feign for inter-service communication

        GeneratorContext context = new GeneratorContext(serviceDir, config);

        // Add Order entity
        EntityConfig order = new EntityConfig();
        order.setName("Order");
        order.setEntityTableName("jhi_order");
        order.setFields(Arrays.asList(
            createField("orderDate", "Instant", true),
            createField("status", "String", true),
            createField("totalAmount", "BigDecimal", true),
            createField("customerId", "Long", true),
            createField("shippingAddress", "String", false)
        ));
        context.addEntity(order);

        // Add OrderItem entity
        EntityConfig orderItem = new EntityConfig();
        orderItem.setName("OrderItem");
        orderItem.setEntityTableName("order_item");
        orderItem.setFields(Arrays.asList(
            createField("productId", "Long", true),
            createField("quantity", "Integer", true),
            createField("unitPrice", "BigDecimal", true),
            createField("subtotal", "BigDecimal", true)
        ));
        context.addEntity(orderItem);

        new AppGenerator(context).run();
        System.out.println("  -> Order Service generated at " + serviceDir);
    }

    private static void generateInvoiceService(Path baseDir) throws Exception {
        Path serviceDir = baseDir.resolve("invoiceservice");
        Files.createDirectories(serviceDir);

        JHipsterConfig config = createMicroserviceConfig("invoiceservice", "com.mycompany.invoice", 8083);
        config.setMessageBroker("kafka"); // Use Kafka for async events

        GeneratorContext context = new GeneratorContext(serviceDir, config);

        // Add Invoice entity
        EntityConfig invoice = new EntityConfig();
        invoice.setName("Invoice");
        invoice.setEntityTableName("invoice");
        invoice.setFields(Arrays.asList(
            createField("invoiceNumber", "String", true),
            createField("orderId", "Long", true),
            createField("invoiceDate", "Instant", true),
            createField("dueDate", "Instant", true),
            createField("totalAmount", "BigDecimal", true),
            createField("status", "String", true),
            createField("paidDate", "Instant", false)
        ));
        context.addEntity(invoice);

        // Add Payment entity
        EntityConfig payment = new EntityConfig();
        payment.setName("Payment");
        payment.setEntityTableName("payment");
        payment.setFields(Arrays.asList(
            createField("paymentDate", "Instant", true),
            createField("amount", "BigDecimal", true),
            createField("paymentMethod", "String", true),
            createField("transactionId", "String", false)
        ));
        context.addEntity(payment);

        new AppGenerator(context).run();
        System.out.println("  -> Invoice Service generated at " + serviceDir);
    }

    private static JHipsterConfig createMicroserviceConfig(String baseName, String packageName, int port) {
        JHipsterConfig config = new JHipsterConfig();
        config.setBaseName(baseName);
        config.setPackageName(packageName);
        config.setApplicationType("microservice");
        config.setDatabaseType("sql");
        config.setDevDatabaseType("h2Disk");
        config.setProdDatabaseType("postgresql");
        config.setAuthenticationType("jwt");
        config.setServiceDiscoveryType("consul");
        config.setBuildTool("maven");
        config.setCacheProvider("ehcache");
        config.setEnableHibernateCache(true);
        config.setServerPort(port);
        config.setDto("mapstruct");
        config.setService("serviceImpl");
        config.setSkipClient(true);
        return config;
    }

    private static FieldConfig createField(String name, String type, boolean required) {
        FieldConfig field = new FieldConfig();
        field.setFieldName(name);
        field.setFieldType(type);
        field.setRequired(required);
        return field;
    }
}
