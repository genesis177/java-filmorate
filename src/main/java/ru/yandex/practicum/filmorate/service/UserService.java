package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ru.yandex.practicum.filmorate.storage.UserStorage userStorage;
    private final FriendshipService friendshipService;

    public User addUser(User user) {
        return userStorage.add(user);
    }

    public User update(User user) {
        return userStorage.update(user)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public Optional<User> getById(Long id) {
        Optional<User> userOpt = userStorage.getById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Set<Long> friendIds = friendshipService.getFriends(id);
            user.setFriends(friendIds);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public List<User> getFriends(Long userId) {
        Set<Long> friendIds = friendshipService.getFriends(userId);
        return friendIds.stream()
                .map(fid -> userStorage.getById(fid).orElseThrow(() -> new NoSuchElementException("User not found")))
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