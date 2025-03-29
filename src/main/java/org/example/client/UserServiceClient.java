package org.example.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.dto.CategoryDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String USER_SERVICE_URL = "http://user-service";

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserCategoriesFallback")
    public List<String> getUserCategories(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri(USER_SERVICE_URL + "/users/{id}/categories", userId)
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }

    private List<String> getUserCategoriesFallback(Long userId, Throwable throwable) {
        System.err.println("User service is down! Returning empty category list.");
        return List.of(); // Возвращаем пустой список
    }
}

