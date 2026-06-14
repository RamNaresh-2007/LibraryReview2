package com.library.management;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthDTOs {

    @Schema(description = "Credentials for authentication")
    public static class LoginRequest {
        @Schema(example = "admin")
        private String username;
        @Schema(example = "admin123")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Schema(description = "Details for new user registration")
    public static class RegisterRequest {
        @Schema(example = "newuser")
        private String username;
        @Schema(example = "password123")
        private String password;
        @Schema(example = "New User Name")
        private String fullname;
        @Schema(example = "user@example.com")
        private String email;
        @Schema(example = "student")
        private String role;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
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
    }

    @Schema(description = "Successful authentication response")
    public static class AuthResponse {
        private String token;
        private String username;
        private String fullname;
        private String email;
        private String role;

        // Getters and Setters
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
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
    }
}
