package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryUserStorage implements UserStorage {

    @Override
    public User add(User user) {
        int id = idCounter.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            return Optional.empty();
        }
        users.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> getById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
}

