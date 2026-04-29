package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.PhotoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.model.UserRole;
import ro.unibuc.prodeng.repository.PhotoRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.PhotoResponse;
import ro.unibuc.prodeng.util.CategoryPermissions;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    public PhotoService(PhotoRepository photoRepository, UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
    }

    public List<PhotoResponse> getAllPhotos() {
        return photoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PhotoResponse getPhotoById(String id) {
        @SuppressWarnings("null")
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + id));
        return toResponse(photo);
    }

    public List<PhotoResponse> getPhotoByUserId(String userId) {
        return photoRepository.getPhotoByuploadedBydUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PhotoResponse createPhoto(CreatePhotoRequest request) {
        @SuppressWarnings("null")
        UserEntity user = userRepository.findById(request.uploadedBydUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.uploadedBydUserId()));

        if (user.role() != UserRole.CONTENT_CREATOR) {
            throw new IllegalArgumentException("Only content creators can upload photos");
        }

        if (!CategoryPermissions.canUploadToCategory(user.role(), request.category())) {
            throw new IllegalArgumentException("User cannot upload to category: " + request.category() +
                    ". Allowed categories: " + CategoryPermissions.getUploadableCategories(user.role()));
        }

        if (!photoRepository.findByTitle(request.title()).isEmpty()) {
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

    @SuppressWarnings("null")
    public void deletePhoto(String id, String userId) {
        @SuppressWarnings("null")
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + id));

        @SuppressWarnings("null")
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.role() != UserRole.CONTENT_CREATOR) {
            throw new IllegalArgumentException("Only content creators can delete photos");
        }

        if (!photo.uploadedBydUserId().equals(userId)) {
            throw new IllegalArgumentException("Users can only delete their own photos");
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
