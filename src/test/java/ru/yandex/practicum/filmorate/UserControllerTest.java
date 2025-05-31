package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanDb() {
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    public void createUser_ShouldReturn201AndCorrectBody() throws Exception {
        String json = "{ " +
                "\"email\": \"testuser@example.com\", " +
                "\"login\": \"test_login\", " +
                "\"name\": \"Test User\", " +
                "\"birthday\": \"2000-01-01\" " +
                "}";

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.login").value("test_login"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.birthday").value("2000-01-01"));
    }

    @Test
    public void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        String json = "{ " +
                "\"email\": \"invalid email\", " +
                "\"login\": \"login\", " +
                "\"name\": \"Name\", " +
                "\"birthday\": \"1990-01-01\" " +
                "}";

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Create initial user
        String createJson = "{ " +
                "\"email\": \"update@example.com\", " +
                "\"login\": \"update_login\", " +
                "\"name\": \"Update\", " +
                "\"birthday\": \"2000-01-01\" " +
                "}";

        String response = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = mapper.readTree(response).get("id").asLong();

        // Update user
        String updateJson = "{ " +
                "\"id\": " + id + ", " +
                "\"email\": \"update@example.com.com\", " +
                "\"login\": \"update_login\", " +
                "\"name\": \"Updated Name\", " +
                "\"birthday\": \"1990-12-12\" " +
                "}";

        mvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.birthday").value("1990-12-12"));
    }

    @Test
    public void updateUser_WithUnknownId_ShouldReturn404() throws Exception {
        String updateJson = "{ " +
                "\"id\": 9999, " +
                "\"email\": \"nonexistent@example.com\", " +
                "\"login\": \"nonexistent\", " +
                "\"name\": \"Nonexistent\", " +
                "\"birthday\": \"1990-01-01\" " +
                "}";

        mvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not found"));
    }
}