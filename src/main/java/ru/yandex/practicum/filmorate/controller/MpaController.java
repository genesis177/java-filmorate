package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaJdbcRepository;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private static final List<Mpa> MPAS = Arrays.asList(
            new Mpa(1, "G"),
            new Mpa(2, "PG"),
            new Mpa(3, "PG-13")
    );

    @GetMapping
    public List<Mpa> getAllMpa() {
        return MPAS;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getMpa(@PathVariable int id) {
        return MPAS.stream()
                .filter(m -> m.getId() == id)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
