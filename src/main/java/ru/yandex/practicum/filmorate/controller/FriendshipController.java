package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FriendshipController {
    private final UserService userService;

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return ResponseEntity.ok(userService.getCommonFriends(id, otherId));
    }


    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriendPost(@PathVariable Long id, @PathVariable Long friendId) {
        return addFriend(id, friendId);
    }

    // ДОБАВЬТЕ ЭТО:
    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<Void> confirmFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.confirmFriendship(id, friendId);
        return ResponseEntity.ok().build();
    }
}