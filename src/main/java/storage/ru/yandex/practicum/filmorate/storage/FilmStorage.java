package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Optional<Film> update(Film film);

    Optional<Film> getById(Integer id);

    List<Film> getAll();
}

