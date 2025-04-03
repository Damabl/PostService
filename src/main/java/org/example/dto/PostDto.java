package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;

    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;
    @NotNull
    @Size(max=500,message = "Заголовок слишком длинний")
    private String title;
    @NotBlank(message = "Содержание поста не может быть пустым")
    private String content;

    @Size(min = 1, message = "Должен быть хотя бы один файл")
    private List<MultipartFile> files;
    @Size(min=1,message = "Должен быть хотя бы один категорий" )
    private List<Long> categoryIds;
}
