package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.start.FinalProjectApplication;

@SpringBootTest(classes = FinalProjectApplication.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private long createTestUser(String email, String login, String name) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        String content = mapper.writeValueAsString(user);
        String response = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(response, User.class).getId();
    }

    @Test
    public void createUser_ShouldReturnStatus200AndBody() throws Exception {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.login").value("testlogin"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
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
        long id = createTestUser("getuser@example.com", "getlogin", "Get User");

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
        long id = createTestUser("update@example.com", "updatelogin", "Update");
        User user = new User();
        user.setId((int) id);
        user.setEmail("update@example.com");
        user.setLogin("update login");
        user.setName("Updated Name");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        mvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    public void deleteUser_ShouldRemoveUser() throws Exception {
        long id = createTestUser("delete@example.com", "deleteLogin", "Delete");
        mvc.perform(delete("/users/" + id))
                .andExpect(status().isOk());

        // Проверка, что пользователь удален
        mvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }
}