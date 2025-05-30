package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(
            @Qualifier("filmJdbcRepository") FilmStorage filmStorage,
            GenreStorage genreStorage,
            MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film add(Film film) {
        film.setGenres(genreStorage.resolveGenres(film.getGenres()));
        film.setMpa(mpaStorage.getById(film.getMpa().getId()).orElseThrow());
        return filmStorage.add(film);
    }

    public Optional<Film> update(Film film) {
        film.setGenres(genreStorage.resolveGenres(film.getGenres()));
        film.setMpa(mpaStorage.getById(film.getMpa().getId()).orElseThrow());
        return filmStorage.update(film);
    }

    public Optional<Film> getById(Integer id) {
        return filmStorage.getById(id);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Integer filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public boolean existsGenreById(Integer genreId) {
        return genreStorage.existsById(genreId);
    }
}