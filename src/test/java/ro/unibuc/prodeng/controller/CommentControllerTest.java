package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.service.CommentService;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private CommentResponse testResponse = new CommentResponse("c1", "p1", "u1", "Nice post!", LocalDateTime.now());

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with the controller we want to test
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void testCreateComment_returnsCreated() throws Exception {
        // ARRANGE
        CreateCommentRequest request = new CreateCommentRequest("p1", "u1", "Nice post!");
        when(commentService.createComment(any(CreateCommentRequest.class))).thenReturn(testResponse);

        // ACT & ASSERT
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("c1")))
                .andExpect(jsonPath("$.text", is("Nice post!")));

        verify(commentService, times(1)).createComment(any());
    }

    @Test
    void testGetCommentsByImage_returnsList() throws Exception {
        // ARRANGE
        when(commentService.getCommentsByImage("p1")).thenReturn(Arrays.asList(testResponse));

        // ACT & ASSERT
        mockMvc.perform(get("/api/comments/image/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("c1")));
    }

    @Test
    void testDeleteComment_returnsNoContent() throws Exception {
        // ARRANGE
        doNothing().when(commentService).deleteComment("c1", "u1");

        // ACT & ASSERT
        mockMvc.perform(delete("/api/comments/c1")
                .param("userId", "u1")) // Importante: passiamo lo userId come parametro URL
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment("c1", "u1");
    }

    @Test
    void testGetCommentById_NotFound_Returns404() throws Exception {
        when(commentService.getCommentById("invalid")).thenThrow(new ro.unibuc.prodeng.exception.EntityNotFoundException("Comment"));

        mockMvc.perform(get("/api/comments/invalid"))
                .andExpect(status().isNotFound());
    }

  @Test
    void testCreateComment_InvalidRequest_Returns400() throws Exception {
        CreateCommentRequest invalidRequest = new CreateCommentRequest("", "", ""); 

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCommentById_notFound_returns404() throws Exception {
        when(commentService.getCommentById("invalid-id"))
                .thenThrow(new ro.unibuc.prodeng.exception.EntityNotFoundException("Comment not found"));

        mockMvc.perform(get("/api/comments/invalid-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteComment_invalidUser_returns400() throws Exception {
        // ARRANGE
        doThrow(new IllegalArgumentException("Not authorized"))
                .when(commentService).deleteComment("c1", "wrong-user");
        // ACT & ASSERT
       assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(delete("/api/comments/c1")
                    .param("userId", "wrong-user"));
        });
    }
}