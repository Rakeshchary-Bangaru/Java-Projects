
package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(
        name = "app.images.storage.type",
        havingValue = "s3"
)
public class S3ProductImageConfiguration {

    @Bean(destroyMethod = "close")
    public S3Client productImageS3Client(
            @Value("${app.images.s3.region}") String region
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
