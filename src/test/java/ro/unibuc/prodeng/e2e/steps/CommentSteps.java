package ro.unibuc.prodeng.e2e.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ro.unibuc.prodeng.request.CreateCommentRequest;
import ro.unibuc.prodeng.request.CreatePhotoRequest;
import ro.unibuc.prodeng.response.CommentResponse;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.response.UserResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CommentSteps {

    private final String BASE_URL = "http://localhost:8080/api";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private ResponseEntity<String> lastResponse;
    private String currentUserId;
    private String currentPhotoId = "p1";

    @Given("a user named {string} with email {word} exists")
    public void a_user_exists(String name, String email) {
        CreateUserRequest request = new CreateUserRequest(name, email, ro.unibuc.prodeng.model.UserRole.CONTENT_CREATOR);
        
        try {
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(BASE_URL + "/users", request, UserResponse.class);
        currentUserId = response.getBody().id();
    } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(BASE_URL + "/users/by-email?email=" + email, UserResponse.class);
        currentUserId = response.getBody().id();
    }
    }

    @Given("a photo exists in the system")
    public void a_photo_exists() {
        String uniqueTitle = "Test Photo " + System.currentTimeMillis();

        CreatePhotoRequest photoReq = new CreatePhotoRequest(
            uniqueTitle,
            "nature",
            "Description Lab 5",
            "Bucharest",
            currentUserId,
            "http://foto.it"
        );

        try {
            ResponseEntity<ro.unibuc.prodeng.response.PhotoResponse> response = restTemplate.postForEntity(
                BASE_URL + "/photos", 
                photoReq, 
                ro.unibuc.prodeng.response.PhotoResponse.class
            );
            
            this.currentPhotoId = response.getBody().id();
            System.out.println("Photo created successfully! Real ID: " + currentPhotoId);
        } catch (Exception e) {
            System.out.println("Creation error, trying to recover existing photo...");
            ResponseEntity<ro.unibuc.prodeng.response.PhotoResponse[]> response = 
            restTemplate.getForEntity(BASE_URL + "/photos", ro.unibuc.prodeng.response.PhotoResponse[].class);
        
            if (response.getBody() != null && response.getBody().length > 0) {
                this.currentPhotoId = response.getBody()[0].id();
            }
        }
    }

    @When("the client adds a comment {string} to that photo")
    public void the_client_adds_a_comment(String text) {
        System.out.println("Send comment for Photo: " + currentPhotoId + " and User: " + currentUserId);
        
        CreateCommentRequest request = new CreateCommentRequest(currentPhotoId, currentUserId, text);
        try {
            lastResponse = restTemplate.postForEntity(BASE_URL + "/comments", request, String.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.out.println("Server error: " + e.getResponseBodyAsString());
            lastResponse = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        }
    }

    @Then("the comment response status code is {int}")
    public void verify_status_code(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value(), is(expectedStatus));
    }

    @Then("the comment text is {string}")
    public void verify_comment_text(String expectedText) throws Exception {
        CommentResponse response = objectMapper.readValue(lastResponse.getBody(), CommentResponse.class);
        assertThat(response.text(), is(expectedText));
    }
}