package com.example.encrypt;

import com.mastercard.developer.encryption.JweConfig;
import com.mastercard.developer.encryption.JweConfigBuilder;
import com.mastercard.developer.encryption.JweEncryption;
import com.mastercard.developer.utils.EncryptionUtils;

import java.nio.file.Path;
import java.security.cert.Certificate;

final class PayloadEncryptor {
    private final JweConfig jweConfig;

    PayloadEncryptor(Path encryptionCertificatePath) throws Exception {
        if (encryptionCertificatePath == null) {
            throw new IllegalArgumentException("encryptionCertificatePath is required for payload encryption.");
        }
        Certificate encryptionCertificate = EncryptionUtils.loadEncryptionCertificate(encryptionCertificatePath.toString());
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
