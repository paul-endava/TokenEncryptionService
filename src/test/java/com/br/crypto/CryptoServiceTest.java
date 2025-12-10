package com.br.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestResultLogger.class)
class CryptoServiceTest {

    /**
     * Simple KeyProvider for tests – fixed 32-byte key.
     */
    static class TestKeyProvider implements KeyProvider {
        private final byte[] key;

        TestKeyProvider() {
            // 32 ASCII bytes = AES-256 key
            this.key = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }

    @Test
    void encryptAndDecrypt_roundTripsPlaintext() {
        KeyProvider keyProvider = new TestKeyProvider();
        CryptoService cryptoService = new CryptoService(keyProvider);

        String plaintext = "{\"userId\":42,\"email\":\"dev@example.com\"}";

        String encrypted = cryptoService.encrypt(plaintext);
        assertNotNull(encrypted);
        assertFalse(encrypted.isBlank());

        String decrypted = cryptoService.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void encrypt_generatesDifferentCiphertextForSamePlaintext() {
        KeyProvider keyProvider = new TestKeyProvider();
        CryptoService cryptoService = new CryptoService(keyProvider);

        String plaintext = "{\"userId\":42,\"email\":\"dev@example.com\"}";

        String encrypted1 = cryptoService.encrypt(plaintext);
        String encrypted2 = cryptoService.encrypt(plaintext);

        assertNotEquals(encrypted1, encrypted2,
                "Ciphertexts should differ because IV is random each time");
    }

    @Test
    void decrypt_withTamperedCiphertextThrows() {
        KeyProvider keyProvider = new TestKeyProvider();
        CryptoService cryptoService = new CryptoService(keyProvider);

        String plaintext = "hello-world";
        String encrypted = cryptoService.encrypt(plaintext);

        // Tamper with the ciphertext by flipping a character
        char[] chars = encrypted.toCharArray();
        chars[chars.length - 1] = chars[chars.length - 1] == 'A' ? 'B' : 'A';
        String tampered = new String(chars);

        assertThrows(RuntimeException.class, () -> cryptoService.decrypt(tampered));
    }
}