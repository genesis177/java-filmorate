package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        // Добавляем друга
        mvc.perform(put("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        // Проверяем список друзей
        mvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(userId2));
    }

    @Test
    public void getCommonFriends_ShouldReturnShared() throws Exception {
        long userA = createTestUser("a@example.com", "a", "A");
        long userB = createTestUser("b@example.com", "b", "B");
        long f1 = createTestUser("f1@example.com", "f1", "F1");
        long f2 = createTestUser("f2@example.com", "f2", "F2");

        mvc.perform(put("/users/" + userA + "/friends/" + f1))
                .andExpect(status().isOk());
        mvc.perform(put("/users/" + userA + "/friends/" + f2))
                .andExpect(status().isOk());

        mvc.perform(put("/users/" + userB + "/friends/" + f2))
                .andExpect(status().isOk());

        mvc.perform(get("/users/" + userA + "/friends/common/" + userB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(f2));
    }

    @Test
    public void removeFriend_WhenNotFriends_ShouldReturn204() throws Exception {
        long userId1 = createTestUser("user1@example.com", "user1", "User1");
        long userId2 = createTestUser("user2@example.com", "user2", "User2");

        mvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isNoContent()); // Expects 204
    }
}