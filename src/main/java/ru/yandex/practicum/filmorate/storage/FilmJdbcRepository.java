package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
public class FilmJdbcRepository implements FilmStorage {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film add(Film film) {
        String sql = "INSERT INTO FILMS (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaId());

        // Получение последнего вставленного id
        Integer id = jdbcTemplate.queryForObject("VALUES IDENTITY()", Integer.class);
        film.setId(id);

        // Обработка жанров, если есть
        if (film.getGenres() != null) {
            for (Integer genreId : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)", id, genreId);
            }
        }
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaId(),
                film.getId());
        if (rows == 0) {
            return Optional.empty();
        }

        // обновляем жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Integer genreId : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), genreId);
            }
        }
        return Optional.of(film);
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

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getInt("mpa_id"));
        // загружаем жанры
        List<Integer> genreIds = jdbcTemplate.queryForList(
                "SELECT genre_id FROM film_genres WHERE film_id = ?", Integer.class, film.getId());
        if (!genreIds.isEmpty()) {
            film.setGenres(new HashSet<>(genreIds));
        } else {
            film.setGenres(new HashSet<>());
        }
        return film;
    };
}