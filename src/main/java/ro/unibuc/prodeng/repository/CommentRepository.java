package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.unibuc.prodeng.model.CommentEntity;
import java.util.List;

public interface CommentRepository extends MongoRepository<CommentEntity, String> {
    List<CommentEntity> findByImageId(String imageId);
}