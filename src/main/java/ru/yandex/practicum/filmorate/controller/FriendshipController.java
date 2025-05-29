package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

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

    private boolean userExists(Long userId) {
        try {
            userService.getUserById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userExists(id) || !userExists(friendId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            friendshipService.sendFriendRequest(id, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            // Если дружба уже есть или заявка отправлена, возвращаем 200
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<Void> confirmFriendship(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userExists(id) || !userExists(friendId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            friendshipService.confirmFriendship(id, friendId);
            return ResponseEntity.ok().build(); // 200
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userExists(id) || !userExists(friendId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            friendshipService.removeFriend(id, friendId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<Long>> getFriends(@PathVariable Long id) {
        if (!userExists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            Set<Long> friends = friendshipService.getFriends(id);
            return ResponseEntity.ok(friends); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/common-friends/{otherId}")
    public ResponseEntity<Set<Long>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        if (!userExists(id) || !userExists(otherId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            Set<Long> commonFriends = friendshipService.getCommonFriends(id, otherId);
            return ResponseEntity.ok(commonFriends); // 200
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}