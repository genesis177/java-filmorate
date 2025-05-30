package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(1);

    @Override
    public Film add(Film film) {
        int id = idGen.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        if (!films.containsKey(film.getId())) return Optional.empty();
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

    @Override
    public void addLike(Integer filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Integer filmId, Long userId) {
        Film film = films.get(filmId);
        if (film != null) film.getLikes().remove(userId);
    }
}