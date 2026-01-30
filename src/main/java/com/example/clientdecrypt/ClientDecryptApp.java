package com.example.clientdecrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import picocli.CommandLine;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "client-decrypt", mixinStandardHelpOptions = true, description = "Fetch and decrypt Click to Pay checkout payloads.")
public final class ClientDecryptApp implements Callable<Integer> {
    @CommandLine.Option(names = "--input", required = true, description = "Input CSV file with cardNumber and merchantTransactionId columns.")
    private Path inputCsv;

    @CommandLine.Option(names = "--output", required = true, description = "Output CSV file path.")
    private Path outputCsv;

    @CommandLine.Option(names = "--config", required = true, description = "Properties file with API, OAuth, and encryption settings.")
    private Path configPath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ClientDecryptApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AppConfig config = AppConfig.load(configPath);
        CheckoutClient checkoutClient = new CheckoutClient(mapper, config);
        PayloadDecryptor decryptor = new PayloadDecryptor(mapper, config);

        CSVFormat inputFormat = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build();
        CSVFormat outputFormat = CSVFormat.DEFAULT.builder()
            .setHeader(
                "cardNumber",
                "merchantTransactionId",
                "cardPrimaryAccountNumber",
                "cardPanExpirationMonth",
                "cardPanExpirationYear",
                "cardholderFullName",
                "tokenPaymentToken",
                "tokenExpirationMonth",
                "tokenExpirationYear",
                "tokenPaymentAccountReference",
                "dynamicDataType",
                "dynamicDataValue",
                "dynamicDataExpiration"
            )
            .build();

        try (Reader reader = Files.newBufferedReader(inputCsv);
             CSVParser parser = new CSVParser(reader, inputFormat);
             Writer writer = Files.newBufferedWriter(outputCsv);
             CSVPrinter printer = new CSVPrinter(writer, outputFormat)) {

            for (CSVRecord record : parser) {
                String cardNumber = value(record, "cardNumber");
                String merchantTransactionId = value(record, "merchantTransactionId");
                if (merchantTransactionId == null || merchantTransactionId.isBlank()) {
                    printer.printRecord(blankRecord(cardNumber, merchantTransactionId));
                    System.err.println("Skipping row with missing merchantTransactionId.");
                    continue;
                }

                try {
                    String encryptedPayload = checkoutClient.checkoutEncryptedPayload(merchantTransactionId);
                    PayloadDecryptor.DecryptedResult decrypted = decryptor.decrypt(encryptedPayload);
                    printer.printRecord(
                        nullToEmpty(cardNumber),
                        nullToEmpty(merchantTransactionId),
                        nullToEmpty(decrypted.cardPrimaryAccountNumber()),
                        nullToEmpty(decrypted.cardPanExpirationMonth()),
                        nullToEmpty(decrypted.cardPanExpirationYear()),
                        nullToEmpty(decrypted.cardholderFullName()),
                        nullToEmpty(decrypted.tokenPaymentToken()),
                        nullToEmpty(decrypted.tokenExpirationMonth()),
                        nullToEmpty(decrypted.tokenExpirationYear()),
                        nullToEmpty(decrypted.tokenPaymentAccountReference()),
                        nullToEmpty(decrypted.dynamicDataType()),
                        nullToEmpty(decrypted.dynamicDataValue()),
                        nullToEmpty(decrypted.dynamicDataExpiration())
                    );
                } catch (Exception ex) {
                    printer.printRecord(blankRecord(cardNumber, merchantTransactionId));
                    System.err.println("Failed to process transaction " + merchantTransactionId + ": " + ex.getMessage());
                }
            }
        }

        return 0;
    }

    private static String value(CSVRecord record, String header) {
        if (!record.isMapped(header)) {
            return null;
        }
        String value = record.get(header);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Object[] blankRecord(String cardNumber, String merchantTransactionId) {
        return new Object[] {
            nullToEmpty(cardNumber),
            nullToEmpty(merchantTransactionId),
            "", "", "", "", "", "", "", "", "", "", ""
        };
    }
}
