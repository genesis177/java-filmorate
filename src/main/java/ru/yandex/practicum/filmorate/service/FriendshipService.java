package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.repository.FriendshipJdbcRepository;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class FriendshipService {
    private final UserStorage userStorage;
    private final FriendshipJdbcRepository friendshipRepository;

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
        if (friendshipRepository.existsConfirmedFriendship(userId, friendId)) {
            throw new IllegalStateException("Дружба уже есть");
        }
        if (friendshipRepository.existsPendingRequest(userId, friendId)) {
            throw new IllegalStateException("Заявка уже отправлена");
        }
        friendshipRepository.addFriend(userId, friendId);
    }

    public void confirmFriendship(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        if (!friendshipRepository.existsPendingRequest(friendId, userId)) {
            throw new IllegalStateException("Заявки нет");
        }
        friendshipRepository.confirmFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        if (!friendshipRepository.existsConfirmedFriendship(userId, friendId)) {
            return;
        }
        friendshipRepository.removeFriend(userId, friendId);
    }

    public Set<Long> getFriends(Long userId) {
        checkUserExists(userId);
        return Set.copyOf(friendshipRepository.getFriends(userId));
    }

    public Set<Long> getCommonFriends(Long userId, Long otherId) {
        checkUserExists(userId);
        checkUserExists(otherId);
        return Set.copyOf(friendshipRepository.getCommonFriends(userId, otherId));
    }
}
