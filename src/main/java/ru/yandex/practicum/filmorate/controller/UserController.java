package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Integer id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> sendFriendRequest(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.sendFriendRequest(id, friendId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/friends/{friendId}/confirm")
    public ResponseEntity<Void> confirmFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.confirmFriendship(id, friendId);
        return ResponseEntity.ok().build();
    }
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        ValidationUtil.validateUser(user);
        User createdUser = userService.addUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        ValidationUtil.validateUser(user);
        User updated = userService.updateUser(user).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return ResponseEntity.ok(updated);
    }

}
