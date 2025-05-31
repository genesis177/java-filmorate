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

    public boolean existsFriendship(Long u1, Long u2) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, u1, u2, u2, u1);
        return cnt != null && cnt > 0;
    }

    public void addFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId))
            throw new IllegalArgumentException("Пользователь не найден");
        String sql = "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, "PENDING", Timestamp.valueOf(LocalDateTime.now()));
    }

    public void confirmFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId))
            throw new IllegalArgumentException("Пользователь не найден");
        // Проверяем заявку от friendId к userId
        String checkSql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status='PENDING'";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);
        if (count == null || count == 0)
            throw new IllegalStateException("Заявки нет");
        String sql = "UPDATE FRIENDS SET status='CONFIRMED' WHERE (user_id=? AND friend_id=?) OR (user_id=? AND friend_id=?)";
        jdbcTemplate.update(sql, friendId, userId, userId, friendId);
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