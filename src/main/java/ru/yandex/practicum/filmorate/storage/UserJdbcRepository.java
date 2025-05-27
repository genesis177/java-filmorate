package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class UserJdbcRepository implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO USERS (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), java.sql.Date.valueOf(user.getBirthday()));
        Long id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class); // или "SELECT MAX(id)" в зависимости от базы
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
    public Optional<User> getById(Long id) {
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

    public void delete(Long id) {
        String sql = "DELETE FROM USERS WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        // если есть поле friends - оно должно быть сериализовано, либо отдельной таблицей
        return user;
    };
}