package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.metrics.AppMetrics;
import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.PhotoResponse;
import ro.unibuc.prodeng.service.PhotoService;

import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final AppMetrics appMetrics;

    public PhotoController(PhotoService photoService, AppMetrics appMetrics) {
        this.photoService = photoService;
        this.appMetrics = appMetrics;
    }

    @GetMapping
    public ResponseEntity<List<PhotoResponse>> getAllPhotos() {
        Timer.Sample sample = appMetrics.startInvocationTimer();
        try {
            List<PhotoResponse> photos = photoService.getAllPhotos();
            return ResponseEntity.ok(photos);
        } finally {
            appMetrics.incrementInvocationCount("api/photos");
            appMetrics.stopInvocationTimer(sample, "api/photos");
        }
    }

    //to add param category

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoById(@PathVariable String id) {
        Timer.Sample sample = appMetrics.startInvocationTimer();
        try {
            PhotoResponse photo = photoService.getPhotoById(id);
            return ResponseEntity.ok(photo);
        } finally {
            appMetrics.incrementInvocationCount("api/photos/{id}");
            appMetrics.stopInvocationTimer(sample, "api/photos/{id}");
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PhotoResponse>> getPhotoByUserId(@PathVariable String userId) {
        Timer.Sample sample = appMetrics.startInvocationTimer();
        try {
            List<PhotoResponse> photos = photoService.getPhotoByUserId(userId);
            return ResponseEntity.ok(photos);
        } finally {
            appMetrics.incrementInvocationCount("api/photos/user/{userId}");
            appMetrics.stopInvocationTimer(sample, "api/photos/user/{userId}");
        }
    }

    @PostMapping
    public ResponseEntity<PhotoResponse> createPhoto(@Valid @RequestBody CreatePhotoRequest request) {
        Timer.Sample sample = appMetrics.startInvocationTimer();
        try {
            PhotoResponse photo = photoService.createPhoto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(photo);
        } finally {
            appMetrics.incrementInvocationCount("api/photos");
            appMetrics.stopInvocationTimer(sample, "api/photos");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String id, @RequestParam String userId) {
        Timer.Sample sample = appMetrics.startInvocationTimer();
        try {
            photoService.deletePhoto(id, userId);
            return ResponseEntity.noContent().build();
        } finally {
            appMetrics.incrementInvocationCount("api/photos/{id}");
            appMetrics.stopInvocationTimer(sample, "api/photos/{id}");
        }
    }

    @GetMapping("/category")
    public ResponseEntity<List<PhotoResponse>> getPhotosByCategory(@RequestParam String category) {
        Timer.Sample sample = appMetrics.startInvocationTimer();
        try {
            List<PhotoResponse> photos = photoService.getPhotosByCategory(category);
            return ResponseEntity.ok(photos);
        } finally {
            appMetrics.incrementInvocationCount("api/photos/category");
            appMetrics.stopInvocationTimer(sample, "api/photos/category");
        }
    }
}