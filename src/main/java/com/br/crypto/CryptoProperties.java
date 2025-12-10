package com.br.crypto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crypto")
@Getter
@Setter
public class CryptoProperties {

    /**
     * Secrets Manager secret name containing {"key": "<base64>"}
     */
    private String aesKeySecretName;
    private String localKeyBase64;
}