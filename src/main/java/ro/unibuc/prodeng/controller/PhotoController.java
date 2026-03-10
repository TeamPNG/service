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
        List<PhotoResponse> books = photoService.getAllPhotos();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable String id) {
        PhotoResponse book = photoService.getPhotoById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<PhotoResponse> createPhoto(@Valid @RequestBody CreatePhotoRequest request) {
        PhotoResponse book = photoService.createPhoto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<PhotoResponse>> getPhotosByCategory(String category) {
        List<PhotoResponse> books = photoService.getPhotosByCategory(category);
        return ResponseEntity.ok(books);
    }
}