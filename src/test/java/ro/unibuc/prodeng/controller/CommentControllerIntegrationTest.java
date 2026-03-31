package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.model.PhotoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.model.UserRole;
import ro.unibuc.prodeng.repository.CommentRepository;
import ro.unibuc.prodeng.repository.PhotoRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateCommentRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CommentController Integration Tests")
class CommentControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @BeforeEach
    void cleanUp() {
        commentRepository.deleteAll();
        userRepository.deleteAll();
        photoRepository.deleteAll();
    }

    @Test
    void testCreateComment_Integration() throws Exception {
        // ARRANGE
        userRepository.save(new UserEntity("u1", "Serena", "serena@test.com", UserRole.CONTENT_CREATOR));
        photoRepository.save(new PhotoEntity("p1", "Title", "Desc", "Loc", "url", 0, "nature", "u1"));

        CreateCommentRequest request = new CreateCommentRequest("p1", "u1", "Nice photo!");

        // ACT
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Nice photo!"));

        // ASSERT
        assertEquals(1, commentRepository.count(), "The comment should have been saved in the database");
        assertEquals("Nice photo!", commentRepository.findAll().get(0).text());
    }
}