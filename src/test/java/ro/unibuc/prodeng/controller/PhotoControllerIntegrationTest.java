package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.model.PhotoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.model.UserRole;
import ro.unibuc.prodeng.repository.PhotoRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreatePhotoRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PhotoController Integration Tests")
class PhotoControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String contentCreatorUserId;
    private String regularUserId;

    @BeforeEach
    void setUp() {
        // Clean up databases before each test
        photoRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        UserEntity contentCreator = userRepository.save(
                new UserEntity("content-creator", "creator@example.com", UserRole.CONTENT_CREATOR));
        contentCreatorUserId = contentCreator.id();

        UserEntity regularUser = userRepository.save(
                new UserEntity("regular-user", "user@example.com", UserRole.VIEWER));
        regularUserId = regularUser.id();
    }

    // ==================== Create Photo Tests ====================

    @Test
    @DisplayName("POST create photo with valid request should return CREATED and persist to database")
    void testCreatePhoto_validRequest_returnsCreatedAndPersiststoDatabase() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Beautiful Mountain",
                "nature",
                "A stunning mountain landscape",
                "Alps",
                contentCreatorUserId,
                "https://example.com/mountain.jpg");

        // Act
        String response = mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Beautiful Mountain"))
                .andExpect(jsonPath("$.description").value("A stunning mountain landscape"))
                .andExpect(jsonPath("$.location").value("Alps"))
                .andExpect(jsonPath("$.category").value("nature"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        // Assert - verify database state
        String photoId = objectMapper.readTree(response).get("id").asText();
        PhotoEntity savedPhoto = photoRepository.findById(photoId).orElse(null);

        assertNotNull(savedPhoto, "Photo should be persisted to database");
        assertEquals("Beautiful Mountain", savedPhoto.title());
        assertEquals("A stunning mountain landscape", savedPhoto.description());
        assertEquals("Alps", savedPhoto.location());
        assertEquals("nature", savedPhoto.category());
        assertEquals(contentCreatorUserId, savedPhoto.uploadedBydUserId());
        assertEquals(0, savedPhoto.likes());
    }

    @Test
    @DisplayName("POST create photo with duplicate title should return 400")
    void testCreatePhoto_duplicateTitle_returnsBadRequest() throws Exception {
        // Arrange
        CreatePhotoRequest request1 = new CreatePhotoRequest(
                "Sunset Beach",
                "nature",
                "Beautiful sunset",
                "Maldives",
                contentCreatorUserId,
                "https://example.com/sunset1.jpg");

        // Create first photo
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Verify it's in database
        assertEquals(1, photoRepository.count());

        // Try to create photo with same title
        CreatePhotoRequest request2 = new CreatePhotoRequest(
                "Sunset Beach",
                "travel",
                "Different sunset",
                "Caribbean",
                contentCreatorUserId,
                "https://example.com/sunset2.jpg");

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());

        // Verify only first photo is in database
        assertEquals(1, photoRepository.count());
    }

    @Test
    @DisplayName("POST create photo with non-content-creator should return 400")
    void testCreatePhoto_nonContentCreator_returnsBadRequest() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Unauthorized Photo",
                "nature",
                "Regular user trying to upload",
                "Somewhere",
                regularUserId,
                "https://example.com/photo.jpg");

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify photo is not in database
        assertEquals(0, photoRepository.count());
    }

    @Test
    @DisplayName("POST create photo with non-existent user should return 404")
    void testCreatePhoto_nonExistentUser_returnsNotFound() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Photo by ghost",
                "nature",
                "Ghost user photo",
                "Somewhere",
                "non-existent-user-id",
                "https://example.com/photo.jpg");

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // Verify no photo is in database
        assertEquals(0, photoRepository.count());
    }

    // ==================== Get All Photos Tests ====================

    @Test
    @DisplayName("GET all photos when photos exist should return list and verify database")
    void testGetAllPhotos_whenPhotosExist_returnsListAndVerifiesDatabase() throws Exception {
        // Arrange - create multiple photos
        PhotoEntity photo1 = photoRepository.save(new PhotoEntity(
                "Sunset", "Beautiful sunset", "Beach", "https://example.com/sunset.jpg", "travel", contentCreatorUserId));
        PhotoEntity photo2 = photoRepository.save(new PhotoEntity(
                "Mountain", "Snow-capped peak", "Alps", "https://example.com/mountain.jpg", "nature", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(get("/api/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Sunset"))
                .andExpect(jsonPath("$[1].title").value("Mountain"));

        // Verify database count matches
        assertEquals(2, photoRepository.count());
    }

    @Test
    @DisplayName("GET all photos when none exist should return empty list")
    void testGetAllPhotos_whenNoPhotosExist_returnsEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify database is empty
        assertEquals(0, photoRepository.count());
    }

    // ==================== Get Photo By ID Tests ====================

    @Test
    @DisplayName("GET photo by ID when exists should return photo and verify database")
    void testGetPhotoById_whenExists_returnsPhotoAndVerifiesDatabase() throws Exception {
        // Arrange
        PhotoEntity photo = photoRepository.save(new PhotoEntity(
                "Test Photo", "Test description", "Test location", "https://example.com/test.jpg", "nature", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(get("/api/photos/" + photo.id())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(photo.id()))
                .andExpect(jsonPath("$.title").value("Test Photo"))
                .andExpect(jsonPath("$.description").value("Test description"));

        // Verify the photo still exists in database
        assertTrue(photoRepository.existsById(photo.id()));
    }

    @Test
    @DisplayName("GET photo by ID when not exists should return 404")
    void testGetPhotoById_whenNotExists_returnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/photos/non-existent-id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify database is empty
        assertEquals(0, photoRepository.count());
    }

    // ==================== Get Photos By User ID Tests ====================

    @Test
    @DisplayName("GET photos by user ID should return only photos by that user and verify database")
    void testGetPhotoByUserId_shouldReturnOnlyUserPhotos() throws Exception {
        // Arrange - create photos by different users
        photoRepository.save(new PhotoEntity(
                "Creator1 Photo", "desc1", "loc1", "url1", "nature", contentCreatorUserId));
        photoRepository.save(new PhotoEntity(
                "Creator2 Photo", "desc2", "loc2", "url2", "travel", regularUserId));
        
        UserEntity otherCreator = userRepository.save(
                new UserEntity("other-creator", "other@example.com", UserRole.CONTENT_CREATOR));
        photoRepository.save(new PhotoEntity(
                "Other Photo", "desc3", "loc3", "url3", "nature", otherCreator.id()));

        // Act & Assert
        mockMvc.perform(get("/api/photos/user/" + contentCreatorUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Creator1 Photo"))
                .andExpect(jsonPath("$[0].uploadedBydUserId").value(contentCreatorUserId));

        // Verify total photos in database
        assertEquals(3, photoRepository.count());
        assertEquals(1, photoRepository.getPhotoByuploadedBydUserId(contentCreatorUserId).size());
    }

    @Test
    @DisplayName("GET photos by user ID when user has no photos should return empty list")
    void testGetPhotoByUserId_whenUserHasNoPhotos_returnsEmptyList() throws Exception {
        // Arrange - create photo by different user
        photoRepository.save(new PhotoEntity(
                "Other Photo", "desc", "loc", "url", "nature", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(get("/api/photos/user/" + regularUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify database has photos from other user
        assertEquals(1, photoRepository.count());
    }

    // ==================== Delete Photo Tests ====================

    @Test
    @DisplayName("DELETE photo by owner should remove from database and return 204")
    void testDeletePhoto_byOwner_removesFromDatabaseAndReturnsNoContent() throws Exception {
        // Arrange
        PhotoEntity photo = photoRepository.save(new PhotoEntity(
                "To Delete", "This will be deleted", "Somewhere", "https://example.com/delete.jpg", "nature", contentCreatorUserId));

        // Verify photo exists in database
        assertTrue(photoRepository.existsById(photo.id()));

        // Act
        mockMvc.perform(delete("/api/photos/" + photo.id())
                .param("userId", contentCreatorUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Assert - verify photo is removed from database
        assertFalse(photoRepository.existsById(photo.id()));
        assertEquals(0, photoRepository.count());
    }

    @Test
    @DisplayName("DELETE photo by non-owner should return 400 and not delete")
    void testDeletePhoto_byNonOwner_returnsBadRequestAndDoesNotDelete() throws Exception {
        // Arrange
        PhotoEntity photo = photoRepository.save(new PhotoEntity(
                "Protected Photo", "Cannot delete", "Somewhere", "https://example.com/protected.jpg", "nature", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(delete("/api/photos/" + photo.id())
                .param("userId", regularUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify photo is still in database
        assertTrue(photoRepository.existsById(photo.id()));
        assertEquals(1, photoRepository.count());
    }

    @Test
    @DisplayName("DELETE non-existent photo should return 404 and not affect database")
    void testDeletePhoto_nonExistent_returnsNotFound() throws Exception {
        // Arrange
        PhotoEntity existingPhoto = photoRepository.save(new PhotoEntity(
                "Existing Photo", "desc", "loc", "url", "nature", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(delete("/api/photos/non-existent-id")
                .param("userId", contentCreatorUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify existing photo is still in database
        assertEquals(1, photoRepository.count());
        assertTrue(photoRepository.existsById(existingPhoto.id()));
    }

    // ==================== Get Photos by Category Tests ====================

    @Test
    @DisplayName("GET photos by category should return only photos in that category")
    void testGetPhotosByCategory_shouldReturnOnlyPhotosInCategory() throws Exception {
        // Arrange - create photos in different categories
        photoRepository.save(new PhotoEntity(
                "Beach Photo", "sandy beach", "Caribbean", "url1", "travel", contentCreatorUserId));
        photoRepository.save(new PhotoEntity(
                "Forest Photo", "dense forest", "Carpathians", "url2", "nature", contentCreatorUserId));
        photoRepository.save(new PhotoEntity(
                "Mountain Photo", "snowy peak", "Alps", "url3", "nature", contentCreatorUserId));
        photoRepository.save(new PhotoEntity(
                "City Photo", "urban landscape", "NYC", "url4", "travel", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(get("/api/photos/category")
                .param("category", "nature")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Verify total photos in database
        assertEquals(4, photoRepository.count());
        assertEquals(2, photoRepository.findByCategory("nature").size());
        assertEquals(2, photoRepository.findByCategory("travel").size());
    }

    @Test
    @DisplayName("GET photos by category when none exist should return empty list")
    void testGetPhotosByCategory_whenNoneExist_returnsEmptyList() throws Exception {
        // Arrange - create photos in different category
        photoRepository.save(new PhotoEntity(
                "Photo", "desc", "loc", "url", "nature", contentCreatorUserId));

        // Act & Assert
        mockMvc.perform(get("/api/photos/category")
                .param("category", "food")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify database has photos from other category
        assertEquals(1, photoRepository.count());
    }
}
