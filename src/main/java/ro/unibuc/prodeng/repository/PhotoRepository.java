package ro.unibuc.prodeng.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ro.unibuc.prodeng.model.PhotoEntity;

@Repository
public interface PhotoRepository extends MongoRepository<PhotoEntity, String> {

    List<PhotoEntity> getPhotoByuploadedBydUserId(String uploadedBydUserId);
    List<PhotoEntity> findByTitle(String title);
    List<PhotoEntity> findByLocation(String location);
    List<PhotoEntity> findByCategory(String category);




}
