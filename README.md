# Client Decrypt Java

Command-line tool that calls the Click to Pay Checkout API (`/checkout`), decrypts the JWE `encryptedPayload`, and writes PAN/token data to a CSV file. It uses Java 21, the Mastercard OAuth1 signer, and the Mastercard client-encryption library.

## Requirements
- Java 21
- Mastercard API credentials and keys:
  - OAuth consumer key + signing key (PKCS#12)
  - Encryption certificate (public key)
  - Decryption key (private key)

## Project Layout
- `src/main/java` — CLI application and API/decryption logic
- `config/example.properties` — configuration template
- `examples/input.csv` — sample input format
- `checkout-and-confirmations-api-swagger.yaml` — API contract

## Configuration
Copy `config/example.properties` and fill in real values. Keys follow Mastercard API basics:
- `encryptionCertificatePath` is the **public** encryption certificate (Client Encryption Keys).
- `decryptionKeyPath`/`decryptionKeyPlainPath` is the **private** response decryption key (Mastercard Encryption Keys).

See: https://developer.mastercard.com/unified-checkout-solutions/documentation/api-basics/

## CSV Format
Input CSV must include:
- `X-Src-Cx-Flow-Id` (can be blank)
- `merchantTransactionId`

Output CSV columns:
- `merchantTransactionId`
- `cardPrimaryAccountNumber`
- `cardPanExpirationMonth`
- `cardPanExpirationYear`
- `cardholderFullName`
- `tokenPaymentToken`
- `tokenExpirationMonth`
- `tokenExpirationYear`
- `tokenPaymentAccountReference`
- `dynamicDataType`
- `dynamicDataValue`
- `dynamicDataExpiration`

## Build
```bash
./mvnw -q -DskipTests package
```

## Run
```bash
./mvnw -q -DskipTests package
java -jar target/client-decrypt-java-1.0.0-SNAPSHOT-shaded.jar \
  --config config/example.properties \
  --input examples/input.csv \
  --output out.csv
```

## Encrypt Example Payload
Encrypt `examples/chekcoutDecryptedCardResponse.json` into a JSON wrapper containing `encryptedValue`:
```bash
./mvnw -q -DskipTests package
java -cp target/client-decrypt-java-1.0.0-SNAPSHOT-shaded.jar \
  com.example.clientdecrypt.EncryptExampleApp \
  --config config/example.properties \
  --input examples/chekcoutDecryptedCardResponse.json \
  --output examples/encryptedPayload.json
```

## Method 2: Manual CSR Upload (Key Generation Steps)
Use Method 2 (manual CSR upload) in the Mastercard Developers Portal. Generate a key and CSR locally, upload the CSR, then download the encryption certificate and set `encryptionCertificatePath`.

OpenSSL example:
```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out encryption.key
openssl req -new -key encryption.key -out encryption.csr -subj "/C=US/ST=State/L=City/O=Example/OU=Payments/CN=example.com"
```

Java keytool example:
```bash
keytool -genkeypair -alias encryption -keyalg RSA -keysize 2048 \
  -keystore encryption-keystore.p12 -storetype PKCS12 -storepass changeit \
  -dname "CN=example.com, OU=Payments, O=Example, L=City, ST=State, C=US"
keytool -certreq -alias encryption -keystore encryption-keystore.p12 \
  -storepass changeit -file encryption.csr
```

After downloading the public encryption certificate, update `encryptionCertificatePath` in your config.

## Notes
- The CLI calls `POST /checkout` with `checkoutReference.type=MERCHANT_TRANSACTION_ID`.
- If `encryptedPayload` is missing or decryption fails, PAN/token fields are left blank and the error is printed to stderr.
