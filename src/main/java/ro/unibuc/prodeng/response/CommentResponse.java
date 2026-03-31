package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record CommentResponse(
    String id,
    String imageId,
    String userId,
    String text,
    LocalDateTime createdAt
) {}
