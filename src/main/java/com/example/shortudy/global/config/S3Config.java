package com.example.shortudy.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private final AwsProperties awsProperties;

    public S3Config(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    /**
     * 서명된 URL(Presigned URL)을 만드는 전용 도구입니다.
     * 실제로 S3와 통신하지 않고, 서버 내부에서 암호화 알고리즘만 돌려서 URL을 생성합니다.
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.getCredentials().getAccessKey(),
                                awsProperties.getCredentials().getSecretKey()
                        ))).build();
    }

    /**
     * S3에 직접 명령을 내리는 도구입니다. (파일 삭제, 목록 조회 등)
     * 실제로 AWS S3 서버와 네트워크 통신을 합니다.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.getCredentials().getAccessKey(),
                                awsProperties.getCredentials().getSecretKey()
                        )))
                .build();
    }
}
