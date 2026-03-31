package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "comments")
public record CommentEntity(
    @Id String id,
    String imageId,
    String userId,
    String text,
    LocalDateTime createdAt
) {
    public CommentEntity(String imageId, String userId, String text) {
        this(null, imageId, userId, text, LocalDateTime.now());
    }
}