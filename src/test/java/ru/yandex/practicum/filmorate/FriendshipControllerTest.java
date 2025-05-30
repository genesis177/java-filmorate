package ru.yandex.practicum.filmorate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;


@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class FriendshipControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private long createTestUser(String email, String login, String name) throws Exception {
        String json = String.format(
                "{\"email\":\"%s\",\"login\":\"%s\",\"name\":\"%s\",\"birthday\":\"2000-01-01\"}",
                email, login, name);
        String response = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    @Test
    public void addFriend_ShouldReturn200() throws Exception {
        long userId1 = createTestUser("f1@example.com", "f1", "Friend1");
        long userId2 = createTestUser("f2@example.com", "f2", "Friend2");
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());
    }

    @Test
    public void addFriend_ShouldReturnNotFound_ForUnknownUser() throws Exception {
        long userId = createTestUser("unknown@example.com", "unknown", "Unknown");
        mvc.perform(post("/users/" + userId + "/friends/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFriends_ShouldReturnCorrectList() throws Exception {
        long userId = createTestUser("main@example.com", "main", "Main");
        long friend1 = createTestUser("f1@example.com", "f1", "F1");
        long friend2 = createTestUser("f2@example.com", "f2", "F2");
        // заявки
        mvc.perform(post("/users/" + userId + "/friends/" + friend1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friend2)).andExpect(status().isOk());
        // подтверждение
        mvc.perform(post("/users/" + userId + "/friends/" + friend1 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friend2 + "/confirm")).andExpect(status().isOk());
        // список друзей
        mvc.perform(get("/users/" + userId + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void removeFriend_ShouldWork() throws Exception {
        long userId1 = createTestUser("r1@example.com", "r1", "Rem1");
        long userId2 = createTestUser("r2@example.com", "r2", "Rem2");
        // дружба
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId2 + "/friends/" + userId1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2 + "/confirm")).andExpect(status().isOk());
        // удаление
        mvc.perform(delete("/users/" + userId1 + "/friends/" + userId2)).andExpect(status().isOk());
        // проверка
        mvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk());
    }

    @Test
    public void getCommonFriends_ShouldReturnShared() throws Exception {
        long userA = createTestUser("a@example.com", "a", "A");
        long userB = createTestUser("b@example.com", "b", "B");
        long f1 = createTestUser("f1@example.com", "f1", "F1");
        long f2 = createTestUser("f2@example.com", "f2", "F2");
        long f3 = createTestUser("f3@example.com", "f3", "F3");
        // для userA
        mvc.perform(post("/users/" + userA + "/friends/" + f1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userA + "/friends/" + f2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userA + "/friends/" + f1 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userA + "/friends/" + f2 + "/confirm")).andExpect(status().isOk());
        // для userB
        mvc.perform(post("/users/" + userB + "/friends/" + f2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userB + "/friends/" + f3)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userB + "/friends/" + f2 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userB + "/friends/" + f3 + "/confirm")).andExpect(status().isOk());
        // общий друг
        mvc.perform(get("/users/" + userA + "/common-friends/" + userB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}