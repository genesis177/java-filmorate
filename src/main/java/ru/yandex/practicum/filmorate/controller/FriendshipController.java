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

    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        friendshipService.sendFriendRequest(id, friendId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<Void> confirmFriend(@PathVariable Long id, @PathVariable Long friendId) {
        friendshipService.confirmFriendship(id, friendId);
        return ResponseEntity.ok().build();
    }

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

    @GetMapping("/{id}/friends")  // исправлено здесь
    public ResponseEntity<List<User>> getFriends(@PathVariable Long id) {
        List<User> friends = userService.getFriends(id);
        return ResponseEntity.ok(friends);
    }

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