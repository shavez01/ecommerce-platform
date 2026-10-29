
package com.ecommerce.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRegistrationRequest(

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    String email

) {
}
