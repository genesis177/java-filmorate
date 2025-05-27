package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Repository
public class GenreJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> findAll() {
        String sql = "SELECT * FROM GENRES";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Genre findById(int id) {
        String sql = "SELECT * FROM GENRES WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    private final RowMapper<Genre> rowMapper = new RowMapper<>() {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }
    };

}

