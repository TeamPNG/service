package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePhotoRequest (
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "category is required")
    String category,
    
    @NotBlank(message = "description is required")
    String description,

    @NotBlank(message = "location is required")
    String location,

    @NotBlank(message = "uploadedBydUserId is required")
    String uploadedBydUserId,

    @NotBlank(message = "URL is required")
    String URL

    
){}
