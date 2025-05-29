package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.util.ValidationUtil;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController {


    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService; // бизнес-логика по фильмам
    private FriendshipService userService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    // Создать фильм
    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody Film film) {
        try {
            ValidationUtil.validateFilm(film);
            Film createdFilm = filmService.add(film);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
        } catch (AssertionError | IllegalArgumentException e) {
            log.error("Validation error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Unexpected error creating film", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Обновить фильм по id
    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable Integer id, @RequestBody Film film) {
        try {
            ValidationUtil.validateFilm(film);
            film.setId(id);
            Optional<Film> updatedFilm = filmService.updateFilm(film);
            return updatedFilm
                    .map(value -> ResponseEntity.status(HttpStatus.OK).body(value))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        boolean isFriendRemoved = userService.removeFriend(userId, friendId);
        if (isFriendRemoved) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

}