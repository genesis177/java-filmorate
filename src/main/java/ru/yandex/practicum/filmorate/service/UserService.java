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
    private final FriendshipService friendshipService;

    public User addUser(User user) {
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user)
                .orElseThrow(() -> new NoSuchElementException("User with id " + user.getId() + " not found"));
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

    // Получить список друзей пользователя (только подтвержденных)
    public List<User> getFriends(Long userId) {
        Set<Long> friendIds = friendshipService.getFriends(userId);
        return friendIds.stream()
                .map(fid -> userStorage.getById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Получить общих друзей двух пользователей
    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> commonFriendIds = friendshipService.getCommonFriends(userId, otherId);
        return commonFriendIds.stream()
                .map(fid -> userStorage.getById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}