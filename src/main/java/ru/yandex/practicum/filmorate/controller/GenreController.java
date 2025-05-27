package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreJdbcRepository;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreJdbcRepository genreRepository;

    @Autowired
    public GenreController(GenreJdbcRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenre(@PathVariable int id) {
        try {
            Genre genre = genreRepository.findById(id);
            return ResponseEntity.ok(genre);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

}

