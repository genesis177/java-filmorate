package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaJdbcRepository;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaJdbcRepository mpaRepository;

    @Autowired
    public MpaController(MpaJdbcRepository mpaRepository) {
        this.mpaRepository = mpaRepository;
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        return mpaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getMpa(@PathVariable int id) {
        try {
            Mpa mpa = mpaRepository.findById(id);
            return ResponseEntity.ok(mpa);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

}
