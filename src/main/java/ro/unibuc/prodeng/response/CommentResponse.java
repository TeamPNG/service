package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record CommentResponse(
    String id,
    String imageId,
    String userEmail,
    String text,
    LocalDateTime createdAt
) {}
