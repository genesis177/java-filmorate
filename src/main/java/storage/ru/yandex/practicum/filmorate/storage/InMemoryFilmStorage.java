package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    @Override
    public Film add(Film film) {
        int id = idCounter.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            return Optional.empty();
        }
        films.put(film.getId(), film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> getById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
}

