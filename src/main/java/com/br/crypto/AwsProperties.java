package com.br.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AwsProperties {

    @Value("${aws.region:us-east-1}")
    private String region;

    public String getRegion() {
        return region;
    }
}