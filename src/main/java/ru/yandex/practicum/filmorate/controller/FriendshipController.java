package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final UserService userService;

    // Добавление заявки в друзья (POST)
    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        friendshipService.sendFriendRequest(id, friendId);
        return ResponseEntity.ok().build();
    }

    // Подтверждение заявки в друзья (POST)
    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<Void> confirmFriend(@PathVariable Long id, @PathVariable Long friendId) {
        friendshipService.confirmFriendship(id, friendId);
        return ResponseEntity.ok().build();
    }

    // Удаление из друзей (DELETE)
    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        try {
            friendshipService.removeFriend(id, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            if ("Дружба не найдена".equals(e.getMessage())) {
                // Возвращаем 200, если дружба не найдена
                return ResponseEntity.ok().build();
            }
            throw e;
        }
    }

    // Получить список друзей (GET)
    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long id) {
        List<User> friends = userService.getFriends(id);
        return ResponseEntity.ok(friends);
    }

    // Получить общих друзей (GET)
    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        return ResponseEntity.ok(commonFriends);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriendPut(@PathVariable Long id, @PathVariable Long friendId) {
        friendshipService.sendFriendRequest(id, friendId);
        return ResponseEntity.ok().build();
    }
}