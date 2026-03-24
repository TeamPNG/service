package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.TodoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.model.UserRole;
import ro.unibuc.prodeng.repository.TodoRepository;
import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DisplayName("TodoService Unit Tests")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TodoService todoService;

    // ==================== Get Todos By User Email ====================

    @Test
    @DisplayName("Get todos by user email with multiple todos should return all todos")
    void testGetTodosByUserEmail_withMultipleTodos_returnsAllTodos() throws EntityNotFoundException {
        // Arrange
        List<TodoEntity> todos = Arrays.asList(
                new TodoEntity("todo-1", "Buy milk", false, "1"),
                new TodoEntity("todo-2", "Walk dog", true, "1")
        );
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);

        when(userService.getUserEntityByEmail("alice@example.com")).thenReturn(user);
        when(todoRepository.findByAssignedUserId("1")).thenReturn(todos);

        // Act
        List<TodoResponse> result = todoService.getTodosByUserEmail("alice@example.com");

        // Assert
        assertEquals(2, result.size());
        assertEquals("Buy milk", result.get(0).description());
        assertEquals("Walk dog", result.get(1).description());
        assertEquals("Alice", result.get(0).assigneeName());
        verify(userService, times(1)).getUserEntityByEmail("alice@example.com");
        verify(todoRepository, times(1)).findByAssignedUserId("1");
    }

    @Test
    @DisplayName("Get todos by user email with no todos should return empty list")
    void testGetTodosByUserEmail_withNoTodos_returnsEmptyList() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);
        when(userService.getUserEntityByEmail("alice@example.com")).thenReturn(user);
        when(todoRepository.findByAssignedUserId("1")).thenReturn(Arrays.asList());

        // Act
        List<TodoResponse> result = todoService.getTodosByUserEmail("alice@example.com");

        // Assert
        assertTrue(result.isEmpty());
        verify(userService, times(1)).getUserEntityByEmail("alice@example.com");
    }

    // ==================== Get Todo By ID ====================

    @Test
    @DisplayName("Get existing todo by ID should return todo with correct data")
    void testGetTodoById_existingTodoRequested_returnsTodo() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);
        TodoEntity todo = new TodoEntity("todo-1", "Complete project", false, "1");

        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityById("1")).thenReturn(user);

        // Act
        TodoResponse result = todoService.getTodoById("todo-1");

        // Assert
        assertNotNull(result);
        assertEquals("todo-1", result.id());
        assertEquals("Complete project", result.description());
        assertFalse(result.done());
        assertEquals("Alice", result.assigneeName());
        assertEquals("alice@example.com", result.assigneeEmail());
        verify(todoRepository, times(1)).findById("todo-1");
    }

    @Test
    @DisplayName("Get non-existing todo by ID should throw EntityNotFoundException")
    void testGetTodoById_nonExistingTodoRequested_throwsEntityNotFoundException() {
        // Arrange
        when(todoRepository.findById("non-existing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> todoService.getTodoById("non-existing"));
        verify(todoRepository, times(1)).findById("non-existing");
    }

    // ==================== Create Todo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create todo with valid data should save and return todo")
    void testCreateTodo_newTodoWithValidData_createsAndReturnsTodo() throws EntityNotFoundException {
        // Arrange
        UserEntity assignee = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);
        CreateTodoRequest request = new CreateTodoRequest("Finish unit tests", "alice@example.com");

        when(userService.getUserEntityByEmail("alice@example.com")).thenReturn(assignee);
        when(todoRepository.save(any(TodoEntity.class))).thenAnswer(invocation -> {
            TodoEntity entity = invocation.getArgument(0);
            // Simulate MongoDB generating an ID for new entities
            String id = "generated-todo-id-123";
            return new TodoEntity(id, entity.description(), entity.done(), entity.assignedUserId());
        });

        // Act
        TodoResponse result = todoService.createTodo(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("Finish unit tests", result.description());
        assertFalse(result.done());
        assertEquals("Alice", result.assigneeName());
        assertEquals("alice@example.com", result.assigneeEmail());
        verify(todoRepository, times(1)).save(any(TodoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create todo with non-existing assignee should throw EntityNotFoundException")
    void testCreateTodo_nonExistingAssigneeEmail_throwsEntityNotFoundException() throws EntityNotFoundException {
        // Arrange
        CreateTodoRequest request = new CreateTodoRequest("Do something", "nonexistent@example.com");
        when(userService.getUserEntityByEmail("nonexistent@example.com"))
                .thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> todoService.createTodo(request));
        verify(todoRepository, never()).save(any(TodoEntity.class));
    }

    // ==================== Set Done ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Set todo as done should update status to true")
    void testSetDone_changingStatusToTrue_updatesDoneStatus() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);
        TodoEntity todo = new TodoEntity("todo-1", "Buy milk", false, "1");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityById("1")).thenReturn(user);
        when(todoRepository.save(any(TodoEntity.class))).thenAnswer(invocation -> {
            TodoEntity entity = invocation.getArgument(0);
            return new TodoEntity(entity.id(), entity.description(), entity.done(), entity.assignedUserId());
        });

        // Act
        TodoResponse result = todoService.setDone("todo-1", true);

        // Assert
        assertTrue(result.done());
        verify(todoRepository, times(1)).save(any(TodoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Set todo as not done should update status to false")
    void testSetDone_changingStatusToFalse_updatesDoneStatus() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);
        TodoEntity todo = new TodoEntity("todo-1", "Buy milk", true, "1");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityById("1")).thenReturn(user);
        when(todoRepository.save(any(TodoEntity.class))).thenAnswer(invocation -> {
            TodoEntity entity = invocation.getArgument(0);
            return new TodoEntity(entity.id(), entity.description(), entity.done(), entity.assignedUserId());
        });

        // Act
        TodoResponse result = todoService.setDone("todo-1", false);

        // Assert
        assertFalse(result.done());
        verify(todoRepository, times(1)).save(any(TodoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Set done on non-existing todo should throw EntityNotFoundException")
    void testSetDone_nonExistingTodo_throwsEntityNotFoundException() {
        // Arrange
        when(todoRepository.findById("non-existing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> todoService.setDone("non-existing", true));
        verify(todoRepository, never()).save(any(TodoEntity.class));
    }

    // ==================== Assign Todo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Assign todo to another user should update assignee")
    void testAssignTodo_reassigningToAnotherUser_updatesAssignee() throws EntityNotFoundException {
        // Arrange
        UserEntity newUser = new UserEntity("2", "Bob", "bob@example.com", UserRole.VIEWER);
        TodoEntity todo = new TodoEntity("todo-1", "Buy milk", false, "1");
        AssignTodoRequest request = new AssignTodoRequest("bob@example.com");

        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityByEmail("bob@example.com")).thenReturn(newUser);
        when(todoRepository.save(any(TodoEntity.class))).thenAnswer(invocation -> {
            TodoEntity entity = invocation.getArgument(0);
            return new TodoEntity(entity.id(), entity.description(), entity.done(), entity.assignedUserId());
        });

        // Act
        TodoResponse result = todoService.assign("todo-1", request);

        // Assert
        assertEquals("Bob", result.assigneeName());
        assertEquals("bob@example.com", result.assigneeEmail());
        verify(todoRepository, times(1)).save(any(TodoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Assign to non-existing user should throw EntityNotFoundException")
    void testAssignTodo_nonExistingUser_throwsEntityNotFoundException() throws EntityNotFoundException {
        // Arrange
        TodoEntity todo = new TodoEntity("todo-1", "Buy milk", false, "1");
        AssignTodoRequest request = new AssignTodoRequest("nonexistent@example.com");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityByEmail("nonexistent@example.com"))
                .thenThrow(new EntityNotFoundException("User not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> todoService.assign("todo-1", request));
        verify(todoRepository, never()).save(any(TodoEntity.class));
    }

    // ==================== Edit Description ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Edit todo description should update description")
    void testEditDescription_changingDescription_updatesDescription() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", UserRole.VIEWER);
        TodoEntity todo = new TodoEntity("todo-1", "Buy milk", false, "1");
        EditTodoRequest request = new EditTodoRequest("Buy oat milk");

        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityById("1")).thenReturn(user);
        when(todoRepository.save(any(TodoEntity.class))).thenAnswer(invocation -> {
            TodoEntity entity = invocation.getArgument(0);
            return new TodoEntity(entity.id(), entity.description(), entity.done(), entity.assignedUserId());
        });

        // Act
        TodoResponse result = todoService.edit("todo-1", request);

        // Assert
        assertEquals("Buy oat milk", result.description());
        verify(todoRepository, times(1)).save(any(TodoEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Edit description of non-existing todo should throw EntityNotFoundException")
    void testEditDescription_nonExistingTodo_throwsEntityNotFoundException() {
        // Arrange
        EditTodoRequest request = new EditTodoRequest("New description");
        when(todoRepository.findById("non-existing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> todoService.edit("non-existing", request));
        verify(todoRepository, never()).save(any(TodoEntity.class));
    }

    // ==================== Delete Todo ====================

    @Test
    @DisplayName("Delete existing todo should remove it successfully")
    void testDeleteTodo_existingTodoRequested_deletesSuccessfully() throws EntityNotFoundException {
        // Arrange
        when(todoRepository.existsById("todo-1")).thenReturn(true);

        // Act
        todoService.deleteTodo("todo-1");

        // Assert
        verify(todoRepository, times(1)).deleteById("todo-1");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Delete non-existing todo should throw EntityNotFoundException")
    void testDeleteTodo_nonExistingTodoRequested_throwsEntityNotFoundException() {
        // Arrange
        when(todoRepository.existsById("non-existing")).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> todoService.deleteTodo("non-existing"));
        verify(todoRepository, never()).deleteById(anyString());
    }
}
