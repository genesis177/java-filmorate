package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
public class FilmJdbcRepository implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO FILMS (name, description, releaseDate, duration, mpaId, genres) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpaId(),
                genresToString(film.getGenres()));
        Integer id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Integer.class);
        film.setId(id);
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE FILMS SET name = ?, description = ?, releaseDate = ?, duration = ?, mpaId = ?, genres = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpaId(),
                genresToString(film.getGenres()), film.getId());
        return rows > 0 ? Optional.of(film) : Optional.empty();
    }

    @Override
    public Optional<Film> getById(Integer id) {
        try {
            String sql = "SELECT * FROM FILMS WHERE id = ?";
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            return Optional.ofNullable(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM FILMS";
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    private String genresToString(Set<Integer> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Integer g : genres) {
            sb.append(g).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getInt("mpaId"));
        String genresStr = rs.getString("genres");
        if (genresStr != null && !genresStr.isBlank()) {
            String[] parts = genresStr.split(",");
            Set<Integer> genres = new HashSet<>();
            for (String part : parts) {
                genres.add(Integer.parseInt(part.trim()));
            }
            film.setGenres(genres);
        }
        return film;
    };
}