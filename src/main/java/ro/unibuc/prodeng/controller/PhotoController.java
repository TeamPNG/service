package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.PhotoResponse;
import ro.unibuc.prodeng.service.PhotoService;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public ResponseEntity<List<PhotoResponse>> getAllPhotos() {
        List<PhotoResponse> photos = photoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }

    //to add param category

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable String id) {
        PhotoResponse photo = photoService.getPhotoById(id);
        return ResponseEntity.ok(photo);
    }

    @GetMapping("/{userid}")
    public ResponseEntity<List<PhotoResponse>> getPhotoByUserId(@PathVariable String UserId) {
        List<PhotoResponse> photo = photoService.getPhotoByUserId(UserId);
        return ResponseEntity.ok(photo);
    }

    @PostMapping
    public ResponseEntity<PhotoResponse> createPhoto(@Valid @RequestBody CreatePhotoRequest request) {
        PhotoResponse photo = photoService.createPhoto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(photo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category")
    public ResponseEntity<List<PhotoResponse>> getPhotosByCategory(String category) {
        List<PhotoResponse> photos = photoService.getPhotosByCategory(category);
        return ResponseEntity.ok(photos);
    }
}