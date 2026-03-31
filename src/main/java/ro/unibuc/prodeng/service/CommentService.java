package ro.unibuc.prodeng.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.*;
import ro.unibuc.prodeng.repository.*;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository; // AGGIUNTO: Per verificare i ruoli degli utenti

    public CommentResponse createComment(CreateCommentRequest request) {
        // Check if the user exists
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.userId()));

        // Check if the photo exists
        PhotoEntity photo = photoRepository.findById(request.imageId())
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + request.imageId()));

        // Only Content Creators can comment photo of "architecture" category
        if ("architecture".equalsIgnoreCase(photo.category()) && user.role() != UserRole.CONTENT_CREATOR) {
            throw new IllegalArgumentException("Only Content Creators can comment on architecture photos.");
        }

        CommentEntity comment = new CommentEntity(
                request.imageId(),
                request.userId(),
                request.text()
        );
        
        CommentEntity saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    public void deleteComment(String id, String requestUserId) {
        CommentEntity comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));

        PhotoEntity photo = photoRepository.findById(comment.imageId())
                .orElseThrow(() -> new EntityNotFoundException("Photo not found"));

        UserEntity requester = userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // You can delete if you are the author of the comment or if you are the owner of the photo
        boolean isAuthor = comment.userId().equals(requestUserId);
        boolean isPhotoOwner = photo.uploadedBydUserId().equals(requestUserId);

        if (!isAuthor && !isPhotoOwner) {
            throw new IllegalArgumentException("User not authorized to delete this comment");
        }

        commentRepository.deleteById(id);
    }

    public List<CommentResponse> getCommentsByImage(String imageId) {
        return commentRepository.findByImageId(imageId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CommentResponse getCommentById(String id) {
        CommentEntity comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
        return toResponse(comment);
    }

    private CommentResponse toResponse(CommentEntity comment) {
        return new CommentResponse(
                comment.id(),
                comment.imageId(),
                comment.userId(),
                comment.text(),
                comment.createdAt()
        );
    }
}