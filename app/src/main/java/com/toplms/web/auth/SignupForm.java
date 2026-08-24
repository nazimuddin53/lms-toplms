package com.toplms.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class SignupForm {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 80, message = "Business name must be 2–80 characters")
    private String businessName;


    @NotBlank(message = "Workspace URL is required")
    @Pattern(
        regexp = "^[a-z0-9][a-z0-9-]{2,29}$",
        message = "Use 3–30 lowercase letters, digits or hyphens (must start with a letter or digit)"
    )
    private String slug;

    @NotBlank(message = "Your full name is required")
    @Size(min = 2, max = 80, message = "Name must be 2–80 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    // Getters & setters — Spring's form binding sets fields via setX(...) on
    // submit, and Thymeleaf reads them via getX() when re-rendering after a
    // validation error.
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
