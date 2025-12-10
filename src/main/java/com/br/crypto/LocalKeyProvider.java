package com.br.crypto;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Profile("local")   // Only active when spring.profiles.active: local
public class LocalKeyProvider implements KeyProvider {

    private volatile byte[] cachedKey;

    @Override
    public byte[] getKey() {
        if (cachedKey != null) {
            return cachedKey;
        }

        synchronized (this) {
            if (cachedKey != null) {
                return cachedKey;
            }

            String keyB64Env = System.getenv("AES_KEY_B64");
            if (keyB64Env == null || keyB64Env.isBlank()) {
                throw new IllegalStateException(
                        "AES_KEY_B64 environment variable is not set. " +
                                "Set AES_KEY_B64 to a base64-encoded 32-byte AES key (local profile)."
                );
            }

            byte[] key = Base64.getDecoder().decode(keyB64Env);
            if (key.length != 32) {
                throw new IllegalStateException(
                        "AES-256 key must be 32 bytes; got " + key.length +
                                " (check AES_KEY_B64 env var)"
                );
            }

            cachedKey = key;
            return cachedKey;
        }
    }
}