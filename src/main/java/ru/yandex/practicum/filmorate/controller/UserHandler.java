package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserHandler {
    private final Map<Long, User> storage = new HashMap<>();
    private long generatedId = 0;

    public User create(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        user.setId(++generatedId);
        storage.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        if (user.getId() == null || !storage.containsKey(user.getId())) {
            throw new DataNotFoundException("Пользователь не найден");
        }
        storage.put(user.getId(), user);
        return user;
    }

    public List<User> getAll() {
        return new ArrayList<>(storage.values());
    }

    public User getById(Long id) {
        User user = storage.get(id);
        if (user == null) {
            throw new DataNotFoundException("Пользователь не найден");
        }
        return user;
    }
}