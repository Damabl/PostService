package org.example.payload;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class MessageResponse {
    private String field;
    private String message;
    public MessageResponse(String field, String message, HttpStatus ok) {

        this.message = message;
        this.field = field;
    }

}
