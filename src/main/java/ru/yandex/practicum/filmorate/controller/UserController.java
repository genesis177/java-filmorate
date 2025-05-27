package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Простая проверка
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        long id = idGenerator.incrementAndGet();
        user.setId((int) id);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(id, user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!users.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        user.setId(Math.toIntExact(id));
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(id, user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        users.remove(id);
        return ResponseEntity.ok().build();
    }

    // Для тестов: очистка базы
    @PostMapping("/clear")
    public ResponseEntity<Void> clear() {
        users.clear();
        return ResponseEntity.ok().build();
    }
}
