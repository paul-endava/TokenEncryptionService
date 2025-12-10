package com.br.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EncryptionServiceApplication.class)
@ActiveProfiles("test")
@Import(CryptoServiceIntegrationTest.TestKeyProviderConfig.class)
@ExtendWith(TestResultLogger.class)
class CryptoServiceIntegrationTest {

    @TestConfiguration
    static class TestKeyProviderConfig {
        @Bean
        @Primary
        public KeyProvider testKeyProvider() {
            return () -> "0123456789abcdef0123456789abcdef"
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    @Autowired
    private CryptoService cryptoService;

    @Test
    void encryptAndDecrypt_withSpringContext_roundTripsSuccessfully() {
        String plaintext = "{\"userId\":42,\"email\":\"test@example.com\"}";

        String encrypted = cryptoService.encrypt(plaintext);
        assertNotNull(encrypted);
        assertFalse(encrypted.isBlank());

        String decrypted = cryptoService.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_withSpringContext_generatesDifferentCiphertexts() {
        String plaintext = "{\"userId\":42,\"email\":\"test@example.com\"}";

        String encrypted1 = cryptoService.encrypt(plaintext);
        String encrypted2 = cryptoService.encrypt(plaintext);

        assertNotEquals(encrypted1, encrypted2,
                "Ciphertexts should differ due to random IV");
    }

    @Test
    void decrypt_withTamperedCiphertext_throwsException() {
        String plaintext = "hello-world";
        String encrypted = cryptoService.encrypt(plaintext);

        // Tamper with the ciphertext
        char[] chars = encrypted.toCharArray();
        chars[chars.length - 1] = chars[chars.length - 1] == 'A' ? 'B' : 'A';
        String tampered = new String(chars);

        assertThrows(RuntimeException.class, () -> cryptoService.decrypt(tampered));
    }

    @Test
    void decrypt_withInvalidBase64_throwsException() {
        assertThrows(RuntimeException.class,
                () -> cryptoService.decrypt("not-valid-base64!"));
    }

    @Test
    void decrypt_withEmptyString_throwsException() {
        assertThrows(RuntimeException.class,
                () -> cryptoService.decrypt(""));
    }
}