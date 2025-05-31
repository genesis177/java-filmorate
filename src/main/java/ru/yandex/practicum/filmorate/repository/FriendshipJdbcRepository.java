package ru.yandex.practicum.filmorate.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
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
        return count != null && count > 0;
    }

    public boolean existsConfirmedFriendship(Long u1, Long u2) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) AND status = 'CONFIRMED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, u1, u2, u2, u1);
        return count != null && count > 0;
    }

    public boolean existsPendingRequest(Long fromUser, Long toUser) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fromUser, toUser);
        return count != null && count > 0;
    }

    public void addFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId))
            throw new IllegalArgumentException("Пользователь не найден");
        String sql = "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, "PENDING", Timestamp.valueOf(LocalDateTime.now()));
    }

    public void confirmFriend(Long userId, Long friendId) {
        // Проверяем, что заявка есть от friendId к userId
        String checkSql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status='PENDING'";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);
        if (count == null || count == 0) {
            throw new IllegalStateException("Заявки нет");
        }
        // Обновляем статус заявки от friendId к userId
        String updateSql = "UPDATE FRIENDS SET status='CONFIRMED' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(updateSql, friendId, userId);

        // Создаем обратную запись, если ее нет или не подтверждена
        if (!existsConfirmedFriendship(userId, friendId)) {
            String insertSql = "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, friendId, "CONFIRMED", Timestamp.valueOf(LocalDateTime.now()));
        } else {
            // Обновляем статус обратной записи
            String updateReverseSql = "UPDATE FRIENDS SET status='CONFIRMED' WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(updateReverseSql, userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId))
            throw new IllegalArgumentException("Пользователь не найден");
        String sql = "DELETE FROM FRIENDS WHERE (user_id=? AND friend_id=?) OR (user_id=? AND friend_id=?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    public List<Long> getFriends(Long userId) {
        if (!userExists(userId))
            throw new NoSuchElementException("Пользователь не найден");
        String sql = "SELECT CASE WHEN user_id=? THEN friend_id ELSE user_id END AS friendId FROM FRIENDS WHERE (user_id=? OR friend_id=?) AND status='CONFIRMED'";
        return jdbcTemplate.queryForList(sql, Long.class, userId, userId, userId);
    }

    public List<Long> getCommonFriends(Long userId1, Long userId2) {
        if (!userExists(userId1) || !userExists(userId2))
            throw new NoSuchElementException("Пользователь не найден");
        String sql = "SELECT friendId FROM (" +
                "SELECT CASE WHEN user_id=? THEN friend_id ELSE user_id END AS friendId " +
                "FROM FRIENDS WHERE (user_id=? OR friend_id=?) AND status='CONFIRMED'" +
                ") AS u1 INTERSECT " +
                "SELECT friendId FROM (" +
                "SELECT CASE WHEN user_id=? THEN friend_id ELSE user_id END AS friendId " +
                "FROM FRIENDS WHERE (user_id=? OR friend_id=?) AND status='CONFIRMED'" +
                ")";
        return jdbcTemplate.queryForList(sql, Long.class, userId1, userId1, userId1, userId2, userId2, userId2);
    }
}