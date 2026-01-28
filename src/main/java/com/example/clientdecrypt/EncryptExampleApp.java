package com.example.clientdecrypt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "client-encrypt-example",
    mixinStandardHelpOptions = true,
    description = "Encrypt a JSON payload into a JWE encryptedValue wrapper."
)
public final class EncryptExampleApp implements Callable<Integer> {
    @CommandLine.Option(names = "--config", required = true, description = "Properties file with encryption settings.")
    private Path configPath;

    @CommandLine.Option(
        names = "--input",
        description = "JSON payload to encrypt.",
        defaultValue = "examples/chekcoutDecryptedCardResponse.json"
    )
    private Path inputJson;

    @CommandLine.Option(
        names = "--output",
        description = "Output JSON file containing encryptedValue.",
        defaultValue = "examples/encryptedPayload.json"
    )
    private Path outputJson;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EncryptExampleApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AppConfig config = AppConfig.load(configPath);
        PayloadEncryptor encryptor = new PayloadEncryptor(config);

        String payloadJson = Files.readString(inputJson);
        String encryptedJson = encryptor.encrypt(payloadJson);

        JsonNode encryptedNode = mapper.readTree(encryptedJson);
        String prettyEncrypted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(encryptedNode);
        Files.writeString(outputJson, prettyEncrypted);
        return 0;
    }
}
