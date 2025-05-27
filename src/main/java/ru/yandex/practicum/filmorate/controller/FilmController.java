package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService; // бизнес-логика по фильмам

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    // Создать фильм
    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody Film film) {
        try {
            ValidationUtil.validateFilm(film); // валидация входных данных
            Film created = filmService.addFilm(film); // добавляем в хранилище
            return ResponseEntity.status(HttpStatus.CREATED).body(created); // статус 201
        } catch (AssertionError | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // ошибка в данных
        }
    }

    // Обновить фильм по id
    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable Integer id, @RequestBody Film film) {
        try {
            ValidationUtil.validateFilm(film);
            film.setId(id);
            // Обновляем фильм, если есть, возвращаем 200, иначе 404
            return filmService.updateFilm(film)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (AssertionError | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Получить фильм по id
    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilm(@PathVariable Integer id) {
        try {
            Film film = filmService.getFilmById(id);
            return ResponseEntity.ok(film);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Получить все фильмы
    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = filmService.getAllFilms();
        return ResponseEntity.ok(films);
    }

    // Получить популярные фильмы (по лайкам)
    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopular(@RequestParam(defaultValue = "10") int count) {
        List<Film> popularFilms = filmService.getPopularFilms(count);
        return ResponseEntity.ok(popularFilms);
    }
}