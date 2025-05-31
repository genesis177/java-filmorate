package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final UserService userService;

    // Отправка заявки в друзья (PUT или POST)
    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriendPut(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление друга: userId={}, friendId={}", id, friendId);
        friendshipService.sendFriendRequest(id, friendId);
        User user = userService.getUserWithFriends(id);  // <-- здесь
        log.info("Пользователь с друзьями: {}", user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriendPost(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление друга (POST): userId={}, friendId={}", id, friendId);
        friendshipService.sendFriendRequest(id, friendId);
        User friend = userService.getById(friendId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        log.info("Друг найден: {}", friend);
        return ResponseEntity.ok(friend);
    }

    // Подтверждение заявки в друзья (POST)
    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<User> confirmFriend(@PathVariable Long id, @PathVariable Long friendId) {
        friendshipService.confirmFriendship(id, friendId);
        User user = userService.getUserWithoutFriends(id);
        return ResponseEntity.ok(user);
    }

    // Удаление из друзей (DELETE)
    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        try {
            friendshipService.removeFriend(id, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            if ("Дружба не найдена".equals(e.getMessage())) {
                return ResponseEntity.ok().build();
            }
            throw e;
        }
    }

    // Получение списка друзей (GET)
    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long id) {
        List<User> friends = userService.getFriends(id);
        return ResponseEntity.ok(friends);
    }

    // Получение общих друзей (GET)
    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        return ResponseEntity.ok(commonFriends);
    }


}