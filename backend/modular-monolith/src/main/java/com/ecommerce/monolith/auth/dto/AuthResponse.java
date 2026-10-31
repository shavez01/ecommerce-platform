
package com.ecommerce.monolith.auth.dto;

public class AuthResponse {

    private Long userId;
    private String email;
    private String role;
    private String message;
    private String accessToken;

    public AuthResponse() {
    }

    // Registration constructor
    public AuthResponse(Long userId, String email,
                        String role, String message) {
        this(userId, email, role, message, null);
    }

    // Login constructor
    public AuthResponse(Long userId, String email,
                        String role, String message,
                        String accessToken) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.message = message;
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
