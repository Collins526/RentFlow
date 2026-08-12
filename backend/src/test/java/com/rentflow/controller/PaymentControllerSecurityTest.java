package com.rentflow.controller;

import com.rentflow.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentControllerSecurityTest {

    @Test
    void createPayment_AllowsTenantRole() throws NoSuchMethodException {
        Method method = PaymentController.class.getDeclaredMethod("createPayment", PaymentRequest.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertTrue(annotation != null);
        assertTrue(annotation.value().contains("TENANT"));
    }
}
