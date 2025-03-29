package org.example.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.example.service.props.MinioProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Config {
    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getUrl()) // URL MinIO
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey()) // Доступ
                .build();
    }
}
