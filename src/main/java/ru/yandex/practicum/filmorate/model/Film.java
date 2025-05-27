package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Integer> genres;
    private Integer mpaId;
    private Set<Integer> likes;

    // Валидация перемещена в отдельный сервис класс.
}

