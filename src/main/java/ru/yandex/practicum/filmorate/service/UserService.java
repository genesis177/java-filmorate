package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(User user) {
        return userStorage.add(user);
    }

    public Optional<User> updateUser(User user) {
        return userStorage.update(user);
    }

    public Optional<User> getById(Long id) {
        return userStorage.getById(id);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public void deleteUser(Long id) {
        userStorage.delete(id);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId).orElseThrow();
        User friend = userStorage.getById(friendId).orElseThrow();
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId).orElseThrow();
        user.getFriends().remove(friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.getById(userId).orElseThrow();
        return user.getFriends().stream()
                .map(fid -> userStorage.getById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> friends1 = new HashSet<>(userStorage.getById(userId).orElseThrow().getFriends());
        Set<Long> friends2 = new HashSet<>(userStorage.getById(otherId).orElseThrow().getFriends());
        friends1.retainAll(friends2);
        return friends1.stream()
                .map(fid -> userStorage.getById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void confirmFriendship(Long id, Long friendId) {
    }
}