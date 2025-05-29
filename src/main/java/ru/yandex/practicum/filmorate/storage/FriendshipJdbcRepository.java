package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
public class FriendshipJdbcRepository {
    private final JdbcTemplate jdbcTemplate;


    public FriendshipJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO FRIENDS (user_id, friend_id, status, request_time) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, "PENDING", Timestamp.valueOf(LocalDateTime.now()));
    }

    public void confirmFriend(Long userId, Long friendId) {
        String sqlCheck = "SELECT COUNT(*) FROM FRIENDS WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, userId, friendId);
        if (count == null || count == 0) {
            throw new IllegalStateException("Нет заявки");
        }
        String sqlUpdate = "UPDATE FRIENDS SET status = 'CONFIRMED' WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sqlUpdate, userId, friendId, friendId, userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    public List<Long> getFriends(Long userId) {
        String sql = "SELECT CASE WHEN user_id = ? THEN friend_id ELSE user_id END AS friendId FROM FRIENDS WHERE (user_id = ? OR friend_id = ?) AND status = 'CONFIRMED'";
        return jdbcTemplate.queryForList(sql, Long.class, userId, userId, userId);
    }

    public List<Long> getCommonFriends(Long userId1, Long userId2) {
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

    public Long add(User user) {
        String sql = "INSERT INTO USERS (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public boolean existsFriendship(Long u1, Long u2) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, u1, u2, u2, u1);
        return count != null && count > 0;
    }

    public boolean isConfirmed(Long u1, Long u2) {
        String sql = "SELECT COUNT(*) FROM FRIENDS WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) AND status = 'CONFIRMED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, u1, u2, u2, u1);
        return count != null && count > 0;
    }

    public boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
