package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getLikes().contains(userId)) {
            throw new IllegalStateException("Пользователь уже поставил лайк");
        }
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = getFilmById(filmId);
        if (film.getLikes() != null && film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            filmStorage.update(film);
        } else {
            throw new NoSuchElementException("Лайк от пользователя не найден");
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> {
                    int size1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int size2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(size2, size1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Фильм не найден"));
    }

    public Film addFilm(Film film) {
        ValidationUtil.validateFilm(film);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        return filmStorage.add(film);
    }

    public Optional<Film> updateFilm(Film film) {
        ValidationUtil.validateFilm(film);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        return filmStorage.update(film);
    }

    // добавляем публичный метод
    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }
}

