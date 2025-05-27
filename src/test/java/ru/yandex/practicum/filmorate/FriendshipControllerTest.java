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
public class FriendshipControllerTest {


    @Test
    public void addFriendship_ShouldReturnNotFound_ForNonExistentUser() throws Exception {
        // Предположим, что userId 9999 не существует
        long nonExistentUserId = 9999L;
        long existingUserId = createTestUser("existing@example.com", "existing", "Existing User");

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

        // Попытка снова отправить заявку — ожидаем Conflict (409)
        mvc.perform(post("/users/" + user1Id + "/friends/" + user2Id))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteFriend_ShouldReturnNotFound_IfNotFriends() throws Exception {
        long user1Id = createTestUser("userA@example.com", "userA", "User A");
        long user2Id = createTestUser("userB@example.com", "userB", "User B");

        // Попытка удалить дружбу, которой не существует
        mvc.perform(delete("/users/" + user1Id + "/friends/" + user2Id))
                .andExpect(status().isNotFound());
    }

    private long createTestUser(String email, String login, String name) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        String responseContent = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();

        User createdUser = mapper.readValue(responseContent, User.class);
        return createdUser.getId();
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;
}
