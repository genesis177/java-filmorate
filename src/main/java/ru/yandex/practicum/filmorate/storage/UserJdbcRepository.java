package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class UserJdbcRepository implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO USERS (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), java.sql.Date.valueOf(user.getBirthday()));
        Integer id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM USERS", Integer.class);
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        String sql = "UPDATE USERS SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), java.sql.Date.valueOf(user.getBirthday()), user.getId());
        return rows > 0 ? Optional.of(user) : Optional.empty();
    }

    @Override
    public Optional<User> getById(Integer id) {
        try {
            String sql = "SELECT * FROM USERS WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM USERS";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    private final RowMapper<User> userRowMapper = new RowMapper<>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            // загрузка друзей
            String friendIdsStr = rs.getString("friends");
            if (friendIdsStr != null && !friendIdsStr.isBlank()) {
                String[] parts = friendIdsStr.split(",");
                Set<Integer> friends = new HashSet<>();
                for (String part : parts) {
                    friends.add(Integer.parseInt(part.trim()));
                }
                user.setFriends(friends);
            }
            return user;
        }
    };

}

