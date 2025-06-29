package com.sandesh.formbuilder.exception;

import com.sandesh.formbuilder.util.APIResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                Collections.singletonList(ex.getMessage())
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                Collections.singletonList("Request body is missing")
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<Object>>handleIllegalArgumentException(IllegalArgumentException exception){

        APIResponse<Object>response = new APIResponse<>(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                Collections.singletonList(exception.getMessage())
        );

        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.BAD_REQUEST,
                "User not found",
                Collections.singletonList(ex.getMessage())
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<APIResponse<Object>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.BAD_REQUEST,
                "User already exists",
                Collections.singletonList(ex.getMessage())
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.BAD_REQUEST,
                "The request is not valid",
                Collections.singletonList(ex.getMessage())
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIResponse<Object>> handleRuntimeException(RuntimeException ex) {

        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                Collections.singletonList(ex.getMessage())
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGeneralException(Exception ex) {
        APIResponse<Object> response = new APIResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                Collections.singletonList(ex.getMessage())
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }





}
