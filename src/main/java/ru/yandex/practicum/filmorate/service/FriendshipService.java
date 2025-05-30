package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.repository.FriendshipJdbcRepository;

import java.util.NoSuchElementException;

@Service
public class FriendshipService {
    private final UserStorage userStorage;
    private final FriendshipJdbcRepository friendshipRepository;

    @Autowired
    public FriendshipService(UserStorage userStorage, FriendshipJdbcRepository friendshipRepository) {
        this.userStorage = userStorage;
        this.friendshipRepository = friendshipRepository;
    }

    private void checkUserExists(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NoSuchElementException("Пользователь не найден");
        }
    }

    public void sendFriendRequest(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        if (friendshipRepository.existsFriendship(userId, friendId)) {
            // Можно просто ничего не делать или возвращать успех
            throw new IllegalStateException("Дружба уже есть");
        }
        friendshipRepository.addFriend(userId, friendId);
    }

    public void confirmFriendship(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        friendshipRepository.confirmFriend(userId, friendId);
    }

    public boolean removeFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        if (!friendshipRepository.existsFriendship(userId, friendId)) {
            throw new IllegalStateException("Дружба не найдена");
        }
        friendshipRepository.removeFriend(userId, friendId);
        return true; // или void
    }

    public java.util.Set<Long> getFriends(Long userId) {
        checkUserExists(userId);
        return new java.util.HashSet<>(friendshipRepository.getFriends(userId));
    }

    public java.util.Set<Long> getCommonFriends(Long userId, Long otherId) {
        checkUserExists(userId);
        checkUserExists(otherId);
        return new java.util.HashSet<>(friendshipRepository.getCommonFriends(userId, otherId));
    }


}