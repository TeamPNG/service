package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByImage(@PathVariable String imageId) {
        return ResponseEntity.ok(commentService.getCommentsByImage(imageId));
    }
}