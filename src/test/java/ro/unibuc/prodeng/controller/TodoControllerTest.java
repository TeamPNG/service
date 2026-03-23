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

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;
import ro.unibuc.prodeng.service.TodoService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@DisplayName("TodoController Unit Tests")
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private TodoResponse testTodo1 = new TodoResponse("todo-1", "Complete project", false, "Alice", "alice@example.com");
    private TodoResponse testTodo2 = new TodoResponse("todo-2", "Write documentation", true, "Alice", "alice@example.com");
    private CreateTodoRequest createTodoRequest = new CreateTodoRequest("Complete project", "alice@example.com");
    private AssignTodoRequest assignTodoRequest = new AssignTodoRequest("bob@example.com");
    private EditTodoRequest editTodoRequest = new EditTodoRequest("Updated description");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();
    }

    // ==================== Get Todos By User Email ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Get todos by user email with multiple todos should return list")
    void testGetTodosByUserEmail_withMultipleTodos_returnsList() throws Exception {
        // Arrange
        List<TodoResponse> todos = Arrays.asList(testTodo1, testTodo2);
        when(todoService.getTodosByUserEmail("alice@example.com")).thenReturn(todos);

        // Act & Assert
        mockMvc.perform(get("/api/todos")
                .param("assigneeEmail", "alice@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("todo-1")))
                .andExpect(jsonPath("$[0].description", is("Complete project")))
                .andExpect(jsonPath("$[0].done", is(false)))
                .andExpect(jsonPath("$[0].assigneeName", is("Alice")))
                .andExpect(jsonPath("$[0].assigneeEmail", is("alice@example.com")))
                .andExpect(jsonPath("$[1].id", is("todo-2")))
                .andExpect(jsonPath("$[1].description", is("Write documentation")))
                .andExpect(jsonPath("$[1].done", is(true)))
                .andExpect(jsonPath("$[1].assigneeName", is("Alice")))
                .andExpect(jsonPath("$[1].assigneeEmail", is("alice@example.com")));

        verify(todoService, times(1)).getTodosByUserEmail("alice@example.com");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Get todos by user email with no todos should return empty list")
    void testGetTodosByUserEmail_withNoTodos_returnsEmptyList() throws Exception {
        // Arrange
        when(todoService.getTodosByUserEmail("alice@example.com")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/todos")
                .param("assigneeEmail", "alice@example.com")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(todoService, times(1)).getTodosByUserEmail("alice@example.com");
    }

    // ==================== Get Todo By ID ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Get existing todo by ID should return todo")
    void testGetTodoById_existingTodoRequested_returnsTodo() throws Exception {
        // Arrange
        when(todoService.getTodoById("todo-1")).thenReturn(testTodo1);

        // Act & Assert
        mockMvc.perform(get("/api/todos/{id}", "todo-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo-1")))
                .andExpect(jsonPath("$.description", is("Complete project")))
                .andExpect(jsonPath("$.done", is(false)))
                .andExpect(jsonPath("$.assigneeName", is("Alice")))
                .andExpect(jsonPath("$.assigneeEmail", is("alice@example.com")));

        verify(todoService, times(1)).getTodoById("todo-1");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Get non-existing todo by ID should return 404 Not Found")
    void testGetTodoById_nonExistingTodoRequested_returnsNotFound() throws Exception {
        // Arrange
        when(todoService.getTodoById("non-existing")).thenThrow(new EntityNotFoundException("Todo not found with id: non-existing"));

        // Act & Assert
        mockMvc.perform(get("/api/todos/{id}", "non-existing")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).getTodoById("non-existing");
    }

    // ==================== Create Todo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create todo with valid request should return created todo")
    void testCreateTodo_validRequestProvided_createsAndReturnsTodo() throws Exception {
        // Arrange
        when(todoService.createTodo(any(CreateTodoRequest.class))).thenReturn(testTodo1);

        // Act & Assert
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTodoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Complete project")))
                .andExpect(jsonPath("$.done", is(false)))
                .andExpect(jsonPath("$.assigneeEmail", is("alice@example.com")))
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(todoService, times(1)).createTodo(any(CreateTodoRequest.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Create todo with non-existing assignee should return 404 Not Found")
    void testCreateTodo_nonExistentAssignee_returnsNotFound() throws Exception {
        // Arrange
        CreateTodoRequest request = new CreateTodoRequest("Do something", "nonexistent@example.com");
        when(todoService.createTodo(any(CreateTodoRequest.class)))
                .thenThrow(new EntityNotFoundException("User not found with email: nonexistent@example.com"));

        // Act & Assert
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).createTodo(any(CreateTodoRequest.class));
    }

    // ==================== Set Done ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Mark todo as done should update and return updated todo")
    void testSetDone_changingStatusToTrue_returnsUpdatedTodo() throws Exception {
        // Arrange
        TodoResponse completedTodo = new TodoResponse("todo-1", "Complete project", true, "Alice", "alice@example.com");
        when(todoService.setDone("todo-1", true)).thenReturn(completedTodo);

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/done", "todo-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo-1")))
                .andExpect(jsonPath("$.done", is(true)));

        verify(todoService, times(1)).setDone("todo-1", true);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Mark todo as not done should update and return updated todo")
    void testSetDone_changingStatusToFalse_returnsUpdatedTodo() throws Exception {
        // Arrange
        TodoResponse uncompletedTodo = new TodoResponse("todo-2", "Write documentation", false, "Alice", "alice@example.com");
        when(todoService.setDone("todo-2", false)).thenReturn(uncompletedTodo);

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/done", "todo-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo-2")))
                .andExpect(jsonPath("$.done", is(false)));

        verify(todoService, times(1)).setDone("todo-2", false);
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Set done on non-existing todo should return 404 Not Found")
    void testSetDone_nonExistentTodo_returnsNotFound() throws Exception {
        // Arrange
        when(todoService.setDone("non-existing", true))
                .thenThrow(new EntityNotFoundException("Todo not found with id: non-existing"));

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/done", "non-existing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("true"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).setDone("non-existing", true);
    }

    // ==================== Assign Todo ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Assign todo to another user should return updated todo")
    void testAssignTodo_reassigningToAnotherUser_returnsUpdatedTodo() throws Exception {
        // Arrange
        TodoResponse reassignedTodo = new TodoResponse("todo-1", "Complete project", false, "Bob", "bob@example.com");
        when(todoService.assign("todo-1", assignTodoRequest)).thenReturn(reassignedTodo);

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/assignee", "todo-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignTodoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeName", is("Bob")))
                .andExpect(jsonPath("$.assigneeEmail", is("bob@example.com")));

        verify(todoService, times(1)).assign(eq("todo-1"), any(AssignTodoRequest.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Assign todo to non-existing user should return 404 Not Found")
    void testAssignTodo_nonExistentUser_returnsNotFound() throws Exception {
        // Arrange
        AssignTodoRequest request = new AssignTodoRequest("nonexistent@example.com");
        when(todoService.assign("todo-1", request))
                .thenThrow(new EntityNotFoundException("User not found with email: nonexistent@example.com"));

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/assignee", "todo-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).assign(eq("todo-1"), any(AssignTodoRequest.class));
    }

    // ==================== Edit Description ====================

    @SuppressWarnings("null")
    @Test
    @DisplayName("Edit todo description should return updated todo")
    void testEditDescription_validDescription_returnsUpdatedTodo() throws Exception {
        // Arrange
        TodoResponse updatedTodo = new TodoResponse("todo-1", "Updated description", false, "Alice", "alice@example.com");
        when(todoService.edit("todo-1", editTodoRequest)).thenReturn(updatedTodo);

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/description", "todo-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editTodoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated description")));

        verify(todoService, times(1)).edit(eq("todo-1"), any(EditTodoRequest.class));
    }

    @SuppressWarnings("null")
    
    @Test
    @DisplayName("Edit description of non-existing todo should return 404 Not Found")
    void testEditDescription_nonExistentTodo_returnsNotFound() throws Exception {
        // Arrange
        when(todoService.edit("non-existing", editTodoRequest))
                .thenThrow(new EntityNotFoundException("Todo not found with id: non-existing"));

        // Act & Assert
        mockMvc.perform(patch("/api/todos/{id}/description", "non-existing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editTodoRequest)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).edit(eq("non-existing"), any(EditTodoRequest.class));
    }

    // ==================== Delete Todo ====================

    @Test
    @DisplayName("Delete existing todo should return 204 No Content")
    void testDeleteTodo_existingTodoRequested_deletesSuccessfully() throws Exception {
        // Arrange
        doNothing().when(todoService).deleteTodo("todo-1");

        // Act & Assert
        mockMvc.perform(delete("/api/todos/{id}", "todo-1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).deleteTodo("todo-1");
    }

    @Test
    @DisplayName("Delete non-existing todo should return 404 Not Found")
    void testDeleteTodo_nonExistentTodo_returnsNotFound() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("Todo not found with id: non-existing"))
                .when(todoService).deleteTodo("non-existing");

        // Act & Assert
        mockMvc.perform(delete("/api/todos/{id}", "non-existing"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).deleteTodo("non-existing");
    }
}
