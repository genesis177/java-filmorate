package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Test
    public void createUser_ShouldReturnStatus200AndBody() throws Exception {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setLogin("test login");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.login").value("test login"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    @Test
    public void createUser_WithInvalidEmail_ShouldReturn400() throws Exception {
        User user = new User();
        user.setEmail("invalid email");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getUserById_ShouldReturnUser() throws Exception {
        User user = new User();
        user.setEmail("getuser@example.com");
        user.setLogin("get login");
        user.setName("Get User");
        user.setBirthday(LocalDate.of(1985, 5, 5));
        String content = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();

        User createdUser = mapper.readValue(content, User.class);
        Integer id = createdUser.getId();

        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("getuser@example.com"));
    }

    @Test
    public void getUserById_NotFound_ShouldReturn404() throws Exception {
        mvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User user = new User();
        user.setEmail("update@example.com");
        user.setLogin("update login");
        user.setName("Update");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        String content = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();

        User createdUser = mapper.readValue(content, User.class);
        Integer id = createdUser.getId();

        createdUser.setName("Updated Name");
        String updatedContent = mapper.writeValueAsString(createdUser);

        mvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    public void deleteUser_ShouldRemoveUser() throws Exception {
        User user = new User();
        user.setEmail("delete@example.com");
        user.setLogin("delete login");
        user.setName("Delete");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        String content = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();

        User createdUser = mapper.readValue(content, User.class);
        Integer id = createdUser.getId();


        mvc.perform(delete("/users/" + id))
                .andExpect(status().isOk());

        mvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;
}