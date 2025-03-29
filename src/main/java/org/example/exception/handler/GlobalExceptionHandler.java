package org.example.exception.handler;

import org.example.exception.exceptions.PostNotFoundException;
import org.example.payload.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleUserNotFound(PostNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("An error occurred: " + ex.getMessage()));
    }
}
