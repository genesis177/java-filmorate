package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserDto;
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

    public Optional<User> updateUser(User user) {
        return userStorage.update(user);
    }

    public Optional<User> getById(Long id) {
        Optional<User> userOpt = userStorage.getById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Set<Long> friendIds = friendshipService.getFriends(id);
            user.setFriends(friendIds);
            return Optional.of(user);  // вернуть пользователя с обновленными друзьями
        }
        return Optional.empty();
    }

    public User getUserWithoutFriends(Long userId) {
        return userStorage.getById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
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

    public List<User> getFriends(Long userId) {
        Set<Long> friendIds = friendshipService.getFriends(userId);
        return friendIds.stream()
                .map(fid -> userStorage.getById(fid).orElseThrow(() -> new NoSuchElementException("Пользователь не найден")))
                .collect(Collectors.toList());
    }

    public User getUserWithFriends(Long userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        Set<Long> friendIds = friendshipService.getFriends(userId);
        user.setFriends(friendIds);
        return user;
    }

    public UserDto getUserWithFriendsDto(Long userId, int depth) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        // Обновляем поле friends у user из friendshipService
        Set<Long> friendIds = friendshipService.getFriends(userId);
        user.setFriends(friendIds);
        return toUserDto(user, depth);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> commonFriendIds = friendshipService.getCommonFriends(userId, otherId);
        return commonFriendIds.stream()
                .map(fid -> userStorage.getById(fid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public UserDto toUserDto(User user, int depth) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());

        if (user.getFriends() != null) {
            dto.setFriends(user.getFriends().size());
        } else {
            dto.setFriends(0);
        }

        return dto;
    }
}