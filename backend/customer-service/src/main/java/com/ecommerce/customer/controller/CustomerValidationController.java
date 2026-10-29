package com.ecommerce.customer.controller;

import com.ecommerce.customer.dto.CustomerRegistrationRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerValidationController {

    @PostMapping("/validate")
    public ResponseEntity<String> validateRegistration(
            @Valid @RequestBody CustomerRegistrationRequest request) {

        return ResponseEntity.ok("Customer request is valid");
    }
}
