package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        return filmStorage.add(film);
    }

    public Optional<Film> updateFilm(Film film) {
        validateFilm(film);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        return filmStorage.update(film);
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Фильм не найден"));
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new IllegalArgumentException("Некорректное название");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            throw new IllegalArgumentException("Некорректное описание");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new IllegalArgumentException("Некорректная дата релиза");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new IllegalArgumentException("Некорректная продолжительность");
        }
    }
}