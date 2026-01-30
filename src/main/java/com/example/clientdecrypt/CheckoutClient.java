package com.example.clientdecrypt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mastercard.developer.oauth.OAuth;
import com.mastercard.developer.utils.AuthenticationUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

final class CheckoutClient {
    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String consumerKey;
    private final PrivateKey signingKey;
    private final String checkoutType;
    private final String srcDpaId;
    private final String organizationId;

    CheckoutClient(ObjectMapper mapper, AppConfig config) throws Exception {
        this.mapper = mapper;
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = AppConfig.normalizeBaseUrl(config.baseUrl());
        this.consumerKey = config.consumerKey();
        this.signingKey = AuthenticationUtils.loadSigningKey(
            config.signingKeyPath(),
            config.signingKeyAlias(),
            config.signingKeyPassword());
        this.checkoutType = config.checkoutType();
        this.srcDpaId = config.srcDpaId();
        this.organizationId = config.organizationId();
    }

    String checkoutEncryptedPayload(String merchantTransactionId) throws IOException, InterruptedException {
        URI uri = URI.create(baseUrl + "/checkout");
        String payload = buildCheckoutPayload(merchantTransactionId);
        String authHeader = OAuth.getAuthorizationHeader(
            uri,
            "POST",
            payload,
            StandardCharsets.UTF_8,
            consumerKey,
            signingKey);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", authHeader)
            .POST(HttpRequest.BodyPublishers.ofString(payload));

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Checkout request failed with status " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode encryptedPayloadNode = root.path("encryptedPayload");
        if (encryptedPayloadNode.isTextual()) {
            return encryptedPayloadNode.asText();
        }
        if (encryptedPayloadNode.has("encryptedValue")) {
            return encryptedPayloadNode.path("encryptedValue").asText();
        }
        throw new IOException("Encrypted payload missing or in unexpected format.");
    }

    private String buildCheckoutPayload(String merchantTransactionId) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        if (srcDpaId != null) {
            root.put("srcDpaId", srcDpaId);
        } else if (organizationId != null) {
            root.put("organizationId", organizationId);
        }
        root.put("checkoutType", checkoutType);
        ObjectNode checkoutReference = root.putObject("checkoutReference");
        checkoutReference.put("type", "MERCHANT_TRANSACTION_ID");
        checkoutReference.putObject("data").put("merchantTransactionId", merchantTransactionId);
        return mapper.writeValueAsString(root);
    }
}
