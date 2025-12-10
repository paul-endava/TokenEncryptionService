package com.br.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crypto")
public class CryptoProperties {

    /**
     * Secrets Manager secret name containing {"key": "<base64>"}
     */
    private String aesKeySecretName;

    public String getAesKeySecretName() {
        return aesKeySecretName;
    }

    public void setAesKeySecretName(String aesKeySecretName) {
        this.aesKeySecretName = aesKeySecretName;
    }
}