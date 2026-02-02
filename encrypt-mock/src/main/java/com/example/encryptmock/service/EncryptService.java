package com.example.encryptmock.service;

import com.mastercard.developer.encryption.JweConfig;
import com.mastercard.developer.encryption.JweEncryption;
import org.springframework.stereotype.Service;

@Service
public class EncryptService {
    private final JweConfig jweConfig;

    public EncryptService(JweConfig jweConfig) {
        this.jweConfig = jweConfig;
    }

    public String encrypt(String payloadJson) throws Exception {
        return JweEncryption.encryptPayload(payloadJson, jweConfig);
    }
}

