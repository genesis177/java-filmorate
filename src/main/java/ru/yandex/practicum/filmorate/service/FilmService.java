package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreService genreService;

    @Autowired
    public FilmService(FilmStorage filmStorage, GenreService genreService) {
        this.filmStorage = filmStorage;
        this.genreService = genreService;
    }

    // Проверка существования жанра
    public boolean existsGenreById(Integer genreId) {
        return genreService.existsById(genreId);
    }

    // Добавление фильма
    public Film add(Film film) {
        return filmStorage.add(film);
    }

    // Обновление фильма
    public Optional<Film> update(Film film) {
        return filmStorage.update(film);
    }

    // Получение фильма по id
    public Film getFilmById(Integer id) {
        return filmStorage.getById(id).orElseThrow(() -> new NoSuchElementException("Фильм не найден"));
    }

    // Получить все фильмы
    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    // Получить список популярных фильмов
    public List<Film> getPopularFilms(int count) {
        return getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList();
    }

    public interface GenreService {
        boolean existsById(Integer genreId);
    }
}