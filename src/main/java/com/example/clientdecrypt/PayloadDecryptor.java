package com.example.clientdecrypt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mastercard.developer.encryption.JweConfig;
import com.mastercard.developer.encryption.JweConfigBuilder;
import com.mastercard.developer.encryption.JweEncryption;
import com.mastercard.developer.utils.EncryptionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.Certificate;

final class PayloadDecryptor {
    private final ObjectMapper mapper;
    private final JweConfig jweConfig;

    PayloadDecryptor(ObjectMapper mapper, AppConfig config) throws Exception {
        this.mapper = mapper;
        PrivateKey decryptionKey = loadDecryptionKey(config);
        JweConfigBuilder builder = JweConfigBuilder.aJweEncryptionConfig()
            .withDecryptionKey(decryptionKey)
            .withDecryptionPath("$.encryptedValue", "$");

        if (config.encryptionCertificatePath() != null) {
            Certificate encryptionCertificate = EncryptionUtils.loadEncryptionCertificate(
                config.encryptionCertificatePath());
            builder.withEncryptionCertificate(encryptionCertificate);
        }
        this.jweConfig = builder.build();
    }

    DecryptedResult decrypt(String encryptedPayload) throws IOException {
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.put("encryptedValue", encryptedPayload);
        String decryptedJson;
        try {
            decryptedJson = JweEncryption.decryptPayload(wrapper.toString(), jweConfig);
        } catch (com.mastercard.developer.encryption.EncryptionException ex) {
            throw new IOException("Failed to decrypt payload", ex);
        }
        JsonNode root = mapper.readTree(decryptedJson);
        JsonNode card = root.path("card");
        JsonNode token = root.path("token");
        JsonNode dynamicData = root.path("dynamicData");

        return new DecryptedResult(
            textOrNull(card.path("primaryAccountNumber")),
            textOrNull(card.path("panExpirationMonth")),
            textOrNull(card.path("panExpirationYear")),
            textOrNull(card.path("cardholderFullName")),
            textOrNull(token.path("paymentToken")),
            textOrNull(token.path("tokenExpirationMonth")),
            textOrNull(token.path("tokenExpirationYear")),
            textOrNull(token.path("paymentAccountReference")),
            textOrNull(dynamicData.path("dynamicDataType")),
            textOrNull(dynamicData.path("dynamicDataValue")),
            textOrNull(dynamicData.path("dynamicDataExpiration"))
        );
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static PrivateKey loadDecryptionKey(AppConfig config) throws Exception {
        if (config.decryptionKeyPath() != null) {
            return EncryptionUtils.loadDecryptionKey(
                config.decryptionKeyPath(),
                config.decryptionKeyAlias(),
                config.decryptionKeyPassword());
        }
        return EncryptionUtils.loadDecryptionKey(config.decryptionKeyPlainPath());
    }

    record DecryptedResult(
        String cardPrimaryAccountNumber,
        String cardPanExpirationMonth,
        String cardPanExpirationYear,
        String cardholderFullName,
        String tokenPaymentToken,
        String tokenExpirationMonth,
        String tokenExpirationYear,
        String tokenPaymentAccountReference,
        String dynamicDataType,
        String dynamicDataValue,
        String dynamicDataExpiration
    ) {
    }
}
