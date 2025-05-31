package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.repository.FriendshipJdbcRepository;

import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class FriendshipService {
    private final UserStorage userStorage;
    private final FriendshipJdbcRepository friendshipRepository;
    public static final Logger log = LoggerFactory.getLogger(FriendshipService.class);
    private final JdbcTemplate jdbcTemplate;

    public FriendshipService(UserStorage userStorage,
                             FriendshipJdbcRepository friendshipRepository,
                             JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.friendshipRepository = friendshipRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    private void checkUserExists(Long userId) {
        if (userStorage.getById(userId).isEmpty()) {
            throw new NoSuchElementException("Пользователь не найден");
        }
    }

    public void sendFriendRequest(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        jdbcTemplate.update(
                "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, 'CONFIRMED', CURRENT_TIMESTAMP)",
                userId, friendId
        );
        log.info("Добавление друга: userId={}, friendId={}", userId, friendId);
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

        // Only delete the friendship record from userId to friendId
        String sql = "DELETE FROM FRIENDS WHERE user_id = ? AND friend_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, userId, friendId);
        log.info("Deleted {} friendship records from {} to {}", rowsDeleted, userId, friendId);
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