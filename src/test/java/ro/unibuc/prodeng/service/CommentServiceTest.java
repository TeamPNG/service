package ro.unibuc.prodeng.service;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.prodeng.model.*;
import ro.unibuc.prodeng.repository.*;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void testCreateComment_success() {
        // Arrange
        UserEntity user = new UserEntity("u1", "User", "u@test.com", UserRole.VIEWER);
        PhotoEntity photo = new PhotoEntity("p1", "Rome", "Amor", "Rome", "http://url.com", 0, "nature", "creator1");
        CommentEntity comment = new CommentEntity("c1", "p1", "u1", "Nice!", java.time.LocalDateTime.now());

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(photo));
        when(commentRepository.save(any())).thenReturn(comment);

        // Act
        CommentResponse response = commentService.createComment(new CreateCommentRequest("p1", "u1", "Bello!"));

        // Assert
        assertNotNull(response);
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void testCreateComment_whenUserNotFound_throwsException() {
        when(userRepository.findById("any-user")).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> 
            commentService.createComment(new CreateCommentRequest("p1", "any-user", "text")));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void testCreateComment_whenUserIsViewerOnArchitecture_throwsException() {
        UserEntity viewer = new UserEntity("1", "Serena", "s@test.com", UserRole.VIEWER);
        PhotoEntity archiPhoto = new PhotoEntity("p1", "Bridge", "Desc", "Italy", "url", 0, "architecture", "c1");

        when(userRepository.findById("1")).thenReturn(Optional.of(viewer));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(archiPhoto));

        assertThrows(IllegalArgumentException.class, () -> 
            commentService.createComment(new CreateCommentRequest("p1", "1", "Nice!")));
        
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testDeleteComment_whenPhotoOwnerDeletes_succeeds() {
        // Arrange
        CommentEntity comment = new CommentEntity("c1", "p1", "author-id", "Hello", java.time.LocalDateTime.now());
        PhotoEntity photo = new PhotoEntity("p1", "Title", "Desc", "Loc", "url", 0, "nature", "owner-id");
        UserEntity owner = new UserEntity("owner-id", "Owner", "o@t.com", UserRole.CONTENT_CREATOR);

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("owner-id")).thenReturn(Optional.of(owner));

        // Act
        commentService.deleteComment("c1", "owner-id");

        // Assert
        verify(commentRepository, times(1)).deleteById("c1");
    }

    @Test
    void testDeleteComment_whenNotAuthorized_throwsException() {
        // Arrange
        CommentEntity comment = new CommentEntity("c1", "p1", "user-a", "Test Text", java.time.LocalDateTime.now());
        PhotoEntity photo = new PhotoEntity("p1", "Title", "Desc", "Loc", "url", 0, "nature", "user-b");
        UserEntity stranger = new UserEntity("user-c", "Stranger", "c@test.com", UserRole.VIEWER);

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("user-c")).thenReturn(Optional.of(stranger));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            commentService.deleteComment("c1", "user-c"));
        
        verify(commentRepository, never()).deleteById(anyString());
    }

    @Test
    void testGetCommentById_notFound_throwsException() {
        when(commentRepository.findById("invalid-id")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
            commentService.getCommentById("invalid-id"));
    }

    @Test
    void testCreateComment_PhotoNotFound_ThrowsException() {
        UserEntity user = new UserEntity("u1", "U", "u@t.com", UserRole.VIEWER);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(photoRepository.findById("p-invalid")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            commentService.createComment(new CreateCommentRequest("p-invalid", "u1", "test")));
    }

    @Test
    void testCreateComment_Architecture_AllowedForCreator() {
        UserEntity creator = new UserEntity("u1", "C", "c@t.com", UserRole.CONTENT_CREATOR);
        PhotoEntity archiPhoto = new PhotoEntity("p1", "T", "D", "L", "U", 0, "architecture", "owner");
        CommentEntity comment = new CommentEntity("p1", "u1", "text");

        when(userRepository.findById("u1")).thenReturn(Optional.of(creator));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(archiPhoto));
        when(commentRepository.save(any())).thenReturn(comment);

        assertNotNull(commentService.createComment(new CreateCommentRequest("p1", "u1", "text")));
    }

    @Test
    void testDeleteComment_PhotoNotFound_ThrowsException() {
        CommentEntity comment = new CommentEntity("c1", "p1", "u1", "text", java.time.LocalDateTime.now());
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(photoRepository.findById("p1")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.deleteComment("c1", "u1"));
    }

    @Test
    void testDeleteComment_RequesterUserNotFound_ThrowsException() {
        CommentEntity comment = new CommentEntity("c1", "p1", "u1", "text", java.time.LocalDateTime.now());
        PhotoEntity photo = new PhotoEntity("p1", "T", "D", "L", "U", 0, "nature", "owner");
        
        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("u-ghost")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.deleteComment("c1", "u-ghost"));
    }

    @Test
    void testDeleteComment_SuccessByAuthorOnly() {
        CommentEntity comment = new CommentEntity("c1", "p1", "author-id", "text", java.time.LocalDateTime.now());
        PhotoEntity photo = new PhotoEntity("p1", "T", "D", "L", "U", 0, "nature", "another-owner");
        UserEntity requester = new UserEntity("author-id", "A", "a@t.com", UserRole.VIEWER);

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(photoRepository.findById("p1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("author-id")).thenReturn(Optional.of(requester));

        commentService.deleteComment("c1", "author-id");
        verify(commentRepository).deleteById("c1");
    }

    @Test
    void testGetCommentsByImage_ReturnsList() {
        CommentEntity c = new CommentEntity("c1", "p1", "u1", "text", java.time.LocalDateTime.now());
        when(commentRepository.findByImageId("p1")).thenReturn(List.of(c));

        List<CommentResponse> result = commentService.getCommentsByImage("p1");
        assertEquals(1, result.size());
    }

    @Test
void testGetCommentsByImage_ReturnsPopulatedList() {
    // ARRANGE
    CommentEntity c1 = new CommentEntity("c1", "p1", "u1", "First", LocalDateTime.now());
    CommentEntity c2 = new CommentEntity("c2", "p1", "u2", "Second", LocalDateTime.now());
    
    when(commentRepository.findByImageId("p1")).thenReturn(List.of(c1, c2));

    // ACT
    List<CommentResponse> results = commentService.getCommentsByImage("p1");

    // ASSERT
    assertEquals(2, results.size());
    assertEquals("First", results.get(0).text());
    assertEquals("Second", results.get(1).text());
}
}