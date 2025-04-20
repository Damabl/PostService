package org.example.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceClient {
    private final WebClient.Builder webClientBuilder;
    @Value("${USER_SERVICE_URL}")
    private String USER_SERVICE_URL;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserCategoriesFallback")
    public List<String> getUserCategories(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri(USER_SERVICE_URL + "/users/{id}/categories", userId)
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserInfoFallback")
    public UserInfoDto getUserInfo(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri(USER_SERVICE_URL + "/users/{id}/info", userId)
                .retrieve()
                .bodyToMono(UserInfoDto.class)
                .block();
    }

    private UserInfoDto getUserInfoFallback(Long userId, Throwable throwable) {
        System.err.println("User service is down! Returning default user info.");
        UserInfoDto fallbackUser = new UserInfoDto();
        fallbackUser.setUsername("Unknown");
        fallbackUser.setAvatarId(null);
        return fallbackUser;
    }
    private List<String> getUserCategoriesFallback(Long userId, Throwable throwable) {
        System.err.println("User service is down! Returning empty category list.");
        return List.of(); // Возвращаем пустой список
    }
}

