package com.example.encryptmock.web;

import com.example.encryptmock.service.EncryptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
public class EncryptController {

    private final EncryptService encryptService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String payload;

    public EncryptController(EncryptService encryptService,
                             @Value("${mock.payload.path}") String payloadPath) throws Exception {
        this.encryptService = encryptService;
        this.payload = Files.readString(Path.of(payloadPath));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> encrypt(@RequestBody String body) throws Exception {
        String encrypted = encryptService.encrypt(payload);
        ObjectNode response = objectMapper.createObjectNode();
        response.set("encryptedPayload", objectMapper.readTree(encrypted));
        response.put("merchantTransactionId", extractMerchantTransactionId(body));
        return ResponseEntity.ok(response);
    }

    private String extractMerchantTransactionId(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("merchantTransactionId is required in request body");
        }
        try {
            JsonNode mtid = objectMapper.readTree(body)
                    .path("checkoutReference").path("data").path("merchantTransactionId");
            if (mtid.isTextual() && !mtid.asText().isBlank()) {
                return mtid.asText();
            }
            throw new IllegalArgumentException("merchantTransactionId is missing or not textual");
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON body", e);
        }
    }
}
