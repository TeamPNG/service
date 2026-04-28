package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.service.CommentService;
import ro.unibuc.prodeng.service.MetricsService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final MetricsService metricsService;

    public CommentController(CommentService commentService, MetricsService metricsService) {
        this.commentService = commentService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
       try {
            CommentResponse response = commentService.createComment(request);

            metricsService.recordCommentCreated();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {

           metricsService.recordCommentFailed();
           throw e;
        }   
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByImage(@PathVariable String imageId) {
        return metricsService.getCommentLookupTimer().record(() ->
            ResponseEntity.ok(commentService.getCommentsByImage(imageId))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable String id) {
        CommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id, @RequestParam String userId) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}