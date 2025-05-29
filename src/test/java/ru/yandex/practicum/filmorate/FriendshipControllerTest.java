package ru.yandex.practicum.filmorate;

import org.hamcrest.Matchers;
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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class FriendshipControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void cleanDatabase() {
    }

    public long createTestUser(String email, String name, String login) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2000, 1, 1)); // добавьте эту строку
        String response = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = mapper.readValue(response, User.class);
        return createdUser.getId();
    }

    @Test
    public void addFriend_ShouldReturn201() throws Exception {
        long userId1 = createTestUser("addfriend1@example.com", "add friend1", "Add Friend 1");
        long userId2 = createTestUser("addfriend2@example.com", "add friend2", "Add Friend 2");

        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isCreated());
    }

    @Test
    public void addFriend_ShouldReturnNotFound_ForUnknownUser() throws Exception {
        long userId = createTestUser("unknownadd@example.com", "unknown add", "Unknown Add");
        long unknownId = 9999L;

        mvc.perform(post("/users/" + userId + "/friends/" + unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFriends_ShouldReturnFriendsList() throws Exception {
        long userId = createTestUser("friendlist2@example.com", "friend list2", "Friend List 2");
        long friendId1 = createTestUser("friendA@example.com", "friendA", "Friend A");
        long friendId2 = createTestUser("friendB@example.com", "friendB", "Friend B");

        // Отправляем заявки и подтверждаем дружбу
        mvc.perform(post("/users/" + userId + "/friends/" + friendId1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friendId2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friendId1 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friendId2 + "/confirm")).andExpect(status().isOk());

        // Получаем список друзей
        mvc.perform(get("/users/" + userId + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", Matchers.containsInAnyOrder(friendId1, friendId2)));

    }

    @Test
    public void getFriends_ShouldReturnNotFound_ForUnknownUser() throws Exception {
        long unknownId = 99999L;
        mvc.perform(get("/users/" + unknownId + "/friends"))


                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFriend_ShouldWork() throws Exception {
        long userId1 = createTestUser("remove1@example.com", "remove1", "Remove 1");
        long userId2 = createTestUser("remove2@example.com", "remove2", "Remove 2");

        // Создаем дружбу
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId2 + "/friends/" + userId1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2 + "/confirm")).andExpect(status().isOk());

        // Удаляем дружбу
        mvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        // Проверяем, что друзья удалены
        mvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void removeFriend_ShouldReturnNotFound_IfNotFriends() throws Exception {
        long userId1 = createTestUser("notfriend1@example.com", "not friend1", "Not Friend 1");
        long userId2 = createTestUser("notfriend2@example.com", "not friend2", "Not Friend 2");

        // Попытка удалить дружбу, которой нет
        mvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFriend_ShouldRemoveBothDirections() throws Exception {
        long userId1 = createTestUser("recip1@example.com", "rec ip1", "Reciprocity 1");
        long userId2 = createTestUser("recip2@example.com", "rec ip2", "Reciprocity 2");

        // Создаем дружбу
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId2 + "/friends/" + userId1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId1 + "/friends/" + userId2 + "/confirm")).andExpect(status().isOk());

        // Удаляем дружбу
        mvc.perform(delete("/users/" + userId1 + "/friends/" + userId2))
                .andExpect(status().isOk());

        // Проверяем, что друзья удалены
        mvc.perform(get("/users/" + userId1 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mvc.perform(get("/users/" + userId2 + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void deleteFriend_ShouldReturnNotFound_ForUnknownFriend() throws Exception {
        long userId = createTestUser("unknownfriend1@example.com", "unknown friend1", "Unknown Friend 1");
        long unknownFriendId = 9999L;

        mvc.perform(delete("/users/" + userId + "/friends/" + unknownFriendId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteFriend_ShouldReturnNotFound_ForUnknownUser() throws Exception {
        long unknownUserId = 8888L;
        long friendId = createTestUser("friendforunknown@example.com", "friend unknown", "Friend Unknown");

        mvc.perform(delete("/users/" + unknownUserId + "/friends/" + friendId))
                .andExpect(status().isNotFound());
    }



    @Test
    public void getCommonFriends_ShouldReturnSharedFriends() throws Exception {
        long userA = createTestUser("a@example.com", "a", "A");
        long userB = createTestUser("b@example.com", "b", "B");
        long friend1 = createTestUser("friend1@example.com", "friend1", "Friend 1");
        long friend2 = createTestUser("friend2@example.com", "friend2", "Friend 2");
        long friend3 = createTestUser("friend3@example.com", "friend3", "Friend 3");

        int friend2Int = (int) friend2;

        // для userA
        mvc.perform(post("/users/" + userA + "/friends/" + friend1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userA + "/friends/" + friend2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userA + "/friends/" + friend1 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userA + "/friends/" + friend2 + "/confirm")).andExpect(status().isOk());

        // для userB
        mvc.perform(post("/users/" + userB + "/friends/" + friend2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userB + "/friends/" + friend3)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userB + "/friends/" + friend2 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userB + "/friends/" + friend3 + "/confirm")).andExpect(status().isOk());

        // получаем общих друзей
        mvc.perform(get("/users/" + userA + "/common-friends/" + userB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasItem(friend2Int)));
    }
}