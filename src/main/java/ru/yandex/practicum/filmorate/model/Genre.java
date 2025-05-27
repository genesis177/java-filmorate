package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Genre {
    private int id; // Идентификатор жанра
    private String name; // Название жанра

    public Genre() {
    }

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
