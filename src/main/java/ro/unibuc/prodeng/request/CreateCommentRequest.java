package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank(message = "Image ID is mandatory")
    String imageId,

    @NotBlank(message = "User email is mandatory")
    String userEmail,

    @NotBlank(message = "Comment text cannot be empty")
    String text
) {}