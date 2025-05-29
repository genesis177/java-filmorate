package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
    }


    private long createTestUser(String email, String login, String name) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2000, 1, 1));

        String response = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Предположим, что API возвращает созданного пользователя в JSON
        User createdUser = mapper.readValue(response, User.class);
        return createdUser.getId();
    }

    @Test
    public void createUser_ShouldReturnStatus201AndCorrectBody() throws Exception {
        String json = "{ \"email\": \"testuser@example.com\", \"login\": \"test_login\", \"name\": \"Test User\", \"birthday\": \"2000-01-01\" }";

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.login").value("test_login"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.birthday").value("2000-01-01"));
    }

    @Test
    public void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        User user = new User();
        user.setEmail("invalid email");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getUserById_ShouldReturnUser() throws Exception {
        long id = createTestUser("getuser@example.com", "get login", "Get User");
        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int)id))
                .andExpect(jsonPath("$.email").value("getuser@example.com"))
                .andExpect(jsonPath("$.login").value("get login"))
                .andExpect(jsonPath("$.name").value("Get User"))
                .andExpect(jsonPath("$.birthday").value("2000-01-01"));
    }

    @Test
    public void getUserById_NotFound_ShouldReturn404() throws Exception {
        mvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_ShouldReturnUpdatedUser() throws Exception {
        long id = createTestUser("update@example.com", "update_login", "Update");

        String json = "{ \"id\": " + id + ", \"email\": \"update@example.com\", \"login\": \"update_login\", \"name\": \"Updated Name\", \"birthday\": \"1990-12-12\" }";

        // Expect status 200 OK (not 201)
        mvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk()) // changed here
                .andExpect(jsonPath("$.name").value("Updated Name"));

        // Additional check
        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }


    @Test
    public void deleteUser_ShouldRemoveUser() throws Exception {
        long id = createTestUser("delete@example.com", "deleteLogin", "Delete");

        // Удаляем пользователя
        mvc.perform(delete("/users/" + id))
                .andExpect(status().isNoContent()); // 204

        // Проверяем, что пользователь удален
        mvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createUser_WithFutureBirthday_ShouldReturn400() throws Exception {
        User user = new User();
        user.setEmail("futurebday@example.com");
        user.setLogin("future");
        user.setName("Future Birthday");
        user.setBirthday(java.time.LocalDate.now().plusDays(1)); // дата в будущем

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUser_NotFound_ShouldReturn404() throws Exception {
        User user = new User();
        user.setId(99999L);
        user.setEmail("nonexistent@example.com");
        user.setLogin("nonexistent");
        user.setName("Nonexistent");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));

        mvc.perform(put("/users/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createUser_WithBlankLogin_ShouldReturn400() throws Exception {
        User user = new User();
        user.setEmail("blanklogin@example.com");
        user.setLogin(" "); // пробел
        user.setName("Test");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }
}