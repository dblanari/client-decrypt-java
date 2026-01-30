package com.example.encrypt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "encrypt-payload",
    mixinStandardHelpOptions = true,
    description = "Encrypt a JSON payload into a JWE encryptedValue using Method 2 (CSR upload) certificate."
)
public final class EncryptPayloadApp implements Callable<Integer> {
    @CommandLine.Option(
        names = "--config",
        description = "Path to encrypt.properties.",
        defaultValue = "config/encrypt.properties"
    )
    private Path configPath;

    @CommandLine.Option(names = "--input", description = "Override input JSON payload path.")
    private Path inputOverride;

    @CommandLine.Option(names = "--output", description = "Override output JSON path.")
    private Path outputOverride;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EncryptPayloadApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        EncryptConfig config = EncryptConfig.load(configPath);
        Path inputPath = inputOverride != null ? inputOverride : config.inputJson();
        Path outputPath = outputOverride != null ? outputOverride : config.outputJson();

        String payloadJson = Files.readString(inputPath);
        PayloadEncryptor encryptor = new PayloadEncryptor(config.encryptionCertificatePath());
        String encryptedJson = encryptor.encrypt(payloadJson);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode encryptedNode = mapper.readTree(encryptedJson);
        String prettyEncrypted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(encryptedNode);

        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputPath, prettyEncrypted);
        return 0;
    }
}
