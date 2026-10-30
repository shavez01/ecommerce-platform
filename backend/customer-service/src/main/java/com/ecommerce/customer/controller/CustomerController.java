package com.ecommerce.customer.controller;

import com.ecommerce.customer.dto.CustomerRegistrationRequest;
import com.ecommerce.customer.entity.Customer;
import com.ecommerce.customer.service.CustomerService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer register(
            @Valid @RequestBody CustomerRegistrationRequest request) {
        return customerService.register(request);
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable("id") @NonNull Long id) {
        return customerService.getCustomerById(id);
    }
}
