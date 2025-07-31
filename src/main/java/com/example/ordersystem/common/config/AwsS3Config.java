package com.example.ordersystem.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

// s3에 접근하기 위한 스프링빈 생성
@Configuration  // @Bean이랑 한 쌍
public class AwsS3Config {

    @Value("${cloud.aws.credentials.access-key}")   // yml에서 설정한 옵션 가져오기
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")   // yml에서 설정한 옵션 가져오기
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;
    

    @Bean   // 리턴되는 객체를 싱글톤 객체로 만들기 위해 선언한 어노테이션
    public S3Client client() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return (S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build());
    }
}
