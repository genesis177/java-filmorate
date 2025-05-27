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

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody Film film) {
        try {
            ValidationUtil.validateFilm(film);
            Film created = filmService.addFilm(film);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (AssertionError | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable Integer id, @RequestBody Film film) {
        try {
            ValidationUtil.validateFilm(film);
            film.setId(id);
            return filmService.updateFilm(film)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (AssertionError | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilm(@PathVariable Integer id) {
        try {
            Film film = filmService.getFilmById(id);
            return ResponseEntity.ok(film);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = filmService.getAllFilms();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopular(@RequestParam(defaultValue = "10") int count) {
        List<Film> popularFilms = filmService.getPopularFilms(count);
        return ResponseEntity.ok(popularFilms);
    }
}