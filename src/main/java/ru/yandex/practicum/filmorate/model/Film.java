package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id; // Id фильма
    private String name;
    private String description; // Описание фильма
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate; // Дата релиза
    private Integer duration; // Продолжительность в минутах
    private Set<Integer> genres = new HashSet<>(); // Идентификаторы жанров
    private Integer mpaId; // рейтинг MPAA по id
    private Set<Long> likes = new HashSet<>(); // ID пользователей, поставивших лайк
}
