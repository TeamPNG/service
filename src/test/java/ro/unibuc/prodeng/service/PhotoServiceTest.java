package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.PhotoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.model.UserRole;
import ro.unibuc.prodeng.repository.PhotoRepository;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.PhotoResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DisplayName("PhotoService Unit Tests")
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PhotoService photoService;

    // ==================== Get All Photos ====================

    @Test
    @DisplayName("Get all photos when multiple exist should return all photos")
    void testGetAllPhotos_withMultiplePhotos_returnsAllPhotos() {
        // Arrange
        List<PhotoEntity> photos = Arrays.asList(
                new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1"),
                new PhotoEntity("photo-2", "Forest", "Dark forest", "Carpathians", "url2", 0, "nature", "user-2")
        );
        when(photoRepository.findAll()).thenReturn(photos);

        // Act
        List<PhotoResponse> result = photoService.getAllPhotos();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Cat", result.get(0).title());
        assertEquals("Forest", result.get(1).title());
        verify(photoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get all photos when none exist should return empty list")
    void testGetAllPhotos_whenNoPhotosExist_returnsEmptyList() {
        // Arrange
        when(photoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<PhotoResponse> result = photoService.getAllPhotos();

        // Assert
        assertTrue(result.isEmpty());
        verify(photoRepository, times(1)).findAll();
    }

    // ==================== Get Photo By ID ====================

    @Test
    @DisplayName("Get existing photo by ID should return photo with correct data")
    void testGetPhotoById_existingPhotoRequested_returnsPhoto() {
        // Arrange
        PhotoEntity photo = new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1");
        when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photo));

        // Act
        PhotoResponse result = photoService.getPhotoById("photo-1");

        // Assert
        assertNotNull(result);
        assertEquals("photo-1", result.id());
        assertEquals("Cat", result.title());
        assertEquals("Cute cat", result.description());
        assertEquals("animals", result.category());
        verify(photoRepository, times(1)).findById("photo-1");
    }

    @Test
    @DisplayName("Get non-existing photo by ID should throw EntityNotFoundException")
    void testGetPhotoById_nonExistingPhotoRequested_throwsEntityNotFoundException() {
        // Arrange
        when(photoRepository.findById("non-existing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> photoService.getPhotoById("non-existing"));
        verify(photoRepository, times(1)).findById("non-existing");
    }

    // ==================== Get Photo By User ID ====================

    @Test
    @DisplayName("Get photos by user ID should return all photos from that user")
    void testGetPhotoByUserId_withPhotosFromUser_returnsPhotos() {
        // Arrange
        List<PhotoEntity> userPhotos = Arrays.asList(
                new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1"),
                new PhotoEntity("photo-2", "Dog", "Happy dog", "Constanta", "url2", 0, "animals", "user-1")
        );
        when(photoRepository.getPhotoByuploadedBydUserId("user-1")).thenReturn(userPhotos);

        // Act
        List<PhotoResponse> result = photoService.getPhotoByUserId("user-1");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.uploadedBydUserId().equals("user-1")));
        verify(photoRepository, times(1)).getPhotoByuploadedBydUserId("user-1");
    }

    // ==================== Create Photo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create photo by content creator with valid data should save photo")
    void testCreatePhoto_contentCreatorWithValidData_createsPhoto() {
        // Arrange
        UserEntity user = new UserEntity("user-1", "Alice", "alice@example.com", UserRole.CONTENT_CREATOR);
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "location", "user-1", "url"
        );

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(photoRepository.findByTitle("Mountain")).thenReturn(Arrays.asList());
        when(photoRepository.save(any(PhotoEntity.class))).thenAnswer(invocation -> {
            PhotoEntity entity = invocation.getArgument(0);
            return new PhotoEntity("photo-123", entity.title(), entity.description(), entity.location(), 
                    entity.URL(), entity.likes(), entity.category(), entity.uploadedBydUserId());
        });

        // Act
        PhotoResponse result = photoService.createPhoto(request);

        // Assert
        assertNotNull(result);
        assertEquals("Mountain", result.title());
        assertEquals("animals", result.category());
        verify(photoRepository, times(1)).save(any(PhotoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create photo by viewer should throw IllegalArgumentException")
    void testCreatePhoto_viewerAttemptsToCreate_throwsIllegalArgumentException() {
        // Arrange
        UserEntity user = new UserEntity("user-1", "Alice", "alice@example.com", UserRole.VIEWER);
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "location", "user-1", "url"
        );

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> photoService.createPhoto(request));
        verify(photoRepository, never()).save(any(PhotoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create photo with non-allowed category should throw IllegalArgumentException")
    void testCreatePhoto_unsupportedCategory_throwsIllegalArgumentException() {
        // Arrange
        UserEntity user = new UserEntity("user-1", "Alice", "alice@example.com", UserRole.CONTENT_CREATOR);
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Photo", "unsupported-category", "Description", "location", "user-1", "url"
        );

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> photoService.createPhoto(request));
        verify(photoRepository, never()).save(any(PhotoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create photo with duplicate title should throw IllegalArgumentException")
    void testCreatePhoto_duplicateTitle_throwsIllegalArgumentException() {
        // Arrange
        UserEntity user = new UserEntity("user-1", "Alice", "alice@example.com", UserRole.CONTENT_CREATOR);
        PhotoEntity existingPhoto = new PhotoEntity("photo-1", "Mountain", "Old photo", "location", "url", 0, "animals", "user-2");
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "location", "user-1", "url"
        );

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(photoRepository.findByTitle("Mountain")).thenReturn(Arrays.asList(existingPhoto));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> photoService.createPhoto(request));
        verify(photoRepository, never()).save(any(PhotoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create photo with non-existing user should throw EntityNotFoundException")
    void testCreatePhoto_nonExistingUser_throwsEntityNotFoundException() {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest(
                "Mountain", "animals", "Beautiful mountain", "location", "user-1", "url"
        );

        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> photoService.createPhoto(request));
        verify(photoRepository, never()).save(any(PhotoEntity.class));
    }

    // ==================== Get Photos By Category ====================

    @Test
    @DisplayName("Get photos by category should return all photos in that category")
    void testGetPhotosByCategory_withPhotosInCategory_returnsPhotos() {
        // Arrange
        List<PhotoEntity> categoryPhotos = Arrays.asList(
                new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1"),
                new PhotoEntity("photo-2", "Dog", "Happy dog", "Constanta", "url2", 0, "animals", "user-2")
        );
        when(photoRepository.findByCategory("animals")).thenReturn(categoryPhotos);

        // Act
        List<PhotoResponse> result = photoService.getPhotosByCategory("animals");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.category().equals("animals")));
        verify(photoRepository, times(1)).findByCategory("animals");
    }

    @Test
    @DisplayName("Get photos by category with no photos should return empty list")
    void testGetPhotosByCategory_withNoPhotosInCategory_returnsEmptyList() {
        // Arrange
        when(photoRepository.findByCategory("sports")).thenReturn(Arrays.asList());

        // Act
        List<PhotoResponse> result = photoService.getPhotosByCategory("sports");

        // Assert
        assertTrue(result.isEmpty());
        verify(photoRepository, times(1)).findByCategory("sports");
    }

    // ==================== Delete Photo ====================

    @Test
    @DisplayName("Delete own photo by content creator should succeed")
    void testDeletePhoto_ownerDeletesOwnPhoto_succeeds() {
        // Arrange
        PhotoEntity photo = new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1");
        UserEntity user = new UserEntity("user-1", "Alice", "alice@example.com", UserRole.CONTENT_CREATOR);

        when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // Act
        photoService.deletePhoto("photo-1", "user-1");

        // Assert
        verify(photoRepository, times(1)).deleteById("photo-1");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Delete photo by viewer should throw IllegalArgumentException")
    void testDeletePhoto_viewerAttemptsDelete_throwsIllegalArgumentException() {
        // Arrange
        PhotoEntity photo = new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1");
        UserEntity user = new UserEntity("user-2", "Bob", "bob@example.com", UserRole.VIEWER);

        when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> photoService.deletePhoto("photo-1", "user-2"));
        verify(photoRepository, never()).deleteById(anyString());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Delete another user's photo should throw IllegalArgumentException")
    void testDeletePhoto_userDeletesAnotherUsersPhoto_throwsIllegalArgumentException() {
        // Arrange
        PhotoEntity photo = new PhotoEntity("photo-1", "Cat", "Cute cat", "Bucharest", "url1", 0, "animals", "user-1");
        UserEntity user = new UserEntity("user-2", "Bob", "bob@example.com", UserRole.CONTENT_CREATOR);

        when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photo));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> photoService.deletePhoto("photo-1", "user-2"));
        verify(photoRepository, never()).deleteById(anyString());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Delete non-existing photo should throw EntityNotFoundException")
    void testDeletePhoto_nonExistingPhoto_throwsEntityNotFoundException() {
        // Arrange
        UserEntity user = new UserEntity("user-1", "Alice", "alice@example.com", UserRole.CONTENT_CREATOR);

        when(photoRepository.findById("non-existing")).thenReturn(Optional.empty());
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> photoService.deletePhoto("non-existing", "user-1"));
        verify(photoRepository, never()).deleteById(anyString());
    }
}
