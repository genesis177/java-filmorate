package ru.yandex.practicum.filmorate.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@Primary
public class FilmJdbcRepository implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmJdbcRepository(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, MpaStorage mpaStorage) {
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
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Integer id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.existsById(genre.getId())) {
                    throw new GenreNotFoundException(genre.getId());
                }
                jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)", id, genre.getId());
            }
        }

        return getById(id).orElseThrow(() -> new RuntimeException("Film not found after insert"));
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rows == 0) {
            return Optional.empty();
        }

        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.existsById(genre.getId())) {
                    throw new GenreNotFoundException(genre.getId());
                }
                jdbcTemplate.update("INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)", film.getId(), genre.getId());
            }
        }

        return getById(film.getId());
    }

    @Override
    public Optional<Film> getById(Integer id) {
        try {
            String sql = "SELECT f.*, m.name as mpa_name FROM FILMS f JOIN MPA m ON f.mpa_id = m.id WHERE f.id = ?";
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);

            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM FILM_GENRES WHERE film_id = ?", Integer.class, id);
            Set<Genre> genres = new LinkedHashSet<>();
            for (Integer genreId : genreIds) {
                genreStorage.getById(genreId).ifPresent(genres::add);
            }
            film.setGenres(genres);

            List<Long> likes = jdbcTemplate.queryForList(
                    "SELECT user_id FROM FILM_LIKES WHERE film_id = ?", Long.class, id);
            film.setLikes(new HashSet<>(likes));

            return Optional.of(film);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, m.name as mpa_name FROM FILMS f JOIN MPA m ON f.mpa_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            List<Integer> genreIds = jdbcTemplate.queryForList(
                    "SELECT genre_id FROM FILM_GENRES WHERE film_id = ?", Integer.class, film.getId());
            Set<Genre> genres = new LinkedHashSet<>();
            for (Integer genreId : genreIds) {
                genreStorage.getById(genreId).ifPresent(genres::add);
            }
            film.setGenres(genres);

            List<Long> likes = jdbcTemplate.queryForList(
                    "SELECT user_id FROM FILM_LIKES WHERE film_id = ?", Long.class, film.getId());
            film.setLikes(new HashSet<>(likes));
        }
        return films;
    }

    @Override
    public void addLike(Integer filmId, Long userId) {
        jdbcTemplate.update("INSERT INTO FILM_LIKES (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    };
}