package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.util.*;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getUserById(Long id) {
        return userStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    public User addUser(User user) {
        ValidationUtil.validateUser(user);
        return userStorage.add(user);
    }

    public Optional<User> updateUser(User user) {
        ValidationUtil.validateUser(user);
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователь уже в друзьях");
        }
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        user.getFriends().remove(friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        List<User> friends = new ArrayList<>();
        for (Long fid : user.getFriends()) {
            userStorage.getById(fid).ifPresent(friends::add);
        }
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);
        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(otherUser.getFriends());
        List<User> commonFriends = new ArrayList<>();
        for (Long id : commonIds) {
            userStorage.getById(id).ifPresent(commonFriends::add);
        }
        return commonFriends;
    }

    public void sendFriendRequest(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователь уже в друзьях");
        }
        user.getFriends().add(friendId);
        userStorage.update(user);
        // Можно добавить логику для заявок, если есть отдельное хранение
    }

    public void confirmFriendship(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public void deleteUser(Long id) {
    }
}