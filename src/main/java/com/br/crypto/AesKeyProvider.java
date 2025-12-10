package com.br.crypto;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Profile("!local & !test")
public class AesKeyProvider implements KeyProvider {

    private final CryptoProperties cryptoProperties;
    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;

    private volatile byte[] cachedKey;

    public AesKeyProvider(CryptoProperties cryptoProperties,
                          AwsProperties awsProperties,
                          ObjectMapper objectMapper) {
        this.cryptoProperties = cryptoProperties;
        this.awsProperties = awsProperties;
        this.objectMapper = objectMapper;
    }

    public byte[] getKey() {
        if (cachedKey != null) {
            return cachedKey;
        }

        synchronized (this) {
            if (cachedKey != null) {
                return cachedKey;
            }

            // 1. Local dev override: AES_KEY_B64
            String keyB64Env = System.getenv("AES_KEY_B64");
            if (keyB64Env != null && !keyB64Env.isBlank()) {
                byte[] key = Base64.getDecoder().decode(keyB64Env);
                if (key.length != 32) {
                    throw new IllegalStateException("AES-256 key must be 32 bytes; got " + key.length);
                }
                cachedKey = key;
                return cachedKey;
            }

            // 2. Fallback: AWS Secrets Manager
            String secretName = cryptoProperties.getAesKeySecretName();
            if (secretName == null || secretName.isBlank()) {
                throw new IllegalStateException("crypto.aes-key-secret-name is not configured");
            }

            AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                    .withRegion(Regions.fromName(awsProperties.getRegion()))
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .build();

            GetSecretValueRequest request = new GetSecretValueRequest()
                    .withSecretId(secretName);

            try {
                GetSecretValueResult result = client.getSecretValue(request);
                String secretString = result.getSecretString();

                JsonNode root = objectMapper.readTree(secretString);
                String keyB64 = root.get("key").asText();
                byte[] key = Base64.getDecoder().decode(keyB64);

                if (key.length != 32) {
                    throw new IllegalStateException("AES-256 key must be 32 bytes; got " + key.length);
                }

                cachedKey = key;
                return cachedKey;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load AES key from Secrets Manager", e);
            }
        }
    }
}