package com.br.crypto;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String AES = "AES";
    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";

    private final KeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] key = keyProvider.getKey();
            SecretKeySpec keySpec = new SecretKeySpec(key, AES);

            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt", e);
        }
    }

    public String decrypt(String base64IvAndCiphertext) {
        try {
            byte[] key = keyProvider.getKey();
            SecretKeySpec keySpec = new SecretKeySpec(key, AES);

            byte[] combined = Base64.getDecoder().decode(base64IvAndCiphertext);
            if (combined.length < 17) {
                throw new IllegalArgumentException("Ciphertext too short");
            }

            byte[] iv = new byte[16];
            byte[] ciphertext = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt", e);
        }
    }
}