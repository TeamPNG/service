package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ro.unibuc.prodeng.model.UserRole;

public record CreateUserRequest(
    @NotBlank(message = "Name is required")
    String name,

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email,
    
    UserRole role
) {}
