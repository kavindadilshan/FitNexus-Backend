package com.fitnexus.server.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

//import org.springframework.cloud.aws.mail.simplemail.SimpleEmailServiceJavaMailSender;


@Configuration
@PropertySource("classpath:${aws-property-file}")
public class AWSConfig {

    @Value("${aws.keyId}")
    private String awsKeyId;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Bean
    public String bucketName() {
        return bucketName;
    }

    @Bean
    public AmazonS3 awsS3Client() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsKeyId, accessKey);
        return AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
    }

}
