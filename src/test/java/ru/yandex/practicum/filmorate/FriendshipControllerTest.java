package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.start.FinalProjectApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FinalProjectApplication.class)
@AutoConfigureMockMvc
public class FriendshipControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // Вспомогательный метод для создания тестового пользователя
    private long createTestUser(String email, String login, String name) throws Exception {
        String userJson = mapper.writeValueAsString(new ru.yandex.practicum.filmorate.model.User() {
            {
                setEmail(email);
                setLogin(login);
                setName(name);
                setBirthday(java.time.LocalDate.of(1990, 1, 1));
            }
        });

        String responseContent = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(responseContent, ru.yandex.practicum.filmorate.model.User.class).getId();
    }

    //добавление дружбы с несуществующим пользователем - должно вернуть 404
    @Test
    public void addFriendship_ShouldReturnNotFound_ForNonExistentUser() throws Exception {
        long existingUserId = createTestUser("existing@example.com", "existing", "Existing User");
        long nonExistentUserId = 9999L;

        mvc.perform(post("/users/" + existingUserId + "/friends/" + nonExistentUserId))
                .andExpect(status().isNotFound());
    }

    //подтверждение дружбы с несуществующим пользователем - должно вернуть 404
    @Test
    public void confirmFriendship_ShouldReturnNotFound_ForNonExistentUser() throws Exception {
        long userId = createTestUser("user@example.com", "user", "User");
        long nonExistentFriendId = 8888L;

        mvc.perform(post("/users/" + userId + "/friends/" + nonExistentFriendId + "/confirm"))
                .andExpect(status().isNotFound());
    }

    //добавление заявки в друзья, если уже друзья, должно вернуть 409 Conflict
    @Test
    public void addFriendship_ShouldReturnConflict_IfAlreadyFriends() throws Exception {
        long user1Id = createTestUser("user1@example.com", "user1", "User One");
        long user2Id = createTestUser("user2@example.com", "user2", "User Two");

        // Отправляем заявку
        mvc.perform(post("/users/" + user1Id + "/friends/" + user2Id))
                .andExpect(status().isOk());

        // Подтверждаем
        mvc.perform(post("/users/" + user1Id + "/friends/" + user2Id + "/confirm"))
                .andExpect(status().isOk());

        // Повторная заявка — ожидаем Conflict (409)
        mvc.perform(post("/users/" + user1Id + "/friends/" + user2Id))
                .andExpect(status().isConflict());
    }

    //удаление дружбы, если дружбы не было, возвращает 404
    @Test
    public void deleteFriend_ShouldReturnNotFound_IfNotFriends() throws Exception {
        long user1Id = createTestUser("userA@example.com", "userA", "User A");
        long user2Id = createTestUser("userB@example.com", "userB", "User B");

        // Попытка удалить несуществующую дружбу
        mvc.perform(delete("/users/" + user1Id + "/friends/" + user2Id))
                .andExpect(status().isNotFound());
    }

    // Получение друзей существующего пользователя
    @Test
    public void getFriends_ShouldReturnFriendsList() throws Exception {
        long userId = createTestUser("friendlist1@example.com", "friendlist1", "Friend List 1");
        long friendId1 = createTestUser("friend1@example.com", "friend1", "Friend 1");
        long friendId2 = createTestUser("friend2@example.com", "friend2", "Friend 2");

        // Отправляем заявки и подтверждаем дружбу
        mvc.perform(post("/users/" + userId + "/friends/" + friendId1)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friendId2)).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friendId1 + "/confirm")).andExpect(status().isOk());
        mvc.perform(post("/users/" + userId + "/friends/" + friendId2 + "/confirm")).andExpect(status().isOk());

        // Получаем список друзей
        mvc.perform(get("/users/" + userId + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasItems(friendId1, friendId2)));
    }

    // Получение друзей у несуществующего пользователя
    @Test
    public void getFriends_ShouldReturnNotFound_ForUnknownUser() throws Exception {
        long unknownUserId = 99999L;
        mvc.perform(get("/users/" + unknownUserId + "/friends"))
                .andExpect(status().isNotFound());
    }
}