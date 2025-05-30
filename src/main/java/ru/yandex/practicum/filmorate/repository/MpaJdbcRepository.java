package ru.yandex.practicum.filmorate.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Repository
public class MpaJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> findAll() {
        String sql = "SELECT * FROM MPA";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Mpa findById(int id) {
        String sql = "SELECT * FROM MPA WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    private final RowMapper<Mpa> rowMapper = new RowMapper<>() {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }
    };
}

