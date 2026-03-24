package ro.unibuc.prodeng.response;

import ro.unibuc.prodeng.model.UserRole;

public record UserResponse(
    String id,
    String name,
    String email,
    UserRole role
) {}
