package ru.yandex.practicum.filmorate.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Repository
@Primary
public class FriendshipJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean userExists(Long id) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        // Исправлено: возвращать true, если пользователь существует
        return count != null && count > 0;
    }

    public boolean existsFriendship(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId, friendId, userId);
        return count != null && count > 0;
    }

    public boolean existsPendingRequest(Long fromUser, Long toUser) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fromUser, toUser);
        return count != null && count > 0;
    }

    public boolean existsConfirmedFriendship(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) AND status = 'CONFIRMED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId, friendId, userId);
        return count != null && count > 0;
    }

    public void addFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (existsFriendship(userId, friendId)) {
            throw new IllegalStateException("Заявка или дружба уже существует");
        }
        String sql = "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, 'PENDING', ?)";
        int updated = jdbcTemplate.update(sql, userId, friendId, Timestamp.valueOf(LocalDateTime.now()));
        if (updated == 0) {
            throw new RuntimeException("Не удалось добавить заявку в друзья");
        }
    }

    public void confirmFriend(Long userId, Long friendId) {
        // Проверка наличия заявки от friendId к userId
        String checkSql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);
        if (count == null || count == 0) {
            throw new IllegalStateException("Заявки нет");
        }

        // Обновляем статус заявки на CONFIRMED
        String updateSql = "UPDATE FRIENDS SET status = 'CONFIRMED', request_time = ? WHERE user_id = ? AND friend_id = ?";
        int updated = jdbcTemplate.update(updateSql, Timestamp.valueOf(LocalDateTime.now()), friendId, userId);
        if (updated == 0) {
            throw new RuntimeException("Не удалось подтвердить заявку");
        }

    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        String sql = "DELETE FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    public List<Long> getFriends(Long userId) {
        if (!userExists(userId)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        String sql = "SELECT friend_id FROM FRIENDS WHERE user_id = ? AND status = 'CONFIRMED'";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    public List<Long> getCommonFriends(Long userId1, Long userId2) {
        if (!userExists(userId1) || !userExists(userId2)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        String sql = "SELECT friend_id FROM (" +
                "SELECT CASE WHEN user_id = ? THEN friend_id ELSE user_id END AS friend_id " +
                "FROM FRIENDS WHERE (user_id = ? OR friend_id = ?) AND status = 'CONFIRMED'" +
                ") AS u1 INTERSECT " +
                "SELECT friend_id FROM (" +
                "SELECT CASE WHEN user_id = ? THEN friend_id ELSE user_id END AS friend_id " +
                "FROM FRIENDS WHERE (user_id = ? OR friend_id = ?) AND status = 'CONFIRMED'" +
                ")";
        return jdbcTemplate.queryForList(sql, Long.class, userId1, userId1, userId1, userId2, userId2, userId2);
    }
}