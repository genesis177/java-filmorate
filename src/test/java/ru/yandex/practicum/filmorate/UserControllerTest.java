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
                .andExpect(status().isCreated()) // ожидаем 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(response, User.class).getId();
    }

    //проверка, что создание пользователя возвращает статус 201 и правильное тело
    @Test
    public void createUser_ShouldReturnStatus201AndBody() throws Exception {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setLogin("test login");
        user.setName("Test Name");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test Name"))
                .andExpect(jsonPath("$.login").value("test login"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    //создание пользователя с некорректным email должно возвращать 400 Bad Request
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

    //получение пользователя по существующему ID
    @Test
    public void getUserById_ShouldReturnUser() throws Exception {
        long id = createTestUser("getuser@example.com", "get login", "Get User");

        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("getuser@example.com"));
    }

    //запрос несуществующего пользователя должен возвращать 404 Not Found
    @Test
    public void getUserById_NotFound_ShouldReturn404() throws Exception {
        mvc.perform(get("/users/9999"))
                .andExpect(status().isNotFound());
    }

    //обновление пользователя
    @Test
    public void updateUser_ShouldReturnUpdatedUser() throws Exception {
        long id = createTestUser("update@example.com", "update login", "Update");

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setEmail("update@example.com");

        // Можно оставить имя пустым или задать новое имя
        updatedUser.setLogin("update login");

        // Обновляем имя на новое значение
        updatedUser.setName("Updated Name");

        mvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        // Проверка, что обновление прошло успешно
        mvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

    }

    //удаление пользователя
    @Test
    public void deleteUser_ShouldRemoveUser() throws Exception {
        long id = createTestUser("delete@example.com", "deleteLogin", "Delete");

        // Удаляем пользователя
        mvc.perform(delete("/users/" + id))
                .andExpect(status().isOk());

        // Проверка, что пользователь удален - запрос должен вернуть 404
        mvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }

    //тест на проверку даты рождения в будущем
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

    //обновление несуществующего пользователя
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

    //тест на ошибки логина(например если он пустой)
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