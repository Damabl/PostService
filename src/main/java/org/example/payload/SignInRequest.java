package org.example.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
