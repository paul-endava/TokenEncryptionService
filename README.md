# Token Encryption Service

A Spring Boot service providing AES-256-CBC encryption/decryption capabilities for sensitive token data. Designed as a reusable component that can be injected into other Spring applications.

## Purpose

This service provides secure, production-ready encryption and decryption of token data using AES-256-CBC with PKCS5 padding. Each encryption operation generates a unique random IV (Initialization Vector), ensuring identical plaintexts produce different ciphertexts.

## Intended Audience

- **Backend Developers** integrating encryption into Spring Boot microservices
- **Security Engineers** implementing token protection mechanisms
- **DevOps Engineers** deploying encryption services in AWS environments

## Features

- ✅ AES-256-CBC encryption with random IV per operation
- ✅ Base64-encoded output for easy transmission
- ✅ Flexible key management with fallback chain
- ✅ Spring Boot integration via dependency injection
- ✅ Comprehensive unit and integration tests
- ✅ AWS Secrets Manager integration for production

## Pre-requisites
- Java 17 or higher
- Maven 3.6+
- (Production only) AWS account with Secrets Manager access

## Architecture

### Core Components

- **`CryptoService`** - Main service providing `encrypt()` and `decrypt()` methods
- **`KeyProvider`** - Interface for encryption key retrieval
- **`AesKeyProvider`** - Production implementation with fallback chain
- **`CryptoProperties`** - Configuration properties from `application.yml`

### Key Management Strategy

The service uses a **fallback chain** to retrieve the encryption key. Keys are loaded in the following priority order:

Key management is intentionally flexible and environment-aware.  
The service loads a 256-bit AES key using a *fallback chain*

### **1. Unit Tests & Integration Tests (test profile)**
- Key is injected via a test-scoped `@Primary KeyProvider` bean.
- No AWS calls occur.
- Tests are deterministic and isolated.

### **2. Local Development (`local` profile)**
Key is exclusively loaded from the environment variable:
A base64-encoded 32-byte AES key is required.  
This avoids AWS calls entirely during development.

### **3. Production / Dev / Stage (any non-local profile)**
Keys are retrieved from **AWS Secrets Manager**.

Secret format:

```json
{
  "key": "base64-encoded-32-byte-aes-key"
}
```


#### How Keys Are Loaded
Unified Key Loading Logic

Actual resolution sequence in AesKeyProvider:
	1.	AWS Secrets Manager (production)
	2.	Environment variable AES_KEY_B64 (CI/CD, overrides AWS if present)
	3.	YAML config (crypto.local-key-base64) as a final fallback

This allows:
	•	Seamless local dev
	•	Seamless CI/CD pipeline operation
	•	Secure production deployment

## Key Generation
openssl rand -base64 32
Copy this output into:
	•	AES_KEY_B64 for local development or CI/CD
	•	AWS Secrets Manager for deployment environments
	
```JSON
{
  "key": "<some key>"
}
```

### Local Development
```BASH
export SPRING_PROFILES_ACTIVE=local
export AES_KEY_B64="base64-encoded-32-byte-key"

mvn spring-boot:run
```
### CI/CD (e.g., GitHub Actions or GitLab ENV variables

```BASH
env:
SPRING_PROFILES_ACTIVE: local
AES_KEY_B64: ${{ secrets.AES_KEY_B64 }}

steps:
- uses: actions/checkout@v4
- run: mvn test
```
### Production (AWS Secrets Manager)
```YAML
env:
  SPRING_PROFILES_ACTIVE: local
  AES_KEY_B64: ${{ secrets.AES_KEY_B64 }}

steps:
  - uses: actions/checkout@v4
  - run: mvn test
```
## 🧪 Testing Strategy

The Token Encryption Service is a **library-only component** (no HTTP/API layer).  
Tests focus on verifying the cryptographic behavior and Spring wiring of `CryptoService`.

### 1. Unit Tests (`CryptoServiceTest`)

These tests validate the core encryption logic in isolation, without Spring, AWS, or any external dependencies.

- Instantiate `CryptoService` directly with a simple in-memory `KeyProvider` test implementation.
- Use a fixed 32-byte AES key (test-only) for deterministic behavior.
- Validate:
    - Plaintext → ciphertext → plaintext round-trip:
        - `encryptAndDecrypt_roundTripsPlaintext()`
    - Random IV behavior (same plaintext produces different ciphertexts):
        - `encrypt_generatesDifferentCiphertextForSamePlaintext()`
    - Correct failure behavior when ciphertext is tampered with:
        - `decrypt_withTamperedCiphertextThrows()`

These tests prove that the `encrypt(String plaintext)` and `decrypt(String base64IvAndCiphertext)` methods in `CryptoService` behave correctly at the algorithm level.

---

### 2. Spring Integration Tests (`CryptoServiceIntegrationTest`)

These tests run with a real Spring Boot context to verify that `CryptoService` is correctly wired and that key management behaves as expected under the `test` profile.

- The application context starts with:
    - `CryptoService`
    - `AesKeyProvider` (or a test-specific `KeyProvider` override)
- No real AWS calls are made; the key is provided by a controlled test configuration.
- Validate:
    - Successful encryption/decryption using the Spring-managed `CryptoService` bean:
        - `encryptAndDecrypt_withSpringContext_roundTripsSuccessfully()`
    - Encryption still produces different ciphertexts for the same plaintext:
        - `encrypt_withSpringContext_generatesDifferentCiphertexts()`
    - Decryption error handling for bad inputs:
        - `decrypt_withInvalidBase64_throwsException()`
        - `decrypt_withEmptyString_throwsException()`
        - `decrypt_withTamperedCiphertext_throwsException()`

These tests ensure that the library works correctly when used inside a real Spring application and that the key loading abstraction (`KeyProvider` / `AesKeyProvider`) integrates cleanly with `CryptoService`.

---

### 3. No AWS or Network Dependencies in Tests

All tests are designed to be:

- **Fast** – no network calls, no external services
- **Deterministic** – fixed test keys and controlled inputs
- **Safe for CI/CD** – no AWS credentials or Secrets Manager access required

AWS integration (Secrets Manager) is only used in non-test runtime profiles (e.g., `dev`, `stage`, `prod`), not in the `test` profile.

---

### 4. How to Run Tests

From the project root:

```bash
mvn test
```
## Configuration

### Application Properties

Edit `src/main/resources/application.yml`:

