package com.example.encrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class EncryptConfig {
    private final Path encryptionCertificatePath;
    private final Path inputJson;
    private final Path outputJson;

    private EncryptConfig(Properties properties, Path projectRoot) {
        this.encryptionCertificatePath = resolve(require(properties, "encryptionCertificatePath"), projectRoot);
        this.inputJson = resolve(get(properties, "inputJson", "examples/chekcoutDecryptedCardResponse.json"), projectRoot);
        this.outputJson = resolve(get(properties, "outputJson", "examples/encryptedPayload.json"), projectRoot);
    }

    static EncryptConfig load(Path path) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        }
        Path configDir = path.toAbsolutePath().getParent();
        Path projectRoot = configDir != null ? configDir.getParent() : path.toAbsolutePath().getParent();
        return new EncryptConfig(properties, projectRoot);
    }

    Path encryptionCertificatePath() {
        return encryptionCertificatePath;
    }

    Path inputJson() {
        return inputJson;
    }

    Path outputJson() {
        return outputJson;
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

    private static Path resolve(String value, Path baseDir) {
        Path path = Path.of(value);
        if (!path.isAbsolute() && baseDir != null) {
            return baseDir.resolve(path).normalize();
        }
        return path.toAbsolutePath().normalize();
    }
}
