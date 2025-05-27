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

    public User getUserById(Integer id) {
        return userStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    public User addUser(User user) {
        ValidationUtil.validateUser(user); // Вызов валидации
        return userStorage.add(user);
    }

    public Optional<User> updateUser(User user) {
        ValidationUtil.validateUser(user);
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = getUserById(userId);
        user.getFriends().remove(friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(Integer userId) {
        User user = getUserById(userId);
        List<User> friends = new ArrayList<>();
        for (Integer fid : user.getFriends()) {
            userStorage.getById(fid).ifPresent(friends::add);
        }
        return friends;
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);
        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(otherUser.getFriends());
        List<User> commonFriends = new ArrayList<>();
        for (Integer id : commonIds) {
            userStorage.getById(id).ifPresent(commonFriends::add);
        }
        return commonFriends;
    }

    public void sendFriendRequest(Integer userId, Integer friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user.getFriends().contains(friendId)) {
            throw new IllegalStateException("Пользователь уже в друзьях");
        }
        user.getFriends().add(friendId);
        updateUser(user);
    }

    public void confirmFriendship(Integer userId, Integer friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        updateUser(user);
        updateUser(friend);
    }
}
