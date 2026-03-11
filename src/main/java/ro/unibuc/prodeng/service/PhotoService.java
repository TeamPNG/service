package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.PhotoEntity;
import ro.unibuc.prodeng.repository.PhotoRepository;
import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.PhotoResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository){
        this.photoRepository = photoRepository;
    }


     public List<PhotoResponse> getAllPhotos() {
        return photoRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PhotoResponse getPhotoById(String id) {
        PhotoEntity photo = photoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + id));
        return toResponse(photo);
    }


    public List<PhotoResponse>  getPhotoByUserId(String userId) {
        return photoRepository.getPhotoByuploadedBydUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PhotoResponse createPhoto(CreatePhotoRequest request) {
        if (photoRepository.findByTitle(request.title()).equals(request.title())) {
            throw new IllegalArgumentException("Photo with title " + request.title() + " already exists");
        }

        PhotoEntity photo = new PhotoEntity(
            request.title(),
            request.description(),
            request.location(),
            request.URL(),
            request.category(),
            request.uploadedBydUserId()
        );

        PhotoEntity saved = photoRepository.save(photo);
        return toResponse(saved);
    }

    public void deletePhoto(String id) {
        if (!photoRepository.existsById(id)) {
            throw new EntityNotFoundException("Photo not found with id: " + id);
        }
        photoRepository.deleteById(id);
    }

    public List<PhotoResponse> getPhotosByCategory(String category) {
        return photoRepository.findByCategory(category).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private PhotoResponse toResponse(PhotoEntity photo) {
        return new PhotoResponse(
            photo.id(),
            photo.title(),
            photo.description(),
            photo.location(),
            photo.URL(),
            photo.likes(),
            photo.category(),
            photo.uploadedBydUserId()

        );
    }


}
