package ru.yandex.practicum.filmorate.controller;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Service
public class FilmHandler {
    private final Map<Long, Film> storage = new HashMap<>();
    private long generatedId = 0;

    public Film create(Film film) {
        film.setId(++generatedId);
        storage.put(film.getId(), film);
        return film;
    }

    public Film update(Film film) {
        if (film.getId() == null || !storage.containsKey(film.getId())) {
            throw new DataNotFoundException("Фильм не найден.");
        }
        storage.put(film.getId(), film);
        return film;
    }

    public List<Film> getAll() {
        return new ArrayList<>(storage.values());
    }
}
