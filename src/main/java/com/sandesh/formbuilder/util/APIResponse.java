package com.sandesh.formbuilder.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<T> {

    private Boolean success;
    private HttpStatus status;
    private String message;
    private T data;
    private List<String> errors;
    private String timestamp;


    // Success response constructor
    public APIResponse(HttpStatus status, String message, T data) {
        this.success = true;
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = null;
        this.timestamp = LocalDateTime.now().toString();
    }
    // Success response without data
    public APIResponse(HttpStatus status, String message) {
        this.success = true;
        this.status = status;
        this.message = message;
        this.data = null;
        this.errors = null;
        this.timestamp = LocalDateTime.now().toString();
    }


    // Error response constructor
    public APIResponse(HttpStatus status, String message, List<String> errors) {
        this.success = false;
        this.status = status;
        this.message = message;
        this.data = null;
        this.errors = errors;
        this.timestamp = LocalDateTime.now().toString();
    }
}
