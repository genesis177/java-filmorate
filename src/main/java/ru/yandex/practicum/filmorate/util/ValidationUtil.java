package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;

public class ValidationUtil {
    // Проверка корректности фильма
    public static void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new AssertionError("Введите название фильма");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new AssertionError("Описание не должно превышать 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new AssertionError("Некорректная дата релиза");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new AssertionError("Длительность должна быть положительной");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
    }

    // Проверка корректности пользователя
    public static void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new AssertionError("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new AssertionError("Некорректный login");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new AssertionError("Некорректная дата рождения");
        }
        // Если имя отсутствует или пустое, заполняем логином
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}