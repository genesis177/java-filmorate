package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Mpa {
    private int id; // Идентификатор рейтинга
    private String name; // Название рейтинга (G, PG и т.д.)

    public Mpa() {
    }

    public Mpa(int id, String name) {
        this.id = id;
        this.name = name;
    }
}