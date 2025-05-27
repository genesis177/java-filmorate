package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

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

    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userExists(id) || !userExists(friendId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            friendshipService.sendFriendRequest(id, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<?> confirmFriendship(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userExists(id) || !userExists(friendId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            friendshipService.confirmFriendship(id, friendId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userExists(id) || !userExists(friendId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            friendshipService.removeFriend(id, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private boolean userExists(Long userId) {
        try {
            userService.getUserById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}