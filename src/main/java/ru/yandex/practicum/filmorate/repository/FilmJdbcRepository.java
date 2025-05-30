package ru.yandex.practicum.filmorate.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class FilmJdbcRepository implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmJdbcRepository(JdbcTemplate jdbcTemplate, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO FILMS (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpaId());
            return ps;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(id);

        // Обработка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Integer genreId : film.getGenres()) {
                if (!genreStorage.existsById(genreId)) {
                    throw new IllegalArgumentException("Жанр с id " + genreId + " не существует");
                }
                jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)", id, genreId);
            }
        }
        // просто возвращайте переданный объект без вызова getById()
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

        // Обновляем жанры
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Integer genreId : film.getGenres()) {
                if (!genreStorage.existsById(genreId)) {
                    throw new IllegalArgumentException("Жанр с id " + genreId + " не существует");
                }
                jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)", film.getId(), genreId);
            }
        }
        return Optional.of(film);
    }

    @Override
    public Optional<Film> getById(Integer id) {
        try {
            String sql = "SELECT * FROM FILMS WHERE id = ?";
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            // загружаем жанры
            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM FILM_GENRES WHERE film_id = ?", Integer.class, id);
            if (genreIds != null) {
                film.setGenres(new HashSet<>(genreIds));
            }
            return Optional.ofNullable(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM FILMS";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        for (Film film : films) {
            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM FILM_GENRES WHERE film_id = ?", Integer.class, film.getId());
            if (genreIds != null) {
                film.setGenres(new HashSet<>(genreIds));
            } else {
                film.setGenres(new HashSet<>());
            }
        }
        return films;
    }

    @Override
    public Optional<Film> update() {
        return Optional.empty();
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpaId(rs.getInt("mpa_id"));
        // жанры загружены позже
        return film;
    };
}