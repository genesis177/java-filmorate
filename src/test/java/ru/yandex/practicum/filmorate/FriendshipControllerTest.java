package ru.yandex.practicum.filmorate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
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

        // Отправляем заявку на дружбу
        mvc.perform(put("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        // Подтверждаем заявку дружбы
        mvc.perform(post("/users/" + userId2 + "/friends/" + userId1 + "/confirm"))
                .andExpect(status().isOk());

        // Теперь друг должен отображаться
        mvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId2));

        mvc.perform(put("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void addFriend_ShouldReturnNotFound_ForUnknownUser() throws Exception {
        long userId = createTestUser("unknown@example.com", "unknown", "Unknown");
        mvc.perform(put("/users/" + userId + "/friends/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFriends_ShouldReturnCorrectList() throws Exception {
        long userId = createTestUser("main@example.com", "main", "Main");
        long friend1 = createTestUser("f1@example.com", "f1", "F1");
        long friend2 = createTestUser("f2@example.com", "f2", "F2");

        // userId отправляет заявки friend1 и friend2
        mvc.perform(put("/users/" + userId + "/friends/" + friend1)).andExpect(status().isOk());
        mvc.perform(put("/users/" + userId + "/friends/" + friend2)).andExpect(status().isOk());

        // friend1 и friend2 подтверждают заявки userId
        mvc.perform(post("/users/" + friend1 + "/friends/" + userId + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + friend2 + "/friends/" + userId + "/confirm")).andExpect(status().isOk());

        // список друзей userId должен содержать friend1 и friend2
        mvc.perform(get("/users/" + userId + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == %d)]", friend1).exists())
                .andExpect(jsonPath("$[?(@.id == %d)]", friend2).exists());
    }

    @Test
    public void removeFriend_ShouldWork() throws Exception {
        long userId1 = createTestUser("r1@example.com", "r1", "Rem1");
        long userId2 = createTestUser("r2@example.com", "r2", "Rem2");

        // userId2 отправляет заявку userId1
        mvc.perform(put("/users/" + userId2 + "/friends/" + userId1))
                .andExpect(status().isOk());

        // userId1 подтверждает заявку userId2
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2 + "/confirm"))
                .andExpect(status().isOk());

        // удаление из друзей (удаляем обе записи)
        mvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        mvc.perform(delete("/users/" + userId2 + "/friends/" + userId1))
                .andExpect(status().isOk());

        // проверка, что userId1 больше не имеет userId2 в друзьях
        mvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]", userId2).doesNotExist());

        // проверка, что userId2 больше не имеет userId1 в друзьях
        mvc.perform(get("/users/" + userId2 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]", userId1).doesNotExist());
    }

    @Test
    public void getCommonFriends_ShouldReturnShared() throws Exception {
        long userA = createTestUser("a@example.com", "a", "A");
        long userB = createTestUser("b@example.com", "b", "B");
        long f1 = createTestUser("f1@example.com", "f1", "F1");
        long f2 = createTestUser("f2@example.com", "f2", "F2");
        long f3 = createTestUser("f3@example.com", "f3", "F3");

        // userA отправляет заявку f1 и f2
        mvc.perform(put("/users/" + userA + "/friends/" + f1)).andExpect(status().isOk());
        mvc.perform(put("/users/" + userA + "/friends/" + f2)).andExpect(status().isOk());

        // f1 и f2 подтверждают заявку userA
        mvc.perform(post("/users/" + f1 + "/friends/" + userA + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + f2 + "/friends/" + userA + "/confirm")).andExpect(status().isOk());

        // userB отправляет заявку f2 и f3
        mvc.perform(put("/users/" + userB + "/friends/" + f2)).andExpect(status().isOk());
        mvc.perform(put("/users/" + userB + "/friends/" + f3)).andExpect(status().isOk());

        // f2 и f3 подтверждают заявку userB
        mvc.perform(post("/users/" + f2 + "/friends/" + userB + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + f3 + "/friends/" + userB + "/confirm")).andExpect(status().isOk());

        // общий друг должен быть f2
        mvc.perform(get("/users/" + userA + "/friends/common/" + userB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == %d)]", f2).exists());
    }
}