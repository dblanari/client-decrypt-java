package com.example.encryptmock.config;

import com.mastercard.developer.encryption.JweConfig;
import com.mastercard.developer.encryption.JweConfigBuilder;
import com.mastercard.developer.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.cert.Certificate;

@Configuration
public class EncryptionConfig {

    @Bean
    public JweConfig jweConfig(
        @Value("${mock.cert.path}") String certPath
    ) throws Exception {
        Certificate encryptionCertificate = EncryptionUtils.loadEncryptionCertificate(certPath);
        return JweConfigBuilder.aJweEncryptionConfig()
            .withEncryptionCertificate(encryptionCertificate)
            .withEncryptionPath("$", "$")
            .withEncryptedValueFieldName("encryptedValue")
            .build();
    }
}

