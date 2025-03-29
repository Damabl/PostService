package org.example.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.dto.CategoryDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String CATEGORY_SERVICE_URL = "http://category-service";

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getCategoryFallback")
    public CategoryDto getCategoryById(String categoryId) {
        return webClientBuilder.build()
                .get()
                .uri(CATEGORY_SERVICE_URL + "/categories/{id}", categoryId)
                .retrieve()
                .bodyToMono(CategoryDto.class)
                .block(); // Синхронный вызов
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "getAllCategoriesFallback")
    public List<CategoryDto> getAllCategories() {
        return webClientBuilder.build()
                .get()
                .uri(CATEGORY_SERVICE_URL + "/categories")
                .retrieve()
                .bodyToFlux(CategoryDto.class)
                .collectList()
                .block();
    }

    // Фолбэк для одиночной категории
    private CategoryDto getCategoryFallback(String categoryId, Throwable throwable) {
        System.err.println("Category service is down! Returning fallback category.");
        return new CategoryDto("default", "Fallback Category");
    }

    // Фолбэк для списка категорий
    private List<CategoryDto> getAllCategoriesFallback(Throwable throwable) {
        System.err.println("Category service is down! Returning empty category list.");
        return List.of(new CategoryDto("default", "Fallback Category"));
    }
}
