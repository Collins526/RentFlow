package com.rentflow.exception;

import com.rentflow.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void authenticationFailureReturnsUnauthorizedWithGenericMessage() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAuthenticationFailure(new BadCredentialsException("bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    void unexpectedFailureRemainsInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGenericException(new IllegalStateException("database unavailable"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals(null, response.getBody().getErrors());
    }
}
