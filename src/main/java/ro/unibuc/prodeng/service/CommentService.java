package ro.unibuc.prodeng.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.CommentEntity;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.PhotoRepository;
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

    public CommentResponse createComment(CreateCommentRequest request) {
        // Check if the photo exists before saving the comment
        if (!photoRepository.existsById(request.imageId())) {
            throw new EntityNotFoundException("Photo not found with id: " + request.imageId());
        }

        CommentEntity comment = new CommentEntity(
                request.imageId(),
                request.userEmail(),
                request.text()
        );
        CommentEntity saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    public List<CommentResponse> getCommentsByImage(String imageId) {
        return commentRepository.findByImageId(imageId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(CommentEntity comment) {
        return new CommentResponse(
                comment.id(),
                comment.imageId(),
                comment.userEmail(),
                comment.text(),
                comment.createdAt()
        );
    }
}