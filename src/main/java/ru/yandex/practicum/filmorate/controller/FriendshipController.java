package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users")
public class FriendshipController {
    private final UserService userService;
    private final FriendshipService friendshipService;

    @Autowired
    public FriendshipController(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    private void checkUserExists(Long userId) {
        if (!userService.existsById(userId)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
    }

    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        checkUserExists(id);
        checkUserExists(friendId);
        try {
            friendshipService.sendFriendRequest(id, friendId);
            return ResponseEntity.ok(Collections.singletonMap("message", "Заявка отправлена"));
        } catch (IllegalStateException e) {
            // если дружба уже есть или заявка отправлена, возвращаем 200 с сообщением
            return ResponseEntity.ok(Collections.singletonMap("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<Map<String, String>> confirmFriendship(@PathVariable Long id, @PathVariable Long friendId) {
        checkUserExists(id);
        checkUserExists(friendId);
        friendshipService.confirmFriendship(id, friendId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Дружба подтверждена"));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        checkUserExists(id);
        checkUserExists(friendId);
        try {
            friendshipService.removeFriend(id, friendId);
            return ResponseEntity.ok(Collections.singletonMap("message", "Дружба удалена"));
        } catch (IllegalStateException e) {
            throw e;
        }
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<Long>> getFriends(@PathVariable Long id) {
        checkUserExists(id);
        Set<Long> friends = friendshipService.getFriends(id);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/common-friends/{otherId}")
    public ResponseEntity<Set<Long>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        checkUserExists(id);
        checkUserExists(otherId);
        Set<Long> common = friendshipService.getCommonFriends(id, otherId);
        return ResponseEntity.ok(common);
    }
}