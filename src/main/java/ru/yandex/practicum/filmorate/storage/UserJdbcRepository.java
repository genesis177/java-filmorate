package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
public class UserJdbcRepository implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User add(User user) {
        // Проверка существования пользователя с таким email (если есть)
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO USERS (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());

            if (user.getBirthday() != null) {
                ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
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
        return user;
    };


}
