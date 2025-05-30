package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

public class ValidationUtil {
    public static void validateFilm(Film film, FilmService filmService) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не должно превышать 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Длительность должна быть положительным числом");
        }
        if (film.getMpa() == null || film.getMpa().getId() <= 0) {
            throw new ValidationException("Некорректный MPA");
        }
        if (film.getGenres() == null) {
            throw new ValidationException("Жанры не должны быть null");
        }
        if (filmService != null) {
            for (var genre : film.getGenres()) {
                if (!filmService.existsGenreById(genre.getId())) {
                    throw new ValidationException("Жанр с id " + genre.getId() + " не существует");
                }
            }
        }
    }

    public static void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Некорректный login");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Некорректная дата рождения");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}