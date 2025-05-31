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

    /**
     * Проверяет, есть ли запись (заявка или дружба) между двумя пользователями в любом направлении
     */
    public boolean existsFriendship(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId, friendId, userId);
        return count != null && count > 0;
    }

    /**
     * Проверяет, есть ли заявка от fromUser к toUser (status = 'PENDING')
     */
    public boolean existsPendingRequest(Long fromUser, Long toUser) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fromUser, toUser);
        return count != null && count > 0;
    }

    /**
     * Проверяет, есть ли подтвержденная дружба между двумя пользователями (status = 'CONFIRMED' в любом направлении)
     */
    public boolean existsConfirmedFriendship(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) AND status = 'CONFIRMED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId, friendId, userId);
        return count != null && count > 0;
    }

    /**
     * Добавляет заявку в друзья (одна запись)
     */
    public void addFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (existsFriendship(userId, friendId)) {
            throw new IllegalStateException("Заявка или дружба уже существует");
        }
        String sql = "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, 'PENDING', ?)";
        jdbcTemplate.update(sql, userId, friendId, Timestamp.valueOf(LocalDateTime.now()));
    }

    /**
     * Подтверждает заявку: обновляет статус заявки с PENDING на CONFIRMED
     */
    public void confirmFriend(Long userId, Long friendId) {
        // Проверяем, что есть заявка от friendId к userId
        String checkSql = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);
        if (count == null || count == 0) {
            throw new IllegalStateException("Заявки нет");
        }
        // Обновляем статус заявки на CONFIRMED
        String updateSql = "UPDATE FRIENDS SET status = 'CONFIRMED', request_time = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(updateSql, Timestamp.valueOf(LocalDateTime.now()), friendId, userId);
    }

    /**
     * Удаляет дружбу или заявку (одна запись)
     */
    public void removeFriend(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        String sql = "DELETE FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    /**
     * Возвращает список id друзей пользователя (только подтвержденные дружбы)
     */
    public List<Long> getFriends(Long userId) {
        if (!userExists(userId)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        String sql = "SELECT CASE WHEN user_id = ? THEN friend_id ELSE user_id END AS friendId " +
                "FROM FRIENDS WHERE (user_id = ? OR friend_id = ?) AND status = 'CONFIRMED'";
        return jdbcTemplate.queryForList(sql, Long.class, userId, userId, userId);
    }

    /**
     * Возвращает список общих друзей (id) двух пользователей (только подтвержденные дружбы)
     */
    public List<Long> getCommonFriends(Long userId1, Long userId2) {
        if (!userExists(userId1) || !userExists(userId2)) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        String sql = "SELECT friendId FROM (" +
                "SELECT CASE WHEN user_id = ? THEN friend_id ELSE user_id END AS friendId " +
                "FROM FRIENDS WHERE (user_id = ? OR friend_id = ?) AND status = 'CONFIRMED'" +
                ") AS u1 INTERSECT " +
                "SELECT friendId FROM (" +
                "SELECT CASE WHEN user_id = ? THEN friend_id ELSE user_id END AS friendId " +
                "FROM FRIENDS WHERE (user_id = ? OR friend_id = ?) AND status = 'CONFIRMED'" +
                ")";
        return jdbcTemplate.queryForList(sql, Long.class, userId1, userId1, userId1, userId2, userId2, userId2);
    }
}