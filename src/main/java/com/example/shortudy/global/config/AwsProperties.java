package com.example.shortudy.global.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String region;
    private final S3 s3 = new S3();
    private final Credentials credentials = new Credentials();

    public void setRegion(String region) {
        this.region = region;
    }

    @Getter
    public static class S3 {
        private String bucket;
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }

    @Getter
    public static class Credentials {
        private String accessKey;
        private String secretKey;

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
