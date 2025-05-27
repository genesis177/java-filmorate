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
        String userJson = mapper.writeValueAsString(new ru.yandex.practicum.filmorate.model.User() {{
            setEmail(email);
            setLogin(login);
            setName(name);
            setBirthday(java.time.LocalDate.of(1990, 1, 1));
        }});

        String responseContent = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return mapper.readValue(responseContent, ru.yandex.practicum.filmorate.model.User.class).getId();
    }

    @Test
    public void addFriendship_ShouldReturnNotFound_ForNonExistentUser() throws Exception {
        long existingUserId = createTestUser("existing@example.com", "existing", "Existing User");
        long nonExistentUserId = 9999L;

        mvc.perform(post("/users/" + existingUserId + "/friends/" + nonExistentUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void confirmFriendship_ShouldReturnNotFound_ForNonExistentUser() throws Exception {
        long userId = createTestUser("user@example.com", "user", "User");
        long nonExistentFriendId = 8888L;

        mvc.perform(post("/users/" + userId + "/friends/" + nonExistentFriendId + "/confirm"))
                .andExpect(status().isNotFound());
    }

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

    @Test
    public void deleteFriend_ShouldReturnNotFound_IfNotFriends() throws Exception {
        long user1Id = createTestUser("userA@example.com", "userA", "User A");
        long user2Id = createTestUser("userB@example.com", "userB", "User B");

        // Попытка удалить несуществующую дружбу
        mvc.perform(delete("/users/" + user1Id + "/friends/" + user2Id))
                .andExpect(status().isNotFound());
    }
}