package com.example.clientdecrypt;

import com.mastercard.developer.encryption.JweConfig;
import com.mastercard.developer.encryption.JweConfigBuilder;
import com.mastercard.developer.encryption.JweEncryption;
import com.mastercard.developer.utils.EncryptionUtils;

import java.security.cert.Certificate;

final class PayloadEncryptor {
    private final JweConfig jweConfig;

    PayloadEncryptor(AppConfig config) throws Exception {
        if (config.encryptionCertificatePath() == null) {
            throw new IllegalArgumentException("Missing encryptionCertificatePath for payload encryption.");
        }
        Certificate encryptionCertificate = EncryptionUtils.loadEncryptionCertificate(
            config.encryptionCertificatePath());
        this.jweConfig = JweConfigBuilder.aJweEncryptionConfig()
            .withEncryptionCertificate(encryptionCertificate)
            .withEncryptionPath("$", "$")
            .withEncryptedValueFieldName("encryptedValue")
            .build();
    }

    String encrypt(String payloadJson) throws com.mastercard.developer.encryption.EncryptionException {
        return JweEncryption.encryptPayload(payloadJson, jweConfig);
    }
}
