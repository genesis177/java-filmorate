package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserHandler userHandler;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userHandler.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userHandler.update(user);
    }

    @GetMapping
    public List<User> getAll() {
        return userHandler.getAll();
    }
}
