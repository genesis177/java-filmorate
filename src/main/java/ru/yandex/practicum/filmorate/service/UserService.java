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
        Optional<User> userOpt = userStorage.getById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<User> friends = getFriends(id);
            user.setFriends(new HashSet<>(friends));
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public User getUserWithFriends(Long userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        List<User> friends = getFriends(userId);
        user.setFriends(new HashSet<>(friends));
        return user;
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public void deleteUser(Long id) {
        userStorage.delete(id);
    }

    public Set<Long> getFriendIds(Long userId) {
        return friendshipService.getFriends(userId);
    }

    // Если нужно получить объекты друзей, можно оставить этот метод
    public List<User> getFriends(Long userId) {
        Set<Long> friendIds = friendshipService.getFriends(userId);
        return friendIds.stream()
                .map(id -> userStorage.getById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> commonFriendIds = friendshipService.getCommonFriends(userId, otherId);
        return commonFriendIds.stream()
                .map(fid -> userStorage.getById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}