package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.PhotoResponse;
import ro.unibuc.prodeng.service.PhotoService;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@DisplayName("PhotoController Unit Tests")
class PhotoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PhotoService photoService;

    @InjectMocks
    private PhotoController photoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(photoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== Get All Photos ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET all photos when photos exist should return list with status OK")
    void testGetAllPhotos_whenPhotosExist_returnsOkWithPhotos() throws Exception {
        // Arrange
        List<PhotoResponse> photos = Arrays.asList(
                new PhotoResponse("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1"),
                new PhotoResponse("photo-2", "Forest", "Dark forest", "Carpathians", "url2", 0, "nature", "user-2"));
        when(photoService.getAllPhotos()).thenReturn(photos);

        // Act & Assert
        mockMvc.perform(get("/api/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("photo-1")))
                .andExpect(jsonPath("$[0].title", is("Cat")))
                .andExpect(jsonPath("$[1].id", is("photo-2")))
                .andExpect(jsonPath("$[1].title", is("Forest")));

        verify(photoService, times(1)).getAllPhotos();
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET all photos when none exist should return empty list with OK")
    void testGetAllPhotos_whenNoPhotosExist_returnsOkWithEmptyList() throws Exception {
        // Arrange
        when(photoService.getAllPhotos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/photos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(photoService, times(1)).getAllPhotos();
    }

    // ==================== Get Photo By ID ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET photo by existing ID should return photo with OK status")
    void testGetPhotoById_existingPhotoId_returnsOkWithPhoto() throws Exception {
        // Arrange
        PhotoResponse photo = new PhotoResponse("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals",
                "user-1");
        when(photoService.getPhotoById("photo-1")).thenReturn(photo);

        // Act & Assert
        mockMvc.perform(get("/api/photos/{id}", "photo-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("photo-1")))
                .andExpect(jsonPath("$.title", is("Cat")))
                .andExpect(jsonPath("$.category", is("animals")))
                .andExpect(jsonPath("$.description", is("Cute cat")));

        verify(photoService, times(1)).getPhotoById("photo-1");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET photo by non-existing ID should return NOT_FOUND status")
    void testGetPhotoById_nonExistingPhotoId_returnsNotFound() throws Exception {
        // Arrange
        when(photoService.getPhotoById("non-existing")).thenThrow(new EntityNotFoundException("Photo not found"));

        // Act & Assert
        mockMvc.perform(get("/api/photos/{id}", "non-existing")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(photoService, times(1)).getPhotoById("non-existing");
    }

    // ==================== Get Photos By User ID ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET photos by user ID should return user's photos with OK status")
    void testGetPhotoByUserId_whenPhotosExist_returnsOkWithPhotos() throws Exception {
        // Arrange
        List<PhotoResponse> userPhotos = Arrays.asList(
                new PhotoResponse("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1"),
                new PhotoResponse("photo-2", "Dog", "Happy dog", "Constanta", "url2", 0, "animals", "user-1"));
        when(photoService.getPhotoByUserId("user-1")).thenReturn(userPhotos);

        // Act & Assert
        mockMvc.perform(get("/api/photos/user/{userId}", "user-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].uploadedBydUserId", is("user-1")))
                .andExpect(jsonPath("$[1].uploadedBydUserId", is("user-1")));

        verify(photoService, times(1)).getPhotoByUserId("user-1");
    }

    // ==================== Create Photo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST create photo with valid data should return CREATED status")
    void testCreatePhoto_validRequest_returnsCreatedStatus() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "Alps", "user-1", "url");
        PhotoResponse createdPhoto = new PhotoResponse("photo-123", "Mountain", "Beautiful mountain", "Alps", "url", 0,
                "animals", "user-1");
        when(photoService.createPhoto(any(CreatePhotoRequest.class))).thenReturn(createdPhoto);

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("photo-123")))
                .andExpect(jsonPath("$.title", is("Mountain")))
                .andExpect(jsonPath("$.category", is("animals")));

        verify(photoService, times(1)).createPhoto(any(CreatePhotoRequest.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST create photo by viewer should return BAD_REQUEST")
    void testCreatePhoto_viewerRole_returnsBadRequest() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "Alps", "user-1", "url");
        when(photoService.createPhoto(any(CreatePhotoRequest.class)))
                .thenThrow(new IllegalArgumentException("Only content creators can upload photos"));

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(photoService, times(1)).createPhoto(any(CreatePhotoRequest.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST create photo with unsupported category should return BAD_REQUEST")
    void testCreatePhoto_unsupportedCategory_returnsBadRequest() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Photo", "unsupported", "Description", "location", "user-1", "url");
        when(photoService.createPhoto(any(CreatePhotoRequest.class)))
                .thenThrow(new IllegalArgumentException("User cannot upload to category"));

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(photoService, times(1)).createPhoto(any(CreatePhotoRequest.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST create photo with non-existing user should return NOT_FOUND")
    void testCreatePhoto_nonExistingUser_returnsNotFound() throws Exception {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "Alps", "user-1", "url");
        when(photoService.createPhoto(any(CreatePhotoRequest.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(photoService, times(1)).createPhoto(any(CreatePhotoRequest.class));
    }

    // ==================== Get Photos By Category ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET photos by category should return photos in category with OK")
    void testGetPhotosByCategory_withPhotosInCategory_returnsOkWithPhotos() throws Exception {
        // Arrange
        List<PhotoResponse> categoryPhotos = Arrays.asList(
                new PhotoResponse("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1"),
                new PhotoResponse("photo-2", "Dog", "Happy dog", "Constanta", "url2", 0, "animals", "user-2"));
        when(photoService.getPhotosByCategory("animals")).thenReturn(categoryPhotos);

        // Act & Assert
        mockMvc.perform(get("/api/photos/category")
                .param("category", "animals")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("animals")))
                .andExpect(jsonPath("$[1].category", is("animals")));

        verify(photoService, times(1)).getPhotosByCategory("animals");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("GET photos by category with no photos should return empty list with OK")
    void testGetPhotosByCategory_noPhotosInCategory_returnsOkWithEmpty() throws Exception {
        // Arrange
        when(photoService.getPhotosByCategory("sports")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/photos/category")
                .param("category", "sports")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(photoService, times(1)).getPhotosByCategory("sports");
    }

    // ==================== Delete Photo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("DELETE photo by owner should return NO_CONTENT status")
    void testDeletePhoto_ownerDeletesPhoto_returnsNoContent() throws Exception {
        // Arrange
        doNothing().when(photoService).deletePhoto("photo-1", "user-1");

        // Act & Assert
        mockMvc.perform(delete("/api/photos/{id}", "photo-1")
                .param("userId", "user-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(photoService, times(1)).deletePhoto("photo-1", "user-1");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("DELETE photo as viewer should return BAD_REQUEST")
    void testDeletePhoto_viewerAttemptsDelete_returnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Only content creators can delete")).when(photoService)
                .deletePhoto("photo-1", "user-2");

        // Act & Assert
        mockMvc.perform(delete("/api/photos/{id}", "photo-1")
                .param("userId", "user-2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(photoService, times(1)).deletePhoto("photo-1", "user-2");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("DELETE another user's photo should return BAD_REQUEST")
    void testDeletePhoto_userDeletesAnotherUsersPhoto_returnsBadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("User does not own this photo")).when(photoService)
                .deletePhoto("photo-1", "user-2");

        // Act & Assert
        mockMvc.perform(delete("/api/photos/{id}", "photo-1")
                .param("userId", "user-2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(photoService, times(1)).deletePhoto("photo-1", "user-2");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("DELETE non-existing photo should return NOT_FOUND")
    void testDeletePhoto_nonExistingPhoto_returnsNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Photo not found")).when(photoService)
                .deletePhoto("non-existing", "user-1");

        // Act & Assert
        mockMvc.perform(delete("/api/photos/{id}", "non-existing")
                .param("userId", "user-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(photoService, times(1)).deletePhoto("non-existing", "user-1");
    }
}
