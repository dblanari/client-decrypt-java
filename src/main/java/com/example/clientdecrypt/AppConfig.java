package com.example.clientdecrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

final class AppConfig {
    private final String baseUrl;
    private final String consumerKey;
    private final String signingKeyPath;
    private final String signingKeyAlias;
    private final String signingKeyPassword;
    private final String srcDpaId;
    private final String organizationId;
    private final String checkoutType;
    private final String encryptionCertificatePath;
    private final String decryptionKeyPath;
    private final String decryptionKeyAlias;
    private final String decryptionKeyPassword;
    private final String decryptionKeyPlainPath;
    private final String environment;
    private final String mockBaseUrl;

    private AppConfig(Properties properties) {
        this.baseUrl = get(properties, "baseUrl", "https://sandbox.api.mastercard.com/srci/api");
        this.consumerKey = require(properties, "consumerKey");
        this.signingKeyPath = require(properties, "signingKeyPath");
        this.signingKeyAlias = require(properties, "signingKeyAlias");
        this.signingKeyPassword = require(properties, "signingKeyPassword");
        this.srcDpaId = get(properties, "srcDpaId", null);
        this.organizationId = get(properties, "organizationId", null);
        this.checkoutType = get(properties, "checkoutType", "CLICK_TO_PAY");
        this.encryptionCertificatePath = get(properties, "encryptionCertificatePath", null);
        this.decryptionKeyPath = get(properties, "decryptionKeyPath", null);
        this.decryptionKeyAlias = get(properties, "decryptionKeyAlias", null);
        this.decryptionKeyPassword = get(properties, "decryptionKeyPassword", null);
        this.decryptionKeyPlainPath = get(properties, "decryptionKeyPlainPath", null);
        this.environment = get(properties, "environment", "mock").toLowerCase();
        this.mockBaseUrl = get(properties, "mockBaseUrl", "http://localhost:8081");

        if (srcDpaId != null && organizationId != null) {
            throw new IllegalArgumentException("Provide only one of srcDpaId or organizationId.");
        }
        if (srcDpaId == null && organizationId == null) {
            throw new IllegalArgumentException("Provide srcDpaId or organizationId.");
        }
        boolean hasPkcs12 = decryptionKeyPath != null;
        boolean hasPlain = decryptionKeyPlainPath != null;
        if (!hasPkcs12 && !hasPlain) {
            throw new IllegalArgumentException("Provide decryptionKeyPath (PKCS#12) or decryptionKeyPlainPath.");
        }
        if (hasPkcs12) {
            require(properties, "decryptionKeyAlias");
            require(properties, "decryptionKeyPassword");
        }
        if (!environment.equals("sandbox") && !environment.equals("mock")) {
            throw new IllegalArgumentException("environment must be sandbox or mock.");
        }
    }

    static AppConfig load(Path path) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        return new AppConfig(properties);
    }

    String baseUrl() {
        return baseUrl;
    }

    String consumerKey() {
        return consumerKey;
    }

    String signingKeyPath() {
        return signingKeyPath;
    }

    String signingKeyAlias() {
        return signingKeyAlias;
    }

    String signingKeyPassword() {
        return signingKeyPassword;
    }

    String srcDpaId() {
        return srcDpaId;
    }

    String organizationId() {
        return organizationId;
    }

    String checkoutType() {
        return checkoutType;
    }

    String encryptionCertificatePath() {
        return encryptionCertificatePath;
    }

    String decryptionKeyPath() {
        return decryptionKeyPath;
    }

    String decryptionKeyAlias() {
        return decryptionKeyAlias;
    }

    String decryptionKeyPassword() {
        return decryptionKeyPassword;
    }

    String decryptionKeyPlainPath() {
        return decryptionKeyPlainPath;
    }

    boolean useMock() {
        return "mock".equals(environment);
    }

    String mockBaseUrl() {
        return mockBaseUrl;
    }

    private static String get(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String require(Properties properties, String key) {
        String value = get(properties, key, null);
        if (value == null) {
            throw new IllegalArgumentException("Missing required config property: " + key);
        }
        return value;
    }

    static String normalizeBaseUrl(String baseUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
